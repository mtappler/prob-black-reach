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

import at.tugraz.alergia.Alergia;
import at.tugraz.alergia.active.strategy.TestStrategy;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.automata.McTransformer;
import at.tugraz.alergia.automata.states.McStateFactory;
import at.tugraz.alergia.automata.states.MdpStateFactory;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.pta.IOFPTANodeFactory;
import at.tugraz.alergia.pta.PTANodeFactory;

public class ActiveTestingStrategyInference {
	private static final double EPSILON_FACTOR = 100.0;
	private static final double DEFAULT_EPS_LEARNING = Math.pow(10, -6);
	private int maxNrRounds = -1;
	private Alergia<InputOutputStep> alergia = null;
	private McStateFactory<InputOutputStep> stateFactory = new MdpStateFactory();
	private PTANodeFactory<InputOutputStep> nodeFactory = new IOFPTANodeFactory();
	private McTransformer mcTransformer = new McTransformer();
	private TestStrategy strategy = null;
	private double errorEpsilon = 0.01;
	private double confidenceDelta = 0.01;
	private long lastRunDuration = 0;
	private int convergenceRounds = 3; // 10 for emqtt, 5 for TCP
	private double convergenceDelta = 0.01; // 0.01 for emqtt
	private double z_alpha =  2.5758;
	
	private boolean convergenceCheck = false;

	public ActiveTestingStrategyInference(int maxNrRounds, TestStrategy strategy) {
		this.maxNrRounds = maxNrRounds;
		this.alergia = new Alergia<>(DEFAULT_EPS_LEARNING);
		this.strategy = strategy;
		// this is probably only smart for maximising the propability of satisfying a property
		// for reachability, we don't want to have chaos states for all outputs,
		// as an adversary
		// may try to reach those steps because the probability a reaching a
		// certain label there may be high
		// for safety we don't want a dontknow-chaos state because by reaching
		// such a state we would
		// be able to avoid labels we don't want to see so we should rather have
		// labels for all outputs (as
		// it is better to avoid places with uncertainty)
		// FOR NOW: only true as we have no properties other than reachability
		this.mcTransformer.setDontKnowChaos(true);
		Property property = strategy.getProperty();
		alergia.setAdditionalCompatibilityCheck(
				Optional.of((string1, string2) -> property.evaluate(string1) == property.evaluate(string2)));
		// this does not do anything for !F<s("end") since this is almost always the last symbol in a string
		alergia.setMaxDepthAddtional(0); 
	}

	public int getConvergenceRounds() {
		return convergenceRounds;
	}

	public void setConvergenceRounds(int convergenceRounds) {
		this.convergenceRounds = convergenceRounds;
	}

	public double getConvergenceDelta() {
		return convergenceDelta;
	}

	public void setConvergenceDelta(double convergenceDelta) {
		this.convergenceDelta = convergenceDelta;
	}

	public double getZ_alpha() {
		return z_alpha;
	}

	public void setZ_alpha(double z_alpha) {
		this.z_alpha = z_alpha;
	}

	public boolean isConvergenceCheck() {
		return convergenceCheck;
	}

	public void setConvergenceCheck(boolean convergenceCheck) {
		this.convergenceCheck = convergenceCheck;
	}

	public double run() throws Exception {
		
		List<FiniteString<InputOutputStep>> sample = new ArrayList<>();
		int round = 0;
		long startTime = System.currentTimeMillis();
		
		for (round = 0; round < maxNrRounds; round++) {
			List<FiniteString<InputOutputStep>> currSample = strategy.sample();
			sample.addAll(currSample);
			MarkovChain<InputOutputStep> mdp = learn(sample);
			strategy.update(mdp);
			double freqTrueInSample = (double) currSample.stream()
					.filter(t->strategy.getProperty().evaluate(t)).count() / currSample.size();
				
			if(convergenceCheck && converging(sample, currSample.size(), round)){
				System.out.println("Converging");
				break;
			}
			System.out.println("Frequency in sample: " + freqTrueInSample);
		}
		lastRunDuration = System.currentTimeMillis() - startTime;
		System.out.println("Total nr steps: " + strategy.getTotalNrSteps() + " after " + round + " rounds.");
		double result = strategy.evaluate(errorEpsilon, confidenceDelta);
		return result;
	}

	private boolean converging(List<FiniteString<InputOutputStep>> sample, int sampleSizeRound, int round) {
		if(round < convergenceRounds * 2)
			return false;
		int n = sampleSizeRound * convergenceRounds;
		List<FiniteString<InputOutputStep>> oldSamples = sample.subList((round - convergenceRounds * 2) * sampleSizeRound, 
				(round - convergenceRounds)* sampleSizeRound );
		
		List<FiniteString<InputOutputStep>> newSamples = sample.subList((round - convergenceRounds) * sampleSizeRound, round * sampleSizeRound );
		double px = oldSamples.stream().filter(t -> strategy.getProperty().evaluate(t)).count() / (double)n;
		double py = newSamples.stream().filter(t -> strategy.getProperty().evaluate(t)).count() / (double)n;

		double l_1 = (-Math.sqrt(n) * z_alpha * Math.sqrt(4*n*px + n*z_alpha*z_alpha - 4*px*px) +2*n*px + 
				n*z_alpha*z_alpha) / (2*(n*n+n*z_alpha*z_alpha));
		
		double l_2 = (-Math.sqrt(n) * z_alpha * Math.sqrt(4*n*py + n*z_alpha*z_alpha - 4*py*py) +2*n*py + 
				n*z_alpha*z_alpha) / (2*(n*n+n*z_alpha*z_alpha));
		
		double u_1 = (Math.sqrt(n) * z_alpha * Math.sqrt(4*n*px + n*z_alpha*z_alpha - 4*px*px) +2*n*px + 
				n*z_alpha*z_alpha) / (2*(n*n+n*z_alpha*z_alpha));
		double u_2 = (Math.sqrt(n) * z_alpha * Math.sqrt(4*n*py + n*z_alpha*z_alpha - 4*py*py) +2*n*py + 
				n*z_alpha*z_alpha) / (2*(n*n+n*z_alpha*z_alpha));

		double smallDelta = z_alpha * Math.pow(l_1*(1-l_1)/n + u_2*(1-u_2)/n, 0.5);
		double epsilon = z_alpha * Math.pow(u_1*(1-u_1)/n + l_2*(1-l_2)/n, 0.5);
		double L = px - py - smallDelta;
		double U = px - py + epsilon;
		System.out.println(px);
		System.out.println(py);
		System.out.println(L);
		System.out.println(U);
		
		return L >= -convergenceDelta && U <= convergenceDelta && L <= U;
	}

	private MarkovChain<InputOutputStep> learn(List<FiniteString<InputOutputStep>> sample) {
		double epsilon = EPSILON_FACTOR / strategy.getTotalNrSteps();
		System.out.println("Epsilon: " + epsilon);
		alergia.setEpsilon(epsilon);
		MarkovChain<InputOutputStep> mc = alergia.runAlergia(sample, stateFactory, nodeFactory);
		mcTransformer.reNameStateIds(mc);
		mcTransformer.completeModel(stateFactory, mc);
		return mc;
	}

	public int getMaxNrRounds() {
		return maxNrRounds;
	}

	public void setMaxNrRounds(int maxNrRounds) {
		this.maxNrRounds = maxNrRounds;
	}

	public McStateFactory<InputOutputStep> getStateFactory() {
		return stateFactory;
	}

	public void setStateFactory(McStateFactory<InputOutputStep> stateFactory) {
		this.stateFactory = stateFactory;
	}

	public PTANodeFactory<InputOutputStep> getNodeFactory() {
		return nodeFactory;
	}

	public void setNodeFactory(PTANodeFactory<InputOutputStep> nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

	public McTransformer getMcTransformer() {
		return mcTransformer;
	}

	public void setMcTransformer(McTransformer mcTransformer) {
		this.mcTransformer = mcTransformer;
	}

	public TestStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(TestStrategy strategy) {
		this.strategy = strategy;
	}

	public double getErrorEpsilon() {
		return errorEpsilon;
	}

	public void setErrorEpsilon(double errorEpsilon) {
		this.errorEpsilon = errorEpsilon;
	}

	public double getConfidenceDelta() {
		return confidenceDelta;
	}

	public void setConfidenceDelta(double confidenceDelta) {
		this.confidenceDelta = confidenceDelta;
	}

	public long getLastRunDuration() {
		return lastRunDuration;
	}

	public void setLastRunDuration(long lastRunDuration) {
		this.lastRunDuration = lastRunDuration;
	}

	
}
