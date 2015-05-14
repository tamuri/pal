// LikelihoodEvaluator.java
//
// (c) 2000-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.statistics;

/**
 * <p>Title: LikelihoodEvalutator </p>
 * <p>Description: A base class for evaluating the likelihood</p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.tree.*;
import pal.alignment.*;
import pal.substmodel.*;
import pal.treesearch.*;
import pal.util.*;

public interface LikelihoodEvaluator {
	public MLResult getMLOptimised(Tree tree, Alignment alignment, AlgorithmCallback callback);
	public double calculateLikelihood(Tree tree, Alignment alignment);

	// =========================================================================================

	public static interface MLResult {
		public double getLogLikelihood();
		public Tree getOptimisedTree();
	}
	public static final class SimpleMLResult implements MLResult {
	  private final double logLikelihood_;
		private final Tree optimisedTree_;
		public SimpleMLResult(double logLikelihood, Tree optimisedTree) {
		  this.logLikelihood_ = logLikelihood;
			this.optimisedTree_ = optimisedTree;
		}
		public double getLogLikelihood() { return logLikelihood_; }
		public Tree getOptimisedTree() { return optimisedTree_; }
	}

	// Utility Class
	public static final class Utils {
		/**
		 * Create a simple evaluator that uses UnrootedTreeSearch
		 * @param model The substitution model to use
		 * @return an appropriate LikelihoodEvaluator
		 */
		public static final LikelihoodEvaluator createSimpleEvaluator(SubstitutionModel model) {
		  return new SimpleLikelihoodEvaluator(model,0.000001);
		}

		// =================
		private final static class SimpleLikelihoodEvaluator implements LikelihoodEvaluator {
		  private final SubstitutionModel model_;
			private final double likelihoodConvergenceTolerance_;
			public SimpleLikelihoodEvaluator(SubstitutionModel model,double likelihoodConvergenceTolerance) {
			  this.model_ = model;
				this.likelihoodConvergenceTolerance_ = likelihoodConvergenceTolerance;
			}
			public MLResult getMLOptimised(Tree tree, Alignment alignment, AlgorithmCallback callback) {
				UnrootedMLSearcher searcher = new UnrootedMLSearcher(tree,alignment,model_);
				double logLikelihood = searcher.simpleOptimiseLikelihood(likelihoodConvergenceTolerance_,callback);
				return new SimpleMLResult(logLikelihood,searcher.buildPALTree());
			}
			public double calculateLikelihood(Tree tree, Alignment alignment) {
				UnrootedMLSearcher searcher = new UnrootedMLSearcher(tree,alignment,model_);
				return searcher.calculateLogLikelihood();
			}
		}
	}

}