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
package at.tugraz.alergia.active.strategy.adversary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.distribution.BinomialDistribution;

import at.tugraz.alergia.active.Property;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.strategy.TestStrategy;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.util.export.DotMDPHelper;
import at.tugraz.alergia.util.export.RMLExporterMDP;

public class AdversaryBasedTestStrategy extends TestStrategy {

	private static final double EPS_EXPLORATION = 0.01;
	private String pathToPrism;
	private int round = 0; // keep track of rounds internally
	private RMLExporterMDP exporter = new RMLExporterMDP("learned_model");
	private int stepBound;
	private int advChoices = 0;
	private int advAgreeingChoices = 0;
	private String pathToTmpFiles = "tmp/";
	private Adversary adversary = null;
	private Adversary lastAdversary = null;
	private double probRandomSample;
	private double probRandomChangeFactor;
	private Property trainingProperty = null;
	private double initialProbRandomSample;
	private boolean noAdversary = true;
	private boolean hybrid = false;
	public static final Pattern PRISM_PROB_CALC = Pattern
			.compile("Value in the initial state: (1\\.\\d+|1|0\\.0|0|0\\.\\d+|\\d\\.\\d+E-\\d)");

	public AdversaryBasedTestStrategy(Adapter adapter, int stepBound, int batchSize, long seed, double stopProb,
			InputSymbol[] inputAlphabet, double probRandomSampleInit, double probRandomChangeFactor,
			String pathToPrism) {
		super(adapter, batchSize, seed, stopProb, inputAlphabet);
		this.pathToPrism = pathToPrism;
		this.stepBound = stepBound;
		this.probRandomSample = probRandomSampleInit;
		this.probRandomChangeFactor = probRandomChangeFactor;
		this.initialProbRandomSample = probRandomSampleInit;
	}

	@Override
	public void update(MarkovChain<InputOutputStep> mdp) throws Exception {
		round++;
		String learnedModelFileName = createTempFile(mdp);
		String advFileName = String.format("%sadv_%d.tra", pathToTmpFiles, round);
		String concreteModelName = String.format("%sout_%d", pathToTmpFiles, round);
		String trainingPropertyString = trainingProperty.toString();
		String prismCall = String.format("%s %s -pf %s -noprob1 -exportadvmdp %s -exportmodel %s.all", pathToPrism,
				learnedModelFileName, trainingPropertyString, advFileName, concreteModelName);
		callPrism(prismCall);
		if (!noAdversary) {
			lastAdversary = adversary;
			readAdvFile(advFileName, concreteModelName);
		} else
			System.out.println("Warning: no adversary created!");
	}

	private void readAdvFile(String advFileName, String concreteModelName) throws IOException {
		adversary = new Adversary(advFileName, concreteModelName);
	}

	private void callPrism(String prismCall) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(prismCall);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		boolean advCreated = false;
		while ((line = reader.readLine()) != null) {
			if (line.contains("Adversary written to") || line.contains("Model checking: ")
					|| line.contains("Value in the initial state"))
				System.out.println(line);
			if (line.contains("Adversary written to"))
				advCreated = true;
			if (line.contains("Value in the initial state")) {
				Matcher m = PRISM_PROB_CALC.matcher(line);
				m.matches();
				// FIXME if this is zero then there is no adversary
				// if there exists an adversary from a previous round, then this
				// will be used
				// and it's probably to sample uniformly in this case
				additionalProbEstimation = Optional.of(Double.parseDouble(m.group(1)));
			}
		}
		noAdversary = !advCreated;
		reader.close();
	}

	private String createTempFile(MarkovChain<InputOutputStep> mdp) throws IOException {
		File temp = File.createTempFile("mdp_" + round + "_", ".tmp");
		File temp2 = File.createTempFile("mdp_" + round + "_", ".dot");
		DotMDPHelper dotMdpHelper = new DotMDPHelper();
		
		
		String rmlString = exporter.toRML(mdp, stepBound);
		try (FileWriter writer = new FileWriter(temp)) {
			writer.write(rmlString);
			writer.flush();
			dotMdpHelper.writeToFile(mdp, temp2.getAbsolutePath());
		}

		return temp.getAbsolutePath();
	}

	@Override
	public List<FiniteString<InputOutputStep>> sample() {
		if (round == 0 || noAdversary) {
			return sampleUniformly();
		}
		if(hybrid){
			List<FiniteString<InputOutputStep>> traces = sampleUniformly();
			traces.addAll(sampleWithAdversary());
			return traces;
		}
		else
			return sampleWithAdversary();
	}

	private List<FiniteString<InputOutputStep>> sampleWithAdversary() {
		List<FiniteString<InputOutputStep>> traces = new ArrayList<>();
		advAgreeingChoices = 0;
		advChoices = 0;
		for (int i = 0; i < batchSize; i++) {
			traces.add(sampleTraceWithAdversary(false));
		}
		probRandomSample *= probRandomChangeFactor;
		return traces;
	}

	private FiniteString<InputOutputStep> sampleTraceWithAdversary(boolean alwaysOptimal) {
		return sampleTraceWithAdversary(alwaysOptimal, property.getSteps());
	}

	private FiniteString<InputOutputStep> sampleTraceWithAdversary(boolean alwaysOptimal, int minNrSteps) {
		List<InputOutputStep> stringContent = new ArrayList<>();
		String initialOutput = adapter.reset();
		adversary.reset();
		if (lastAdversary != null)
			lastAdversary.reset();
		int nrSteps = 0;
		while (true) {
			if (nrSteps++ >= minNrSteps && rndSource.nextDouble() < stopProb)
				break;
			Optional<String> input = Optional.empty();
			if (alwaysOptimal || rndSource.nextDouble() >= probRandomSample) {

				input = adversary.optimalInput();
				if (input.isPresent())
					advChoices++;
				if (lastAdversary != null) {
					Optional<String> lastInput = lastAdversary.optimalInput();
					if (lastInput.isPresent() && input.isPresent() && lastInput.get().equals(input.get())) {
						advAgreeingChoices++;
					}
				}
			}
			String actualInput = input//.map(i -> inputSymbol(i))
					.orElse(inputAlphabet[rndSource.nextInt(inputAlphabet.length)].stringRepresentation());

			String output = adapter.execute(actualInput);
			stringContent.add(inputOutputStep(actualInput, output));
			adversary.executeStep(actualInput, output);
			if (lastAdversary != null)
				lastAdversary.executeStep(actualInput, output);
			if (alternativeStoppingCriterion != null && alternativeStoppingCriterion.test(stringContent))
				break;
		}
		totalNrSteps += nrSteps;
		return new FiniteString<>(stringContent, outputSymbol(initialOutput));
	}

	@Override
	public void init(String propertiesFile, int selectedProperty) throws IOException {
		super.init(propertiesFile, selectedProperty);
		this.round = 0;
		this.lastAdversary = null;
		this.adversary = null;
		this.probRandomSample = initialProbRandomSample;
		if (property.isSafety() && property.isMax()) {
			trainingProperty = property.invertOptimisationDirection();
			trainingProperty.setSteps(-1);
		} else if (!property.isSafety()) { // this should probably work in all
											// cases
			trainingProperty = property;
		} else {
			throw new UnsupportedOperationException("training might not work with this property");
		}
	}

	@Override
	public boolean finishedExploring() {
		return this.probRandomSample < EPS_EXPLORATION;
	}

	@Override
	protected FiniteString<InputOutputStep> sampleForEvaluation() {
		if (noAdversary)
			return sampleTraceUniformly();
		return sampleTraceWithAdversary(true, getProperty().getSteps());
	}

	@Override
	public boolean converging(double confidence, double threshhold) {
		if (lastAdversary == null || adversary == null)
			return false;
		// p = probability of agreement
		// p_0 = 1 - threshhold, if threshhold = 0.01 we test
		// H_0 : p <= p_0 against H_1: p > p_0, i.e. we check if we can
		// canclude that the probability of agreement between successive
		// schedulers
		// is greater than 0.99
		int n = advChoices;
		int s_n = advAgreeingChoices;
		System.out.println("Adv choices: " + advChoices + " where " + advAgreeingChoices + " are agreeing.");
		System.out.println(confidence + ". " + threshhold);
		double alpha = 1 - confidence;
		double p_0 = 1 - threshhold;
		if (s_n <= n * p_0){
			return false;
		}
		double p_val1 = 2 * binomialDist(p_0, n, s_n);
		double p_val2 = 2 * (1 - binomialDist(p_0, n, s_n));
		System.out.println(p_val1);
		System.out.println(p_val2);
		
		double p = Math.min(p_val1, p_val2);
		if (p / 2 <= alpha){
			return true;
		}else{
			return false;
		}
	}

	private double binomialDist(double p_0, int n, int s_n) {
		BinomialDistribution dist = new BinomialDistribution(n,p_0);
//		for (int k = 0; k <= s_n; k++) {
//			System.out.println(CombinatoricsUtils.binomialCoefficientDouble(n, k));
//			prob += CombinatoricsUtils.binomialCoefficientDouble(n, k) * Math.pow(p_0, k) * 
//					Math.pow((1 - p_0), n - k);
//		}
		return dist.cumulativeProbability(s_n);
	}

	public void setHybrid(boolean b) {
		this.hybrid = b;
	}

}
