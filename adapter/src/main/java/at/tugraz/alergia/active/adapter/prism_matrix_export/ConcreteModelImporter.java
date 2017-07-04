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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ConcreteModelImporter {

	public static final String LABEL_FILE_EXT = ".lab";
	public static final String TRANS_FILE_EXT = ".tra";
	public static final String STATE_FILE_EXT = ".sta";
	
	
	public Pair<LabelHeader,Map<Long, StateLabels>> readLabelsFile(String labelsFileName) throws IOException {
		Pair<LabelHeader, List<StateLabels>> readContent = readAndTransformFile(labelsFileName, StateLabels::fromString,
				LabelHeader::fromString);
		Map<Long, StateLabels> labels = new HashMap<>();
		readContent.getRight().forEach(l -> labels.put(l.getStateId(), l));
		return new ImmutablePair<LabelHeader, Map<Long,StateLabels>>(readContent.getLeft(), labels);
	}

	public Pair<TransitionHeader,Map<Long, Set<Transition>>> readTransFile(String transitionsFileName) throws IOException {
		return readTransFile(transitionsFileName,false);
	}
	public Pair<TransitionHeader,Map<Long, Set<Transition>>> readTransFile(String transitionsFileName,boolean ignoreHeader) 
			throws IOException {
		Pair<TransitionHeader, List<Transition>> readContent = readAndTransformFile(transitionsFileName,
				Transition::fromString, TransitionHeader::fromString, ignoreHeader);
		Map<Long,Set<Transition>> transitions = new HashMap<>();
		readContent.getRight().forEach(t -> {
			if (!transitions.containsKey(t.getSourceId()))
				// I think I should use a linked hash set to ensure that the
				// iterations through
				// the set will return elements in the same order, but maybe a
				// HashSet would suffice
				transitions.put(t.getSourceId(), new LinkedHashSet<>());
			transitions.get(t.getSourceId()).add(t);
		});
		return new ImmutablePair<TransitionHeader, Map<Long,Set<Transition>>>(readContent.getLeft(), transitions);
	}
	public Pair<StateHeader,Map<Long,State>> readStatesFile(String statesFileName) throws IOException {

		Pair<StateHeader, List<State>> readContent = readAndTransformFile(statesFileName, State::fromString,
				StateHeader::fromString);
		Map<Long,State> states = new HashMap<>();
		readContent.getRight().forEach(s -> states.put(s.getId(), s));
		
		return new ImmutablePair<>(readContent.getLeft(), states);
	}

	private <T, H> Pair<H, List<T>> readAndTransformFile(String fileName, Function<String, T> mapper,
			Function<String, H> headerMapper) throws IOException {
		return readAndTransformFile(fileName, mapper, headerMapper,false);
	}
	private <T, H> Pair<H, List<T>> readAndTransformFile(String fileName, Function<String, T> mapper,
				Function<String, H> headerMapper,boolean ignoreHeader) throws IOException {
		Pair<String, Stream<String>> fileContent = linesInFile(fileName);
		
		H mappedHeader = null;
		if(!ignoreHeader)
			mappedHeader = headerMapper.apply(fileContent.getLeft());
		return new ImmutablePair<>(mappedHeader,
				fileContent.getRight().map(mapper).collect(Collectors.toList()));
	}

	private Pair<String, Stream<String>> linesInFile(String fileName) throws IOException {
		File file = new File(fileName);
		List<String> lines = Files.readAllLines(file.toPath());
		String firstElem = lines.remove(0);
		return new ImmutablePair<String, Stream<String>>(firstElem, lines.stream());
	}
	
	public State findInitState(boolean removeInitLabel, LabelHeader labelHeader, Map<Long, StateLabels> labels,
			Map<Long, State> states) {
		Long initLabelId = -1l;
		for (Long labelId : labelHeader.getIdToTextualLabel().keySet()) {
			if ("init".equals(labelHeader.getIdToTextualLabel().get(labelId))) {
				initLabelId = labelId;
			}
		}

		for (StateLabels l : labels.values()) {
			if (l.getLabelIds().contains(initLabelId)) {
				Long initStateId = l.getStateId();
				if(removeInitLabel){
					l.getLabelIds().remove((Long)initLabelId);
				}
				return states.get(initStateId);
				// make sure it is List.remove(Object o); and not List.remove(int index);
			}
		}
		return null;
	}
}
