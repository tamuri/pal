// TreeResricter.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
package pal.tree;

/**
 * <p>Title: TreeRestricter</p>
 * <p>Description: Creates trees based on restircting the leaf nodes. Preserves remaining branchlengths</p>
 * <author Matthew Goode
 * @version 1.0
 */
import pal.misc.Identifier;
public class TreeRestricter {
	private final RNode root_;
	private final int units_;
	/**
	 * @param root The root of the tree
	 * @param units the units of the tree
	 * @param names the names of the nodes to either exclude, or include
	 * @param inclusion if true the names supplied mark leaves to include, else mark leaves to exclude
	 */
	public TreeRestricter(Node root, int units, String[] names, boolean inclusion) {
		this.root_ = construct(root, names, inclusion,true);
		this.units_ = units;
	}
	/**
	 * @param root The root of the tree
	 * @param units the units of the tree
	 * @param names the names of the nodes to either exclude, or include
	 * @param inclusion if true the names supplied mark leaves to include, else mark leaves to exclude
	 */
	public TreeRestricter(Tree t, String[] names, boolean inclusion) {
		this(t.getRoot(),t.getUnits(),names,inclusion);
	}
	private static final boolean isAccept(String query, String[] names, boolean inclusion) {
		boolean found = false;
		for(int i = 0 ; i < names.length ; i++) {
			if(query.equals(names[i])) {
				found = true;
				break;
			}
		}
		if(inclusion) { return found; }
		return !found;
	}
	public final Node generateNode() {
		return root_.constructPAL();
	}
	public final Tree generateTree() {
		SimpleTree st = new SimpleTree(root_.constructPAL());
		st.setUnits(units_);
		return st;
	}

	public final RNode construct(Node peer, String[] names, boolean inclusion, boolean isRoot) {
		if(peer.isLeaf()) {
			String leafName = peer.getIdentifier().getName();
			if(isAccept(leafName,names,inclusion)) {
				return new LeafNode(leafName, peer.getBranchLength());
			}
			return null;
		}
		final double branchLength = (isRoot ? 0 : peer.getBranchLength());
		final int childCount = peer.getChildCount();
		if(childCount==2) {
			RNode left = construct(peer.getChild(0), names,inclusion,false);
			RNode right = construct(peer.getChild(1), names,inclusion,false);
			if(left==null) {
				if(right==null) { return null; }
				if(isRoot) {
					right.zeroBranchLength();
				} else {
					right.increaseBranchLength(branchLength);
				}
				return right;
			} else {
				if(right==null) {
					if(isRoot) {
						left.zeroBranchLength();
					} else {
						left.increaseBranchLength(branchLength);
					}
					return left;
				}
				return new InternalNode(left, right, branchLength);
			}
		}
		int numberOfChildrenFound = 0;
		RNode[] children = new RNode[childCount];
		for(int i = 0 ; i < childCount ; i++) {
			RNode c = construct(peer.getChild(i),names,inclusion,false);
			if(c!=null) {
				children[numberOfChildrenFound++] = c;
			}
		}
		if(numberOfChildrenFound==0) {	return null;	}
		if(numberOfChildrenFound==1) {
			children[0].increaseBranchLength(branchLength);
			return children[0];
		}
		RNode[] realChildren = new RNode[numberOfChildrenFound];
		System.arraycopy(children,0,realChildren,0,numberOfChildrenFound);
		return new InternalNode(realChildren,branchLength);
	}
	private static interface RNode {
		public Node constructPAL();
		public void increaseBranchLength(double amount);
		public void zeroBranchLength();
	}
	private static final class LeafNode implements RNode {
		private final String leafID_;
		private double branchLength_;
		public LeafNode(String leafID, double branchLength) {
			this.leafID_ = leafID;
			this.branchLength_ = branchLength;
		}
		public void increaseBranchLength(double amount) {
			branchLength_+=amount;
		}
		public void zeroBranchLength() { this.branchLength_ = 0; }

		public Node constructPAL() {
			return NodeFactory.createNodeBranchLength(branchLength_, new Identifier(leafID_));
		}
	}
	private static final class InternalNode implements RNode {
		private final RNode[] children_;
		private double branchLength_;
		public InternalNode(RNode left, RNode right, double branchLength) {
			this(new RNode[] { left, right }, branchLength);
		}
		public InternalNode(RNode[] children, double branchLength) {
			this.children_ = children;
			this.branchLength_ = branchLength;
		}
		public void zeroBranchLength() { this.branchLength_ = 0; }

		public void increaseBranchLength(double amount) {
			branchLength_+=amount;
		}
		public Node constructPAL() {
			Node[] palChildren = new Node[children_.length];
			for(int i = 0 ; i < palChildren.length ; i++) {
				palChildren[i] = children_[i].constructPAL();
			}
			return NodeFactory.createNodeBranchLength(branchLength_, palChildren);
		}

	}
}