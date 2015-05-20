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
 *
 * @author Matthew Goode
 * @version 1.0
 */

import pal.eval.ConditionalProbabilityStore;
import pal.eval.PatternInfo;
import pal.tree.Node;

import java.util.ArrayList;

public interface ConstrainedNode extends GeneralTreeComponent {
    ConstrainedNode getLeftChild();

    ConstrainedNode getRightChild();

    /**
     * @return the minum distance to a child or zero if no children
     */
    double getMinimumDirectChildDistance();

    void recursivelyAdjustNodeHeight(HeightAdjustment heightAdjustment);

    void recursivelySetParentPivot(PivotNode parentPivot);

    void setupInternalNodeHeights(ConstraintModel.GroupManager groupConstraints);

    double getMinimumChildSeperation(double currentSeperation);

    double getMinimumLeafChildSeperation(double parentHeight);

    double getMinOriginalDescendentLeafHeight();

    double getMaxOriginalDescendentLeafHeight();

    PatternInfo getDescendentPatternInfo(GeneralConstructionTool tool);

//	public void rebuildDescendentPattern(GeneralConstructionTool tool);

    void getSubTreeComponents(ArrayList store, Class componentType);

    void getNonSubTreeComponents(ArrayList store, Class componentType);

    double getNodeHeight();

    /**
     * Build node model base units (eg years)
     * @return A normal PAL node
     */
    Node buildDescendentPALNodeBase();

    /**
     * Build node with Expected Substitution Units
     * @param groupConstraints The constraints object to do the conversion with
     * @return A normal PAL node
     */
    Node buildDescendentPALNodeES(ConstraintModel.GroupManager groupConstraints);

    /**
     * Obtain information regarding the current state of the leaf heights (called when first constructed).
     * The resulting heights will, for exampled, be averaged across components and then used as the starting
     * height values (see setLeafHeights...())
     * @param user An object that uses the height information
     */
    void obtainLeafInformation(HeightInformationUser user);

    void testLikelihood(GeneralConstructionTool tool);

    String toStringHeights();

    String toStringLengths(double parentHeight);

    // -----------------------------------------------------------------------------------------------
    ConditionalProbabilityStore getDescendentExtendedConditionals(double extensionHeight, GeneralConstructionTool tool, boolean allowCaching);

    ConditionalProbabilityStore getDescendentExtendedConditionalsWithAdjustedInternalHeights(double adjustedExtensionHeight, GeneralConstructionTool tool, HeightAdjustment internalNodeHeightAdjuster, boolean allowCaching);


    ConditionalProbabilityStore getDescendentFlatConditionals(GeneralConstructionTool tool, boolean allowCaching);

    interface HeightAdjustment {
        double getAdjustedHeight(Object relatedNode, double baseHeight);
    }

}