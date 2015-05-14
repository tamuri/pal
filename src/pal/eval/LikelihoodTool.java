// LikelihoodTool.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: LikelihoodTool</p>
 * <p>Description: A set of static methods for doing common Likelihood tasks. Also serves as example code for doing likeilihood analysis. </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.tree.*;
import pal.math.*;
import pal.alignment.*;
import pal.substmodel.SubstitutionModel;
import pal.misc.TimeOrderCharacterData;
import pal.mep.*;
import pal.datatype.*;

public final class LikelihoodTool {
	/**
	 * Calculate the log likelihood of a particular set of phylogenetic data
	 * @param tree The tree with set branch lengths
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use
	 * @return The log likelihood
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static double calculateLogLikelihood(Tree tree, Alignment alignment, SubstitutionModel model) {
		GeneralLikelihoodCalculator lc = new GeneralLikelihoodCalculator(alignment,tree,model);
		return lc.calculateLogLikelihood();
	}

	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with no constraints on the branchlengths (as for an unrooted tree). The topology is unchanged.
	 * @param tree The tree (remains unchanged)
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use (is changed if optimisation of the model is choosen)
	 * @param optimiseModel if true the model is also optimised, otherwise just the tree
	 * @return The optimised tree
	 * @see pal.treesearch.optimiseUnrootedFixed() for an equivalient, but potentially faster method.
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseUnrooted(Tree tree, Alignment alignment, SubstitutionModel model, boolean optimiseModel) {
		UnconstrainedTree ut = new UnconstrainedTree(TreeTool.getUnrooted(tree));
		DataTranslator dt = new DataTranslator(alignment);
		alignment = dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
		if(optimiseModel) {
			LikelihoodOptimiser.optimiseCombined(ut, alignment, model,
																					 new OrthogonalSearch(), 6, 6);
		} else {
			LikelihoodOptimiser.optimiseTree(ut, alignment, model, new OrthogonalSearch(), 6, 6);
		}
		return new SimpleTree(ut);
	}

	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with a molecular clock assumption, that is, constrained such that all tips are contemporaneous, the tree is treated as rooted. The topology is unchanged.
	 * @param tree The tree with set branch lengths
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use
	 * @param optimiseModel if true the model is optimised as well
	 * @return The resulting optimised tree
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseClockConstrained(Tree tree, Alignment alignment, SubstitutionModel model, boolean optimiseModel) {
	  ClockTree ut = new ClockTree(tree);
		DataTranslator dt = new DataTranslator(alignment);
		alignment = dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
		if(optimiseModel) {
			LikelihoodOptimiser.optimiseCombined(ut, alignment, model,
																					 new OrthogonalSearch(), 6, 6);
		} else {
			LikelihoodOptimiser.optimiseTree(ut, alignment, model, new OrthogonalSearch(), 6, 6);
		}
		return new SimpleTree(ut);
	}
	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with under an assumption of a molecular clock with serially sampled data and a single mutation rate parameter. This is equivalent to the TipDate model. The topology is unchanged.
	 * @param tree The tree with set branch lengths
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use
	 * @param tocd The sample information object relating sequences to time or order
	 * @param optimiseModel if true the model is optimised as well
	 * @param rateStore storage space for the mutation rate, the initial value is used as the starting rate in the optimisation
	 * @return The resulting optimised tree
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseSRDT(Tree tree, Alignment alignment, SubstitutionModel model, TimeOrderCharacterData tocd, boolean optimiseModel, double[] rateStore) {
	  ConstantMutationRate cm = new ConstantMutationRate(rateStore[0], tocd.getUnits(),1);
		DataTranslator dt = new DataTranslator(alignment);
		alignment = dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
		MutationRateModelTree mt = new MutationRateModelTree( tree,tocd, cm);
		if(optimiseModel) {
			LikelihoodOptimiser.optimiseCombined(mt, alignment, model,
																					 new OrthogonalSearch(), 6, 6);
		} else {
			LikelihoodOptimiser.optimiseTree(mt, alignment, model, new OrthogonalSearch(), 6, 6);
		}
		rateStore[0] = cm.getMu();
		return new SimpleTree(mt);
	}
	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with under an assumption of a molecular clock with serially sampled data and multiple mutation rate parameters, mu - one for each sampling interval. The topology is unchanged.
	 * @param tree The tree with set branch lengths
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use
	 * @param tocd The sample information object relating sequences to time or order
	 * @param optimiseModel if true the model is optimised as well
	 * @param rateStore storage space for the mus, the initial values are used as the starting mus in the optimisation
	 * @return The resulting optimised tree
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseMRDT(Tree tree, Alignment alignment, SubstitutionModel model, TimeOrderCharacterData tocd, boolean optimiseModel, double[] rateStore) {
	  SteppedMutationRate smr = new SteppedMutationRate(pal.misc.Utils.getCopy(rateStore), tocd);
		DataTranslator dt = new DataTranslator(alignment);
		alignment = dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
		MutationRateModelTree mt = new MutationRateModelTree( tree,tocd, smr);
		if(optimiseModel) {
			LikelihoodOptimiser.optimiseCombined(mt, alignment, model,
																					 new OrthogonalSearch(), 6, 6);
		} else {
			LikelihoodOptimiser.optimiseTree(mt, alignment, model, new OrthogonalSearch(), 6, 6);
		}
		smr.getMus(rateStore);
		return new SimpleTree(mt);
	}
	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with under an assumption of a molecular clock with serially sampled data and multiple mutation rate parameters, mu,  over general time intervals. The topology is unchanged.
	 * @param tree The tree with set branch lengths
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use
	 * @param tocd The sample information object relating sequences to time or order
	 * @param optimiseModel if true the model is optimised as well
	 * @param rateChangeTimes the times (as related to the sample information) of when a new mu is used (should be of length mus.length -1 )
	 * @param rateStore storage space for the mus, the initial values are used as the starting mus in the optimisation
	 * @return The resulting optimised tree
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseMRDT(Tree tree, Alignment alignment, SubstitutionModel model, TimeOrderCharacterData tocd, boolean optimiseModel, double[] rateChangeTimes, double[] rateStore) {
		DataTranslator dt = new DataTranslator(alignment);
		alignment = dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
		SteppedMutationRate smr = new SteppedMutationRate(pal.misc.Utils.getCopy(rateStore), pal.misc.Utils.getCopy(rateChangeTimes), tocd.getUnits(),false,tocd.getSuggestedMaximumMutationRate()*2);
		MutationRateModelTree mt = new MutationRateModelTree( tree,tocd, smr);
		if(optimiseModel) {
			LikelihoodOptimiser.optimiseCombined(mt, alignment, model,
																					 new OrthogonalSearch(), 6, 6);
		} else {
			LikelihoodOptimiser.optimiseTree(mt, alignment, model, new OrthogonalSearch(), 6, 6);
		}
		smr.getMus(rateStore);
		return new SimpleTree(mt);
	}
	/**
	 * Creates a new alignment that has a compatible data type with a substution model (needed for likelihood stuff)
	 * @param alignment The base alignment
	 * @param model The substitution model that will be used with the alignment data
	 * @return An appropriately converted alignment
	 * @note this is also neccessary if the alignment uses IUPACNucleotides and the model is Nucleotides
	 */
	public static final Alignment getMatchingDataType(Alignment alignment, SubstitutionModel model) {
	  DataTranslator dt = new DataTranslator(alignment);
		return dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
	}

}