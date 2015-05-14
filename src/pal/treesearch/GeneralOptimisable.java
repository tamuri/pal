// GeneralOptimisable.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GeneralOptimisable </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.math.*;

public interface GeneralOptimisable {
	public int getNumberOfOptimisationTypes();

	/**
	 *
	 * @param minimiser The single dimensional minimisation tool
	 * @param tool The construction tool
	 * @param fracDigits the number of fractional digits to converge to
	 * @return The optimised log likelihood, or >0 if not optimisation occured
	 */
	public double optimise(int optimisationType, UnivariateMinimum minimiser, GeneralConstructionTool tool, int fracDigits);

}