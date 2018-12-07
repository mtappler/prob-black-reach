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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;

public class IOFPTANode extends PTANode<InputOutputStep> {

	private Map<InputSymbol, Integer> frequencyForInputCache = new HashMap<>();
	public IOFPTANode(OutputSymbol label, PTATransition<InputOutputStep> incomingTransition) {
		super(label, incomingTransition);
	}
	
	public int frequencyForInput(InputSymbol i){
		if(frequencyForInputCache.containsKey(i))
			return frequencyForInputCache.get(i);
		else 
			return 0;
	}

	@Override
	public PTANode<InputOutputStep> addSuccessor(PTANodeFactory<InputOutputStep> factory, InputOutputStep step) {
		PTANode<InputOutputStep> result = super.addSuccessor(factory, step);
		if(frequencyForInputCache.containsKey(step.getInput()))
			frequencyForInputCache.put(step.getInput(), frequencyForInputCache.get(step.getInput())+1);
		else
			frequencyForInputCache.put(step.getInput(), 1);
		
		return result;
	}

	@Override
	public boolean localCompatible(PTANode<InputOutputStep> q_b, SampleData<InputOutputStep> sampleData,
			double epsilon) {
		Set<OutputSymbol> outputAlphabet = sampleData.getOutputAlphabet();
		Set<InputSymbol> inputAlphabet = sampleData.getInputAlphabet();
		for(InputSymbol i : inputAlphabet){
			int n_1 = this.frequencyForInput(i);
			int n_2 = ((IOFPTANode)q_b).frequencyForInput(i);
			if(n_1 == 0 || n_2 == 0)
				continue; 
			for(OutputSymbol o : outputAlphabet){
				PTATransition<InputOutputStep> succ_1 = getSuccessor(new InputOutputStep(i, o));
				PTATransition<InputOutputStep> succ_2 = q_b.getSuccessor(new InputOutputStep(i, o));
				int f_1 = succ_1 == null ? 0 : succ_1.getFrequency();
				int f_2 = succ_2 == null ? 0 : succ_2.getFrequency();
				boolean hoeffding = hoeffdingTest(epsilon, n_1, n_2, f_1, f_2);
				if(!hoeffding)
					return false;
			}
		}
		return true;
	}

}
