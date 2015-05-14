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
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;
import pal.eval.*;

public interface ParentableConstrainedNode  {
	public double getNodeHeight();

	public void getNonSubTreeOfChildComponents(ArrayList store, Class componentType, ConstrainedNode childCaller);

	public ConditionalProbabilityStore getAscendentExtended(double baseHeight, ConstrainedNode childCaller, GeneralConstructionTool tool,boolean allowCaching);
	public ConditionalProbabilityStore getAscendentFlat(ConstrainedNode childCaller, GeneralConstructionTool tool, boolean allowCaching);

	public PatternInfo getAscendentPatternInfo(ConstrainedNode childCaller, GeneralConstructionTool tool);
}