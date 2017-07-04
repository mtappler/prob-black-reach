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

public class OutputStep implements Step {
	private OutputSymbol output = null;

	public OutputStep(OutputSymbol output) {
		super();
		this.output = output;
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
		if(other instanceof OutputStep == false){
			return false;
		}
		else{
			OutputStep otherOutStep = (OutputStep) other;
			return otherOutStep.output.equals(this.output);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((output == null) ? 0 : output.hashCode());
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
		OutputStep other = (OutputStep) obj;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		return true;
	}	
}
