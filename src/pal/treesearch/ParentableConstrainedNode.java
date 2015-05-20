// ParentableConstrainedNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: ParentableConstrainedNode </p>
 * <p>Description: </p>
 *
 * @author Matthew Goode
 * @version 1.0
 */

import pal.eval.ConditionalProbabilityStore;
import pal.eval.PatternInfo;

import java.util.ArrayList;

public interface ParentableConstrainedNode {
    double getNodeHeight();

    void getNonSubTreeOfChildComponents(ArrayList store, Class componentType, ConstrainedNode childCaller);

    ConditionalProbabilityStore getAscendentExtended(double baseHeight, ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching);

    ConditionalProbabilityStore getAscendentFlat(ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching);

    PatternInfo getAscendentPatternInfo(ConstrainedNode childCaller, GeneralConstructionTool tool);
}