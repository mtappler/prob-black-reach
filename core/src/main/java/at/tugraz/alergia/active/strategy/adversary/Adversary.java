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
package at.tugraz.alergia.active.strategy.adversary;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.adapter.prism_matrix_export.ConcreteModelImporter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.LabelHeader;
import at.tugraz.alergia.active.adapter.prism_matrix_export.State;
import at.tugraz.alergia.active.adapter.prism_matrix_export.StateHeader;
import at.tugraz.alergia.active.adapter.prism_matrix_export.StateLabels;
import at.tugraz.alergia.active.adapter.prism_matrix_export.Transition;
import at.tugraz.alergia.active.adapter.prism_matrix_export.TransitionHeader;

public class Adversary {
	Map<Long, State> states = null;
	Map<Long, Set<Transition>> concMTransitions = null;
	LabelHeader labelHeader = null;
	Map<Long, StateLabels> labels = null;
	State initState = null;
	private ConcreteModelImporter importer = new ConcreteModelImporter();
	private Map<Long, Set<Transition>> advTransitions = null;
	private State currentState = null;
	private boolean chaos = false;
	@SuppressWarnings("unused")
	private int nrSteps = 0;
	
	public Adversary(String advFileName, String concreteModelName) throws IOException {
		Pair<TransitionHeader, Map<Long, Set<Transition>>> transitionsFileContent = 
				importer.readTransFile(concreteModelName + ConcreteModelImporter.TRANS_FILE_EXT);
		concMTransitions = transitionsFileContent.getRight();
		Pair<StateHeader,Map<Long,State>> stateFileContent = 
				importer.readStatesFile(concreteModelName + ConcreteModelImporter.STATE_FILE_EXT);
		states = stateFileContent.getRight();
		Pair<LabelHeader, Map<Long, StateLabels>> labelFileContent = importer.readLabelsFile(concreteModelName + ConcreteModelImporter.LABEL_FILE_EXT);
		labelHeader = labelFileContent.getLeft();
		labels = labelFileContent.getRight();
		Pair<TransitionHeader, Map<Long, Set<Transition>>> advFileContent = importer.readTransFile(advFileName,true);
		advTransitions  = advFileContent.getRight();
		initState = importer.findInitState(true, labelHeader,labels, states);
	}
	
	public void reset(){
		currentState = initState;
		nrSteps = 0;
		chaos  = false;
	}
	public void executeStep(String input, String output){
		if(chaos)
			return;
		long currentStateId = currentState.getId();
		Set<Transition> transFromState = concMTransitions.get(currentStateId);
		List<State> statesForInputOutput = transFromState.stream()
			.filter(t -> t.getInput().equals(input))
			.map(t-> states.get(t.getTargetId()))
			.filter(s -> labelHeader.textualLabel(labels.get(s.getId()).getLabelIds()).equals(output))
			.collect(Collectors.toList());
		nrSteps ++;
		if(statesForInputOutput.size() > 1)
			System.out.println("More than one state for input output pair, impossible");
		if(statesForInputOutput.isEmpty()){
			chaos = true;
//			System.out.println(String.format("Entering chaos in %dth step",nrSteps));
		}
		else {
			currentState = statesForInputOutput.get(0);
		}		
	}

	public Optional<String> optimalInput(){
		if(chaos)
			return Optional.empty();
		else {
			Set<Transition> transForState = advTransitions.get(currentState.getId());
			if(transForState == null || transForState.isEmpty()){
//				System.out.println(String.format("Entering chaos after %d steps",nrSteps));
//				chaos = true;
				return Optional.empty();
			} else {
				if(transForState.stream().map(Transition::getInput).collect(Collectors.toSet()).size() != 1)
					System.out.println("More than one optimal input");
//				System.out.println("Returned optimal input");
				return Optional.of(transForState.iterator().next().getInput());
			}
		}
	}

}
