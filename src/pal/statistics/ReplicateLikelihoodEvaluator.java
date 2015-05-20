// ReplicateLikelihoodEvaluator.java
//
// (c) 2000-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.statistics;

/**
 * <p>Title: Replicate Likelihood Evaluator</p>
 * <p>Description: A method for evaluating the likelihood of a replicate</p>
 *
 * @author Matthew Goode
 * @version 1.0
 */

import pal.alignment.Alignment;
import pal.substmodel.SubstitutionModel;
import pal.tree.Tree;
import pal.treesearch.UnrootedMLSearcher;

public interface ReplicateLikelihoodEvaluator {
    double getReplicateLogLikelihood(Tree tree, Alignment alignment);

    //Utility Class
    final class Utils {
        /**
         * Create a ReplicateLikelihoodEvaluator that based likelihood on original tree (does no optimisation)
         * @param model The subtitution model to use
         * @return an appropriate evaluator
         */
        public static final ReplicateLikelihoodEvaluator createRELLEvaluator(SubstitutionModel model) {
            return new RELLEvaluator(model);
        }

        // RELLEvaluator
        private final static class RELLEvaluator implements ReplicateLikelihoodEvaluator {
            private final SubstitutionModel model_;

            public RELLEvaluator(SubstitutionModel model) {
                this.model_ = model;
            }

            public double getReplicateLogLikelihood(Tree tree, Alignment alignment) {
                UnrootedMLSearcher searcher = new UnrootedMLSearcher(tree, alignment, model_);
                return searcher.calculateLogLikelihood();
            }
        }
    }
}