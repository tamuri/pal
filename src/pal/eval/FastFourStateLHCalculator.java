// FastFourStateLHCalculator.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: FastFourStateLHCalculator</p>
 * <p>Description:  A simpler LHCalculator with unrolled loops for four state data types (eg Nucleotides) </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.datatype.*;
import pal.substmodel.*;

public final class FastFourStateLHCalculator implements LHCalculator {
  private final static int FOUR_STATES = 4;

	private final static void calculateSingleExtendedIndirectImpl(
			  double distance, SubstitutionModel model,
				int numberOfPatterns,
        ConditionalProbabilityStore baseConditionalProbabilities,
        ConditionalProbabilityStore resultConditionalProbabilities,
				double[][][] transitionProbabilityStore,
				int numberOfCategories
      ) {

			model.getTransitionProbabilities( distance, transitionProbabilityStore );

      double[][][] baseStoreValues = baseConditionalProbabilities.getCurrentConditionalProbabilities();
			double[][][] resultStoreValues = baseConditionalProbabilities.getConditionalProbabilityAccess(numberOfPatterns,false);
			for( int category = 0; category<numberOfCategories; category++ ) {
        final double[][] basePatternStateProbabilities = baseStoreValues[category];
        final double[][] resultPatternStateProbabilities = resultStoreValues[category];
        final double[][] transProb = transitionProbabilityStore[category];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] baseStateProbabilities = basePatternStateProbabilities[pattern];
          final double[] resultStateProbabilities = resultPatternStateProbabilities[pattern];
          final double[] speedupArray0 = transProb[0];
          final double[] speedupArray1 = transProb[1];
          final double[] speedupArray2 = transProb[2];
          final double[] speedupArray3 = transProb[3];
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
    }

  private static final void calculateFlatImpl( PatternInfo centerPattern,
                                               final ConditionalProbabilityStore
                                               leftConditionalProbabilityProbabilties,
                                               final ConditionalProbabilityStore
                                               rightConditionalProbabilityProbabilties,
                                               final ConditionalProbabilityStore resultStore,
                                               final int numberOfCategories ) {
    final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();
    final double[][][] resultStoreValues = resultStore.
                                           getConditionalProbabilityAccess(
      numberOfPatterns, false );
    for( int category = 0; category<numberOfCategories; category++ ) {
      int patternAccess = 0;
      final double[][] myPatternStateProbabilities = resultStoreValues[category];
      final double[][] leftPatternStateProbabilities =
        leftConditionalProbabilityProbabilties.getCurrentConditionalProbabilities(
        category );
      final double[][] rightPatternStateProbabilities =
        rightConditionalProbabilityProbabilties.getCurrentConditionalProbabilities(
        category );
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final int leftPattern = patternLookup[patternAccess++];
        final int rightPattern = patternLookup[patternAccess++];
        final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
        final double[] leftStateProbabilities = leftPatternStateProbabilities[leftPattern];
        final double[] rightStateProbabilities = rightPatternStateProbabilities[rightPattern];
				myStateProbabilities[0] = leftStateProbabilities[0]*rightStateProbabilities[0];
        myStateProbabilities[1] = leftStateProbabilities[1]*rightStateProbabilities[1];
        myStateProbabilities[2] = leftStateProbabilities[2]*
                                  rightStateProbabilities[2];
        myStateProbabilities[3] = leftStateProbabilities[3]*
                                  rightStateProbabilities[3];

      }
    }
  }

  private static final void calculateExtendedImpl( double[][][]
    transitionProbabilityStore,
    PatternInfo centerPattern,
    final ConditionalProbabilityStore
    leftConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    rightConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    resultStore,
    int numberOfCategories ) {
    final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();
    double[][][] resultStoreValues = resultStore.getConditionalProbabilityAccess(
      numberOfPatterns, false );
    for( int category = 0; category<numberOfCategories; category++ ) {
      int patternAccess = 0;
      final double[][] myPatternStateProbabilities = resultStoreValues[category];
      final double[][] leftPatternStateProbabilities =
        leftConditionalProbabilityProbabilties.getCurrentConditionalProbabilities(
        category );
      final double[][] rightPatternStateProbabilities =
        rightConditionalProbabilityProbabilties.getCurrentConditionalProbabilities(
        category );
      final double[][] transProb = transitionProbabilityStore[category];
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final int leftPattern = patternLookup[patternAccess++];
        final int rightPattern = patternLookup[patternAccess++];
        final double[] myStateProbabilities = myPatternStateProbabilities[
                                              pattern];
        final double[] leftStateProbabilities = leftPatternStateProbabilities[
                                                leftPattern];
        final double[] rightStateProbabilities = rightPatternStateProbabilities[
                                                 rightPattern];

        final double es0 = leftStateProbabilities[0]*rightStateProbabilities[0];
        final double es1 = leftStateProbabilities[1]*rightStateProbabilities[1];
        final double es2 = leftStateProbabilities[2]*rightStateProbabilities[2];
        final double es3 = leftStateProbabilities[3]*rightStateProbabilities[3];
        final double[] sa0 = transProb[0];
        final double[] sa1 = transProb[1];
        final double[] sa2 = transProb[2];
        final double[] sa3 = transProb[3];
        myStateProbabilities[0] = sa0[0]*es0+sa0[1]*es1+sa0[2]*es2+sa0[3]*es3;
        myStateProbabilities[1] = sa1[0]*es0+sa1[1]*es1+sa1[2]*es2+sa1[3]*es3;
        myStateProbabilities[2] = sa2[0]*es0+sa2[1]*es1+sa2[2]*es2+sa2[3]*es3;
        myStateProbabilities[3] = sa3[0]*es0+sa3[1]*es1+sa3[2]*es2+sa3[3]*es3;
      }
    }
  }

  // Class Internal Impl
  private final static class InternalImpl implements Internal {
    private final int numberOfCategories_;
    private final ConditionalProbabilityStore myResultStore_;
    private final double[][][] transitionProbabilityStore_;
    private final double[] endStateProbabilityStore_;
    private double lastDistance_ = -1;

    private InternalImpl( int numberOfCategories,
                          double[] endStateProbabilityStore,
                          Generator parentGenerator ) {
      this.numberOfCategories_ = numberOfCategories;
      this.endStateProbabilityStore_ = endStateProbabilityStore;
      this.transitionProbabilityStore_ = new double[numberOfCategories][
                                         FOUR_STATES][FOUR_STATES];
      this.myResultStore_ = parentGenerator.createAppropriateConditionalProbabilityStore( false );
    }
		public final ConditionalProbabilityStore calculateSingleExtended( final double
      distance, final SubstitutionModel model, PatternInfo centerPattern,
      final ConditionalProbabilityStore baseConditionalProbabilityProbabilties,
      final boolean modelChangedSinceLastCall  ) {
			calculateSingleExtendedIndirectImpl(distance,model,centerPattern.getNumberOfPatterns(),baseConditionalProbabilityProbabilties,myResultStore_,transitionProbabilityStore_,numberOfCategories_);
			return myResultStore_;
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
      distance, final SubstitutionModel model, PatternInfo centerPattern,
      final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
      final ConditionalProbabilityStore
      rightConditionalProbabilityProbabilties,
      final boolean modelChangedSinceLastCall  ) {
      if( modelChangedSinceLastCall||distance!=lastDistance_||lastDistance_<0 ) {
        model.getTransitionProbabilities( distance, transitionProbabilityStore_ );
        lastDistance_ = distance;
      }
      calculateExtendedImpl( transitionProbabilityStore_, centerPattern,
                             leftConditionalProbabilityProbabilties,
                             rightConditionalProbabilityProbabilties,

                             myResultStore_,
                             numberOfCategories_ );
      return myResultStore_;
    }

    public final ConditionalProbabilityStore calculateFlat(
				final PatternInfo centerPattern,
        final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore  rightConditionalProbabilityProbabilties ) {
      calculateFlatImpl( centerPattern,
                         leftConditionalProbabilityProbabilties,
                         rightConditionalProbabilityProbabilties, myResultStore_,
                         numberOfCategories_ );
      return myResultStore_;
    }

  }

// -=-=-=-=-=-=-=-=-=-=
  private final static class ExternalImpl extends AbstractExternal implements External {
    private final int numberOfCategories_;
    private final double[][][] transitionProbabilityStore_;
    private final double[] endStateProbabilityStore_;

    private ExternalImpl( int numberOfCategories,
                          double[] endStateProbabilityStore ) {
      this.numberOfCategories_ = numberOfCategories;
      this.endStateProbabilityStore_ = endStateProbabilityStore;
      this.transitionProbabilityStore_ = new double[numberOfCategories][FOUR_STATES][FOUR_STATES];
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
      model.getTransitionProbabilities( distance, transitionProbabilityStore_ );
      calculateExtendedImpl( transitionProbabilityStore_, centerPattern,
                             leftConditionalProbabilityProbabilties,
                             rightConditionalProbabilityProbabilties,
                             resultStore, numberOfCategories_ );
    }

    public void calculateSingleExtendedDirect(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore conditionalProbabilities
                                  ) {
			model.getTransitionProbabilities( distance, transitionProbabilityStore_ );

      double[][][] baseStoreValues = conditionalProbabilities.getCurrentConditionalProbabilities();
			for( int category = 0; category<numberOfCategories_; category++ ) {
        final double[][] basePatternStateProbabilities = baseStoreValues[category];
        final double[][] transProb = transitionProbabilityStore_[category];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] baseStateProbabilities = basePatternStateProbabilities[pattern];
          final double[] speedupArray0 = transProb[0];
          final double[] speedupArray1 = transProb[1];
          final double[] speedupArray2 = transProb[2];
          final double[] speedupArray3 = transProb[3];
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
    }
		public double calculateLogLikelihoodSingle(
		  SubstitutionModel model, int[] patternWeights, int numberOfPatterns,
      ConditionalProbabilityStore conditionalProbabilityStore
		) {
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
			double logLikelihood = 0;
      double[][][] conditionalProbabilities =
        conditionalProbabilityStore.getCurrentConditionalProbabilities();
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        double total = 0;
				for( int cat = 0; cat<numberOfCategories_; cat++ ) {
          final double[] baseStates = conditionalProbabilities[cat][pattern];
          total += probabilities[cat]*
					  (
						  equilibriumFrequencies[0]*baseStates[0] +
						  equilibriumFrequencies[1]*baseStates[1] +
						  equilibriumFrequencies[2]*baseStates[2] +
						  equilibriumFrequencies[3]*baseStates[3]
						);
        }
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

    public void calculateSingleExtendedIndirect(
			  double distance, SubstitutionModel model,
				int numberOfPatterns,
        ConditionalProbabilityStore baseConditionalProbabilities,
        ConditionalProbabilityStore resultConditionalProbabilities
      ) {
		  calculateSingleExtendedIndirectImpl(distance,model,numberOfPatterns,baseConditionalProbabilities,resultConditionalProbabilities,transitionProbabilityStore_,numberOfCategories_);
    }

    public final void calculateFlat( final PatternInfo centerPattern,
                                     final ConditionalProbabilityStore
                                     leftConditionalProbabilityProbabilties,
                                     final ConditionalProbabilityStore
                                     rightConditionalProbabilityProbabilties,
                                     final ConditionalProbabilityStore
                                     resultStore ) {

      calculateFlatImpl( centerPattern, leftConditionalProbabilityProbabilties,
                         rightConditionalProbabilityProbabilties, resultStore,
                         numberOfCategories_ );
    }

    private final double[][][] getResultStoreValues(double distance,
      SubstitutionModel model,
      PatternInfo centerPattern,
      ConditionalProbabilityStore leftFlatConditionalProbabilities,
      ConditionalProbabilityStore rightFlatConditionalProbabilities,
      ConditionalProbabilityStore tempStore
      ) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();

      model.getTransitionProbabilities( distance, transitionProbabilityStore_ );
      double[][][] resultStoreValues = tempStore.getConditionalProbabilityAccess(
        numberOfPatterns, false );
      for( int category = 0; category<numberOfCategories_; category++ ) {
        int patternAccess = 0;
        final double[][] myPatternStateProbabilities = resultStoreValues[
          category];
        final double[][] leftPatternStateProbabilities = leftFlatConditionalProbabilities.getCurrentConditionalProbabilities( category );
        final double[][] rightPatternStateProbabilities = rightFlatConditionalProbabilities.getCurrentConditionalProbabilities( category );
        final double[][] transProb = transitionProbabilityStore_[category];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final int leftPattern = patternLookup[patternAccess++];
          final int rightPattern = patternLookup[patternAccess++];
          final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
          final double[] leftStateProbabilities = leftPatternStateProbabilities[leftPattern];
          final double[] rightStateProbabilities = rightPatternStateProbabilities[rightPattern];
          final double[] sa0 = transProb[0];
          final double[] sa1 = transProb[1];
          final double[] sa2 = transProb[2];
          final double[] sa3 = transProb[3];
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
      }
      return resultStoreValues;
    }
    protected final void calculateCategoryPatternProbabilities( double distance,
      SubstitutionModel model,
      PatternInfo centerPattern,
      ConditionalProbabilityStore leftFlatConditionalProbabilities,
      ConditionalProbabilityStore rightFlatConditionalProbabilities,
      ConditionalProbabilityStore tempStore,
      double[][] categoryPatternLogLikelihoodStore
      ) {
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final int[] patternWeights = centerPattern.getPatternWeights();
      double[][][] resultStoreValues = getResultStoreValues(distance,model,centerPattern,leftFlatConditionalProbabilities,rightFlatConditionalProbabilities,tempStore);
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      for( int cat = 0; cat<numberOfCategories_; cat++ ) {
        double total = 0;
        final double[] patternLogLikelihoodStore = categoryPatternLogLikelihoodStore[cat];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] states = resultStoreValues[cat][pattern];
          double prob = equilibriumFrequencies[0]*states[0]+
                        equilibriumFrequencies[1]*states[1]+
                        equilibriumFrequencies[2]*states[2]+
                        equilibriumFrequencies[3]*states[3];
          patternLogLikelihoodStore[pattern] =prob ;
        }
      }
    }

    public final double calculateLogLikelihood( double distance,
                                             SubstitutionModel model,
                                             PatternInfo centerPattern,
                                             ConditionalProbabilityStore
                                             leftFlatConditionalProbabilities,
                                             ConditionalProbabilityStore
                                             rightFlatConditionalProbabilities,
                                             ConditionalProbabilityStore tempStore
                                             ) {
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final int[] patternWeights = centerPattern.getPatternWeights();
      double[][][] resultStoreValues = getResultStoreValues(distance,model,centerPattern,leftFlatConditionalProbabilities,rightFlatConditionalProbabilities,tempStore);

      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
      double logLikelihood = 0;
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        double total = 0;
        for( int cat = 0; cat<numberOfCategories_; cat++ ) {
          final double[] states = resultStoreValues[
                                  cat][pattern];
          double prob = equilibriumFrequencies[0]*states[0]+
                        equilibriumFrequencies[1]*states[1]+
                        equilibriumFrequencies[2]*states[2]+
                        equilibriumFrequencies[3]*states[3];
          total += probabilities[cat]*prob;
        }
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

    protected final void calculateCategoryPatternProbabilities( SubstitutionModel model,
      PatternInfo centerPattern,
      ConditionalProbabilityStore leftConditionalProbabilities,
      ConditionalProbabilityStore rightConditionalProbabilities,
      double[][] categoryPatternLikelihoodStore
      ) {
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int[] patternWeights = centerPattern.getPatternWeights();
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();

      double[][][] leftValues = leftConditionalProbabilities.
                                getCurrentConditionalProbabilities();
      double[][][] rightValues = rightConditionalProbabilities.
                                 getCurrentConditionalProbabilities();
      for( int cat = 0; cat<numberOfCategories_; cat++ ) {
        double total = 0;
        final double[][] leftPattern = leftValues[cat];
        final double[][] rightPattern = rightValues[cat];
        final double[] patternLikelihoodStore = categoryPatternLikelihoodStore[cat];
        int patternIndex = 0;
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] left = leftPattern[patternLookup[patternIndex++]];
          final double[] right = rightPattern[patternLookup[patternIndex++]];
          double prob =
            equilibriumFrequencies[0]*( left[0]*right[0] )+
            equilibriumFrequencies[1]*( left[1]*right[1] )+
            equilibriumFrequencies[2]*( left[2]*right[2] )+
            equilibriumFrequencies[3]*( left[3]*right[3] );
          patternLikelihoodStore[pattern] = ( prob);
        }
      }
    }

    public double calculateLogLikelihood( SubstitutionModel model,
                                       PatternInfo centerPattern,
                                       ConditionalProbabilityStore
                                       leftConditionalProbabilities,
                                       ConditionalProbabilityStore
                                       rightConditionalProbabilities ) {
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int[] patternWeights = centerPattern.getPatternWeights();
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
      double logLikelihood = 0;
      int patternIndex = 0;
      double[][][] leftValues = leftConditionalProbabilities.
                                getCurrentConditionalProbabilities();
      double[][][] rightValues = rightConditionalProbabilities.
                                 getCurrentConditionalProbabilities();

      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        double total = 0;
        final int leftIndex = patternLookup[patternIndex++];
        final int rightIndex= patternLookup[patternIndex++];
        for( int cat = 0; cat<numberOfCategories_; cat++ ) {
          final double[] left = leftValues[cat][leftIndex];
          final double[] right = rightValues[cat][rightIndex];
          double prob =
            equilibriumFrequencies[0]*( left[0]*right[0] )+
            equilibriumFrequencies[1]*( left[1]*right[1] )+
            equilibriumFrequencies[2]*( left[2]*right[2] )+
            equilibriumFrequencies[3]*( left[3]*right[3] );
          total += probabilities[cat]*prob;
        }
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

  }

// -=-=-=-=-=-=-=-=-=-=

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
    return new SimpleFactory( SimpleLHCalculator.getFactory() );
  }

// -=-=--==-=-=-=---=-==-=--==-=-=-=-
  private static final class SimpleFactory implements Factory {
    private final Factory fallbackFactory_;
    public SimpleFactory( Factory fallbackFactory ) {
      this.fallbackFactory_ = fallbackFactory;
    }

    public Generator createSeries( int numberOfCategories, DataType dt ) {
      if( dt.getNumStates()==4 ) {
        return new SimpleGenerator( numberOfCategories );
      }
      return fallbackFactory_.createSeries( numberOfCategories, dt );
    }
  }

  // -=-=--==-=-=-=---=-==-=--==-=-=-=-

  private static final class SimpleGenerator implements Generator {
    private final int numberOfCategories_;
    private final double[] endStateProbabilityStore_ = new double[4];
		public SimpleGenerator( int numberOfCategories ) {
      this.numberOfCategories_ = numberOfCategories;
    }
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns) {
		  return new SimpleLeafCalculator(patternStateMatchup,numberOfPatterns, 4, numberOfCategories_, this);
		}
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns, Generator parentGenerator ) {
		  return new SimpleLeafCalculator(patternStateMatchup,numberOfPatterns, 4, numberOfCategories_, parentGenerator);
		}

    public LHCalculator.Internal createNewInternal() {
      return new InternalImpl( numberOfCategories_,
                               endStateProbabilityStore_, this );
    }

    public LHCalculator.External createNewExternal() {
      return new ExternalImpl( numberOfCategories_,
                               endStateProbabilityStore_ );
    }

    public LHCalculator.External createNewExternal( Generator parentGenerator ) throws
      IllegalArgumentException {
      return new ExternalImpl( numberOfCategories_,
                               endStateProbabilityStore_ );

    }

    public LHCalculator.Internal createNewInternal( Generator parentGenerator ) throws
      IllegalArgumentException {
      return new InternalImpl( numberOfCategories_,
                               endStateProbabilityStore_, parentGenerator );
    }

    public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore(  boolean isForLeaf ) {
      return new ConditionalProbabilityStore( numberOfCategories_, 4 );
    }
		public boolean isAllowCaching() { return true; }

  }

}