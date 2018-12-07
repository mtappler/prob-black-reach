/*******************************************************************************
 * prob-black-reach
 * Copyright (C) 2017 TU Graz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package at.tugraz.alergia.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.automata.states.McStateFactory;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;
import at.tugraz.alergia.util.IdGen;
import at.tugraz.alergia.util.ProbabilityDistribution;

// TODO make smarter structure
public class McTransformer {

	// dontknowchaos is better as it does not inadvertently give probability to
	// unknown system actions
	private boolean dontKnowChaos = true;
	private OutputSymbol dontKnowLabel = OutputSymbol.dont_know;

	public <S extends Step> void reNameStateIds(MarkovChain<S> mc) {
		IdGen newIdGen = new IdGen();
		List<McState<S>> states = new ArrayList<>();
		// changing hash values of objects in a hash set is usually not a good
		// idea
		// so let's avoid doing that
		List<McState<S>> tempStateCollection = new ArrayList<>(mc.getStates());
		McState<S> initialState = mc.initialState().get();
		initialState.setId(newIdGen.next());
		states.add(initialState);
		for (McState<S> s : tempStateCollection) {
			if (s != initialState) {
				// let's hope that this does not break anything, but actually it
				// shouldn't
				// states contain a hashset with transitions and their hash
				// values depend on their reference values
				s.setId(newIdGen.next());
				states.add(s);
			}
		}
		mc.getStates().clear();
		mc.getStates().addAll(states);
		mc.setInitialProbDist(ProbabilityDistribution.singletonDistribution(initialState));

		mc.setIdGen(newIdGen);
	}

	public void completeModel(McStateFactory<InputOutputStep> stateFactory, MarkovChain<InputOutputStep> mc) {
		completeModel(stateFactory, mc, Optional.empty());
	}

	public void completeModel(McStateFactory<InputOutputStep> stateFactory, MarkovChain<InputOutputStep> mc,
			Optional<String> ignoreLabel) {
		Map<OutputSymbol, McState<InputOutputStep>> chaosStates = createChaosStates(stateFactory, mc, ignoreLabel);
		// assumption, mc is normalised, i.e. actual probabilities are attached
		// to transitions
		mc.getStates().forEach(s -> completeState(s, chaosStates, mc.getSampleData(), ignoreLabel));
	}

	private void completeState(McState<InputOutputStep> s, Map<OutputSymbol, McState<InputOutputStep>> chaosStates,
			SampleData<InputOutputStep> sampleData, Optional<String> ignoreLabel) {
		for (InputSymbol i : sampleData.getInputAlphabet()) {
			List<McTransition<InputOutputStep>> transForI = s.getTransitions().stream()
					.filter(t -> t.getStep().getInput().equals(i)).collect(Collectors.toList());
			// transitions to chaos states with uniform probability if we do not
			// have a transition for some input
			if (transForI.isEmpty()) {
				if (dontKnowChaos) {
					s.addTransition(chaosStates.get(dontKnowLabel), 1.0,
							new InputOutputStep(i, dontKnowLabel));
				} else {
					for (OutputSymbol o : sampleData.getOutputAlphabet()) {
						if (ignoreLabel.isPresent() && ignoreLabel.get().equals(o.stringRepresentation())) {
							continue;
						}
						if (ignoreLabel.isPresent()) {
							s.addTransition(chaosStates.get(o), 1.0 / (sampleData.getOutputAlphabet().size() - 1),
									new InputOutputStep(i, o));
						} else {
							s.addTransition(chaosStates.get(o), 1.0 / sampleData.getOutputAlphabet().size(),
									new InputOutputStep(i, o));
						}
					}
				}
			}
		}

		// TODO check if we need that or if we can account for that in the
		// strategy
		// for (InputSymbol i : sampleData.getInputAlphabet()) {
		// List<McTransition<InputOutputStep>> transForI =
		// s.getTransitions().stream()
		// .filter(t ->
		// t.getStep().getInput().equals(i)).collect(Collectors.toList());
		// if(!transForI.isEmpty()){
		// for (OutputSymbol o : sampleData.getOutputAlphabet()) {
		// if (!transForI.stream().anyMatch(t ->
		// t.getStep().getOutput().equals(o))) {
		// // maybe use an epsilon instead of 0 (adversary generation might
		// // ignore transitions with
		// // probability zero
		// s.addTransition(chaosStates.get(o), 0, new InputOutputStep(i, o));
		// }
		// }
		// }
		// }
	}

	private Map<OutputSymbol, McState<InputOutputStep>> createChaosStates(McStateFactory<InputOutputStep> stateFactory,
			MarkovChain<InputOutputStep> mc, Optional<String> ignoreLabel) {
		Map<OutputSymbol, McState<InputOutputStep>> chaosStates = new HashMap<>();
		// assumption, we have seen every relevant output during learning
		if (dontKnowChaos) {
			McState<InputOutputStep> newState = stateFactory.create(mc.getIdGen().next(), dontKnowLabel, null);
			chaosStates.put(dontKnowLabel, newState);
			// TODO refactor, that's not the nicest software design
			mc.getStates().add(newState);

			for (InputSymbol i : mc.getSampleData().getInputAlphabet()) {
				newState.addTransition(newState, 1, new InputOutputStep(i, dontKnowLabel));
			}
			if(!mc.getOutputAlphabet().contains(dontKnowLabel)){
				mc.getOutputAlphabet().add(dontKnowLabel);
				mc.getSampleData().getOutputAlphabet().add(dontKnowLabel);
			}

			return chaosStates;
		}

		for (OutputSymbol o : mc.getOutputAlphabet()) {
			if (!(ignoreLabel.isPresent() && ignoreLabel.get().equals(o.stringRepresentation()))) {
				// pass null intentionally, access of ptaNode of chaos states
				// should
				// not be silently ignored
				McState<InputOutputStep> newState = stateFactory.create(mc.getIdGen().next(), o, null);
				chaosStates.put(o, newState);
				// TODO refactor, that's not the nicest software design
				mc.getStates().add(newState);
			}
		}

		double transProb = ignoreLabel.isPresent() ? 1.0 / (mc.getOutputAlphabet().size() - 1)
				: 1.0 / mc.getOutputAlphabet().size();

		for (OutputSymbol sourceO : mc.getOutputAlphabet()) {
			if (!(ignoreLabel.isPresent() && ignoreLabel.get().equals(sourceO.stringRepresentation()))) {
				McState<InputOutputStep> sourceState = chaosStates.get(sourceO);
				for (InputSymbol i : mc.getSampleData().getInputAlphabet()) {
					for (OutputSymbol targetO : mc.getOutputAlphabet()) {
						if (!(ignoreLabel.isPresent() && ignoreLabel.get().equals(targetO.stringRepresentation())))
							sourceState.addTransition(chaosStates.get(targetO), transProb,
									new InputOutputStep(i, targetO));
					}
				}
			}
		}
		return chaosStates;
	}

	public boolean isDontKnowChaos() {
		return dontKnowChaos;
	}

	public void setDontKnowChaos(boolean dontKnowChaos) {
		this.dontKnowChaos = dontKnowChaos;
	}

	public void setDontKnowLabel(String label) {
		this.dontKnowLabel  = new OutputSymbol(label);
	}

}
