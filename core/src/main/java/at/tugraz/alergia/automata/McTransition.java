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

import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.data.Step;

public class McTransition<S extends Step> {

	private McState<S> source = null;
	private McState<S> target = null;
	private double probability = 0.0;
	private boolean normalized = false; // if normalized == false, probability = frequency
	private S step = null;
	
	public McTransition(McState<S> source, McState<S> target, double probability, S step) {
		super();
		this.source = source;
		this.target = target;
		this.probability = probability;
		this.step  = step;
	}
	
	public McState<S> getSource() {
		return source;
	}
	public void setSource(McState<S> source) {
		this.source = source;
	}
	public McState<S> getTarget() {
		return target;
	}
	public void setTarget(McState<S> target) {
		this.target = target;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}

	public boolean isNormalized() {
		return normalized;
	}

	public void setNormalized(boolean normalized) {
		this.normalized = normalized;
	}

	public S getStep() {
		return step;
	}
}
