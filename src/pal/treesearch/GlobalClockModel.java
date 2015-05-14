// GlobalClockModel.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GlobalClockModel </p>
 * <p>Description: A constraint model that assumes a molecular clock across the tree</p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.eval.*;
import pal.misc.*;

public class GlobalClockModel implements ConstraintModel, ConstraintModel.GroupManager, MolecularClockLikelihoodModel.HeightConverter, NeoParameterized{
	private final MolecularClockLikelihoodModel.Instance likelihoodModel_;
	private double rate_;
	private static final double[] DEFAULTS = {1};
	private final boolean includeScaling_ = true;

	public GlobalClockModel(MolecularClockLikelihoodModel.Instance likelihoodModel) {
		this.likelihoodModel_ = likelihoodModel;
	}
	public String getRateModelSummary() {
		return "Molecular clock assumed (Single Rate model)";
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
	public double getLeafBaseHeight(String leafLabel) {	return 0;	}
	public double getBaseHeight(double originalExpectSubstitutionHeight) {		return originalExpectSubstitutionHeight/rate_;	}
	public int getBaseHeightUnits() { return Units.EXPECTED_SUBSTITUTIONS; }

	public void initialiseParameters(String[] leafNames, double[] leafHeights ) {
		this.rate_ = 1;
	}
	public NeoParameterized getAllGroupRelatedParameterAccess() { return this; }
	public NeoParameterized getPrimaryGroupRelatedParameterAccess() { return this; }
	public NeoParameterized getSecondaryGroupRelatedParameterAccess() { return null; }

	public MolecularClockLikelihoodModel.Leaf createNewClockLeaf(PatternInfo pattern, int[] patternStateMatchup) { return likelihoodModel_.createNewLeaf(this,pattern,patternStateMatchup); }
	public MolecularClockLikelihoodModel.External createNewClockExternal() { return likelihoodModel_.createNewExternal(this); }
	public MolecularClockLikelihoodModel.Internal createNewClockInternal() { return likelihoodModel_.createNewInternal(this); }

// =============================================================
// === HeightConverter Stuff ===================================
	public double getExpectedSubstitutionHeight(double baseHeight) { return rate_*baseHeight; }
	public double getExpectedSubstitutionDistance(double lowerBaseHeight, double upperBaseHeight) { return (upperBaseHeight-lowerBaseHeight)*rate_; }

	public int getNumberOfParameters() { return (includeScaling_ ? 1 : 0); }

	public void setParameters(double[] parameters, int startIndex) {
		if( includeScaling_ ) {
			this.rate_ = parameters[0+startIndex];
		}
	}

	public void getParameters(double[] store,int startIndex) { if(includeScaling_) { store[startIndex] = rate_; }	}

	public double getLowerLimit(int n) { return 0; }

	public double getUpperLimit(int n) { return 100; }

	public void getDefaultValues(double[] store, int startIndex) {
		System.arraycopy(DEFAULTS,0,store,startIndex,DEFAULTS.length); }
} //End of class GlobalClockModel
