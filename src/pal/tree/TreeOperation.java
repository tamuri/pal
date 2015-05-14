// TreeOperation.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

/**
 * <p>Title: TreeOperation</p>
 * <p>Description: A class that creates an altered tree base on input tree. This isn't used by much, SerialCoalescentGenerator.</p>
 * @version $Id: TreeOperation.java,v 1.1 2003/10/19 02:35:26 matt Exp $
 * @author Matthew Goode
 * @version 1.0
 */

public interface TreeOperation {
	/**
	 * Operates on input tree. Users can assume that the input tree is not altered,
	 * but should allow that the result tree may just be the input tree!
	 * @param tree The input tree.
	 * @return the A new tree, or the input tree
	 */
	public Tree operateOn(Tree tree);

// -==--=-=-=======-==--=-=-=-=-=-=-==-=--==--=-=-==-=--==-=-=--=-==--=-=

	/**
	 * Utility class
	 */
	public static final class Utils {
		/**
		 * Create a tree operation that scales the input tree and changes the units
		 * @param scaleFactor The scaling to be done
		 * @param resultingUnits The new units
		 * @return A Tree Operation
		 */
		public static final TreeOperation createScale(double scaleFactor, int resultingUnits) {
			return new Scale(scaleFactor, resultingUnits);
		}
		/**
		 * Creates a tree operation that first applies one tree operation and then applies a second operation to get it's result
		 * @param first the first operation to apply
		 * @param second the operation to apply on the result of the first
		 * @return A tree operation
		 */
		public static final TreeOperation createPipeline(TreeOperation first, TreeOperation second) {
			return new Pipeline(first, second);
		}
		public static final TreeOperation getNoOperation() { return NOP.INSTANCE; }

	// ========

		private static final class NOP implements TreeOperation {
			public static final TreeOperation INSTANCE = new NOP();
			public Tree operateOn(Tree tree) { return tree; }
		}
		private static final class Scale implements TreeOperation {
			private final double scaleFactor_;
			private final int resultingUnits_;
			public Scale(double scaleFactor, int resultingUnits) {
				this.scaleFactor_ = scaleFactor;
				this.resultingUnits_ = resultingUnits;
			}
			public Tree operateOn(Tree tree) {
				return TreeUtils.getScaled(tree,scaleFactor_,resultingUnits_);
			}

		}
		// ========
		private static final class Pipeline implements TreeOperation {
			private final TreeOperation first_, second_;
			public Pipeline(TreeOperation first, TreeOperation second) {
				this.first_ = first; this.second_ = second;
			}
			public Tree operateOn(Tree tree) {
				return second_.operateOn(first_.operateOn(tree));
			}
		}
	}

}