// ConstrainedNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: ConstrainedNode </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.tree.*;

public interface ConstrainedNode extends GeneralTreeComponent {
	public ConstrainedNode getLeftChild();
	public ConstrainedNode getRightChild();
	/**
	  * @return the minum distance to a child or zero if no children
		*/
	public double getMinimumDirectChildDistance();

	public void recursivelyAdjustNodeHeight(HeightAdjustment heightAdjustment);
	public void recursivelySetParentPivot(PivotNode parentPivot);
	public void setupInternalNodeHeights(ConstraintModel.GroupManager groupConstraints);

	public double getMinimumChildSeperation(double currentSeperation);
	public double getMinimumLeafChildSeperation(double parentHeight);

	public double getMinOriginalDescendentLeafHeight();
	public double getMaxOriginalDescendentLeafHeight();

	public PatternInfo getDescendentPatternInfo(GeneralConstructionTool tool);

//	public void rebuildDescendentPattern(GeneralConstructionTool tool);

	public void getSubTreeComponents(ArrayList store, Class componentType);

	public void getNonSubTreeComponents(ArrayList store, Class componentType);

	public double getNodeHeight();

	/**
	 * Build node model base units (eg years)
	 * @return A normal PAL node
	 */
	public Node buildDescendentPALNodeBase();
	/**
	 * Build node with Expected Substitution Units
	 * @param groupConstraints The constraints object to do the conversion with
	 * @return A normal PAL node
	 */
	public Node buildDescendentPALNodeES(ConstraintModel.GroupManager groupConstraints);
	/**
	 * Obtain information regarding the current state of the leaf heights (called when first constructed).
	 * The resulting heights will, for exampled, be averaged across components and then used as the starting
	 * height values (see setLeafHeights...())
	 * @param user An object that uses the height information
	 */
	public void obtainLeafInformation(HeightInformationUser user);

	public void testLikelihood(GeneralConstructionTool tool);

	public String toStringHeights();
	public String toStringLengths(double parentHeight);
// -----------------------------------------------------------------------------------------------
	public ConditionalProbabilityStore getDescendentExtendedConditionals(double extensionHeight, GeneralConstructionTool tool, boolean allowCaching);

	public ConditionalProbabilityStore getDescendentExtendedConditionalsWithAdjustedInternalHeights(double adjustedExtensionHeight, GeneralConstructionTool tool, HeightAdjustment internalNodeHeightAdjuster, boolean allowCaching);


	public ConditionalProbabilityStore getDescendentFlatConditionals(GeneralConstructionTool tool, boolean allowCaching);

	public static interface HeightAdjustment {
		public double getAdjustedHeight(Object relatedNode, double baseHeight);
	}

}