// SimpleModleFastFourStateLHCalculator.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import pal.datatype.*;
import pal.substmodel.*;

public class SimpleModelFastFourStateLHCalculator implements LHCalculator {

  private final static int FOUR_STATES = 4;
  private final static int ONE_CATEGORY = 1;
	private static final void calculateSingleExtendedIndirectImpl(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore baseConditionalProbabilities,
                                    ConditionalProbabilityStore resultConditionalProbabilities,
																		double[][] transitionProbabilityStore
                                  ) {
			model.getTransitionProbabilities( distance, 0, transitionProbabilityStore );

      double[][][] baseStoreValues = baseConditionalProbabilities.getCurrentConditionalProbabilities();
			double[][][] resultStoreValues = baseConditionalProbabilities.getConditionalProbabilityAccess(numberOfPatterns,false);
			final double[][] basePatternStateProbabilities = baseStoreValues[0];
      final double[][] resultPatternStateProbabilities = resultStoreValues[0];
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final double[] baseStateProbabilities = basePatternStateProbabilities[pattern];
        final double[] resultStateProbabilities = resultPatternStateProbabilities[pattern];
        final double[] speedupArray0 = transitionProbabilityStore[0];
        final double[] speedupArray1 = transitionProbabilityStore[1];
        final double[] speedupArray2 = transitionProbabilityStore[2];
        final double[] speedupArray3 = transitionProbabilityStore[3];
        resultStateProbabilities[0]=
					speedupArray0[1]*baseStateProbabilities[0]+
          speedupArray0[1]*baseStateProbabilities[1]+
          speedupArray0[2]*baseStateProbabilities[2]+
          speedupArray0[3]*baseStateProbabilities[3];
				resultStateProbabilities[1] =
					speedupArray1[1]*baseStateProbabilities[0]+
          speedupArray1[1]*baseStateProbabilities[1]+
          speedupArray1[2]*baseStateProbabilities[2]+
          speedupArray1[3]*baseStateProbabilities[3];
				resultStateProbabilities[2] =
					speedupArray2[1]*baseStateProbabilities[0]+
          speedupArray2[1]*baseStateProbabilities[1]+
          speedupArray2[2]*baseStateProbabilities[2]+
          speedupArray2[3]*baseStateProbabilities[3];
				resultStateProbabilities[3] =
					speedupArray3[1]*baseStateProbabilities[0]+
          speedupArray3[1]*baseStateProbabilities[1]+
          speedupArray3[2]*baseStateProbabilities[2]+
          speedupArray3[3]*baseStateProbabilities[3];
      }
    }
  private static final void calculateExtendedImpl( final double[][]
    transitionProbabilityStore, final PatternInfo centerPattern,
    final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    rightConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore resultStore ) {
    final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();
    int patternAccess = 0;
    final double[][] myPatternStateProbabilities = resultStore.
      getConditionalProbabilityAccess( numberOfPatterns, false )[0];
    final double[][] leftPatternStateProbabilities =
      leftConditionalProbabilityProbabilties.getCurrentConditionalProbabilities( 0 );
    final double[][] rightPatternStateProbabilities =
      rightConditionalProbabilityProbabilties.getCurrentConditionalProbabilities(
      0 );
    for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
      final int leftPattern = patternLookup[patternAccess++];
      final int rightPattern = patternLookup[patternAccess++];
      final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
      final double[] leftStateProbabilities = leftPatternStateProbabilities[
                                              leftPattern];
      final double[] rightStateProbabilities = rightPatternStateProbabilities[
                                               rightPattern];

      final double es0 = leftStateProbabilities[0]*rightStateProbabilities[0];
      final double es1 = leftStateProbabilities[1]*rightStateProbabilities[1];
      final double es2 = leftStateProbabilities[2]*rightStateProbabilities[2];
      final double es3 = leftStateProbabilities[3]*rightStateProbabilities[3];
      final double[] sa0 = transitionProbabilityStore[0];
      final double[] sa1 = transitionProbabilityStore[1];
      final double[] sa2 = transitionProbabilityStore[2];
      final double[] sa3 = transitionProbabilityStore[3];
      myStateProbabilities[0] = sa0[0]*es0+sa0[1]*es1+sa0[2]*es2+sa0[3]*es3;
      myStateProbabilities[1] = sa1[0]*es0+sa1[1]*es1+sa1[2]*es2+sa1[3]*es3;
      myStateProbabilities[2] = sa2[0]*es0+sa2[1]*es1+sa2[2]*es2+sa2[3]*es3;
      myStateProbabilities[3] = sa3[0]*es0+sa3[1]*es1+sa3[2]*es2+sa3[3]*es3;
    }
  }

  private static final void calculateFlatImpl( final PatternInfo centerPattern,
                                               final ConditionalProbabilityStore
    leftConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    rightConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore resultStore ) {
    int patternAccess = 0;
    final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();
    final double[][] myPatternStateProbabilities = resultStore.
      getConditionalProbabilityAccess( numberOfPatterns, false )[0];
    final double[][] leftPatternStateProbabilities =
      leftConditionalProbabilityProbabilties.getCurrentConditionalProbabilities( 0 );
    final double[][] rightPatternStateProbabilities =
      rightConditionalProbabilityProbabilties.getCurrentConditionalProbabilities(
      0 );
    for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
      final int leftPattern = patternLookup[patternAccess++];
      final int rightPattern = patternLookup[patternAccess++];
      final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
      final double[] leftStateProbabilities = leftPatternStateProbabilities[
                                              leftPattern];
      final double[] rightStateProbabilities = rightPatternStateProbabilities[
                                               rightPattern];

      myStateProbabilities[0] = leftStateProbabilities[0]*
                                rightStateProbabilities[0];
      myStateProbabilities[1] = leftStateProbabilities[1]*
                                rightStateProbabilities[1];
      myStateProbabilities[2] = leftStateProbabilities[2]*
                                rightStateProbabilities[2];
      myStateProbabilities[3] = leftStateProbabilities[3]*
                                rightStateProbabilities[3];
    }
  }

// -=-=-=-=-=-=-=-=-==-=-=--==--==--=

  private static final class InternalImpl implements Internal {
    private final ConditionalProbabilityStore myResultStore_;
    private final double[][] transitionProbabilityStore_;
    private double lastDistance_ = -1;


    private InternalImpl(Generator parentGenerator) {
     this.transitionProbabilityStore_ = new double[FOUR_STATES][FOUR_STATES];
     this.myResultStore_ = parentGenerator.createAppropriateConditionalProbabilityStore( false );
   }
	public final ConditionalProbabilityStore calculatePostExtendedFlat(
		final double distance, final SubstitutionModel model, final PatternInfo centerPattern,
		final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
		final ConditionalProbabilityStore
		rightConditionalProbabilityProbabilties,
		final boolean modelChangedSinceLastCall
		) {
			throw new RuntimeException("Not implemented yet!");
	}
    public final ConditionalProbabilityStore calculateExtended( final double
      distance, final SubstitutionModel model, final PatternInfo centerPattern,
      final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
      final ConditionalProbabilityStore
      rightConditionalProbabilityProbabilties,
      final boolean modelChangedSinceLastCall ) {
      if( modelChangedSinceLastCall||distance!=lastDistance_||lastDistance_<0 ) {
        model.getTransitionProbabilities( distance, 0,
                                          transitionProbabilityStore_ );
        lastDistance_ = distance;
      }

      calculateExtendedImpl( transitionProbabilityStore_, centerPattern,
                             leftConditionalProbabilityProbabilties,
                             rightConditionalProbabilityProbabilties,
                             myResultStore_ );
      return myResultStore_;
    }

		public ConditionalProbabilityStore calculateSingleExtended( double distance, SubstitutionModel model, PatternInfo centerPattern, final ConditionalProbabilityStore baseConditionalProbabilities,
      boolean modelChangedSinceLastCall ) {
		  calculateSingleExtendedIndirectImpl(distance,model,centerPattern.getNumberOfPatterns(),baseConditionalProbabilities,myResultStore_,transitionProbabilityStore_);
		  return myResultStore_;
		}

    public final ConditionalProbabilityStore calculateFlat( final PatternInfo centerPattern,
      final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
      final ConditionalProbabilityStore rightConditionalProbabilityProbabilties 	) {
      calculateFlatImpl( centerPattern,
                         leftConditionalProbabilityProbabilties,
                         rightConditionalProbabilityProbabilties, myResultStore_ );
      return myResultStore_;
    }

  } //End of class InternalImpl

//-=-=-=-=-==--=-=-=-=-=

  private static final class ExternalImpl extends AbstractExternal implements External {
    private final double[][] transitionProbabilityStore_;

    private ExternalImpl() {
      this.transitionProbabilityStore_ = new double[FOUR_STATES][FOUR_STATES];
    }

    private final double[][] getResultStoreValues(double distance,
      SubstitutionModel model,
      PatternInfo centerPattern,
      ConditionalProbabilityStore leftFlatConditionalProbabilities,
      ConditionalProbabilityStore rightFlatConditionalProbabilities,
      ConditionalProbabilityStore tempStore
      ) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();

      model.getTransitionProbabilities( distance, 0, transitionProbabilityStore_ );
      double[][][] resultStoreValues = tempStore.getConditionalProbabilityAccess(
        numberOfPatterns, false );
      int patternAccess = 0;
      final double[][] myPatternStateProbabilities = resultStoreValues[0];
      final double[][] leftPatternStateProbabilities = leftFlatConditionalProbabilities.getCurrentConditionalProbabilities( 0 );
      final double[][] rightPatternStateProbabilities = rightFlatConditionalProbabilities.getCurrentConditionalProbabilities( 0 );
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final int leftPattern = patternLookup[patternAccess++];
        final int rightPattern = patternLookup[patternAccess++];
        final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
        final double[] leftStateProbabilities = leftPatternStateProbabilities[leftPattern];
        final double[] rightStateProbabilities = rightPatternStateProbabilities[rightPattern];
        final double[] sa0 = transitionProbabilityStore_[0];	final double[] sa1 = transitionProbabilityStore_[1];
        final double[] sa2 = transitionProbabilityStore_[2]; final double[] sa3 = transitionProbabilityStore_[3];
          myStateProbabilities[0] =
            ( sa0[0]*leftStateProbabilities[0]+
              sa0[1]*leftStateProbabilities[1]+
              sa0[2]*leftStateProbabilities[2]+
              sa0[3]*leftStateProbabilities[3]
              )*rightStateProbabilities[0];
          myStateProbabilities[1] =
            ( sa1[0]*leftStateProbabilities[0]+
              sa1[1]*leftStateProbabilities[1]+
              sa1[2]*leftStateProbabilities[2]+
              sa1[3]*leftStateProbabilities[3]
              )*rightStateProbabilities[1];
          myStateProbabilities[2] =
            ( sa2[0]*leftStateProbabilities[0]+
              sa2[1]*leftStateProbabilities[1]+
              sa2[2]*leftStateProbabilities[2]+
              sa2[3]*leftStateProbabilities[3]
              )*rightStateProbabilities[2];
          myStateProbabilities[3] =
            ( sa3[0]*leftStateProbabilities[0]+
              sa3[1]*leftStateProbabilities[1]+
              sa3[2]*leftStateProbabilities[2]+
              sa3[3]*leftStateProbabilities[3]
              )*rightStateProbabilities[3];
      }
      return resultStoreValues[0];
    }
		public double calculateLogLikelihoodSingle( SubstitutionModel model, int[] patternWeights, int numberOfPatterns,
                                       ConditionalProbabilityStore conditionalProbabilityStore) {
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
			double logLikelihood = 0;
      double[][] conditionalProbabilities =
        conditionalProbabilityStore.getCurrentConditionalProbabilities()[0];
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final double[] baseStates = conditionalProbabilities[pattern];
        double total =
						equilibriumFrequencies[0]*baseStates[0] +
						equilibriumFrequencies[1]*baseStates[1] +
						equilibriumFrequencies[2]*baseStates[2] +
						equilibriumFrequencies[3]*baseStates[3];
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

    public void calculateSingleExtendedDirect(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore conditionalProbabilities
                                  ) {
			model.getTransitionProbabilities( distance, 0, transitionProbabilityStore_ );

      double[][][] baseStoreValues = conditionalProbabilities.getCurrentConditionalProbabilities();
			final double[][] basePatternStateProbabilities = baseStoreValues[0];
//      final double[][] transProb = transitionProbabilityStore_[category];
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final double[] baseStateProbabilities = basePatternStateProbabilities[pattern];
        final double[] speedupArray0 = transitionProbabilityStore_[0];
        final double[] speedupArray1 = transitionProbabilityStore_[1];
        final double[] speedupArray2 = transitionProbabilityStore_[2];
        final double[] speedupArray3 = transitionProbabilityStore_[3];
        double probTotal0 =
					speedupArray0[1]*baseStateProbabilities[0]+
          speedupArray0[1]*baseStateProbabilities[1]+
          speedupArray0[2]*baseStateProbabilities[2]+
          speedupArray0[3]*baseStateProbabilities[3];
				double probTotal1 =
					speedupArray1[1]*baseStateProbabilities[0]+
          speedupArray1[1]*baseStateProbabilities[1]+
          speedupArray1[2]*baseStateProbabilities[2]+
          speedupArray1[3]*baseStateProbabilities[3];
				double probTotal2 =
					speedupArray2[1]*baseStateProbabilities[0]+
          speedupArray2[1]*baseStateProbabilities[1]+
          speedupArray2[2]*baseStateProbabilities[2]+
          speedupArray2[3]*baseStateProbabilities[3];
				double probTotal3 =
					speedupArray3[1]*baseStateProbabilities[0]+
          speedupArray3[1]*baseStateProbabilities[1]+
          speedupArray3[2]*baseStateProbabilities[2]+
          speedupArray3[3]*baseStateProbabilities[3];
				baseStateProbabilities[0] = probTotal0;
				baseStateProbabilities[1] = probTotal1;
				baseStateProbabilities[2] = probTotal2;
				baseStateProbabilities[3] = probTotal3;

      }

    }

    public void calculateSingleExtendedIndirect(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore baseConditionalProbabilities,
                                    ConditionalProbabilityStore resultConditionalProbabilities
                                  ) {
			calculateSingleExtendedIndirectImpl(distance,model,numberOfPatterns,baseConditionalProbabilities,resultConditionalProbabilities,transitionProbabilityStore_);
    }

    public final void calculateExtended( final double distance,
                                         final SubstitutionModel model,
                                         final PatternInfo centerPattern,
                                         final ConditionalProbabilityStore
                                         leftConditionalProbabilityProbabilties,
                                         final ConditionalProbabilityStore
                                         rightConditionalProbabilityProbabilties,
                                         final ConditionalProbabilityStore
                                         resultStore ) {
      model.getTransitionProbabilities( distance, 0,
                                        transitionProbabilityStore_ );

      calculateExtendedImpl( transitionProbabilityStore_, centerPattern,
                             leftConditionalProbabilityProbabilties,
                             rightConditionalProbabilityProbabilties,
                             resultStore );
    }

    public final void calculateFlat( final PatternInfo centerPattern,
                                     final ConditionalProbabilityStore
                                     leftConditionalProbabilityProbabilties,
                                     final ConditionalProbabilityStore
                                     rightConditionalProbabilityProbabilties,
                                     final ConditionalProbabilityStore
                                     resultStore ) {
      calculateFlatImpl( centerPattern,
                         leftConditionalProbabilityProbabilties,
                         rightConditionalProbabilityProbabilties, resultStore ); ;
    }

    public double calculateLogLikelihood( final double distance, final SubstitutionModel model,
                                       final PatternInfo centerPattern,
                                       final ConditionalProbabilityStore
      leftFlatConditionalProbabilities,
      final ConditionalProbabilityStore
      rightFlatConditionalProbabilities ,
      final ConditionalProbabilityStore tempStore) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();

      final double[][] myPatternStateProbabilities =
        getResultStoreValues(distance,model,centerPattern,leftFlatConditionalProbabilities,rightFlatConditionalProbabilities,tempStore);
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
      double logLikelihood = 0;

      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final double[] states = myPatternStateProbabilities[pattern];
        final double total = equilibriumFrequencies[0]*states[0]+
                             equilibriumFrequencies[1]*states[1]+
                             equilibriumFrequencies[2]*states[2]+
                             equilibriumFrequencies[3]*states[3];
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }
    protected final void calculateCategoryPatternProbabilities(  SubstitutionModel model,
                                 PatternInfo centerPattern,
                                 ConditionalProbabilityStore	leftConditionalProbabilities,
                                 ConditionalProbabilityStore	rightConditionalProbabilities,
                                 double[][] categoryPatternLogLikelihoodStore
                                ) {
        final int[] patternLookup = centerPattern.getPatternLookup();
        final int numberOfPatterns = centerPattern.getNumberOfPatterns();

        final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
        final double[] probabilities = model.getTransitionCategoryProbabilities();
        int patternIndex = 0;
        final double[][] leftValues = leftConditionalProbabilities.
                                getCurrentConditionalProbabilities( 0 );
        final double[][] rightValues = rightConditionalProbabilities.
                                 getCurrentConditionalProbabilities( 0 );
        final double[] patternLogLikelihoods = categoryPatternLogLikelihoodStore[0];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] left = leftValues[patternLookup[patternIndex++]];
          final double[] right = rightValues[patternLookup[patternIndex++]];
          double total =
            equilibriumFrequencies[0]*( left[0]*right[0] )+
            equilibriumFrequencies[1]*( left[1]*right[1] )+
            equilibriumFrequencies[2]*( left[2]*right[2] )+
            equilibriumFrequencies[3]*( left[3]*right[3] );
          patternLogLikelihoods[pattern] = total ;
        }
    }

    protected final void calculateCategoryPatternProbabilities( double distance, SubstitutionModel model,
                          PatternInfo centerPattern,
                          ConditionalProbabilityStore	leftFlatConditionalProbabilities,
                          ConditionalProbabilityStore	rightFlatConditionalProbabilities,
                          ConditionalProbabilityStore  tempStore,
                          double[][] categoryPatternLogLikelihoodStore
		                     ) {

      final double[][] myPatternStateProbabilities =
        getResultStoreValues(distance,model,centerPattern,leftFlatConditionalProbabilities,rightFlatConditionalProbabilities,tempStore);
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final double[] patternLogLikelihoodStore = categoryPatternLogLikelihoodStore[0];

			for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
				final double[] states = myPatternStateProbabilities[pattern];
				final double total = equilibriumFrequencies[0]*states[0]+
														 equilibriumFrequencies[1]*states[1]+
														 equilibriumFrequencies[2]*states[2]+
														 equilibriumFrequencies[3]*states[3];
				patternLogLikelihoodStore[pattern] = total;
			}
    }

    public double calculateLogLikelihood( final SubstitutionModel model,
                                       final PatternInfo centerPattern,
                                       final ConditionalProbabilityStore
                                       leftConditionalProbabilities,
                                       final ConditionalProbabilityStore
                                       rightConditionalProbabilities ) {

      final int[] patternWeights = centerPattern.getPatternWeights();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();

      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
      double logLikelihood = 0;
      int patternIndex = 0;
      double[][] leftValues = leftConditionalProbabilities.
                              getCurrentConditionalProbabilities( 0 );
      double[][] rightValues = rightConditionalProbabilities.
                               getCurrentConditionalProbabilities( 0 );

      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final double[] left = leftValues[patternLookup[patternIndex++]];
        final double[] right = rightValues[patternLookup[patternIndex++]];
        double total =
          equilibriumFrequencies[0]*( left[0]*right[0] )+
          equilibriumFrequencies[1]*( left[1]*right[1] )+
          equilibriumFrequencies[2]*( left[2]*right[2] )+
          equilibriumFrequencies[3]*( left[3]*right[3] );
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

  } //End of class InternalImpl

// =--==--=-=-==-=--==--=-==-=-=-=-=--==-=--=
  /**
   *
   * @param fallbackFactory A LHCalculator.Factory that can be used if the number of states is not four
   * @return
   */
  public static final Factory getFactory( Factory fallbackFactory ) {
    return new SimpleFactory( fallbackFactory );
  }

  /**
   *
   * @return
   */
  public static final Factory getFactory() {
    return new SimpleFactory( FastFourStateLHCalculator.getFactory() );
  }

// -=-=--==-=-=-=---=-==-=--==-=-=-=-
  private static final class SimpleFactory implements Factory {
    private final Factory fallbackFactory_;
    public SimpleFactory( Factory fallbackFactory ) {
      this.fallbackFactory_ = fallbackFactory;
    }

    public Generator createSeries( int numberOfCategories, DataType dt ) {
      if( dt.getNumStates()==4&&numberOfCategories==1 ) {
        return new SimpleGenerator();
      }
      return fallbackFactory_.createSeries( numberOfCategories,dt );
    }
  }

  // -=-=--==-=-=-=---=-==-=--==-=-=-=-

  private static final class SimpleGenerator implements Generator {
    public SimpleGenerator() { }
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns) {
		  return new SimpleLeafCalculator(patternStateMatchup,numberOfPatterns, 4, 1, this);
		}
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns, Generator parentGenerator ) {
		  return new SimpleLeafCalculator(patternStateMatchup,numberOfPatterns, 4, 1, parentGenerator);
		}
    public LHCalculator.Internal createNewInternal() {
      return new InternalImpl(this);
    }

    public LHCalculator.External createNewExternal() {
      return new ExternalImpl();
    }
    public LHCalculator.External createNewExternal(Generator parentGenerator) throws IllegalArgumentException {
      return new ExternalImpl();
    }

    public LHCalculator.Internal createNewInternal(Generator parentGenerator) throws IllegalArgumentException{
      return new InternalImpl( parentGenerator );
    }

    public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore( boolean isForLeaf ) {
      return new ConditionalProbabilityStore( 1, 4 );
    }
		public boolean isAllowCaching() { return true; }

  }

}