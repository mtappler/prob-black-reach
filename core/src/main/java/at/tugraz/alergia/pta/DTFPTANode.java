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

import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.data.OutputStep;
import at.tugraz.alergia.data.OutputSymbol;

public class DTFPTANode extends PTANode<OutputStep> {

	public DTFPTANode(OutputSymbol label, PTATransition<OutputStep> incomingTransition) {
		super(label, incomingTransition);
	}

	@Override
	public boolean localCompatible(PTANode<OutputStep> q_b, SampleData<OutputStep> sampleData, double epsilon) {
		int n_1 = transitionFrequency();
		int n_2 = q_b.transitionFrequency();
		if (n_1 == 0 || n_2 == 0)
			return true;
		for (OutputSymbol o : sampleData.getOutputAlphabet()) {
			int f_1 = transitionFrequency(o);
			int f_2 = q_b.transitionFrequency(o);
			boolean hoeffdingTest = hoeffdingTest(epsilon, n_1, n_2, f_1, f_2);
			if (!hoeffdingTest)
				return false;
		}
		return true;
	}
}
