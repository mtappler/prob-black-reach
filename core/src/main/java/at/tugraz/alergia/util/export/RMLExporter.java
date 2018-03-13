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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.data.Step;

public abstract class RMLExporter<S extends Step> {

	protected static final String locationVar = "loc";
	protected static final String stepInc = "(steps'=min(BOUND,steps + 1))";
	private String modelName;
	public RMLExporter(String modelName){
		this.modelName = modelName;
	}
	public String toRML(MarkovChain<S> mc){
		return toRML(mc, -1);
	}
	public String toRML(MarkovChain<S> mc, int stepBound){
		StringBuilder sb = new StringBuilder();
		appendLine(sb,modelType());
		appendStepBountConstant(sb,stepBound);
		appendLine(sb, "module " + modelName);
		Map<String,Integer> stateIdsRemapping = remapStates(mc);
		Integer initialIntId = stateIdsRemapping.get(mc.initialState().get().getId());
		appendLine(sb, locationVariable(stateIdsRemapping,initialIntId));
		appendStepCountVar(sb,stepBound);
		appendTransitionDefinitions(sb,mc,stateIdsRemapping, stepBound > -1);
		appendLine(sb, "endmodule");
		appendOutLabels(sb,mc,stateIdsRemapping);
		return sb.toString();
	}
	public void appendStepBountConstant(StringBuilder sb, int stepBound) {
		if(stepBound > -1)
			appendLine(sb, String.format("const int BOUND = %d;", stepBound));
	}
	public void appendStepCountVar(StringBuilder sb, int stepBound) {
		if(stepBound > -1){
			 appendLine(sb,"steps : [0..BOUND] init 0;");
			  
		}
	}
	private void appendOutLabels(StringBuilder sb, MarkovChain<S> mc, Map<String, Integer> stateIdsRemapping) {
		Set<String> labels =  mc.getOutputAlphabet()
				.stream()
				.flatMap(o -> o.getSatisfiedProps().stream())
				.collect(Collectors.toSet());
		for(String l : labels){
			appendOutputSymbol(l,sb,mc, stateIdsRemapping);
		}
	}
	private void appendOutputSymbol(String label, StringBuilder sb, MarkovChain<S> mc, Map<String, Integer> stateIdsRemapping) {
		String labelFormula = 
				String.join("|", mc.getStates().stream()
					  .filter(s -> s.getLabel().getSatisfiedProps().contains(label))
					  .map(s -> String.format("%s=%d", locationVar,stateIdsRemapping.get(s.getId())))
					  .collect(Collectors.toList()));
		appendLine(sb,String.format("label \"%s\" = %s;", label,labelFormula));
	}
	private void appendTransitionDefinitions(StringBuilder sb, MarkovChain<S> mc, Map<String, Integer> stateIdsRemapping,
			boolean hasStepBound) {
		for(McState<S> s : mc.getStates()) {
			appendTransitionsForState(sb,s,mc, stateIdsRemapping, hasStepBound);
		}
	}
	protected abstract void appendTransitionsForState(StringBuilder sb, McState<S> s, MarkovChain<S> mc,
			Map<String, Integer> stateIdsRemapping, boolean hasStepBound);
	
	private String locationVariable(Map<String, Integer> stateIdsRemapping, Integer initialIntId) {
		return String.format("%s : [0..%d] init %d;",locationVar, stateIdsRemapping.size() - 1,initialIntId);
	}
	// TODO move functionality to mc, I think already moved it to the McTransformer class
	private Map<String, Integer> remapStates(MarkovChain<S> mc) {
		Map<String,Integer> result = new HashMap<>();
		int i = 0;
		for(McState<S> s : mc.getStates()){
			result.put(s.getId(), i++);
		}
		return result;
	}
	public abstract String modelType();
	public void appendLine(StringBuilder sb, String line){
		sb.append(line);
		sb.append(System.lineSeparator());
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
}
