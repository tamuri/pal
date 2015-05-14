// DistanceMatrixAccess.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.distance;

/**
 * Title:        DistanceMatrixAccess
 * Description:  A means for accessing a DistanceMatrix
 * @author Matthew Goode
 * @version 1.0
 * @note originally part of DistanceMatrixSource
 * @see DistanceMatrixGenerator
 */
import pal.alignment.*;
import pal.substmodel.*;
import pal.util.*;

public interface DistanceMatrixAccess {
	public DistanceMatrix obtainMatrix(AlgorithmCallback callback);

// ============================================================================
	public static final class Utils {
		public static DistanceMatrixAccess createSimple(DistanceMatrix base) {
			return new Simple(base);
		}
		public static DistanceMatrixAccess createEvolutionary(Alignment a, SubstitutionModel sm) {
			return new Evolutionary(a,sm);
		}

		// - - - - -- - - - -- - - - - - -- - - - -- - - - -- - - - - -- - - - --
		private static final class Simple implements DistanceMatrixAccess {
			private final DistanceMatrix base_;
			public Simple(DistanceMatrix base) { this.base_ = base;		}
			public DistanceMatrix obtainMatrix(AlgorithmCallback callback) { return base_;	}
		} //End of class Simple
		// - - - - -- - - - -- - - - - - -- - - - -- - - - -- - - - - -- - - - --
		private static final class Evolutionary implements DistanceMatrixAccess {
			private final Alignment alignment_;
			private final SubstitutionModel model_;
			public Evolutionary(Alignment alignment, SubstitutionModel model) {
				this.alignment_ = alignment;	this.model_ = model;
			}
			public DistanceMatrix obtainMatrix(AlgorithmCallback callback) {
				return new AlignmentDistanceMatrix(SitePattern.getSitePattern(alignment_),model_, callback);
			}
		} //End of class Evolutionary
	} //End of class Utils
}