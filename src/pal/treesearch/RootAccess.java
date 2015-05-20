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
 *
 * @author Matthew Goode
 * @version 1.0
 */

import pal.tree.Node;

import java.util.ArrayList;
import java.util.List;

public interface RootAccess {
    void getAllComponents(List<GeneralOptimisable> store, Class componentType);

    double calculateLogLikelihood(GeneralConstructionTool tool);

    Node buildPALNodeBase();

    Node buildPALNodeES();


    void testLikelihood(GeneralConstructionTool tool);

//	public void rebuildPatterns(GeneralConstructionTool tool);

}