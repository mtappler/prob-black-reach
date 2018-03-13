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
import java.util.List;
import java.util.function.Predicate;

import at.tugraz.alergia.active.ActiveTestingStrategyInference;
import at.tugraz.alergia.active.Experiment;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.MatrixExportAdapter;
import at.tugraz.alergia.active.strategy.UniformSelectionTestStrategy;
import at.tugraz.alergia.active.strategy.adversary.AdversaryBasedTestStrategy;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;

public class EvalEmqttTwoClientJournal {
	public static long[] seeds = { 1000l, 2000l, 3000l, 4000l, 5000l, 6000l, 7000l, 8000l, 9000l, 10000l,
			11000l, 12000l, 13000l, 14000l, 15000l, 16000l, 17000l, 18000l, 19000l, 110000l};
	public static final int STEP_BOUND = 20;
	public static final double STOP_PROBABILITY = 0.025;
	public static final int BASE_BATCH_SIZE = 100;
	public static final int ROUNDS = 60;

	public static final Predicate<List<InputOutputStep>> alternativeStoppingCriterion = (
			List<InputOutputStep> trace) -> false;

	public static ActiveTestingStrategyInference incremental(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.95;
		int nrRounds = ROUNDS;
		int batchSize = BASE_BATCH_SIZE;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);

		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		return new ActiveTestingStrategyInference(nrRounds, testStrategy);
	}
	public static ActiveTestingStrategyInference incrementalConv(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 0.75;
		double probRandomSampleChangeFactor = 0.95;
		int nrRounds = 200;
		int batchSize = BASE_BATCH_SIZE;
		long initialSeed = 0;

		AdversaryBasedTestStrategy testStrategy = new AdversaryBasedTestStrategy(adapter, STEP_BOUND, batchSize,
				initialSeed, STOP_PROBABILITY, inputs, probRandomSample, probRandomSampleChangeFactor, prismLocation);

		testStrategy.setAlternativeStoppingCriterion(alternativeStoppingCriterion);
		ActiveTestingStrategyInference inferrer = new ActiveTestingStrategyInference(nrRounds, testStrategy);
		inferrer.setConvergenceCheck(true);
		inferrer.setConfidenceDelta(0.01);
		return inferrer;
	}

	public static ActiveTestingStrategyInference monolithic(Adapter adapter, InputSymbol[] inputs,
			String prismLocation) {
		double probRandomSample = 1;
		double probRandomSampleChangeFactor = 1;
		int nrRounds = 1;
		int batchSize = BASE_BATCH_SIZE * ROUNDS;
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
		InputSymbol[] inputs = new InputSymbol[] { new InputSymbol("ConnectC2"), new InputSymbol("ConnectC1WithWill"),
				new InputSymbol("PublishQoS0C2"), new InputSymbol("PublishQoS1C1"), new InputSymbol("SubscribeC1"),
				new InputSymbol("UnSubScribeC1"), new InputSymbol("SubscribeC2"), new InputSymbol("UnSubScribeC2"),
				new InputSymbol("DisconnectTCPC1") };

		Adapter adapter = new MatrixExportAdapter("src/main/resources/mqtt/emqtt_two_client");

		String prismFile = "src/main/resources/mqtt/emqtt_two_client.prism";
		String propertiesFile = "src/main/resources/mqtt/emqtt_two_client.props";

		int lowerBound = 3;
		int upperBound = 8;
		int[] properties = new int[upperBound - lowerBound];
		for (int i = lowerBound; i < upperBound; i++)
			properties[i - lowerBound] = i + 1;
		String path = "log/emqtt/journal";
		Experiment baseline = new Experiment(path, baseline(adapter, inputs, prismLocation), adapter, seeds,
				propertiesFile, "baseline", properties);
		baseline.run();
		Experiment monolithicExperiment = new Experiment(path, monolithic(adapter, inputs, prismLocation), adapter,
				seeds, propertiesFile, "monolithic", properties);
		monolithicExperiment.run();

		Experiment incrementalSlowExperiment = new Experiment(path, incremental(adapter, inputs, prismLocation),
				adapter, seeds, propertiesFile, "incremental-slow", properties);
		incrementalSlowExperiment.run();
	
		Experiment incrementalConvExperiment = new Experiment(path, incrementalConv(adapter, inputs, prismLocation),
				adapter, seeds, propertiesFile, "incremental-conv", properties);
		incrementalConvExperiment.run();

		EvalUtil.printSummaries(prismLocation, prismFile, propertiesFile, monolithicExperiment,
				incrementalSlowExperiment,incrementalConvExperiment, baseline);
		BoxplotExporter exporter = new BoxplotExporter(true, prismLocation, prismFile, propertiesFile);
		List<Experiment> experiments = Arrays.asList(incrementalConvExperiment,incrementalSlowExperiment ,monolithicExperiment);
		List<String> colours = Arrays.asList("red", "black","blue");
		List<Integer> boxProperties = Arrays.asList(5,6,7,4,8);
		List<Integer> stepBounds = Arrays.asList(5,8,11,14,17);
		System.out.println(exporter.export(stepBounds,boxProperties, experiments, colours));
	}

}
