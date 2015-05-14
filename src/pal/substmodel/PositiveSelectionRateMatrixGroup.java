// PositiveSelectionRateMatrixGroup.java
//
// (c) 1999-2004 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: PositiveSelectionRateMatrixGroup</p>
 * <p>Description: The M2 model of codon substitution </p>
 * @author Matthew Goode
 */
import pal.datatype.*;
import pal.misc.*;

public class PositiveSelectionRateMatrixGroup implements RateMatrixGroup {

	private static final int NUMBER_OF_CATEGORIES = 3;

	//The YangCodonModel has two parameters, Kappa and Omega. This value is not to be confused with the number of parameters of this matrix group (which also happens to be 2)
	private static final int NUMBER_OF_BASE_MATRIX_PARAMETERS = 2;

	private static final int PURIFYING_MATRIX_INDEX = 0;
	private static final int NEUTRAL_MATRIX_INDEX = 1;
	private static final int POSITIVE_MATRIX_INDEX = 2;

	private final YangCodonModel purifyingModel_;
	private final YangCodonModel neutralModel_;
	private final YangCodonModel positiveModel_;
	private final YangCodonModel[] allMatrices_;

	private final double[] freqs_;
	private final double[] purifyingParameters_;
	private final double[] neutralParameters_;
	private final double[] positiveParameters_;
	private final MutableDouble kappaStore_;
	private final MutableDouble omegaStore_;


	//
	// Serialization Code
	//
	private static final long serialVersionUID = 4829485859252L;

	public PositiveSelectionRateMatrixGroup(MutableDouble kappaStore, MutableDouble omegaStore, double[] freqs, CodonTable table) {
		this.freqs_ = pal.misc.Utils.getCopy(freqs);
		this.kappaStore_ = kappaStore;
		this.omegaStore_ = omegaStore;
		this.purifyingModel_ = new YangCodonModel(0,kappaStore.getValue(),freqs,table);
		this.neutralModel_ = new YangCodonModel(1,kappaStore.getValue(),freqs,table);
		this.positiveModel_ = new YangCodonModel(omegaStore.getValue(),kappaStore.getValue(),freqs,table);
		this.neutralParameters_ = new double[NUMBER_OF_BASE_MATRIX_PARAMETERS] ;
		this.positiveParameters_ = new double[NUMBER_OF_BASE_MATRIX_PARAMETERS] ;
		this.purifyingParameters_ = new double[NUMBER_OF_BASE_MATRIX_PARAMETERS] ;
		this.allMatrices_ = new YangCodonModel[NUMBER_OF_CATEGORIES];
		allMatrices_[PURIFYING_MATRIX_INDEX] = purifyingModel_;
		allMatrices_[POSITIVE_MATRIX_INDEX] = positiveModel_;
		allMatrices_[NEUTRAL_MATRIX_INDEX] = neutralModel_;
  }
	public int getNumberOfTransitionCategories() { return NUMBER_OF_CATEGORIES; }

	public void updateParameters(double[] categoryProbabilities) {
	  final double kappa = kappaStore_.getValue();
		final double omega = omegaStore_.getValue();
		purifyingParameters_[YangCodonModel.KAPPA_PARAMETER] =
		  neutralParameters_[YangCodonModel.KAPPA_PARAMETER] =
			positiveParameters_[YangCodonModel.KAPPA_PARAMETER] = kappa;
		purifyingParameters_[YangCodonModel.OMEGA_PARAMETER] = 0;
		neutralParameters_[YangCodonModel.OMEGA_PARAMETER] = 1;
		positiveParameters_[YangCodonModel.OMEGA_PARAMETER] = omega;
		double scale =
		  categoryProbabilities[PURIFYING_MATRIX_INDEX]*purifyingModel_.setParametersNoScale(purifyingParameters_)+
			categoryProbabilities[NEUTRAL_MATRIX_INDEX]*neutralModel_.setParametersNoScale(neutralParameters_)+
			categoryProbabilities[POSITIVE_MATRIX_INDEX]*positiveModel_.setParametersNoScale(positiveParameters_)
			;
		neutralModel_.scale(scale);
		purifyingModel_.scale(scale);
		positiveModel_.scale(scale);
	}

	public double[] getEquilibriumFrequencies() { return freqs_; }

	public DataType getDataType() { return Codons.DEFAULT_INSTANCE; }

	public void getTransitionProbabilities(double branchLength, double[][][] tableStore) {
	  for(int i = 0 ; i < NUMBER_OF_CATEGORIES ; i++) {
		  allMatrices_[i].setDistance(branchLength);
		  allMatrices_[i].getTransitionProbabilities(tableStore[i]);
		}
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, double[][][] tableStore) {
	  for(int i = 0 ; i < NUMBER_OF_CATEGORIES ; i++) {
		  allMatrices_[i].setDistanceTranspose(branchLength);
		  allMatrices_[i].getTransitionProbabilities(tableStore[i]);
		}
	}
	public void getTransitionProbabilities(double branchLength, int category, double[][] tableStore) {
	  RateMatrix rm = allMatrices_[category];
		rm.setDistance(branchLength);
	  rm.getTransitionProbabilities(tableStore);
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, int category, double[][] tableStore) {
		RateMatrix rm = allMatrices_[category];
		rm.setDistanceTranspose(branchLength);
	  rm.getTransitionProbabilities(tableStore);

	}
	public String getSummary(double[] categoryProbabilities) {
		return "Positive Selection (kappa = "+kappaStore_.getValue()+", omega = "+omegaStore_.getValue()+", p0 = "+categoryProbabilities[0]+", p1 =  "+categoryProbabilities[1]+", p2 =  "+categoryProbabilities[2]+")";
	}

}