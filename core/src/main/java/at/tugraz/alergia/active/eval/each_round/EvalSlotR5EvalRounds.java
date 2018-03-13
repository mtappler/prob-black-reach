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
package at.tugraz.alergia.active.eval.each_round;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import at.tugraz.alergia.active.ActiveTestingStrategyInference;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.adapter.prism_matrix_export.MatrixExportAdapter;
import at.tugraz.alergia.active.eval.Config;
import at.tugraz.alergia.active.eval.EvalSlotR5Pr10R111;
import at.tugraz.alergia.data.InputSymbol;

public class EvalSlotR5EvalRounds {
	public static long[] seeds = {1000l, 2000l, 3000l, 4000l, 5000l, 6000l, 7000l, 8000l, 9000l, 10000l, 11000l,
						12000l, 13000l, 14000l, 15000l, 16000l, 17000l, 18000l, 19000l, 110000l 
	};
	private static String logFileName = null;

	public static void main(String[] args) throws Exception {

		String prismLocation = Config.prismLocation();
		InputSymbol[] inputs = new InputSymbol[] { new InputSymbol("stop"), new InputSymbol("spin1"),
				new InputSymbol("spin2"), new InputSymbol("spin3") };

		Adapter adapter = new MatrixExportAdapter("src/main/resources/slot_machine_step_count_r5/slot_machine");

		String propertiesFile = "src/main/resources/slot_machine_step_count_r5/Pr10_R111.props";

		int property = 9;
		String path = "log/eval_each_round/logr5_pr10_d14";
		logFileName = path + ".log";
		List<List<Double>> allEvaluations = new ArrayList<>();
		for (long seed : seeds) {
			System.out.println("SEED: " + seed);
			ActiveTestingStrategyInference inferrer = EvalSlotR5Pr10R111.incremental(adapter, inputs, prismLocation);
			inferrer.setEvalEachRound(true);
			adapter.init(seed);
			inferrer.getStrategy().setSeed(seed);
			inferrer.getStrategy().init(propertiesFile, property);
			inferrer.run();
			List<Double> evaluations = inferrer.getEvaluations();
			allEvaluations.add(evaluations);

			printToFile(allEvaluations);
		}
		printToFile(allEvaluations);
		createMeanGraph(allEvaluations);
	}

	private static void createMeanGraph(List<List<Double>> allEvaluations) {
		List<Double> means = new ArrayList<>();
		for (int round = 0; round < EvalSlotR5Pr10R111.ROUNDS; round++) {
			double meanForRound = 0.0;
			for (List<Double> evals : allEvaluations) {
				Double evalForRound = evals.get(round);
				meanForRound += evalForRound / allEvaluations.size();
			}
			means.add(meanForRound);
		}
		System.out.println(means.stream().map(Object::toString).collect(Collectors.joining(";")));
	}

	private static void printToFile(List<List<Double>> allEvaluations) throws IOException {
		File file = new File(logFileName);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter fw = new FileWriter(file)) {
			for (List<Double> evals : allEvaluations) {
				String logString = evals.stream().map(Object::toString).collect(Collectors.joining(","));
				fw.write(logString);
				fw.write(System.lineSeparator());
			}
		}

	}

}
