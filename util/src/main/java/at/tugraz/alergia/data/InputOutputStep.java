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

import java.util.Optional;

public class InputOutputStep implements Step {

	@Override
	public String toString() {
		return "InputOutputStep [input=" + input + ", output=" + output + "]";
	}
	private InputSymbol input = null;
	public InputOutputStep(InputSymbol input, OutputSymbol output) {
		super();
		this.input = input;
		this.output = output;
	}
	public InputSymbol getInput() {
		return input;
	}
	public void setInput(InputSymbol input) {
		this.input = input;
	}
	public OutputSymbol getOutput() {
		return output;
	}
	public void setOutput(OutputSymbol output) {
		this.output = output;
	}
	private OutputSymbol output = null;
	@Override
	public OutputSymbol getOutputSymbol() {
		return output;
	}
	@Override
	public boolean sameAs(Step other) {
		if(other instanceof InputOutputStep == false){
			return false;
		}else {
			InputOutputStep otherIOstep = (InputOutputStep) other;
			return otherIOstep.input.equals(this.input) && otherIOstep.output.equals(this.output);
		}
	}
	@Override
	public Optional<String> additionalStepLabel() {
		return Optional.of(input.stringRepresentation());
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
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
		InputOutputStep other = (InputOutputStep) obj;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (output == null) {
			if (other.output != null)
				return false;
		} else if (!output.equals(other.output))
			return false;
		return true;
	}
	
}
