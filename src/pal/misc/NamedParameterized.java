// Parameterized.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.*;


/**
 * interface for class with (optimizable) named parameters
 *
 * @version $Id: NamedParameterized.java,v 1.2 2001/11/20 19:58:45 alexi Exp $
 *
 * @author Alexei Drummond
 */
public interface NamedParameterized extends Parameterized {
	
	/**
	 * @return a short identifier for this parameter type. Should be the same for 
	 * all instances of a given class!
	 */
	String getParameterName(int i);
}
