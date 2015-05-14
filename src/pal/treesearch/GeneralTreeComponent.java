// GeneralTreeComponent.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GeneralTreeComponent</p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

public interface GeneralTreeComponent {
	public void getAllComponents(ArrayList store, Class componentType);
}