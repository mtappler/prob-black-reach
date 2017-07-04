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

import java.util.List;
import java.util.Random;
import java.util.Set;

class Executor {
	State currentState = null;
	private MatrixExportAdapter adapter = null;
	private long seed = -1;
	private Random rnd = null;

	public Executor(MatrixExportAdapter adapter, long seed) {
		this.adapter = adapter;
		this.seed = seed;
		if (seed == -1) {
			this.seed = System.currentTimeMillis();
		}
		this.rnd = new Random(this.seed);
	}

	public String reset() {
		currentState = adapter.initState;

		StateLabels stateLabel = adapter.labels.get(currentState.getId());
		List<Long> outputLabelIds = stateLabel.getLabelIds();
		return adapter.labelHeader.textualLabel(outputLabelIds);
	}

	public String executeControllable(String input) {
		String output = execute(input);
		return output;
	}
	public String execute(String input) {
		Set<Transition> transFromState = adapter.transitions.get(currentState.getId());
		double selectionProbability = rnd.nextDouble();
		for (Transition t : transFromState) {
			if (t.getInput().equals(input)) {
				if (selectionProbability <= t.getProbability()) {
					long targetStateId = t.getTargetId();
					currentState = adapter.states.get(targetStateId);
					StateLabels stateLabel = adapter.labels.get(targetStateId);
					List<Long> outputLabelIds = stateLabel.getLabelIds();
					return adapter.labelHeader.textualLabel(outputLabelIds);
				} else {
					selectionProbability -= t.getProbability();
				}
			}
		}

		// self loop if input is not enabled in current state
		StateLabels stateLabel = adapter.labels.get(currentState.getId());
		List<Long> outputLabelIds = stateLabel.getLabelIds();
		return adapter.labelHeader.textualLabel(outputLabelIds);
//		throw new RuntimeException("No transition found, probability \"left:\"" + selectionProbability);
	}
}