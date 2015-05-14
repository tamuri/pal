// RootedTreeInterface.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

/**
 * <p>Title: RootedTreeInterface </p>
 * <p>Description: An interface to construction of a rooted Tree</p>
 * @author Matthew Goode
 * @version 1.0
 */

public interface RootedTreeInterface {

	public RNode createRoot();

	// ================
	// == Instructee ==
	// ================
	public static interface Instructee {
		public void instruct(RootedTreeInterface treeInterface);
	}

	// ==========
	// == Node ==
	// ==========
	public static interface RNode {
		/**
		 * Get the parent branch (or null if the root)
		 * @return
		 */
		public RBranch getParentRBranch();
		public void setLabel(String label);

		public void setAnnotation(Object annotation);

		public void resetChildren();
		/**
		 * Create a child that is further from the base
		 * @return
		 */
		public RNode createRChild();

	}

	// ============
	// == Branch ==
	// ============
	public static interface RBranch  {
		public void setLength(double length);
		public void setAnnotation(Object annotation);
		public RNode getMoreRecentNode();
		public RNode getLessRecentNode();
	}

	public static final class Utils {
		/**
		 * Recursively build tree
		 * @param palNode
		 * @param displayNode
		 */
		private final static void create(Node palNode, RNode rNode) {

			int numberOfChildren = palNode.getChildCount();
		  rNode.resetChildren();
			if(numberOfChildren==0) {
			  rNode.setLabel(palNode.getIdentifier().getName());
			} else {
				for( int i = 0; i<numberOfChildren; i++ ) {
					Node palChild = palNode.getChild( i );
					RNode displayChild = rNode.createRChild();
					RBranch b = displayChild.getParentRBranch();
					b.setLength( palChild.getBranchLength() );
					create( palChild, displayChild );
				}
			}
		}
		/**
		 * Build a tree display based on a normal pal node.
		 * @param root
		 * @param display
		 */
		public static final void instruct(Node palRoot, RootedTreeInterface treeInterface) {
		  create(palRoot,treeInterface.createRoot());
		}
	}
}
