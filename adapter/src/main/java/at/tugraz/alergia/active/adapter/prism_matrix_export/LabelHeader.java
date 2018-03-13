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
	private Map<String, Long> textualLabelToId = new HashMap<>();
	private static final int ID = 1;
	private static final int LABEL_TEXT = 2;

	private static final Pattern mappingPattern = Pattern.compile("(\\d+)=\"(\\w+)\"");
	private static final Long DUMMY_ID = -1l; // any ID that is not used by PRISM

	public LabelHeader(Map<Long, String> idToTextualLabel, Map<String, Long> textualLabelToId) {
		super();
		this.idToTextualLabel = idToTextualLabel;
		this.setTextualLabelToId(textualLabelToId);
	}

	public static LabelHeader fromString(String textual) {
		String[] labelMappingStrings = textual.split(" ");
		Map<Long, String> idToTextualLabel = new HashMap<>();
		Map<String, Long> textualLabelToId = new HashMap<>();
		Arrays.stream(labelMappingStrings).forEach(labelMappingStr -> {
			Matcher matcher = mappingPattern.matcher(labelMappingStr);
			MatrixExportAdapter.checkMatch(matcher,textual);
			
			idToTextualLabel.put(Long.parseLong(matcher.group(ID)), matcher.group(LABEL_TEXT));
			textualLabelToId.put(matcher.group(LABEL_TEXT), Long.parseLong(matcher.group(ID)));
		});
		return new LabelHeader(idToTextualLabel,textualLabelToId);
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

	public Map<String, Long> getTextualLabelToId() {
		return textualLabelToId;
	}

	public void setTextualLabelToId(Map<String, Long> textualLabelToId) {
		this.textualLabelToId = textualLabelToId;
	}

	public Long getTextualLabelToId(String label) {
		Long id = textualLabelToId.get(label);
		if(id == null)
			return DUMMY_ID;
		return id;
	}
}