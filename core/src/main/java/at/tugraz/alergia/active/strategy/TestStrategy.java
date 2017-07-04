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
package at.tugraz.alergia.active.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import at.tugraz.alergia.active.Property;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;

public abstract class TestStrategy {
	
	protected Adapter adapter;
	protected String propertiesFile;
	protected int selectedProperty;
	protected int batchSize;
	protected Random rndSource = null;
	protected double stopProb;
	protected InputSymbol[] inputAlphabet = null;
	protected Property property;
	protected Predicate<List<InputOutputStep>> alternativeStoppingCriterion = null;
	protected long totalNrSteps;
	// an estimation of the probability, for instance calculated by PRISM 
	protected Optional<Double> additionalProbEstimation = Optional.empty();
	
	public TestStrategy(Adapter adapter, int batchSize, long seed, double stopProb, InputSymbol[] inputAlphabet){
		this.adapter = adapter;
		this.batchSize = batchSize;
		this.rndSource = new Random(seed);
		this.stopProb = stopProb;
		this.inputAlphabet = inputAlphabet;
	}

	public abstract void update(MarkovChain<InputOutputStep> mdp) throws Exception;

	public Optional<Double> getAdditionalProbEstimation() {
		return additionalProbEstimation;
	}

	public void setAdditionalProbEstimation(Optional<Double> additionalProbEstimation) {
		this.additionalProbEstimation = additionalProbEstimation;
	}

	public abstract List<FiniteString<InputOutputStep>> sample();
	
	
	public void init(String propertiesFile, int selectedProperty) throws IOException {
		this.propertiesFile = propertiesFile;
		this.selectedProperty = selectedProperty;
		this.property = new Property(propertiesFile,selectedProperty);
		this.additionalProbEstimation = Optional.empty();
		this.totalNrSteps = 0;
	}

	public double evaluate(double errorEpsilon, double confidenceDelta) {
		int nrSamples = chernoffHoeffdingBound(errorEpsilon,confidenceDelta);
		System.out.println(nrSamples + " required");

		// we don't want to include steps performed during evaluation 
		long totalNrStepsSave = totalNrSteps;
		int nrSuccess = 0;
		for(int i = 0; i < nrSamples; i ++){
			FiniteString<InputOutputStep> trace = sampleForEvaluation();
//			System.out.println(trace.toString().contains("crash") + " " + trace.size());
			nrSuccess += getProperty().evaluate(trace) ? 1 : 0;	
		}
		double estProb = (double)nrSuccess / nrSamples;
		
		totalNrSteps = totalNrStepsSave;
		System.out.println("Estimated probability for property " + estProb);		
		return estProb;
	}
	
	protected abstract FiniteString<InputOutputStep> sampleForEvaluation();

	protected List<FiniteString<InputOutputStep>> sampleUniformly() {
		List<FiniteString<InputOutputStep>> traces = new ArrayList<>();
		for(int i = 0;i < batchSize; i ++){
			traces.add(sampleTraceUniformly());
		}
		return traces;
	}

	protected FiniteString<InputOutputStep> sampleTraceUniformly() {
		return sampleTraceUniformly(property.getSteps());
	}
	protected FiniteString<InputOutputStep> sampleTraceUniformly(int minNrSteps) {
		List<InputOutputStep> stringContent = new ArrayList<>();
		String initialOutput = adapter.reset();
		int nrSteps = 0;
		while(true){
			if(nrSteps++ >= minNrSteps && rndSource.nextDouble() < stopProb)
				break;
			InputSymbol input = inputAlphabet[rndSource.nextInt(inputAlphabet.length)];
			String output = adapter.execute(input.stringRepresentation());
			stringContent.add(new InputOutputStep(input, new OutputSymbol(output)));
			if(alternativeStoppingCriterion != null && alternativeStoppingCriterion.test(stringContent))
				break;
		}
		totalNrSteps += nrSteps;
		return new FiniteString<>(stringContent, new OutputSymbol(initialOutput));
	}
	public void setAlternativeStoppingCriterion(Predicate<List<InputOutputStep>> alternativeStoppingCriterion) {
		this.alternativeStoppingCriterion = alternativeStoppingCriterion;
	}

	public boolean checksSafetyProperty() {
		return property.isSafety();
	}

	public Property getProperty() {
		return property;
	}

	protected int chernoffHoeffdingBound(double errorEpsilon, double confidenceDelta) {
		return (int)Math.ceil((1.0/(2*Math.pow(errorEpsilon, 2))) * Math.log(2/confidenceDelta));
	}
	
	public long getTotalNrSteps() {
		return totalNrSteps;
	}

	public abstract boolean finishedExploring();

	public void setSeed(long seed) {
		this.rndSource = new Random(seed);
	}

}
