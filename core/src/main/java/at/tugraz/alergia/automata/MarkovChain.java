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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.automata.states.McStateFactory;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;
import at.tugraz.alergia.pta.PTANode;
import at.tugraz.alergia.pta.PTATransition;
import at.tugraz.alergia.pta.PrefixTreeAcceptor;
import at.tugraz.alergia.util.IdGen;
import at.tugraz.alergia.util.ProbabilityDistribution;

public  class MarkovChain<S extends Step> {
	
	private Set<McState<S>> states = null;
	private Set<OutputSymbol> outputAlphabet = null;
	private ProbabilityDistribution<McState<S>> initialProbDist = null;
	private SampleData<S> sampleData = null;
	private IdGen idGen = new IdGen();
	// moved to state class
//	private Map<State,ProbabilityDistribution<State>> transitionProbDist = null;
//	private Map<State,OutputSymbol> labellingFunction = null;
	
	
	public MarkovChain(Set<McState<S>> states, Set<OutputSymbol> outputAlphabet,
			ProbabilityDistribution<McState<S>> initialProbDist,
			Map<McState<S>, ProbabilityDistribution<McState<S>>> transitionProbDist, 
			Map<McState<S>, OutputSymbol> labellingFunction) {
		super();
		this.states = states;
		this.outputAlphabet = outputAlphabet;
		this.setInitialProbDist(initialProbDist);
	}
	public MarkovChain(Set<OutputSymbol> outputAlphabet, McState<S> initial) {
		this.states = new HashSet<>();
		this.outputAlphabet = outputAlphabet;
		this.setInitialProbDist(ProbabilityDistribution.singletonDistribution(initial));
	}
	public MarkovChain(PrefixTreeAcceptor<S> fpta, McStateFactory<S> stateFactory, 
			SampleData<S> initialSampleData) {
		PTANode<S> root = fpta.getRoot();

		McState<S> initState = stateFactory.create(idGen.next(), root.getLabel(), root);
		Map<PTANode<S>,McState<S>> ptaToMcState = new HashMap<>();
		states = new HashSet<>();
		outputAlphabet = new HashSet<>();
		sampleData = initialSampleData;
		
		ptaToMcState.put(root, initState);
		states.add(initState);
		ptaToMC(root, ptaToMcState, stateFactory);
		
		this.setInitialProbDist(ProbabilityDistribution.singletonDistribution(initState));
	}
	private void ptaToMC(PTANode<S> initNode, Map<PTANode<S>, McState<S>> ptaToMcState, 
			McStateFactory<S> stateFactory) {
		// make breadth-first exploration (does not matter actually, but to have low IDs near the initial state)
		LinkedList<PTANode<S>> schedule = new LinkedList<>();
		schedule.add(initNode);
		while(!schedule.isEmpty()){
			PTANode<S> currentNode = schedule.poll();
			McState<S> currentState = ptaToMcState.get(currentNode);
			// note that start symbol is included
			outputAlphabet.add(currentNode.getLabel()); 
			for(PTATransition<S> succ : currentNode.getSuccessors()){
				sampleData.add(succ.getStep());
				PTANode<S> succNode = succ.getTarget();
				
				McState<S> stateForTarget = stateFactory.create(idGen.next(), succNode.getLabel(),succNode);
				
				ptaToMcState.put(succNode, stateForTarget);
				states.add(stateForTarget);
				// TODO handle inputs/different types of steps
				McTransition<S> trans = currentState.addTransition(stateForTarget, succ.getFrequency(), succ.getStep());
				stateForTarget.setUniqueIncomingTransition(trans);
				schedule.add(succNode);
			}
		}
	}
	public Optional<McState<S>> initialState(){
		return getInitialProbDist().singleElem();
	}
	public Set<OutputSymbol> getOutputAlphabet() {
		return outputAlphabet;
	}
	public Set<McState<S>> getStates() {
		return states;
	}
	public SampleData<S> getSampleData() {
		return sampleData;
	}
	public void removeState(McState<S> q_b) {
		
		boolean removed = states.remove(q_b);
		if(!removed)
			System.out.println("weird");
	}
	public IdGen getIdGen() {
		return idGen;
	}	
	// use with care
	public void setIdGen(IdGen idGen) {
		this.idGen = idGen;
	}
	public ProbabilityDistribution<McState<S>> getInitialProbDist() {
		return initialProbDist;
	}
	public void setInitialProbDist(ProbabilityDistribution<McState<S>> initialProbDist) {
		this.initialProbDist = initialProbDist;
	}	
}
