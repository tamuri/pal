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

import java.io.*;

import pal.datatype.*;
import pal.math.*;
import pal.misc.*;
import pal.substmodel.*;


public class SingleSplitMolecularClockLikelihoodModel implements MolecularClockLikelihoodModel {
	private static final boolean isUseLowerModelOnly(double changeHeight, double beforeSplitHeight, double lowerHeight) {
	  return (changeHeight>=beforeSplitHeight);
	}
	private static final boolean isUseUpperSampleOnly(double changeHeight, double beforeSplitHeight, double afterSplitHeight) {
	  return (changeHeight<=afterSplitHeight);
	}



// -=-=--==-=-=-=---=-==-=--==-=-=-=-

	private static final class ExternalImpl implements External {
		private final LHCalculator.External base_;
		private final CombineModel model_;
		private final HeightConverter heightConverter_;
		public ExternalImpl(LHCalculator.External base, CombineModel model, HeightConverter heightConverter) {
		  this.base_ = base;
			this.model_ = model;
			this.heightConverter_ = heightConverter;
		}

	  public void calculateSingleExtendedConditionals(double topBaseHeight, double bottomBaseHeight,
															 int numberOfPatterns,
															 ConditionalProbabilityStore baseConditionalProbabilities,
															 ConditionalProbabilityStore resultConditionalProbabilities) {
			model_.setup(bottomBaseHeight,heightConverter_,false);
			base_.calculateSingleExtendedIndirect(model_.getAdjustedDistance(topBaseHeight), model_,numberOfPatterns,baseConditionalProbabilities,resultConditionalProbabilities);
		}

		public void calculateSingleDescendentExtendedConditionals(
		  double topBaseHeight, double bottomBaseHeight,
			PatternInfo centerPattern,
			ConditionalProbabilityStore descendentConditionalProbabilities
		) {
			model_.setup(bottomBaseHeight,heightConverter_,false);
			base_.calculateSingleExtendedDirect(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern.getNumberOfPatterns(),descendentConditionalProbabilities);
		}
		/**
     */
    public void calculateSingleAscendentExtendedConditionalsDirect(
		  double topBaseHeight, double bottomBaseHeight,
			PatternInfo centerPattern,
		  ConditionalProbabilityStore ascendentConditionalProbabilityProbabilties
		) {
//			System.out.println("**** ASCENDENT 3************");

			model_.setup(bottomBaseHeight,heightConverter_,true);
			base_.calculateSingleExtendedDirect(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern.getNumberOfPatterns(),ascendentConditionalProbabilityProbabilties);
		}
		/**
     */
    public void calculateSingleAscendentExtendedConditionalsIndirect(
		  double topBaseHeight, double bottomBaseHeight,
			PatternInfo centerPattern,
		  ConditionalProbabilityStore baseAscendentConditionalProbabilityProbabilties,
		  ConditionalProbabilityStore resultConditionalProbabilityProbabilties
		) {
//			System.out.println("**** ASCENDENT 2************");

			model_.setup(bottomBaseHeight,heightConverter_,true);
			base_.calculateSingleExtendedIndirect(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern.getNumberOfPatterns(),baseAscendentConditionalProbabilityProbabilties,resultConditionalProbabilityProbabilties);
		}

		public void calculateExtendedConditionals( double topBaseHeight, double bottomBaseHeight,
                                   PatternInfo centerPattern,
                                   ConditionalProbabilityStore
                                   leftConditionalProbabilities,
                                   ConditionalProbabilityStore
                                   rightConditionalProbabilities,
                                   ConditionalProbabilityStore resultStore ) {
			model_.setup(bottomBaseHeight,heightConverter_,false);
		  base_.calculateExtended(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern,leftConditionalProbabilities,rightConditionalProbabilities,resultStore);
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
			model_.setup(nodeHeight,heightConverter_,false);
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
			model_.setup(rootHeight,heightConverter_,false);
			return  base_.calculateLogLikelihood(model_,centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore);
		}
		public double calculateLogLikelihoodSingle( double rootHeight, PatternInfo centerPattern,
																	 ConditionalProbabilityStore conditionalProbabilitiesStore ) {
			model_.setup(rootHeight,heightConverter_,false);
			return base_.calculateLogLikelihoodSingle(model_,centerPattern.getPatternWeights(),centerPattern.getNumberOfPatterns(),conditionalProbabilitiesStore);
		}
		public void calculateFlatConditionals( double rootHeight, PatternInfo centerPattern,
                                   ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                   ConditionalProbabilityStore rightConditionalProbabilitiesStore,
																	 ConditionalProbabilityStore resultConditionalProbabilitiesStore) {
			model_.setup(rootHeight,heightConverter_,false);
			base_.calculateFlat(centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore,resultConditionalProbabilitiesStore);
		}
		public SiteDetails calculateSiteDetails( double rootHeight, PatternInfo centerPattern,
                                       ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                       ConditionalProbabilityStore rightConditionalProbabilitiesStore ) {
			model_.setup(rootHeight,heightConverter_,false);
			return base_.calculateSiteDetailsRooted(model_,centerPattern, leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore);
		}
		public void calculateFlatConditionals( double rootHeight, PatternInfo centerPattern,
                                   ConditionalProbabilityStore leftConditionalProbabilitiesStore,
                                   ConditionalProbabilityStore rightConditionalProbabilitiesStore,
																	 ConditionalProbabilityStore resultConditionalProbabilitiesStore,
																	 double[] sampleHeights) {
			model_.setup(rootHeight,heightConverter_,false);
		  base_.calculateFlat(centerPattern,leftConditionalProbabilitiesStore,rightConditionalProbabilitiesStore,resultConditionalProbabilitiesStore);
		}


	}

// -=-=--==-=-=-=---=-==-=--==-=-=-=-

	private static final class LeafImpl implements Leaf {
		private final LHCalculator.Leaf base_;
		private final CombineModel model_;
		private final int numberOfPatterns_;
		private final HeightConverter heightConverter_;
		public LeafImpl(LHCalculator.Leaf base,  CombineModel model, int numberOfPatterns, HeightConverter heightConverter) {
		  this.base_ = base;	this.model_ = model;	this.numberOfPatterns_ = numberOfPatterns;
			this.heightConverter_ = heightConverter;
		}
		public ConditionalProbabilityStore calculateExtendedConditionals(double topBaseHeight, double bottomBaseHeight) {
		  model_.setup(bottomBaseHeight,heightConverter_,false);
			return base_.getExtendedConditionalProbabilities(model_.getAdjustedDistance(topBaseHeight),model_,true);
		}
		public ConditionalProbabilityStore calculateFlatConditionals(double relatedHeight) {
		  return base_.getFlatConditionalProbabilities();
		}
	}

	private static final class InternalImpl implements Internal {

		private final LHCalculator.Internal base_;
		private final CombineModel model_;
		private final HeightConverter heightConverter_;
		public InternalImpl(LHCalculator.Generator generator, CombineModel model, HeightConverter heightConverter) {
		  this.base_ = generator.createNewInternal();
			this.model_ = model;
			this.heightConverter_ = heightConverter;
		}
    public ConditionalProbabilityStore calculatePostExtendedFlatConditionals( double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
		  ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
      ConditionalProbabilityStore rightConditionalProbabilityProbabilties 	) {
		  model_.setup(bottomBaseHeight,heightConverter_,false);
		  return base_.calculatePostExtendedFlat(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern,leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties,true);

		}

		public ConditionalProbabilityStore calculateExtendedConditionals(
		    final double topBaseHeight, final double bottomBaseHeight, final PatternInfo centerPattern,
			  final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore rightConditionalProbabilityProbabilties
			) {
		  model_.setup(bottomBaseHeight,heightConverter_,false);
		  return base_.calculateExtended(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern,leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties,true);
		}

		public ConditionalProbabilityStore calculateAscendentExtendedConditionals( double topBaseHeight, double bottomBaseHeight, PatternInfo centerPattern,
		  ConditionalProbabilityStore ascenedentConditionalProbabilityProbabilties,
      ConditionalProbabilityStore otherConditionalProbabilityProbabilties 	) {
//		  System.out.println("**** ASCENDENT 1************");
		  model_.setup(bottomBaseHeight,heightConverter_,true);
		  return base_.calculateExtended(model_.getAdjustedDistance(topBaseHeight), model_,centerPattern,ascenedentConditionalProbabilityProbabilties,otherConditionalProbabilityProbabilties,true);
		}
		public ConditionalProbabilityStore calculateAscendentFlatConditionals( PatternInfo centerPattern,
		  ConditionalProbabilityStore ascenedentConditionalProbabilityProbabilties,
      ConditionalProbabilityStore otherConditionalProbabilityProbabilties 	) {
//				System.out.println("**** ASCENDENT FLAT ************");

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


	public static final Instance createInstance(RateMatrixGroup beforeSplitMatrices, RateMatrixGroup afterSplitMatrics, NeoParameterized acrossSplitParameters, SingleSplitDistribution probabilityModel, LHCalculator.Factory baseFactory, double splitTime) {
		int numberOfBaseCategories = beforeSplitMatrices.getNumberOfTransitionCategories();
		DataType dt = beforeSplitMatrices.getDataType();
		return new SimpleInstance(beforeSplitMatrices, afterSplitMatrics, acrossSplitParameters, probabilityModel, splitTime, baseFactory.createSeries( numberOfBaseCategories*numberOfBaseCategories, dt ));
	}

	public static final Instance createInstance(RateMatrixGroup beforeSplitMatrices, RateMatrixGroup afterSplitMatrics, NeoParameterized acrossSplitParameters, SingleSplitDistribution probabilityModel, double splitTime) {
		return createInstance(beforeSplitMatrices,afterSplitMatrics,acrossSplitParameters, probabilityModel,SimpleLHCalculator.getFactory(),splitTime);
	}
	public static final Instance createInstance(RateMatrixGroup beforeSplitMatrices, RateMatrixGroup afterSplitMatrics, NeoParameterized acrossSplitParameters, double splitTime) {
		return createInstance(beforeSplitMatrices,afterSplitMatrics, acrossSplitParameters,new SaturatedSingleSplitDistribution(beforeSplitMatrices.getNumberOfTransitionCategories()),SimpleLHCalculator.getFactory(),splitTime);
	}
	public static final Instance createInstance(RateMatrixGroup beforeSplitMatrices, RateMatrixGroup afterSplitMatrics, NeoParameterized acrossSplitParameters, double[] classProbabilities, double splitTime) {
		SingleSplitDistribution pm;
		int numberOfClasses = beforeSplitMatrices.getNumberOfTransitionCategories();
		if(classProbabilities.length == numberOfClasses) {
			double[] result = new double[numberOfClasses*numberOfClasses];
			for(int i = 0 ; i < numberOfClasses ; i++) {
			  result[i*numberOfClasses+i] = classProbabilities[i];
			}
			pm = new SaturatedSingleSplitDistribution(beforeSplitMatrices.getNumberOfTransitionCategories(),result );
		} else {
			pm = new SaturatedSingleSplitDistribution( beforeSplitMatrices.getNumberOfTransitionCategories(),classProbabilities );
		}
		return createInstance(beforeSplitMatrices,afterSplitMatrics,acrossSplitParameters,pm,SimpleLHCalculator.getFactory(),splitTime);
	}
  private static final class SimpleInstance implements Instance, java.io.Serializable {
		private final LHCalculator.Generator baseGenerator_;
		private final TotalModel totalModel_;

		public SimpleInstance(RateMatrixGroup beforeSplitModel, RateMatrixGroup afterSplitModel, NeoParameterized acrossSplitParameters, SingleSplitDistribution probabilityModel, double splitTime, LHCalculator.Generator baseGenerator) {
      this.totalModel_ = new TotalModel(beforeSplitModel,afterSplitModel,acrossSplitParameters, probabilityModel, splitTime,baseGenerator.createNewExternal());
			this.baseGenerator_ = baseGenerator;
    }
		public NeoParameterized getSubstitutionModelParameterAccess() {return totalModel_; }
		public boolean hasSubstitutionModelParameters(){ return  true; }




		public Leaf createNewLeaf(HeightConverter heightConverter, PatternInfo pattern, int[] patternStateMatchup) {
			return new LeafImpl(
			  baseGenerator_.createNewLeaf(
				  patternStateMatchup,pattern.getNumberOfPatterns()
				),
				totalModel_.getCombineModel(), pattern.getNumberOfPatterns(),
				heightConverter
			  );
			}
    public External createNewExternal(HeightConverter heightConverter) {
		  return new ExternalImpl( baseGenerator_.createNewExternal(), totalModel_.getCombineModel(), heightConverter);
		}

    public Internal createNewInternal(HeightConverter heightConverter) {
      return new InternalImpl( baseGenerator_, totalModel_.getCombineModel(), heightConverter );
    }

    public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore( boolean isForLeaf ) {
      return baseGenerator_.createAppropriateConditionalProbabilityStore(isForLeaf);
    }
		public String getSubstitutionModelSummary() { return "Model:"+totalModel_.getSummary(); }
		public NeoParameterized getParameterAccess() { return totalModel_; }

  }

// -=-=--==-=-=-=---=-==-=--==-=-=-=-




// =================================================================================================
// ================================ TotalModel =====================================================
// =================================================================================================
	private final static class TotalModel implements java.io.Serializable, NeoParameterized {
		//Serialized variables
		private final SingleSplitDistribution probabilityModel_;
		private final CombineModel combineModel_;
		private final int numberOfProbabilityParameters_;
		private final int numberOfParameters_;
		public TotalModel(RateMatrixGroup beforeSplitMatrices, RateMatrixGroup afterSplitMatrices, NeoParameterized acrossSplitParameters, SingleSplitDistribution probabilityModel, double splitHeight, LHCalculator.External externalCalculator) {
		  this.probabilityModel_ = probabilityModel;
			this.numberOfProbabilityParameters_ = probabilityModel.getNumberOfParameters();
			this.combineModel_ = new CombineModel(this, beforeSplitMatrices, afterSplitMatrices, acrossSplitParameters, probabilityModel.getNumberOfBaseTransitionCategories(), splitHeight, externalCalculator);
			this.numberOfParameters_ = probabilityModel.getNumberOfParameters()+combineModel_.getNumberOfParameters();
		}
		public final int getNumberOfTransitionCategories() { return combineModel_.getNumberOfTransitionCategories(); }

		public final double[][] getBaseCategoryProbabilities() {  return probabilityModel_.getDistributionInfo(); 	}
		public final CombineModel getCombineModel() { return combineModel_; }

		public String getSummary() {	return "Distribution:"+probabilityModel_+"\nModel:"+combineModel_.getSummary();	}

		public int getNumberOfParameters() { return numberOfParameters_; }

	  public void setParameters(double[] parameters, int startIndex) {
		  probabilityModel_.setParameters(parameters,startIndex);
			combineModel_.setParameters(parameters,startIndex+numberOfProbabilityParameters_);
		}
		public void getParameters(double[] parameterStore, int startIndex) {
		  probabilityModel_.getParameters(parameterStore,startIndex);
			combineModel_.getParameters(parameterStore,startIndex+numberOfProbabilityParameters_);

		}
		public double getLowerLimit(int n) {
		  if(n<numberOfProbabilityParameters_) {
			  return probabilityModel_.getLowerLimit(n);
			} else {
			  return combineModel_.getLowerLimit(n-numberOfProbabilityParameters_);
			}
		}
		public double getUpperLimit(int n) {
		  if(n<numberOfProbabilityParameters_) {
			  return probabilityModel_.getUpperLimit(n);
			} else {
			  return combineModel_.getUpperLimit(n-numberOfProbabilityParameters_);
			}
		}
	  public void getDefaultValues(double[] store, int startIndex) {
			probabilityModel_.getDefaultValues(store,startIndex);
			combineModel_.getDefaultValues(store,startIndex+numberOfProbabilityParameters_);

		}

	} //End of class TotalModel
// -=-=-=-=-==--=-=-=

// =================================================================================================
// =============================== CombineModel ====================================================
// =================================================================================================
	private final static class CombineModel implements SubstitutionModel, java.io.Serializable, NeoParameterized {
		// Things that form parameters
		private final RateMatrixGroup beforeSplitMatrices_;
		private final RateMatrixGroup afterSplitMatrices_;
		private final NeoParameterized parameters_;


		private final LHCalculator.External externalCalculator_;

		private final int numberOfStates_;
		private final DataType dataType_;

		private final int numberOfTransitionCategories_;
		private final int numberOfBaseTransitionCategories_;

		private final double[][][] beforeSplitTransitionStore_;
		private final double[][][] afterSplitTransitionStore_;
		private final double[][] singleBeforeSplitTransitionStore_;
		private final double[][] singleAfterSplitTransitionStore_;


		private final double[] equilibriumFrequencies_;

		private final double[] overallCategoryProbabilities_;
		private final double[] afterSplitCategoryProbabilities_;
		private final double[] beforeSplitCategoryProbabilities_;

		private double afterSplitBaseHeight_;
		private final double splitHeight_;
		private boolean isAscendent_;
		private HeightConverter currentHeightConverter_;

		private final TotalModel parent_;
		private boolean needsRebuild_;

		//
	  // Serialization code
	  //
	  private static final long serialVersionUID= -9348239481572L;


		public CombineModel(TotalModel parent, RateMatrixGroup beforeSplitMatrices, RateMatrixGroup afterSplitMatrices, NeoParameterized parameters, int numberOfBaseTransitionCategories, double splitHeight, LHCalculator.External externalCalculator) {
		  this.parameters_ = parameters;

			this.parent_ = parent;
			this.numberOfBaseTransitionCategories_ = numberOfBaseTransitionCategories;
			if(
			    numberOfBaseTransitionCategories!=beforeSplitMatrices.getNumberOfTransitionCategories()||
			    numberOfBaseTransitionCategories!=afterSplitMatrices.getNumberOfTransitionCategories()
				) {
			  throw new IllegalArgumentException("Invalid matrix array: wrong length (expecting "+numberOfBaseTransitionCategories+")");
			}
		  this.splitHeight_ = splitHeight;
		  this.equilibriumFrequencies_ = beforeSplitMatrices.getEquilibriumFrequencies();
		  this.numberOfTransitionCategories_ = numberOfBaseTransitionCategories*numberOfBaseTransitionCategories;
			this.beforeSplitMatrices_ = beforeSplitMatrices;
			this.afterSplitMatrices_ = afterSplitMatrices;
			this.externalCalculator_ = externalCalculator;
		  this.overallCategoryProbabilities_ = new double[numberOfTransitionCategories_];
		  this.afterSplitCategoryProbabilities_ = new double[numberOfBaseTransitionCategories];
		  this.beforeSplitCategoryProbabilities_ = new double[numberOfBaseTransitionCategories];

			dataType_ = beforeSplitMatrices.getDataType();
			numberOfStates_ = dataType_.getNumStates();

			this.beforeSplitTransitionStore_ = new double[numberOfBaseTransitionCategories][numberOfStates_][numberOfStates_];
			this.afterSplitTransitionStore_ = new double[numberOfBaseTransitionCategories][numberOfStates_][numberOfStates_];

			this.singleAfterSplitTransitionStore_ = new double[numberOfStates_][numberOfStates_];
			this.singleBeforeSplitTransitionStore_ = new double[numberOfStates_][numberOfStates_];


			int index = 0;

			scheduleRebuild();
		}
		// Some attribute has changed so rebuild!
		public final void scheduleRebuild() {	  this.needsRebuild_ = true;		}
		private final void checkRebuild() {
		  if(needsRebuild_) {
				rebuildCategoryProbabilities();
		    afterSplitMatrices_.updateParameters(afterSplitCategoryProbabilities_);
				beforeSplitMatrices_.updateParameters(beforeSplitCategoryProbabilities_);
				needsRebuild_ = false;
			}
		}

		private final void rebuildCategoryProbabilities() {
		  double[][] baseProbabilities = parent_.getBaseCategoryProbabilities();
			int index = 0;
			for(int beforeSplit = 0 ; beforeSplit < numberOfBaseTransitionCategories_ ; beforeSplit++) {
			  double total = 0;
				for(int afterSplit = 0 ; afterSplit < numberOfBaseTransitionCategories_ ; afterSplit++) {
					total += baseProbabilities[beforeSplit][afterSplit];
					overallCategoryProbabilities_[index++] = baseProbabilities[beforeSplit][afterSplit];
			  }
				this.beforeSplitCategoryProbabilities_[beforeSplit] = total;
			}
			for(int afterSplit = 0 ; afterSplit < numberOfBaseTransitionCategories_ ; afterSplit++) {
			  double total = 0;
				for(int beforeSplit = 0 ; beforeSplit < numberOfBaseTransitionCategories_ ; beforeSplit++) {
					total += baseProbabilities[beforeSplit][afterSplit];
			  }
				this.afterSplitCategoryProbabilities_[afterSplit] = total;
			}
		}
		// =====


		public final int getNumberOfStates() { return numberOfStates_; }

//For adjusting relative to a base height (to allow simply LH calculator stuff to work
		public final void setup(double afterSplitBaseHeight, HeightConverter converter, boolean isAscendent) {
		  this.afterSplitBaseHeight_ = afterSplitBaseHeight;
			this.currentHeightConverter_ = converter;
			this.isAscendent_ = isAscendent;
		}
		public final double getAdjustedDistance(double beforeSplitBaseHeight) {	  return beforeSplitBaseHeight-afterSplitBaseHeight_;	}

//For NeoParameterized interface
	  public int getNumberOfParameters() { return parameters_.getNumberOfParameters(); }
		public void setParameters(double[] parameters, int startIndex) {
		  parameters_.setParameters(parameters,startIndex);
			scheduleRebuild();
		}
		public void getParameters(double[] store, int startIndex) {
		  parameters_.getParameters(store,startIndex);
		}

		public void getDefaultValues(double[] store, int startIndex) {
		  parameters_.getDefaultValues(store,startIndex);

		}

		public double getLowerLimit(int n) { return parameters_.getLowerLimit(n); }
	  public double getUpperLimit(int n) { return parameters_.getUpperLimit(n); }

//For Parameterized interface (legacy stuff, that we don't use)

	  public int getNumParameters() { throw new RuntimeException("Assertion error : not expecting use of legacy code!"); }
		public double getDefaultValue(int n) { throw new RuntimeException("Assertion error : not expecting use of legacy code!"); }
		public void setParameter(double param, int n) {
			throw new RuntimeException("Assertion error : not expecting use of legacy code!");
		}
		public double getParameter(int n){
			throw new RuntimeException("Assertion error : not expecting use of legacy code!");
		}
		public void setParameterSE(double paramSE, int n) {
			throw new RuntimeException("Assertion error : not expecting use of legacy code!");
		}

// Substitution Model stuff
		public DataType getDataType() { return dataType_; }

	  public int getNumberOfTransitionCategories() { return numberOfTransitionCategories_; }
	  public double getTransitionCategoryProbability(int category) {
			checkRebuild();  return overallCategoryProbabilities_[category];
		}
	  public double[] getTransitionCategoryProbabilities() {
		  checkRebuild();	return overallCategoryProbabilities_;
		}
		private final void copy(double[][] source, double[][] destination) {
		  for(int i = 0 ; i < numberOfStates_ ; i++) {
				System.arraycopy(source[i], 0, destination[i], 0 , numberOfStates_);
			}
		}
		private final void combine(double[][] beforeSplit, double[][] afterSplit, double[][] result) {
			for(int from = 0 ; from < numberOfStates_ ; from++) {
				for(int to = 0 ; to < numberOfStates_ ; to++) {
				  double total = 0;
					for(int intermediate = 0 ; intermediate < numberOfStates_ ; intermediate++) {
						total+=beforeSplit[from][intermediate]*afterSplit[intermediate][to];
					}
					result[from][to] = total;
				}
			}
		}

		private final void combineTranspose(double[][] beforeSplit, double[][] afterSplit, double[][] result) {
			for(int from = 0 ; from < numberOfStates_ ; from++) {
				for(int to = 0 ; to < numberOfStates_ ; to++) {
				  double total = 0;
					for(int intermediate = 0 ; intermediate < numberOfStates_ ; intermediate++) {
						total+=beforeSplit[intermediate][from]*afterSplit[to][intermediate];
					}
					result[to][from] = total;
				}
			}
		}

		private final int resultIndex(int beforeSplit, int afterSplit) {  return beforeSplit*numberOfBaseTransitionCategories_+afterSplit;		}

		private final void getTransitionProbabilities(double branchLength, double[][][] transitionStore, RateMatrixGroup group, boolean transpose) {
			if(transpose) {	  group.getTransitionProbabilitiesTranspose(branchLength,transitionStore);	}
			else {  group.getTransitionProbabilities(branchLength,transitionStore);	}
		}
		private final void getTransitionProbabilities(double branchLength, int category, double[][] transitionStore, RateMatrixGroup group, boolean transpose) {
			if(transpose) {  group.getTransitionProbabilitiesTranspose(branchLength,category, transitionStore);	}
			else { group.getTransitionProbabilities(branchLength,category, transitionStore);	}
		}
		private final double sum(double[][] tableStore) {
			double total = 0;
			for(int j = 0 ; j < numberOfStates_ ; j++) {
			  for(int i = 0 ; i < numberOfStates_ ; i++) {	total+=tableStore[j][i];  }
			}
			return total;
		}
		public boolean isSplit(double beforeSplitBaseHeight) {
		  if(splitHeight_>=beforeSplitBaseHeight || splitHeight_<=afterSplitBaseHeight_) {  return false;}
			else {  return true;	}
		}
		private final void getSplitTransitionProbabilitiesDescendentImpl( boolean isTranspose, double[][][] tableStore) {
		  if(isTranspose) {
				for( int first = 0; first<numberOfBaseTransitionCategories_; first++ ) {
					for( int second = 0; second<numberOfBaseTransitionCategories_; second++ ) {
						combineTranspose( beforeSplitTransitionStore_[first], afterSplitTransitionStore_[second], tableStore[resultIndex( first, second )] );
					}
				}
			} else {
				for( int first = 0; first<numberOfBaseTransitionCategories_; first++ ) {
					for( int second = 0; second<numberOfBaseTransitionCategories_; second++ ) {
						combine(  beforeSplitTransitionStore_[first], afterSplitTransitionStore_[second], tableStore[resultIndex( first, second )] );
					}
				}
			}
		}
		private final void getSplitTransitionProbabilitiesAscendentImpl( boolean isTranspose, double[][][] tableStore) {
		  if(isTranspose) {
				for( int first = 0; first<numberOfBaseTransitionCategories_; first++ ) {
					for( int second = 0; second<numberOfBaseTransitionCategories_; second++ ) {
						combineTranspose( afterSplitTransitionStore_[second], beforeSplitTransitionStore_[first],  tableStore[resultIndex( first, second )] );
					}
				}
			} else {
				for( int first = 0; first<numberOfBaseTransitionCategories_; first++ ) {
					for( int second = 0; second<numberOfBaseTransitionCategories_; second++ ) {
						combine(  afterSplitTransitionStore_[second], beforeSplitTransitionStore_[first], tableStore[resultIndex( first, second )] );
					}
				}
			}
		}
		/**
		 * Main transition probability method
		 * @param branchLength
		 * @param tableStore
		 * @param isTranspose
		 */
		private final void getTransitionProbabilitiesImpl(double branchLength, double[][][] tableStore,boolean isTranspose) {
			//Convert back after tricking...
			final double beforeSplitBaseHeight = afterSplitBaseHeight_+branchLength;
//			System.out.println(afterSplitBaseHeight_+" - "+beforeSplitBaseHeight+"    ("+splitHeight_+")");
			if(splitHeight_>=beforeSplitBaseHeight ) {
			  //Use afterSplit Model
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(afterSplitBaseHeight_,beforeSplitBaseHeight),afterSplitTransitionStore_, afterSplitMatrices_, isTranspose);
				for(int beforeSplit = 0 ; beforeSplit < numberOfBaseTransitionCategories_ ; beforeSplit++) {
				  for(int afterSplit = 0 ; afterSplit < numberOfBaseTransitionCategories_ ; afterSplit++) {
						copy(afterSplitTransitionStore_[afterSplit], tableStore[resultIndex(beforeSplit,afterSplit)]);
					}
				}
			} else if(splitHeight_<=afterSplitBaseHeight_) {
				//Use beforeSplit model
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(afterSplitBaseHeight_,beforeSplitBaseHeight),beforeSplitTransitionStore_,beforeSplitMatrices_,isTranspose);
				for(int beforeSplit = 0 ; beforeSplit < numberOfBaseTransitionCategories_ ; beforeSplit++) {
				  for(int afterSplit = 0 ; afterSplit < numberOfBaseTransitionCategories_ ; afterSplit++) {
				    copy(beforeSplitTransitionStore_[beforeSplit], tableStore[resultIndex(beforeSplit,afterSplit)]);
				  }
				}
			} else {
				 //split
//				System.out.println("Split:"+isAscendent_);
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(afterSplitBaseHeight_, splitHeight_),afterSplitTransitionStore_, afterSplitMatrices_, isTranspose);
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(splitHeight_, beforeSplitBaseHeight),beforeSplitTransitionStore_,beforeSplitMatrices_,isTranspose);

				if(isAscendent_) {
				  getSplitTransitionProbabilitiesAscendentImpl(isTranspose,tableStore);
				} else {
				  getSplitTransitionProbabilitiesDescendentImpl(isTranspose,tableStore);
				}
			}
		}

	  private final void getTransitionProbabilitiesImpl(double branchLength, int category, double[][] tableStore, boolean isTranspose) {
			double beforeSplitBaseHeight = afterSplitBaseHeight_+branchLength;
		  int beforeSplit = category/numberOfBaseTransitionCategories_;
			int afterSplit = category%numberOfBaseTransitionCategories_;

			if(splitHeight_>=beforeSplitBaseHeight ) {
			  //Use afterSplit Model
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(afterSplitBaseHeight_,beforeSplitBaseHeight),afterSplit, tableStore, afterSplitMatrices_, isTranspose);
			} else if(splitHeight_<=afterSplitBaseHeight_) {
			  //Use beforeSplit model
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(afterSplitBaseHeight_,beforeSplitBaseHeight),beforeSplit, tableStore, beforeSplitMatrices_, isTranspose);
			} else {
			  //split
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(afterSplitBaseHeight_,splitHeight_),afterSplit, afterSplitTransitionStore_[0], afterSplitMatrices_, isTranspose);
				getTransitionProbabilities(currentHeightConverter_.getExpectedSubstitutionDistance(splitHeight_,beforeSplitBaseHeight),beforeSplit, beforeSplitTransitionStore_[0],beforeSplitMatrices_,isTranspose);
				if(isAscendent_) {
				  if( isTranspose ) {
						combineTranspose( afterSplitTransitionStore_[0], beforeSplitTransitionStore_[0], tableStore );
					} else {
						combine( afterSplitTransitionStore_[0], beforeSplitTransitionStore_[0], tableStore );
					}
				} else {
					if( isTranspose ) {
						combineTranspose( beforeSplitTransitionStore_[0], afterSplitTransitionStore_[0], tableStore );
					} else {
						combine( beforeSplitTransitionStore_[0], afterSplitTransitionStore_[0], tableStore );
					}
				}
			}
		}

// -=-=-=-=-=
	  public void getTransitionProbabilities(double branchLength, double[][][] tableStore) {
		  checkRebuild();	getTransitionProbabilitiesImpl(branchLength,tableStore, false);
		}
		public void getTransitionProbabilities(double branchLength, int category, double[][] tableStore) {
	    checkRebuild();	getTransitionProbabilitiesImpl(branchLength,category,tableStore,false);
		}

	  public void getTransitionProbabilitiesTranspose(double branchLength, double[][][] tableStore) {
		  checkRebuild();	getTransitionProbabilitiesImpl(branchLength,tableStore, true);
		}
		public void getTransitionProbabilitiesTranspose(double branchLength, int category, double[][] tableStore) {
	    checkRebuild();	getTransitionProbabilitiesImpl(branchLength,category,tableStore,true);
		}

		public double[] getEquilibriumFrequencies() { return equilibriumFrequencies_; }

		public void addPalObjectListener(PalObjectListener l) { throw new RuntimeException("Not implemented yet!"); }
	  public void removePalObjectListener(PalObjectListener l) { throw new RuntimeException("Not implemented yet!"); }

		public OrthogonalHints getOrthogonalHints() { return null; }

		public void report(PrintWriter out) { }

		public String getSummary() {
			checkRebuild();
			final double[] afterSplitProbs = new double[numberOfBaseTransitionCategories_];
			final double[] beforeSplitProbs = new double[numberOfBaseTransitionCategories_];
			for(int i = 0 ; i < numberOfTransitionCategories_ ; i++) {
			  int beforeSplit = i/numberOfBaseTransitionCategories_;
				int afterSplit = i%numberOfBaseTransitionCategories_;
				afterSplitProbs[afterSplit]+=overallCategoryProbabilities_[i];
				beforeSplitProbs[beforeSplit]+=overallCategoryProbabilities_[i];
			}
			return
			  "Split height:"+splitHeight_+"\n"+
			  "Category probs:"+pal.misc.Utils.toString(overallCategoryProbabilities_)+"\n"+
			  "Before Split probs:"+pal.misc.Utils.toString(beforeSplitCategoryProbabilities_)+"\n"+
				"Before Split probs (check):"+pal.misc.Utils.toString(beforeSplitProbs)+"\n"+
			  "After Split probs:"+pal.misc.Utils.toString(afterSplitCategoryProbabilities_)+"\n"+
				"After Split probs (check):"+pal.misc.Utils.toString(afterSplitProbs)+"\n"+
				"Parameters:"+parameters_.toString();
		}
		public final Object clone() { throw new RuntimeException("Not implemented yet!"); }
	} //End of class CombineModel

	private static final void transpose(final double[][] matrix, final int numberOfStates) {
		for(int from = 0 ; from < numberOfStates ; from++) {
			for( int to = from; to<numberOfStates; to++ ) {
				double temp = matrix[from][to];
				matrix[from][to] = matrix[to][from];
				matrix[to][from] = temp;
			}
		}
	}
	public static final void main(String[] args) {
		double[][] m = {{ 1, 8, 1, 1}, {2,7,2,2}, {3,6,3,3}, {4,4,5,4}};
		System.out.println("Base");
		System.out.println(pal.misc.Utils.toString(m));
		transpose(m,4);
		System.out.println("Transpose");
		System.out.println(pal.misc.Utils.toString(m));

	}
// =============================================================================================
// =========== Probability Model stuff =========================================================


}