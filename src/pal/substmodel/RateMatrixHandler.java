// RateMatrixHandler.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: RateMatrixHandler </p>
 * <p>Description: A utility class to manage the new style of rate matrices </p>
 * @author Matthew Goode
 * @version 1.0
 * <ul>
 *  <li> 11 May 2004 - Created, still a work in progress </li>
 * </ul>
 */
import pal.misc.Parameterized;
import java.io.PrintWriter;
public class RateMatrixHandler implements Parameterized, java.io.Serializable {
	private final NeoRateMatrix rateMatrix_;
	private final double[] parameters_;
	private final double[] parametersSE_;
	private final double[] defaultParameters_;
	private boolean updateMatrix_ = true;
	private final double[] equilibriumFrequencies_;
	private final int dimension_;

	private final double[][] relativeRateStore_;
	private final double[][] qMatrixStore_;

	private final MatrixExponential matrixExp_;

	private final boolean reversible_;

	private RateMatrixHandler(RateMatrixHandler toCopy) {
	  this(toCopy.rateMatrix_, toCopy.equilibriumFrequencies_);
		System.arraycopy(toCopy.parameters_, 0, parameters_,0,dimension_);
		System.arraycopy(toCopy.parametersSE_, 0, parametersSE_,0,dimension_);
	}

	public RateMatrixHandler(NeoRateMatrix rateMatrix, double[] equilibriumFrequencies) {
		this.rateMatrix_ = rateMatrix;
		System.out.println("Number of Parameters:"+rateMatrix.getNumberOfRateParameters());
		this.dimension_ = rateMatrix_.getDimension();
		this.parameters_ = new double[rateMatrix.getNumberOfRateParameters()];
		this.parametersSE_ = new double[parameters_.length];
		this.defaultParameters_ = new double[parameters_.length];
		rateMatrix.getDefaultRateParameters(defaultParameters_,0);
		System.arraycopy(defaultParameters_,0,parameters_,0,parameters_.length);
		this.equilibriumFrequencies_ = pal.misc.Utils.getCopy(equilibriumFrequencies);
		this.relativeRateStore_ = new double[dimension_][dimension_];
		this.qMatrixStore_ = new double[dimension_][dimension_];
		matrixExp_ = new MatrixExponential(dimension_);
		this.reversible_ = rateMatrix_.isReversible();
  }

	public final RateMatrixHandler getCopy() { return new RateMatrixHandler(this); }

	public final double[] getEquilibriumFrequencies() { return equilibriumFrequencies_; }

	private final void checkMatrix() {
	  if(updateMatrix_) {
		  rateMatrix_.createRelativeRates(relativeRateStore_,parameters_,0);
		  fromQToR(relativeRateStore_,equilibriumFrequencies_,qMatrixStore_,dimension_,reversible_);
			double scale = makeValid(qMatrixStore_,equilibriumFrequencies_,dimension_);
			scale(qMatrixStore_,dimension_,scale);
			matrixExp_.updateByRelativeRates(qMatrixStore_);
			updateMatrix_ = false;
		}
	}


	public void getTransitionProbabilities(double distance, double[][] store ) {
		checkMatrix();
		matrixExp_.setDistance(distance);
		matrixExp_.getTransitionProbabilities(store);
	}
	public void getTransitionProbabilitiesTranspose(double distance, double[][] store ) {
		checkMatrix();
		matrixExp_.setDistanceTranspose(distance);
		matrixExp_.getTransitionProbabilities(store);
	}


	// interface Report (remains abstract)

	// interface Parameterized (remains abstract)

	/** Computes normalized rate matrix from Q matrix (general reversible model)
	 * - Q_ii = 0
	 * - Q_ij = Q_ji
	 * - Q_ij is stored in R_ij (rate)
	 * - only upper triangular is used
	 * Also updates related MatrixExponential
	 */
	private static final void fromQToR(double[][] relativeRates, double[] equilibriumFrequencies, double[][] qMatrix, int dimension, boolean reversible) {
		if(reversible) {
			for( int i = 0; i<dimension; i++ ) {
				for( int j = i+1; j<dimension; j++ ) {
					qMatrix[i][j] = relativeRates[i][j]*equilibriumFrequencies[j];
					qMatrix[j][i] = relativeRates[i][j]*equilibriumFrequencies[i];
				}
			}
		} else {
		  for( int i = 0; i<dimension; i++ ) {
				for( int j = i+1; j<dimension; j++ ) {
					qMatrix[i][j] = relativeRates[i][j]*equilibriumFrequencies[j];
					//This is the only difference
					qMatrix[j][i] = relativeRates[j][i]*equilibriumFrequencies[i];
				}
			}
		}
	}

	//
	// Private stuff
	//

	/** Make it a valid rate matrix (make sum of rows = 0)
		* @return current rate scale
		*/
	private static final  double makeValid(double[][] relativeRates, double[] equilibriumFrequencies, int dimension) {
		double total = 0;
		for (int i = 0; i < dimension ; i++){
			double sum = 0.0;
			for (int j = 0; j < dimension ; j++)	{
				if (i != j)	{
					sum += relativeRates[i][j];
				}
			}
			relativeRates[i][i] = -sum;
			total+=equilibriumFrequencies[i]*sum;
		 }
		 return total;
	}
	private final static double calculateNormalScale(double[][] relativeRates, double[] equilibriumFrequencies, int dimension) {
	  double scale = 0.0;

		for (int i = 0; i < dimension; i++)	{
			scale += -relativeRates[i][i]*equilibriumFrequencies[i];
		}
		return scale;
	}

	// Normalize rate matrix to one expected substitution per unit time
	private static final void normalize(final double[][] relativeRates, double[] equilibriumFrequencies, int dimension) {
		scale(relativeRates,dimension, calculateNormalScale(relativeRates,equilibriumFrequencies,dimension));
	}
	 // Normalize rate matrix by a certain scale to acheive an overall scale (used with a complex site class model)
	private static final void scale(final double[][] relativeRates,int dimension, double scale)  {
		for (int i = 0; i < dimension; i++)  {
			for (int j = 0; j < dimension; j++)  {
				relativeRates[i][j] = relativeRates[i][j]/scale;
			}
		}
	}

	/**
	 * ensures that frequencies are not smaller than MINFREQ and
	 * that two frequencies differ by at least 2*MINFDIFF.
	 * This avoids potentiak problems later when eigenvalues
	 * are computed.
	 */
	private static final void checkFrequencies(final double[] frequencies, final int dimension)	{
		// required frequency difference
		final double MINFDIFF = 1e-10;

		// lower limit on frequency
		final double MINFREQ = 1e-10;

		int maxi = 0;
		double sum = 0.0;
		double maxfreq = 0.0;
		for (int i = 0; i < dimension; i++) {
			double freq = frequencies[i];
			if (freq < MINFREQ) { frequencies[i] = MINFREQ; }
			if (freq > maxfreq)	{
				maxfreq = freq;		maxi = i;
			}
			sum += frequencies[i];
		}
		frequencies[maxi] += 1.0 - sum;

		for (int i = 0; i < dimension - 1; i++)	{
			for (int j = i+1; j < dimension; j++)	{
				if (frequencies[i] == frequencies[j])	{
					frequencies[i] += MINFDIFF;		frequencies[j] -= MINFDIFF;
				}
			}
		}
	}

	/**
	 * Reporting stuff
	 * @param out where to report too
	 */
	public void report(PrintWriter out) {
	  out.println("Reporting Not functioning yet...");
	}

	private final void parametersChanged() { this.updateMatrix_ = true; }

	public int getNumParameters() { 	return parameters_.length;	}
	public void setParameter(double param, int n) { this.parameters_[n] = param; parametersChanged(); }
	public double getParameter(int n) { return parameters_[n]; }
	public void setParameterSE(double paramSE, int n) { this.parametersSE_[n] = paramSE; }
	public double getLowerLimit(int n) { return rateMatrix_.getRateParameterLowerBound(n); }
	public double getUpperLimit(int n) { return rateMatrix_.getRateParameterUpperBound(n); }
	public double getDefaultValue(int n) { return defaultParameters_[n]; }
}