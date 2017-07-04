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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Transition {

	private static final int SOURCE = 1;
	private static final int TARGET = 3;
	@SuppressWarnings("unused")
	private static final int CHOICE = 2;
	private static final int PROBABILITY = 4;
	private static final int INPUT = 5;

	public Transition(long sourceId, long targetId, double probability, String input) {
		super();
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.probability = probability;
		this.input = input;
	}

	private long sourceId = -1;
	private long targetId = -1;
	private double probability = -1;
	private String input = null;

	public long getSourceId() {
		return sourceId;
	}

	public void setSourceId(long sourceId) {
		this.sourceId = sourceId;
	}

	public long getTargetId() {
		return targetId;
	}

	public void setTargetId(long targetId) {
		this.targetId = targetId;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	private static final Pattern formatPattern = Pattern
			.compile("(\\d+) (\\d+) (\\d+) (\\d+.\\d+|1|0|\\d+.\\d+e-\\d+|\\de-\\d+) (\\w+)");

	public static Transition fromString(String textualTransition) {
		Matcher matcher = formatPattern.matcher(textualTransition);
		MatrixExportAdapter.checkMatch(matcher,textualTransition);
		
		long sourceId = Long.parseLong(matcher.group(SOURCE));
		long targetId = Long.parseLong(matcher.group(TARGET));
		double probability = Double.parseDouble(matcher.group(PROBABILITY));
		String input = matcher.group(INPUT);
		return new Transition(sourceId, targetId, probability, input);
	}
}