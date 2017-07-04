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
package at.tugraz.alergia.pta;

import java.util.stream.Stream;

import at.tugraz.alergia.automata.SampleData;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.OutputStep;
import at.tugraz.alergia.data.OutputSymbol;
import at.tugraz.alergia.data.TimeStep;
import at.tugraz.alergia.util.StringUtil;

public class CTFPTANode extends PTANode<TimeStep> {
	protected double cachedTimeDelay = -1;
	protected SampleData<TimeStep> sampleDataCache = null;

	public CTFPTANode(OutputSymbol label, PTATransition<TimeStep> incomingTransition) {
		super(label, incomingTransition);
	}

	@Override
	public boolean localCompatible(PTANode<TimeStep> q_b, SampleData<TimeStep> sampleData, double epsilon) {
		// TODO account for certainty
		if (sampleData != sampleDataCache) {
			sampleDataCache = sampleData;
			cachedTimeDelay = -1;
		}
		int n_1 = transitionFrequency();
		int n_2 = q_b.transitionFrequency();
		if (n_1 == 0 || n_2 == 0)
			return true;
		for (OutputSymbol o : sampleData.getOutputAlphabet()) {
			int f_1 = transitionFrequency(o);
			int f_2 = q_b.transitionFrequency(o);
			boolean hoeffdingTest = hoeffdingTest(epsilon, n_1, n_2, f_1, f_2);
			if (!hoeffdingTest)
				return false;
		}

		return fTest(q_b, sampleData, n_1, n_2, epsilon);
	}

	private boolean fTest(PTANode<TimeStep> q_b, SampleData<TimeStep> sampleData, double n_r, int n_b,
			/* n_r and n_b to match paper */ double epsilon) {
		if (n_r <= 1 || n_b <= 2) {
			return true;
		}
		double mu = n_b / (n_b - 1);
		double sigma = Math.sqrt(n_b * n_b * (n_r + n_b - 1) / (n_r * (n_b - 1) * (n_b - 1) * (n_b - 2)));
		double gamma_1 = mu - sigma / Math.sqrt(epsilon);
		double gamma_2 = mu + sigma / Math.sqrt(epsilon);
		double timeDelayRatio = averageEmpiricalTimeDelay() / ((CTFPTANode) q_b).averageEmpiricalTimeDelay();
		return gamma_1 <= timeDelayRatio && timeDelayRatio <= gamma_2;
	}

	protected double averageEmpiricalTimeDelay() {

		if (cachedTimeDelay < 0) {
			FiniteString<OutputStep> skeletonStringFromRoot = StringUtil.skeleton(getStringFromRoot());
			Stream<FiniteString<TimeStep>> X = sampleDataCache.getSample().stream()
					.filter(rho -> skeletonStringFromRoot.prefixOf(StringUtil.skeleton(rho)));
			Stream<Double> delaysInCurrentStep = X
					.map(timeString -> timeString.get(skeletonStringFromRoot.size()).getDelay());
			cachedTimeDelay = 1.0 / transitionFrequency() * delaysInCurrentStep.mapToDouble(Double::doubleValue).sum();
		}

		return cachedTimeDelay;
	}
}
