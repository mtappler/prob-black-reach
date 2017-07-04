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
import at.tugraz.alergia.data.OutputStep;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.pta.PTANode;

public class DtmcState extends McState<OutputStep> {

	DtmcState(String id, OutputSymbol label, PTANode<OutputStep> node) {
		super(id, label,node);
	}

	@Override
	public void normalize(SampleData<OutputStep> sampleData) {
		double total = getTransitions().stream().mapToDouble(McTransition::getProbability)
				.sum();
		getTransitions().stream().forEach(t -> {
			t.setNormalized(true);
			t.setProbability(t.getProbability()/total);
		});
	}
}
