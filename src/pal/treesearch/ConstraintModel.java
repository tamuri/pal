// ConstraintModel.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: ConstraintModel </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */

import pal.eval.*;
import pal.misc.*;

public interface ConstraintModel {
	/**
	 * Enquire about the clock constraint grouping of the leaf
	 * @param leafLabel the label of the leaf
	 * @return the grouping of the leaf, or null if outside the leaf is unconstrained (free)
	 */
	public GroupManager getGlobalClockConstraintGrouping(String[] leafLabelSet);

	/**
	 * Obtain the permanent clade sets. That is, when randomly building the tree, and when tree searching, what labels must always
	 * form a clade.
	 * @param allLabelSet The set of all leaf labels in the tree
	 * @return An array of string arrays dividing up the label set
	 */
	public String[][] getCladeConstraints(String[] allLabelSet);

  public UnconstrainedLikelihoodModel.Leaf createNewFreeLeaf(int[] patternStateMatchup, int numberOfPatterns);
	public UnconstrainedLikelihoodModel.External createNewFreeExternal();
  public UnconstrainedLikelihoodModel.Internal createNewFreeInternal();

	public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(  boolean isForLeaf );

	public NeoParameterized getGlobalParameterAccess();

	public String getRateModelSummary();

// ===================================================================================================

	public static interface GroupManager {
		public double getLeafBaseHeight(String leafLabel);
		public double getBaseHeight(double originalExpectSubstitutionHeight);
		public double getExpectedSubstitutionHeight(double baseHeight);

		public int getBaseHeightUnits();

		public void initialiseParameters(String[] leafNames, double[] leafHeights );

		public NeoParameterized getAllGroupRelatedParameterAccess();
		public NeoParameterized getPrimaryGroupRelatedParameterAccess();
		public NeoParameterized getSecondaryGroupRelatedParameterAccess();

		public MolecularClockLikelihoodModel.Leaf createNewClockLeaf(PatternInfo pattern, int[] patternStateMatchup);
		public MolecularClockLikelihoodModel.External createNewClockExternal();
		public MolecularClockLikelihoodModel.Internal createNewClockInternal();
	}
}