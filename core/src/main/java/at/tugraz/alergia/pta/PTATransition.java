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

import at.tugraz.alergia.data.Step;

public class PTATransition<S extends Step> {

	private PTANode<S> source = null;
	private PTANode<S> target = null;
	private int frequency = 0;
	private S step;
	public PTATransition(PTANodeFactory<S> factory, PTANode<S> source, int frequency, S step) {
		super();
		this.source = source;
		this.target = factory.create(step.getOutputSymbol(), this);
		this.frequency = frequency;
		this.step = step;
	}
	public PTANode<S> getSource() {
		return source;
	}
	public void setSource(PTANode<S> source) {
		this.source = source;
	}
	public PTANode<S> getTarget() {
		return target;
	}
	public void setTarget(PTANode<S> target) {
		this.target = target;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	public void incrementFequency() {
		frequency ++;
	}
	public boolean isSameStep(S step) {
		return this.step.sameAs(step);
	}
	public String label(){
		return step.additionalStepLabel().map(s -> s + ":").orElse("") + frequency;
	}
	public S getStep() {
		return step;
	}
}
