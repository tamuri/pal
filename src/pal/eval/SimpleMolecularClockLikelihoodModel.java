// SimpleMolecularClockLikelihoodModel.java
//
// (c) 1999-2004 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: SimpleMolecularClockLikelihoodModel </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */

import pal.datatype.*;
import pal.misc.*;
import pal.substmodel.*;


public class SimpleMolecularClockLikelihoodModel implements MolecularClockLikelihoodModel {

	private static final class InternalImpl implements Internal {
	  private final LHCalculator.Internal base_;
		private final SubstitutionModel model_;
		private final HeightConverter heightConverter_;
		public InternalImpl(LHCalculator.Internal base, SubstitutionModel model, HeightConverter heightConverter) {
		  this.base_ = base;
			this.model_ = model;
			this.heightConverter_ = heightConverter;
		}

		/**
     * Extends left and right conditionals by type and then calculates flat conditionals
     * @param patternLookup
     * @param numberOfPatterns
     * @param leftConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
     * @param rightConditionalProbabilityProbabilties Implementations should be allowed to overwrite in certain cases
     * @return true if result build on cached information
     * @note An assumption may be made that after a call to this method the leftConditionals and rightConditionals are not used again!
     */
    public ConditionalProbabilityStore calculatePostExtendedFlatConditionals( double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
		  ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
      ConditionalProbabilityStore rightConditionalProbabilityProbabilties 	) {
		  return base_.calculatePostExtendedFlat(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern,leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties,true);

		}

		public ConditionalProbabilityStore calculateExtendedConditionals(
		    final double topBaseHeight, final double bottomBaseHeight, final PatternInfo centerPattern,
			  final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore rightConditionalProbabilityProbabilties
			) {
		  return base_.calculateExtended(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern,leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties,true);
		}

		public ConditionalProbabilityStore calculateAscendentExtendedConditionals( double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
		  ConditionalProbabilityStore ascenedentConditionalProbabilityProbabilties,
      ConditionalProbabilityStore otherConditionalProbabilityProbabilties 	) {
		  return base_.calculateExtended(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern,ascenedentConditionalProbabilityProbabilties,otherConditionalProbabilityProbabilties,true);
		}
		public ConditionalProbabilityStore calculateAscendentFlatConditionals( PatternInfo centerPattern,
		  ConditionalProbabilityStore ascenedentConditionalProbabilityProbabilties,
      ConditionalProbabilityStore otherConditionalProbabilityProbabilties 	) {
		  return base_.calculateFlat(centerPattern,ascenedentConditionalProbabilityProbabilties,otherConditionalProbabilityProbabilties);
		}
		public ConditionalProbabilityStore calculateFlatConditionals(
		    final PatternInfo centerPattern,
			  final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore rightConditionalProbabilityProbabilties
			) {
		  return base_.calculateFlat(centerPattern,leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties);
		}

	}

// -=-=--==-=-=-=---=-==-=--==-=-=-=-

	private static final class ExternalImpl implements External {
		private final LHCalculator.External base_;
		private final SubstitutionModel model_;
		private final HeightConverter heightConverter_;
		public ExternalImpl(LHCalculator.External base, SubstitutionModel model, HeightConverter heightConverter) {
		  this.base_ = base;
			this.model_ = model;
			this.heightConverter_ = heightConverter;
		}
		public void calculateSingleExtendedConditionals(double topBaseHeight, double bottomBaseHeight,
															 int numberOfPatterns,
															 ConditionalProbabilityStore baseConditionalProbabilities,
															 ConditionalProbabilityStore resultConditionalProbabilities) {
	   	base_.calculateSingleExtendedIndirect(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,numberOfPatterns,baseConditionalProbabilities,resultConditionalProbabilities);
		}

		public void calculateSingleDescendentExtendedConditionals(
		  double topBaseHeight, double bottomBaseHeight,
			PatternInfo centerPattern,
			ConditionalProbabilityStore descendentConditionalProbabilities
		) {
			base_.calculateSingleExtendedDirect(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern.getNumberOfPatterns(),descendentConditionalProbabilities);
		}
		/**
     */
    public void calculateSingleAscendentExtendedConditionalsDirect(
		  double topBaseHeight, double bottomBaseHeight,
			PatternInfo centerPattern,
		  ConditionalProbabilityStore ascendentConditionalProbabilityProbabilties
		) {
			base_.calculateSingleExtendedDirect(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern.getNumberOfPatterns(),ascendentConditionalProbabilityProbabilties);
		}
		/**
     */
    public void calculateSingleAscendentExtendedConditionalsIndirect(
		  double topBaseHeight, double bottomBaseHeight,
			PatternInfo centerPattern,
		  ConditionalProbabilityStore baseAscendentConditionalProbabilityProbabilties,
		  ConditionalProbabilityStore resultConditionalProbabilityProbabilties
		) {
			base_.calculateSingleExtendedIndirect(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern.getNumberOfPatterns(),baseAscendentConditionalProbabilityProbabilties,resultConditionalProbabilityProbabilties);
		}

		public void calculateExtendedConditionals( double topBaseHeight, double bottomBaseHeight,
                                   PatternInfo centerPattern,
                                   ConditionalProbabilityStore
                                   leftConditionalProbabilities,
                                   ConditionalProbabilityStore
                                   rightConditionalProbabilities,
                                   ConditionalProbabilityStore resultStore ) {
		  base_.calculateExtended(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight), model_,centerPattern,leftConditionalProbabilities,rightConditionalProbabilities,resultStore);
		}
		/**
     * Calculate the likelihood given a non root node
		 * @param nodeHeight the height of node doing the likelihood calculation
		 * @param centerPatter assumed left is ascendent component, right is descendent
     * @param ascendentConditionalProbabilities Assumed to be extended (downwards) to the nodeHeight
     * @param descendentConditionalProbabilities Assumed to be extended (upwards) to the nodeHeight
     * @return the Log likelihood
     */
    public double calculateLogLikelihoodNonRoot(
		  double nodeHeight, PatternInfo centerPattern,
      ConditionalProbabilityStore ascendentConditionalProbabilitiesStore,
      ConditionalProbabilityStore descendentConditionalProbabilitiesStore
		) {


			return base_.calculateLogLikelihood(model_,centerPattern,ascendentConditionalProbabilitiesStore,descendentConditionalProbabilitiesStore);
		}

    /**
     * Calculate the likelihood given two sub trees (left, right) and their extended likeihood probabilities
		 * @param rootHeight the height of the likelihood calculation
     * @param leftConditionalProbabilities Assumed to be extended to the rootHeight
     * @param rightConditionalProbabilities Assumed to be extended to the rootHeight
     * @return the Log likelihood
     */
    public double calculateLogLikelihood( double rootHeight, PatternInfo centerPattern,
                                       ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                       ConditionalProbabilityStore rightConditionalProbabilitiesStore ) {
		  return base_.calculateLogLikelihood(model_,centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore);
		}
		public double calculateLogLikelihoodSingle( double rootHeight, PatternInfo centerPattern,
																	 ConditionalProbabilityStore conditionalProbabilitiesStore ) {
			return base_.calculateLogLikelihoodSingle(model_,centerPattern.getPatternWeights(),centerPattern.getNumberOfPatterns(),conditionalProbabilitiesStore);
		}
		public void calculateFlatConditionals( double rootHeight, PatternInfo centerPattern,
                                   ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                   ConditionalProbabilityStore rightConditionalProbabilitiesStore,
																	 ConditionalProbabilityStore resultConditionalProbabilitiesStore) {
			base_.calculateFlat(centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore,resultConditionalProbabilitiesStore);
		}
		public SiteDetails calculateSiteDetails( double rootHeight, PatternInfo centerPattern,
                                       ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                       ConditionalProbabilityStore rightConditionalProbabilitiesStore ) {
		  return base_.calculateSiteDetailsRooted(model_,centerPattern, leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore);
		}
	}

// -=-=--==-=-=-=---=-==-=--==-=-=-=-

	private static final class LeafImpl implements Leaf {
		private final LHCalculator.Leaf base_;
		private final SubstitutionModel model_;
		private final HeightConverter heightConverter_;
		public LeafImpl(LHCalculator.Leaf base, SubstitutionModel model, HeightConverter heightConverter) {
		  this.base_ = base;
			this.model_ = model;
			this.heightConverter_ = heightConverter;
		}
		public ConditionalProbabilityStore calculateExtendedConditionals(double topBaseHeight, double bottomBaseHeight) {

			return base_.getExtendedConditionalProbabilities(heightConverter_.getExpectedSubstitutionDistance(bottomBaseHeight,topBaseHeight),model_,true);
		}
		public ConditionalProbabilityStore calculateFlatConditionals(double relatedHeight) {
		  return base_.getFlatConditionalProbabilities();
		}

	}

// -=-=--==-=-=-=---=-==-=--==-=-=-=-

//	private final static class SimulatorImpl implements Simulator{
//		private final SequenceSimulator simulator_;
//		private final boolean stochasticDistribution_;
//		public SimulatorImpl(SubstitutionModel model, int sequenceLength, boolean stochasticDistribution) {
//		  this.simulator_ = new SequenceSimulator(model,sequenceLength,stochasticDistribution);
//			this.stochasticDistribution_ = stochasticDistribution;
//		}
//	  public int[] getSimulated(int[] baseSequence, double topBaseHeight, double bottomBaseHeight) {
//		  return simulator_.getSimulated(baseSequence,topTime-bottomTime);
//		}
//		public void simulated(int[] baseSequence, double topBaseHeight, double bottomBaseHeight, int[] newSequence) {
//		  simulator_.simulate(baseSequence,topTime-bottomTime,newSequence);
//		}
//		public int[] generateRoot(double rootHeight) {
//		  return simulator_.generateRoot();
//		}
//		public void resetDistributions() {
//		  simulator_.resetSiteCategoryDistribution(stochasticDistribution_);
//		}
//	} //End of class SimulatorImpl

// -=-=--==-=-=-=---=-==-=--==-=-=-=-

	public static final Instance createInstance(LHCalculator.Factory baseFactory, SubstitutionModel model) {
		int numberOfCategories = model.getNumberOfTransitionCategories();
		DataType dt = model.getDataType();
		return new SimpleInstance(model, baseFactory.createSeries( numberOfCategories, dt ));
	}
	public static final Instance createInstance(SubstitutionModel model) {
		return createInstance(SimpleLHCalculator.getFactory(), model);
	}

// -=-=--==-=-=-=---=-==-=--==-=-=-=-

  private static final class SimpleInstance implements Instance {
    private int numberOfCategories_;
		private LHCalculator.Generator baseGenerator_;
		private SubstitutionModel substitutionModel_;
		private NeoParameterized parameterAccess_;

//		private static final
		//
		// Serialization Code
		//
		private static final long serialVersionUID = 2661663212643526344L;

		private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
			out.writeByte( 1 ); //Version number
			out.writeInt( numberOfCategories_ );
			out.writeObject( baseGenerator_ );
			out.writeObject( substitutionModel_ );
		}

		private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
			byte version = in.readByte();
			switch( version ) {
				default: {
					this.numberOfCategories_ = in.readInt();
					this.baseGenerator_ = (LHCalculator.Generator)in.readObject();
				  this.substitutionModel_ = (SubstitutionModel)in.readObject();
					break;
				}
			}
			this.parameterAccess_ = new ParameterizedNeoWrapper(substitutionModel_);
		}

    public SimpleInstance( SubstitutionModel sm, LHCalculator.Generator baseGenerator) {
      this.numberOfCategories_ = sm.getNumberOfTransitionCategories();
			this.substitutionModel_ = sm;
			this.baseGenerator_ = baseGenerator;
			this.parameterAccess_ = new ParameterizedNeoWrapper(substitutionModel_);
    }
		public Parameterized getSubstitutionModelParameterAccess() { return substitutionModel_; }
		public boolean hasSubstitutionModelParameters(){ return substitutionModel_.getNumParameters()!=0; }
		public Leaf createNewLeaf(HeightConverter heightConverter, PatternInfo pattern, int[] patternStateMatchup) {
		  return new LeafImpl( baseGenerator_.createNewLeaf(patternStateMatchup,pattern.getNumberOfPatterns()),substitutionModel_, heightConverter );
		}
    public External createNewExternal(HeightConverter heightConverter) {
      return new ExternalImpl( baseGenerator_.createNewExternal(), substitutionModel_, heightConverter );
    }

    public Internal createNewInternal(HeightConverter heightConverter) {
      return new InternalImpl( baseGenerator_.createNewInternal(), substitutionModel_, heightConverter );
    }

    public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore( boolean isForLeaf ) {
      return baseGenerator_.createAppropriateConditionalProbabilityStore(isForLeaf);
    }
		public String getSubstitutionModelSummary() { return "Model:"+substitutionModel_.toString(); }
		public NeoParameterized getParameterAccess() { return parameterAccess_; }
	}

}