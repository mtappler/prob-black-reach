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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.MatrixExportAdapter;
import at.tugraz.alergia.active.strategy.adversary.Adversary;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;

public class AdversaryTest {
	private static Property property = null;
	private static Random rndSource = new Random();
	private static double stopProb = 0.025;
	private static Predicate<List<InputOutputStep>> alternativeStoppingCriterion = (List<InputOutputStep> trace) -> 
		trace.get(trace.size()-1).getOutput().getSatisfiedProps().contains("s1_finished") && 
		trace.get(trace.size()-1).getOutput().getSatisfiedProps().contains("s2_finished");
	private static InputSymbol[] inputAlphabet = new InputSymbol[] { new InputSymbol("time"), new InputSymbol("send1"),
			new InputSymbol("send2"), new InputSymbol("finish1"), new InputSymbol("finish2"),new InputSymbol("int1"),
			new InputSymbol("int2")};

	public static void main(String[] args) throws Exception {
		property = new Property("src/main/resources/wlan/test_props.props", 5);
		long seed = 1;
		Adapter adapter = new MatrixExportAdapter(
				"src/main/resources/wlan/wlan");
		Adversary adversary = new Adversary(
				"src/main/resources/wlan/wlan_adv.tra",
				"src/main/resources/wlan/wlan");
		adapter.init(seed);
		evaluate(adapter, adversary);
	}

	public static double evaluate(Adapter adapter, Adversary adversary) {
		int nrSamples = 25000;
		System.out.println(nrSamples + " required");

		int nrSuccess = 0;
		for (int i = 0; i < nrSamples; i++) {
			FiniteString<InputOutputStep> trace = sampleTraceWithAdversary(adapter, property.getSteps(), adversary);
			nrSuccess += property.evaluate(trace) ? 1 : 0;
		}
		double estProb = (double) nrSuccess / nrSamples;

		System.out.println("Estimated probability for property " + estProb);
		return estProb;
	}

	private static FiniteString<InputOutputStep> sampleTraceWithAdversary(Adapter adapter, int minNrSteps,
			Adversary adversary) {
		List<InputOutputStep> stringContent = new ArrayList<>();
		String initialOutput = adapter.reset();
		adversary.reset();
		int nrSteps = 0;
//		System.out.println("START");
		while (true) {
			if (nrSteps++ >= minNrSteps && rndSource.nextDouble() < stopProb)
				break;
			Optional<String> input = Optional.empty();
			input = adversary.optimalInput();
//			if(input.isPresent())
//				System.out.println(input.get());
//			else
//				System.out.println("END");
			
			InputSymbol actualInput = input.map(i -> new InputSymbol(i))
					.orElse(inputAlphabet[rndSource.nextInt(inputAlphabet.length)]);

			String output = adapter.execute(actualInput.stringRepresentation());
//			System.out.println(output);
			stringContent.add(new InputOutputStep(actualInput, new OutputSymbol(output)));
			adversary.executeStep(actualInput.stringRepresentation(), output);

			if (alternativeStoppingCriterion != null && alternativeStoppingCriterion.test(stringContent))
				break;
		}

		return new FiniteString<>(stringContent,new OutputSymbol(initialOutput));
	}
}
