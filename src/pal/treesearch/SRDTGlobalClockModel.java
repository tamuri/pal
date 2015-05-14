// MRDTGlobalClockModel.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: SRDTGlobalClockModel </p>
 * <p>Description: A constraint model for the serial sample analysis case where a molecular clock is assumed and there is only one rate </p>
 * author Matthew Goode
 * @version 1.0
 */
import pal.misc.*;
import pal.eval.*;

public class SRDTGlobalClockModel implements ConstraintModel, ConstraintModel.GroupManager, NeoParameterized, MolecularClockLikelihoodModel.HeightConverter {
	private final SampleInformation sampleInformation_;
	private final MolecularClockLikelihoodModel.Instance likelihoodModel_;

	private final PrimaryParameters primaryParameters_;
	private final SecondaryParameters secondaryParameters_;


	private double rate_;
	private double upperRate_ = 1;
	private double metaRate_ = 1;
	private final boolean allowMetaRate_;

	private final static double RATE_LOWER_LIMIT = 0;
	private final static double RATE_UPPER_LIMIT = 10;

	private final static double[] DEFAULTS = { 0.1,1,1 };

	private final double maxLeafHeight_;

	public SRDTGlobalClockModel(SampleInformation sampleInformation, MolecularClockLikelihoodModel.Instance likelihoodModel) {
	  this(sampleInformation,likelihoodModel,false);
	}
	public SRDTGlobalClockModel(SampleInformation sampleInformation, MolecularClockLikelihoodModel.Instance likelihoodModel, boolean allowMetaRate) {
		this.sampleInformation_ = sampleInformation;
		this.maxLeafHeight_ = sampleInformation.getMaxHeight();
		this.likelihoodModel_ = likelihoodModel;
		this.primaryParameters_ = new PrimaryParameters();
		this.secondaryParameters_ = new SecondaryParameters();
		this.allowMetaRate_ = allowMetaRate;
	}
	public String getRateModelSummary() {
		return "Single Rate Dated Tips, rate = "+getSubstitutionRate();
	}
// =============================================================
// === ConstraintModel Stuff ===================================
	public GroupManager getGlobalClockConstraintGrouping(String[] leafLabelSet) { return this; }
	public String[][] getCladeConstraints(String[] allLabelSet) { return new String[][] { allLabelSet }; }

  public UnconstrainedLikelihoodModel.Leaf createNewFreeLeaf(int[] patternStateMatchup, int numberOfPatterns) { return null; }
	public UnconstrainedLikelihoodModel.External createNewFreeExternal() { return null; }
  public UnconstrainedLikelihoodModel.Internal createNewFreeInternal() { return null; }

	public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(  boolean isForLeaf ) {
		return likelihoodModel_.createAppropriateConditionalProbabilityStore(isForLeaf);
	}
	public double getSubstitutionRate() { return rate_; }
	public void setSubstitutionRate(double rate) {
		this.upperRate_ = upperRate_/metaRate_;
	  this.rate_ = rate;
		this.metaRate_ = 1;
	}
	private final void setBaseRate(double value) {	this.rate_ = allowMetaRate_ ? value*metaRate_ : value;	}
	private final void setBaseUpperRate(double value) {  this.upperRate_ = allowMetaRate_ ? value*metaRate_ : value;	}
	private final double getBaseRate() {	  return allowMetaRate_ ? rate_/metaRate_ : rate_;	}
	private final double getBaseUpperRate() {	  return allowMetaRate_ ? upperRate_/metaRate_ : upperRate_;	}
	private final void setMetaRate(double value) {
		if(!allowMetaRate_) { throw new RuntimeException("Assertion error : should be calling meta rate stuff as meta rate not allowed"); }
	  this.rate_ = getBaseRate()*value;
		this.upperRate_ = getBaseUpperRate()*value;
		this.metaRate_ = value;
	}
	private final void setBaseUpperRateAndMetaRate(double upperValue, double metaValue) {
		if(!allowMetaRate_) { throw new RuntimeException("Assertion error : should be calling meta rate stuff as meta rate not allowed"); }
	  this.rate_ = getBaseRate()*metaValue;
		this.upperRate_ = upperValue*metaValue;
		this.metaRate_ = metaValue;
	}
	private final double getMetaRate() {
		if(!allowMetaRate_) { throw new RuntimeException("Assertion error : should be calling meta rate stuff as meta rate not allowed"); }
		return metaRate_;
	}


	public NeoParameterized getGlobalParameterAccess() { return likelihoodModel_.getParameterAccess(); }

// =============================================================
// === GroupManagerStuff Stuff ===================================
	public double getLeafBaseHeight(String leafLabel) {
		return sampleInformation_.getHeight(sampleInformation_.getSampleOrdinal(leafLabel));
	}
	public double getBaseHeight(double originalExpectSubstitutionHeight) {
		double esMaxLeafHeight = maxLeafHeight_*rate_;
		if(originalExpectSubstitutionHeight<esMaxLeafHeight) {
			return originalExpectSubstitutionHeight/rate_;
		}
		return maxLeafHeight_+(originalExpectSubstitutionHeight-esMaxLeafHeight)/upperRate_;
	}

	public int getBaseHeightUnits() { return sampleInformation_.getHeightUnits(); }

	public void initialiseParameters(String[] leafNames, double[] leafHeights ) {
		double rateTotal = 0;
		int count = 0;
		for(int i = 0 ; i < leafNames.length ;i++) {
			int sample = sampleInformation_.getSampleOrdinal(leafNames[i]);
			double sampleHeight = sampleInformation_.getHeight(sample);
			if(sampleHeight>0) {
				double rate = leafHeights[i]/sampleHeight;
				rateTotal += rate;
				count++;
			}
		}
		if(count==0) {
		  this.rate_ = 0;
		} else {
		  this.rate_ = Math.min(1,rateTotal/count);
		}
//		if(rate_<=0.0001) {
//		  rate_ = 0.0001; //It's just better if the initial rate is > zero (otherwise all coalescent events will be infered to start before first sample)
//		}
//		this.rate_ = 1;
		this.upperRate_ = rate_;
		this.metaRate_ = 1;
		System.out.println("Inferered rate:"+rate_);
//		this.rate_ = 1;

	}

	public NeoParameterized getAllGroupRelatedParameterAccess() { return this; }
	public NeoParameterized getPrimaryGroupRelatedParameterAccess() { return primaryParameters_; }
	public NeoParameterized getSecondaryGroupRelatedParameterAccess() { return secondaryParameters_; }


	public MolecularClockLikelihoodModel.Leaf createNewClockLeaf(PatternInfo pattern, int[] patternStateMatchup) { return likelihoodModel_.createNewLeaf(this,pattern,patternStateMatchup); }
	public MolecularClockLikelihoodModel.External createNewClockExternal() { return likelihoodModel_.createNewExternal(this); }
	public MolecularClockLikelihoodModel.Internal createNewClockInternal() { return likelihoodModel_.createNewInternal(this); }

// =============================================================
// === HeightConverter Stuff ===================================
	public double getExpectedSubstitutionHeight(double baseHeight) {
		if(baseHeight>maxLeafHeight_) {
		  return maxLeafHeight_*rate_+(baseHeight-maxLeafHeight_)*upperRate_;
		}
		return baseHeight*rate_;
	}
	public double getExpectedSubstitutionDistance(double lowerBaseHeight, double upperBaseHeight) {
		if(upperBaseHeight<maxLeafHeight_) {
			return( upperBaseHeight-lowerBaseHeight )*rate_;
		} else if(lowerBaseHeight>maxLeafHeight_) {
			return( upperBaseHeight-lowerBaseHeight )*upperRate_;
		}
		return getExpectedSubstitutionHeight(upperBaseHeight)-getExpectedSubstitutionHeight(lowerBaseHeight);
	}
// =============================================================
// === NeoParameterized Stuff ==================================
	public int getNumberOfParameters() { return (allowMetaRate_ ? 3 : 2); }

	public void setParameters(double[] parameters, int startIndex) {
		if(allowMetaRate_) {
		  this.metaRate_ = parameters[2+startIndex];
		  this.rate_ = parameters[startIndex]*metaRate_;
		  this.upperRate_ = parameters[1+startIndex]*metaRate_;
		} else {
		  this.rate_ = parameters[startIndex];
		  this.upperRate_ = parameters[1+startIndex];
		}
	}

	public void getParameters(double[] store, int startIndex) {
	  if(allowMetaRate_) {
		  store[startIndex] = rate_/metaRate_;
		  store[1+startIndex] = upperRate_/metaRate_;
			store[2+startIndex] = metaRate_;
		} else {
		  store[startIndex] = rate_;
			store[1+startIndex] = upperRate_;
		}
	}

	public double getLowerLimit(int n) { return RATE_LOWER_LIMIT; }
	public double getUpperLimit(int n) { return RATE_UPPER_LIMIT; }
	public void getDefaultValues(double[] store, int startIndex) {
		System.arraycopy(DEFAULTS,0,store,startIndex,(allowMetaRate_ ? 3 : 2));
	}
	private final class PrimaryParameters implements NeoParameterized {
		public int getNumberOfParameters() { return 1; }
		public void setParameters(double[] parameters, int startIndex) {
		  setBaseRate(parameters[startIndex]);
		}
	  public void getParameters(double[] parameterStore, int startIndex) {
		  parameterStore[startIndex] = getBaseRate();
		}

	  public double getLowerLimit(int n) { return RATE_LOWER_LIMIT; }
	  public double getUpperLimit(int n) { return RATE_UPPER_LIMIT; }
	  public void getDefaultValues(double[] store, int startIndex) { store[startIndex] = 0.1; }
	}

	private final class SecondaryParameters implements NeoParameterized {
		public int getNumberOfParameters() { return (allowMetaRate_ ? 2 : 1); }
		public void setParameters(double[] parameters, int startIndex) {
			if(allowMetaRate_) {
				setBaseUpperRateAndMetaRate( parameters[startIndex], parameters[startIndex+1] );
			} else {
				setBaseUpperRate( parameters[startIndex] );
			}
		}
	  public void getParameters(double[] parameterStore, int startIndex) {
		  parameterStore[startIndex] = getBaseUpperRate();
			if(allowMetaRate_) {
				parameterStore[startIndex+1] = getMetaRate();
			}
		}

	  public double getLowerLimit(int n) { return RATE_LOWER_LIMIT; }
	  public double getUpperLimit(int n) { return RATE_UPPER_LIMIT; }
	  public void getDefaultValues(double[] store, int startIndex) {
			store[startIndex] = 1;
			if(allowMetaRate_) {
				store[startIndex+1] = 1;
			}
		}
	}
} //End of class SRDTGlobalClockModel


