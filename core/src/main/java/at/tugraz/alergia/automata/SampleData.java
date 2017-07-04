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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;


public class SampleData<S extends Step> implements Iterable<S>{
	private Set<S> possibleSteps = new HashSet<>();
	private List<FiniteString<S>> sample;
	private Set<OutputSymbol> outputAlphabet = null;
	private Set<InputSymbol> inputAlphabet = null; // TODO change when working iteratively

	public SampleData(List<FiniteString<S>> sample) {
		this.sample = sample;
	}

	public void add(S step) {
		possibleSteps.add(step);
	}

	@Override
	public Iterator<S> iterator() {
		return possibleSteps.iterator();
	}

	public Set<OutputSymbol> getOutputAlphabet() {
		if(outputAlphabet == null)
			outputAlphabet = possibleSteps.stream().map(s -> s.getOutputSymbol()).collect(Collectors.toSet());
		return outputAlphabet;
	}
	// make fancier at some point, i.e. make abstract and create one implementation per type of step
	// as input alphabet only makes sense for MDPs
	// at the moment the caller has to be sure about the type of steps, so mistakes are generally possible
	@SuppressWarnings("unchecked")
	public Set<InputSymbol> getInputAlphabet() {
		if(inputAlphabet == null){
			Set<InputOutputStep> stepsCasted = (Set<InputOutputStep>) possibleSteps;
			inputAlphabet = stepsCasted.stream().map(s -> s.getInput()).collect(Collectors.toSet());
		}
		return inputAlphabet;
	}

	public List<FiniteString<S>> getSample() {
		return sample;
	}
}
