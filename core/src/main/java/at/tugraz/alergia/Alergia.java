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
package at.tugraz.alergia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.automata.states.McStateFactory;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.Step;
import at.tugraz.alergia.pta.PTANode;
import at.tugraz.alergia.pta.PTANodeFactory;
import at.tugraz.alergia.pta.PTATransition;
import at.tugraz.alergia.pta.PrefixTreeAcceptor;

public class Alergia<S extends Step> {

	private double epsilon;
	private boolean debug = false;
	private Optional<BiFunction<FiniteString<S>, FiniteString<S>, Boolean>> additionalCompatibilityCheck = 
			Optional.empty();
	private int maxDepthAddtional = 2;
	
	public Alergia(double epsilon){
		// todo make more general, encapsulate into more general parameter set for compatibility check
		this.epsilon = epsilon;
	}
	public Alergia(double epsilon, Optional<BiFunction<FiniteString<S>,FiniteString<S>,Boolean>> 
		additionalCompatibilityCheck){
		this.epsilon = epsilon;
		this.setAdditionalCompatibilityCheck(additionalCompatibilityCheck);
	}
	public PrefixTreeAcceptor<S> createPrefixTreeAcceptor(List<FiniteString<S>> sample,
			PTANodeFactory<S> nodeFactory){
		PrefixTreeAcceptor<S> pta = new PrefixTreeAcceptor<>();
		for(FiniteString<S> s : sample)
			pta.add(nodeFactory, s);
		return pta;
	}
	public MarkovChain<S> runAlergia(List<FiniteString<S>> sample,McStateFactory<S> stateFactory,
			PTANodeFactory<S> nodeFactory){
		PrefixTreeAcceptor<S> fpta = createPrefixTreeAcceptor(sample,nodeFactory);
		MarkovChain<S> mc = new MarkovChain<S>(fpta, stateFactory, new SampleData<S>(sample));

		McState<S> initState = mc.initialState().get(); // we know here that we have a deterministic MC
		ArrayList<McState<S>> red = new ArrayList<>(); // sorted list

		red.add(initState);
		List<McState<S>> blue = succs(red);
		System.out.println("Initial number of states: " + mc.getStates().size());
		while(!blue.isEmpty()){
			// straight-forward implementation following paper by Mao et al.
			McState<S> q_b = blue.get(0);
			boolean merged = false;
			Iterator<McState<S>> redIter = red.iterator();
			McState<S> compatibleState = null;
			while (redIter.hasNext()) {
				McState<S> q_r = redIter.next();
				if (compatible(fpta, q_r.getPtaNode(), q_b.getPtaNode(), mc, nodeFactory, epsilon,0)) {
					compatibleState = q_r; 
					break; // comment out to use ad-hoc-calculated certainty value
				}
			}
			if(compatibleState != null){
				
				merge(mc, compatibleState,q_b);
				merged = true;
				if(debug)
					System.out.println("Merged " + q_b.getId() + " into " +compatibleState.getId());
			}
			
			if(!merged){
				if(debug)
					System.out.println("Created red state " + q_b.getId());
				insertIntoSortedList(red,q_b);
			}
			// it's hard to do it more efficient than that because
			// you might think that you only need to consider successors of q_r (the target of a possible merge) or q_b
			// but this is not true because merging changes the structure in a way, such that nodes other than q_r/q_b
			// can get new successors (because the subtree rooted q_b is RECURSIVELY folded into q_r)
			blue = succs(red);
		
			if(debug)
				System.out.println("Blue now: " + 
					String.join(",", blue.stream().map(McState::getId).collect(Collectors.toList())));
		}
		System.out.println("System has " + mc.getStates().size() + " states");
		return normalize(mc);
	}

	private MarkovChain<S> normalize(MarkovChain<S> mc) {
		mc.getStates().forEach(s -> s.normalize(mc.getSampleData()));
		return mc;
	}
	
	protected void insertIntoSortedList(ArrayList<McState<S>> sortedList, McState<S> newState) {
		ListIterator<McState<S>> listIter = sortedList.listIterator();
		while(listIter.hasNext()){
			McState<S> currentElem = listIter.next();
			if(newState.compareTo(currentElem) < 0){
				listIter.previous();
				listIter.add(newState);
				return;
			}
		}
		sortedList.add(newState);
	}
	private void merge(MarkovChain<S> mc, McState<S> q_r, McState<S> q_b) {
		McTransition<S> incomingTrans = q_b.getUniqueIncomingTransition().get();
		q_b.invalidateUniqueIncomingTransition();
		incomingTrans.setTarget(q_r);
		fold(q_b,q_r, mc);
		mc.removeState(q_b);
	}
	private void fold(McState<S> q_b, McState<S> q_r, MarkovChain<S> mc) {
		for(McTransition<S> outgoingT : q_b.getTransitions()){
			Optional<McTransition<S>> matchtingTrans = 
					q_r.getTransitions().stream()
					.filter(t -> t.getStep().sameAs(outgoingT.getStep())) // same as to ignore time
					.findAny();
			if(matchtingTrans.isPresent()){
				McTransition<S> actualMatchingTrans = matchtingTrans.get();
				actualMatchingTrans.setProbability(
						actualMatchingTrans.getProbability() + outgoingT.getProbability());
				mc.removeState(outgoingT.getTarget());
				fold(outgoingT.getTarget(),actualMatchingTrans.getTarget(),mc);
			} else {
				// simply transit to successor of blue state
				McTransition<S> newTrans = q_r.addTransition(outgoingT.getTarget(), outgoingT.getProbability(), 
						outgoingT.getStep());
				outgoingT.getTarget().setUniqueIncomingTransition(newTrans);
				
			}
		}
	}
	private boolean compatible(PrefixTreeAcceptor<S> fpta, PTANode<S> q_b, PTANode<S> q_r, MarkovChain<S> mc, 
			PTANodeFactory<S> factory, double epsilon, int depth) {
		if(q_b.isEmpty() || q_r.isEmpty())
			return true;
//		if(q_b.transitionFrequency() > 1 && q_r.isEmpty())
//			return false;
//		if(q_b.isEmpty() && q_r.transitionFrequency() > 1)
//			return false;
		
		if(!q_b.getLabel().equals(q_r.getLabel()))
			return false;
		if(depth < getMaxDepthAddtional() && additionalCompatibilityCheck.isPresent() && 
				!additionalCompatibilityCheck.get().apply(q_b.getStringFromRoot(), q_r.getStringFromRoot())){
			return false;
		}
		if(!localCompatible(q_b,q_r, mc.getSampleData(),epsilon))
			return false;
		// account for MDPs, succ depends on both input and output
		for(S step: mc.getSampleData()){
			PTANode<S> q_bp = succ(q_b,step,factory);
			PTANode<S> q_rp = succ(q_r,step,factory); 
			if(!compatible(fpta, q_bp, q_rp, mc,factory,epsilon, depth + 1))
				return false;
		}
		return true;
	}
	
	private PTANode<S> succ(PTANode<S> q_b, S step, PTANodeFactory<S> factory) {
		PTATransition<S> succ = q_b.getSuccessor(step);

		return succ == null ? factory.empty(step.getOutputSymbol()) : succ.getTarget();
	}
	private boolean localCompatible(PTANode<S> q_b, PTANode<S> q_r, SampleData<S> possibleSteps, double epsilon) {
		return q_b.localCompatible(q_r,possibleSteps, epsilon);
	}
	// use list to sort states by id (lexicographical minimal elem should be chosen in each iteration 
	private List<McState<S>> succs(List<McState<S>> red) {
//		return red.stream().flatMap(mcState -> succsState(mcState))
//				.filter(s -> Collections.binarySearch(red, s) < 0)
//				.sorted()
//				.collect(Collectors.toList());
		Set<McState<S>> redSet = new HashSet<>(red); 
		Set<McState<S>> resultSet = new HashSet<>();
//		for(McState<S> r : red){
//			for(McTransition<S> t : r.getTransitions()){ // inline succsState
//				if(Collections.binarySearch(red, t.getTarget()) < 0 && 
//						Collections.binarySearch(result, t.getTarget()) <0)
//					insertIntoSortedList(result,t.getTarget());
//			}
//		}
		for(McState<S> r : red){
			for(McTransition<S> t : r.getTransitions()){ // inline succsState
				if(!redSet.contains(t.getTarget()))
					resultSet.add(t.getTarget());
			}
		}
		List<McState<S>> result = new ArrayList<>(resultSet);
		Collections.sort(result);
		return result;
	}

	private Stream<McState<S>> succsState(McState<S> mcS) {
		return mcS.getTransitions().stream().map(McTransition::getTarget);
		
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public Optional<BiFunction<FiniteString<S>, FiniteString<S>, Boolean>> getAdditionalCompatibilityCheck() {
		return additionalCompatibilityCheck;
	}
	public void setAdditionalCompatibilityCheck(Optional<BiFunction<FiniteString<S>, FiniteString<S>, Boolean>> additionalCompatibilityCheck) {
		this.additionalCompatibilityCheck = additionalCompatibilityCheck;
	}
	public int getMaxDepthAddtional() {
		return maxDepthAddtional;
	}
	public void setMaxDepthAddtional(int maxDepthAddtional) {
		this.maxDepthAddtional = maxDepthAddtional;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
}
