// ConstantIndependentSingleSplitDistribution.java
//
// (c) 1999-2004 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: ConstantIndependentSingleSplitDistribution </p>
 * <p>Description: Allows for a split in substitution model parameters where the class distribution before the split is the same as after the split (and the probabilities of being in a before class and an after class are independent)</p>
 * @author Matthew Goode
 */

public class ConstantIndependentSingleSplitDistribution implements SingleSplitDistribution{
  private final double[][] probabilityStore_;
	private final double[] parameterStore_;
	private final double[] normalisedParameterStore_;
	private final int numberOfBaseTransitionCategories_;

	private final static double PARAMETER_BASE = 0.00000001;
	private final static double PARAMETER_MINIMUM = 0+PARAMETER_BASE;
	private final static double PARAMETER_DEFAULT = 0.5+PARAMETER_BASE;
	private final static double PARAMETER_MAXIMUM = 1+PARAMETER_BASE;

	public ConstantIndependentSingleSplitDistribution(int numberOfBaseTransitionCategories) {
		this(numberOfBaseTransitionCategories,null);
	}
	public ConstantIndependentSingleSplitDistribution(int numberOfBaseTransitionCategories, double[] initialParameters) {
	  this.numberOfBaseTransitionCategories_ = numberOfBaseTransitionCategories;
		this.probabilityStore_ = new double[numberOfBaseTransitionCategories][numberOfBaseTransitionCategories];
		this.parameterStore_ = new double[numberOfBaseTransitionCategories];
		this.normalisedParameterStore_ = new double[numberOfBaseTransitionCategories];
		if(initialParameters==null) {
			for( int i = 0; i<parameterStore_.length; i++ ) {
				parameterStore_[i] = PARAMETER_DEFAULT;
			}
		} else {

			if(initialParameters.length == numberOfBaseTransitionCategories) {
				//Traditional values supplied
				for(int i = 0 ; i < numberOfBaseTransitionCategories ; i++) {
				  parameterStore_[i] = initialParameters[i];
				}
			} else {
			  throw new IllegalArgumentException("Cannot handle "+initialParameters.length+" initial parameters");
			}
		}
		correctParameters(parameterStore_);
		updateProbabilityStore();
		System.out.println("Constant Independent probability model created:"+this);

	}
	public double[] getDistribution() {
		double[] distribution = new double[numberOfBaseTransitionCategories_*numberOfBaseTransitionCategories_];
		for(int i = 0 ; i < distribution.length ; i++) {
			final int a = i%numberOfBaseTransitionCategories_;
			final int b = i/numberOfBaseTransitionCategories_;
			distribution[i] = normalisedParameterStore_[a]*normalisedParameterStore_[b];
		}
		return distribution;
	}

	public int getNumberOfBaseTransitionCategories() {
		return numberOfBaseTransitionCategories_;
	}
	public double[][] getDistributionInfo() {
		return probabilityStore_;
	}
	private final void updateProbabilityStore() {
		double total = 0;
		for(int i = 0 ; i < parameterStore_.length ; i++) {
			total+=parameterStore_[i];
		}
		for(int i = 0 ; i < normalisedParameterStore_.length ; i++) {
		  normalisedParameterStore_[i] = parameterStore_[i]/total;
		}
		int parameterIndex =0;
		for(int before=0 ; before < numberOfBaseTransitionCategories_ ; before++) {
			for(int after=0 ; after < numberOfBaseTransitionCategories_ ; after++) {
				probabilityStore_[before][after] = normalisedParameterStore_[before]*normalisedParameterStore_[after];
			}
		}
	}
	public int getNumberOfParameters() { return numberOfBaseTransitionCategories_; }
	public void setParameters(double[] store, int startIndex) {
		System.arraycopy(store,startIndex,parameterStore_,0,parameterStore_.length);
		updateProbabilityStore();
	}
	public void getParameters(double[] store, int startIndex) {
		System.arraycopy(parameterStore_,0,store,startIndex,parameterStore_.length);
	}
	public double getLowerLimit(int n) { return PARAMETER_MINIMUM; }
	public double getUpperLimit(int n) { return PARAMETER_MAXIMUM; }
	private static final void correctParameters(double[] parameters) {
	  for(int i = 0 ; i < parameters.length ; i++) {
		  double p = parameters[i]+PARAMETER_BASE;
			if(p<PARAMETER_MINIMUM) {
			  p = PARAMETER_MINIMUM;
			} else if(p>PARAMETER_MAXIMUM) {
			  p = PARAMETER_MAXIMUM;
			}
			parameters[i] = p;
		}
	}
	public void getDefaultValues(double[] store, int startIndex) {
		for( int i = 0; i<parameterStore_.length; i++ ) {
			store[i+startIndex] = PARAMETER_DEFAULT;
		}
	}
	public String toString() {
		return "Constant Independent("+pal.misc.Utils.toString(parameterStore_)+" - > "+pal.misc.Utils.toString(getDistribution())+")";
	}
}