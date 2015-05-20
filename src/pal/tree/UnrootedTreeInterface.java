// UnrootedTreeInterface.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

/**
 * <p>Title: UnrootedTreeInterface </p>
 * <p>Description: An interface to construction of an Unrooted Tree</p>
 *
 * @author Matthew Goode
 * @version 1.0
 */

public interface UnrootedTreeInterface {

    BaseBranch createBase();

    // ================
    // == Instructee ==
    // ================
    interface Instructee {
        void instruct(UnrootedTreeInterface treeInterface);
    }

    // ==========
    // == Node ==
    // ==========
    interface UNode {
        /**
         * Get the branch closest to the base (or maybe even the base)
         *
         * @return
         */
        UBranch getParentUBranch();

        void setLabel(String label);


        void setAnnotation(Object annotation);


        void resetChildren();

        /**
         * Create a child that is further from the base
         *
         * @return
         */
        UNode createUChild();

    }

    // ====================
    // == General Branch ==
    // ====================
    interface GeneralBranch {
        void setLength(double length);

        void setAnnotation(Object annotation);
    }

    // ============
    // == Branch ==
    // ============
    interface UBranch extends GeneralBranch {
        UNode getCloserNode();

        UNode getFartherNode();
    }
    // =======================
    // == Idea Base Branch ==
    // =======================

    interface BaseBranch extends GeneralBranch {
        UNode getLeftNode();

        UNode getRightNode();
    }

    final class Utils {
        /**
         * Recursively build tree
         *
         * @param palNode
         * @param displayNode
         */
        private static void create(Node palNode, UNode uNode) {

            int numberOfChildren = palNode.getChildCount();
            uNode.resetChildren();
            if (numberOfChildren == 0) {
                uNode.setLabel(palNode.getIdentifier().getName());
            } else {
                for (int i = 0; i < numberOfChildren; i++) {
                    Node palChild = palNode.getChild(i);
                    UNode displayChild = uNode.createUChild();
                    UBranch b = displayChild.getParentUBranch();
                    b.setLength(palChild.getBranchLength());
                    create(palChild, displayChild);
                }
            }
        }

        /**
         * Build a tree display based on a normal pal node.
         *
         * @param root
         * @param display
         */
        public static final void instruct(Node root, UnrootedTreeInterface treeInterface) {
            if (root.getChildCount() != 2) {
                root = new TreeManipulator(root).getMidPointRooted();
            }
            BaseBranch b = treeInterface.createBase();
            Node palLeft = root.getChild(0);
            Node palRight = root.getChild(1);
            b.setLength(palLeft.getBranchLength() + palRight.getBranchLength());
            create(palLeft, b.getLeftNode());
            create(palRight, b.getRightNode());

        }
    }
}
