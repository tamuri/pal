// Report.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.*;

/**
 * interface for classes that can print out a human readable report of itself
 *
 * @version $Id: Report.java,v 1.4 2002/12/05 04:27:28 matt Exp $
 *
 * @author Korbinian Strimmer
 */
public interface Report
{
	/**
	 * print human readable report (e.g., on parameters and associated model)
	 *
	 * @param out output stream
	 */
	void report(PrintWriter out);

}
