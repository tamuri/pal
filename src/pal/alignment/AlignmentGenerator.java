// AlignmentGenerator.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.util.*;

/**
 * A AlignmentGenerator is an object that can generate alignments! (most probably replicates for statistical tests)
 *
 * @version $Id: AlignmentGenerator.java,v 1.2 2004/04/28 01:03:14 matt Exp $
 *
 * @author Matthew Goode
 */

public interface AlignmentGenerator {
	public Alignment getNextAlignment( AlgorithmCallback callback);

	// =====================================================================================
	// ============= Utilities =============================================================
	// =====================================================================================
	public static final class Utils {
	  public static final AlignmentGenerator createBootstrapGenerator(Alignment baseAlignment) {
		  return new BootstrapGenerator(baseAlignment);
		}
		//===========================================================================
		private static final class BootstrapGenerator implements AlignmentGenerator {
			private final Alignment baseAlignment_;
			public BootstrapGenerator(Alignment baseAlignment) {
				this.baseAlignment_ = baseAlignment;
			}
			public Alignment getNextAlignment( AlgorithmCallback callback) {
			  return new BootstrappedAlignment(baseAlignment_);
			}
		}
	}
}
