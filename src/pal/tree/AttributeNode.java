// AttributeNode.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import java.util.Iterator;


/**
 * interface for a node (includes branch) in a binary/non-binary
 * rooted/unrooted tree. Unlike its superclass this node
 * can have an arbitrary number of named attributes associated with it.
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 * @version $Id: AttributeNode.java,v 1.4 2001/12/03 11:04:39 korbinian Exp $
 */

public interface AttributeNode extends Node {

    /**
     * attribute name for the standard error on a node's height.
     */
    String NODE_HEIGHT_SE = "node height SE";

    /**
     * attribute name for the probability of the clade defined by an internal node.
     */
    String CLADE_PROBABILITY = "clade probability";

    /**
     * attribute name for the probability of the subtree defined by an internal node.
     */
    String SUBTREE_PROBABILITY = "subtree probability";

    /**
     * attribute name for the mean height of this clade in a group of trees.
     */
    String MEAN_CLADE_HEIGHT = "mean clade height";

    /**
     * Sets a named attribute to the given value.
     *
     * @param name  the name of the attribute
     * @param value the value to set the attribute
     */
    void setAttribute(String name, Object value);

    /**
     * @param name the name of the attribute.
     * @return the attribute with the given name or null if it doesn't exist.
     */
    Object getAttribute(String name);

    /**
     * @return an enumeration of the attributes that this node has.
     */
    Iterator<String> getAttributeNames();

}

