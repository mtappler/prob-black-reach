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
package at.tugraz.alergia;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.McTransition;
import at.tugraz.alergia.automata.states.DtmcStateFactory;
import at.tugraz.alergia.automata.states.McState;
import at.tugraz.alergia.automata.states.MdpStateFactory;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputStep;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.pta.IOFPTANodeFactory;
import at.tugraz.alergia.util.IdGen;
import at.tugraz.alergia.util.export.DotMDPHelper;

public class AlergiaTest {

	@Test
	public void testInsertIntoSorted() {
		Alergia<OutputStep> alergia = new Alergia<>(0.1);
		DtmcStateFactory factory = new DtmcStateFactory();
		IdGen idGen = new IdGen();
		List<McState<OutputStep>> states = new ArrayList<>();
		for(int i = 0; i < 100; i++){
			states.add(factory.create(idGen.next(), new OutputSymbol("foo"), null));
		}
		Collections.shuffle(states);
		ArrayList<McState<OutputStep>> sortedList = new ArrayList<>();
		for(McState<OutputStep> state : states)
			alergia.insertIntoSortedList(sortedList, state);
		Iterator<McState<OutputStep>> iter = sortedList.iterator();
		McState<OutputStep> current = iter.next();

		System.out.println(current.getId());
		while(iter.hasNext()){
			McState<OutputStep> next = iter.next();
			System.out.println(next.getId());
			Assert.assertTrue(current.compareTo(next) < 0);
			current = next;
		}
	}

	@Test 
	@Ignore
	public void allStatesReachable() throws IOException{
		Alergia<InputOutputStep> alergia = new Alergia<>(0.01);
		List<FiniteString<InputOutputStep>> sample = parseSampleFile();
		MarkovChain<InputOutputStep> mc = alergia.runAlergia(sample, new MdpStateFactory(), new IOFPTANodeFactory() );
		DotMDPHelper dotHelper = new DotMDPHelper();
		System.out.println(dotHelper.toDot(mc));
		for(McState<InputOutputStep> s : mc.getStates()){
			Assert.assertTrue(reachable(s,mc.initialState().get(), new HashSet<>()));
		}
		
	}
	
	private boolean reachable(McState<InputOutputStep> s, McState<InputOutputStep> current, 
			HashSet<McState<InputOutputStep>> visited) {
		if(current.equals(s))
			return true;
		else if(!visited.contains(current)){
			visited.add(current);
			for(McTransition<InputOutputStep> t : current.getTransitions()){
				if(reachable(s,t.getTarget(),visited))
					return true;
			}
		}
		return false;
	}

	private static List<FiniteString<InputOutputStep>> parseSampleFile() throws IOException {
		FileInputStream fstream = new FileInputStream("src/main/resources/samples_1500_hbmqtt_simple.dat");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;
		List<FiniteString<InputOutputStep>> strings = new ArrayList<>();

		//Read File Line By Line
		while ((strLine = br.readLine()) != null)   {
		  // Print the content on the console
		  strings.add(lineToString(strLine));
		}

		//Close the input stream
		br.close();
		return strings;
	}

	private static FiniteString<InputOutputStep> lineToString(String strLine) {
		String[] strLineSplit = strLine.split("(>|<)+");
	
		List<InputOutputStep> stringContent = Arrays.stream(strLineSplit)
				.filter(inputOutputString -> !"".equals(inputOutputString))
				.map(inputOutputString -> {
					String[] inputOutputSplit = inputOutputString.split(",");
					return new InputOutputStep(new InputSymbol(inputOutputSplit[0]), 
											   new OutputSymbol(inputOutputSplit[1]));
				}).collect(Collectors.toList());
		
		return new FiniteString<>(stringContent, new OutputSymbol("start"));
	}
}
