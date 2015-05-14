// SimpleUnconstrainedLikelihoodModel.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: SimpleUnconstrainedLikelihoodModel </p>
 * <p>Description: A wrapper around LHCalculator stuff</p>
 * @author Matthew Goode
 * @version 1.0
 */

import pal.misc.*;
import pal.substmodel.*;

public class SimpleUnconstrainedLikelihoodModel implements UnconstrainedLikelihoodModel {

  private static final class ExternalImpl implements External {
    private final LHCalculator.External base_;
		private final SubstitutionModel model_;
		public ExternalImpl(LHCalculator.External base, SubstitutionModel model) {
			this.base_ = base;
			this.model_ = model;
		}
		public void calculateFlat( PatternInfo centerPattern, ConditionalProbabilityStore leftConditionalProbabilities, ConditionalProbabilityStore rightConditionalProbabilities, ConditionalProbabilityStore resultStore ) {
		  base_.calculateFlat(centerPattern, leftConditionalProbabilities, rightConditionalProbabilities, resultStore);
		}

    public void calculateExtended( double distance,
                                   PatternInfo centerPattern,
                                   ConditionalProbabilityStore
                                   leftConditionalProbabilities,
                                   ConditionalProbabilityStore
                                   rightConditionalProbabilities,
                                   ConditionalProbabilityStore resultStore ) {
			base_.calculateExtended(distance,model_,centerPattern,leftConditionalProbabilities,rightConditionalProbabilities,resultStore);
		}


    public void calculateSingleExtendedDirect(
																		double distance,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore conditionalProbabilities
                                  ) {
			base_.calculateSingleExtendedDirect(distance,model_,numberOfPatterns,conditionalProbabilities);
		}

    public void calculateSingleExtendedIndirect(
																		double distance,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore baseConditionalProbabilities,
                                    ConditionalProbabilityStore resultConditionalProbabilities
                                  ) {
			base_.calculateSingleExtendedIndirect(distance,model_,numberOfPatterns,baseConditionalProbabilities,resultConditionalProbabilities);
		}

    public double calculateLogLikelihood( double distance,
                                       PatternInfo centerPattern,
                                       ConditionalProbabilityStore leftFlatConditionalProbabilities,
                                       ConditionalProbabilityStore rightFlatConditionalProbabilities,
                                       ConditionalProbabilityStore tempStore
                                       ) {
		  return base_.calculateLogLikelihood(distance,model_,centerPattern,leftFlatConditionalProbabilities,rightFlatConditionalProbabilities,tempStore);
		}

    public double calculateLogLikelihood( PatternInfo centerPattern,
                                       ConditionalProbabilityStore leftConditionalProbabilities,
                                       ConditionalProbabilityStore rightConditionalProbabilities ) {
			return base_.calculateLogLikelihood(model_,centerPattern,leftConditionalProbabilities,rightConditionalProbabilities);
		}

    public double calculateLogLikelihoodSingle( int[] patternWeights, int numberOfPatterns,
                                       ConditionalProbabilityStore conditionalProbabilityStore) {
			return base_.calculateLogLikelihoodSingle(model_,patternWeights,numberOfPatterns,conditionalProbabilityStore);
		}

    public SiteDetails calculateSiteDetailsRooted(
      PatternInfo centerPattern,
      ConditionalProbabilityStore leftConditionalProbabilitiesStore,
      ConditionalProbabilityStore rightConditionalProbabilitiesStore
      ) {
			return base_.calculateSiteDetailsRooted(model_,centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore);
		}

		public SiteDetails calculateSiteDetailsUnrooted( double distance,
	    PatternInfo centerPattern,
      ConditionalProbabilityStore leftConditionalProbabilitiesStore,
      ConditionalProbabilityStore rightConditionalProbabilitiesStore,
			ConditionalProbabilityStore tempStore
    ) {
			return base_.calculateSiteDetailsUnrooted(distance,model_,centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore,tempStore);
		}
  } //End of class ExternalImpl
// =================================================================================================
// ================= Internal ======================================================================
// =================================================================================================

  public static final class InternalImpl implements Internal {
		private final LHCalculator.Internal base_;
		private final SubstitutionModel model_;
		public InternalImpl(LHCalculator.Internal base, SubstitutionModel model) {
			this.base_ = base;
			this.model_ = model;
		}

    public ConditionalProbabilityStore calculateFlat( PatternInfo centerPattern, ConditionalProbabilityStore leftConditionalProbabilities, ConditionalProbabilityStore rightConditionalProbabilities ) {
			return base_.calculateFlat(centerPattern,leftConditionalProbabilities,rightConditionalProbabilities);
		}

    public ConditionalProbabilityStore calculateExtended( double distance,  PatternInfo centerPattern, final ConditionalProbabilityStore leftConditionalProbabilities,
      final ConditionalProbabilityStore rightConditionalProbabilities) {
			return base_.calculateExtended(distance,model_,centerPattern,leftConditionalProbabilities,rightConditionalProbabilities,true);
		}
  } //End of class InternalImpl

// =================================================================================================
// ================= Leaf ==========================================================================
// =================================================================================================
	public static final class LeafImpl implements Leaf {
		private final LHCalculator.Leaf base_;
		private final SubstitutionModel model_;
		public LeafImpl(LHCalculator.Leaf base, SubstitutionModel model) {
			this.base_ = base;
			this.model_ = model;
		}
		public ConditionalProbabilityStore getFlatConditionalProbabilities() {
			return  base_.getFlatConditionalProbabilities();
		}
		public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance) {
			return base_.getExtendedConditionalProbabilities(distance,model_,true);
		}
		public Leaf getCopy() {
			return new LeafImpl(base_.getCopy(), model_);
		}
	}


  private static final class InstanceImpl implements Instance   {
	  private final LHCalculator.Generator base_;
		private final SubstitutionModel model_;
		private final NeoParameterized parameterAccess_;
		public InstanceImpl(LHCalculator.Generator base, SubstitutionModel model) {
			this.base_ = base;
			this.model_ = model;
			this.parameterAccess_ = new ParameterizedNeoWrapper(model);
		}
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns) {
			return new LeafImpl(base_.createNewLeaf(patternStateMatchup,numberOfPatterns),model_);
		}


		public External createNewExternal() {
			return new ExternalImpl(base_.createNewExternal(),model_);
		}

    public Internal createNewInternal() {
			return new InternalImpl(base_.createNewInternal(),model_);
		}

		public boolean isAllowCaching() { return base_.isAllowCaching(); }

    public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore( boolean isForLeaf ) {
			return base_.createAppropriateConditionalProbabilityStore(isForLeaf);
		}
		public String getSubstitutionModelSummary() { return model_.toString(); }
		public NeoParameterized getParameterAccess() { return parameterAccess_; }
  }
	/**
	 * Create a SimpleUnconstrainedLikelihoodModel instance
	 *
	 * @param base The base LHCalculator generator to utilise
	 * @param model The substitution model
	 * @return An appropriate UnconstrianedLikelihoodModel instance
	 */
	public static final Instance createInstance(LHCalculator.Generator base, SubstitutionModel model) {
	  return new InstanceImpl(base,model);
	}
	/**
	 * Create a SimpleUnconstrainedLikelihoodModel instance
	 *
	 * @param base The base LHCalculator generator to utilise
	 * @param model The substitution model
	 * @return An appropriate UnconstrianedLikelihoodModel instance
	 */
	public static final Instance createInstance(LHCalculator.Factory base, SubstitutionModel model) {
	  return new InstanceImpl(base.createSeries(model.getNumberOfTransitionCategories(),model.getDataType()),model);
	}
}