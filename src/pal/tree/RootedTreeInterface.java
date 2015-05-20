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
 *
 * @author Matthew Goode
 * @version 1.0
 */

public interface RootedTreeInterface {

    RNode createRoot();

    // ================
    // == Instructee ==
    // ================
    interface Instructee {
        void instruct(RootedTreeInterface treeInterface);
    }

    // ==========
    // == Node ==
    // ==========
    interface RNode {
        /**
         * Get the parent branch (or null if the root)
         *
         * @return
         */
        RBranch getParentRBranch();

        void setLabel(String label);

        void setAnnotation(Object annotation);

        void resetChildren();

        /**
         * Create a child that is further from the base
         *
         * @return
         */
        RNode createRChild();

    }

    // ============
    // == Branch ==
    // ============
    interface RBranch {
        void setLength(double length);

        void setAnnotation(Object annotation);

        RNode getMoreRecentNode();

        RNode getLessRecentNode();
    }

    final class Utils {
        /**
         * Recursively build tree
         */
        private static void create(Node palNode, RNode rNode) {

            int numberOfChildren = palNode.getChildCount();
            rNode.resetChildren();
            if (numberOfChildren == 0) {
                rNode.setLabel(palNode.getIdentifier().getName());
            } else {
                for (int i = 0; i < numberOfChildren; i++) {
                    Node palChild = palNode.getChild(i);
                    RNode displayChild = rNode.createRChild();
                    RBranch b = displayChild.getParentRBranch();
                    b.setLength(palChild.getBranchLength());
                    create(palChild, displayChild);
                }
            }
        }

        /**
         * Build a tree display based on a normal pal node.
         */
        public static final void instruct(Node palRoot, RootedTreeInterface treeInterface) {
            create(palRoot, treeInterface.createRoot());
        }
    }
}
