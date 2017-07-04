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
package at.tugraz.alergia.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OutputSymbol implements Symbol{
	protected String symbol;
	protected Set<String> satisfiedProps = null;
	
	public static final OutputSymbol dont_know = new OutputSymbol("dont_know");
	
	public OutputSymbol(String symbol){
		this.symbol = symbol;
	}

	@Override
	public String stringRepresentation() {
		return symbol;
	}

	public Set<String> getSatisfiedProps(){
		if(satisfiedProps == null)
			satisfiedProps = new HashSet<>(Arrays.asList(symbol.split("&")));
		return satisfiedProps;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OutputSymbol other = (OutputSymbol) obj;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return stringRepresentation();
	}
}
