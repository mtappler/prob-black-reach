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
package at.tugraz.alergia.active;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.Step;

public class Property {

	private static final Pattern outerRegex = Pattern.compile("P(max|min)=\\?\\s*\\[\\s*(.+)\\s*\\]");
	private static final Pattern innerRegex = Pattern
			.compile("!?\\(?F\\s*\\((\\s*(\"\\w+\"\\s*&\\s*)+)\\s*(\\s*steps\\s*<\\s*(\\d+))?\\s*\\)\\s*\\)?");
	private boolean safety;
	private Set<String> labels;
	private int steps;
	private boolean max = true;

	public Property(String propertiesFile, int selectedProperty) throws IOException {
		List<String> lines = Files.readAllLines(new File(propertiesFile).toPath());
		Iterator<String> iter = lines.iterator();
		String propertyLine = "";
		while (iter.hasNext()) {
			String currentLine = iter.next();
			if (!currentLine.trim().equals("")) {
				selectedProperty--;
				if (selectedProperty == 0)
					propertyLine = currentLine.trim();
			}
		}
		if (propertyLine.isEmpty())
			throw new IllegalArgumentException("Did not find selected property");
		parseProperty(propertyLine);
	}

	public Property(String property) {
		parseProperty(property);
	}

	public Property invertOptimisationDirection() {
		return new Property(!safety, getLabels(), steps, !max);
	}

	public Property(boolean safety, Set<String> label, int steps, boolean max) {
		super();
		this.safety = safety;
		this.setLabels(label);
		this.steps = steps;
		this.max = max;
	}

	@Override
	public String toString() {
		// avoid white space as prism call would not work with white space
		return String.format("P%s=?[%s(F(%s%s))]", max ? "max" : "min", safety ? "!" : "",
				getLabels().stream().collect(Collectors.joining("\"&\"", "\"", "\"")),
				steps > -1 ? "&steps<" + steps : "");
	}

	private void parseProperty(String propertyLine) {
		Matcher m = outerRegex.matcher(propertyLine);
		if (!m.matches())
			throw new IllegalArgumentException("Unknown syntax: " + propertyLine);
		String maxOrMin = m.group(1).trim();
		if (maxOrMin.contains("max")) {
			max = true;
		} else {
			max = false;
		}

		String actualProperty = m.group(2).trim();
		if (actualProperty.startsWith("!"))
			setSafety(true);
		else
			setSafety(false);

		Matcher innerMatcher = innerRegex.matcher(actualProperty);

		if (!innerMatcher.matches())
			throw new IllegalArgumentException(
					"Unknown syntax (inner): " + actualProperty + " for " + innerRegex.toString());

		setLabels(Arrays.stream(innerMatcher.group(1).trim().split("&")).map(s -> s.replace("\"", "").trim())
				.collect(Collectors.toSet()));
		if (actualProperty.contains("steps")) {
			setSteps(Integer.parseInt(innerMatcher.group(4)));
		} else {
			setSteps(-1);
		}
	}

	public <S extends Step> boolean evaluate(FiniteString<S> trace) {
		// indexes of prefixes are a bit weird
		FiniteString<S> relevantPart = getSteps() > -1 ? trace.prefix(getSteps() - 2) : trace;
		boolean containsLabel = relevantPart.getContent().stream().anyMatch(
				s -> propSatisfiedInStep(s.getOutputSymbol())) || propSatisfiedInStep(trace.getInitialOutput());
		return (!isSafety() && containsLabel) || (isSafety() && !containsLabel);
	}

	private boolean propSatisfiedInStep(OutputSymbol outSymbol) {
		Set<String> satisfiedPropositions = outSymbol.getSatisfiedProps();
		return getLabels().stream().allMatch(satisfiedPropositions::contains);
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public boolean isSafety() {
		return safety;
	}

	public void setSafety(boolean safety) {
		this.safety = safety;
	}

	public boolean isMax() {
		return max;
	}

	public Set<String> getLabels() {
		return labels;
	}

	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

}
