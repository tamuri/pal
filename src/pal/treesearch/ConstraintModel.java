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
 *
 * @author Matthew Goode
 * @version 1.0
 */

import pal.eval.ConditionalProbabilityStore;
import pal.eval.MolecularClockLikelihoodModel;
import pal.eval.PatternInfo;
import pal.eval.UnconstrainedLikelihoodModel;
import pal.misc.NeoParameterized;

public interface ConstraintModel {
    /**
     * Enquire about the clock constraint grouping of the leaf
     * @param leafLabel the label of the leaf
     * @return the grouping of the leaf, or null if outside the leaf is unconstrained (free)
     */
    GroupManager getGlobalClockConstraintGrouping(String[] leafLabelSet);

    /**
     * Obtain the permanent clade sets. That is, when randomly building the tree, and when tree searching, what labels must always
     * form a clade.
     * @param allLabelSet The set of all leaf labels in the tree
     * @return An array of string arrays dividing up the label set
     */
    String[][] getCladeConstraints(String[] allLabelSet);

    UnconstrainedLikelihoodModel.Leaf createNewFreeLeaf(int[] patternStateMatchup, int numberOfPatterns);

    UnconstrainedLikelihoodModel.External createNewFreeExternal();

    UnconstrainedLikelihoodModel.Internal createNewFreeInternal();

    ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(boolean isForLeaf);

    NeoParameterized getGlobalParameterAccess();

    String getRateModelSummary();

// ===================================================================================================

    interface GroupManager {
        double getLeafBaseHeight(String leafLabel);

        double getBaseHeight(double originalExpectSubstitutionHeight);

        double getExpectedSubstitutionHeight(double baseHeight);

        int getBaseHeightUnits();

        void initialiseParameters(String[] leafNames, double[] leafHeights);

        NeoParameterized getAllGroupRelatedParameterAccess();

        NeoParameterized getPrimaryGroupRelatedParameterAccess();

        NeoParameterized getSecondaryGroupRelatedParameterAccess();

        MolecularClockLikelihoodModel.Leaf createNewClockLeaf(PatternInfo pattern, int[] patternStateMatchup);

        MolecularClockLikelihoodModel.External createNewClockExternal();

        MolecularClockLikelihoodModel.Internal createNewClockInternal();
    }
}