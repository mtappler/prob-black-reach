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
package at.tugraz.alergia.util.export;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;

public class RMLExporterMDP extends RMLExporter<InputOutputStep> {

	public RMLExporterMDP(String modelName) {
		super(modelName);
	}

	@Override
	protected String modelType() {
		return "mdp";
	}
	
	private void appendTransitionDefinitions(StringBuilder sb, InputSymbol input, List<McTransition<InputOutputStep>> ts, 
			McState<InputOutputStep> s, Map<String, Integer> stateIdsRemapping, boolean hasStepBound) {
		if(ts.isEmpty())
			return;
		appendLine(sb, String.format("[%s] %s=%d -> ",input,
				locationVar, 
				stateIdsRemapping.get(s.getId())));
		String transDefs = String.join(" + ", ts.stream()
		  .map(t -> {
		  String transString = String.format("%.10f : (%s'=%d)", 
				  t.getProbability(),
				  locationVar,
				  stateIdsRemapping.get(t.getTarget().getId()));
		  transString += hasStepBound ? ("&" + stepInc) : "";
		  return transString;
		  })
		  .collect(Collectors.toList()));
		appendLine(sb, transDefs + ";");
	}

	@Override
	protected void appendTransitionsForState(StringBuilder sb, McState<InputOutputStep> s,
			MarkovChain<InputOutputStep> mc, Map<String, Integer> stateIdsRemapping, boolean hasStepBound) {
		for(InputSymbol input : mc.getSampleData().getInputAlphabet()){
			List<McTransition<InputOutputStep>> tsForInput = s.getTransitions().stream()
			.filter(t -> t.getStep().getInput().equals(input))
			.collect(Collectors.toList());
			appendTransitionDefinitions(sb,input, tsForInput,s, stateIdsRemapping,hasStepBound);
		}
	}
	
}
