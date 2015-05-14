// ConditionalProbabilityStore.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: ConditionalProbabilityStore (was ConditionalLikelihoodStore) </p>
 * <p>Description: A container class for various bits of data relating to
 * the conditional likelihood. Things stored include the conditional likelihood,
 * an scale factors and whether the current conditional likelihoods were created
 * from cached data.</p>
 * @author Matthew Goode
 * @version 1.0
 */

public final class ConditionalProbabilityStore implements java.io.Serializable {
  private final int numberOfCategories_;
  private final int numberOfStates_;
  private final double[][][] store_;
  private int patternCapacity_;

	private final ExtraProcessor extraProcessor_;

  private boolean isBasedOnCachedData_ = false;
	private boolean fix_ = false;

	private ConditionalProbabilityStore(ConditionalProbabilityStore toCopy) {
	  this.numberOfCategories_ = toCopy.numberOfCategories_;
		this.numberOfStates_ = toCopy.numberOfStates_;
		this.store_ = pal.misc.Utils.getCopy(toCopy.store_);
		this.patternCapacity_ = toCopy.patternCapacity_;
		this.extraProcessor_ = (toCopy.extraProcessor_ == null ? null : toCopy.extraProcessor_.getCopy());
		this.isBasedOnCachedData_ = toCopy.isBasedOnCachedData_;
		this.fix_ = toCopy.fix_;
	}
  public ConditionalProbabilityStore(int numberOfCategories, int numberOfStates) {
    this(numberOfCategories,numberOfStates,null);
  }
  public ConditionalProbabilityStore(int numberOfCategories, int numberOfStates, ExtraProcessor extraProcessor) {
    this.numberOfCategories_ = numberOfCategories;
    this.numberOfStates_ = numberOfStates;
    this.store_ = new double[numberOfCategories][][];
    this.patternCapacity_ = 0;
		this.extraProcessor_ = extraProcessor;
		if(extraProcessor_!=null) {
			this.extraProcessor_.setParent(this);
			this.extraProcessor_.setNewNumberOfPatterns( 0 );
		}
  }
	/**
	 * Cloning
	 * @return a copy of this conditional probability store
	 */
	public final ConditionalProbabilityStore getCopy() {
	  return new ConditionalProbabilityStore(this);
	}

  public final boolean isHasExtraProcessor() { return extraProcessor_!=null; }
	public final ExtraProcessor getExtraProcessor() { return extraProcessor_; }

  /**
   * Will check the current allocation to see if it can accomodate the requested number
   * of patterns. If not, new arrays are allocated and old data is lost.
   * @param numberOfPatterns
   */
  private final void ensureSize(int numberOfPatterns, boolean createStateArray) {
    if(numberOfPatterns>patternCapacity_) {
			if(fix_) {
			  throw new IllegalArgumentException("Cannot resize to accomodate "+numberOfPatterns+" patterns (store has been fixed)");
			}
      if(createStateArray) {
				for( int i = 0; i<numberOfCategories_; i++ ) {
          this.store_[i] = new double[numberOfPatterns][numberOfStates_];
        }

      } else {
        for( int i = 0; i<numberOfCategories_; i++ ) {
          this.store_[i] = new double[numberOfPatterns][];
        }
      }
      if(extraProcessor_!=null) {	extraProcessor_.setNewNumberOfPatterns(numberOfPatterns);      }

			this.patternCapacity_ = numberOfPatterns;
    }
  }

  public int getPatternCapacity() {  return patternCapacity_;  }

  /**
   * Used for getting access to the internal conditional probability store when
   * the data is not to be directly changed.
   * @return An array of arrays of arrays, in the form [category][pattern][state]
   */
  public double[][][] getCurrentConditionalProbabilities() { return store_;  }
  /**
   * Used for getting access to the internal conditional probability store when
   * the data is not to be directly changed.
   * @param category the transition category of interest
   * @return An array of arrays in the form [pattern][state]
   */
  public double[][] getCurrentConditionalProbabilities(int category) { return store_[category]; }

  /**
   * Use this when access the internal conditional likelihood store for the purpose
   * of changing the contents.
   * @param numberOfPatterns An indication of how much space will be required. The result will always be big enough to accomodate the requested number of patterns.
   * @param resultsBasedOnCachedData An indication of whether the new conditionals about to be stored are based on cached data
   * @return
   */
  public double[][][] getConditionalProbabilityAccess(int numberOfPatterns, boolean resultsBasedOnCachedData) {
    ensureSize(numberOfPatterns,true);
    this.isBasedOnCachedData_ = resultsBasedOnCachedData;
    return store_;
  }
	/**
   * Use this when access the internal conditional likelihood store for the purpose
   * of changing the contents. This version will not automatically resize array, and will throw an exception if the numberOfPatterns requested is incompatible with the current contents of this store.
   * @param numberOfPatterns An indication of how much space will be required. An exception is thrown if this number of patterns cannot be accomodated without being resized.
   * @param resultsBasedOnCachedData An indication of whether the new conditionals about to be stored are based on cached data
   * @return
	 * @throws IllegalArgumentException if incompatible number of patterns
   */
  public double[][][] getConditionalProbabilityAccessNoChangeData(int numberOfPatterns, boolean resultsBasedOnCachedData) {
    if(numberOfPatterns>patternCapacity_) {
		  throw new IllegalArgumentException("Cannot provided for requested number of patterns. Asked for "+numberOfPatterns+" can only give "+patternCapacity_);
		}
    this.isBasedOnCachedData_ = resultsBasedOnCachedData;
    return store_;
  }
  /**
   * Use this when access the internal conditional likelihood store for the purpose
   * of changing the contents.
   * The state arrays will not be created.
   * @param numberOfPatterns An indication of how much space will be required. The result will always be big enough to accomodate the requested number of patterns.
   * @param resultsBasedOnCachedData An indication of whether the new conditionals about to be stored are based on cached data
   * @return
   */
  public double[][][] getIncompleteConditionalProbabilityAccess(int numberOfPatterns, boolean resultsBasedOnCachedData, boolean fix) {
    ensureSize(numberOfPatterns,false);
    this.isBasedOnCachedData_ = resultsBasedOnCachedData;
    this.fix_ = fix;
		return store_;
  }
	public double calculateLogLikelihood(double[] categoryProbabilities, double[] equilibriumFrequencies, int[] patternWeights, int numberOfPatterns) {
		double logLikelihood = 0;
    for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
      double total = 0;
      for( int cat = 0; cat<numberOfCategories_; cat++ ) {
				double prob = 0;
				final double[] stateArray = store_[cat][pattern];
				for(int state = 0 ; state < numberOfStates_ ; state++) {
				  prob+=equilibriumFrequencies[state]*stateArray[state];
				}
        total += categoryProbabilities[cat]*prob;
      }
		  if(patternWeights!=null) {
				logLikelihood += Math.log( total )*patternWeights[pattern];
			} else {
				logLikelihood += Math.log( total );
			}
    }
    return logLikelihood;
	}

	public double calculateLogLikelihood(double[] categoryProbabilities, double[] equilibriumFrequencies, int numberOfPatterns) {
		return calculateLogLikelihood(categoryProbabilities,equilibriumFrequencies,null,numberOfPatterns);
	}

	public double[] calculatePatternLogLikelihoods(double[] categoryProbabilities, double[] equilibriumFrequencies, int numberOfPatterns) {
    double[] result = new double[numberOfPatterns];
		for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
			double total = 0;
      for( int cat = 0; cat<numberOfCategories_; cat++ ) {
				double prob = 0;
				final double[] stateArray = store_[cat][pattern];
				for(int state = 0 ; state < numberOfStates_ ; state++) {
				  prob+=equilibriumFrequencies[state]*stateArray[state];
				}

    		total += categoryProbabilities[cat]*prob;
      }
			result[pattern] = Math.log(total);
	  }
    return result;

	}


	/**
	 * Calculate the conditional probabilities for each ancestral state at each site pattern, multiplied by related equilibrium frequencies
	 * @param categoryProbabilities The prior probability of a site belonging to a particular category
	 * @param equilibriumFrequencies the prior probabibilities of seeing a particular state
	 * @param numberOfPatterns The number of patterns
	 * @return the related conditional probability array organised [category][pattern]
	 */
	public double[][] calculateCategoryPatternConditionalProbabilities( double[] categoryProbabilities, double[] equilibriumFrequencies, int numberOfPatterns ) {
		double logLikelihood = 0;
		int patternIndex = 0;
		double[][] result = new double[numberOfCategories_][numberOfPatterns];
		for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
			double total = 0;
			for( int cat = 0; cat<numberOfCategories_; cat++ ) {
				double prob = 0;
				final double[] stateArray = store_[cat][pattern];
				if(equilibriumFrequencies==null) {
					for( int state = 0; state<numberOfStates_; state++ ) {
						prob += stateArray[state];
					}
				} else {
					for( int state = 0; state<numberOfStates_; state++ ) {
						prob += equilibriumFrequencies[state]*stateArray[state];
					}
				}
				if(categoryProbabilities==null) {
			  	result[cat][pattern] = prob;
				} else {
					result[cat][pattern] = categoryProbabilities[cat]*prob;
				}
			}
		}
		return result;
	}
	public boolean isBasedOnCachedData() { return isBasedOnCachedData_; }
	public void setBasedOnCachedData(boolean v) { this.isBasedOnCachedData_ = v; }

	public String toString() {	return toString(patternCapacity_);	}

	public String toString(int numberOfPatterns) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0 ; i < numberOfCategories_ ; i++) {
			sb.append(i);
			sb.append(":");
			sb.append(pal.misc.Utils.toString(store_[i]));
			sb.append("\n");

		}
		return sb.toString();
	}
// ====================================================================================
	public static interface ExtraProcessor {
		public ExtraProcessor getCopy();
		public void setNewNumberOfPatterns(int numberOfPatterns);
		public void setParent(ConditionalProbabilityStore parent);
	}

}