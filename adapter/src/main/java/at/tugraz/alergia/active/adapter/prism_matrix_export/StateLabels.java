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
import java.util.stream.Collectors;

public class StateLabels {
	private long stateId = -1;
	// must be sorted 
	private List<Long> labelIds = null;

	public StateLabels(long stateId, List<Long> labelIds) {
		super();
		this.stateId = stateId;
		this.labelIds = labelIds;
	}

	private static final Pattern formatPattern = Pattern.compile("(\\d+): ((\\d+ )*\\d+)");
	private static final int STATE_ID = 1;
	private static final int LABEL_IDS = 2;

	public static StateLabels fromString(String textual) {
		Matcher matcher = formatPattern.matcher(textual);
		MatrixExportAdapter.checkMatch(matcher, textual);
		
		int stateId = Integer.parseInt(matcher.group(STATE_ID));
		List<Long> labelIds = Arrays.asList(matcher.group(LABEL_IDS).split(" ")).stream().map(Long::parseLong)
				.sorted().collect(Collectors.toList());
		return new StateLabels(stateId, labelIds);
	}

	public long getStateId() {
		return stateId;
	}

	public void setStateId(long stateId) {
		this.stateId = stateId;
	}

	public List<Long> getLabelIds() {
		return labelIds;
	}

	public void setLabelIds(List<Long> labelIds) {
		this.labelIds = labelIds;
	}

//	public Long getSingleLabel() {
//		if(labelIds.size() != 1)
//			throw new RuntimeException("More or less than one output per state -> not supported");
//		return labelIds.get(0);
//	}
}