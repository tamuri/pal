// Comparator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.util;

/**
 * interface for an object that can compare other objects for the
 * purposes of ordering them.
 * This interface is analogous to the Comparator interface in
 * Java 1.2 and higher, and it should be superceded by the collections
 * framework when PAL is moved to 1.2 or higher.
 *
 * @version $Id: Comparator.java,v 1.2 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond
 */
public interface Comparator
{
	/**
	 * Returns a number representing the ordering relationship that
	 * the two objects have.
	 * A negative number indicates that the first object is "smaller" than
	 * the second object, a positive number means it is "larger" and zero
	 * indicates that the objects are equal.
	 */
	int compare(Object o1, Object o2);

	/**
	 * Returns true if the two objects are equal.
	 */
	boolean equals(Object o1, Object o2);
}
