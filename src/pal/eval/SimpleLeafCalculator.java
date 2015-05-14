// SimpleLeafCalculator.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: SimpleLeafCalculator </p>
 * <p>Description: A simple implementation of a calculator for conditional probabilites are a leaf (tip), with no ambiguities in the data</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 * @note not designed to be serialized
 */
import pal.substmodel.*;
public class SimpleLeafCalculator implements LHCalculator.Leaf{
	private final int numberOfStates_;
	private final int[] patternStateMatchup_; //Only needed for cloning...
	private final int numberOfPatterns_; //Only needed for cloning...
	private final int numberOfCategories_; //Only needed for cloning...
	private final LHCalculator.Generator parent_;//Only needed for cloning...

	private final double[][][] transitionProbabilitiyStore_;
	private final ConditionalProbabilityStore conditionalProbabilities_;
	private final ConditionalProbabilityStore flatConditionalProbabilities_;

	private double lastDistance_ = -1;

	private SimpleLeafCalculator(SimpleLeafCalculator toCopy) {
		this.numberOfStates_ = toCopy.numberOfStates_;
		this.patternStateMatchup_ = toCopy.patternStateMatchup_;
		this.numberOfPatterns_ = toCopy.numberOfPatterns_;
		this.numberOfCategories_ = toCopy.numberOfCategories_;
		this.parent_ = toCopy.parent_;

		this.transitionProbabilitiyStore_ = pal.misc.Utils.getCopy(toCopy.transitionProbabilitiyStore_);
		this.flatConditionalProbabilities_ = createFlat(patternStateMatchup_,numberOfPatterns_,numberOfCategories_,numberOfStates_,parent_);
		this.conditionalProbabilities_ = createExtended(transitionProbabilitiyStore_, patternStateMatchup_,numberOfPatterns_,numberOfCategories_,numberOfStates_,parent_);
		this.lastDistance_ = toCopy.lastDistance_;
	}
	public SimpleLeafCalculator( int[] patternStateMatchup, int numberOfPatterns, int numberOfStates, int numberOfCategories, LHCalculator.Generator parent ) {
		this.numberOfStates_ = numberOfStates;
		this.numberOfCategories_ = numberOfCategories;
		this.numberOfPatterns_ = numberOfPatterns;
		this.parent_ = parent;
		this.patternStateMatchup_ = pal.misc.Utils.getCopy(patternStateMatchup);

		// StatePatternMatchup matches a state to it's new pattern (is undefined if state does not occur)

		this.transitionProbabilitiyStore_ = new double[numberOfCategories][numberOfStates][numberOfStates];

		this.conditionalProbabilities_ = createExtended(transitionProbabilitiyStore_,patternStateMatchup,numberOfPatterns,numberOfCategories,numberOfStates,parent);
		this.flatConditionalProbabilities_ = createFlat(patternStateMatchup,numberOfPatterns,numberOfCategories,numberOfStates,parent);
	}
// ====================================
	private static final ConditionalProbabilityStore createFlat(int[] patternStateMatchup, int numberOfPatterns, int numberOfCategories, int numberOfStates, LHCalculator.Generator parent ) {
		ConditionalProbabilityStore flatConditionalProbabilities = parent.createAppropriateConditionalProbabilityStore( true );
	  final double[] gapStore = new double[numberOfStates];
		for(int i = 0 ; i < gapStore.length ; i++) {
		  gapStore[i] = 1;
		}
		double[][] stateStuff = new double[numberOfStates][numberOfStates];
		for( int i = 0; i<numberOfStates; i++ ) {
			stateStuff[i][i] = 1;
		}
		double[][][] flatStore = flatConditionalProbabilities.getIncompleteConditionalProbabilityAccess( numberOfPatterns, true, true );

		for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
			int state = patternStateMatchup[pattern];
			//If state is gap
			if( state==numberOfStates ) {
				for( int cat = 0; cat<numberOfCategories; cat++ ) {	flatStore[cat][pattern] = gapStore;	}
			} else {
				for( int cat = 0; cat<numberOfCategories; cat++ ) {	flatStore[cat][pattern] = stateStuff[state]; }
			}
		}
		return flatConditionalProbabilities;
	}
	private static final ConditionalProbabilityStore createExtended(double[][][] transitionProbabilityStore, int[] patternStateMatchup, int numberOfPatterns, int numberOfCategories, int numberOfStates, LHCalculator.Generator parent ) {
		ConditionalProbabilityStore extendedConditionalProbabilities = parent.createAppropriateConditionalProbabilityStore( true );
	  final double[] gapStore = new double[numberOfStates];
		for(int i = 0 ; i < gapStore.length ; i++) {
		  gapStore[i] = 1;
		}
		double[][] stateStuff = new double[numberOfStates][numberOfStates];
		for( int i = 0; i<numberOfStates; i++ ) {
			stateStuff[i][i] = 1;
		}
		double[][][] extendedStore = extendedConditionalProbabilities.getIncompleteConditionalProbabilityAccess( numberOfPatterns, true, true );

		for( int pattern = 0; pattern<numberOfPatterns; pattern++ ) {
			int state = patternStateMatchup[pattern];
			//If state is gap
			if( state==numberOfStates ) {
				for( int cat = 0; cat<numberOfCategories; cat++ ) {	extendedStore[cat][pattern] = gapStore;	}
			} else {
				for( int cat = 0; cat<numberOfCategories; cat++ ) {	extendedStore[cat][pattern] = transitionProbabilityStore[cat][state];	}
			}
		}
		return extendedConditionalProbabilities;
	}
// ======================================


	public LHCalculator.Leaf getCopy() {
		return new SimpleLeafCalculator(this);
	}
	public ConditionalProbabilityStore getFlatConditionalProbabilities() {
	  return flatConditionalProbabilities_;
	}

	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, SubstitutionModel model, boolean modelChanged) {
		if(distance!=lastDistance_||lastDistance_<0||modelChanged) {
			model.getTransitionProbabilitiesTranspose( distance,transitionProbabilitiyStore_ );
			lastDistance_ = distance;
			conditionalProbabilities_.setBasedOnCachedData(false);
		} else {
			conditionalProbabilities_.setBasedOnCachedData(true);
		}
		return conditionalProbabilities_;
	}

} //End of class SimpleLeafCalculator
