// RootAccess.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: RootAccess </p>
 * <p>Description: A root access node is one that can be used for the root of the likelihood calculation (does not necessarily mean it's the root of the tree!) </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.tree.*;

public interface RootAccess {
	public void getAllComponents(ArrayList store, Class componentType);
	public double calculateLogLikelihood(GeneralConstructionTool tool);

	public Node buildPALNodeBase();
	public Node buildPALNodeES();


	public void testLikelihood(GeneralConstructionTool tool);

//	public void rebuildPatterns(GeneralConstructionTool tool);

}