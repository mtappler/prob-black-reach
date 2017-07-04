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

import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.pta.PTANode;

public class MdpState extends McState<InputOutputStep>{

	MdpState(String id, OutputSymbol label, PTANode<InputOutputStep> ptaNode) {
		super(id, label, ptaNode);
	}
	@Override
	public void normalize(SampleData<InputOutputStep> sampleData) {
		for(InputSymbol i : sampleData.getInputAlphabet()){
			double totalForI = getTransitions().stream()
			.filter(t -> t.getStep().getInput().equals(i))
			.mapToDouble(McTransition::getProbability)
			.sum();
			getTransitions().stream()
			.filter(t -> t.getStep().getInput().equals(i))
			.forEach(t -> {
				t.setNormalized(true);
				t.setProbability(t.getProbability()/totalForI);
			});
		}
	}


}
