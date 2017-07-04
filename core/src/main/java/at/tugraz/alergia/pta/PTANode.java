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
package at.tugraz.alergia.pta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;

public abstract class PTANode<S extends Step> {
	private OutputSymbol label = null;
	private PTATransition<S> incomingTransition = null;
	private Map<S,PTATransition<S>> successors = new HashMap<>();
	private FiniteString<S> stringFromRoot = null;
	private boolean empty = false; 

	public OutputSymbol getLabel() {
		return label;
	}

	public void setLabel(OutputSymbol label) {
		this.label = label;
	}

	public PTANode(OutputSymbol label, PTATransition<S> incomingTransition) {
		super();
		this.label = label;
		this.incomingTransition = incomingTransition;
	}
	public Collection<PTATransition<S>> getSuccessors(){
		return successors.values();
	}

	@Deprecated
	public int transitionFrequency(OutputSymbol outputSymbol){
		for(PTATransition<S> t : successors.values()){
			if(t.getTarget().getLabel().equals(outputSymbol)){
				return t.getFrequency();
			}
		}
		return 0;
	}
	// sum_{\sigma \in \Sigma_out} f(q_this,\sigma}
	public int transitionFrequency(){
		return successors.values().stream().mapToInt(t -> this.transitionFrequency(t.getTarget().getLabel())).sum();		
	}

	public PTANode<S> addSuccessor(PTANodeFactory<S> factory,S step) {
		PTATransition<S> succ = getSuccessor(step);
		if(succ != null){
			succ.incrementFequency();
			return succ.getTarget();
		}
		
		PTATransition<S> transToSucc = new PTATransition<S>(factory,this, 1, step);
		successors.put(step,transToSucc);

		return transToSucc.getTarget();
	}

	protected boolean hoeffdingTest(double epsilon, int n_1, int n_2, int f_1, int f_2) {
		if(n_1 == 0 || n_2 == 0)
			return true;
		double lhs = Math.abs((double)f_1/n_1 - (double) f_2/n_2);
		double rhs = (Math.sqrt(1/(double)n_1) + Math.sqrt(1/(double)n_2)) * Math.sqrt(0.5 * Math.log(2/epsilon));
		boolean hoeffdingTest = lhs < rhs;
		return hoeffdingTest;
	}
	public abstract boolean localCompatible(PTANode<S> q_b, SampleData<S> sampleData, 
			double epsilon);
	public FiniteString<S> getStringFromRoot() {
		if(stringFromRoot == null){
			List<S> stringFromRootTemp = new ArrayList<>();
			PTATransition<S> incomingTransitionIterator = incomingTransition;
			OutputSymbol rootOutput = null;
			while(true){ // note: we do not include the start symbol
				stringFromRootTemp.add(incomingTransitionIterator.getStep());
				PTATransition<S> nextTransition = incomingTransitionIterator.getSource().incomingTransition;
				if(nextTransition == null){
					rootOutput = incomingTransitionIterator.getSource().getLabel();
					break;
				} else {
					incomingTransitionIterator = nextTransition;
				}
			}
			Collections.reverse(stringFromRootTemp);
			stringFromRoot = new FiniteString<>(stringFromRootTemp,rootOutput);
		}
		return stringFromRoot;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public PTATransition<S> getSuccessor(S step) {
		return successors.get(step);
	}
}
