// DistanceTool.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.distance;

/**
 * Simple access for distance functions. The purpose of this class is to provide a set
 * interface for doing basic distance matrix operations.
 *
 * <b>History</b>
 * <ul>
 *  <li> 15/09/2003 - Created </li>
 * </ul>
 *
 * @version $Id: DistanceTool.java,v 1.1 2003/09/16 03:54:18 matt Exp $
 *
 * @author Matthew Goode
 *
 */

import pal.alignment.*;
import pal.substmodel.*;

public final class DistanceTool {
	/**
	 * Construct a distance matrix object such that the distance between sequence A, and sequence B, is the
	 * evolutionary distance by a given substitution model. The evolutionary distance is the branch length on the
	 * maximum likelihood tree consisting of only sequences A and B at the tips and under the given model
	 * of substitution.
	 * @param a The aligned set of sequences. The resulting distance matrix has defines a distance between each and every sequence in the input alignment to every other sequence.
	 * @param sm The model under which the maximum likelihood calculation is done. The model is not optimised.
	 * @return The relating distance matrix of evolutionary distances.
			*/
  public static final DistanceMatrix constructEvolutionaryDistances(Alignment a, SubstitutionModel sm) {
		return new AlignmentDistanceMatrix(SitePattern.getSitePattern(a),sm);
	}
}