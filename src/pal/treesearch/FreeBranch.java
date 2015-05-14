// FreeBranch.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.math.*;
import pal.tree.*;

public final class FreeBranch implements RootAccess, GeneralOptimisable  {

	private FreeNode leftNode_;
	private FreeNode rightNode_;
	private double branchLength_;
	private final PatternInfo centerPattern_;
	private boolean centerPatternValid_;

	private final OptimisationHandler optimisationHandler_;

	private final int index_;

	private FreeNode markLeftNode_ = null;
	private FreeNode markRightNode_ = null;
	private double markBranchLength_;

	private Object annotation_ = null;

	/**
	 * The starting constructor for building from a given tree
	 * @param n The normal PAL node structure to base this tree on
	 * @param tool to aid in construction
	 */
	public FreeBranch(Node n,  GeneralConstructionTool tool, GeneralConstraintGroupManager.Store store) {
		if(n.getChildCount()!=2) {
			throw new IllegalArgumentException("Base tree must be bificating");
		}
		this.index_ = tool.allocateNextConnectionIndex();
		Node l = n.getChild(0);
		Node r = n.getChild(1);
		this.branchLength_ = l.getBranchLength()+r.getBranchLength();

		leftNode_ = tool.createFreeNode(l, this,store);
		rightNode_ = tool.createFreeNode(r, this,store);

		this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.centerPatternValid_ = false;
	  this.optimisationHandler_ = new OptimisationHandler(tool);
	}
	/**
	 * Continuing recurison constructor for a given tree
	 * @param n The PAL node structure to base sub tree on
	 * @param parent The parent node (sub tree in other direction)
	 * @param tool to aid in construction
	 */
	public FreeBranch(Node n, FreeNode parent, GeneralConstructionTool tool, GeneralConstraintGroupManager.Store store) {
		this.index_ = tool.allocateNextConnectionIndex();
		this.branchLength_ = n.getBranchLength();
		this.rightNode_ = parent;
		this.leftNode_ = tool.createFreeNode(n,this,store);
		this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.centerPatternValid_ = false;
		this.optimisationHandler_ = new OptimisationHandler(tool);

	}

	/**
	 * A generic constructor given two already defined left and right children
	 * @param left The left node
	 * @param right The right node
	 * @param branchLength The length of connection
	 * @param tool to aid in construction
	 */
	public FreeBranch(FreeNode left, FreeNode right, double branchLength, GeneralConstructionTool tool) {
		this.index_ = tool.allocateNextConnectionIndex();
		this.branchLength_ = branchLength;
		this.rightNode_ = right;
		this.leftNode_ = left;
		this.centerPattern_ = new PatternInfo(tool.getNumberOfSites(),true);
		this.centerPatternValid_ = false;
		this.optimisationHandler_ = new OptimisationHandler(tool);
	}

	public void setAnnotation(Object annotation) {	  this.annotation_ = annotation;		}
	/**
	 *
	 * @return The "right" node of this connection.
	 */
	public final FreeNode getLeft() { return leftNode_; }
	/**
	 *
	 * @return The "left" node of this connection.
	 */
	public final FreeNode getRight() { return rightNode_; }

	/**
	 * Mark this node, or in other words store information on left and right nodes and branch length for later retreival (via undoToMark())
	 */
	public final void mark() {
		this.markBranchLength_ = branchLength_;
		this.markLeftNode_ = leftNode_;		this.markRightNode_ = rightNode_;
	}
	/**
	 * @return The pattern info object for the left node leading to this connection
	 */
	public final PatternInfo getLeftPatternInfo(GeneralConstructionTool tool) {		return leftNode_.getPatternInfo(tool, this);	}
	/**
	 * @return The pattern info object for the right node leading to this connection
	 */
	public final PatternInfo getRightPatternInfo(GeneralConstructionTool tool) {	return rightNode_.getPatternInfo(tool, this);	}


	public final PatternInfo getPatternInfo(GeneralConstructionTool tool, FreeNode caller) {
	  if(caller==leftNode_) {
		  return rightNode_.getPatternInfo(tool,this);
		}
		if(caller==rightNode_) {
		  return leftNode_.getPatternInfo(tool,this);
		}
		throw new IllegalArgumentException("Unknown caller!");
	}
	/**
	 *
	 * @return The pattern info across this connection (for use if this connection is the "root" of the likelihood calculation)
	 */
	public final PatternInfo getCenterPatternInfo(GeneralConstructionTool tool) {
		if(!centerPatternValid_) {
			tool.build(centerPattern_, getLeftPatternInfo(tool),getRightPatternInfo(tool));
			centerPatternValid_ = true;
		}
		return centerPattern_;
	}

	public final void undoToMark() {
		if(markLeftNode_==null) {
			throw new RuntimeException("Assertion error : undo to mark when no mark made");
		}
		this.branchLength_ = markBranchLength_;
		this.leftNode_ = markLeftNode_;
		this.rightNode_ = markRightNode_;
	}

	public String toString() {
		return "("+leftNode_+", "+rightNode_+")";
	}
	public boolean hasConnection(FreeBranch c, FreeNode caller) {
		if(c==this) { return true; }
		if(caller==leftNode_) {
			return rightNode_.hasConnection(c,this);
		}
		if(caller==rightNode_) {
			return leftNode_.hasConnection(c,this);
		}
		throw new IllegalArgumentException("Unknown caller");
	}

	/**
	 * @return the "left" connection of the left node
	 */
	public FreeBranch getLeftLeftBranch() {	return leftNode_.getLeftBranch(this);	}
	/**
	 * @return the "right" connection of the left node
	 */
	public FreeBranch getLeftRightBranch() {	return leftNode_.getRightBranch(this);	}
	/**
	 * @return the "left" connection of the right node
	 */
	public FreeBranch getRightLeftBranch() {	return rightNode_.getLeftBranch(this);	}
	/**
	 * @return the "right" connection of the left node
	 */
	public FreeBranch getRightRightBranch() {		return rightNode_.getRightBranch(this);		}

	/**
	 * @return connection that by attaching to we would undo this operation, null if operation no successful
	 */
	public FreeBranch attachTo(FreeBranch attachmentPoint, FreeBranch[] store) {

		final FreeNode used = (leftNode_.hasConnection(attachmentPoint, this) ? leftNode_ : rightNode_ );
		if(used.hasDirectConnection(attachmentPoint)) {
			return null;
		}
		final FreeBranch redundant = used.extract(this);
		final FreeBranch reattachment;
		final FreeBranch leftUsed = used.getLeftBranch(this);
		final FreeBranch rightUsed = used.getRightBranch(this);

		if(leftUsed==redundant) {
			reattachment = rightUsed;
		} else if(rightUsed == redundant) {
			reattachment = leftUsed;
		} else {
			throw new IllegalArgumentException("Assertion error");
		}
		if(redundant==null) {
			throw new RuntimeException("Assertion error : I should be able to extract from one of my nodes!");
		}

		FreeNode attachmentOldRight = attachmentPoint.rightNode_;
		//We will attach the old right to redundant, and move in the used node to the attachment point
		attachmentPoint.swapNode(attachmentOldRight,used);
		redundant.swapNode(redundant.getOther(used),attachmentOldRight);

		//Fix up old right to have correct attachments
		attachmentOldRight.swapConnection(attachmentPoint,redundant);

		//c.swapNode();
		//Fix up the used connections
		store[0] = this;
		store[1] = redundant;
		store[2] = attachmentPoint;
		used.setConnectingBranches(store,3);

		return reattachment;
	}
	public Node buildPALNodeBase() {
		Node[] children = new Node[] {
			leftNode_.buildPALNodeBase(branchLength_/2,this),
			rightNode_.buildPALNodeBase(branchLength_/2,this)
		};
		return NodeFactory.createNode(children);
	}
	public Node buildPALNodeES() {
		Node[] children = new Node[] {
			leftNode_.buildPALNodeES(branchLength_/2,this),
			rightNode_.buildPALNodeES(branchLength_/2,this)
		};
		Node n = NodeFactory.createNode(children);
		NodeUtils.lengths2Heights(n);
		return  n;
	}

	public Node buildPALNodeBase(FreeNode caller) {
		if(leftNode_==caller) {		return rightNode_.buildPALNodeBase(branchLength_,this);		}
		if(rightNode_==caller) {	return leftNode_.buildPALNodeBase(branchLength_,this);		}
		throw new IllegalArgumentException("Unknown caller!");
	}

	public Node buildPALNodeES(FreeNode caller) {
		if(leftNode_==caller) {		return rightNode_.buildPALNodeES(branchLength_,this);		}
		if(rightNode_==caller) {	return leftNode_.buildPALNodeES(branchLength_,this);		}
		throw new IllegalArgumentException("Unknown caller!");
	}

	/**
	 * @return -1 if null
	 */
	private final static int getIndex(FreeBranch c) {
		if(c==null) { return -1;}
		return c.index_;
	}

	/**
	 * Does nothing to fix up tree structure
	 */
	public void setNodes(FreeNode left, FreeNode right) {
		this.leftNode_ = left;		this.rightNode_ = right;
	}
	/**
	 * @note does not change the nodes connection information. Leaves tree in an inconsitent state
	 */
	public void swapNode(FreeNode nodeToReplace, FreeNode replacement) {
		if(nodeToReplace==leftNode_) {
			leftNode_ = replacement;
		} else if(nodeToReplace==rightNode_) {
			rightNode_ = replacement;
		} else {
			throw new RuntimeException("Unknown node to replace");
		}
	}
	public final ConditionalProbabilityStore getLeftFlatConditionalProbabilities( GeneralConstructionTool tool ) {
		return leftNode_.getFlatConditionalProbabilities(this,tool);
	}
	public final ConditionalProbabilityStore getRightFlatConditionalProbabilities( GeneralConstructionTool tool) {
		return rightNode_.getFlatConditionalProbabilities(this,tool);
	}

	//Branch Length stuff
	public final double getBranchLength() { return branchLength_; }
	public final void setBranchLength(double x) { this.branchLength_ = x; }

	public String toString(FreeNode caller) {
		if(caller==leftNode_) {
			return rightNode_.toString(this);
		}
		if(caller!=rightNode_) {
			throw new RuntimeException("Unknown caller");
		}
		return leftNode_.toString(this);
	}
	public void testLikelihood( GeneralConstructionTool tool) {
		testLikelihood(null,tool);
	}
	public void testLikelihood(FreeNode caller, GeneralConstructionTool tool) {
		System.out.println("Test Free Branch:"+calculateLogLikelihood( tool));

		if(caller!=leftNode_) {		leftNode_.testLikelihood(this,tool);		}
		if(caller!=rightNode_){		rightNode_.testLikelihood(this,tool);		}
	}

	public ConditionalProbabilityStore getExtendedConditionalProbabilities(  FreeNode caller, GeneralConstructionTool tool) {
		FreeNode other = getOther(caller);
		return other.getExtendedConditionalProbabilities(branchLength_,  this,tool);
	}
	public ConditionalProbabilityStore getExtendedConditionalProbabilities(  FreeNode caller, UnconstrainedLikelihoodModel.External externalCalculator, ConditionalProbabilityStore extendedStore, GeneralConstructionTool tool) {
		FreeNode other = getOther(caller);
		return other.getExtendedConditionalProbabilities(branchLength_,this, externalCalculator, extendedStore,tool);
	}

	public final int getNumberOfOptimisationTypes() { return 1; }
	public double optimise(int optimisationType, UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits) {
		ConditionalProbabilityStore leftFlat = getLeftFlatConditionalProbabilities(tool);
		ConditionalProbabilityStore rightFlat = getRightFlatConditionalProbabilities(tool);
		optimisationHandler_.setup(leftFlat,rightFlat,getCenterPatternInfo(tool),branchLength_, fracDigits,tool.obtainTempConditionalProbabilityStore());
		optimisationHandler_.optimise(minimiser);
		this.branchLength_ = optimisationHandler_.getBranchLength();
		return optimisationHandler_.getLogLikelihood();
	}


	public void getAllComponents(ArrayList store, Class componentType) {	getAllComponents(store,componentType, null);	}

	public void getAllComponents(ArrayList store, Class componentType, FreeNode caller) {
		if(componentType.isAssignableFrom(getClass())) { store.add(this);		}
		if(caller!=leftNode_) {	leftNode_.getAllComponents(store,componentType, this);	}
		if(caller!=rightNode_) { rightNode_.getAllComponents(store,componentType,this);	}
	}

	public void getCenterPatternInfo(GeneralConstructionTool tool, PatternInfo store) {
		PatternInfo left = leftNode_.getPatternInfo(tool, this);
		PatternInfo right = rightNode_.getPatternInfo(tool, this);
		tool.build(store, left,right);
	}

	public FreeNode getOther(FreeNode caller) {
		if(leftNode_==caller) {
			return rightNode_;
		}
		if(rightNode_==caller) {
			return leftNode_;
		}
		throw new RuntimeException("Unknown caller!");
	}

	public final void doNNI(MersenneTwisterFast r) {
		doNNI(r.nextBoolean(),r.nextBoolean());
	}
	/**
	 * Does not reconstruct patterns
	 */
	public boolean doNNI(boolean leftSwapLeft, boolean rightSwapLeft) {
		FreeBranch first = leftSwapLeft ? leftNode_.getLeftBranch(this) : leftNode_.getRightBranch(this);
		if(first==null) {
			return false;
		}
		FreeBranch second = rightSwapLeft ? rightNode_.getLeftBranch(this) : rightNode_.getRightBranch(this);
		if(second==null) {
			return false;
		}
		leftNode_.swapConnection(first,rightNode_,second);
		return true;
	}
	public double calculateLogLikelihood(  GeneralConstructionTool tool) {
		UnconstrainedLikelihoodModel.External calculator = tool.obtainFreeExternalCalculator();
		PatternInfo pi = getCenterPatternInfo(tool);
		final ConditionalProbabilityStore leftConditionalProbabilityProbabilties =
			leftNode_.getFlatConditionalProbabilities( this,tool);
		final ConditionalProbabilityStore rightConditionalProbabilityProbabilties =
			rightNode_.getExtendedConditionalProbabilities(branchLength_, this,tool);
		return calculator.calculateLogLikelihood(pi, leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties);
	}
	public double calculateLogLikelihood2(GeneralConstructionTool tool) {
	  UnconstrainedLikelihoodModel.External calculator = tool.obtainFreeExternalCalculator();
		PatternInfo pi = getCenterPatternInfo(tool);
	  final ConditionalProbabilityStore left = leftNode_.getFlatConditionalProbabilities( this,tool);
	  final ConditionalProbabilityStore right = rightNode_.getFlatConditionalProbabilities(   this,tool);
	  return calculator.calculateLogLikelihood(branchLength_, pi, left,right,tool.newConditionalProbabilityStore(false));
	}
	public SiteDetails calculateSiteDetails(UnconstrainedLikelihoodModel.External calculator, GeneralConstructionTool tool) {
		PatternInfo pi = getCenterPatternInfo(tool);
		final ConditionalProbabilityStore left = leftNode_.getFlatConditionalProbabilities( this,tool);
		final ConditionalProbabilityStore right = rightNode_.getFlatConditionalProbabilities(  this,tool);
		return calculator.calculateSiteDetailsUnrooted(branchLength_, pi,left,right,tool.newConditionalProbabilityStore(false));
	}
	// ================================================================================================
	private static final class OptimisationHandler implements UnivariateFunction {
		private ConditionalProbabilityStore leftFlatConditionals_;
		private ConditionalProbabilityStore rightFlatConditionals_;
		private ConditionalProbabilityStore tempConditionals_;
		private PatternInfo centerPattern_;
		private final UnconstrainedLikelihoodModel.External external_;
		private double branchLength_;
		private double logLikelihood_;
		private int fracDigits_;

		public OptimisationHandler(GeneralConstructionTool tool) {
			this.external_ = tool.obtainFreeExternalCalculator();
		}
		public void setup(ConditionalProbabilityStore leftFlatConditionals, ConditionalProbabilityStore rightFlatConditionals, PatternInfo centerPattern, double branchLength, int fracDigits, ConditionalProbabilityStore tempConditionals) {
		  this.leftFlatConditionals_ = leftFlatConditionals;
			this.tempConditionals_ = tempConditionals;
			this.rightFlatConditionals_ = rightFlatConditionals;
			this.centerPattern_ = centerPattern;
			this.branchLength_ = branchLength;
			this.fracDigits_ = fracDigits;
		}
		public void optimise(UnivariateMinimum minimiser) {
			minimiser.findMinimum(branchLength_,this,fracDigits_);
		  this.branchLength_ = minimiser.minx;
			this.logLikelihood_ = -minimiser.fminx;
		}

		public double evaluate(double argument) {
			return -external_.calculateLogLikelihood(argument,centerPattern_,leftFlatConditionals_,rightFlatConditionals_, tempConditionals_);
		}

	  public double getLowerBound() { return 0; }

	  public double getUpperBound() { return 1; }
		public double getLogLikelihood() { return logLikelihood_; }
		public double getBranchLength() { return branchLength_; }
	}
}