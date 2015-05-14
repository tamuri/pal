// MultiRateMatrixHandler.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: MultiRateMatrixHandler </p>
 * <p>Description: A utility class to manage the new style of rate matrices, with more than one rate matrix grouped together (general site-class stuff) </p>
 * @author Matthew Goode
 * @version 1.0
 * <ul>
 *  <li> 14 August 2004 - Created, still a work in progress </li>
 * </ul>
 */
import pal.misc.NeoParameterized;
import java.io.PrintWriter;
public class MultiRateMatrixHandler implements NeoParameterized, java.io.Serializable {
	private final static double SUBSTITUTION_CLASS_LOWER_LIMIT = 1;
	private final static double SUBSTITUTION_CLASS_UPPER_LIMIT = 100000000;
	private final static double SUBSTITUTION_CLASS_DEFAULT_VALUE = 100;

	private final NeoRateMatrix[] rateMatrices_;
	private final double[] rateParameters_;
	private final double[] substitutionClassProbabilities_;
	private final double[] rateParametersSE_;
	private final double[] defaultRateParameters_;
	private boolean updateMatrix_ = true;
	private final double[] equilibriumFrequencies_;
	private final int dimension_;

	private final double[][] relativeRateStore_;
	private final double[][][] qMatrixStores_;

	private final MatrixExponential[] matrixExps_;

	private final boolean reversible_;

	private final int[] baseLookup_;
	private final int[] rateParameterIndexLookup_;


	private MultiRateMatrixHandler(MultiRateMatrixHandler toCopy) {
	  this(toCopy.rateMatrices_, toCopy.equilibriumFrequencies_,toCopy.substitutionClassProbabilities_);
		System.arraycopy(toCopy.rateParameters_, 0, rateParameters_,0,dimension_);
		System.arraycopy(toCopy.rateParametersSE_, 0, rateParametersSE_,0,dimension_);
	}

	public MultiRateMatrixHandler(NeoRateMatrix[] rateMatrices, double[] equilibriumFrequencies, double[] initialClassProportions) {
		this.rateMatrices_ = rateMatrices;
		this.dimension_ = rateMatrices[0].getDimension();
		for(int i = 1 ; i < rateMatrices.length ; i++ ){
		  if(rateMatrices[i].getDimension()!=dimension_) {
			  throw new IllegalArgumentException("Incompatible dimensions:"+dimension_+" to "+rateMatrices[i].getDimension());
			}
		}
		int totalNumberOfParameters = 0;
		for(int i = 0 ; i < rateMatrices.length ; i++) {
		  totalNumberOfParameters += rateMatrices[i].getNumberOfRateParameters();
		}
		this.rateParameters_ = new double[totalNumberOfParameters];
		this.rateParametersSE_ = new double[totalNumberOfParameters];
		this.defaultRateParameters_ = new double[totalNumberOfParameters];
		int index=0;
		for(int i = 0 ; i < rateMatrices.length ; i++) {
			rateMatrices[i].getDefaultRateParameters( defaultRateParameters_, index );
			index+=rateMatrices[i].getNumberOfRateParameters();
		}
		System.arraycopy(defaultRateParameters_,0,rateParameters_,0,rateParameters_.length);
		this.equilibriumFrequencies_ = pal.math.MathUtils.getNormalized(equilibriumFrequencies);
		this.relativeRateStore_ = new double[dimension_][dimension_];
		this.qMatrixStores_ = new double[rateMatrices_.length][dimension_][dimension_];
		matrixExps_ = new MatrixExponential[rateMatrices.length];
		for(int i = 0 ; i < matrixExps_.length ; i++) {
			matrixExps_[i] = new MatrixExponential( dimension_ );
		}
		boolean reversible = true;
		for(int i = 0 ; i < rateMatrices.length ; i++) {
		  reversible &= rateMatrices_[i].isReversible();
		}
		this.baseLookup_ = new int[totalNumberOfParameters];
		this.rateParameterIndexLookup_ = new int[totalNumberOfParameters];
		setupLookups(rateMatrices,baseLookup_,rateParameterIndexLookup_,totalNumberOfParameters);
		this.reversible_ = reversible;
		this.substitutionClassProbabilities_ = new double[rateMatrices.length];
		setSubstitutionClassProbabilities(initialClassProportions);
		parametersChanged();
  }
	private final void normaliseSubstitutionClassProbabilities() {
	  double total = 0;
		for(int i = 0 ; i < substitutionClassProbabilities_.length ; i++) {
		  total+=substitutionClassProbabilities_[i];
		}
		if(total>0) {
		  for(int i = 0 ; i < substitutionClassProbabilities_.length ; i++) {
			  substitutionClassProbabilities_[i]/=total;
	  	}
		} else {
		  for(int i = 0 ; i < substitutionClassProbabilities_.length ; i++) {
			  substitutionClassProbabilities_[i]=1.0/(double)substitutionClassProbabilities_.length;
	  	}
		}
	}
	public void setSubstitutionClassProbabilities(double[] classProportions) {
	  setSubstitutionClassProbabilities(classProportions,0);
	}
	public void setSubstitutionClassProbabilities(double[] classProportions, int startIndex) {
	  System.arraycopy(classProportions,startIndex,substitutionClassProbabilities_,0,substitutionClassProbabilities_.length);
	  parametersChanged();
	}
	public final int getNumberOfSubstitutionClasses() {
	  return substitutionClassProbabilities_.length;
	}

	public final MultiRateMatrixHandler getCopy() { return new MultiRateMatrixHandler(this); }

	public final double[] getEquilibriumFrequencies() { return equilibriumFrequencies_; }

	private final void checkMatrix() {
	  if(updateMatrix_) {
			System.out.println("Updating parameters:"+pal.misc.Utils.toString(rateParameters_));
			int index = 0 ;
		  double scale = 0;
			for(int i = 0 ; i < rateMatrices_.length ; i++) {
			  rateMatrices_[i].createRelativeRates(relativeRateStore_,rateParameters_,index);
		    fromQToR(relativeRateStore_,equilibriumFrequencies_,qMatrixStores_[i],dimension_,rateMatrices_[i].isReversible());
			  scale+=substitutionClassProbabilities_[i]*makeValid(qMatrixStores_[i],equilibriumFrequencies_,dimension_);
			  index+=rateMatrices_[i].getNumberOfRateParameters();
			}
		  for(int i = 0 ; i < rateMatrices_.length ; i++) {
				scale( qMatrixStores_[i], dimension_, scale );
			  matrixExps_[i].updateByRelativeRates(qMatrixStores_[i]);
			}
			updateMatrix_ = false;
		}
	}


	public void getTransitionProbabilities(double distance, double[][][] store ) {
		checkMatrix();
		for(int i = 0 ; i < rateMatrices_.length ; i++) {
			matrixExps_[i].setDistance( distance );
			matrixExps_[i].getTransitionProbabilities( store[i] );
		}
	}
	public void getTransitionProbabilities(double distance, int category, double[][] store ) {
		checkMatrix();
		matrixExps_[category].setDistance( distance );
		matrixExps_[category].getTransitionProbabilities( store );
	}
	public void getTransitionProbabilitiesTranspose(double distance, double[][][] store ) {
		checkMatrix();
		for(int i = 0 ; i < rateMatrices_.length ; i++) {
			matrixExps_[i].setDistanceTranspose( distance );
			matrixExps_[i].getTransitionProbabilities( store[i] );
		}
	}
	public void getTransitionProbabilitiesTranspose(double distance, int category, double[][] store ) {
		checkMatrix();
		matrixExps_[category].setDistanceTranspose( distance );
		matrixExps_[category].getTransitionProbabilities( store );

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
	public final double getSubstitutionClassLowerLimit() { return SUBSTITUTION_CLASS_LOWER_LIMIT; }
	public final double getSubstitutionClassUpperLimit() { return SUBSTITUTION_CLASS_UPPER_LIMIT; }
	public final double getSubstitutionClassDefaultValue() { return SUBSTITUTION_CLASS_DEFAULT_VALUE; }

	public double getLowerLimit(int n) { return rateMatrices_[baseLookup_[n]].getRateParameterLowerBound(rateParameterIndexLookup_[n]); }
	public double getUpperLimit(int n) { return rateMatrices_[baseLookup_[n]].getRateParameterUpperBound(rateParameterIndexLookup_[n]); }
	public double getDefaultValue(int n) { return defaultRateParameters_[n]; }
	public int getNumberOfParameters() { return rateParameters_.length; }
	public void setAllParameters(double[] rateParameters, double[] classProportions) {
	  System.arraycopy(classProportions,0,substitutionClassProbabilities_,0,substitutionClassProbabilities_.length);
	  System.arraycopy(rateParameters,0,rateParameters_,0,rateParameters_.length);
		parametersChanged();
	}
	public void setParameters(double[] parameters, int startIndex) {
	  System.arraycopy(parameters,startIndex,rateParameters_,0,rateParameters_.length);
		parametersChanged();
	}
	public void getParameters(double[] parameterStore, int startIndex) {
	  System.arraycopy(rateParameters_,0,parameterStore,startIndex,rateParameters_.length);
	}

	public void getDefaultValues(double[] store, int startIndex) {
	  System.arraycopy(defaultRateParameters_,0,store,startIndex,defaultRateParameters_.length);
	}

	private final static void setupLookups(NeoRateMatrix[] bases, int[] baseLookup, int[] parameterIndexLookup, int totalNumberOfParameters) {
		int baseIndex = 0;
		int parameterIndex = 0;
		for(int i = 0 ; i < totalNumberOfParameters ; i++) {
			while(bases[baseIndex].getNumberOfRateParameters()<=parameterIndex) {
				baseIndex++;
				parameterIndex = 0;
			}
			baseLookup[i] = baseIndex;
			parameterIndexLookup[i] = parameterIndex;
			parameterIndex++;
		}
	}
}