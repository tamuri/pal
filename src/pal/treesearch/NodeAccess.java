// NodeAccess.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: NodeAccess </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.tree.*;
import pal.alignment.*;
import pal.substmodel.*;

public interface NodeAccess {
	/**
	 * Set the annotation for this branch (will be used when instructing TreeInterfaces
	 * @param annotation The annotation object (dependent on the TreeInterface instructed)
	 */
	public void setAnnotation(Object annotation);

	public Object getAnnotation();

	public boolean isLeaf();

	public String getLabel();

}