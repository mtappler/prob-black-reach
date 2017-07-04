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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class State {
	private long id = -1;
	private List<String> values = null;
	private boolean initial = false;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (initial ? 1231 : 1237);
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		State other = (State) obj;
		if (id != other.id)
			return false;
		if (initial != other.initial)
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	private static final Pattern formatPattern = Pattern.compile("(\\d+):\\(((\\w+,)*\\w+)\\)");
	private static final int ID = 1;
	private static final int VALS = 2;

	public State(long id, List<String> values) {
		super();
		this.id = id;
		this.values = values;
	}

	public static State fromString(String textualState) {
		Matcher matcher = formatPattern.matcher(textualState);
		MatrixExportAdapter.checkMatch(matcher,textualState);
		
		String idString = matcher.group(ID);
		String labelString = matcher.group(VALS);
		long id = Long.parseLong(idString);
		List<String> valuesList = Arrays.asList(labelString.split(","));
		return new State(id, valuesList);
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}
}