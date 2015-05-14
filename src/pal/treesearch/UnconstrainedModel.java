// UnconstrainedModel.java
//
// (c) 1999-2003 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: UnconstrainedModel </p>
 * <p>Description: A constraint model for where a global molecular clock is not assumed </p>
 * author Matthew Goode
 * @version 1.0
 */

import pal.eval.*;
import pal.misc.*;


public class UnconstrainedModel implements ConstraintModel {
	private final UnconstrainedLikelihoodModel.Instance likelihoodModel_;

	public UnconstrainedModel(UnconstrainedLikelihoodModel.Instance likelihoodModel) {
		this.likelihoodModel_ = likelihoodModel;
	}
	public String[][] getCladeConstraints(String[] allLabelSet) { return new String[][] { allLabelSet }; }
	public GroupManager getGlobalClockConstraintGrouping(String[] leafLabelSet) { return null; }

	public UnconstrainedLikelihoodModel.Leaf createNewFreeLeaf(int[] patternStateMatchup, int numberOfPatterns) { return likelihoodModel_.createNewLeaf(patternStateMatchup,numberOfPatterns); }
	public UnconstrainedLikelihoodModel.External createNewFreeExternal() { return likelihoodModel_.createNewExternal(); }
  public UnconstrainedLikelihoodModel.Internal createNewFreeInternal() { return likelihoodModel_.createNewInternal(); }

	public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(  boolean isForLeaf ) { return likelihoodModel_.createAppropriateConditionalProbabilityStore(isForLeaf); }

	public NeoParameterized getGlobalParameterAccess() { return likelihoodModel_.getParameterAccess(); }
	public String getRateModelSummary() {
		return "No clock";
	}
} //End of class UnconstrainedModel