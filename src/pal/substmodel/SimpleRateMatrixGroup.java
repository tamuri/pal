package pal.substmodel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import pal.datatype.*;

public abstract class SimpleRateMatrixGroup implements RateMatrixGroup {
  private final MultiRateMatrixHandler rateMatrixHandler_;

	private final int numberOfRateMatrixParameters_;
	private final int numberOfSiteClasses_;
	private final int totalNumberOfParameters_;
	private final DataType dataType_;

	public SimpleRateMatrixGroup(NeoRateMatrix[] baseMatrices,  double[] equilibriumFrequencies, double[] initialClassProbabilities, DataType dataType) {
		this.rateMatrixHandler_ = new MultiRateMatrixHandler(baseMatrices,equilibriumFrequencies, initialClassProbabilities);
		this.dataType_ = dataType;
		for(int i = 0 ; i < baseMatrices.length ; i++) {
		  if(baseMatrices[i].getDimension()!=dataType.getNumStates()) {
			  throw new IllegalArgumentException("Data type incompatible with one or more of the base matrices");
			}
		}
		this.numberOfSiteClasses_ = rateMatrixHandler_.getNumberOfSubstitutionClasses();
		this.numberOfRateMatrixParameters_ = rateMatrixHandler_.getNumberOfParameters();
		this.totalNumberOfParameters_ = numberOfSiteClasses_+numberOfRateMatrixParameters_;
	}
	public int getNumberOfParameters() { return totalNumberOfParameters_; }
	public double getLowerLimit(int n) {
		if( n<numberOfSiteClasses_ ) {
			return rateMatrixHandler_.getSubstitutionClassLowerLimit();
		} else {
			return rateMatrixHandler_.getLowerLimit( n-numberOfSiteClasses_ );
		}
	}
	public double getUpperLimit(int n) {
		if( n<numberOfSiteClasses_ ) {
			return rateMatrixHandler_.getSubstitutionClassUpperLimit();
		} else {
			return rateMatrixHandler_.getUpperLimit( n-numberOfSiteClasses_ );
		}
	}
	public double getDefaultValue(int n) {
		if( n<numberOfSiteClasses_ ) {
			return rateMatrixHandler_.getSubstitutionClassDefaultValue();
		} else {
			return rateMatrixHandler_.getDefaultValue( n-numberOfSiteClasses_ );
		}
	}
	public void setParameters(double[] parameters, double[] categoryProbabilities) {
	  rateMatrixHandler_.setAllParameters(parameters,categoryProbabilities);
	}

	public double[] getEquilibriumFrequencies() { return rateMatrixHandler_.getEquilibriumFrequencies(); }

	public DataType getDataType() { return dataType_; }
	public int getNumberOfTransitionCategories() { return numberOfSiteClasses_; }
	public void getTransitionProbabilities(double branchLength, double[][][] tableStore) {
	  rateMatrixHandler_.getTransitionProbabilities(branchLength,tableStore);
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, double[][][] tableStore) {
	  rateMatrixHandler_.getTransitionProbabilitiesTranspose(branchLength,tableStore);
	}
	public void getTransitionProbabilities(double branchLength, int category, double[][] tableStore) {
	  rateMatrixHandler_.getTransitionProbabilities(branchLength,category, tableStore);
	}
	public void getTransitionProbabilitiesTranspose(double branchLength, int category, double[][] tableStore) {
	  rateMatrixHandler_.getTransitionProbabilitiesTranspose(branchLength,category, tableStore);
	}
	public String getSummary(double[] parameters, double[] categoryProbabilities) {
		return "?";
	}
}