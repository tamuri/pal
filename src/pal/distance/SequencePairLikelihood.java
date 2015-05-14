// SequencePairLikelihood.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;

import pal.alignment.*;
import pal.datatype.*;
import pal.math.*;
import pal.misc.*;
import pal.substmodel.*;


/**
 * computation of the (negative) log-likelihood for a pair of sequences
 *
 * @version $Id: SequencePairLikelihood.java,v 1.11 2002/12/05 04:27:28 matt Exp $
 *
 * @author Korbinian Strimmer
 */
public class SequencePairLikelihood implements UnivariateFunction, java.io.Serializable
{
	/**
	 * initialisation
	 *
	 * @param sp site pattern
	 * @param m model of substitution
	 */
	public SequencePairLikelihood(SitePattern sp, SubstitutionModel m)	{
		updateSitePattern(sp);	updateModel(m);
	}

	/**
	 * update model of substitution
	 *
	 * @param model of substitution
	 */
	public void updateModel(SubstitutionModel m)
	{
		model = m;
		double[] equlibriumFrequencies = m.getEquilibriumFrequencies();
		numberOfTransitionCategories_ = model.getNumberOfTransitionCategories();
		transitionCategoryProbabilities_ = model.getTransitionCategoryProbabilities();
		int dimension = model.getDataType().getNumStates();
		transitionProbabilityStores_ = new double[numberOfTransitionCategories_][dimension][dimension];
		fastMatchCount_ = new int[dimension][dimension];
		logEquilibriumFrequencies_ =  new double[equlibriumFrequencies.length];
		for(int i = 0 ; i < equlibriumFrequencies.length ; i++) {
			logEquilibriumFrequencies_[i] = Math.log(equlibriumFrequencies[i]);
		}
	}

	/**
	 * update site pattern
	 *
	 * @param site pattern
	 */
	public void updateSitePattern(SitePattern sp)
	{
		sitePattern = sp;
		numPatterns = sp.numPatterns;
		numSites = sp.getSiteCount();
		patternDataType_ = sp.getDataType();
		isAmbiguous_ = patternDataType_.isAmbiguous();

		numStates = patternDataType_.getNumStates();
		weight = sp.weight;

	}


	/**
	 * specification of two sequences in the given alignment
	 *
	 * @param s1 number of first sequence
	 * @param s2 number of second sequence
	 */
	public void setSequences(int s1, int s2)
	{
		setSequences(sitePattern.pattern[s1], sitePattern.pattern[s2]);
	}

	/**
	 * specification of two sequences (not necessarily in the given
	 * alignment but with the same weights in the site pattern)
	 *
	 * @param s1 site pattern of first sequence
	 * @param s2 site pattern of second sequence
	 */
	public void setSequences(byte[] s1, byte[] s2)
	{
		seqPat1 = s1;
		seqPat2 = s2;

	}

	/**
	 * compute (negative) log-likelihood for a given distance
	 * between the two sequences
	 *
	 * @param arc expected distance
	 *
	 * @return negative log-likelihood
	 */
	/*public double evaluate(double arc)
	{
		model.setDistance(arc);

		double loglkl = 0;
		for (int i = 0; i < numPatterns; i++)
		{
			double sumprob = 0;
			for (int r = 0; r < numRates; r++)
			{
				sumprob += rateProb[r]*probConfig(r, seqPat1[i], seqPat2[i]);
			}
			loglkl += weight[i]*Math.log(sumprob);
		}

		return -loglkl;
	}*/
	private final void clearFastMatchCount(final int numStates) {
		for(int i = 0 ; i < numStates ; i++) {
			for(int j = 0 ; j < numStates ; j++) {
				fastMatchCount_[i][j] = 0;
			}
		}
	}
	private final double evaluateAmbiguous(double arc) {
		AmbiguousDataType adt = patternDataType_.getAmbiguousVersion();
		DataType specificDataType = adt.getSpecificDataType();
		int numberOfSpecficStates = specificDataType.getNumStates();
		model.getTransitionProbabilities(arc, transitionProbabilityStores_);
		double loglkl = 0;
		boolean sequenceOneIsGap, sequenceTwoIsGap;
		double p;
		double fmc;
		clearFastMatchCount(numberOfSpecficStates);

		for (int pattern = 0; pattern < numPatterns; pattern++) {
			int sequenceOneState = seqPat1[pattern];
			int sequenceTwoState = seqPat2[pattern];
			sequenceOneIsGap = patternDataType_.isUnknownState(sequenceOneState);
			sequenceTwoIsGap = patternDataType_.isUnknownState(sequenceTwoState);
			if (!sequenceOneIsGap && !sequenceTwoIsGap) {
				int[] firstStates = adt.getSpecificStates(sequenceOneState);
				int[] secondStates = adt.getSpecificStates(sequenceTwoState);
				for(int f = 0 ; f < firstStates.length ; f++) {
					for(int s = 0 ; s < secondStates.length ; s++) {
						fastMatchCount_[firstStates[f]][secondStates[s]]+=weight[pattern];
					}
				}
			}
		}
		for(int sequenceOneState = 0 ; sequenceOneState < numberOfSpecficStates ; sequenceOneState++) {
			for(int sequenceTwoState = 0 ; sequenceTwoState < numberOfSpecficStates ; sequenceTwoState++) {
				int count = fastMatchCount_[sequenceOneState][sequenceTwoState];
				if(count>0) {
					double total = 0;
					for(int category = 0 ; category < numberOfTransitionCategories_ ; category++) {
						total+=transitionProbabilityStores_[category][sequenceOneState][sequenceTwoState]*transitionCategoryProbabilities_[category];
					}
					loglkl +=
						count*(
							logEquilibriumFrequencies_[sequenceOneState]+
							Math.log(total)
						);
				}
			}
		}
		return -loglkl;
	}
	public final double evaluate(final double arc) {
		if(isAmbiguous_) {
			return evaluateAmbiguous(arc);
		}
		model.getTransitionProbabilities(arc, transitionProbabilityStores_);
		double loglkl = 0;
		boolean sequenceOneIsGap, sequenceTwoIsGap;
		double p;
		double fmc;
		clearFastMatchCount(numStates);
		for (int pattern = 0; pattern < numPatterns; pattern++) {
			int sequenceOneState = seqPat1[pattern];
			int sequenceTwoState = seqPat2[pattern];
			sequenceOneIsGap = patternDataType_.isUnknownState(sequenceOneState);
			sequenceTwoIsGap = patternDataType_.isUnknownState(sequenceTwoState);
			//I've changed the following because the gapped contributions are always constant!
			/*if (!sequenceOneIsGap || !sequenceTwoIsGap) {
				if (sequenceOneIsGap) {
					loglkl += weight[pattern]*logEquilibriumFrequencies_[sequenceTwoState];
				}	else if (sequenceTwoIsGap){
					loglkl += weight[pattern]*logEquilibriumFrequencies_[sequenceOneState];
				} else {
					fastMatchCount_[sequenceOneState][sequenceTwoState]+=weight[pattern];
				}
			}*/
			if (!sequenceOneIsGap && !sequenceTwoIsGap) {
				fastMatchCount_[sequenceOneState][sequenceTwoState]+=weight[pattern];
			}
		}
		for(int sequenceOneState = 0 ; sequenceOneState < numStates ; sequenceOneState++) {
			for(int sequenceTwoState = 0 ; sequenceTwoState < numStates ; sequenceTwoState++) {
				int count = fastMatchCount_[sequenceOneState][sequenceTwoState];
				if(count>0) {
					double total = 0;
					for(int category = 0 ; category < numberOfTransitionCategories_ ; category++) {
						total+=transitionProbabilityStores_[category][sequenceOneState][sequenceTwoState]*transitionCategoryProbabilities_[category];
					}
					loglkl +=
						count*(
							logEquilibriumFrequencies_[sequenceOneState]+
							Math.log(total)
						);
				}
			}
		}
		return -loglkl;
	}



	public double getLowerBound() {	return BranchLimits.MINARC;	}
	public double getUpperBound() {	return BranchLimits.MAXARC;	}

	//
	// Private stuff
	//

	private SubstitutionModel model;
	private SitePattern sitePattern;
	private DataType patternDataType_;
	private int numPatterns;
	private int numSites;
	private int numStates;
	private int numberOfTransitionCategories_;
	private int[] weight;
	private double[] logEquilibriumFrequencies_;
	private double[][][] transitionProbabilityStores_;
	private double[] transitionCategoryProbabilities_;
	private int[][] fastMatchCount_;
	private byte[] seqPat1;
	private byte[] seqPat2;
	boolean isAmbiguous_;

}
