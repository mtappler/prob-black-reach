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
package at.tugraz.alergia.data;

public class TimeStep implements Step{
	private double delay = 0.0;
	private OutputSymbol output = null;
	
	public TimeStep(double delay, OutputSymbol output) {
		super();
		this.delay = delay;
		this.output = output;
	}
	
	public double getDelay() {
		return delay;
	}
	public void setDelay(double delay) {
		this.delay = delay;
	}
	public OutputSymbol getOutput() {
		return output;
	}
	
	public void setOutput(OutputSymbol output) {
		this.output = output;
	}

	@Override
	public OutputSymbol getOutputSymbol() {
		return output;
	}

	@Override
	public boolean sameAs(Step other) {
		if(other instanceof TimeStep == false){
			return false;
		}
		else{
			TimeStep otherTimeStep = (TimeStep) other;
			return otherTimeStep.output.equals(this.output);
		}
	}
}
