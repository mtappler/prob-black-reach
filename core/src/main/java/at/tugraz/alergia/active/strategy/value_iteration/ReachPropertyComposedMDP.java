package at.tugraz.alergia.active.strategy.value_iteration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;
import javax.print.attribute.HashAttributeSet;

import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.Property;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;

public class ReachPropertyComposedMDP {

	public static class State{
		private int stepCount = 0;
		private OutputSymbol output = null;
		private Map<String,Set<Pair<Double,State>>> transitions = new HashMap<>();
		public State(int stepCount, OutputSymbol output, McState<InputOutputStep> originalState) {
			super();
			this.stepCount = stepCount;
			this.output = output;
			this.originalState = originalState;
		}
		private McState<InputOutputStep> originalState = null;
		public Set<Pair<Double,State>> getTransitions(String input) {
			return transitions.get(input);
		}
		public String getLabel() {
			return output.stringRepresentation();
		}
	}
	
	private State initialState = null;
	private Set<State> states = new HashSet<>();
	private Property property;
	private Set<InputSymbol> inputs = null;
	private Set<State> s0Min;
	public Set<State> getStates() {
		return states;
	}


	public void setStates(Set<State> states) {
		this.states = states;
	}


	public Set<State> getS0Min() {
		return s0Min;
	}


	public void setS0Min(Set<State> s0Min) {
		this.s0Min = s0Min;
	}


	public Set<State> getS1Min() {
		return s1Min;
	}


	public void setS1Min(Set<State> s1Min) {
		this.s1Min = s1Min;
	}


	public Set<State> getS0Max() {
		return s0Max;
	}


	public void setS0Max(Set<State> s0Max) {
		this.s0Max = s0Max;
	}


	public Set<State> getS1Max() {
		return s1Max;
	}


	public void setS1Max(Set<State> s1Max) {
		this.s1Max = s1Max;
	}


	public Set<State> getTargetSet() {
		return targetSet;
	}


	public void setTargetSet(Set<State> targetSet) {
		this.targetSet = targetSet;
	}
	private Set<State> s1Min;
	private Set<State> s0Max;
	private Set<State> s1Max;
	private Set<State> targetSet;
	
	public ReachPropertyComposedMDP(MarkovChain<InputOutputStep> originalMdp,Property property, 
			Set<InputSymbol> inputs){
		Map<Pair<McState<InputOutputStep>,Integer>,State> orignalToCompState = new HashMap<>();
		McState<InputOutputStep> originalInit = originalMdp.getInitialProbDist().singleElem().get();
		initialState = new State(0, originalInit.getLabel(), originalInit);
		states.add(initialState);
		orignalToCompState.put(Pair.of(originalInit,0), initialState);
		this.property = property;
		this.inputs = inputs;
		transform(initialState,originalMdp,property.getSteps(), orignalToCompState);
		computeTargetSet();
	}


	public void computeS0MinSet() {
		if(s0Min != null)
			return;
		s0Min = computeS0Min();
	}
	public void computeS1MinSet() {
		if(s1Min != null)
			return;
		s1Min = computeS1Min();
	}

	public void computeS0MaxSet() {
		if(s0Max != null)
			return;
		s0Max = computeS0Max();
	}
	public void computeS1MaxSet() {
		if(s1Max != null)
			return;
		s1Max = computeS1Max();
	}
	private void computeTargetSet() {
		Set<State> target = new HashSet<>();
		for(State s : states){
			boolean satisfied = true;

			// assume all properties use strict inequalities
			if(s.stepCount >= property.getSteps())
				continue;
			for(String subProp : property.getLabels()){
				 if(!s.output.getSatisfiedProps().contains(subProp)){
					 satisfied = false;
					 break;
				 }
			}
			if(satisfied)
				target.add(s);
		}
		targetSet = target;
	}
	private Set<State> computeS0Min() {
		Set<State> R = new HashSet<>(targetSet);
		do{
			int sizeRBefore = R.size();
			Set<State> newStates = new HashSet<>();
			for(State s : states){
				if(canEnterWithAllInputs(s,R)){
					newStates.add(s);
				}
			}
			R.addAll(newStates);
			if(sizeRBefore == R.size())
				break;
		} while(true);
		return diff(states,R);
	}
	private Set<State> diff(Set<State> left, Set<State> right) {
		Set<State> result = new HashSet<>(left);
		result.removeAll(right);
		return result;
	}
	private boolean canEnterWithAllInputs(State s, Set<State> r) {
		boolean result = true;
		for(InputSymbol input : inputs){
			boolean canEnterWithCurrentInput = false;
			Set<Pair<Double, State>> succForInputs = s.transitions.get(input.stringRepresentation());
			for(Pair<Double, State> succ : succForInputs){
				if(r.contains(succ.getRight())){
					canEnterWithCurrentInput = true;
					break;
				}
			}
			result &= canEnterWithCurrentInput;
			if(!result)
				return result;
		}
	
		return result;
	}
	private Set<State> computeS1Min() {
		computeS0Min();
		Set<State> R = new HashSet<>(states);
		R.removeAll(s0Min);
		do{
			int sizeRBefore = R.size();
			Set<State> statesToRemove = new HashSet<>();
			for(State s: R){
				// TODO check this: this is not in the reference algorithm, but I think it needs to be here
				if(targetSet.contains(s))
					continue;
				inputLoop:for(InputSymbol input : inputs){
					Set<Pair<Double, State>> succForInputs = s.transitions.get(input.stringRepresentation());
					for(Pair<Double, State> succ : succForInputs){
						if(!R.contains(succ.getRight())){
							statesToRemove.add(s);
							break inputLoop;
						}
					}
				}
			}
			R.removeAll(statesToRemove);
			if(sizeRBefore == R.size()){
				break;
			}

		}while(true);
		return R;
	}
	
	private Set<State> computeS0Max() {
		Set<State> R = new HashSet<>(targetSet);
		do{
			int sizeRBefore = R.size();
			Set<State> newStates = new HashSet<>();
			for(State s : states){
				if(canEnterWithAnyInputs(s,R)){
					newStates.add(s);
				}
			}
			R.addAll(newStates);
			if(sizeRBefore == R.size())
				break;
		} while(true);
		return diff(states,R);
	}
	private boolean canEnterWithAnyInputs(State s, Set<State> r) {
		for(Set<Pair<Double, State>> succForInputs: s.transitions.values()){
			for(Pair<Double, State> succ : succForInputs)
				if(r.contains(succ.getRight()))
					return true;
		}
		return false;
	}
	private Set<State> computeS1Max() {
		Set<State> R = new HashSet<State>(states);
		
		do{
			Set<State> Rprime = new HashSet<>(R);
			Set<State> Rpp = new HashSet<>(targetSet);
			do{
				int sizeRinnerBefore = Rpp.size();
				Set<State> newStatesInner = new HashSet<>();
				for(State s : states){
					if(s1MaxCondition(s,Rprime,Rpp)){
						newStatesInner.add(s);
					}
				}
				Rpp.addAll(newStatesInner);
				if(sizeRinnerBefore == Rpp.size()){
					R = Rpp;
					break;
				}
			} while(true);
			if(Rprime.size() == R.size())
				break;
		}while(true);
		return R;
	}
	
	
	private boolean s1MaxCondition(State s, Set<State> rprime, Set<State> rpp) {
		for(InputSymbol input : inputs){
			if(s1MaxConditionForInput(s,input, rprime, rpp))
				return true;
		}
		return false;
	}
	private boolean s1MaxConditionForInput(State s, InputSymbol input, Set<State> rprime, Set<State> rpp) {
		boolean cond1 = true;
		Set<Pair<Double, State>> transForInputs = s.transitions.get(input.stringRepresentation()); // assume input-enabled
		boolean cond2 = false;
		for(Pair<Double, State> sp : transForInputs){ // only positive trans prob in succ Set 
			cond1 &= rprime.contains(sp.getRight());
			if(!cond1)
				return false;
			cond2 |= rpp.contains(sp);
		}
		return cond2;
	}
	private void transform(State current, MarkovChain<InputOutputStep> originalMdp, int propertyBound,
			Map<Pair<McState<InputOutputStep>, Integer>, State> orignalToCompState) {
		McState<InputOutputStep> originalCurrent = current.originalState;

		// assume all properties use strict inequalities
		int nextStepCount = current.stepCount >= propertyBound ? current.stepCount : current.stepCount+1;
		for(McTransition<InputOutputStep> succ : originalCurrent.getTransitions()){
			InputSymbol input = succ.getStep().getInput();
			double transProb = succ.getProbability();
			McState<InputOutputStep> originalNextState = succ.getTarget();
			Pair<McState<InputOutputStep>,Integer> origNextStepCounter = 
					Pair.of(originalNextState, nextStepCount);
			
			State nextState = orignalToCompState.get(origNextStepCounter);
			if(nextState == null){
				nextState = new State(nextStepCount, originalNextState.getLabel(), originalNextState);
				states.add(nextState);
				orignalToCompState.put(origNextStepCounter, nextState);
				addSuccessor(current, input,transProb, nextState);
				transform(nextState, originalMdp, propertyBound, orignalToCompState);
			} else {
				addSuccessor(current, input, transProb, nextState);
			} 
		}
	}
	private void addSuccessor(State current, InputSymbol input, double transProb, State nextState) {
		Set<Pair<Double,State>> succsForInput = current.transitions.get(input.stringRepresentation());
		if(succsForInput == null)
			succsForInput = new HashSet<>();
		current.transitions.put(input.stringRepresentation(), succsForInput);
		succsForInput.add(Pair.of(transProb, nextState));
	}


	public State getInitial() {
		return initialState;
	}
}
