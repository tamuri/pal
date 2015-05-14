// ParameterEvent.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
package pal.misc;

/**
 * An event used by ExternalParameterListeners
 *
 * @version $Id: ParameterEvent.java,v 1.2 2001/10/10 07:50:02 matt Exp $
 *
 * @author Matthew Goode
 */


public class ParameterEvent extends java.util.EventObject {

	public ParameterEvent(Object source) {
		super(source);
	}
}