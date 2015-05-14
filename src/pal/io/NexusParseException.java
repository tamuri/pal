// NexusParseException.java
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.io;


/**
 *  Exception class for the NexusParser.
 *
 * @author    Alex Moore, a.d.moore@ex.ac.uk
 * @version
 */
public class NexusParseException extends Exception {

	/**  Constructor for the <code>NexusParseException</code> object */
	public NexusParseException() {
		super();
	}


	/**
	 *  Constructor for the <code>NexusParseException</code> object
	 *
	 * @param  msg	A message string for users etc.
	 */
	public NexusParseException(String msg) {
		super(msg);
	}

}

