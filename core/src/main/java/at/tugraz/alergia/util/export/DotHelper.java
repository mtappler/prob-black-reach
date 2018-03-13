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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.data.Step;

public class DotHelper<S extends Step> {
	private static final String stateLabelFormatString = //"%s / %s";
			"<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">" +
					 "<TR><TD>%s</TD></TR><TR><TD>%s</TD></TR></TABLE>>";

	public String stateLabel(McState<S> state){
		return String.format(stateLabelFormatString, state.getId(),state.getLabel().stringRepresentation());
	}
	public String stateString(McState<S> state){
		return String.format("%s [shape=\"circle\" margin=0 label=%s];", state.getId(),stateLabel(state));
		// TODO exit rates for CTMC
	}
	public String toDot(MarkovChain<S> mc){
		StringBuilder sb = new StringBuilder();
		appendLine(sb, "digraph g {");
		if(mc.initialState().isPresent()){
			appendLine(sb, "__start0 [label=\"\" shape=\"none\"];");
		}
		mc.getStates().forEach(s -> appendLine(sb,stateString(s)));
		mc.getStates().forEach(s -> appendTransitionLines(sb,s));

		if(mc.initialState().isPresent()){
			appendLine(sb, String.format("__start0 -> %s;", mc.initialState().get().getId()));
		}
		appendLine(sb, "}");
		return sb.toString();
	}
	private void appendTransitionLines(StringBuilder sb, McState<S> s) {
		s.getTransitions().forEach(t -> appendLine(sb,transitionString(t)));
	}
	private String transitionString(McTransition<S> t) {
		return String.format("%s -> %s [label=\"%s%.2f\"];", 
				t.getSource().getId(),t.getTarget().getId(),additionalTransLabel(t), t.getProbability());
	}
	protected String additionalTransLabel(McTransition<S> t) {
		return "";
	}
	public static void appendLine(StringBuilder sb, String line){
		sb.append(line);
		sb.append(System.lineSeparator());
	}
	
	public void writeToFile(MarkovChain<S> mc, String fileName) throws IOException{
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		try(FileWriter fw = new FileWriter(f)){
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(toDot(mc));
			bw.flush();
		}
		

	}
}
