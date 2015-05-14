// TreeIterator.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

/**
 * An interface for classes that iterate through trees. Useful for cases where large numbers of
 * trees are generator (like bootstrapping) but, to save memory, it is better to
 * generate the trees on the fly, instead of pregenerating them and storing in an
 * array.
 *
 * @version $Id: TreeIterator.java,v 1.2 2004/04/25 22:53:14 matt Exp $
 *
 * @author Matthew Goode
 */
import pal.util.AlgorithmCallback;

public interface TreeIterator extends TreeGenerator {

	/**
	 * @return true if more trees to come
	 */
	public boolean isMoreTrees();
}
