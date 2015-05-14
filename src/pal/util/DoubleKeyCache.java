// DoubleKeyCache.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.util;

import java.io.*;

/**
 * @author Alexei Drummond
 */
public interface DoubleKeyCache {

	/**
	 * retrieves the object with the key nearest to given value
	 */
	//DoubleKey getNearest(DoubleKey d, double tolerance);
	Object getNearest(double targetKey, double tolerance);

	//void addDoubleKey(DoubleKey d);
	void addDoubleKey(double relatedKey, Object value);

	void clearCache();

	Object clone();
}
