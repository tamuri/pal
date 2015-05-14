// TreeManimulator.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

/**
 * <b>Was TreeRooter.</b>
 * A class to provide all your tree rooting and unrooting needs plus more. Allows
 * Unrooting, Midpoint Rooting (reasonably efficiently), General Rooting, and
 * obtaining every root. Also allows for the collapsing and uncollapsing of short branches, and the attachment of sub trees.
 *
 * This class replaces methods in TreeUtil (and is more swanky)
 *
 * In general just use the static access methods. (eg TreeManipulator.getUnrooted(myTree); )
 *
 * @version $Id: TreeManipulator.java,v 1.3 2004/08/02 05:22:04 matt Exp $
 *
 * @author Matthew Goode
 * @note REDUCE_CONSTRUCTION functioning (relatively untested) as of 18 September 2003
 *
 * <br><br><em>History</em>
 * <ul>
 *  <li> 18/9/2003 MG:Corrected rooting for complex case, added in getAllRoot methods, REDUCED_CONSTRUCTION stuff working, added in ingroup branch length stuff to rooting (to help make pretty pictures), added getAsInput() methods </li>
 *  <li> 25/10/2003 MG:Fixed bug with EXPAND_CONSTRUCTION on a unrooted tree </li>
 *  <li> 16/4/2003 MG:Changed name (TreeRooter -> TreeManipulator), added branch access stuff
 * </ul>
 */
import pal.util.AlgorithmCallback;
import pal.misc.Units;
import pal.misc.BranchLimits;
import pal.misc.Identifier;

import java.util.ArrayList;


public class TreeManipulator implements UnrootedTreeInterface.Instructee, RootedTreeInterface.Instructee {


	/**
	 * Construct tree with same multification as original
	 */
	public static final int MIMIC_CONSTRUCTION = 100;
	/**
	 * Construct tree, but convert general multifications to a series of bifications
	 */
	public static final int EXPAND_CONSTRUCTION = 200 ;
	/**
	 * Construct tree, but convert bificating nodes that appear as multifications (due to very short branchlengths) to multifications
	 */
	public static final int REDUCE_CONSTRUCTION = 300;

	private Connection unrootedTree_;
	private final int units_;
	/**
	 * Only used by getBinaryTree()
	 */
	private final double firstChildNodeLength_;
	private final boolean inputTreeUnrooted_;

	/**
	 * Construct a TreeManipulator based around a normal tree
	 * @param base The base tree, which can be rooted or unrooted (will be treated as unrooted either way)
	 * @param constructionMode the way in which the internal tree representation is constructed
	 * @note The base tree is never altered
	 */
	public TreeManipulator(Tree base, int constructionMode) {
		this(base.getRoot(), base.getUnits(),constructionMode);
	}
	/**
	 * Construct a TreeManipulator based around a normal tree
	 * @param base The base tree, which can be rooted or unrooted (will be treated as unrooted either way)
	 * @note The base tree is never altered
	 */
	public TreeManipulator(Tree base) {
		this(base.getRoot(), base.getUnits());
	}
	/**
	 * Units will be Units.UNKNOWN
	 */
	public TreeManipulator(Node base) {
		this(base,Units.UNKNOWN);
	}
	/**
	 * Construct a TreeManipulator based around a normal tree
	 * @param base The base tree, which can be rooted or unrooted (will be treated as unrooted either way)
	 * @param units, the units of generated trees. Not really of concern if only Node trees built
	 * @note The base tree is never altered
	 */
	public TreeManipulator(Node base, int units) {
		this(base,units,MIMIC_CONSTRUCTION);
	}
	/**
	 * Construct a TreeManipulator based around a normal tree
	 * @param base The base tree, which can be rooted or unrooted (will be treated as unrooted either way)
	 * @param units, the units of generated trees. Not really of concern if only Node trees built
	 * @note The base tree is never altered
	 */
	public TreeManipulator(Node base, int units, int constructionMode) {
		SimpleNode simpleBase = new PALNodeWrapper(base);
		this.unrootedTree_ = construct(simpleBase, constructionMode);
		this.inputTreeUnrooted_ = base.getChildCount()>2;
		this.firstChildNodeLength_ = base.getChild(0).getBranchLength();
		this.units_ = units;
		this.unrootedTree_.clearPathInfo();
	}


	public TreeManipulator(UnrootedTreeInterface.Instructee base, int units, int constructionMode) {
	  UnrootedInterfaceImpl ui = new UnrootedInterfaceImpl();
		base.instruct(ui);
		SimpleBranch root = ui.getSimpleRootBranch();
		this.unrootedTree_ = new Connection(root,constructionMode);
		this.firstChildNodeLength_ = root.getBranchLength()/2;
		this.units_ = units;
		this.unrootedTree_.clearPathInfo();
		this.inputTreeUnrooted_ = true;
	}

	public TreeManipulator(RootedTreeInterface.Instructee base, int units, int constructionMode) {
	  RootedInterfaceImpl ri = new RootedInterfaceImpl();
		base.instruct(ri);
		SimpleNode root = ri.getSimpleRoot();
		this.unrootedTree_ = construct(root, constructionMode);
		this.inputTreeUnrooted_ = false;
		this.firstChildNodeLength_ = root.getChild(0).getParentBranchLength();
		this.units_ = units;
		this.unrootedTree_.clearPathInfo();

	}
	/**
	 * Attachment constructor
	 * @param base The basis TreeManipulator
	 * @param baseSubTreeConnector The connection in the base that the sub tree will be attached
	 * @param subTree the sub tree to attach
	 * @param constructionMode the construction mode for the new sub tree (construction will match current for other parts of the tree)
	 */
	private TreeManipulator(TreeManipulator base, Connection baseSubTreeConnector, Node subTree, int constructionMode) {
		SimpleNode simpleSubTree = new PALNodeWrapper(subTree);
		this.unrootedTree_ = base.unrootedTree_.getAttached(baseSubTreeConnector,simpleSubTree, constructionMode);
		this.inputTreeUnrooted_ = (base.unrootedTree_==baseSubTreeConnector ? true : base.inputTreeUnrooted_);
		this.firstChildNodeLength_ = base.firstChildNodeLength_;
		this.units_ = base.units_;
		this.unrootedTree_.clearPathInfo();
	}

	private static final Connection construct(SimpleNode n, int constructionMode) {
		if(n.isLeaf()) {
		  throw new IllegalArgumentException("Tree must contain more than a single OTU!");
		}
		if(n.getNumberOfChildren()==2) {
			return new Connection(n.getChild(0), n.getChild(1),constructionMode);
		}

		UndirectedNode un = new UndirectedNode(n,constructionMode);
		return un.getPeerParentConnection();
	}


	/**
	 * @return the MidPoint rooted tree (as root node);
	 */
	public Node getMidPointRooted() {
		Node n = unrootedTree_.getMidPointRooted();
		NodeUtils.lengths2Heights(n);
		return n;
	}
	/**
	 * @return a tree rooted around the node it was originally rooted around (if originally rooted),
	 * @note With
	 */
	public Node getDefaultRoot() {
		Node n = unrootedTree_.getRootedAround(firstChildNodeLength_);
		NodeUtils.lengths2Heights(n);
		return n;
	}

	/**
	 * Tests if the given clade memebers form an exact clade that does not include any other members other
	 * than the ones listed. If there are members that are not actually in the tree, they will be ignored.
	 * @param possibleCladeMembers the names of the members in the clade of interest
	 * @return true if the conditions are met
	 * @note not currently correctly implemented
	 */
	private boolean isFormsFormsExactClade(String[] possibleCladeMembers) {
		return unrootedTree_.isFormsExactClade(possibleCladeMembers);
	}

	/**
	 * A method for recovering the input (construction) tree (with the EXPANSION/MIMIC/REDUCED differences)
	 * @return An unrooted tree if the input tree was unrooted, otherwise the default rooting
	 */
	public Node getAsInputRooting() {
		if(inputTreeUnrooted_) {
			return getUnrooted();
		}
		return getDefaultRoot();
	}
	/**
	 * A method for recovering the input (construction) tree (with the EXPANSION/MIMIC/REDUCED differences)
	 * @return An unrooted tree if the input tree was unrooted, otherwise the default rooting
	 */
	public Tree getAsInputRootingTree() {
		return constructTree(getAsInputRooting(),units_);
	}
	/**
	 * @return a tree rooted around the node it was originally rooted around (if originally rooted),
	 * @note With
	 */
	public Tree getDefaultRootTree() {
		return constructTree(getDefaultRoot(),units_);
	}
	/**
	 * @return the MidPoint rooted tree
	 */
	public Tree getMidPointRootedTree() {
		return constructTree(getMidPointRooted(),units_);
	}

	/**
	 * return unrooted node
	 */
	public Node getUnrooted() {
		Node n = unrootedTree_.getUnrooted();
		NodeUtils.lengths2Heights(n);
		return n;
	}
	/**
	 * return unrooted node
	 */
	public Tree getUnrootedTree() {
		return constructTree(getUnrooted(),units_);
	}

	/**
	 * @return all connections in tree
	 */
	private Connection[] getAllConnections() {
		return unrootedTree_.getAllConnections();
	}

	/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @return the tree rooted by an outgroup defined by the mrca of a set of nodes
	 * @throws IllegalArgument exception if outgroup names does not contain any valid node names
	 * @note If the outgroup is not well defined, this may not be the only rooting
	 */
	public Node getRootedBy(String[] outgroupNames) {
		Node n = unrootedTree_.getRootedAroundMRCA(outgroupNames);
		NodeUtils.lengths2Heights(n);
		return n;
	}
		/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @return the tree rooted by an outgroup defined by the mrca of a set of nodes
	 * @throws IllegalArgument exception if outgroup names does not contain any valid node names
	 * @note If the outgroup is not well defined, this may not be the only rooting
	 */
	public void instructRootedBy(RootedTreeInterface rootedInterface, String[] outgroupNames) {
		unrootedTree_.instructRootedAroundMRCA(rootedInterface, outgroupNames);
	}
	/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @param ingroupBranchLength the maximum length of the branch leading to the ingroup clade
	 * @return the tree rooted by an outgroup defined by the mrca of a set of nodes
	 * @throws IllegalArgument exception if outgroup names does not contain any valid node names
	 * @note If the outgroup is not well defined, this may not be the only rooting
	 */
	public Node getRootedBy(String[] outgroupNames,double ingroupBranchLength) {
		return unrootedTree_.getRootedAroundMRCA(outgroupNames,ingroupBranchLength);
	}

	/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @return all the trees rooted by an outgroup defined by the mrca of a set of nodes
	 * @throws IllegalArgument exception if outgroup names does not contain any valid node names
	 */
	public Node[] getAllRootedBy(String[] outgroupNames) {
		return unrootedTree_.getAllRootedAroundMRCA(outgroupNames);
	}

	/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @return the tree rooted by an outgroup defined by the mrca of a set of nodes
	 * @note If the outgroup is not well defined, this may not be the only rooting
	 */
	public Tree getTreeRootedBy(String[] outgroupNames) {
		return constructTree(getRootedBy(outgroupNames),units_);
	}
	/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @param ingroupBranchLength the maximum length of the branch leading to the ingroup clade
	 * @return the tree rooted by an outgroup defined by the mrca of a set of nodes
	 * @note If the outgroup is not well defined, this may not be the only rooting
	 */
	public Tree getTreeRootedBy(String[] outgroupNames, double ingroupBranchLength) {
		return constructTree(getRootedBy(outgroupNames,ingroupBranchLength),units_);
	}

	/**
	 * @param outgroupNames the names of the members of the outgroup
	 * @return all the possible rootings defined by the outgroup
	 */
	public Tree[] getAllTreesRootedBy(String[] outgroupNames) {
		Node[] nodes = getAllRootedBy(outgroupNames);
		Tree[] trees = new Tree[nodes.length];
		for(int i = 0 ; i < nodes.length ;i++) {
		  trees[i] = constructTree(nodes[i],units_);
		}
		return trees;
	}
	/**
	 * @return a tree iterator that returns each and every possible root of the base tree (as a new tree object each time)
	 * @note All Rooted trees are not constructed at once, but only on request. Use this method instead
	 * of getEveryRoot() if memory is an issue
	 */
	public TreeIterator getEveryRootIterator() {
		return new RootIterator(getAllConnections(),units_);
	}

	public void instruct(UnrootedTreeInterface treeInterface) {
		UnrootedTreeInterface.BaseBranch base = treeInterface.createBase();
		unrootedTree_.instruct(base);
	}
	public void instruct(RootedTreeInterface treeInterface) {
		RootedTreeInterface.RNode base = treeInterface.createRoot();
		unrootedTree_.instruct(base,firstChildNodeLength_);

	}
	/**
	 * Obtain access to individual branches
 	 * @return an array of branch access objects
	 */
	public BranchAccess[] getBranchAccess() {
	  final Connection[] connections = getAllConnections();
		final BranchAccess[] results = new BranchAccess[connections.length];
		for(int i = 0 ; i < connections.length ; i++) {
			results[i] = new BranchAccessImpl(this,connections[i], units_);
		}
		return results;
	}
	/**
	 * @return each and every possible root of the base tree
	 */
	public Tree[] getEveryRoot() {
		final Connection[] connections = getAllConnections();
		final Tree[] results = new Tree[connections.length];
		for(int i = 0 ; i < connections.length ; i++) {
			results[i] = constructTree(connections[i].getRootedAround(), units_);
		}
		return results;
	}
	/**
	 * @param Node n, a node from the original base tree that this TreeManipulator was
	 * constructed on
	 * @throws Illegal argument exception if input node was not in original base tree
	 */
	public Node getRootedAbove(Node base) {
		UndirectedNode match = unrootedTree_.getRelatedNode(base);
		if(match==null) {
			throw new IllegalArgumentException("Parameter node not found in original tree");
		}
		Node n = match.getPeerParentConnection().getRootedAround();
		NodeUtils.lengths2Heights(n);
		return n;
	}
	/**
	 * @param Node n, a node from the original base tree that this TreeManipulator was
	 * constructed on
	 * @throws Illegal argument exception if input node was not in original base tree
	 */
	public Tree getTreeRootedAbove(Node n) {
		return constructTree(getRootedAbove(n),units_);
	}
// -=-==--=-==-=--=-=-=-=-=-=-=-=-=-=-====--=-=-=--=====-=-=-=-=-=---====-=-=-=
// Static access methods
	/**
	 * Unroots a tree
	 * @param base The input tree that may or may not be unrooted
	 * @return an unrooted tree (has a trification at base)
	 */
	public static final Tree getUnrooted(Tree base) {
		return new TreeManipulator(base).getUnrootedTree();
	}

	/**
	 * Returns the mid point rooting of a tree. This is the rooting that divides
	 * the data between the two most distinct taxa
	 * @see http://www.mun.ca/biology/scarr/Panda_midpoint_rooting.htm
	 * @param base The input tree that may or may not be unrooted
	 * @return an unrooted tree (has a trification at base)
	 */
	public static final Tree getMidpointRooted(Tree base) {
		return new TreeManipulator(base).getMidPointRootedTree();
	}

	/**
	 * Obtains every rooting of a base tree
	 * @param base The input tree that may or may not be unrooted
	 */
	public static final Tree[] getEveryRoot(Tree base) {
		return new TreeManipulator(base).getEveryRoot();
	}

	/**
	 * Obtains every rooting of a base tree
	 * @param base The input tree that may or may not be unrooted
	 */
	public static final TreeIterator getEveryRootIterator(Tree base) {
		return new TreeManipulator(base).getEveryRootIterator();
	}

	/**
	 * Roots a tree by an outgroup
	 * @param base The input tree that may or may not be unrooted
	 * @param outgroupNames The names of the members of the outgroup. Names not matching taxa in the tree are ignored. The node that is the MCRA of
	 * members of the outgroup will influence the rooting.
	 * @throws IllegalArgumentException if no members of the tree appear in the outgroup
	 * @note if the outgroup is not well defined the returned tree may not be the only rooting
	 */
	public static final Tree getRootedBy(Tree base, String[] outgroupNames) {
		return new TreeManipulator(base).getTreeRootedBy(outgroupNames);
	}
	/**
	 * Roots a tree by an outgroup
	 * @param base The input tree that may or may not be unrooted
	 * @param outgroupNames The names of the members of the outgroup. Names not matching taxa in the tree are ignored. The node that is the MCRA of
	 * members of the outgroup will influence the rooting.
	 * @throws IllegalArgumentException if no members of the tree appear in the outgroup
	 * @note if the outgroup is not well defined the returned tree may not be the only rooting
	 */
	public static final Tree getRootedBy(Tree base, String[] outgroupNames, double ingroupBranchLength) {
		return new TreeManipulator(base).getTreeRootedBy(outgroupNames,ingroupBranchLength);
	}

	/**
	 * Roots a tree by an outgroup
	 * @param base The input tree that may or may not be unrooted
	 * @param ingroupBranchLength the maximum length of the branch leading to the ingroup clade
	 * @param outgroupNames The names of the members of the outgroup. Names not matching taxa in the tree are ignored. The node that is the MCRA of
	 * members of the outgroup will influence the rooting.
	 * @return every possible interpretation of rooting a tree by the given outgroup. If the outgroup is well defined there will be only one tree.
	 * @throws IllegalArgumentException if no members of the tree appear in the outgroup
	 */
	public static final Tree[] getAllRootingsBy(Tree base, String[] outgroupNames) {
		return new TreeManipulator(base).getAllTreesRootedBy(outgroupNames);
	}


// -=-==--=-==-=--=-=-=-=-=-=-=-=-=-=-====--=-=-=--=====-=-=-=-=-=---====-=-=-=
	/**
	 * A connection between two nodes
	 */
	private static final class Connection {
		private UndirectedNode firstNode_;
		private double maximumPathLengthToLeafViaFirstNode_;
		private boolean isFirstPathInfoFound_ = false;

		private UndirectedNode secondNode_;
		private double maximumPathLengthToLeafViaSecondNode_;
		private boolean isSecondPathInfoFound_ = false;

		private double distance_;

		private Object annotation_;

		public Connection(UndirectedNode firstNode, UndirectedNode secondNode, SimpleBranch connectingBranch) {
			this.firstNode_ = firstNode;
			this.secondNode_ = secondNode;
			this.distance_ = connectingBranch.getBranchLength();
			this.annotation_ = connectingBranch.getAnnotation();
		}


		public Connection(UndirectedNode baseNode, SimpleNode parent, int startingIndex, double branchLength, Object annotation) {
			this.firstNode_ = baseNode;
			this.secondNode_ = new UndirectedNode(this,startingIndex, parent);
			this.distance_ = branchLength;
			this.annotation_ = annotation;
		}

		public Connection(UndirectedNode parentNode, SimpleNode child, int constructionMode) {
			this.firstNode_ = parentNode;
			SimpleBranch connectingBranch = child.getParentBranch();
			this.distance_ = connectingBranch.getBranchLength();
			this.annotation_ = connectingBranch.getAnnotation();
			this.secondNode_ = new UndirectedNode(constructionMode, this,child);
		}
		public Connection(SimpleNode first, SimpleNode second,int constructionMode) {
			this.distance_ = first.getParentBranchLength()+second.getParentBranchLength();
			this.firstNode_ = new UndirectedNode(constructionMode, this,first);
			this.secondNode_ = new UndirectedNode(constructionMode, this,second);
		}
		/**
		 * The root branch constructor
		 * @param branch The simple root branch
		 * @param constructionMode the construction mode
		 */
		public Connection(SimpleBranch branch,int constructionMode) {
			SimpleNode first = branch.getParentNode();
			SimpleNode second = branch.getChildNode();

			this.distance_ = branch.getBranchLength();
			this.annotation_ = branch.getAnnotation();

			this.firstNode_ = new UndirectedNode(constructionMode, this,first);
			this.secondNode_ = new UndirectedNode(constructionMode, this,second);
		}
		private Connection(Connection original, Connection attachmentPoint, SimpleNode subTree, int constructionMode) {
		  if(original==attachmentPoint) {
				throw new RuntimeException("Not implemented yet!");
			} else {
				this.distance_ = original.distance_;
				this.annotation_ = original.annotation_;
				this.firstNode_ = original.firstNode_.getAttached( attachmentPoint, subTree, constructionMode, this );
				this.secondNode_ = original.secondNode_.getAttached( attachmentPoint, subTree, constructionMode, this );
			}
		}
		public final Connection getAttached(Connection attachmentPoint, SimpleNode subTree, int constructionMode) {
		  return new Connection(this,attachmentPoint,subTree,constructionMode);
		}
		public final String[][] getLabelSplit() { throw new RuntimeException("Not implemented yet!"); }
		public final void setDistance(double distance) {  this.distance_ = distance;		}

		public final UndirectedNode getFirst() { return firstNode_; }
		public final UndirectedNode getSecond() { return secondNode_; }

		public final int getExactCladeCount(String[] possibleCladeMembers, UndirectedNode caller) {
		  if(caller==firstNode_) {
			  return secondNode_.getExactCladeCount(possibleCladeMembers,this);
			} else if(caller==secondNode_) {
			  return firstNode_.getExactCladeCount(possibleCladeMembers,this);
			} else {
			  throw new RuntimeException("Assertion erro : unknown caller");
			}
		}

		public final boolean isFormsExactClade(String[] possibleCladeMembers) {
		  int leftCount = firstNode_.getExactCladeCount(possibleCladeMembers,this);
			int rightCount = secondNode_.getExactCladeCount(possibleCladeMembers,this);
			if(leftCount<0||rightCount<0) { return false; }
			return (leftCount>0&&rightCount==0)||(rightCount>0&&leftCount==0);
		}
		public final int getNumberOfMatchingLeaves(String[] leafSet) {
		  return firstNode_.getNumberOfMatchingLeaves(leafSet,this)
						+ secondNode_.getNumberOfMatchingLeaves(leafSet,this);
		}
		public final int getNumberOfMatchingLeaves(String[] leafSet, UndirectedNode caller) {
		  if(caller==firstNode_) {
				return secondNode_.getNumberOfMatchingLeaves( leafSet, this );
			} else  if(caller==secondNode_) {
				return firstNode_.getNumberOfMatchingLeaves( leafSet, this );
			}
			throw new RuntimeException("Assertion error : unknown caller");
		}
		public final UndirectedNode getRelatedNode(Node n) {
			UndirectedNode fromFirst = firstNode_.getRelatedNode(n,this);
			if(fromFirst!=null) {
				return fromFirst;
			}
			return secondNode_.getRelatedNode(n,this);
		}
		/**
		 * @return a new node rooted on the first node of this tree
		 */
		public Node getUnrooted() {
			if(firstNode_.isLeaf()) {
				return secondNode_.buildUnrootedTree();
			}
			return firstNode_.buildUnrootedTree();
		}
		public final double getMaximumPathLengthToLeafViaFirst() {
			if(!isFirstPathInfoFound_) {
				maximumPathLengthToLeafViaFirstNode_ = firstNode_.getMaximumPathLengthToLeaf(this);
				isFirstPathInfoFound_ = true;
			}
			return maximumPathLengthToLeafViaFirstNode_;
		}
		public final double getMaximumPathLengthToLeafViaSecond() {
			if(!isSecondPathInfoFound_) {
				maximumPathLengthToLeafViaSecondNode_ = secondNode_.getMaximumPathLengthToLeaf(this);
				isSecondPathInfoFound_ = true;
			}
			return maximumPathLengthToLeafViaSecondNode_;
		}
		public final void addLabels(ArrayList store, UndirectedNode callingNode) {
		  if(callingNode==firstNode_) {
			  secondNode_.addLabels(store,this);
			} else if(callingNode==secondNode_) {
			  firstNode_.addLabels(store,this);
			} else {
			  throw new RuntimeException("Assertion error : unknown calling node!");
			}
		}
		public void setAnnotation(Object annotation) {
		  this.annotation_ = annotation;
		}
		public void instruct(UnrootedTreeInterface.BaseBranch base) {
		  base.setLength(this.distance_);
			if(annotation_!=null) {
				base.setAnnotation( annotation_ );
			}
			firstNode_.instruct(base.getLeftNode(),this);
			secondNode_.instruct(base.getRightNode(),this);
		}
		public void instruct(RootedTreeInterface.RNode base, double firstChildLength) {
		  base.resetChildren();
			RootedTreeInterface.RNode left = base.createRChild();
			RootedTreeInterface.RNode right = base.createRChild();
		  RootedTreeInterface.RBranch leftBranch = left.getParentRBranch();
			RootedTreeInterface.RBranch rightBranch = right.getParentRBranch();
		  leftBranch.setLength(firstChildLength);
			rightBranch.setLength(distance_-firstChildLength);

			if(annotation_!=null) {
				leftBranch.setAnnotation( annotation_ );
				rightBranch.setAnnotation( annotation_ );
			}
			firstNode_.instruct(left,this);
			secondNode_.instruct(right,this);
		}
		public void instruct(UnrootedTreeInterface.UBranch base, UndirectedNode callingNode) {
		  base.setLength(this.distance_);
			if(annotation_!=null) {
				base.setAnnotation( annotation_ );
			}
			if(callingNode==firstNode_) {
			  secondNode_.instruct(base.getFartherNode(),this);
			} else if(callingNode==secondNode_) {
			  firstNode_.instruct(base.getFartherNode(),this);
			} else {
				throw new IllegalArgumentException("Calling node is unknown!");
			}
		}
		public void instruct(RootedTreeInterface.RBranch base, UndirectedNode callingNode) {

			base.setLength(this.distance_);
			if(annotation_!=null) {			base.setAnnotation( annotation_ );		}
			if(callingNode==firstNode_) {
				//We are fanning out towards more recent tips
			  secondNode_.instruct(base.getMoreRecentNode(),this);
			} else if(callingNode==secondNode_) {
			  firstNode_.instruct(base.getMoreRecentNode(),this);
			} else {
				throw new IllegalArgumentException("Calling node is unknown!");
			}
		}

		/**
		 * @return the difference between the maximum path length to leaf via first node
		 * and the maximum path lenght to leaf via second node
		 */
		public final double getMaximumPathDifference() {
			return Math.abs(getMaximumPathLengthToLeafViaFirst()-getMaximumPathLengthToLeafViaSecond());
		}
		public Connection getMRCAConnection(String[] nodeNames) {
			return getMRCAConnection(null, nodeNames);
		}

		public Node getRootedAroundMRCA(String[] nodeNames) {
			Connection mrca = getMRCAConnectionBaseTraverse(nodeNames);
			if(mrca!=null) {
				return mrca.getRootedAround();
			}
			throw new IllegalArgumentException("Non existent outgroup:"+pal.misc.Utils.toString(nodeNames));
		}
		public void instructRootedAroundMRCA(RootedTreeInterface rootedInterface, String[] nodeNames) {
			Connection mrca = getMRCAConnectionBaseTraverse(nodeNames);
			if(mrca!=null) {
				mrca.instructRootedAround(rootedInterface);
			} else{
				throw new IllegalArgumentException( "Non existent outgroup:"+pal.misc.Utils.toString( nodeNames ) );
			}
		}
		public Node[] getAllRootedAroundMRCA(String[] nodeNames) {
			Connection[] mrca = getAllMRCAConnectionBaseTraverse(nodeNames);
			if(mrca.length==0) {
				throw new IllegalArgumentException( "Non existent outgroup:"+
																						pal.misc.Utils.toString( nodeNames ) );
			}
			Node[] nodes = new Node[mrca.length];
			for(int i = 0 ; i < nodes.length ; i++) {
			  nodes[i] = mrca[i].getRootedAround();
			}
			return nodes;
		}
		public Node getRootedAroundMRCA(String[] nodeNames, double ingroupBranchLength) {
		  Connection mrca = getMRCAConnectionBaseTraverse(nodeNames);
			if(mrca!=null) {
				return mrca.getRootedAround(ingroupBranchLength,nodeNames);
			}
			if(getNumberOfMatchingLeaves(nodeNames)>0) {
				//Basically the node names includes all of the taxa!
			  return getRootedAround(ingroupBranchLength,nodeNames);
			}

			throw new IllegalArgumentException("Non existent outgroup:"+pal.misc.Utils.toString(nodeNames));
		}
		/**
		 * @param blockingNode
		 * @param nodeNames
		 * @return
		 */
		public Connection getMRCAConnection(UndirectedNode blockingNode, String[] nodeNames) {

			Connection first = (firstNode_!=blockingNode) ? firstNode_.getMRCAConnection(this,nodeNames) : null;
			Connection second = (secondNode_!=blockingNode) ? secondNode_.getMRCAConnection(this,nodeNames) : null;
			if(first!=null) {
				if(second!=null) { return this; }
				return first;
			}
			//Second may be null
			return second;
		}
		public Connection getMRCAConnectionBaseTraverse(String[] nodeNames) {

			return getMRCAConnectionBaseTraverse(null,nodeNames);

		}
		public Connection[] getAllMRCAConnectionBaseTraverse(String[] nodeNames) {
			Connection[] store = new Connection[getNumberOfConnections()];
			int total = getAllMRCAConnectionBaseTraverse(nodeNames, store,0);
			Connection[] result = new Connection[total];
			System.arraycopy(store,0,result,0,total);
			return result;
		}
		public int getAllMRCAConnectionBaseTraverse(String[] nodeNames, Connection[] store, int numberInStore) {
			return getAllMRCAConnectionBaseTraverse(null,nodeNames, store, numberInStore);
		}
		public Connection getMRCAConnectionBaseTraverse(UndirectedNode callingNode, String[] nodeNames) {

			Connection first = firstNode_.getMRCAConnection(this,nodeNames) ;
			Connection second = secondNode_.getMRCAConnection(this,nodeNames) ;
			System.out.println("Traverse:"+first+"   "+second);

			if(first!=null) {
			  if(second==null) { return first; }

				//If the MRCA of either sides is not us, then the true MRCA has not been found
				//(because the outgroup is distributed on both sides of this base).
				//We try a different base (by traversing tree, so we will eventually get a suitable base)
			  if(firstNode_!=callingNode) {
					Connection attempt = firstNode_.getMRCAConnectionBaseTraverse( this, nodeNames);
					if( attempt!=null ) {	return attempt; }
				}
				if(secondNode_!=callingNode) {
					Connection attempt = secondNode_.getMRCAConnectionBaseTraverse( this, nodeNames );
					if( attempt!=null ) {	return attempt; }
				}
				return null;
			} else {
				//Second may be null
				return second;
			}
		}
		private final int addToStore(Connection c, Connection[] store, int numberInStore) {
		  for(int i = 0 ; i < numberInStore ; i++) {
			  if(store[i]==c) {
					return numberInStore;
				}
			}
			store[numberInStore++] = c;
			return numberInStore;
		}
		public int getAllMRCAConnectionBaseTraverse(UndirectedNode callingNode, String[] nodeNames, Connection[] store, int numberInStore) {
			Connection first = firstNode_.getMRCAConnection(this,nodeNames) ;
			Connection second = secondNode_.getMRCAConnection(this,nodeNames) ;
			if(first!=null) {
				if(second==null) { return addToStore(first,store,numberInStore); }
				//Both left and right attempts return a connection,
				if(first==second&&second==this) {
				  //If the MRCA of either side is us then we are the MRCA
					return addToStore(this,store,numberInStore);
				}
				//If the MRCA of either sides is not us, then the true MRCA has not been found
				//(because the outgroup is distributed on both sides of this base).
				//We try a different base (by traversing tree, so we will eventually get a suitable base)
			  if(firstNode_!=callingNode) {
					numberInStore = firstNode_.getAllMRCAConnectionBaseTraverse( this, nodeNames,store, numberInStore );
				}
				if(secondNode_!=callingNode) {
					numberInStore = secondNode_.getAllMRCAConnectionBaseTraverse( this, nodeNames,store, numberInStore );
				}
			}
			return numberInStore;
		}

		/**
		 * @return the total number of connections in the tree that this connection is part of
		 */
		public final int getNumberOfConnections() {
			return getNumberOfConnections(null);
		}
		protected final int getNumberOfConnections(UndirectedNode blockingNode) {
			int count = 0;
			if(firstNode_!=blockingNode) {
				count+=firstNode_.getNumberOfConnections(this);
			}
			if(secondNode_!=blockingNode) {
				count+=secondNode_.getNumberOfConnections(this);
			}
			return count+1; //Plus one for me!
		}
		/**
		 * @return all connections in the tree that includes this connection
		 */
		public final Connection[] getAllConnections() {
			int size = getNumberOfConnections();
			Connection[] array = new Connection[size];
			getConnections(array,0);
			return array;
		}
		protected final int getConnections(Connection[] array, int index) {
			return getConnections(null,array,index);
		}
		protected final int getConnections(UndirectedNode blockingNode, Connection[] array, int index) {
			array[index++] = this;
			if(firstNode_!=blockingNode) {
				index=firstNode_.getConnections(this,array,index);
			}
			if(secondNode_!=blockingNode) {
				index=secondNode_.getConnections(this,array,index);
			}
			return index; //Plus one for me!
		}

		public final Connection getMidPointConnection(final UndirectedNode blockingNode, Connection best) {
			if(blockingNode==secondNode_) {
				best = firstNode_.getMidPointConnection(this,best);
			} else if(blockingNode==firstNode_) {
				best = secondNode_.getMidPointConnection(this,best);
			} else {
				throw new RuntimeException("Assertion error : getMidPointConnection called with invalid blockingNode");
			}
			final double myPathDiff = getMaximumPathDifference();
			final double bestDiff = best.getMaximumPathDifference();

			return (myPathDiff<bestDiff) ? this : best;
		}

		public Connection getMidPointConnection() {
			Connection best = this;
			best = getMidPointConnection(firstNode_,best);
			best = getMidPointConnection(secondNode_,best);
			return best;
		}
		public Node getMidPointRooted() {
			return getMidPointConnection().getRootedAround();
		}
		/**
		 * @return the maximum length from the node that isn't the blockingNode to it's leafs without going via this connection
		 * @note returned length does <b>not</b> include the length of this connection
		 */
		public double getMaxLengthToLeaf(UndirectedNode blockingNode) {
			if(secondNode_==blockingNode) {
				return getMaximumPathLengthToLeafViaFirst();
			}
			if(firstNode_==blockingNode) {
				return getMaximumPathLengthToLeafViaSecond();
			}
			throw new RuntimeException("Connection.GetMaxLengthToLeaf() called from unknown asking node");
		}

		/**
		 * Force a recalculation
		 */
		public void recalculateMaximumPathLengths() {
			clearPathInfo();
			updatePathInfo();
			assertPathInfo();
		}
		public void assertPathInfo() {
			assertPathInfo(null);
		}

		/**
		 * @throws RuntimeException if not all nodes have path info setup
		 */
		public void assertPathInfo(UndirectedNode blockingNode) {
			if(isFirstPathInfoFound_&&isSecondPathInfoFound_) {
				if(blockingNode!=firstNode_) {
					firstNode_.callMethodOnConnections(this,ASSERT_PATH_INFO_CALLER);
				}
				if(blockingNode!=secondNode_) {
					secondNode_.callMethodOnConnections(this,ASSERT_PATH_INFO_CALLER);
				}
			} else {
				throw new RuntimeException("Assertion error : assertPathInfo failed!");
			}
		}
		public void updatePathInfo() {
			updatePathInfo(null);
		}
		public void updatePathInfo(UndirectedNode blockingNode) {
			if(!isFirstPathInfoFound_) {
				this.maximumPathLengthToLeafViaFirstNode_ = firstNode_.getMaximumPathLengthToLeaf(this);
				isFirstPathInfoFound_ = true;
			}
			if(blockingNode!=firstNode_) {
				firstNode_.callMethodOnConnections(this,UPDATE_PATH_INFO_CALLER);
			}

			if(!isSecondPathInfoFound_) {
				this.maximumPathLengthToLeafViaSecondNode_ = secondNode_.getMaximumPathLengthToLeaf(this);
				isSecondPathInfoFound_ = true;
			}
			if(blockingNode!=secondNode_) {
				secondNode_.callMethodOnConnections(this,UPDATE_PATH_INFO_CALLER);
			}
		}
		public void clearPathInfo() {
			clearPathInfo(null);
		}
		public void clearPathInfo(UndirectedNode blockingNode) {
			this.isFirstPathInfoFound_ = false;
			this.isSecondPathInfoFound_ = false;
			if(blockingNode!=firstNode_) {
				this.firstNode_.callMethodOnConnections(this,CLEAR_PATH_INFO_CALLER);
			}
			if(blockingNode!=secondNode_) {
				this.secondNode_.callMethodOnConnections(this,CLEAR_PATH_INFO_CALLER);
			}
		}
		public final double getDistance() { return distance_; }
		public final boolean isConnectedTo(final UndirectedNode node) {
			return(node==firstNode_)||(node==secondNode_);
		}
		public final UndirectedNode getOtherEnd(final UndirectedNode oneEnd) {
			if(oneEnd==firstNode_) {		return secondNode_;		}
			if(oneEnd==secondNode_) {		return firstNode_;		}
			throw new RuntimeException("Assertion error : getOtherEnd called with non connecting node");
		}
		public final void instructRootedAround(RootedTreeInterface rootedInterface) {
			RootedTreeInterface.RNode root = rootedInterface.createRoot();
			instructRootedAround(root);
		}
		public final void instructRootedAround(RootedTreeInterface.RNode peer) {

			double leftDist = getMaximumPathLengthToLeafViaFirst();
			double rightDist = getMaximumPathLengthToLeafViaSecond();

			double diff = leftDist-rightDist;
			if(diff>distance_) {
				diff = 0;//distance_;
			} else if(diff<-distance_) {
				diff = 0;//-distance_;
			}
		  peer.resetChildren();
			RootedTreeInterface.RNode left = peer.createRChild();
			RootedTreeInterface.RNode right = peer.createRChild();
		  RootedTreeInterface.RBranch leftBranch = left.getParentRBranch();
			RootedTreeInterface.RBranch rightBranch = right.getParentRBranch();
		  leftBranch.setLength((distance_-diff)/2);
			rightBranch.setLength((distance_+diff)/2);

			if(annotation_!=null) {
				leftBranch.setAnnotation( annotation_ );
				rightBranch.setAnnotation( annotation_ );
			}
			firstNode_.instruct(left, this);
			secondNode_.instruct(right, this);
		}
		public final Node getRootedAround() {
			double leftDist = getMaximumPathLengthToLeafViaFirst();
			double rightDist = getMaximumPathLengthToLeafViaSecond();

			double diff = leftDist-rightDist;
			if(diff>distance_) {
				diff = 0;//distance_;
			} else if(diff<-distance_) {
				diff = 0;//-distance_;
			}

			Node left = firstNode_.buildTree(this, (distance_-diff)/2);
			Node right = secondNode_.buildTree(this, (distance_+diff)/2);
			Node n = NodeFactory.createNode(new Node[] { left, right});
			return n;

		}
		public final Node getRootedAround(double distanceForFirstChild) {
			double distanceForSecondChild = distance_-distanceForFirstChild;
			if(distanceForSecondChild<0) {
				distanceForFirstChild = distance_;
				distanceForSecondChild = 0;
			}
			Node left = firstNode_.buildTree(this, distanceForFirstChild);
			Node right = secondNode_.buildTree(this, distanceForSecondChild);
			Node n = NodeFactory.createNode(new Node[] { left, right});
			return n;
		}
		/**
		 * Not the most efficient way of doing this. Roots tree around outgroup, and restricts distance of ingroup to base (to make it look pretty)
		 * @param ingroupDistance
		 * @param outgroupMembers
		 * @return
		 */
		public final Node getRootedAround(double ingroupDistance, String[] outgroupMembers) {
		  final UndirectedNode ingroup, outgroup;
		  if(firstNode_.getMRCA(this,outgroupMembers)!=null) {
				outgroup = firstNode_;
				ingroup = secondNode_;
			}  else {
			  ingroup = firstNode_;
				outgroup = secondNode_;
			}
			double distanceForOutgroup = distance_-ingroupDistance;
			if(distanceForOutgroup<0) {
				ingroupDistance = distance_;
				distanceForOutgroup = 0;
			}
			Node left = ingroup.buildTree(this, ingroupDistance);
			Node right = outgroup.buildTree(this, distanceForOutgroup);
			return NodeFactory.createNode(new Node[] { left, right});
		}
	}


// =-=-=-=-=-=-=----==-=-=--==--=-==-=--=-==--==--==--=-=--=----==-=-=-=-=-==-=
// ==== Static methods
// -------------------
	/**
	 * @return a new tree constructions with node n as root
	 */
	private final static Tree constructTree(Node n, int units) {
		SimpleTree st = new SimpleTree(n);
		st.setUnits(units);
		return st;
	}
	/**
	 * @return tre if name is in names
	 */
	private static final boolean contains(String[] names, String name) {
		for(int i = 0 ; i < names.length ; i++) {
			if(name.equals(names[i])) { return true; }
		}
		return false;
	}
// =-=-=-=-=-=-=----==-=-=--==--=-==-=--=-==--==--==--=-=--=----==-=-=-=-=-==-=

	private static interface ConnectionMethodCaller {
		public void callOn(Connection c, UndirectedNode callingNode);
	}

	private static final ConnectionMethodCaller ASSERT_PATH_INFO_CALLER =
		new ConnectionMethodCaller() {
			public void callOn(Connection c, UndirectedNode callingNode) {	c.assertPathInfo(callingNode);	}
		};

	private static final ConnectionMethodCaller CLEAR_PATH_INFO_CALLER =
		new ConnectionMethodCaller() {
			public void callOn(Connection c, UndirectedNode callingNode) {	c.clearPathInfo(callingNode);	}
		};

	private static final ConnectionMethodCaller UPDATE_PATH_INFO_CALLER =
		new ConnectionMethodCaller() {
			public void callOn(Connection c, UndirectedNode callingNode) {	c.updatePathInfo(callingNode);	}
		};

	private static final ConnectionMethodCaller GET_NUMBER_OF_CONNECTIONS_CALLER =
		new ConnectionMethodCaller() {
			public void callOn(Connection c, UndirectedNode callingNode) {	c.getNumberOfConnections(callingNode);	}
		};

// =-=-=-=-=-=-=----==-=-=--==--=-==-=--=-==--==--==--=-=--=----==-=-=-=-=-==-=

	/**
	 * A node with no set idea of parent and children (just sibling connections)
	 */
	private static final class UndirectedNode {
		private Connection[] connectedNodes_;
		private final Node palPeer_;
		private final String label_;
		private final Object annotation_;

		/**
		 * Auto expands
		 */
		private UndirectedNode(Connection connection, int childStartingIndex, SimpleNode parent) {
			this.palPeer_ = null;
			this.label_ = null;
			this.annotation_ = null;
			this.connectedNodes_ = new Connection[3];
			int numberOfChildren = parent.getNumberOfChildren();
			this.connectedNodes_[0] = connection;
			if((numberOfChildren-childStartingIndex)==2)  {
				this.connectedNodes_[1] =
					new Connection(this,parent.getChild(childStartingIndex), EXPAND_CONSTRUCTION);
				this.connectedNodes_[2] =
					new Connection(this,parent.getChild(childStartingIndex+1), EXPAND_CONSTRUCTION);

			} else {
				this.connectedNodes_[1] = new Connection(this,parent.getChild(childStartingIndex), EXPAND_CONSTRUCTION);
				this.connectedNodes_[2] = new Connection(this,parent,childStartingIndex+1,0,null);
			}
		}
		/**
		 * The already unrooted tree constructor.
		 * @param peer The root of the tree (expects three or more children)
		 * @param constructionMode The construction mode
		 * @throws IllegalArgumentException if peer has less than three children
		 */
		public UndirectedNode(SimpleNode peer, int constructionMode) {

			final int numberOfChildren = peer.getNumberOfChildren();
			if(numberOfChildren<=2) {
			  throw new IllegalArgumentException("Peer must have at least three children!");
			}
			this.palPeer_ = peer.getPALPeer();
			this.label_ = peer.getLabel();
			this.annotation_ = peer.getLabel();
			if(constructionMode==REDUCE_CONSTRUCTION) {
				int numberOfReducedChildren = countReducedChildren(peer);
				this.connectedNodes_ = new Connection[numberOfReducedChildren];
				for(int i = 0 ;i < numberOfReducedChildren ; i++) {

					Connection c = new Connection(this,getReducedChild(peer, i), REDUCE_CONSTRUCTION);
					this.connectedNodes_[i] = c;
				}
			}	else if((constructionMode==MIMIC_CONSTRUCTION)||(numberOfChildren<=3))  {

				//Plus one for parent connection
				this.connectedNodes_ = new Connection[numberOfChildren];
				for(int i = 0 ; i< numberOfChildren ; i++) {
					this.connectedNodes_[i] = new Connection(this,peer.getChild(i),constructionMode);
				}
			} else  {
				//Expand construction
				this.connectedNodes_ = new Connection[3];
				this.connectedNodes_[0] = new Connection(this,peer.getChild(0), constructionMode);
				this.connectedNodes_[1] = new Connection(this,peer.getChild(1), constructionMode);
				this.connectedNodes_[2] = new Connection(this,peer,2, 0,null);
			}
		}
		private UndirectedNode( int constructionMode, Connection parentConnection, SimpleNode peer) {
			this.palPeer_ = peer.getPALPeer();
			this.label_ = peer.getLabel();
			this.annotation_ = peer.getAnnotation();
			final int numberOfChildren = peer.getNumberOfChildren();
			if(constructionMode==REDUCE_CONSTRUCTION) {

				int numberOfReducedChildren = countReducedChildren(peer);
			  this.connectedNodes_ = new Connection[numberOfReducedChildren+1];
				this.connectedNodes_[0] = parentConnection;
				for(int i = 0 ;i < numberOfReducedChildren ; i++) {
					Connection c = new Connection(this, getReducedChild(peer, i),REDUCE_CONSTRUCTION);
					this.connectedNodes_[i+1] = c;
				}
			} else if((constructionMode==MIMIC_CONSTRUCTION)||(numberOfChildren<=2))  {
				//Plus one for parent connection
				this.connectedNodes_ = new Connection[numberOfChildren+1];
				this.connectedNodes_[0] = parentConnection;
				for(int i = 0 ; i< numberOfChildren ; i++) {
					this.connectedNodes_[i+1] = new Connection(this,peer.getChild(i),constructionMode);
				}
			} else {
				this.connectedNodes_ = new Connection[3];
				this.connectedNodes_[0] = parentConnection;
				this.connectedNodes_[1] = new Connection(this,peer.getChild(0), constructionMode);
				this.connectedNodes_[2] = new Connection(this, peer, 1,0, null);
			}
		}
		private UndirectedNode(UndirectedNode orginal, Connection attachmentPoint, SimpleNode subTree, int constructionModel, Connection parent) {
		 throw new RuntimeException("Not implemented yet!");
		}
		public final UndirectedNode getAttached( Connection attachmentPoint, SimpleNode subTree, int constructionMode, Connection parent ) {
		  return new UndirectedNode(this,attachmentPoint,subTree, constructionMode,parent);
		}
		private static final int countReducedChildren(SimpleNode base) {
			int count = 0;
			int childCount = base.getNumberOfChildren();
			for(int  i = 0 ; i < childCount ; i++) {
			  SimpleNode c = base.getChild(i);
				if(!c.isLeaf()&&c.getParentBranchLength()<=BranchLimits.MINARC) {
				  count+=countReducedChildren(c);
				} else {
				  count++;
				}
			}
			return count;
	  }

	  private static final SimpleNode getReducedChild(SimpleNode base, int childIndex){
			int childCount = base.getNumberOfChildren();
			for(int  i = 0 ; i < childCount ; i++) {
			  SimpleNode c = base.getChild(i);
				if(!c.isLeaf()&&c.getParentBranchLength()<=BranchLimits.MINARC) {
				  SimpleNode rc = getReducedChild(c,childIndex);
					if(rc!=null) {
						return rc;
					}
					childIndex-=countReducedChildren(c);
				} else {
					if(childIndex == 0) {
					  return c;
					}
					childIndex--;
				}
			}
			return null;
	  }
		public void instruct(UnrootedTreeInterface.UNode node, Connection callingConnection) {

			if(label_!=null) {			  node.setLabel(label_);			}
			if(annotation_!=null) {			node.setAnnotation(annotation_);			}

			for(int i = 0 ; i < connectedNodes_.length ; i++) {
			  Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
				  c.instruct(node.createUChild().getParentUBranch(),this);
				}
			}
		}
		public final int getNumberOfMatchingLeaves(String[] leafSet, Connection caller)  {
		  if(isLeaf()) {
			  return contains(leafSet,label_) ? 1 : 0;
			} else{
				int count = 0;
		  	for(int i = 0 ; i < connectedNodes_.length ; i++) {
			    Connection c = connectedNodes_[i];
			  	if(c!=caller) {
						count+=c.getNumberOfMatchingLeaves(leafSet, this);
				  }
			  }
				return count;
			}
		}

		public int getExactCladeCount(String[] possibleCladeMembers,Connection caller) {
		  if(isLeaf()) {
			  return (pal.misc.Utils.isContains(possibleCladeMembers, label_) ? 1 : 0 );
			}
			int count = 0;
			for(int i = 0 ; i < connectedNodes_.length ; i++) {
			  Connection c = connectedNodes_[i];
				if(c!=caller) {
				  int subCount = c.getExactCladeCount(possibleCladeMembers,this);
					if(subCount<0) { return -1; }
					if(subCount==0) {
					  if(count>0) { return -1; }
					} else if(i==0) {
						count=subCount;
					} else if(count==0) {
						return -1;
					} else {
					  count+=subCount;
					}
				}
			}
			return count;
		}

		public void instruct(RootedTreeInterface.RNode base, Connection callingConnection) {

			if(label_!=null) {  base.setLabel(label_);		}
			if(annotation_!=null) { base.setAnnotation(annotation_); }

			for(int i = 0 ; i < connectedNodes_.length ; i++) {
			  Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
				  c.instruct(base.createRChild().getParentRBranch(),this);
				}
			}
		}
		public Connection getPeerParentConnection() { return connectedNodes_[0];	}
		private void assertCallingConnection(final Connection callingConnection) {
			boolean found = false;
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				if(connectedNodes_[i]==callingConnection) {
					found = true; break;
				}
			}
			if(!found) {
				throw new RuntimeException("Assertion error : calling connection not one of my connections");
			}
		}
		public void callMethodOnConnections(Connection callingConnection, ConnectionMethodCaller caller) {
			assertCallingConnection(callingConnection);
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				if(connectedNodes_[i]!=callingConnection) {
					caller.callOn(connectedNodes_[i],this);
				}
			}
		}
		public int getNumberOfConnections() {
			return getNumberOfConnections(null);
		}

		public int getNumberOfConnections(Connection callingConnection) {
			int count = 0;
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
					count+=c.getNumberOfConnections(this);
				}
			}
			return count;
		}

		public final void addLabels(ArrayList store, Connection callingConnection) {
			int count = 0;
			if(connectedNodes_.length==1) {
			  if(callingConnection!=connectedNodes_[0]) {
				  throw new RuntimeException("Assertion error : calling connection not recognised");
				}
				store.add(label_);
			} else {
				for( int i = 0; i<connectedNodes_.length; i++ ) {
					Connection c = connectedNodes_[i];
					if( c!=callingConnection ) {
						c.addLabels( store, this );
					}
				}
			}
		}
		/**
		 * Get all the connections of the tree that includes this node
		 */
		public Connection[] getAllConnections() {
			int size = getNumberOfConnections();
			Connection[] array = new Connection[size];
			getConnections(array,0);
			return array;
		}
		/**
		 * Fill in all connections fanning out from this node
		 */
		public int getConnections(Connection[] array, int index) {
			return getConnections(null, array, index);
		}
		/**
		 * @return new index for inserting connections into array. Assumes array is large enough
		 */
		public int getConnections(Connection callingConnection, Connection[] array, int index) {
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
					index=c.getConnections(this, array,index);
				}
			}
			return index;
		}

		private final Connection getMidPointConnection(Connection callingConnection, Connection best) {
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
					best = c.getMidPointConnection(this,best);
				}
			}
			return best;
		}

		public final Connection getMRCAConnectionBaseTraverse(Connection callingConnection,String[] nodeNames) {
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
					Connection mrca = c.getMRCAConnectionBaseTraverse(this,nodeNames);
					if(mrca !=null) {
						return mrca;
					}
				}
			}
			return null;
		}
		public final int getAllMRCAConnectionBaseTraverse(Connection callingConnection,String[] nodeNames, Connection[] store, int numberInStore) {
			for(int i = 0 ; i < connectedNodes_.length ; i++ ){
				Connection c = connectedNodes_[i];
				if(c!=callingConnection) {
					numberInStore = c.getAllMRCAConnectionBaseTraverse(this,nodeNames,store,numberInStore);
				}
			}
			return numberInStore;
		}
		/**
		 * @return true if this is a leaf node (has only one connection at most)
		 */
		public final boolean isLeaf() {
			return connectedNodes_.length<=1;
		}


		/**
		 * @return the maximum path length to a leaf without following the blocking connection
		 */
		public double getMaximumPathLengthToLeaf(Connection blockingConnection) {
			double maxPathLength = 0;
			for(int i = 0 ; i < connectedNodes_.length ; i++) {
				final Connection c = connectedNodes_[i];
				if(connectedNodes_[i]!=blockingConnection) {
					UndirectedNode other = c.getOtherEnd(this);
					double length = c.getMaxLengthToLeaf(this)+c.getDistance();
					maxPathLength = Math.max(maxPathLength,length);
				}
			}
			return maxPathLength;
		}
		/**
		 * Build a tree starting at this node and not going via blockingNode, with a branch length as set by distance
		 * @param blockingNode The sibling node not to include in the tree
		 * @param distance the branch length for the root
		 */
		public Node buildTree(Connection blockingConnection, double distance) {
			if(connectedNodes_.length==1) {
				return NodeFactory.createNodeBranchLength(distance, new Identifier(label_));
			}
			Node[] children = new Node[connectedNodes_.length-1];
			int addIndex = 0;
			for(int i = 0 ; i < connectedNodes_.length ; i++) {
				if(blockingConnection!=connectedNodes_[i]) {
					UndirectedNode other = connectedNodes_[i].getOtherEnd(this);
					children[addIndex++] = other.buildTree(connectedNodes_[i], connectedNodes_[i].distance_);
				}
			}
			return NodeFactory.createNodeBranchLength(distance, children);
		}
		/**
		 * @return an unrooted tree around this node
		 * @note does not work if this node is a leaf node (one connection)! (should check and use another node)
		 */
		public Node buildUnrootedTree() {
			if(connectedNodes_.length==1) {
				return NodeFactory.createNode(new Identifier(label_));
			}
			Node[] children = new Node[connectedNodes_.length];
			for(int i = 0 ; i < connectedNodes_.length ; i++) {
				UndirectedNode other = connectedNodes_[i].getOtherEnd(this);
				children[i] = other.buildTree(connectedNodes_[i], connectedNodes_[i].distance_);
			}
			return NodeFactory.createNode(children);
		}

		public UndirectedNode getMRCA(Connection callingConnection, String[] nodeNames ) {
			if(isLeaf()) {
				if(contains(nodeNames,label_)) {
					return this;
				}
				return null;
			}
			int count = 0;
			UndirectedNode lastMRCA = null;

			for(int i = 0 ; i < connectedNodes_.length ; i++) {
				final Connection c = connectedNodes_[i];
				if(callingConnection!=c) {
					UndirectedNode other = c.getOtherEnd(this);
					UndirectedNode mrca = other.getMRCA(c,nodeNames);
					if(mrca!=null) {
						count++;
						lastMRCA = mrca;
					}
				}
			}
			switch(count) {
				case 0 : { return null; } //Leafs aren't here
				case 1 : { return lastMRCA; } //We are no better than last MRCA
				default : { return this; } //We are intersection of multiple paths to outgroups
			}
		}
		public Connection getMRCAConnection(Connection callingConnection, String[] nodeNames ) {
			if(isLeaf()) {
				if(contains(nodeNames,label_)) {
					return callingConnection;
				}
				return null;
			}
			int count = 0;
			Connection lastMRCA = null;

			for(int i = 0 ; i < connectedNodes_.length ; i++) {
				final Connection c = connectedNodes_[i];
				if(callingConnection!=c) {
					Connection mrca = c.getMRCAConnection(this,nodeNames);
					if(mrca!=null) {
						count++;
						lastMRCA = mrca;
					}
				}
			}
			switch(count) {
				case 0 : { return null; } //Leafs aren't here
				case 1 : { return lastMRCA; } //We are no better than last MRCA
				default : { return callingConnection; } //We are intersection of multiple paths to outgroups
			}
		}
		private UndirectedNode getRelatedNode(Node peer, Connection callerConnection) {
			if(palPeer_ == peer) { return this; }
			for(int i = 0 ; i < connectedNodes_.length ; i++) {
				final Connection c = connectedNodes_[i];
				if(c!=callerConnection) {
					UndirectedNode n = connectedNodes_[i].getOtherEnd(this).getRelatedNode(peer,c);
					if(n!=null) { return n; }
				}
			}
			return null;
		}
		public UndirectedNode getRelatedNode(Node peer) {
			return getRelatedNode(peer,null);
		}
	}
	// =-=-=-==--=-=-=-=-=-==--=-=-==--==-=--==--==--=-=-==-=-=-=--==--==--==--==
	/**
	 * Root Iterator
	 */
	private static final class RootIterator implements TreeIterator {
		private final Connection[] connections_;
		private final int units_;
		private int currentConnection_;

		public RootIterator(Connection[] connections, int units) {
			this.connections_ = connections;
			this.units_ = units;
			this.currentConnection_ = 0;
		}
		public Tree getNextTree(AlgorithmCallback callback) {
			return constructTree(connections_[currentConnection_++].getRootedAround(),units_);
		}
		public boolean isMoreTrees() { return currentConnection_!=connections_.length; }
	} //End of class RootIterator
	// ===================================================================================================
	// ===================================================================================================
	/**
	 * The actual, hidden implementation of BranchAccess
	 */
	private static final class BranchAccessImpl implements BranchAccess {
	  private final Connection connection_;
		private final int units_;
		private final TreeManipulator parent_;
		public BranchAccessImpl(TreeManipulator parent, Connection connection, int units) {
			this.connection_ = connection;
			this.units_ = units;
			this.parent_ = parent;
	  }
		public TreeManipulator attachSubTree(Node subTree, int constructionMode) {
			return new TreeManipulator(parent_,connection_,subTree,constructionMode);
		}
		public String[][] getLabelSplit() {
			return connection_.getLabelSplit();
		}
		public void setAnnotation(Object annotation) {
		  connection_.setAnnotation(annotation);
		}
	}
	// ===================================================================================================
	// ===================================================================================================
	// ===================================================================================================
	/**
	 * The branch access objects allow specific operations on a particular branch (refered to as connections
	 * internally to confuse and bewilder)
	 */
	public static interface BranchAccess {
		/**
		 * Create a new TreeManipulator object that has sub grafted on (half way across this branch)
		 * @param subTree The sub tree, as normal Node object.
		 * @return A new TreeManipulator
		 */
	  public TreeManipulator attachSubTree(Node subTree, int constructionMode);
		/**
		 * Obtain the split of the labels around this branch.
		 * @return A two dimensional array of string arrays (the first element is the label names of one side of the split, the second element is the remainder)
		 */
		public String[][] getLabelSplit();

		/**
		 * Set the annotation for this branch (will be used when instructing a TreeInterface
		 * @param annotation the annotation object (is dependent on the TreeInterface instructed)
		 */
		public void setAnnotation(Object annotation);
	}
// ==============================================================================================
// ================================== Support Classes ===========================================
// ==============================================================================================

	private static interface SimpleNode {
		public boolean isLeaf();
		public int getNumberOfChildren();
		public SimpleNode getChild(int child);
		public SimpleBranch getParentBranch();
		public Object getAnnotation();

		public String getLabel();
		/**
		 * Obtain the length of the parent branch (or 0 if no parent branch)
		 * @return the approriate length
		 */
		public double getParentBranchLength();
		public Node getPALPeer();
	}
	private static interface SimpleBranch {
		public double getBranchLength();
		public Object getAnnotation();
		public SimpleNode getParentNode();
		public SimpleNode getChildNode();
	}
	private static final class RootedInterfaceImpl implements RootedTreeInterface {
		private InstructableNode root_;
		public RNode createRoot() {
			this.root_ = new InstructableNode();
			return root_;
		}
		public SimpleNode getSimpleRoot() { return root_; }
	}
	private static final class UnrootedInterfaceImpl implements UnrootedTreeInterface {
		private InstructableBranch root_;
		public BaseBranch createBase() {
			this.root_ = new InstructableBranch();
			return root_;
		}
		public SimpleBranch getSimpleRootBranch() { return root_; }


	}

	private static final class InstructableNode implements SimpleNode, RootedTreeInterface.RNode, UnrootedTreeInterface.UNode {
		private final InstructableBranch parent_;
	  private String label_;
		private ArrayList children_ = null;

		public Object annotation_;

		public InstructableNode() {
			this((InstructableBranch)null);
		}
		public String getLabel() { return label_; }
		public InstructableNode(InstructableNode parent) {
			this.parent_ = new InstructableBranch(parent,this);
		}
		public InstructableNode(InstructableBranch parent) {
			this.parent_ = parent;
		}
		public void setAnnotation(Object annotation) {
			this.annotation_ = annotation;
		}
		public Object getAnnotation() { return annotation_; }
		// Simple Node stuff
		public boolean isLeaf() {
			return children_ == null || children_.size()==0;
		}
		public int getNumberOfChildren() {
			return children_ == null ? 0 : children_.size();
		}
		public SimpleNode getChild(int child) {
			return (SimpleNode)children_.get(child);
		}
		public SimpleBranch getParentBranch() { return parent_;		}
		public double getParentBranchLength() { return (parent_ == null ? 0 : parent_.getBranchLength()); }
		public Node getPALPeer() { return null; }

		// General stuff
		public void setLabel(String label) { this.label_ = label; }

		public void resetChildren() {
			if(children_!=null) { children_.clear(); }
		}
		private final InstructableNode createChildImpl() {
			InstructableNode child = new InstructableNode(this);
			if(children_ ==null) {
				children_ = new ArrayList();
			}
			children_.add(child);
			return child;
		}

		// Rooted stuff
		public RootedTreeInterface.RBranch getParentRBranch() { return parent_; 		}
		public RootedTreeInterface.RNode createRChild() {  return createChildImpl();		}

		// Unrooted stuuff
		public UnrootedTreeInterface.UBranch getParentUBranch() {		return parent_;	}
		public UnrootedTreeInterface.UNode createUChild() {  return createChildImpl();		}



	} //End of class InstructableNode

	private static final class InstructableBranch implements SimpleBranch, RootedTreeInterface.RBranch, UnrootedTreeInterface.UBranch,UnrootedTreeInterface.BaseBranch {
		private final InstructableNode parent_;
		private final InstructableNode child_;

		private double length_;
		private Object annotation_;
		public InstructableBranch() {
			this.parent_ = new InstructableNode(this);
			this.child_ = new InstructableNode(this);
		}
		public SimpleNode getParentNode() { return parent_; }

		public SimpleNode getChildNode() { return child_; }


		public InstructableBranch(InstructableNode parent, InstructableNode child) {
			this.parent_ = parent;
			this.child_ = child;
		}
		public double getBranchLength() { return length_; }
		public Object getAnnotation() { return annotation_; }
		public void setLength(double length) {	this.length_ = length;		}
		public void setAnnotation(Object annotation) {	this.annotation_ = annotation;	}
		public RootedTreeInterface.RNode getMoreRecentNode() { return child_; }
		public RootedTreeInterface.RNode getLessRecentNode() { return parent_; }
		public UnrootedTreeInterface.UNode getCloserNode() { return parent_; }
		public UnrootedTreeInterface.UNode getFartherNode() { return child_; }
		public UnrootedTreeInterface.UNode getLeftNode() { return parent_; }
		public UnrootedTreeInterface.UNode getRightNode() { return child_; }
	}
	public static final class PALBranchWrapper implements SimpleBranch {
		private final PALNodeWrapper parent_;
		private final PALNodeWrapper child_;
		private final double branchLength_;

		public PALBranchWrapper(PALNodeWrapper parent, PALNodeWrapper child, double branchLength) {
		  this.parent_ = parent;
			this.child_ = child;
			this.branchLength_ = branchLength;
		}
		public SimpleNode getParentNode() { return parent_; }

		public SimpleNode getChildNode() { return child_; }

		public final double getBranchLength() {		return branchLength_;		}
		public final Object getAnnotation() { return null; }
	}
	public static final class PALNodeWrapper implements SimpleNode {
		private final Node peer_;
		private final PALNodeWrapper[] children_;
		private final PALBranchWrapper parentBranch_;

		public PALNodeWrapper(Node peer) {
			this(peer,null);
		}
		public PALNodeWrapper(Node peer, PALNodeWrapper parent) {

			this.peer_ = peer;
			if(parent==null) {
				this.parentBranch_ = null;
			} else {
				this.parentBranch_ = new PALBranchWrapper(parent,this, peer.getBranchLength());
			}
			this.children_ = new PALNodeWrapper[peer.getChildCount()];
		  for(int i = 0 ; i < children_.length ; i++) {
				this.children_[i] = new PALNodeWrapper(peer.getChild(i), this);
			}
		}
		public Object getAnnotation() { return null; }
		public String getLabel() {
		  Identifier id = peer_.getIdentifier();
			if(id!=null) {
			  return id.getName();
			}
			return null;
		}
		public boolean isLeaf() { return children_.length == 0; }
		public int getNumberOfChildren() { return children_.length; }
		public SimpleNode getChild(int child) { return children_[child]; }
		public SimpleBranch getParentBranch() {	return parentBranch_;		}
		public Node getPALPeer() { return peer_; }
		public double getParentBranchLength() {
			return parentBranch_==null ? 0 : parentBranch_.getBranchLength();
		}
	}

}