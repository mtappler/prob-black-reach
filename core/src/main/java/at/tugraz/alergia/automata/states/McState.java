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
package at.tugraz.alergia.automata.states;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;
import at.tugraz.alergia.pta.PTANode;

public abstract class McState<S extends Step> implements Comparable<McState<S>>{
	
	protected String id = "";
	protected OutputSymbol label = null;
	protected Set<McTransition<S>> transitions = null;
	private PTANode<S> ptaNode;
	private Optional<McTransition<S>> uniqueIncomingTransition = Optional.empty();
	private Integer intId;
	
	public Set<McTransition<S>> getTransitions() {
		return transitions;
	}

	// package protected
	McState(String id, OutputSymbol label, PTANode<S> ptaNode) {
		super();
		this.id = id;
		this.intId = new Integer(id.replace("s", ""));
		this.label = label;
		this.transitions = new HashSet<>();
		this.ptaNode = ptaNode;
	}
	public String getId() {
		return id;
	}
	// use with care
	public void setId(String id) {
		this.id = id;
		this.intId = new Integer(id.replace("s", ""));
	}	
	
	public McTransition<S> addTransition(McState<S> target, double probability, S step){
		McTransition<S> trans = new McTransition<S>(this, target, probability, step);
		transitions.add(trans);
		return trans;
	}
	// id suffices for checking equality
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		McState<S> other = (McState<S>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	// consistent with equals -> only checks id
	// maybe change, as s19 < s2 using this implementation (but lexicographical order is probably not that 
	// significant in Alergia)
	@Override
	public int compareTo(McState<S> o) {
		return intId.compareTo(o.intId);
	}

	public OutputSymbol getLabel() {
		return label;
	}

	public boolean isEmpty() {
		return "empty".equals(id);
	}

//	public abstract boolean localCompatible(McState<S> q_b, SampleData<S> sampleData, 
//			double epsilon);
	
	protected boolean hoeffdingTest(double epsilon, int n_1, int n_2, int f_1, int f_2) {
		if(n_1 == 0 || n_2 == 0)
			return true;
		boolean hoeffdingTest = Math.abs((double)f_1/n_1 - (double) f_2/n_2) <
				(Math.sqrt(1/(double)n_1) + Math.sqrt(1/(double)n_2)) * Math.sqrt(0.5 * Math.log(2/epsilon));
		return hoeffdingTest;
	}

	public Optional<McTransition<S>> getUniqueIncomingTransition() {
		return uniqueIncomingTransition;
	}

	public void setUniqueIncomingTransition(McTransition<S> uniqueIncomingTransition) {
		this.uniqueIncomingTransition = Optional.of(uniqueIncomingTransition);
	}
	public  void invalidateUniqueIncomingTransition(){
		this.uniqueIncomingTransition = Optional.empty();
	}

	public PTANode<S> getPtaNode() {
		return ptaNode;
	}

	public abstract void normalize(SampleData<S> sampleData);
}
