// UnitsProvider.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * interface for classes that can provide the related Units used, (as
 *
 * @author Matthew Goode
 * @version $Id: UnitsProvider.java,v 1.1 2002/02/05 22:02:36 matt Exp $
 */
public interface UnitsProvider extends Units {

    /**
     * @return the units relating to this object.
     */
    int getUnits();
}
