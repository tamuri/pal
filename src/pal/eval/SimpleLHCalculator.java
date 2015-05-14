// SimpleLHCalculator.java
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

public class SimpleLHCalculator implements LHCalculator {
  private static final SimpleFactory FACTORY_INSTANCE = new SimpleFactory();



	private final static void calculateSingleExtendedIndirectImpl(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore baseConditionalProbabilities,
                                    ConditionalProbabilityStore resultConditionalProbabilities,
																		double[][][] transitionProbabilityStore,
																		int numberOfCategories,
																		int numberOfStates
                                  ) {
			model.getTransitionProbabilities( distance, transitionProbabilityStore );

      double[][][] resultStoreValues = resultConditionalProbabilities.getConditionalProbabilityAccess( numberOfPatterns, false );
      double[][][] baseStoreValues = baseConditionalProbabilities.getCurrentConditionalProbabilities();
			for( int category = 0; category<numberOfCategories; category++ ) {
        int patternAccess = 0;
        final double[][] resultPatternStateProbabilities = resultStoreValues[category];
        final double[][] basePatternStateProbabilities = baseStoreValues[category];
        final double[][] transProb = transitionProbabilityStore[category];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] resultStateProbabilities = resultPatternStateProbabilities[pattern];
          final double[] baseStateProbabilities = basePatternStateProbabilities[pattern];

          for( int startState = 0; startState<numberOfStates; startState++ ) {
            double probTotal = 0;
            final double[] speedupArray = transProb[startState];
            for( int endState = 0; endState<numberOfStates; endState++ ) {
              probTotal += speedupArray[endState]*baseStateProbabilities[endState];
            }
            resultStateProbabilities[startState] = probTotal;
          }
        }
      }
   	}

  private static final void calculateFlatImpl( final PatternInfo centerPattern,
                                               final ConditionalProbabilityStore
                                               leftConditionalProbabilityProbabilties,
                                               final ConditionalProbabilityStore
                                               rightConditionalProbabilityProbabilties,
                                               final ConditionalProbabilityStore
                                               resultStore, int numberOfCategories, int numberOfStates ) {
    final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();
    double[][][] resultStoreValues = resultStore.
                                     getConditionalProbabilityAccess(
      numberOfPatterns, false );

    for( int category = 0; category<numberOfCategories; category++ ) {
      int patternAccess = 0;
      final double[][] myPatternStateProbabilities = resultStoreValues[
        category];
      final double[][] leftPatternStateProbabilities =
        leftConditionalProbabilityProbabilties.
        getCurrentConditionalProbabilities( category );
      final double[][] rightPatternStateProbabilities =
        rightConditionalProbabilityProbabilties.
        getCurrentConditionalProbabilities( category );
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final int leftPattern = patternLookup[patternAccess++];
        final int rightPattern = patternLookup[patternAccess++];
        final double[] myStateProbabilities = myPatternStateProbabilities[
                                              pattern];
        final double[] leftStateProbabilities = leftPatternStateProbabilities[
                                                leftPattern];
        final double[] rightStateProbabilities =
          rightPatternStateProbabilities[rightPattern];

        for( int endState = 0; endState<numberOfStates; endState++ ) {
          myStateProbabilities[endState] = leftStateProbabilities[endState]*
                                           rightStateProbabilities[endState];
        }
      }
    }
  }

  private static final void calculateExtendedImpl( final double[][][] transitionProbabilityStore,
    final PatternInfo centerPattern,
    final ConditionalProbabilityStore
    leftConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    rightConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    resultStore,
    int numberOfCategories,
    int numberOfStates,
    double[] endStateProbabilityStore ) {
    final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();

    double[][][] resultStoreValues = resultStore.
                                     getConditionalProbabilityAccess(
      numberOfPatterns, false );
    for( int category = 0; category<numberOfCategories; category++ ) {
      int patternAccess = 0;
      final double[][] myPatternStateProbabilities = resultStoreValues[
        category];
      final double[][] leftPatternStateProbabilities =
        leftConditionalProbabilityProbabilties.
        getCurrentConditionalProbabilities( category );
      final double[][] rightPatternStateProbabilities =
        rightConditionalProbabilityProbabilties.
        getCurrentConditionalProbabilities( category );
      final double[][] transProb = transitionProbabilityStore[category];
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final int leftPattern = patternLookup[patternAccess++];
        final int rightPattern = patternLookup[patternAccess++];
        final double[] myStateProbabilities = myPatternStateProbabilities[
                                              pattern];
        final double[] leftStateProbabilities = leftPatternStateProbabilities[
                                                leftPattern];
        final double[] rightStateProbabilities =
          rightPatternStateProbabilities[
          rightPattern];

        for( int endState = 0; endState<numberOfStates; endState++ ) {
          endStateProbabilityStore[endState] =
            leftStateProbabilities[endState]*
            rightStateProbabilities[endState];
        }

        for( int startState = 0; startState<numberOfStates; startState++ ) {
          double probTotal = 0;
          final double[] speedupArray = transProb[startState];
          for( int endState = 0; endState<numberOfStates; endState++ ) {
            probTotal += speedupArray[endState]*
              endStateProbabilityStore[endState];
          }
          myStateProbabilities[startState] = probTotal;
        }
      }
    }
  }

	private static final void calculatePostExtendedFlatImpl(
	  double distance,
		SubstitutionModel model,
	  final double[][][] transitionProbabilityStore,
    final PatternInfo centerPattern,
    final ConditionalProbabilityStore
    leftConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    rightConditionalProbabilityProbabilties,
    final ConditionalProbabilityStore
    resultStore,
    int numberOfCategories,
    int numberOfStates) {
    model.getTransitionProbabilities( distance, transitionProbabilityStore );

		final int[] patternLookup = centerPattern.getPatternLookup();
    final int numberOfPatterns = centerPattern.getNumberOfPatterns();

    double[][][] resultStoreValues = resultStore.getConditionalProbabilityAccess( numberOfPatterns, false );
    for( int category = 0; category<numberOfCategories; category++ ) {
      int patternAccess = 0;
      final double[][] myPatternStateProbabilities = resultStoreValues[category];
      final double[][] leftPatternStateProbabilities =
			  leftConditionalProbabilityProbabilties.getCurrentConditionalProbabilities( category );
      final double[][] rightPatternStateProbabilities =
        rightConditionalProbabilityProbabilties.getCurrentConditionalProbabilities( category );
      final double[][] transProb = transitionProbabilityStore[category];
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        final int leftPattern = patternLookup[patternAccess++];
        final int rightPattern = patternLookup[patternAccess++];
        final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
        final double[] leftStateProbabilities = leftPatternStateProbabilities[leftPattern];
        final double[] rightStateProbabilities = rightPatternStateProbabilities[rightPattern];
        for( int startState = 0; startState<numberOfStates; startState++ ) {
          double leftTotal = 0;
					double rightTotal = 0;
					final double[] speedupArray = transProb[startState];
          for( int endState = 0; endState<numberOfStates; endState++ ) {
            leftTotal += speedupArray[endState]*leftStateProbabilities[endState];
            rightTotal += speedupArray[endState]*rightStateProbabilities[endState];
          }
          myStateProbabilities[startState] = leftTotal*rightTotal;
        }
      }
    }
  }

  private static final class ExternalImpl extends AbstractExternal implements LHCalculator.External {

    private final int numberOfCategories_;
    private final int numberOfStates_;
    private final double[][][] transitionProbabilityStore_;
    private final double[] stateProbabilityStore_;

		//
		// Serialization Code
		//
		private static final long serialVersionUID = 98765372758522L;

    private ExternalImpl( int numberOfCategories, int numberOfStates ) {
      this.numberOfCategories_ = numberOfCategories;
			this.numberOfStates_ = numberOfStates;
      this.stateProbabilityStore_ = new double[numberOfStates];
			this.transitionProbabilityStore_ = new double[numberOfCategories][
                                         numberOfStates][numberOfStates];
//			//this.myResultStore_ = new ConditionalProbabilityStore( numberOfCategories,
//			//	numberOfStates );
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
      calculateExtendedImpl( transitionProbabilityStore_, centerPattern, leftConditionalProbabilityProbabilties, rightConditionalProbabilityProbabilties, resultStore, numberOfCategories_, numberOfStates_, stateProbabilityStore_ );
    }

    /**
     *
     * @param patternLookup
     * @param numberOfPatterns
     * @param leftConditionalProbabilityProbabilties
     * @param rightConditionalProbabilityProbabilties
     * @param resultStore Where to stick the created categoryPatternState information
     * @return either possible result store if results built from cached information
     */
    public final void calculateFlat( final PatternInfo centerPattern,
                                     final ConditionalProbabilityStore
                                     leftConditionalProbabilityProbabilties,
                                     final ConditionalProbabilityStore
                                     rightConditionalProbabilityProbabilties,
                                     final ConditionalProbabilityStore
                                     resultStore ) {
      calculateFlatImpl( centerPattern, leftConditionalProbabilityProbabilties, rightConditionalProbabilityProbabilties, resultStore, numberOfCategories_, numberOfStates_ );

    }
    /**
     * Extend the conditionals back in time by some distance, with some model
		 * @param distance The distance to extend by
     * @param model The model to use
     * @param conditionalProbabilities The probabilities to extend
     */
    public void calculateSingleExtendedDirect(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore conditionalProbabilities
                                  ) {
			model.getTransitionProbabilities( distance, transitionProbabilityStore_ );

      double[][][] baseStoreValues = conditionalProbabilities.getCurrentConditionalProbabilities();
			for( int category = 0; category<numberOfCategories_; category++ ) {
        int patternAccess = 0;
        final double[][] basePatternStateProbabilities = baseStoreValues[category];
        final double[][] transProb = transitionProbabilityStore_[category];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] baseStateProbabilities = basePatternStateProbabilities[pattern];

          for( int startState = 0; startState<numberOfStates_; startState++ ) {
            double probTotal = 0;
            final double[] speedupArray = transProb[startState];
            for( int endState = 0; endState<numberOfStates_; endState++ ) {
              probTotal += speedupArray[endState]*baseStateProbabilities[endState];
            }
						stateProbabilityStore_[startState] = probTotal;
          }
					System.arraycopy(stateProbabilityStore_,0,baseStateProbabilities,0,numberOfStates_);
        }
      }
   	}

		/**
     * Extend the conditionals back in time by some distance, with some model
		 * @param distance The distance to extend by
     * @param model The model to use
     * @param baseConditionalProbabilities The probabilities to extend
     * @param resultConditionalProbabilities The probabilities to extend
     */
    public void calculateSingleExtendedIndirect(
																		double distance, SubstitutionModel model,
																		int numberOfPatterns,
                                    ConditionalProbabilityStore baseConditionalProbabilities,
                                    ConditionalProbabilityStore resultConditionalProbabilities
                                  ) {
			calculateSingleExtendedIndirectImpl(distance,model,numberOfPatterns,baseConditionalProbabilities,resultConditionalProbabilities,transitionProbabilityStore_,numberOfCategories_,numberOfStates_);
   	}

		 private final double[][][] getResultStoreValues( double distance, SubstitutionModel model,
      PatternInfo centerPattern,
      ConditionalProbabilityStore
      leftFlatConditionalProbabilities,
      ConditionalProbabilityStore
      rightFlatConditionalProbabilities,
      ConditionalProbabilityStore tempStore
      ) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();

      model.getTransitionProbabilities( distance, transitionProbabilityStore_ );

      double[][][] resultStoreValues = tempStore.getConditionalProbabilityAccess( numberOfPatterns, false );
      for( int category = 0; category<numberOfCategories_; category++ ) {
        int patternAccess = 0;
        final double[][] myPatternStateProbabilities = resultStoreValues[category];
        final double[][] leftPatternStateProbabilities = leftFlatConditionalProbabilities.getCurrentConditionalProbabilities( category );
        final double[][] rightPatternStateProbabilities = rightFlatConditionalProbabilities.getCurrentConditionalProbabilities( category );
        final double[][] transProb = transitionProbabilityStore_[category];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final int leftPattern = patternLookup[patternAccess++];
          final int rightPattern = patternLookup[patternAccess++];
          final double[] myStateProbabilities = myPatternStateProbabilities[pattern];
          final double[] leftStateProbabilities = leftPatternStateProbabilities[leftPattern];
          final double[] rightStateProbabilities = rightPatternStateProbabilities[rightPattern];

          for( int startState = 0; startState<numberOfStates_; startState++ ) {
            double probTotal = 0;
            final double[] speedupArray = transProb[startState];
            for( int endState = 0; endState<numberOfStates_; endState++ ) {
              probTotal += speedupArray[endState]*leftStateProbabilities[endState];
            }
            myStateProbabilities[startState] = probTotal*rightStateProbabilities[startState];
          }
        }
      }
      return resultStoreValues;
    }

    public double calculateLogLikelihood( double distance, SubstitutionModel model,
                                       PatternInfo centerPattern,
                                       ConditionalProbabilityStore
                                       leftFlatConditionalProbabilities,
                                       ConditionalProbabilityStore
                                       rightFlatConditionalProbabilities,
                                       ConditionalProbabilityStore tempStore
                                       ) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final double[][][] resultStoreValues = getResultStoreValues( distance, model, centerPattern, leftFlatConditionalProbabilities, rightFlatConditionalProbabilities, tempStore );
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
      double logLikelihood = 0;
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        double total = 0;
        for( int cat = 0; cat<numberOfCategories_; cat++ ) {
          final double[] states = resultStoreValues[
                                  cat][pattern];
          double prob = 0;
          for( int state = 0; state<numberOfStates_; state++ ) {
            prob += equilibriumFrequencies[state]*states[state];
          }
          total += probabilities[cat]*prob;
        }
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }



    protected void calculateCategoryPatternProbabilities( double distance, SubstitutionModel model,
                                       PatternInfo centerPattern,
                                       ConditionalProbabilityStore
                                       leftFlatConditionalProbabilities,
                                       ConditionalProbabilityStore
                                       rightFlatConditionalProbabilities,
                                       ConditionalProbabilityStore tempStore,
                                       double[][] categoryPatternLogLikelihoods
                                       ) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
      final double[][][] resultStoreValues = getResultStoreValues( distance, model, centerPattern, leftFlatConditionalProbabilities, rightFlatConditionalProbabilities, tempStore );
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      double logLikelihood = 0;
      for( int cat = 0; cat<numberOfCategories_; cat++ ) {
        double total = 0;
        final double[][] patternValues = resultStoreValues[cat];
        final double[] patternLogLikelihoods = categoryPatternLogLikelihoods[cat];
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] states = patternValues[pattern];
          double prob = 0;
          for( int state = 0; state<numberOfStates_; state++ ) {
            prob += equilibriumFrequencies[state]*states[state];
          }
          patternLogLikelihoods[pattern] = prob;
        }
      }

    }
    /**
     * Calculate the likelihood given the conditional probabilites at the root
     * @param model The substitution model used
     * @param centerPattern the pattern information
     * @param conditionalProbabilities The conditionals
     * @return the Log likelihood
     */
    public double calculateLogLikelihoodSingle( SubstitutionModel model, int[] patternWeights, int numberOfPatterns,
                                       ConditionalProbabilityStore conditionalProbabilityStore) {
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
			double logLikelihood = 0;
      double[][][] conditionalProbabilities =
        conditionalProbabilityStore.getCurrentConditionalProbabilities();
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        double total = 0;
				for( int cat = 0; cat<numberOfCategories_; cat++ ) {
          final double[] baseStates = conditionalProbabilities[cat][pattern];
          double prob = 0;
          for( int state = 0; state<numberOfStates_; state++ ) {
            prob += equilibriumFrequencies[state]*baseStates[state] ;
          }
          total += probabilities[cat]*prob;
        }
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

    public double calculateLogLikelihood( SubstitutionModel model,
                                       PatternInfo centerPattern,
                                       ConditionalProbabilityStore
                                       leftConditionalProbabilitiesStore,
                                       ConditionalProbabilityStore
                                       rightConditionalProbabilitiesStore ) {
      final int[] patternWeights = centerPattern.getPatternWeights();
      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();
		  final int[] sitePatternMatchup = centerPattern.getSitePatternMatchup();
      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      final double[] probabilities = model.getTransitionCategoryProbabilities();
			double logLikelihood = 0;
      double[][][] leftConditionalProbabilities =
        leftConditionalProbabilitiesStore.
        getCurrentConditionalProbabilities();
      double[][][] rightConditionalProbabilities =
        rightConditionalProbabilitiesStore.
        getCurrentConditionalProbabilities();
      int patternIndex = 0;
      for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
        double total = 0;
        final int leftIndex = patternLookup[patternIndex++];
        final int rightIndex = patternLookup[patternIndex++];

        for( int cat = 0; cat<numberOfCategories_; cat++ ) {
          final double[] left = leftConditionalProbabilities[cat][leftIndex];
          final double[] right = rightConditionalProbabilities[cat][rightIndex];
          double prob = 0;
          for( int state = 0; state<numberOfStates_; state++ ) {
						prob += equilibriumFrequencies[state]*( left[state]*right[state] );
          }
          total += probabilities[cat]*prob;
        }
        logLikelihood += Math.log( total )*patternWeights[pattern];
      }
      return logLikelihood;
    }

		public void calculateCategoryPatternProbabilities( SubstitutionModel model,
			PatternInfo centerPattern,
			ConditionalProbabilityStore
			leftConditionalProbabilitiesStore,
			ConditionalProbabilityStore
			rightConditionalProbabilitiesStore,
			double[][] categoryPatternLogLikelihoods) {

      final int[] patternLookup = centerPattern.getPatternLookup();
      final int numberOfPatterns = centerPattern.getNumberOfPatterns();

      final double[] equilibriumFrequencies = model.getEquilibriumFrequencies();
      double[][][] leftConditionalProbabilities = leftConditionalProbabilitiesStore.getCurrentConditionalProbabilities();
      double[][][] rightConditionalProbabilities = rightConditionalProbabilitiesStore.getCurrentConditionalProbabilities();

      for( int cat = 0; cat<numberOfCategories_; cat++ ) {
        double total = 0;
        final double[][] leftPatterns = leftConditionalProbabilities[cat];
        final double[][] rightPatterns = rightConditionalProbabilities[cat];
        final double[] patternLogLikelihoods = categoryPatternLogLikelihoods[cat];
        int patternIndex = 0;
        for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
          final double[] left = leftPatterns[patternLookup[patternIndex++]];
          final double[] right = rightPatterns[patternLookup[patternIndex++]];
          double prob = 0;
          for( int state = 0; state<numberOfStates_; state++ ) {
            prob += equilibriumFrequencies[state]*( left[state]*right[state] );
          }
          patternLogLikelihoods[pattern]=prob;
        }
      }
    }


  } //End of class StatefulImpl

// =--==--=-=-==-=--==--=-==-=-=-=-=--==-=--=

  private static final class InternalImpl implements LHCalculator.Internal {

    private final int numberOfCategories_;
    private final int numberOfStates_;
    private final ConditionalProbabilityStore myResultStore_;
    private final double[][][] transitionProbabilityStore_;
    private final double[] endStateProbabilityStore_;
    private int currentNumberOfPatterns_ = 0;
    private double lastDistance_ = -1;

    private InternalImpl( int numberOfCategories, int numberOfStates,Generator parentGenerator ) {
      this.numberOfCategories_ = numberOfCategories;
      this.numberOfStates_ = numberOfStates;
      this.endStateProbabilityStore_ = new double[numberOfStates];
      this.transitionProbabilityStore_ = new double[numberOfCategories][
                                         numberOfStates][numberOfStates];
      this.myResultStore_ = parentGenerator.createAppropriateConditionalProbabilityStore( false );
    }
    public final ConditionalProbabilityStore calculateSingleExtended(
				final double distance, final SubstitutionModel model,
				final PatternInfo centerPattern,
        final ConditionalProbabilityStore baseConditionalProbabilityProbabilties,
        final boolean modelChangedSinceLastCall
			) {
			return baseConditionalProbabilityProbabilties;
//			calculateSingleExtendedIndirectImpl(distance,model,numberOfCategories_,baseConditionalProbabilityProbabilties,myResultStore_,transitionProbabilityStore_,numberOfCategories_,numberOfStates_);
//			return myResultStore_;
		}
    public final ConditionalProbabilityStore calculateExtended(
				final double distance, final SubstitutionModel model,
				final PatternInfo centerPattern,
        final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore rightConditionalProbabilityProbabilties,
        final boolean modelChangedSinceLastCall
			) {

      if( modelChangedSinceLastCall||distance!=lastDistance_||lastDistance_<0 ) {
        model.getTransitionProbabilities( distance, transitionProbabilityStore_ );
        lastDistance_ = distance;
      }
      calculateExtendedImpl( transitionProbabilityStore_, centerPattern, leftConditionalProbabilityProbabilties, rightConditionalProbabilityProbabilties, myResultStore_, numberOfCategories_, numberOfStates_, endStateProbabilityStore_ );
      return myResultStore_;
    }
		public final ConditionalProbabilityStore calculatePostExtendedFlat(
				final double distance, final SubstitutionModel model,
				final PatternInfo centerPattern,
        final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore rightConditionalProbabilityProbabilties,
        final boolean modelChangedSinceLastCall
			) {
      calculatePostExtendedFlatImpl(  distance, model, transitionProbabilityStore_, centerPattern, leftConditionalProbabilityProbabilties, rightConditionalProbabilityProbabilties, myResultStore_, numberOfCategories_, numberOfStates_);
      return myResultStore_;
    }

    public final ConditionalProbabilityStore calculateFlat(
				final PatternInfo centerPattern,
				final ConditionalProbabilityStore leftConditionalProbabilityProbabilties,
        final ConditionalProbabilityStore rightConditionalProbabilityProbabilties
			) {
      calculateFlatImpl( centerPattern,
                         leftConditionalProbabilityProbabilties,
                         rightConditionalProbabilityProbabilties, myResultStore_, numberOfCategories_, numberOfStates_ );
      return myResultStore_;
    }

  } //End of class InternalImpl

// =--==--=-=-==-=--==--=-==-=-=-=-=--==-=--=


  public static final LHCalculator.Factory getFactory() {
    return FACTORY_INSTANCE;
  }

// -=-=--==-=-=-=---=-==-=--==-=-=-=-
  private static final class SimpleFactory implements LHCalculator.Factory {
    public SimpleFactory() {}

    public LHCalculator.Generator createSeries( int numberOfCategories,
                                                DataType dt ) {
      return new SimpleGenerator( numberOfCategories, dt.getNumStates() );
    }
  }

  // -=-=--==-=-=-=---=-==-=--==-=-=-=-

  private static final class SimpleGenerator implements LHCalculator.Generator {
    private int numberOfCategories_;
    private int numberOfStates_;
    //
		// Serialization Code
		//
		private static final long serialVersionUID = 75762749252L;

		private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
			out.writeByte(2); //Version number
			out.writeInt(numberOfCategories_);
			out.writeInt(numberOfStates_);
		}

		private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
			byte version = in.readByte();
			switch(version) {
				case 1 : {
					numberOfCategories_ = in.readInt();
					numberOfStates_ = in.readInt();
					//Old endStateProbabilityStore
					in.readObject();
					break;
				}
				default : {
					numberOfCategories_ = in.readInt();
					numberOfStates_ = in.readInt();
					break;
				}

			}
		}

    public SimpleGenerator( int numberOfCategories, int numberOfStates ) {
      this.numberOfCategories_ = numberOfCategories;
      this.numberOfStates_ = numberOfStates;
    }
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns) {
		  return new SimpleLeafCalculator(patternStateMatchup,numberOfPatterns, numberOfStates_, numberOfCategories_,this);
//		  return new pebble.eval.OldSchoolLeafCalculator(patternStateMatchup,numberOfPatterns, numberOfStates_, numberOfCategories_,this);
		}
		public Leaf createNewLeaf(int[] patternStateMatchup, int numberOfPatterns, Generator parentGenerator ) {
			return parentGenerator.createNewLeaf(patternStateMatchup,numberOfPatterns);
//		  return new pebble.eval.OldSchoolLeafCalculator(patternStateMatchup,numberOfPatterns, numberOfStates_, numberOfCategories_,parentGenerator);
//		  return new SimpleLeafCalculator(patternStateMatchup,numberOfPatterns, numberOfStates_, numberOfCategories_,parentGenerator);
		}
    public LHCalculator.External createNewExternal() {
      return new ExternalImpl( numberOfCategories_, numberOfStates_ );

    }

    public LHCalculator.Internal createNewInternal() {
      return new InternalImpl( numberOfCategories_, numberOfStates_, this );
    }

    public LHCalculator.External createNewExternal( Generator parentGenerator ) throws IllegalArgumentException {
      return new ExternalImpl( numberOfCategories_, numberOfStates_ );

    }

    public LHCalculator.Internal createNewInternal( Generator parentGenerator ) throws IllegalArgumentException {
      return new InternalImpl( numberOfCategories_, numberOfStates_, parentGenerator );
    }

    public ConditionalProbabilityStore createAppropriateConditionalProbabilityStore( boolean isForLeaf ) {
      return new ConditionalProbabilityStore( numberOfCategories_, numberOfStates_);
    }
		public boolean isAllowCaching() { return false; }

  }
}

