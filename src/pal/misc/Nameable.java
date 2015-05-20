// Nameable.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * interface for classes that can be named.
 *
 * @author Alexei Drummond
 * @version $Id: Nameable.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 */
public interface Nameable {
    /**
     * get the name of this object.
     *
     * @return name of this object.
     */
    String getName();

    /**
     * set the name of this object.
     *
     * @param name the new name.
     */
    void setName(String name);
}
