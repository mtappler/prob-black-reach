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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.Experiment;
import at.tugraz.alergia.active.Experiment.ResultSummary;
import at.tugraz.alergia.active.Experiment.ValueSummary;
import at.tugraz.alergia.active.strategy.adversary.AdversaryBasedTestStrategy;

public class EvalUtil {
	public static final int DOUBLE_PRECISION = 3;
	public static final int SUMMARY_WIDTH = 30;
	public static final Pattern PROPERTY_CHECK_REGEX = Pattern.compile("Model checking: (P(max|min)=\\? \\[.+\\])");
	private boolean includeQuartiles = true;

	
	public static void printSummaries(String pathToPrism, String prismFile, String propertyFile,
			Experiment... experiments) throws Exception {
		Map<String, Map<Integer, ResultSummary>> nameToExperiment = new HashMap<>();
		Arrays.stream(experiments).forEach(e -> nameToExperiment.put(e.getExperimentName(), e.getResultSummaries()));
		List<Integer> properties = nameToExperiment.values().stream().flatMap(m -> m.keySet().stream()).distinct()
				.collect(Collectors.toList());
		Collections.sort(properties);
		for (int property : properties) {
			printSummary(nameToExperiment, pathToPrism, prismFile, propertyFile, property);
		}
	}

	private static void printSummary(Map<String, Map<Integer, ResultSummary>> nameToExperiment,
			String pathToPrism, String prismFile, String propertyFile, int property) throws Exception {
		System.out.print("Summary for property: " + property + " -- ");
		printPropertyAndTrueValue(pathToPrism, prismFile, propertyFile,property);
		Map<String, List<String>> summaryStrings = summaryStringsPerExperiment(nameToExperiment, property);
		List<String> concatenatedSummaries = new ArrayList<>();
		int stringNumber = 0; // admittedly there are certainly better to
								// implement this
		boolean abort = false;
		while (!abort) {
			List<String> currLineString = new ArrayList<>();
			for (String expName : summaryStrings.keySet()) {
				currLineString.add(summaryStrings.get(expName).get(stringNumber));
				abort |= (summaryStrings.get(expName).size() - 1) == stringNumber;
			}
			stringNumber++;
			concatenatedSummaries.add("|" + String.join("|", currLineString) + "|");
		}
		concatenatedSummaries.forEach(s -> System.out.println(s));
	}

	private static void printPropertyAndTrueValue(String pathToPrism, String prismFile, String propertyFile,
			int property) throws Exception {
		String trueValue = trueValueAndPropertyFromPRISM(pathToPrism, prismFile, propertyFile, property);
		System.out.println(trueValue);
	}

	private static Map<String, List<String>> summaryStringsPerExperiment(
			Map<String, Map<Integer, ResultSummary>> nameToExperiment, int property) {
		Map<String, List<String>> summaryStrings = new HashMap<>();
		for (String experimentName : nameToExperiment.keySet()) {
			summaryStrings.put(experimentName, summaryStringsForExperiment(nameToExperiment, experimentName, property));
		}
		return summaryStrings;
	}

	private static List<String> summaryStringsForExperiment(Map<String, Map<Integer, ResultSummary>> nameToExperiment,
			String experimentName, int property) {
		Optional<ResultSummary> resultSummary = Optional.ofNullable(nameToExperiment.get(experimentName).get(property));
		List<String> summaryStrings = new ArrayList<>();
		summaryStrings.add(separator());
		summaryStrings.add(pad(experimentName));
		summaryStrings.add(separator());
		Function<Double, String> doubleMinMaxConverter = (Double d) -> String.format("%." + DOUBLE_PRECISION + "f", d);
		Function<Long, String> longMinMaxConverter = (Long l) -> String.format("%d", l);
		Function<Integer, String> intMinMaxConverter = (Integer l) -> String.format("%d", l);
		
		summaryStrings.addAll(valueSummaryStrings(resultSummary.map(rs -> rs.probabilitySummary), doubleMinMaxConverter,
				"Probability"));
		summaryStrings.add(separator());
		summaryStrings
				.addAll(valueSummaryStrings(resultSummary.map(rs -> rs.stepsSummary), longMinMaxConverter, "# Steps:"));
		summaryStrings.add(separator());
		summaryStrings.addAll(valueSummaryStrings(resultSummary.flatMap(rs -> rs.addProbEstimation),
				doubleMinMaxConverter, "Add. Prob. Est."));
		summaryStrings.add(separator());
		summaryStrings.addAll(valueSummaryStrings(resultSummary.flatMap(rs -> rs.rounds),
				intMinMaxConverter, "Rounds"));
		summaryStrings.add(separator());
		summaryStrings
			.addAll(valueSummaryStrings(resultSummary.map(rs -> rs.runTimeSummary), longMinMaxConverter, "inference duration[ms]:"));
			summaryStrings.add(separator());
			
		summaryStrings.addAll(valueSummaryStrings(resultSummary.flatMap(rs -> rs.learnDuration),
				longMinMaxConverter, "Learn time"));
		summaryStrings.add(separator());
		summaryStrings.addAll(valueSummaryStrings(resultSummary.flatMap(rs -> rs.stratUpdateDuration),
				longMinMaxConverter, "Strategy time"));
		summaryStrings.add(separator());
		return summaryStrings;
	}

	private static String separator() {
		return pad("").replace(' ', '-');
	}

	private static <T> List<String> valueSummaryStrings(Optional<ValueSummary<T>> valSummary,
			Function<T, String> minMaxConverter, String valueName) {
		List<String> summaryStrings = new ArrayList<>();
		summaryStrings.add(pad(valueName));
		summaryStrings.add(pad(
				"Mean: " + valSummary.map(vs -> String.format("%." + DOUBLE_PRECISION + "f", vs.mean)).orElse("--")));
		summaryStrings.add(pad("Median: "
				+ valSummary.map(vs -> String.format("%." + DOUBLE_PRECISION + "f", vs.median)).orElse("--")));
		summaryStrings.add(pad("1. quart: "
				+ valSummary.map(vs -> String.format("%." + DOUBLE_PRECISION + "f", vs.firstQuartile)).orElse("--")));
		summaryStrings.add(pad("3. quart: "
				+ valSummary.map(vs -> String.format("%." + DOUBLE_PRECISION + "f", vs.thirdQuartile)).orElse("--")));
		summaryStrings.add(pad("Std. dev.: "
				+ valSummary.map(vs -> String.format("%." + DOUBLE_PRECISION + "f", vs.stdDeviation)).orElse("--")));
		summaryStrings.add(pad("Max: " + valSummary.map(vs -> minMaxConverter.apply(vs.max)).orElse("--")));
		summaryStrings.add(
				pad("Min: " + valSummary.map(vs -> minMaxConverter.apply(vs.min)).orElse("--")));
		return summaryStrings;
	}

	private static String pad(String string) {
		return String.format("%1$" + SUMMARY_WIDTH + "s", string);
	}
	public static Pair<String,Double> trueValueAndPropertyPairFromPRISM
			(String pathToPrism,String prismFile, String propertyFile, 
			int property) throws Exception{
		String prismCall = String.format("%s %s %s -prop %d",pathToPrism,prismFile,propertyFile, property);
		Process p = Runtime.getRuntime().exec(prismCall);
	    BufferedReader reader = 
	         new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line = "";
	    double result = -1;
	    String propertyString = "Pmax[prop]=";
	    while ((line = reader.readLine())!= null) {
    		if(line.contains("Value in the initial state")){
    			Matcher m = AdversaryBasedTestStrategy.PRISM_PROB_CALC.matcher(line);
    			m.matches();
    			result = Double.parseDouble(m.group(1));
    		}
    		if(line.contains("Model checking: ")){
    			Matcher m = PROPERTY_CHECK_REGEX.matcher(line);
    			m.matches();
    			String extractedProp = m.group(1);
    			propertyString = extractedProp.replace("Pmax=? ", "Pmax").replace("Pmin=?", "Pmin");
    		}
	    }
	    reader.close();
	    return new ImmutablePair<String, Double>(propertyString, result);
		
	}
	public static String trueValueAndPropertyFromPRISM(String pathToPrism,String prismFile, String propertyFile, 
			int property) throws Exception{
		Pair<String, Double> resultPair = 
				trueValueAndPropertyPairFromPRISM(pathToPrism, prismFile, propertyFile, property);
		return resultPair.getLeft() + " = " + resultPair.getRight();
	}

	public boolean isIncludeQuartiles() {
		return includeQuartiles;
	}

	public void setIncludeQuartiles(boolean includeQuartiles) {
		this.includeQuartiles = includeQuartiles;
	}
}
