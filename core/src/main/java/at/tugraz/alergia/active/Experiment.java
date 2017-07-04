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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.eval.BoxplotExporter.BoxData;

public class Experiment {

	private static final String doubleRegexString = "(1\\.0|1|0\\.0|0|0\\.\\d+)";
	private static final String resultRegexString = "\\{(.+),(.+),(" + doubleRegexString + "|" + "-" + "),(\\d+)\\}";
	private static final String entryRegexString = "(\\d+):(" + resultRegexString + ")";
	private static final Pattern entryRegex = Pattern.compile(entryRegexString);
	private static final Pattern resultRegex = Pattern.compile(resultRegexString);

	private static final Pattern propertyLinePattern = Pattern
			.compile("Prop:(\\d+)\\{((" + entryRegexString + ";)*" + entryRegexString + ")\\}");

	public static class Result {
		public Result(long totalNrSteps, double probability, Optional<Double> additionalProbEstimation,
				long runDuration) {
			super();
			this.totalNrSteps = totalNrSteps;
			this.probability = probability;
			this.additionalProbEstimation = additionalProbEstimation;
			this.runDuration = runDuration;
		}

		public Result(String resultString) {
			Matcher m = resultRegex.matcher(resultString);
			if (!m.matches())
				throw new IllegalArgumentException("illegal syntax");
			totalNrSteps = Long.parseLong(m.group(1));
			probability = Double.parseDouble(m.group(2));
			if (m.group(3).equals("-"))
				additionalProbEstimation = Optional.empty();
			else
				additionalProbEstimation = Optional.of(Double.parseDouble(m.group(3)));
			runDuration = Long.parseLong(m.group(5));
		}

		@Override
		public String toString() {
			return "{" + totalNrSteps + "," + probability + ","
					+ additionalProbEstimation.map(Object::toString).orElse("-") + "," + runDuration + "}";
		}

		public long totalNrSteps = 0;
		public double probability = 0;
		public Optional<Double> additionalProbEstimation = null;
		public long runDuration = 0;
	}

	public static class ValueSummary<T> {
		public double mean;
		public double stdDeviation;
		public double median;
		public double firstQuartile;
		public double thirdQuartile;

		public T max;
		public T min;
		public List<T> values;
	}

	public static class ResultSummary {
		public ValueSummary<Long> stepsSummary;
		public ValueSummary<Double> probabilitySummary;
		public Optional<ValueSummary<Double>> addProbEstimation = Optional.empty();
		public ValueSummary<Long> runTimeSummary;
	}

	private long[] seeds = null;
	private Adapter adapter = null;
	private ActiveTestingStrategyInference inferrer = null;
	private String propertiesFile;
	private Map<Integer, Map<Long, Result>> results = new HashMap<>();
	private int[] properties;
	private String experimentName = null;
	private String path = null;

	public Experiment(String path, ActiveTestingStrategyInference inferrer, Adapter adapter, long[] seeds,
			String propertiesFile, String experimentName, int... properties) {
		this.path = path;
		this.inferrer = inferrer;
		this.seeds = seeds;
		this.propertiesFile = propertiesFile;
		this.properties = properties;
		this.adapter = adapter;
		this.experimentName = experimentName;
	}


	public BoxData getBoxplotData(int prop,boolean simulated) {
		Map<Long, Result> propResults = results.get(prop);
		
		List<Double> probs = null;
		if(simulated)
			probs = propResults.values().stream().map(r -> r.probability).collect(Collectors.toList());
		else 
			probs = propResults.values().stream().flatMap(r -> r.additionalProbEstimation.isPresent()
				? Stream.of(r.additionalProbEstimation.get()) : Stream.empty()).collect(Collectors.toList());
		Collections.sort(probs);
		double middle = median(probs);
		double lowerBorder = quantile(probs, 0.25);
		double upperBorder = quantile(probs, 0.75);
		double iqr = upperBorder - lowerBorder;
		double lowerWhisker = 0; 
		int minIndex = 0;
		do{
			lowerWhisker = probs.get(minIndex);
			if(lowerWhisker >= lowerBorder - 1.5*iqr)
				break;
			minIndex ++;
		}while(true);
		double upperWhisker = 0; 
		int maxIndex = probs.size()-1;
		do{
			upperWhisker = probs.get(maxIndex);
			if(upperWhisker <= upperBorder + 1.5*iqr)
				break;
			maxIndex--;
		}while(true);
		final double lowerWhiskerFinal = lowerWhisker;
		final double upperWhiskerFinal = upperWhisker;
		List<Double> outliers = probs.stream().filter(p -> p < lowerWhiskerFinal
				|| p > upperWhiskerFinal).collect(Collectors.toList());
		return new BoxData(middle, lowerBorder, upperBorder, lowerWhisker, upperWhisker, outliers); // new BoxData();
	}
	public Map<Integer, ResultSummary> getResultSummaries() {
		Map<Integer, ResultSummary> summaries = new HashMap<>();
		for (Integer prop : results.keySet())
			summaries.put(prop, resultSummary(prop));

		return summaries;
	}

	private ResultSummary resultSummary(Integer prop) {
		Map<Long, Result> propResults = results.get(prop);
		ResultSummary summary = new ResultSummary();
		List<Long> steps = propResults.values().stream().map(r -> r.totalNrSteps).collect(Collectors.toList());
		List<Long> runDurations = propResults.values().stream().map(r -> r.runDuration).collect(Collectors.toList());
		List<Double> probabilities = propResults.values().stream().map(r -> r.probability).collect(Collectors.toList());
		List<Double> addProbalities = propResults.values().stream().flatMap(r -> r.additionalProbEstimation.isPresent()
				? Stream.of(r.additionalProbEstimation.get()) : Stream.empty()).collect(Collectors.toList());

		ValueSummary<Long> stepsSummary = new ValueSummary<>();
		stepsSummary.values = steps;
		stepsSummary.mean = mean(steps);
		stepsSummary.stdDeviation = stdDeviation(steps, stepsSummary.mean);
		stepsSummary.median = median(steps);
		stepsSummary.max = max(steps);
		stepsSummary.min = min(steps);
		stepsSummary.firstQuartile = quantile(steps, 0.25);
		stepsSummary.thirdQuartile = quantile(steps, 0.75);

		ValueSummary<Double> probabilitiesSummary = new ValueSummary<>();
		probabilitiesSummary.values = probabilities;
		probabilitiesSummary.mean = mean(probabilities);
		probabilitiesSummary.stdDeviation = stdDeviation(probabilities, probabilitiesSummary.mean);
		probabilitiesSummary.median = median(probabilities);
		probabilitiesSummary.max = max(probabilities);
		probabilitiesSummary.min = min(probabilities);
		probabilitiesSummary.firstQuartile = quantile(probabilities, 0.25);
		probabilitiesSummary.thirdQuartile = quantile(probabilities, 0.75);

		ValueSummary<Long> runTimeSummary = new ValueSummary<>();
		runTimeSummary.values = runDurations;
		runTimeSummary.mean = mean(runDurations);
		runTimeSummary.stdDeviation = stdDeviation(runDurations, runTimeSummary.mean);
		runTimeSummary.median = median(runDurations);
		runTimeSummary.max = max(runDurations);
		runTimeSummary.min = min(runDurations);
		runTimeSummary.firstQuartile = quantile(runDurations, 0.25);
		runTimeSummary.thirdQuartile = quantile(runDurations, 0.75);

		if (!addProbalities.isEmpty()) {
			ValueSummary<Double> addProbSummary = new ValueSummary<>();
			addProbSummary.values = addProbalities;
			addProbSummary.mean = mean(addProbalities);
			addProbSummary.median = median(addProbalities);
			addProbSummary.stdDeviation = stdDeviation(addProbalities, addProbSummary.mean);
			addProbSummary.min = min(addProbalities);
			addProbSummary.max = max(addProbalities);
			addProbSummary.firstQuartile = quantile(addProbalities, 0.25);
			addProbSummary.thirdQuartile = quantile(addProbalities, 0.75);
			summary.addProbEstimation = Optional.of(addProbSummary);
		}

		summary.probabilitySummary = probabilitiesSummary;
		summary.stepsSummary = stepsSummary;
		summary.runTimeSummary = runTimeSummary;
		return summary;
	}

	private <T extends Number & Comparable<? super T>> double quantile(List<T> elems, double p) {

//	      g <- (length(x)-1)*p - floor((length(x)-1)*p)
//	      indexBefore <- floor((length(x)-1)*p)
//	      return ((1-g) * sort(x)[indexBefore+1]+ g*sort(x)[indexBefore + 2])
		List<T> copy = new ArrayList<>(elems);
		Collections.sort(copy);
		double g = (copy.size()-1) * p - Math.floor((copy.size()-1)*p);
		int index = (int)Math.floor((copy.size()-1)*p);
		double result = (1-g) * copy.get(index + 1).doubleValue() + g*copy.get(index + 2).doubleValue();
		return result;
	}

	private <T extends Number & Comparable<? super T>> T max(List<T> steps) {
		return Collections.max(steps);
	}

	private <T extends Number & Comparable<? super T>> T min(List<T> steps) {
		return Collections.min(steps);
	}

	private <T extends Number & Comparable<? super T>> double median(List<T> steps) {
		List<T> copy = new ArrayList<>(steps);
		Collections.sort(copy);
		double result = 0;
		if (copy.size() % 2 == 0) {
			// two numbers cannot be added, so median must be a double
			result = (copy.get(copy.size() / 2 - 1).doubleValue() + copy.get(copy.size() / 2).doubleValue()) / 2;
		} else {
			result = copy.get((copy.size() + 1) / 2 - 1).doubleValue();
		}
		return result;
	}

	private <T extends Number> double stdDeviation(List<T> vals, double mean) {
		double sum = vals.stream().mapToDouble(v -> Math.pow(v.doubleValue() - mean, 2)).sum();
		return Math.sqrt((1.0 / (vals.size() - 1)) * sum);
	}

	private <T extends Number> double mean(List<T> vals) {
		return vals.stream().mapToDouble(Number::doubleValue).sum() / vals.size();
	}

	public void run() throws IOException, Exception {
		readFromLogFile();
		boolean changed = false;
		try {
			for (int p : properties) {
				if (!results.containsKey(p))
					results.put(p, new HashMap<>());
				for (long s : seeds) {
					if (!results.get(p).containsKey(s)) {
						changed = true;
						results.get(p).put(s, runSingle(p, s));
					}
					if (changed) {
						writeToLogFile();
						changed = false;
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (changed)
				writeToLogFile();
		}
	}

	private void readFromLogFile() throws IOException {
		File logFile = new File(logFileName());
		if (logFile.exists()) {
			List<String> lines = Files.readAllLines(logFile.toPath());
			lines.stream().filter(propertyLinePattern.asPredicate()).forEach(validLine -> {
				Matcher m = propertyLinePattern.matcher(validLine);
				m.matches();
				Integer prop = Integer.parseInt(m.group(1));
				String[] resultsStrings = m.group(2).split(";");
				Map<Long, Result> resultMap = new HashMap<>();
				for (String resString : resultsStrings) {
					Matcher entryMatcher = entryRegex.matcher(resString);
					entryMatcher.matches();
					long seed = Long.parseLong(entryMatcher.group(1));
					Result r = new Result(entryMatcher.group(2));
					resultMap.put(seed, r);
				}
				results.put(prop, resultMap);
			});
		}
	}

	private void writeToLogFile() throws IOException {
		String logString = createLogString();
		File file = new File(logFileName());
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter fw = new FileWriter(file)) {
			fw.write(logString);
		}
	}

	private String logFileName() {
		return path + "/" + experimentName + ".log";
	}

	// TODO maybe log configuration as well
	private String createLogString() {
		StringBuilder logSB = new StringBuilder();
		for (Integer property : results.keySet()) {
			logSB.append("Prop:" + property + "{");
			Map<Long, Result> resForProp = results.get(property);
			List<String> propStrings = resForProp.entrySet().stream()
					.map(entry -> entry.getKey().toString() + ":" + entry.getValue().toString())
					.collect(Collectors.toList());
			logSB.append(String.join(";", propStrings));
			logSB.append("}" + System.lineSeparator());

		}
		return logSB.toString();
	}

	private Result runSingle(int propertyIndex, long seed) throws Exception, IOException {
		System.out.println("***********************************************************************");
		System.out.println("Experiment with seed: " + seed);
		System.out.println("***********************************************************************");
		adapter.init(seed);
		inferrer.getStrategy().setSeed(seed);
		inferrer.getStrategy().init(propertiesFile, propertyIndex);

		double probability = inferrer.run();
		long totalNrSteps = inferrer.getStrategy().getTotalNrSteps();
		long runDuration = inferrer.getLastRunDuration();
		Optional<Double> additionalEstimation = inferrer.getStrategy().getAdditionalProbEstimation();
		return new Result(totalNrSteps, probability, additionalEstimation, runDuration);
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}
}
