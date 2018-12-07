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
package at.tugraz.alergia.active.eval;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import at.tugraz.alergia.active.ActiveTestingStrategyInference;
import at.tugraz.alergia.active.Experiment;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.MatrixExportAdapter;
import at.tugraz.alergia.active.strategy.UniformSelectionTestStrategy;
import at.tugraz.alergia.active.strategy.adversary.AdversaryBasedTestStrategy;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;

public class EvalSlotR3 {
	public static long[] seeds = { 1000l, 2000l,3000l, 4000l, 5000l, 6000l, 7000l, 8000l, 9000l, 10000l };
	public static final int STEP_BOUND = 20;
	public static final double STOP_PROBABILITY = 0.05;
	public static final Predicate<List<InputOutputStep>> alternativeStoppingCriterion = (List<InputOutputStep> trace) -> 
		trace.get(trace.size()-1).getOutput().stringRepresentation().equals("end");

	public static ActiveTestingStrategyInference incremental(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.9;
		int nrRounds = 60;
		int batchSize = 250;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);

		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		testStrategy.setInputs(inputSet);
		testStrategy.setUsePrism(false);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference incrementalSlowerMoreRounds(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.975;
		int nrRounds = 240;
		int batchSize = 125;
		long initialSeed = 0;
		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);
		testStrategy.setUsePrism(false);
		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		testStrategy.setInputs(inputSet);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference incrementalSlowerMoreRoundsPrism(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.975;
		int nrRounds = 240;
		int batchSize = 125;
		long initialSeed = 0;
		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);
		testStrategy.setUsePrism(true);
		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		testStrategy.setInputs(inputSet);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference incrementalSlower(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.975;
		int nrRounds = 120;
		int batchSize = 125;
		long initialSeed = 0;
		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);
		testStrategy.setUsePrism(false);
		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		testStrategy.setInputs(inputSet);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference incrementalSlow(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.95;
		int nrRounds = 60;
		int batchSize = 250;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);
		Set<InputSymbol> inputSet = new HashSet<>(Arrays.asList(inputs));
		testStrategy.setInputs(inputSet);
		testStrategy.setUsePrism(false);
		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference monolithicLarge(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 1;
		double probRandomSampleChangeFactor = 1;
		int nrRounds = 1;
		int batchSize = 250 * 60 * 5;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);

		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference monolithic(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 1;
		double probRandomSampleChangeFactor = 1;
		int nrRounds = 1;
		int batchSize = 250 * 60;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);

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
		InputSymbol[] inputs = new InputSymbol[] { new InputSymbol("stop"), new InputSymbol("spin1"),
				new InputSymbol("spin2"), new InputSymbol("spin3") };

		Adapter adapter = new MatrixExportAdapter("src/main/resources/slot_machine_step_count/slot_machine");
		
		String prismFile = "src/main/resources/slot_machine_step_count/slot_machine.prism";	
		String propertiesFile = "src/main/resources/slot_machine_step_count/slot_machine_full.props";
		
		int[] properties = new int[]{58,59,60};
		String path = "log/logr3/my_impl";
//		Experiment baseline = new Experiment(path,baseline(adapter, inputs, prismLocation), adapter, seeds, propertiesFile,
//				"baseline", properties);
//		baseline.run();
//		Experiment monolithicExperiment = new Experiment(path,monolithic(adapter, inputs, prismLocation), adapter, seeds,
//				propertiesFile, "monolithic", properties);
//		monolithicExperiment.run();
////
//		Experiment monolithicLargeExperiment = new Experiment(path,monolithicLarge(adapter, inputs, prismLocation), adapter, seeds,
//				propertiesFile, "monolithic-large", properties);
//		monolithicLargeExperiment.run();

		Experiment incrementalExperiment = new Experiment(path,incremental(adapter, inputs, prismLocation), adapter, seeds,
				propertiesFile, "incremental", properties);
//		incrementalExperiment.run();

//		Experiment incrementalSlow = new Experiment(path,incrementalSlow(adapter, inputs, prismLocation), adapter, seeds,
//				propertiesFile, "incremental-slow", properties);
//		incrementalSlow.run();
		Experiment incrementalSlowerExperiment = new Experiment(path,incrementalSlower(adapter, inputs, prismLocation), adapter, seeds,
				propertiesFile, "incremental-slower", properties);
		incrementalSlowerExperiment.run();
		Experiment moreRounds = new Experiment(path,incrementalSlowerMoreRounds(adapter, inputs, prismLocation), adapter, seeds,
				propertiesFile, "more-rounds", properties);
		moreRounds.run();
		Experiment moreRoundsPrism = new Experiment(path,incrementalSlowerMoreRoundsPrism(adapter, inputs, prismLocation), adapter, seeds,
				propertiesFile, "more-rounds-prism", properties);
//		moreRoundsPrism.run();
		EvalUtil.printSummaries(prismLocation,prismFile, propertiesFile, incrementalExperiment,
				incrementalSlowerExperiment,moreRounds,moreRoundsPrism);
	}

}
