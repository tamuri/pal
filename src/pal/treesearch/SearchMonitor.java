// SearchMonitor.java
//
// (c) 1999-2003 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: SearchMonitor </p>
 * <p>Description: A class that receives information about the state of the search </p>
 * @author Matthew Goode
 * @version 1.0
 */

public interface SearchMonitor {
	/**
	 * When this method is called, it should be safe to access the tree search methods (for example, to build a pal tree)
			* @param logLikelihood
			*/
	public void searchStepComplete(double logLikelihood);
	public static final class Utils {
	  public static final SearchMonitor createNullMonitor() {
		  return Null.INSTANCE;
		}
		private static final class Null implements SearchMonitor {
			public static final SearchMonitor INSTANCE = new Null();
		  private Null() { }
			public void searchStepComplete(double logLikelihood) {}
		}
	}
}