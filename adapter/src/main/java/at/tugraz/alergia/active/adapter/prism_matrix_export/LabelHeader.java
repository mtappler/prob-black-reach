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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LabelHeader {
	private Map<Long, String> idToTextualLabel = new HashMap<>();
	private static final int ID = 1;
	private static final int LABEL_TEXT = 2;

	private static final Pattern mappingPattern = Pattern.compile("(\\d+)=\"(\\w+)\"");

	public LabelHeader(Map<Long, String> idToTextualLabel) {
		super();
		this.idToTextualLabel = idToTextualLabel;
	}

	public static LabelHeader fromString(String textual) {
		String[] labelMappingStrings = textual.split(" ");
		Map<Long, String> idToTextualLabel = new HashMap<>();
		Arrays.stream(labelMappingStrings).forEach(labelMappingStr -> {
			Matcher matcher = mappingPattern.matcher(labelMappingStr);
			MatrixExportAdapter.checkMatch(matcher,textual);
			
			idToTextualLabel.put(Long.parseLong(matcher.group(ID)), matcher.group(LABEL_TEXT));
		});
		return new LabelHeader(idToTextualLabel);
	}

	public Map<Long, String> getIdToTextualLabel() {
		return idToTextualLabel;
	}

	public void setIdToTextualLabel(Map<Long, String> idToTextualLabel) {
		this.idToTextualLabel = idToTextualLabel;
	}

	public String textualLabel(List<Long> outputLabelIds) {
		return outputLabelIds.stream()
				.map(labelId -> idToTextualLabel.get(labelId))
				.collect(Collectors.joining("&"));
	}
}