// MRDTGlobalClockModel.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: MRDTGlobalClockModel </p>
 * <p>Description: A constraint model for the serial sample analysis case where a molecular clock is assumed and there may be more than one rate interval</p>
 * author Matthew Goode
 * @version 1.0
 */
import pal.eval.*;
import pal.misc.*;

public class MRDTGlobalClockModel implements ConstraintModel, ConstraintModel.GroupManager, NeoParameterized, MolecularClockLikelihoodModel.HeightConverter {
	private final SampleInformation sampleInformation_;
	private final MolecularClockLikelihoodModel.Instance likelihoodModel_;
	private final double[] rateChangeTimes_;
	private final double[] rateChangeIntervalSizes_;
	private final double[] heightTotals_;

	private final double[] rates_;

	private final static double RATE_LOWER_LIMIT = 0;
	private final static double RATE_UPPER_LIMIT = 1;

	private final double[] defaults_ ;

	private final double maxLeafHeight_;
	private final int numberOfBaseRates_;

	public MRDTGlobalClockModel(SampleInformation sampleInformation, MolecularClockLikelihoodModel.Instance likelihoodModel) {
		this(sampleInformation,likelihoodModel,createSimpleTimes(sampleInformation));
	}
	public MRDTGlobalClockModel(SampleInformation sampleInformation, MolecularClockLikelihoodModel.Instance likelihoodModel, double[] rateChangeTimes) {
		this.sampleInformation_ = sampleInformation;
		this.rateChangeTimes_ = pal.misc.Utils.getCopy(rateChangeTimes);
		this.numberOfBaseRates_ = rateChangeTimes.length;
		this.rateChangeIntervalSizes_ = new double[numberOfBaseRates_];
		this.heightTotals_ = new double[numberOfBaseRates_];
		this.rates_ = new double[numberOfBaseRates_+1];
		this.defaults_ = new double[numberOfBaseRates_+1];
		double lastTime = 0;
		for(int i = 0 ; i < numberOfBaseRates_ ; i++) {
			rateChangeIntervalSizes_[i] = rateChangeTimes[i]-lastTime;
			lastTime = rateChangeTimes[i];
		}
		for(int i = 0 ; i < defaults_.length ; i++) {
			defaults_[i] = 0.1;
		}
		this.maxLeafHeight_ = sampleInformation.getMaxHeight();
		this.likelihoodModel_ = likelihoodModel;
	}
	private final static double[] createSimpleTimes(SampleInformation si) {
		int numberOfBaseRates = si.getNumberOfSamples()-1;
		double[] rateChangeTimes = new double[numberOfBaseRates];
		for(int i = 0 ; i < numberOfBaseRates ; i++) {
			rateChangeTimes[i] = si.getHeight(i+1);
		}
		return rateChangeTimes;
	}
	public String getRateModelSummary() {
		StringBuffer sb = new StringBuffer();
		sb.append("Multiple Rates Dated Tips");
		sb.append("<UL>");
		for(int i = 0 ; i < rateChangeTimes_.length ; i++) {
			sb.append("<LI>");
			sb.append( i==0? 0.0 : rateChangeTimes_[i-1] );
			sb.append(" - ");
			sb.append(rateChangeTimes_[i]);
			sb.append(" : ");
			sb.append(rates_[i]);
			sb.append("</LI>");
		}
		sb.append("<LI>");
			sb.append(rateChangeTimes_[rateChangeTimes_.length-1] );
			sb.append(" - INFINITY");
			sb.append(" : ");
			sb.append(rates_[rateChangeTimes_.length]);
			sb.append(" (not meaningful if rate change times match sampling times!) ");
			sb.append("</LI>");
		sb.append("</UL>");
		return sb.toString();

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

	public NeoParameterized getGlobalParameterAccess() { return likelihoodModel_.getParameterAccess(); }

// =============================================================
// === GroupManagerStuff Stuff ===================================
	public double getLeafBaseHeight(String leafLabel) {
		return sampleInformation_.getHeight(sampleInformation_.getSampleOrdinal(leafLabel));
	}
	public double getBaseHeight(double originalExpectSubstitutionHeight) {
		double total = 0;
		for(int i = 0 ; i < rateChangeIntervalSizes_.length ;i++) {
			double next = total+rates_[i]*rateChangeIntervalSizes_[i];
			if(originalExpectSubstitutionHeight<next) {
				return (originalExpectSubstitutionHeight-total)/rates_[i]+rateChangeTimes_[i];
			}
			total=next;
		}
		return (originalExpectSubstitutionHeight-total)/rates_[numberOfBaseRates_]+maxLeafHeight_;
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
		double intervalRate;
		if(count==0) {
			intervalRate = 0;
		} else {
			intervalRate = Math.min(1,rateTotal/count);
		}
		if(intervalRate<=0.00000000001) {
			intervalRate = 0.0001; //It's just better if the initial rate is > zero (otherwise all coalescent events will be infered to start before first sample)
		}
//		this.rate_ = 1;

//		System.out.println("Inferered rate:"+intervalRate);

		for(int i = 0 ; i < rates_.length ; i++) {
			rates_[i] = intervalRate;
		}
		updateHeightTotals();
	}

	public NeoParameterized getAllGroupRelatedParameterAccess() { return this; }
	public NeoParameterized getPrimaryGroupRelatedParameterAccess() { return this; }
	public NeoParameterized getSecondaryGroupRelatedParameterAccess() { return null; }


	public MolecularClockLikelihoodModel.Leaf createNewClockLeaf(PatternInfo pattern, int[] patternStateMatchup) { return likelihoodModel_.createNewLeaf(this,pattern,patternStateMatchup); }
	public MolecularClockLikelihoodModel.External createNewClockExternal() { return likelihoodModel_.createNewExternal(this); }
	public MolecularClockLikelihoodModel.Internal createNewClockInternal() { return likelihoodModel_.createNewInternal(this); }

// =============================================================
// === HeightConverter Stuff ===================================
	public double getExpectedSubstitutionHeight(double baseHeight) {
		int rate = rateChangeTimes_.length;
		for(int i = 0 ; i < rateChangeTimes_.length ; i++) {
			if( baseHeight<rateChangeTimes_[i] ) { rate = i; break;	}
		}
		if(rate==0) {
			return baseHeight*rates_[0];
		} else {
			return (baseHeight-rateChangeTimes_[rate-1])*rates_[rate]+ heightTotals_[rate-1];
		}
	}
	public double getExpectedSubstitutionDistance(double lowerBaseHeight, double upperBaseHeight) {
		return getExpectedSubstitutionHeight(upperBaseHeight)-getExpectedSubstitutionHeight(lowerBaseHeight);
	}
// =============================================================
// === NeoParameterized Stuff ==================================
	public int getNumberOfParameters() { return rates_.length; }

	public void setParameters(double[] parameters,int startIndex) {
		System.arraycopy(parameters,startIndex,rates_,0,rates_.length);
		updateHeightTotals();
	}
	private final void updateHeightTotals() {
		double total = 0;
		for(int i = 0 ; i< numberOfBaseRates_ ; i++) {
			total+=rateChangeIntervalSizes_[i]*rates_[i];
			heightTotals_[i] = total;
		}

	}

	public void getParameters(double[] store, int startIndex) {
		System.arraycopy(rates_,0,store,startIndex,rates_.length);
	}

	public double getLowerLimit(int n) { return RATE_LOWER_LIMIT; }
	public double getUpperLimit(int n) { return RATE_UPPER_LIMIT; }
	public void getDefaultValues(double[] store, int startIndex) {
		System.arraycopy(defaults_,0,store,startIndex,defaults_.length);
	}

} //End of class MRDTGlobalClockModel


