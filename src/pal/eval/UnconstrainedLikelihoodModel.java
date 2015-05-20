// UnconstrainedLikelihoodModel.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: UnconstrainedLikelihoodModel </p>
 * <p>Description: An UnconstrainedLikelihoodModel object must be treated as a stateful, single threaded object that can be used
 * for calculating components in an overall likelihood calculation. </p>
 * <p>UnconstrainedLikelihoodModel is a generalisation of the LHCalculator code, that no longer refers to SubstitutionModel directly (this is still need for other code though so LHCalculator remains)</p>
 * <p>History<br>
 * <ul>
 * <li>25/10/2003 Added leaf handling interface </li>
 * <li>30/3/2004 Changed certain methods to more intelligent ones (relating to posterior distribution of sites). Added abstract External class.
 * </ul>
 * </p>
 *
 * @author Matthew Goode
 * @version 1.0
 * @note needs to have the use of the word likelihood altered in certain cases (to conditional probability)
 */

import pal.misc.NeoParameterized;

public interface UnconstrainedLikelihoodModel {
    /**
     * The External calculator does not maintain any state and is approapriate for
     * calculation where a store is provided
     */
    interface External extends java.io.Serializable {
        /**
         *
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilities Implementations must not overwrite or change
         * @param rightConditionalProbabilities Implementations must not overwrite or change
         * @param resultStore Where to stick the created categoryPatternState information
         * @note calls to getLastConditionalProbabilities() does not have to be valid after call this method
         */
        void calculateFlat(PatternInfo centerPattern, ConditionalProbabilityStore leftConditionalProbabilities, ConditionalProbabilityStore rightConditionalProbabilities, ConditionalProbabilityStore resultStore);

        /**
         *
         * @param distance the evolutionary distance
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilities Implementations must not overwrite or change
         * @param rightConditionalProbabilities Implementations must not overwrite or change
         * @param resultStore Where to stick the created categoryPatternState information
         * @note calls to getLastConditionalProbabilities() does not have to be valid after call this method
         */
        void calculateExtended(double distance,
                                      PatternInfo centerPattern,
                                      ConditionalProbabilityStore
                                              leftConditionalProbabilities,
                                      ConditionalProbabilityStore
                                              rightConditionalProbabilities,
                                      ConditionalProbabilityStore resultStore);

        /**
         * Extend the conditionals back in time by some distance
         * @param distance The evolutionary distance to extend by
         * @param numberOfPatterns the number of patterns
         * @param conditionalProbabilities The probabilities to extend
         */
        void calculateSingleExtendedDirect(
                double distance,
                int numberOfPatterns,
                ConditionalProbabilityStore conditionalProbabilities
        );

        /**
         * Extend the conditionals back in time by some distance
         * @param distance The evolutionary distance to extend by
         * @param numberOfPatterns the number of patterns
         * @param baseConditionalProbabilities The probabilities to extend
         * @param resultConditionalProbabilities The probabilities to extend
         */
        void calculateSingleExtendedIndirect(
                double distance,
                int numberOfPatterns,
                ConditionalProbabilityStore baseConditionalProbabilities,
                ConditionalProbabilityStore resultConditionalProbabilities
        );

        /**
         * Calculate the likelihood given two sub trees (left, right) and their flat (unextend) likeihood probabilities
         * @param distance The evolutionary distance
         * @param centerPattern the pattern information
         * @param leftFlatConditionalProbabilities The left conditional probabilities (unextended)
         * @param rightFlatConditionalProbabilities The right conditional probabilities (unextended)
         * @param tempStore may be used internally to calculate likelihood
         * @return the log likelihood
         */
        double calculateLogLikelihood(double distance,
                                             PatternInfo centerPattern,
                                             ConditionalProbabilityStore leftFlatConditionalProbabilities,
                                             ConditionalProbabilityStore rightFlatConditionalProbabilities,
                                             ConditionalProbabilityStore tempStore
        );

        /**
         * Calculate the likelihood given two sub trees (left, right) and their extended likeihood probabilities
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilities The left conditional probabilities
         * @param rightConditionalProbabilities The right conditional probabilities
         * @return the Log likelihood
         */
        double calculateLogLikelihood(PatternInfo centerPattern,
                                             ConditionalProbabilityStore leftConditionalProbabilities,
                                             ConditionalProbabilityStore rightConditionalProbabilities);

        /**
         * Calculate the likelihood given the conditional probabilites at the root
         * @param patternWeights the weights of each pattern
         * @param numberOfPatterns the number of patterns
         * @return the Log likelihood
         */
        double calculateLogLikelihoodSingle(int[] patternWeights, int numberOfPatterns,
                                                   ConditionalProbabilityStore conditionalProbabilityStore);


        /**
         * Calculate the conditional probabilities of each pattern for each category
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilitiesStore The left conditional probabilities
         * @param rightConditionalProbabilitiesStore The right conditional probabilities
         */
        SiteDetails calculateSiteDetailsRooted(
                PatternInfo centerPattern,
                ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                ConditionalProbabilityStore rightConditionalProbabilitiesStore
        );

        /**
         * Calculate the conditional probabilities of each pattern for each category
         * @param distance The distance between the two nodes
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilitiesStore The left conditional probabilities
         * @param rightConditionalProbabilitiesStore The right conditional probabilities
         * @param tempStore after call will hold a matrix of values in the form [cat][pattern], where [cat][pattern] represents the site probability under a particular category/class, *not* multiplied by the category probability or pattern weights
         */
        SiteDetails calculateSiteDetailsUnrooted(double distance,
                                                        PatternInfo centerPattern,
                                                        ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                                        ConditionalProbabilityStore rightConditionalProbabilitiesStore,
                                                        ConditionalProbabilityStore tempStore
        );
    } //End of class External
// =================================================================================================
// ================= Internal ======================================================================
// =================================================================================================

    /**
     * The Internal calculator may maintain state and is approapriate permanent attachment
     * to internal nodes of the tree structure
     */
    interface Internal {
        /**
         * calculate flat probability information (not extended over a branch).
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilities Implementations should be allowed to overwrite in certain cases
         * @param rightConditionalProbabilities Implementations should be allowed to overwrite in certain cases
         * @return true if results built from cached information
         * @note An assumption may be made that after a call to this method the leftConditionals and rightConditionals are not used again!
         */
        ConditionalProbabilityStore calculateFlat(PatternInfo centerPattern, ConditionalProbabilityStore leftConditionalProbabilities, ConditionalProbabilityStore rightConditionalProbabilities);

        /**
         *
         * @param distance The evolutionary distance
         * @param centerPattern the pattern information
         * @param leftConditionalProbabilities Implementations should be allowed to overwrite in certain cases
         * @param rightConditionalProbabilities Implementations should be allowed to overwrite in certain cases
         * @return resulting conditional probabilities
         * @note An assumption may be made that after a call to this method the leftConditionals and rightConditionals are not used again!
         */
        ConditionalProbabilityStore calculateExtended(double distance, PatternInfo centerPattern, final ConditionalProbabilityStore leftConditionalProbabilities,
                                                             final ConditionalProbabilityStore rightConditionalProbabilities);

    } //End of Internal

// =================================================================================================
// ================= Leaf ==========================================================================
// =================================================================================================

    /**
     * A LHCalculator.Leaf object is attached to each leaf node and can be used to calculated conditional probabilities across the related branch.
     * Allows for quick implementations as well as implementations that cope correctly with ambiguous characters
     * @note Should not be made serializable!
     */
    interface Leaf {
        ConditionalProbabilityStore getFlatConditionalProbabilities();

        ConditionalProbabilityStore getExtendedConditionalProbabilities(double distance);

        /**
         * Create a new Leaf calculator that has exactly the same properties as this one (but is different such that it may be used independently)
         * @return a copy of this leaf calculator
         */
        Leaf getCopy();
    }


    interface Instance extends java.io.Serializable {
        /**
         * Create anew leaf calculator
         * @param patternStateMatchup The sequence as reduced to patterns. This should just be one state per pattern.
         * For example given a sequence [ 0, 1,0,1,3,0] a patternMatchup may be [0,1,3] (the first element is the first
         * pattern, which is state 0, the second element is the second pattern which is 1, and the third element is the
         * third pattern (novel pattern) which is state 3)
         * @param numberOfPatterns The number of patterns in the patternStateMatchup array
         * @return a leaf calculator object
         */
        Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns);

        External createNewExternal();

        Internal createNewInternal();

        /**
         * If true, then user can assume that areas of trees that haven't changed, and the model parameters haven't be altered,
         * can have their conditionals cached.
         * @return
         */
        boolean isAllowCaching();


        ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(boolean isForLeaf);

        String getSubstitutionModelSummary();

        NeoParameterized getParameterAccess();


    }
}