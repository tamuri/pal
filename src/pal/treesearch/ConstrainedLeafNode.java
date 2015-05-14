// ConstrainedLeafNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: ConstrainedLeafNode </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.misc.*;
import pal.tree.*;

public class ConstrainedLeafNode extends AbstractLeafNode implements ConstrainedNode {
  private ParentableConstrainedNode parentNode_;

	private final MolecularClockLikelihoodModel.Leaf leafCalculator_;

	private final double originalPeerHeight_;
	private final double height_;

	public ConstrainedLeafNode(ParentableConstrainedNode parentNode, Node peer, double height, GeneralConstructionTool tool, ConstraintModel.GroupManager parentGroup) {
		super(peer.getIdentifier().getName(),tool);
		this.parentNode_ = parentNode;
		this.originalPeerHeight_ = peer.getNodeHeight();
		this.height_ = height;
		this.leafCalculator_ = createNewConstrainedLeafCalculator( parentGroup);
	}

	public void recursivelySetParentPivot(PivotNode parentPivot) {
		//Don't care
	}
	public void setupInternalNodeHeights(ConstraintModel.GroupManager groupConstraints) {
		//Don't care
	}
	public double getMinOriginalDescendentLeafHeight() { return originalPeerHeight_; }
	public double getMaxOriginalDescendentLeafHeight() { return originalPeerHeight_; }
	/**
	 * Returns null as we can't have children...
	 * @return null
	 */
	public ConstrainedNode getLeftChild() { return null; }
	public ConstrainedNode getRightChild() { return null; }

	public void recursivelyAdjustNodeHeight(HeightAdjustment heightDelta) {
		//Leaves don't have a relative height, so doesn't apply
	}
	public String toStringHeights() {
	  return getLabel()+":"+height_;
	}
	public String toStringLengths(double parentHeight){
	  return getLabel()+":"+(parentHeight-height_);
	}

	public double getMinimumDirectChildDistance() {
		return 0;
	}
	public Node buildDescendentPALNodeBase() {
		return NodeFactory.createNode(new Identifier(getLabel()),getNodeHeight());
	}
	public final Node buildDescendentPALNodeES(ConstraintModel.GroupManager groupManager) {
		return NodeFactory.createNode(new Identifier(getLabel()),groupManager.getExpectedSubstitutionHeight(getNodeHeight()));

	}
	public final double getMinimumLeafChildSeperation( double parentHeight) {
		return parentHeight-height_;
	}

	public void rebuildDescendentPattern(GeneralConstructionTool tool) {
		// nothing to do as we never change pattern
	}

	public PatternInfo getDescendentPatternInfo(GeneralConstructionTool tool) { return getPatternInfo(); }

	private final void checkAdd(ArrayList store, Class componentType) {
		if(componentType.isAssignableFrom(getClass())) {
			store.add(this);
		}
	}
	public void getSubTreeComponents(ArrayList store, Class componentType) {checkAdd(store,componentType);	}

	public void getNonSubTreeComponents(ArrayList store, Class componentType) {
		if(parentNode_!=null) { parentNode_.getNonSubTreeOfChildComponents(store,componentType, this); }
	}
	public void getAllComponents(ArrayList store,Class componentType) {
		checkAdd(store,componentType);
		getNonSubTreeComponents(store,componentType);
	}

	public final double getNodeHeight() {		return height_; 	}

	public double getMinimumChildSeperation(double currentSeperation) {
	  //We don't have any childs
		return currentSeperation;
	}
	public void obtainLeafInformation(HeightInformationUser user) {
		user.addHeight(getLabel(), originalPeerHeight_);
	}
// ------------------------------------------------------------------------------------------------

	public ConditionalProbabilityStore getDescendentExtendedConditionals(double extensionHeight, GeneralConstructionTool tool, boolean allowCaching) {
		return leafCalculator_.calculateExtendedConditionals(extensionHeight,height_);
	}
	public ConditionalProbabilityStore getDescendentExtendedConditionalsWithAdjustedInternalHeights(double extensionHeight, GeneralConstructionTool tool, HeightAdjustment internalNodeHeightAdjuster, boolean allowCaching) {
		//We do nothing different because this isn't an internal node
		return leafCalculator_.calculateExtendedConditionals(extensionHeight,height_);
	}


	public ConditionalProbabilityStore getDescendentFlatConditionals(GeneralConstructionTool tool, boolean allowCaching) {
		return leafCalculator_.calculateFlatConditionals(height_);
	}

	public void testLikelihood(GeneralConstructionTool tool) {
	  System.out.println("Test (C-LEAF:"+getLabel()+")");
	}

	public String toString(FreeBranch caller) {		return getLabel(); 	}


}