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
package at.tugraz.alergia.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class FiniteStringTest {

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
	public void testGet() {
		assertThat(string.get(0), is(equalTo(out("1"))));
		assertThat(string.get(1), is(equalTo(out("2"))));
		assertThat(string.get(2), is(equalTo(out("3"))));
		assertThat(string.get(3), is(equalTo(out("4"))));
	}

	@Test
	public void testSize() {
		assertThat(string.size(), is(equalTo(4)));
	}

	@Test
	public void testPrefix0() {
		FiniteString<OutputStep> prefix = string.prefix(0);
		assertThat(prefix.size(), is(equalTo(1)));
		assertThat(prefix.get(0), is(equalTo(out("1"))));
		assertThat(prefix.getInitialOutput(), is(equalTo(initialOutput)));
	}

	@Test
	public void testPrefix2() {
		FiniteString<OutputStep> prefix = string.prefix(2); // should include
															// the index
		assertThat(prefix.size(), is(equalTo(3)));
		assertThat(prefix.get(0), is(equalTo(out("1"))));
		assertThat(prefix.get(1), is(equalTo(out("2"))));
		assertThat(prefix.get(2), is(equalTo(out("3"))));
		assertThat(prefix.getInitialOutput(), is(equalTo(initialOutput)));
	}

	@Test
	public void testPrefixFull() {
		FiniteString<OutputStep> prefix = string.prefix(3); // should include
															// the index
		assertThat(prefix.size(), is(equalTo(4)));
		assertThat(prefix.get(0), is(equalTo(out("1"))));
		assertThat(prefix.get(1), is(equalTo(out("2"))));
		assertThat(prefix.get(2), is(equalTo(out("3"))));
		assertThat(prefix.get(3), is(equalTo(out("4"))));
		assertThat(prefix.getInitialOutput(), is(equalTo(initialOutput)));
	}

	@Test
	public void testSuffix0() {
		FiniteString<OutputStep> suffix = string.suffix(0);
		assertThat(suffix.size(), is(equalTo(4)));
		assertThat(suffix.get(0), is(equalTo(out("1"))));
		assertThat(suffix.get(1), is(equalTo(out("2"))));
		assertThat(suffix.get(2), is(equalTo(out("3"))));
		assertThat(suffix.get(3), is(equalTo(out("4"))));
		assertThat(suffix.getInitialOutput(), is(equalTo(initialOutput)));
	}

	@Test
	public void testSuffix2() {
		FiniteString<OutputStep> suffix = string.suffix(2);
		assertThat(suffix.size(), is(equalTo(2)));
		assertThat(suffix.get(0), is(equalTo(out("3"))));
		assertThat(suffix.get(1), is(equalTo(out("4"))));
		assertThat(suffix.getInitialOutput(), is(equalTo(new OutputSymbol("2"))));
	}

	@Test
	public void testSuffix4() {
		FiniteString<OutputStep> suffix = string.suffix(4);
		assertThat(suffix.size(), is(equalTo(0)));
		assertThat(suffix.getInitialOutput(), is(equalTo(new OutputSymbol("4"))));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testSuffix5() {
		string.suffix(5);
	}

	@Test
	public void testPrefixes() {
		Set<FiniteString<OutputStep>> prefixes = string.prefixes();
		assertThat(prefixes.size(), is(equalTo(4)));
		int size = 1;
		Comparator<? super FiniteString<OutputStep>> comparator = 
				(f1,f2) -> f1.size() - f2.size();
		for(FiniteString<OutputStep> p : prefixes.stream().sorted(comparator).collect(Collectors.toList())){
			assertThat(p.size(), is(equalTo(size++)));
		}
	}

	@Test
	public void testMap() {
		List<OutputStep> contentMapped = content.stream()
				.map(o -> new OutputStep(new OutputSymbol(o.getOutputSymbol().symbol + "m")))
				.collect(Collectors.toList());
		FiniteString<Step> mappedString = string.map(o -> new OutputStep(new OutputSymbol(o.getOutputSymbol().symbol + "m")));
		assertThat(contentMapped, is(equalTo(mappedString.getContent())));
	}
	@Test
	public void testFirstSymbol(){
		assertThat(string.firstSymbol(), is(equalTo(out("1"))));
	}
	@Test
	public void testprefixOfTrue(){
		OutputSymbol initialOutputOther = new OutputSymbol("init");
		ArrayList<OutputStep> contentOther = new ArrayList<>();
		contentOther.add(new OutputStep(new OutputSymbol("1")));
		contentOther.add(new OutputStep(new OutputSymbol("2")));
		FiniteString<OutputStep> stringOther = new FiniteString<>(contentOther, initialOutputOther);
		assertThat(stringOther.prefixOf(string), is(true));
	}
	@Test
	public void testprefixOfEqual(){

		OutputSymbol initialOutputOther = new OutputSymbol("init");
		ArrayList<OutputStep> contentOther = new ArrayList<>();
		contentOther.add(new OutputStep(new OutputSymbol("1")));
		contentOther.add(new OutputStep(new OutputSymbol("2")));
		contentOther.add(new OutputStep(new OutputSymbol("3")));
		contentOther.add(new OutputStep(new OutputSymbol("4")));
		FiniteString<OutputStep> stringOther = new FiniteString<>(contentOther, initialOutputOther);
		assertThat(stringOther.prefixOf(string), is(true));
	}
	@Test
	public void testprefixOfFalse(){

		OutputSymbol initialOutputOther = new OutputSymbol("init");
		ArrayList<OutputStep> contentOther = new ArrayList<>();
		contentOther.add(new OutputStep(new OutputSymbol("1")));
		contentOther.add(new OutputStep(new OutputSymbol("3")));
		FiniteString<OutputStep> stringOther = new FiniteString<>(contentOther, initialOutputOther);
		assertThat(stringOther.prefixOf(string), is(false));
	}
	
	@Test
	public void testprefixOfFalseInitial(){

		OutputSymbol initialOutputOther = new OutputSymbol("no_init");
		ArrayList<OutputStep> contentOther = new ArrayList<>();
		contentOther.add(new OutputStep(new OutputSymbol("1")));
		contentOther.add(new OutputStep(new OutputSymbol("2")));
		FiniteString<OutputStep> stringOther = new FiniteString<>(contentOther, initialOutputOther);
		assertThat(stringOther.prefixOf(string), is(false));
	}
	@Test
	public void testprefixOfFalseLonger(){
		OutputSymbol initialOutputOther = new OutputSymbol("no_init");
		ArrayList<OutputStep> contentOther = new ArrayList<>();
		contentOther.add(new OutputStep(new OutputSymbol("1")));
		contentOther.add(new OutputStep(new OutputSymbol("2")));
		contentOther.add(new OutputStep(new OutputSymbol("3")));
		contentOther.add(new OutputStep(new OutputSymbol("4")));
		contentOther.add(new OutputStep(new OutputSymbol("5")));
		FiniteString<OutputStep> stringOther = new FiniteString<>(contentOther, initialOutputOther);
		assertThat(stringOther.prefixOf(string), is(false));
	}

	private OutputStep out(String output) {
		return new OutputStep(new OutputSymbol(output));
	}

}
