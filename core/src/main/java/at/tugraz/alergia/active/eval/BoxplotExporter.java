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
package at.tugraz.alergia.active.eval;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.tuple.Pair;

import at.tugraz.alergia.active.Experiment;

public class BoxplotExporter {

	public static final double BOX_EXTEND = 0.3;
	private boolean simulatedProbs = true;

	public BoxplotExporter(boolean simulatedProbs, String pathToPrism, String prismFile, String propertyFile) {
		super();
		this.simulatedProbs = simulatedProbs;
		this.pathToPrism = pathToPrism;
		this.prismFile = prismFile;
		this.propertyFile = propertyFile;
	}

	private String pathToPrism;
	private String prismFile;
	private String propertyFile;

	public String export(List<Integer> stepBounds, List<Integer> boxProperties, List<Experiment> experiments,
			List<String> colours) throws Exception {
		StrBuilder sb = new StrBuilder();
		List<String> ticks = stepBounds.stream().map(Object::toString).collect(Collectors.toList());
		boxPlotPreamble(sb, ticks);
		double offset = -BOX_EXTEND * (experiments.size() / 2);
		int j = 0;
		for (Experiment exp : experiments) {
			String colour = colours.get(j);
			for (int i = 0; i < boxProperties.size(); i++) {
				Integer p = boxProperties.get(i);
				int stepBound = stepBounds.get(i);
				BoxData data = exp.getBoxplotData(p, simulatedProbs);
				appendSingleBoxPlot(sb, offset, colour, stepBound, data);
			}
			offset += BOX_EXTEND;
			j++;
		}
		sb.appendln("\\addplot[color=black,dashed,mark=] coordinates {");
		for (int i = 0; i < boxProperties.size(); i++) {
			int stepBound = stepBounds.get(i);
			double optimalValue = getOptimalValue(boxProperties.get(i));
			sb.appendln(String.format("(%f,%f)", stepBound - BOX_EXTEND * (experiments.size() / 2) - BOX_EXTEND / 2,
					optimalValue));
			sb.appendln(String.format("(%f,%f)", stepBound + BOX_EXTEND * (experiments.size() / 2) + BOX_EXTEND / 2,
					optimalValue));
		}
		sb.appendln("};");
		boxPlotEnd(sb);

		return sb.toString();
	}

	public static void boxPlotEnd(StrBuilder sb) {
		sb.appendln("\\end{axis}");
		sb.appendln("\\end{tikzpicture}");
	}

	public static void boxPlotPreamble(StrBuilder sb, List<String> ticks) {
		generalPreamble(sb, ticks);
		sb.appendln(",boxplot/draw direction=y]");
	}

	public static void generalPreamble(StrBuilder sb, List<String> ticks) {
		sb.appendln("\\begin{tikzpicture}");
		sb.appendln("\\begin{axis}[width=\\textwidth,height=.35\\textwidth,");
		sb.appendln("xtick={%s},", String.join(",", ticks));
		sb.appendln("xticklabels={%s}", String.join(",", ticks));
	}

	public static void appendSingleBoxPlot(StrBuilder sb, double offset, String colour, int stepBound, BoxData data) {
		sb.appendln(String.format(
				"\\addplot+[color = %s, mark=x, solid,boxplot prepared={box extend=%.2f, draw position = %.2f,"
						+ "lower whisker=%.4f, lower quartile=%.4f," + "median=%.4f, upper quartile=%.4f,"
						+ "upper whisker=%.4f}]",
				colour, BOX_EXTEND, stepBound - offset, data.lowerWhisker, data.lowerBorder, data.middle,
				data.upperBorder, data.upperWhisker));

		sb.appendln("coordinates {"
				+ data.outliers.stream().map(o -> "(0," + o + ")").collect(Collectors.joining(" ")) + "};");
	}

	private double getOptimalValue(Integer property) throws Exception {
		Pair<String, Double> trueValuePair = EvalUtil.trueValueAndPropertyPairFromPRISM(pathToPrism, prismFile,
				propertyFile, property);
		return trueValuePair.getRight();
	}

	public boolean isSimulatedProbs() {
		return simulatedProbs;
	}

	public void setSimulatedProbs(boolean simulatedProbs) {
		this.simulatedProbs = simulatedProbs;
	}
}
