// DistanceMatrixGenerator.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.distance;

/**
 * Title:        DistanceMatrixGenerator
 * Description:  A method for obtaining a series of Distance Matrixs
 *               Differs from DistanceMatrixAccess as generates different DistanceMatrix objects
 * @author Matthew Goode
 * @version 1.0
 * @note originally part of DistanceMatrixSource
 * @see DistanceMatrixAccess
 */
import pal.alignment.*;
import pal.substmodel.*;
import pal.tree.*;
import pal.util.*;

public interface DistanceMatrixGenerator {
	public DistanceMatrix generateNextMatrix(AlgorithmCallback callback);

// ============================================================================
	public static final class Utils {
		public static DistanceMatrixGenerator createEvolutionary(Alignment a, SubstitutionModel sm) {
			return new Evolutionary(a,sm);
		}
		/**
		 * Silly idea stuff
		 */
		public static DistanceMatrixGenerator createParametric(Tree baseTree, SubstitutionModel sm, int numberOfSites) {
			return new Parametric(baseTree,sm,numberOfSites);
		}

		// - - - - -- - - - -- - - - - - -- - - - -- - - - -- - - - - -- - - - --
		private static final class Parametric implements DistanceMatrixGenerator {
			private final Tree baseTree_;
			private final SubstitutionModel evolutionaryModel_;
			private final int numberOfSites_;
			public Parametric(Tree baseTree, SubstitutionModel evolutionaryModel, int numberOfSites) {
				this.numberOfSites_ = numberOfSites;
				this.baseTree_ = baseTree;
				this.evolutionaryModel_ = evolutionaryModel;
			}
			public DistanceMatrix generateNextMatrix(AlgorithmCallback callback) {
				SimulatedAlignment sa = new SimulatedAlignment(numberOfSites_,baseTree_,evolutionaryModel_);
				return new AlignmentDistanceMatrix(SitePattern.getSitePattern(sa),evolutionaryModel_,callback);
			}
		}
		// - - - - -- - - - -- - - - - - -- - - - -- - - - -- - - - - -- - - - --
		private static final class Evolutionary implements DistanceMatrixGenerator {
			private final Alignment alignment_;
			private final SubstitutionModel model_;
			public Evolutionary(Alignment alignment, SubstitutionModel model) {
				this.alignment_ = alignment;
				this.model_ = model;
			}
			public DistanceMatrix generateNextMatrix(AlgorithmCallback callback) {
			  return
					new
						AlignmentDistanceMatrix(
							SitePattern.getSitePattern(
								new BootstrappedAlignment(alignment_)
							),
							model_,callback);
			}

		}

	}
}