// LikelihoodCalculator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;
import pal.substmodel.*;
import pal.tree.*;

/**
 * classes that calculate likelihoods should implement this interface.
 * @author Matthew Goode
 * @version $Id: LikelihoodCalculator.java,v 1.3 2002/09/08 03:46:12 matt Exp $
 */

public interface LikelihoodCalculator {
	double calculateLogLikelihood();
	/**
	 * This method should be called when the user is through using this calculator
	 * to inform it to release any resources it has allocated.
	 */
	void release();
}
