package at.tugraz.alergia.active.strategy.value_iteration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.Property;
import at.tugraz.alergia.active.strategy.adversary.AdversaryI;
import at.tugraz.alergia.active.strategy.value_iteration.ReachPropertyComposedMDP.State;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;

public class ReachabilityChecker {
	private static final double epsilon = 1e-10;
	private MarkovChain<InputOutputStep> originalMdp = null;
	private Property property = null;
	private ReachPropertyComposedMDP boundedPropertyMDP = null;
	private Set<InputSymbol> inputs;

	public ReachabilityChecker(MarkovChain<InputOutputStep> originalMdp, Property property, Set<InputSymbol> inputs) {
		super();
		this.originalMdp = originalMdp;
		this.property = property;
		this.boundedPropertyMDP = new ReachPropertyComposedMDP(originalMdp, property, inputs);
		this.inputs = inputs;
	}

	public Pair<Double, AdversaryI> computeMinReachability() {
		if (boundedPropertyMDP.getTargetSet().isEmpty()) {
			Pair.of(0, new RandomisedAdversary());
		}
		boundedPropertyMDP.computeS0MinSet();
		boundedPropertyMDP.computeS1MinSet();

		Map<State, Double> xOfS = new HashMap<>();
		Set<State> sWithoutS0MinAndS1Min = new HashSet<>(boundedPropertyMDP.getStates());
		for (State s : boundedPropertyMDP.getStates()) {
			if (boundedPropertyMDP.getS1Min().contains(s)) {
				xOfS.put(s, 1.0);
				sWithoutS0MinAndS1Min.remove(s);
			} else
				xOfS.put(s, 0.0);
			if (boundedPropertyMDP.getS0Min().contains(s))
				sWithoutS0MinAndS1Min.remove(s);
		}

		int n = 0;
		double delta = -1.0;
		do {
//			HashMap<State, Double> xOfSPrime = new HashMap<>(xOfS);
			delta = -1.0;
			for (State s : sWithoutS0MinAndS1Min) {
				Pair<Double, InputSymbol> minXsAndSigmaS = findMinimalAction(s, xOfS);
				//
				if (minXsAndSigmaS.getLeft() - xOfS.get(s) > delta) {
					delta = minXsAndSigmaS.getLeft() - xOfS.get(s);
				}
				// xOfSPrime.put(s, minXsAndSigmaS.getLeft());
				xOfS.put(s, minXsAndSigmaS.getLeft());
			}
			// xOfS = xOfSPrime;
		} while (n++ <= property.getSteps()  && delta > epsilon);
		if (delta > epsilon)
			System.out.println(
					"WARNING: still significant changes before stopping after " + n + " rounds. Delta = " + delta);
		else
			System.out.println("Stopping after " + n + " rounds with delta=" + delta + ".");

		Map<State, InputSymbol> sigma = new HashMap<>();
		for (State s : boundedPropertyMDP.getStates()) {
			Pair<Double, InputSymbol> minXsAndSigmaS = findMinimalAction(s, xOfS);
			sigma.put(s, minXsAndSigmaS.getRight());
		}
		return Pair.of(xOfS.get(boundedPropertyMDP.getInitial()), new ValueIterAdversary(boundedPropertyMDP, sigma));
	}

	private Pair<Double, InputSymbol> findMinimalAction(State s, Map<State, Double> xOfS) {
		Double minXs = Double.POSITIVE_INFINITY;
		InputSymbol minInput = null;
		for (InputSymbol input : inputs) {
			Set<Pair<Double, State>> transitionsForInput = s.getTransitions(input.stringRepresentation());
			double xSPrime = 0;

			for (Pair<Double, State> t : transitionsForInput) {
				xSPrime += t.getLeft() * xOfS.get(t.getRight());
			}
			if (xSPrime < minXs) {
				minXs = xSPrime;
				minInput = input;
			}
		}
		return Pair.of(minXs, minInput);
	}

	public Pair<Double, AdversaryI> computeMaxReachability() {
		if (boundedPropertyMDP.getTargetSet().isEmpty()) {
			Pair.of(0, new RandomisedAdversary());
		}
		boundedPropertyMDP.computeS0MaxSet();
		Map<State, Double> xOfS = new HashMap<>();
		Map<State, InputSymbol> sigma = new HashMap<>();
		Set<State> sWithoutS0MaxAndT = new HashSet<>(boundedPropertyMDP.getStates());
		for (State s : boundedPropertyMDP.getStates()) {
			if (boundedPropertyMDP.getTargetSet().contains(s)) {
				xOfS.put(s, 1.0);
				sWithoutS0MaxAndT.remove(s);
			} else
				xOfS.put(s, 0.0);
			if (boundedPropertyMDP.getTargetSet().contains(s))
				sWithoutS0MaxAndT.remove(s);
			sigma.put(s, null);

		}
		int n = 0;
		// use changes flag instead of delta from original algorithm
		boolean noChanges = true;
		do {
			// HashMap<State,Double> xOfSPrime = new HashMap<>(xOfS);
			noChanges = true;
			for (State s : sWithoutS0MaxAndT) {
				Pair<Double, InputSymbol> maxXsAndSigmaS = findBestAction(s, xOfS);
				if (maxXsAndSigmaS.getLeft() > xOfS.get(s) || sigma.get(s) == null) {
					sigma.put(s, maxXsAndSigmaS.getRight());
					noChanges = false;
				}
				// other interpretation of xOfS update
				// xOfSPrime.put(s, maxXsAndSigmaS.getLeft());
				xOfS.put(s, maxXsAndSigmaS.getLeft());

			}

			// other interpretation of xOfS update
			// xOfS = xOfSPrime;

		} while (n++ <= property.getSteps() && !noChanges);
		if (!noChanges)
			System.out.println("WARNING: still changes before stopping after " + n + " rounds.");
		else
			System.out.println("Stopping after " + n + " rounds.");
		return Pair.of(xOfS.get(boundedPropertyMDP.getInitial()), new ValueIterAdversary(boundedPropertyMDP, sigma));
	}

	private Pair<Double, InputSymbol> findBestAction(State s, Map<State, Double> xOfS) {
		Double maxXs = -1.0;
		InputSymbol maxInput = null;
		for (InputSymbol input : inputs) {
			Set<Pair<Double, State>> transitionsForInput = s.getTransitions(input.stringRepresentation());
			double xSPrime = 0;

			for (Pair<Double, State> t : transitionsForInput) {
				xSPrime += t.getLeft() * xOfS.get(t.getRight());
			}
			if (xSPrime > maxXs) {
				maxXs = xSPrime;
				maxInput = input;
			}
		}
		return Pair.of(maxXs, maxInput);
	}

	public Pair<Double, AdversaryI> computeReachability() {
		if (property.isMax())
			return computeMaxReachability();
		else
			return computeMinReachability();
	}

}
