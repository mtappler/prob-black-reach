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

import static org.junit.Assert.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.OutputStep;
import at.tugraz.alergia.data.OutputSymbol;

public class PropertyTest {
	private OutputSymbol initialOutput;
	private List<OutputStep> content;
	private FiniteString<OutputStep> string;

	@Before
	public void setup() {
		initialOutput = new OutputSymbol("init");
		content = new ArrayList<>();
		content.add(new OutputStep(new OutputSymbol("1")));
		content.add(new OutputStep(new OutputSymbol("2")));
		content.add(new OutputStep(new OutputSymbol("3")));
		content.add(new OutputStep(new OutputSymbol("4")));
		string = new FiniteString<>(content, initialOutput);
	}
	@Test
	public void testParse() {
		Property prop = new Property("Pmax=?[F(\"foo\" & steps < 5)]");
		assertThat(prop.getSteps(),is(equalTo(5)));
		assertThat(prop.isMax(),is(equalTo(true)));
		assertThat(prop.isSafety(),is(equalTo(false)));
		Set<String> labels = new HashSet<>();
		labels.add("foo");
		assertThat(prop.getLabels(),is(equalTo(labels)));
	}
	@Test
	public void testParseSafeMin() {
		Property prop = new Property("Pmin=?[!F(\"foo\" & steps < 5)]");
		assertThat(prop.getSteps(),is(equalTo(5)));
		assertThat(prop.isMax(),is(equalTo(false)));
		assertThat(prop.isSafety(),is(equalTo(true)));
		Set<String> labels = new HashSet<>();
		labels.add("foo");
		assertThat(prop.getLabels(),is(equalTo(labels)));
	}
	@Test
	public void testParseMultLabels() {
		Property prop = new Property("Pmax=?[F(\"foo\" & \"bar\" & steps < 5)]");
		Set<String> labels = new HashSet<>();
		labels.add("foo");
		labels.add("bar");
		
		assertThat(prop.getLabels(),is(equalTo(labels)));	
	}
	@Test
	public void testParseToStringEqual() {
		Set<String> labels = new HashSet<>();
		labels.add("5");
		Property prop = new Property(false, labels,3, true);
		Property parsed = new Property(prop.toString());
		assertThat(parsed.getSteps(), is(equalTo(3)));
		assertThat(parsed.isMax(), is(equalTo(true)));
		assertThat(parsed.isSafety(), is(equalTo(false)));
		assertThat(parsed.getLabels(), is(equalTo(labels)));
		
	}
	@Test
	public void testEvalFalse() {
		Set<String> labels = new HashSet<>();
		labels.add("5");
		Property prop = new Property(false, labels,3, true);
		assertThat(prop.evaluate(string),is(equalTo(false)));
	}
	@Test
	public void testEvalTrue() {
		Set<String> labels = new HashSet<>();
		labels.add("2");
		Property prop = new Property(false, labels,3, true);
		assertThat(prop.evaluate(string),is(equalTo(true)));
	}
	@Test
	public void testEvalTrueInit() {
		Set<String> labels = new HashSet<>();
		labels.add("init");
		Property prop = new Property(false, labels,3, true);
		assertThat(prop.evaluate(string),is(equalTo(true)));
	}
	@Test
	public void testEvalTooLate() {
		Set<String> labels = new HashSet<>();
		labels.add("3");
		Property prop = new Property(false, labels,3, true);
		assertThat(prop.evaluate(string),is(equalTo(false)));
	}


}
