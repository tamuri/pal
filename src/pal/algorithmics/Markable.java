// Markable.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.algorithmics;

/**
 * <p>Title: Markable </p>
 * <p>Description: An interface for objects that can have their state marked</p>
 * @author Matthew Goode
 * @version 1.0
 */

public interface Markable {
	public void mark();
	public void undoToMark();
}