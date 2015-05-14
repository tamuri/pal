// PalObjectEvent.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)
package pal.misc;

/**
 * An event used by PalObjectListeners
 *
 * @version $Id: PalObjectEvent.java,v 1.3 2002/12/05 04:27:28 matt Exp $
 *
 * @author Matthew Goode
 */

public class PalObjectEvent extends java.util.EventObject {
	public static final Object NO_SOURCE = new Object();
	public PalObjectEvent(Object source) {	super(source);	}
	public PalObjectEvent() {	super(NO_SOURCE);	}
	public boolean hasSource() {  return getSource()!=NO_SOURCE; }

}