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
package at.tugraz.alergia.active.adapter.prism_matrix_export;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.adapter.Adapter;

public class MatrixExportAdapter implements Adapter {

	private String statesFileName = "";
	private String transitionsFileName = "";
	private String labelsFileName = "";
	Map<Long, State> states = null;
	Map<Long, Set<Transition>> transitions = null;
	@SuppressWarnings("unused")
	private TransitionHeader transitionHeader = null;
	@SuppressWarnings("unused")
	private StateHeader stateHeader = null;
	LabelHeader labelHeader = null;
	Map<Long, StateLabels> labels = null;
	State initState = null;
	private Executor executor = null;
	private ConcreteModelImporter importer = new ConcreteModelImporter();
//	private boolean executeInternals = true;


	public MatrixExportAdapter(String modelName) {
		this(modelName + ConcreteModelImporter.STATE_FILE_EXT, 
				modelName + ConcreteModelImporter.TRANS_FILE_EXT, 
				modelName + ConcreteModelImporter.LABEL_FILE_EXT);
	}

	static void checkMatch(Matcher matcher, String textual) {
		if (!matcher.matches())
			throw new RuntimeException(
					"Illegal Syntax: " + matcher.toMatchResult().toString() + " in string \"" + textual + "\"");
	}

	public MatrixExportAdapter(String statesFileName, String transitionsFileName, String labelsFileName) {
		super();
		this.statesFileName = statesFileName;
		this.transitionsFileName = transitionsFileName;
		this.labelsFileName = labelsFileName;
	}

	public void init(long seed) throws Exception {
		readStatesFile();
		readTransFile();
		readLabelsFile();
		setInitState();
		executor = new Executor(this, seed);
	}

	private void setInitState() {
		initState = importer.findInitState(true, labelHeader,labels, states);
	}

	

	private void readLabelsFile() throws IOException {
		Pair<LabelHeader, Map<Long, StateLabels>> readContent = importer.readLabelsFile(labelsFileName);
		labels = readContent.getRight();
		labelHeader = readContent.getLeft();
	}

	private void readTransFile() throws IOException {
		Pair<TransitionHeader, Map<Long, Set<Transition>>> readContent = importer.readTransFile(transitionsFileName);
		transitions = readContent.getRight();
		transitionHeader = readContent.getLeft();
	}

	public void readStatesFile() throws IOException {
		Pair<StateHeader, Map<Long, State>> readContent = importer.readStatesFile(statesFileName);
		states = readContent.getRight();
		stateHeader = readContent.getLeft();
	}

	

	@Override
	public String reset() {
		return executor.reset();
	}

	@Override
	public String execute(String input) {
		if (executor == null)
			throw new IllegalStateException("Executor not initialised");

		return executor.executeControllable(input);
	}

}
