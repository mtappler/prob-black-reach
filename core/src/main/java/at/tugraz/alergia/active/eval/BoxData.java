package at.tugraz.alergia.active.eval;

import java.util.List;

public class BoxData {
	double middle = 0;
	double lowerBorder = 0;
	double upperBorder = 0;
	double lowerWhisker = 0;
	double upperWhisker = 0;
	List<Double> outliers = null;

	public BoxData(double middle, double lowerBorder, double upperBorder, double lowerWhisker, double upperWhisker,
			List<Double> outliers) {
		super();
		this.middle = middle;
		this.lowerBorder = lowerBorder;
		this.upperBorder = upperBorder;
		this.lowerWhisker = lowerWhisker;
		this.upperWhisker = upperWhisker;
		this.outliers = outliers;
	}

}