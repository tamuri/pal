// NeutralSelectionRateMatrixGroup.java
//
// (c) 1999-2004 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: NeutralSelectionRateMatrixGroup</p>
 * <p>Description: The M1 model of codon substitution </p>
 * @author Matthew Goode
 */

import pal.datatype.*;
import pal.misc.*;

public class NeutralSelectionRateMatrixGroup implements RateMatrixGroup {

	private final YangCodonModel purifyingModel_;
	private final YangCodonModel neutralModel_;
	private final double[] freqs_;
	private final double[] purifyingParameters_;
	private final double[] neutralParameters_;

	private final MutableDouble kappaStore_;


	//
	// Serialization Code
	//
	private static final long serialVersionUID = 84829859222352L;

	public NeutralSelectionRateMatrixGroup(MutableDouble kappaStore, double[] freqs, CodonTable table) {
		this.freqs_ = pal.misc.Utils.getCopy(freqs);
		this.kappaStore_ =kappaStore;
		this.purifyingModel_ = new YangCodonModel(0,kappaStore.getValue(),freqs,table);
		this.neutralModel_ = new YangCodonModel(1,kappaStore.getValue(),freqs,table);
		this.purifyingParameters_ = new double[2] ;
		this.neutralParameters_ = new double[2];
  }
	public void updateParameters(double[] categoryProbabilities) {
		final double kappa = kappaStore_.getValue();
		purifyingParameters_[YangCodonModel.KAPPA_PARAMETER] = kappa;
		purifyingParameters_[YangCodonModel.OMEGA_PARAMETER] = 0;
		neutralParameters_[YangCodonModel.KAPPA_PARAMETER] = kappa;
		neutralParameters_[YangCodonModel.OMEGA_PARAMETER] = 1;

		double scale =
		  categoryProbabilities[0]*purifyingModel_.setParametersNoScale(purifyingParameters_)+
			categoryProbabilities[1]*neutralModel_.setParametersNoScale(neutralParameters_)
			;
		neutralModel_.scale(scale);
		purifyingModel_.scale(scale);
	}
//	-900.9514906707137
//	-900.9562340445607


	public double[] getEquilibriumFrequencies() { return freqs_; }

	public DataType getDataType() { return Codons.DEFAULT_INSTANCE; }
	public int getNumberOfTransitionCategories() { return 2; }
	public void getTransitionProbabilities(double branchLength, double[][][] tableStore) {
	  purifyingModel_.setDistance(branchLength);
		neutralModel_.setDistance(branchLength);

		purifyingModel_.getTransitionProbabilities(tableStore[0]);
		neutralModel_.getTransitionProbabilities(tableStore[1]);

	}
	public void getTransitionProbabilitiesTranspose(double branchLength, double[][][] tableStore) {
	  purifyingModel_.setDistanceTranspose(branchLength);
		neutralModel_.setDistanceTranspose(branchLength);

		purifyingModel_.getTransitionProbabilities(tableStore[0]);
		neutralModel_.getTransitionProbabilities(tableStore[1]);
	}
	private final RateMatrix getRelatedMatrix(int category) {
	  return (category==0 ? purifyingModel_ : neutralModel_);
	}
	public void getTransitionProbabilities(double branchLength, int category, double[][] tableStore) {
	  RateMatrix rm = getRelatedMatrix(category);
		rm.setDistance(branchLength);
	  rm.getTransitionProbabilities(tableStore);
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, int category, double[][] tableStore) {
		 RateMatrix rm = getRelatedMatrix(category);
		rm.setDistanceTranspose(branchLength);
	  rm.getTransitionProbabilities(tableStore);

	}
	public String getSummary(double[] categoryProbabilities) {
		return "Neutral Selection (kappa = "+kappaStore_.getValue()+", p0 = "+categoryProbabilities[0]+", p1 =  "+categoryProbabilities[1]+")";
	}

}