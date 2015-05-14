// AbstractParentableConstrainedNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: AbstractParentableConstrainedNode </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.tree.*;

public abstract class AbstractParentableConstrainedNode implements ParentableConstrainedNode {
	private ConstrainedNode leftChild_;
	private ConstrainedNode rightChild_;
	private final PatternInfo descendentPatternInfo_;
	private boolean descendentPatternValid_;



	private final double originalPeerHeight_;
	private final double minOriginalDescendentLeafHeight_;
	private final double maxOriginalDescendentLeafHeight_;


	private double nodeHeight_;

	private final MolecularClockLikelihoodModel.Internal constrainedInternal_;

	private final GeneralConstraintGroupManager groupManager_;

	protected AbstractParentableConstrainedNode(Node peer, GeneralConstructionTool tool,GeneralConstraintGroupManager.Store store, GeneralConstraintGroupManager groupManager) {
		this.groupManager_ = groupManager;
		this.descendentPatternInfo_ = new PatternInfo( tool.getNumberOfSites(), true );
	  this.descendentPatternValid_ = false;
		this.originalPeerHeight_ = peer.getNodeHeight();
		Node palLeft = peer.getChild(0);
		Node palRight = peer.getChild(1);
		ConstraintModel.GroupManager parentGroup = groupManager.getRelatedGroup();
		this.leftChild_ = tool.createConstrainedNode(palLeft, this, store,groupManager);
		this.rightChild_ = tool.createConstrainedNode(palRight, this, store,groupManager);
		this.constrainedInternal_ = parentGroup.createNewClockInternal();
		this.minOriginalDescendentLeafHeight_ = Math.min(leftChild_.getMinOriginalDescendentLeafHeight(),rightChild_.getMinOriginalDescendentLeafHeight());
		this.maxOriginalDescendentLeafHeight_ = Math.max(leftChild_.getMaxOriginalDescendentLeafHeight(),rightChild_.getMaxOriginalDescendentLeafHeight());
	}
	public MolecularClockLikelihoodModel.External obtainConstrainedExternalCalculator() {
		return groupManager_.obtainConstrainedExternalCalculator();
	}
	public double getMinOriginalDescendentLeafHeight() { return minOriginalDescendentLeafHeight_; }
	public double getMaxOriginalDescendentLeafHeight() { return maxOriginalDescendentLeafHeight_; }
	public double getMinimumDirectChildDistance() { return nodeHeight_-Math.max(leftChild_.getNodeHeight(), rightChild_.getNodeHeight()); }

	public final String toStringHeights() {
		return "("+leftChild_.toStringHeights()+", "+rightChild_.toStringHeights()+"):"+getNodeHeight();
	}
	protected final String toStringLengths() {
	  return toStringLengths(nodeHeight_);
	}
	public final String toStringLengths(double parentHeight) {
		return "("+leftChild_.toStringLengths(nodeHeight_)+", "+rightChild_.toStringLengths(nodeHeight_)+"):"+(parentHeight-nodeHeight_);

	}


//	public double getMinimumDescendentRelativeHeight(double currentMinimum) {
//	  currentMinimum = leftChild_.getMinimumDescendentRelativeHeight(currentMinimum);
//		currentMinimum = rightChild_.getMinimumDescendentRelativeHeight(currentMinimum);
//		return (relativeHeight_<currentMinimum ? relativeHeight_ : currentMinimum);
//	} //End of getMinimumDescendentRelativeHeight()
//
//	protected final double getMinimumDescendentRelativeHeight() {
//		return getMinimumDescendentRelativeHeight(relativeHeight_);
//	} //End of getMinimumDescendentRelativeHeight()

	public final double getNodeHeight() { return nodeHeight_; }
	public double getMinimumChildSeperation() {
		double currentSeparation = Math.min(nodeHeight_-leftChild_.getNodeHeight(), nodeHeight_-rightChild_.getNodeHeight());
		currentSeparation = leftChild_.getMinimumChildSeperation(currentSeparation);
		currentSeparation = rightChild_.getMinimumChildSeperation(currentSeparation);
		return currentSeparation;
	}
	public double getMinimumChildSeperation(double currentSeparation) {
		currentSeparation = leftChild_.getMinimumChildSeperation(currentSeparation);
		currentSeparation = rightChild_.getMinimumChildSeperation(currentSeparation);

		double mySeperation = Math.min(nodeHeight_-leftChild_.getNodeHeight(), nodeHeight_-rightChild_.getNodeHeight());
		//We don't have any childs
		return Math.min(currentSeparation,mySeperation);
	}
	protected final double getMinimumLeafChildSeperation( ) {
		return Math.min(
		  leftChild_.getMinimumLeafChildSeperation(nodeHeight_),
			rightChild_.getMinimumLeafChildSeperation(nodeHeight_)
		);
	}
	public final double getMinimumLeafChildSeperation( double parentHeight) {
		return getMinimumLeafChildSeperation();
	}

	public final void setupInternalNodeHeights(ConstraintModel.GroupManager groupConstraints) {
	  leftChild_.setupInternalNodeHeights(groupConstraints);
		rightChild_.setupInternalNodeHeights(groupConstraints);
	  nodeHeight_ = groupConstraints.getBaseHeight(originalPeerHeight_);
		final double leftHeight = leftChild_.getNodeHeight();
		final double rightHeight = rightChild_.getNodeHeight();
		if(leftHeight>nodeHeight_) {
		  nodeHeight_ = leftHeight;
		}
		if(rightHeight>nodeHeight_) {
		  nodeHeight_ = rightHeight;
		}
		System.out.println(nodeHeight_+"   "+originalPeerHeight_);
	}
	public final PatternInfo getDescendentPatternInfo(GeneralConstructionTool tool) {
		if(!descendentPatternValid_) {
			tool.build(descendentPatternInfo_,leftChild_.getDescendentPatternInfo(tool),rightChild_.getDescendentPatternInfo(tool));
			descendentPatternValid_ = true;
		}
		return descendentPatternInfo_;
	} //End of getDescendentPatternInfo();

	public final void rebuildDescendentPattern(GeneralConstructionTool tool) {
		tool.build(descendentPatternInfo_,leftChild_.getDescendentPatternInfo(tool),rightChild_.getDescendentPatternInfo(tool));
		descendentPatternValid_ = true;
	}

	protected final PatternInfo getRightChildPatternInfo(GeneralConstructionTool tool) { return rightChild_.getDescendentPatternInfo(tool); }
	protected final PatternInfo getLeftChildPatternInfo(GeneralConstructionTool tool) { return leftChild_.getDescendentPatternInfo(tool); }

	protected final void setNodeHeight(double nodeHeight) { this.nodeHeight_ = nodeHeight; }

	protected final void adjustNodeHeight(double heightDelta) {
		this.nodeHeight_+=heightDelta;
		double childMax = getMaxChildHeight();
		if(nodeHeight_<childMax) {
		  nodeHeight_ = childMax;
		}
	}
	public void recursivelyAdjustNodeHeight(ConstrainedNode.HeightAdjustment height) {

//		System.out.println("Adjusting by:"+heightDelta);
		leftChild_.recursivelyAdjustNodeHeight(height);
		rightChild_.recursivelyAdjustNodeHeight(height);
		double before = nodeHeight_;
		this.nodeHeight_ = height.getAdjustedHeight(this,nodeHeight_);
//		System.out.println("before:"+before+", after:"+nodeHeight_);
	}

	public final ConstrainedNode getLeftChild() { return leftChild_; }
	public final ConstrainedNode getRightChild() { return rightChild_; }

	protected final Node buildLeftDecendentPALNodeBase() {	return leftChild_.buildDescendentPALNodeBase(); 	}
	protected final Node buildRightDecendentPALNodeBase() {  return  rightChild_.buildDescendentPALNodeBase();	}

	public final Node buildDescendentPALNodeBase() {
		final Node l = leftChild_.buildDescendentPALNodeBase();
		final Node r = rightChild_.buildDescendentPALNodeBase();
		return NodeFactory.createNode(new Node[] { l, r}, getNodeHeight());
	}
	public final Node buildDescendentPALNodeES(ConstraintModel.GroupManager groupManager) {
		final Node l = leftChild_.buildDescendentPALNodeES(groupManager);
		final Node r = rightChild_.buildDescendentPALNodeES(groupManager);
		return NodeFactory.createNode(new Node[] { l, r}, groupManager.getExpectedSubstitutionHeight(getNodeHeight()));
	}
	protected final void recursivelySetChildrenParentPivot(PivotNode parentPivot) {
		leftChild_.recursivelySetParentPivot( parentPivot);
		rightChild_.recursivelySetParentPivot(parentPivot);
	}
	/**
	 * Obtain conditionals by extended left child conditionals to height of this node
	 * @param tool The construction tool
	 * @return the extended conditionals
	 */
	protected final ConditionalProbabilityStore getLeftDescendentExtendedConditionals(GeneralConstructionTool tool, boolean allowCaching) {
		return leftChild_.getDescendentExtendedConditionals(getNodeHeight(),tool,allowCaching);
	}
	/**
	 * Obtain conditionals by extended left child conditionals to a particular height
	 * @param specifiedHeight The desired height of extension
	 * @param tool The construction tool
	 * @return the extended conditionals
	 */
	protected final ConditionalProbabilityStore getLeftDescendentExtendedConditionals(double specifiedHeight, GeneralConstructionTool tool,boolean allowCaching) {
		return leftChild_.getDescendentExtendedConditionals(specifiedHeight,tool,allowCaching);
	}
	/**
	 * Obtain conditionals by extended right child conditionals to height of this node
	 * @param tool The construction tool
	 * @return the extended conditionals
	 */
	protected final ConditionalProbabilityStore getRightDescendentExtendedConditionals(GeneralConstructionTool tool,boolean allowCaching) {
		return rightChild_.getDescendentExtendedConditionals(getNodeHeight(),tool,allowCaching);
	}
	/**
	 * Obtain conditionals by extended left child conditionals to a particular height
	 * @param specifiedHeight The desired height of extension
	 * @param tool The construction tool
	 * @return the extended conditionals
	 */
	protected final ConditionalProbabilityStore getRightDescendentExtendedConditionals(double specifiedHeight, GeneralConstructionTool tool, boolean allowCaching) {
		return rightChild_.getDescendentExtendedConditionals(specifiedHeight,tool,allowCaching);
	}

	protected final MolecularClockLikelihoodModel.Internal getConstrainedInternal() { return constrainedInternal_; }

	protected final double getDescendentLogLikelihood(GeneralConstructionTool tool, boolean allowCaching) {
		double height = getNodeHeight();

		final ConditionalProbabilityStore leftConditionalProbabilityProbabilties =
			leftChild_.getDescendentExtendedConditionals(height,tool,allowCaching);
		final ConditionalProbabilityStore rightConditionalProbabilityProbabilties =
			rightChild_.getDescendentExtendedConditionals(height,tool,allowCaching);
		return obtainConstrainedExternalCalculator().calculateLogLikelihood(height, getDescendentPatternInfo(tool), leftConditionalProbabilityProbabilties,rightConditionalProbabilityProbabilties);
	}

	protected final double getMaxChildHeight() {
	  return Math.max(leftChild_.getNodeHeight(), rightChild_.getNodeHeight());
	}

// =-=-=-==--=-=-==-=--=-=-=-=-=-=-=-==--=-==--=-=-=-=-=-==-=--=-=-=-=-==-=-=--=-=-==-=-=-=-=--=-=-==-=--==-=--=-=-==--=

	public ConditionalProbabilityStore getDescendentExtendedConditionals( double extensionHeight, GeneralConstructionTool tool, boolean allowCaching) {
		final double height = getNodeHeight();
		if(extensionHeight==height) {
		  return getDescendentFlatConditionals(tool,allowCaching);
		}

		final ConditionalProbabilityStore leftConditionals = leftChild_.getDescendentExtendedConditionals(height,tool,allowCaching);
		final ConditionalProbabilityStore rightConditionals = rightChild_.getDescendentExtendedConditionals(height,tool,allowCaching);
		return constrainedInternal_.calculateExtendedConditionals(extensionHeight,height,getDescendentPatternInfo(tool),leftConditionals,rightConditionals);
	}

	public ConditionalProbabilityStore getDescendentExtendedConditionalsWithAdjustedInternalHeights(double adjustedExtensionHeight, GeneralConstructionTool tool, ConstrainedNode.HeightAdjustment internalNodeHeightAdjuster, boolean allowCaching) {
		final double adjustedHeight = internalNodeHeightAdjuster.getAdjustedHeight(this, getNodeHeight());
		final ConditionalProbabilityStore leftConditionals = leftChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool,internalNodeHeightAdjuster,allowCaching);
		final ConditionalProbabilityStore rightConditionals = rightChild_.getDescendentExtendedConditionalsWithAdjustedInternalHeights(adjustedHeight,tool,internalNodeHeightAdjuster,allowCaching);
		return constrainedInternal_.calculateExtendedConditionals(adjustedExtensionHeight,adjustedHeight,getDescendentPatternInfo(tool),leftConditionals,rightConditionals);
	}
	public ConditionalProbabilityStore getDescendentFlatConditionals( GeneralConstructionTool tool, boolean allowCaching) {
		final double height = getNodeHeight();
		final ConditionalProbabilityStore leftConditionals = leftChild_.getDescendentExtendedConditionals(height,tool,allowCaching);
		final ConditionalProbabilityStore rightConditionals = rightChild_.getDescendentExtendedConditionals(height,tool,allowCaching);

		return constrainedInternal_.calculateFlatConditionals(getDescendentPatternInfo(tool),leftConditionals,rightConditionals);
	}

// =-=-=-==--=-=-==-=--=-=-=-=-=-=-=-==--=-==--=-=-=-=-=-==-=--=-=-=-=-==-=-=--=-=-==-=-=-=-=--=-=-==-=--==-=--=-=-==--=

	public final void getSubTreeComponents(ArrayList store, Class componentType) {
		if(componentType.isAssignableFrom(getClass())) { store.add(this); }
		leftChild_.getSubTreeComponents(store, componentType);	rightChild_.getSubTreeComponents(store,componentType);
	}
	public final void getAllComponents(ArrayList store, Class componentType) {
		getSubTreeComponents(store,componentType);	getNonSubTreeComponents(store,componentType);
	}
	public void getNonSubTreeOfChildComponents(ArrayList store, Class componentType, ConstrainedNode childCaller) {
		if(componentType.isAssignableFrom(getClass())) { store.add(this); }
		getNonSubTreeComponents(store,componentType);
		if(leftChild_==childCaller) {	rightChild_.getSubTreeComponents(store,componentType);	}
		else if(rightChild_==childCaller) {	leftChild_.getSubTreeComponents(store, componentType);	}
		else {	throw new RuntimeException("Assertion error : unknown child caller!");	}
	}

	/**
	 * Investigate if given node is left node (and not right)
	 * @param node the node to investigate
	 * @return true if left, false if right
	 * @throws IllegalArgumentException if node is neither left nor right
	 */
	public boolean isLeftChild(ConstrainedNode node) {
		if(node==leftChild_) {
			return true;
		}
		if(node==rightChild_) {
			return false;
		}
		throw new IllegalArgumentException("Unknown child");
	}
	public void obtainLeafInformation(HeightInformationUser user) {
		leftChild_.obtainLeafInformation(user);
		rightChild_.obtainLeafInformation(user);
	}
// ================================================================
// == Abstract Stuff ==============================================
	public abstract void getNonSubTreeComponents(ArrayList store, Class componentType);
}