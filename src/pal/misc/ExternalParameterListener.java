// ExternalParameterListener.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
package pal.misc;


/**
 * Defines objects that listen to exteneral ParameterEvents
 *
 * @version $Id: ExternalParameterListener.java,v 1.3 2001/10/10 07:50:02 matt Exp $
 *
 * @author Matthew Goode
 */


public interface ExternalParameterListener {
	void parameterChanged(ParameterEvent pe);
}
