package at.tugraz.alergia.active.eval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.text.StrBuilder;

import at.tugraz.alergia.active.Experiment;

public class RoundEvaluationsExporter {

	public RoundEvaluationsExporter(String fileName) {
		super();
		this.fileName = fileName;
	}

	private String fileName = null;
	public String createBoxGraph() throws IOException {
		File file = new File(fileName);
		List<String> lines = Files.readAllLines(file.toPath());

		List<List<Double>> parsedLines = lines.stream()
				.map(l -> Arrays.stream(l.split(",")).map(Double::parseDouble).collect(Collectors.toList()))
				.collect(Collectors.toList());
		int nrElems = parsedLines.get(0).size(); // assume all arrays are equally
												// long

		StrBuilder sb = new StrBuilder();
		List<String> ticks = IntStream.rangeClosed(0, nrElems).filter(i -> i % 10 == 0)
				.mapToObj(i -> new Integer(i).toString()).collect(Collectors.toList());
		BoxplotExporter.generalPreamble(sb, ticks);
		sb.appendln(String.format(",xmax=%d,xmin=%d",nrElems,0));
		sb.appendln("]");
		appendStatistic(parsedLines, nrElems, sb, elems -> elems.min(Double::compareTo).get());
		appendStatistic(parsedLines, nrElems, sb, elems -> Experiment.quantile(elems.collect(Collectors.toList()),0.25));
		appendStatistic(parsedLines, nrElems, sb, elems -> Experiment.quantile(elems.collect(Collectors.toList()),0.5));
		appendStatistic(parsedLines, nrElems, sb, elems -> Experiment.quantile(elems.collect(Collectors.toList()),0.75));
		appendStatistic(parsedLines, nrElems, sb, elems -> elems.max(Double::compareTo).get());
		BoxplotExporter.boxPlotEnd(sb);
		return sb.toString();
	}
	public String createMinMaxPlot() throws IOException {
		File file = new File(fileName);
		List<String> lines = Files.readAllLines(file.toPath());

		List<List<Double>> parsedLines = lines.stream()
				.map(l -> Arrays.stream(l.split(",")).map(Double::parseDouble).collect(Collectors.toList()))
				.collect(Collectors.toList());
		int nrElems = parsedLines.get(0).size(); // assume all arrays are equally
												// long

		StrBuilder sb = new StrBuilder();
		List<String> ticks = IntStream.rangeClosed(0, nrElems).filter(i -> i % 10 == 0)
				.mapToObj(i -> new Integer(i).toString()).collect(Collectors.toList());
		BoxplotExporter.generalPreamble(sb, ticks);
		sb.appendln(String.format(",xmax=%d,xmin=%d",nrElems,0));
		sb.appendln("]");
		appendStatistic(parsedLines, nrElems, sb, elems -> elems.min(Double::compareTo).get());
		appendStatistic(parsedLines, nrElems, sb, elems -> elems.mapToDouble(d -> d).average().getAsDouble());
		appendStatistic(parsedLines, nrElems, sb, elems -> elems.max(Double::compareTo).get());
		BoxplotExporter.boxPlotEnd(sb);
		return sb.toString();
	}

	private void appendStatistic(List<List<Double>> parsedLines, int nrElems, StrBuilder sb,
			Function<Stream<Double>, Double> statComputation) {
		sb.appendln("\\addplot[color=black,mark=] coordinates {");
		for (int i = 0; i < nrElems; i++) {
			final int finalI = i;
			Stream<Double> currentElems = parsedLines.stream().map(ds -> ds.get(finalI));
			Double statistic = statComputation.apply(currentElems);
			sb.append(String.format("(%d,%f) ", finalI + 1, statistic));
		}
		sb.appendln("};");
	}

	public String createBoxPlot() throws IOException {
		File file = new File(fileName);
		List<String> lines = Files.readAllLines(file.toPath());

		List<String[]> splitLines = lines.stream().map(l -> l.split(",")).collect(Collectors.toList());
		int nrElems = splitLines.get(0).length; // assume all arrays are equally
												// long
		BoxData[] data = new BoxData[nrElems];
		for (int i = 0; i < nrElems; i++) {
			final int finalI = i;
			List<Double> currentElems = splitLines.stream().map(sl -> Double.parseDouble(sl[finalI]))
					.collect(Collectors.toList());

			double middle = Experiment.median(currentElems);
			double lowerBorder = Experiment.quantile(currentElems, 0.25);
			double upperBorder = Experiment.quantile(currentElems, 0.75);

			double iqr = Experiment.iqr(lowerBorder, upperBorder);
			double lowerWhisker = Experiment.lowerWhisker(currentElems, lowerBorder, iqr);
			double upperWhisker = Experiment.upperWhisker(currentElems, upperBorder, iqr);
			List<Double> outliers = Experiment.outliers(currentElems, lowerWhisker, lowerWhisker);
			data[i] = new BoxData(middle, lowerBorder, upperBorder, lowerWhisker, upperWhisker, outliers);
		}
		StrBuilder sb = new StrBuilder();
		List<String> ticks = IntStream.rangeClosed(0, nrElems).filter(i -> i % 5 == 0)
				.mapToObj(i -> new Integer(i).toString()).collect(Collectors.toList());
		BoxplotExporter.boxPlotPreamble(sb, ticks);
		for (int i = 0; i < data.length; i++) {
			BoxplotExporter.appendSingleBoxPlot(sb, 0, "black", i + 1, data[i]);
		}
		BoxplotExporter.boxPlotEnd(sb);

		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		String file = "/home/mtappler/Arbeit/Dependable_IoT/repos/"
				+ "dependableThingsSP3/development/active_alergia/parent/core/log/eval_each_round/log_first_gridworld_d10_more_rounds.log";
		RoundEvaluationsExporter export = new RoundEvaluationsExporter(file);
		System.out.println(export.createBoxGraph());
	}

}
