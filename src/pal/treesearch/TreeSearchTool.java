// TreeSearchTool.java
//
// (c) 1999-2003 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

import pal.algorithmics.*;
import pal.alignment.*;
import pal.datatype.*;
import pal.eval.*;
import pal.math.*;
import pal.substmodel.*;
import pal.tree.*;
import pal.util.*;
import pal.misc.*;

/**
 * <p>Title: TreeSearchTool</p>
 * <p>Description:
 * An access point for a simple and stable interface to tree search methods
 * </p>
 * @author Matthew Goode
 * @version 1.0
 */
public final class TreeSearchTool {
	/**
	 * Do a basic tree search using maximum likelihood on an unrooted tree space, with a given starting tree
	 * @param baseTree The starting tree (which may or may not be unrooted - will be unrooted)
	 * @param a The related alignment, OTU labels must match that of the tree
	 * @param sm The related substitution model. The data type of the model should match that of the alignment. The parameters of this model may change if the optimise model is choosen.
	 * @param optimiseModel If true the substitution model will be optimised concurrently with the tree. This will change in the input model.
	 * @return The optimised tree (the original tree is *not* altered)
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public Tree basicUnrootedTreeMLSearch(Tree baseTree, Alignment a, SubstitutionModel sm, boolean optimiseModel) {
	  return basicUnrootedTreeMLSearch(baseTree,a,sm,optimiseModel,AlgorithmCallback.Utils.getNullCallback());
	}
	/**
	 * Do a basic tree search using maximum likelihood on an unrooted tree space, with a given starting tree
	 * @param baseTree The starting tree (which may or may not be unrooted - will be unrooted)
	 * @param a The related alignment, OTU labels must match that of the tree
	 * @param sm The related substitution model. The data type of the model should match that of the alignment. The parameters of this model may change if the optimise model is choosen.
	 * @param optimiseModel If true the substitution model will be optimised concurrently with the tree. This will change in the input model.
	 * @param callback An AlgorithmCallback object used to control and monitor the search progress
	 * @return The optimised tree (the original tree is *not* altered)
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public Tree basicUnrootedTreeMLSearch(Tree baseTree, Alignment a, SubstitutionModel sm, boolean optimiseModel, AlgorithmCallback callback) {
		UnrootedMLSearcher ut =
          new UnrootedMLSearcher( baseTree.getRoot(), a, sm , SimpleModelFastFourStateLHCalculator.getFactory());
	  return doBasicMLSearch(ut,optimiseModel,callback);

	}
	/**
	 * Do a basic tree search using maximum likelihood on an unrooted tree space, without a given starting tree
	 * @param a The related alignment, OTU labels must match that of the tree
	 * @param sm The related substitution model. The data type of the model should match that of the alignment. The parameters of this model may change if the optimise model is choosen.
	 * @param optimiseModel If true the substitution model will be optimised concurrently with the tree. This will change in the input model.
	 * @return The optimised tree
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public Tree basicUnrootedTreeMLSearch(Alignment a, SubstitutionModel sm, boolean optimiseModel) {
	  return basicUnrootedTreeMLSearch(a,sm,optimiseModel,AlgorithmCallback.Utils.getNullCallback());
	}
	/**
	 * Do a basic tree search using maximum likelihood on an unrooted tree space, without a given starting tree
	 * @param a The related alignment, OTU labels must match that of the tree
	 * @param sm The related substitution model. The data type of the model should match that of the alignment. The parameters of this model may change if the optimise model is choosen.
	 * @param optimiseModel If true the substitution model will be optimised concurrently with the tree. This will change in the input model.
	 * @param callback An AlgorithmCallback object used to control and monitor the search progress
	 * @return The optimised tree
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public Tree basicUnrootedTreeMLSearch(Alignment a, SubstitutionModel sm, boolean optimiseModel, AlgorithmCallback callback) {
			UnrootedMLSearcher ut =
				new UnrootedMLSearcher( a, sm, SimpleModelFastFourStateLHCalculator.getFactory() );
	  return doBasicMLSearch(ut,optimiseModel,callback);
	}
	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with no constraints on the branchlengths (as for an unrooted tree). The topology is unchanged.
	 * No topology changes are made!
	 * @param tree The tree (remains unchanged)
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use (is changed if optimisation of the model is choosen)
	 * @param optimiseModel if true the model is also optimised, otherwise just the tree
	 * @return The optimised tree
	 * @note this performs the same operation as LikelihoodTool.optimiseUnrooted(), but potentially uses alternative and faster code
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseUnrootedFixed(Tree tree, Alignment alignment, SubstitutionModel model, boolean optimiseModel) {
	  return optimiseUnrootedFixed(tree,alignment,model,optimiseModel,AlgorithmCallback.Utils.getNullCallback());
	}
	/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with no constraints on the branchlengths (as for an unrooted tree). The topology is unchanged.
	 * No topology changes are made!
	 * @param tree The tree (remains unchanged)
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use (is changed if optimisation of the model is choosen)
	 * @param optimiseModel if true the model is also optimised, otherwise just the tree
	 * @param callback An algorithm callback object for monitoring process of search algorithm
	 * @return The optimised tree
	 * @note this performs the same operation as LikelihoodTool.optimiseUnrooted(), but potentially uses alternative and faster code
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseUnrootedFixed(Tree tree, Alignment alignment, SubstitutionModel model, boolean optimiseModel, AlgorithmCallback callback) {
		UnrootedMLSearcher ut =
				new UnrootedMLSearcher( tree, alignment, model );
		SearchEngine searcher = new SearchEngine( ProbabilityIterator.Utils.getHillClimb() );
		final Ranker r = new Ranker(1);
	  final UndoableAction action;
	  if(optimiseModel) {
		  action	= UndoableAction.Utils.getCombined(new UndoableAction[] {
					ut.getBranchLengthOptimiseAction( StoppingCriteria.Utils.getIterationCount( 2 ) ),
					ut.getModelOptimiseAction( new OrthogonalSearch(), MinimiserMonitor.Utils.createSystemOuptutMonitor(), 6, 6 )
			  } );
		} else {
		  action = 	ut.getBranchLengthOptimiseAction( StoppingCriteria.Utils.getIterationCount( 1 ) );
		}
		GeneralObjectState gos =
			new GeneralObjectState(action, ut, true );
    searcher.run(
          callback,  Double.NEGATIVE_INFINITY, gos,
          StoppingCriteria.Utils.getNonExactUnchangedScore( 3, false, 0.00001 ),
//				  StoppingCriteria.Utils.getIterationCount(30),
				  r
        );
    return ut.buildPALTree();
	}

/**
	 * Optimise the branches of a tree with regard to maximum likelihood, with the contraints of a global molecular clock - that is, all the tips terminate at the same point.
	 * The topology is unchanged -no topology changes are made!
	 * @param tree The tree (remains unchanged) - should be rooted
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use (is changed if optimisation of the model is choosen)
	 * @param optimiseModel if true the model is also optimised, otherwise just the tree
	 * @param callback An algorithm callback object for monitoring process of search algorithm
	 * @return The optimised tree
	 * @note this performs the same operation as LikelihoodTool.optimiseClockConstrained(), but potentially uses alternative and faster code
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static Tree optimiseClockConstrainedFixed(Tree tree, Alignment alignment, SubstitutionModel model, boolean optimiseModel, AlgorithmCallback callback) {
		GeneralLikelihoodSearcher gls =
				new GeneralLikelihoodSearcher(
				  tree.getRoot(), alignment,
					new GlobalClockModel(
					  SimpleMolecularClockLikelihoodModel.createInstance(
						  SimpleModelFastFourStateLHCalculator.getFactory()
							,model
						)
					)
				);
		StoppingCriteria stopper = StoppingCriteria.Utils.getNonExactUnchangedScore( 10, false, 0.0001 ).newInstance();
	  if(optimiseModel) {
			gls.optimiseAllPlusSubstitutionModel(
					stopper,
					new ConjugateDirectionSearch(),
					new ConjugateDirectionSearch(),
					6,6,
					callback,
					SearchMonitor.Utils.createNullMonitor(),
					3,
					MinimiserMonitor.Utils.createNullMonitor(), MinimiserMonitor.Utils.createNullMonitor()
				);
		} else {
			gls.optimiseAllSimple(
					stopper,
					new ConjugateDirectionSearch(),
					6,6,
					callback,
					SearchMonitor.Utils.createNullMonitor(),
					MinimiserMonitor.Utils.createNullMonitor()
				);
		}
    return gls.buildPALTreeES();
	}

	private final static Tree doBasicMLSearch(UnrootedMLSearcher ut,boolean optimiseModel, AlgorithmCallback callback) {
		SearchEngine searcher = new SearchEngine( ProbabilityIterator.Utils.getHillClimb() );
	  Ranker r = new Ranker(1);
	  UndoableAction[] actions;
		double[] proportions;
		if(optimiseModel) {
		  actions = new UndoableAction[] {
								ut.getNNIBranchLengthOptimiseAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getSPRAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getSweepSPRAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getBranchLengthOptimiseAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getModelOptimiseAction( new OrthogonalSearch(), MinimiserMonitor.Utils.createNullMonitor(), 5, 5 )
			};
			proportions = new double[] { 30,20,10,10,10 };
		} else {
		  actions = new UndoableAction[] {
								ut.getNNIBranchLengthOptimiseAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getSPRAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getSweepSPRAction( StoppingCriteria.Utils.getIterationCount( 1 ) ),
								ut.getBranchLengthOptimiseAction( StoppingCriteria.Utils.getIterationCount( 1 ) )
			};
			proportions = new double[] { 30,20,20,10 };
		}
		GeneralObjectState gos =
			new GeneralObjectState(UndoableAction.Utils.getDistributedSelection(actions, proportions ), ut, true );
    searcher.run(
          callback,  Double.NEGATIVE_INFINITY, gos,
          StoppingCriteria.Utils.getNonExactUnchangedScore( 10, false, 0.0001 ), r
        );

    return ut.buildPALTree();
	}
		/**
	 * Creates a new alignment that has a compatible data type with a substution model (needed for likelihood stuff)
	 * @param alignment The base alignment
	 * @param model The substitution model that will be used with the alignment data
	 * @return An appropriately converted alignment
	 * @note this is also neccessary if the alignment uses IUPACNucleotides and the model is Nucleotides. This code also appears in pal.eval.LikelihoodTool
	 */
	public static final Alignment getMatchingDataType(Alignment alignment, SubstitutionModel model) {
	  DataTranslator dt = new DataTranslator(alignment);
		return dt.toAlignment(MolecularDataType.Utils.getMolecularDataType(model.getDataType()),0);
	}

	/**
	 * Calculate the log likelihood of a particular set of phylogenetic data
	 * @param tree The tree with set branch lengths
	 * @param alignment The alignment (sequence names must match tree)
	 * @param model The substitution model to use
	 * @return The log likelihood
	 * @note If the alignment uses IUPACNucleotides and the model uses Nucleotides see getMatchingDataType()
	 */
	public final static double calculateLogLikelihood(Tree tree, Alignment alignment, SubstitutionModel model) {
		UnrootedMLSearcher uml = new UnrootedMLSearcher(tree,alignment,model);
		return uml.calculateLogLikelihood();
	}

}