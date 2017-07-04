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

import at.tugraz.alergia.active.Property;
import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.active.strategy.TestStrategy;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.util.export.RMLExporterMDP;

public class AdversaryBasedTestStrategy extends TestStrategy {

	private static final double EPS_EXPLORATION = 0.01;
	private String pathToPrism;
	private int round = 0; // keep track of rounds internally
	private RMLExporterMDP exporter = new RMLExporterMDP("learned_model");
	private int stepBound;
	private String pathToTmpFiles = "tmp/";
	private Adversary adversary = null;
	private double probRandomSample;
	private double probRandomChangeFactor;
	private Property trainingProperty = null;
	private double initialProbRandomSample;
	private boolean noAdversary = true;
	public static final Pattern PRISM_PROB_CALC = Pattern.compile(
			"Value in the initial state: (1\\.\\d+|1|0\\.0|0|0\\.\\d+)");
	
	public AdversaryBasedTestStrategy(Adapter adapter, int stepBound, int batchSize,long seed, double stopProb, 
			InputSymbol[] inputAlphabet, double probRandomSampleInit, double probRandomChangeFactor,String pathToPrism) {
		super(adapter,batchSize,seed, stopProb, inputAlphabet);
		this.pathToPrism = pathToPrism;
		this.stepBound = stepBound;
		this.probRandomSample = probRandomSampleInit;
		this.probRandomChangeFactor = probRandomChangeFactor;
		this.initialProbRandomSample = probRandomSampleInit;
	}

	@Override
	public void update(MarkovChain<InputOutputStep> mdp) throws Exception {
		round ++;
		String learnedModelFileName = createTempFile(mdp);
		String advFileName = String.format("%sadv_%d.tra",pathToTmpFiles,round);
		String concreteModelName = String.format("%sout_%d",pathToTmpFiles,round);
		String trainingPropertyString = trainingProperty.toString();
		String prismCall = String.format(
				"%s %s -pf %s -noprob1 -exportadvmdp %s -exportmodel %s.all",
				pathToPrism,learnedModelFileName,trainingPropertyString,advFileName,concreteModelName);
	    callPrism(prismCall);
	    if(!noAdversary)
	    	readAdvFile(advFileName,concreteModelName);
	    else 
	    	System.out.println("Warning: no adversary created!");
	}

	private void readAdvFile(String advFileName, String concreteModelName) throws IOException {
		adversary = new Adversary(advFileName,concreteModelName);
	}

	private void callPrism(String prismCall) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(prismCall);
	    BufferedReader reader = 
	         new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line = "";
	    boolean advCreated = false;
	    while ((line = reader.readLine())!= null) {
	    	if(line.contains("Adversary written to") || line.contains("Model checking: ") || 
	    			line.contains("Value in the initial state") )
	    		System.out.println(line);
	    	if(line.contains("Adversary written to"))
	    		advCreated = true;
    		if(line.contains("Value in the initial state")){
    			Matcher m = PRISM_PROB_CALC.matcher(line);
    			m.matches();
    			// FIXME if this is zero then there is no adversary
    			// if there exists an adversary from a previous round, then this will be used
    			// and it's probably to sample uniformly in this case
    			additionalProbEstimation = Optional.of(Double.parseDouble(m.group(1)));
    		}
	    }
	    noAdversary  = !advCreated;
	    reader.close();
	}

	private String createTempFile(MarkovChain<InputOutputStep> mdp) throws IOException {
 	   	File temp = File.createTempFile("mdp_" + round + "_", ".tmp");
 	   	String rmlString = exporter.toRML(mdp, stepBound);
 	   try (FileWriter writer = new FileWriter(temp)) {
 		   writer.write(rmlString);
 		   writer.flush();
 	   }

 	   return temp.getAbsolutePath();
	}

	@Override
	public List<FiniteString<InputOutputStep>> sample() {
		if(round == 0 || noAdversary){
			return sampleUniformly();
		}
		return sampleWithAdversary();
	}

	private List<FiniteString<InputOutputStep>> sampleWithAdversary() {
		List<FiniteString<InputOutputStep>> traces = new ArrayList<>();
		for(int i = 0;i < batchSize; i ++){
			traces.add(sampleTraceWithAdversary(false));
		}
		probRandomSample *= probRandomChangeFactor;
		return traces;
	}

	private FiniteString<InputOutputStep> sampleTraceWithAdversary(boolean alwaysOptimal) {
		return sampleTraceWithAdversary(alwaysOptimal,property.getSteps());
	}

	private FiniteString<InputOutputStep> sampleTraceWithAdversary(boolean alwaysOptimal, int minNrSteps) {
		List<InputOutputStep> stringContent = new ArrayList<>();
		String initialOutput = adapter.reset();
		adversary.reset();
		int nrSteps = 0;
		while(true){
			if(nrSteps++ >= minNrSteps && rndSource.nextDouble() < stopProb)
				break;
			Optional<String> input = Optional.empty();
			if(alwaysOptimal || rndSource.nextDouble() >= probRandomSample){
				input = adversary.optimalInput();
			}
			InputSymbol actualInput = 
					input.map(i -> new InputSymbol(i)).orElse(
					inputAlphabet[rndSource.nextInt(inputAlphabet.length)]);
			
			String output = adapter.execute(actualInput.stringRepresentation());
			stringContent.add(new InputOutputStep(actualInput, new OutputSymbol(output)));
			adversary.executeStep(actualInput.stringRepresentation(), output);
			if(alternativeStoppingCriterion != null && alternativeStoppingCriterion.test(stringContent))
				break;
		}
		totalNrSteps += nrSteps;
		return new FiniteString<>(stringContent, new OutputSymbol(initialOutput));
	}

	@Override
	public void init(String propertiesFile, int selectedProperty) throws IOException {
		super.init(propertiesFile, selectedProperty);
		this.round = 0;
		this.probRandomSample = initialProbRandomSample;
		if(property.isSafety() && property.isMax()){
			trainingProperty = property.invertOptimisationDirection();
			trainingProperty.setSteps(-1);
		} else if(!property.isSafety()){ // this should probably work in all cases
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
		if(noAdversary)
			return sampleTraceUniformly();
		return sampleTraceWithAdversary(true,getProperty().getSteps());
	}

}
