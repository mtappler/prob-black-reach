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
package at.tugraz.alergia.active.eval.journal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import at.tugraz.alergia.active.ActiveTestingStrategyInference;
import at.tugraz.alergia.active.Experiment;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.MatrixExportAdapter;
import at.tugraz.alergia.active.eval.Config;
import at.tugraz.alergia.active.eval.EvalUtil;
import at.tugraz.alergia.active.strategy.UniformSelectionTestStrategy;
import at.tugraz.alergia.active.strategy.adversary.AdversaryBasedTestStrategy;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;

public class EvalCoin {
	public static long[] seeds = { 1000l, 10000l, 11000l, 12000l, 13000l, 14000l, 15000l, 16000l, 
			17000l, 18000l, 19000l, 110000l, 2000l, 3000l, 4000l, 5000l, 6000l, 7000l, 8000l, 9000l};
	public static final int STEP_BOUND = 20;
	public static final double STOP_PROBABILITY = 0.025;

	public static final int BASE_BATCH_SIZE = 250;
	public static final int ROUNDS = 100;

	public static final Predicate<List<InputOutputStep>> alternativeStoppingCriterion = (
			List<InputOutputStep> trace) -> trace.get(trace.size() - 1).getOutput().stringRepresentation()
					.contains("finished");

	public static ActiveTestingStrategyInference incremental(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.95;
		int nrRounds = ROUNDS;
		int batchSize = BASE_BATCH_SIZE;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);
		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		
		testStrategy.setInputs(inputSet);
		testStrategy.setUsePrism(false);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		ActiveTestingStrategyInference inferrer = new ActiveTestingStrategyInference(nrRounds, testStrategy);
		inferrer.setUseAdaptiveEpsilon(false);
		
		return inferrer;
	}

	public static ActiveTestingStrategyInference monolithic(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 1;
		double probRandomSampleChangeFactor = 1;
		int nrRounds = 1;
		int batchSize = ROUNDS * BASE_BATCH_SIZE;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);
		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		testStrategy.setInputs(inputSet);
		testStrategy.setUsePrism(false);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}

	public static ActiveTestingStrategyInference baseline(Adapter adapter, InputSymbol[] inputs, String prismLocation) {
		UniformSelectionTestStrategy testStrategy = new UniformSelectionTestStrategy(adapter, 0, 0, STOP_PROBABILITY,
				inputs);

		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(0, testStrategy);
	}

	public static void main(String[] args) throws Exception {

		String prismLocation = Config.prismLocation();
		InputSymbol[] inputs = new InputSymbol[] { new InputSymbol("go1"), new InputSymbol("go2") };

		Adapter adapter = new MatrixExportAdapter("src/main/resources/shared_coin/"
				+ "coin2");

		String prismFile = "src/main/resources/shared_coin/coin2.prism";
		String propertiesFile = "src/main/resources/shared_coin/coin2.props";

		int[] properties = new int[] { 6,1 };
		String path = "../log_extended/shared_coin/";

		Experiment monolithicExperiment = new Experiment(path, monolithic(adapter, inputs, prismLocation), adapter,
				seeds, propertiesFile, "monolithic", properties);
		monolithicExperiment.run();	
		Experiment incrementalExperiment = new Experiment(path, incremental(adapter, inputs, prismLocation), adapter,
				seeds, propertiesFile, "incremental", properties);
		incrementalExperiment.run();

		Experiment baseline = new Experiment(path, baseline(adapter, inputs, prismLocation), adapter, seeds,
				propertiesFile, "baseline", properties);
		baseline.run();

		EvalUtil.printSummaries(prismLocation, prismFile, propertiesFile, monolithicExperiment, 
				incrementalExperiment, baseline);
	}

}
