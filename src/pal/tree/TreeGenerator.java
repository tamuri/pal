// TreeGenerator.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

/**
 * An interface for classes that produce trees. Useful for cases where large numbers of
 * trees are generator (like bootstrapping) but, to save memory, it is better to
 * generate the trees on the fly, instead of pregenerating them and storing in an
 * array.
 *
 * @version $Id: TreeGenerator.java,v 1.4 2004/05/04 02:43:28 matt Exp $
 *
 * @author Matthew Goode
 */
import pal.util.AlgorithmCallback;
import pal.distance.*;

public interface TreeGenerator {
	public Tree getNextTree( AlgorithmCallback callback);
	// ==============================================================================
	// ==================== Utilities ===============================================
	// ==============================================================================
	public static final class Utils {
	  public static final TreeGenerator createNeighbourJoiningGenerator(DistanceMatrixGenerator dataGenerator, String[] outgroupNames) {
	    return new NJGenerator(dataGenerator,outgroupNames);
		}
		// ==================================================
		// === NJ Generator
		// ==================================================
		private static final class NJGenerator implements TreeGenerator {
			private final DistanceMatrixGenerator dataGenerator_;
			private final String[] outgroupNames_;
		  public NJGenerator( DistanceMatrixGenerator dataGenerator, String[] outgroupNames ) {
			  this.dataGenerator_ = dataGenerator;
				this.outgroupNames_ = outgroupNames;
			}
			public Tree getNextTree( AlgorithmCallback callback) {
			  DistanceMatrix dm = dataGenerator_.generateNextMatrix(callback);
				Tree t = new NeighborJoiningTree(dm);
				TreeManipulator tm = new TreeManipulator(t,TreeManipulator.REDUCE_CONSTRUCTION);
				return tm.getTreeRootedBy(outgroupNames_);
			}
		} //End of class NJGenerator
 	} //End of class Utils
} //End of interface TreeGenerator
