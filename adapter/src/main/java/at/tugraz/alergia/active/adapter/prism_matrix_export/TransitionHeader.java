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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransitionHeader {

	private static final int NR_STATES = 1;
	@SuppressWarnings("unused")
	private static final int NR_CHOICE = 2;
	private static final int NR_TRANS = 3;

	private long nrStates = -1;
	private long nrTrans = -1;

	public TransitionHeader(long nrStates, long nrTrans) {
		super();
		this.nrStates = nrStates;
		this.nrTrans = nrTrans;
	}

	private static final Pattern formatPattern = Pattern.compile("(\\d+) (\\d+) (\\d+)");

	public static TransitionHeader fromString(String textual) {
		Matcher matcher = formatPattern.matcher(textual);
		MatrixExportAdapter.checkMatch(matcher,textual);
		
		long nrStates = Long.parseLong(matcher.group(NR_STATES));
		long nrTrans = Long.parseLong(matcher.group(NR_TRANS));
		return new TransitionHeader(nrStates, nrTrans);
	}

	public long getNrStates() {
		return nrStates;
	}

	public void setNrStates(long nrStates) {
		this.nrStates = nrStates;
	}

	public long getNrTrans() {
		return nrTrans;
	}

	public void setNrTrans(long nrTrans) {
		this.nrTrans = nrTrans;
	}
}