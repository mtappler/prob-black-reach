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
package at.tugraz.alergia.active.adapter.prism_matrix_export;

import java.util.Arrays;
import java.util.List;

public class StateHeader {
	private List<String> varNames = null;

	public StateHeader(List<String> varNames) {
		super();
		this.varNames = varNames;
	}

	public static StateHeader fromString(String textual) {
		String[] varNamesArray = textual.replaceAll("\\(", "").replaceAll("\\)", "").split(",");
		return new StateHeader(Arrays.asList(varNamesArray));
	}

	public List<String> getVarNames() {
		return varNames;
	}

	public void setVarNames(List<String> varNames) {
		this.varNames = varNames;
	}
}