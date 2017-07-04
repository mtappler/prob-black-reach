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

import java.util.List;

import at.tugraz.alergia.active.adapter.Adapter;
import at.tugraz.alergia.automata.MarkovChain;
import at.tugraz.alergia.data.FiniteString;
import at.tugraz.alergia.data.InputOutputStep;
import at.tugraz.alergia.data.InputSymbol;

public class UniformSelectionTestStrategy extends TestStrategy{

	public UniformSelectionTestStrategy(Adapter adapter, int batchSize, long seed, double stopProb,
			InputSymbol[] inputAlphabet) {
		super(adapter, batchSize, seed, stopProb, inputAlphabet);
	}

	@Override
	public void update(MarkovChain<InputOutputStep> mdp) throws Exception {
		// no-op
	}

	@Override
	public List<FiniteString<InputOutputStep>> sample() {
		return sampleUniformly();
	}


	@Override
	public boolean finishedExploring() {
		return true;
	}

	@Override
	protected FiniteString<InputOutputStep> sampleForEvaluation() {
		return sampleTraceUniformly();
	}

}
