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
package at.tugraz.alergia.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProbabilityDistribution<T> {
	private Map<T,Double> actualDistribution = null;

	public ProbabilityDistribution(Map<T, Double> actualDistribution) {
		super();
		this.actualDistribution = actualDistribution;
	}

	public static <T> ProbabilityDistribution<T> 
					  singletonDistribution(T elemWithProbOne){
		Map<T,Double> actualDistribution = new HashMap<>();
		// all others are implicitly zero
		actualDistribution.put(elemWithProbOne, 1.0);
		return new ProbabilityDistribution<>(actualDistribution);
	}
	public Optional<T> singleElem(){
		if(actualDistribution.size() == 1){
			return Optional.of(actualDistribution.keySet().iterator().next());
		}
		else {
			return Optional.empty();
		}
	}
}
