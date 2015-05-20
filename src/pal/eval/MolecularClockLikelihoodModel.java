// MolecularClockLikelihoodModel.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: MolecularClockLikelihoodModel </p>
 * <p>Description: An interface to objects that can be used for calculating likelihood estimates when a molecular clock is assumed (and therefore knowledge of the relative temporal order of events) </p>
 *
 * @author Matthew Goode
 * @version 1.0
 * <p>History<br>
 * <ul>
 * <li>27/5/2004 Created </li>
 * </ul>
 * </p>
 */

import pal.misc.NeoParameterized;

public interface MolecularClockLikelihoodModel {
    /**
     * The External calculator does not maintain any state and is approapriate for
     * calculation where a store is provided
     */
    interface External {

        void calculateSingleDescendentExtendedConditionals(
                double topBaseHeight, double bottomBaseHeight,
                PatternInfo centerPattern,
                ConditionalProbabilityStore descendentConditionalProbabilities
        );

        /**
         */
        void calculateSingleAscendentExtendedConditionalsDirect(
                double topBaseHeight, double bottomBaseHeight,
                PatternInfo centerPattern,
                ConditionalProbabilityStore ascendentConditionalProbabilityProbabilties
        );

        /**
         */
        void calculateSingleAscendentExtendedConditionalsIndirect(
                double topBaseHeight, double bottomBaseHeight,
                PatternInfo centerPattern,
                ConditionalProbabilityStore baseAscendentConditionalProbabilityProbabilties,
                ConditionalProbabilityStore resultConditionalProbabilityProbabilties
        );

        /**
         *
         * @param distance
         * @param model
         * @param patternLookup
         * @param numberOfPatterns
         * @param leftConditionalProbabilityProbabilties Implementations must not overwrite or change
         * @param rightConditionalProbabilityProbabilties Implementations must not overwrite or change
         * @param resultStore Where to stick the created categoryPatternState information
         * @return true if built on cached information
         * @note calls to getLastConditionalProbabilities() does not have to be valid after call this method
         */
        void calculateExtendedConditionals(double topBaseHeight, double bottomBaseHeight,
                                                  PatternInfo centerPattern,
                                                  ConditionalProbabilityStore
                                                          leftConditionalProbabilities,
                                                  ConditionalProbabilityStore
                                                          rightConditionalProbabilities,
                                                  ConditionalProbabilityStore resultStore);

        /**
         * Calculate the likelihood given two sub trees (left, right) and their extended likeihood probabilities
         * @param rootHeight the height of the likelihood calculation
         * @param leftConditionalProbabilities Assumed to be extended to the rootHeight
         * @param rightConditionalProbabilities Assumed to be extended to the rootHeight
         * @return the Log likelihood
         */
        double calculateLogLikelihood(double rootHeight, PatternInfo centerPattern,
                                             ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                             ConditionalProbabilityStore rightConditionalProbabilitiesStore);

        /**
         * Calculate the likelihood given a non root node
         * @param nodeHeight the height of node doing the likelihood calculation
         * @param centerPatter assumed left is ascendent component, right is descendent
         * @param ascendentConditionalProbabilities Assumed to be extended (downwards) to the nodeHeight
         * @param descendentConditionalProbabilities Assumed to be extended (upwards) to the nodeHeight
         * @return the Log likelihood
         */
        double calculateLogLikelihoodNonRoot(double nodeHeight, PatternInfo centerPattern,
                                                    ConditionalProbabilityStore ascendentConditionalProbabilitiesStore,
                                                    ConditionalProbabilityStore descendentConditionalProbabilitiesStore);

        double calculateLogLikelihoodSingle(double rootHeight, PatternInfo centerPattern,
                                                   ConditionalProbabilityStore conditionalProbabilitiesStore);

        SiteDetails calculateSiteDetails(double rootHeight, PatternInfo centerPattern,
                                                ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                                ConditionalProbabilityStore rightConditionalProbabilitiesStore);

        void calculateFlatConditionals(double rootHeight, PatternInfo centerPattern,
                                              ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                              ConditionalProbabilityStore rightConditionalProbabilitiesStore,
                                              ConditionalProbabilityStore resultConditionalProbabilitiesStore);
    } //End of class External

    /**
     * The Internal calculator may maintain state and is approapriate permanent attachment
     * to internal nodes of the tree structure
     */
    interface Internal {

        /**
         *
         * @param patternLookup
         * @param numberOfPatterns
         * @param leftConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
         * @param rightConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
         * @return true if result build on cached information
         * @note An assumption may be made that after a call to this method the leftConditionals and rightConditionals are not used again!
         */
        ConditionalProbabilityStore calculateExtendedConditionals(double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
                                                                         ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
                                                                         ConditionalProbabilityStore rightConditionalProbabilityProbabilties);

        /**
         * Extends left and right conditionals by type and then calculates flat conditionals
         * @param patternLookup
         * @param numberOfPatterns
         * @param leftConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
         * @param rightConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
         * @return true if result build on cached information
         * @note An assumption may be made that after a call to this method the leftConditionals and rightConditionals are not used again!
         */
        ConditionalProbabilityStore calculatePostExtendedFlatConditionals(double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
                                                                                 ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
                                                                                 ConditionalProbabilityStore rightConditionalProbabilityProbabilties);


        /**
         */
        ConditionalProbabilityStore calculateAscendentExtendedConditionals(double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
                                                                                  ConditionalProbabilityStore ascendentConditionalProbabilityProbabilties,
                                                                                  ConditionalProbabilityStore otherConditionalProbabilityProbabilties);

        /**
         */
        ConditionalProbabilityStore calculateAscendentFlatConditionals(PatternInfo centerPattern,
                                                                              ConditionalProbabilityStore ascenedentConditionalProbabilityProbabilties,
                                                                              ConditionalProbabilityStore otherConditionalProbabilityProbabilties);

        /**
         *
         * @param centerPattern the center pattern info
         * @param leftConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
         * @param rightConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
         * @return true if result build on cached information
         * @note An assumption may be made that after a call to this method the leftConditionals and rightConditionals are not used again!
         */
        ConditionalProbabilityStore calculateFlatConditionals(PatternInfo centerPattern,
                                                                     ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
                                                                     ConditionalProbabilityStore rightConditionalProbabilityProbabilties);
    } //End of Internal

    /**
     * A ConstrainedLHCalculator.Leaf object is attached to each leaf node and can be used to calculated conditional probabilities across the related branch.
     * Allows for quick implementations as well as implementations that cope correctly with ambiguous characters
     */
    interface Leaf {
        ConditionalProbabilityStore calculateExtendedConditionals(double topHeight, double bottomHeight);

        ConditionalProbabilityStore calculateFlatConditionals(double height);

    }

    interface Simulator {
        int[] getSimulated(int[] baseSequence, double topBaseHeight, double bottomBaseHeight);

        void simulated(int[] baseSequence, double topBaseHeight, double bottomBaseHeight, int[] newSequence);

        int[] generateRoot(double sampleHeight);

        void resetDistributions();
    }

    interface HeightConverter {
        double getExpectedSubstitutionHeight(double baseHeight);

        double getExpectedSubstitutionDistance(double lower, double upper);
    }

    interface Instance extends java.io.Serializable {

        String getSubstitutionModelSummary();

        Leaf createNewLeaf(HeightConverter converter, PatternInfo pattern, int[] patternStateMatchup);

        External createNewExternal(HeightConverter converter);

        //		public Simulator createNewSimulator(int sequenceLength, boolean stochasticDistribution);
        Internal createNewInternal(HeightConverter converter);

        ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(boolean isForLeaf);

        NeoParameterized getParameterAccess();
    }

}