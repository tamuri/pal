// JukesCantorDistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;

import pal.alignment.*;
import pal.misc.*;


/**
 * compute jukes-cantor corrected distance matrix
 *
 * @version $Id: JukesCantorDistanceMatrix.java,v 1.5 2002/12/05 04:27:28 matt Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public class JukesCantorDistanceMatrix extends DistanceMatrix
{
	//
	// Public stuff
	//

	/**
	 * compute jukes-cantor corrected distances
	 * (assumes nucleotides as underlying data)
	 *
	 * @param dist distance matrix
	 */
	public JukesCantorDistanceMatrix(DistanceMatrix dist)
	{
		this(dist, 4);
	}


	/**
	 * compute jukes-cantor corrected distances
	 *
	 * @param dist distance matrix
	 * @param numStates number of states of underlying data
	 */
	public JukesCantorDistanceMatrix(DistanceMatrix dist, int numStates)
	{
		super(computeDistances(dist, numStates), dist);

	}


	/**
	 * compute jukes-cantor corrected distances
	 *
	 * @param alignment Alignment
	 */
	public JukesCantorDistanceMatrix(Alignment alignment)
	{
		this(new SitePattern(alignment));
	}

	/**
	 * compute jukes-cantor corrected distances
	 *
	 * @param sitePattern SitePattern
	 */
	public JukesCantorDistanceMatrix(SitePattern sitePattern)
	{
		this(	new AlignmentDistanceMatrix(sitePattern),
			sitePattern.getDataType().getNumStates());
	}

	private static final double[][] computeDistances(final DistanceMatrix dist, final int numberOfStates)
	{
		final int numSeqs = dist.getSize();
		final double[][] distance = new double[numSeqs][numSeqs];
		final double[][] obsDistance = dist.getDistances();
		final double const1 = (numSeqs-1)/(double)numSeqs;
		final double const2 = numSeqs/(double)(numSeqs-1);
		for (int i = 0; i < numSeqs-1; i++)
		{
			distance[i][i] = 0.0;
			for (int j = i+1; j < numSeqs; j++)
			{
				distance[i][j] = distance[j][i] = jccorrection(const1, const2, obsDistance[i][j]);
			}
		}
		return distance;
	}


	private static final double jccorrection(final double const1,  final double const2, double obsdist)
	{
		if (obsdist == 0.0) return 0.0;

		if (obsdist >= const1)
		{
			return BranchLimits.MAXARC;
		}

		double expDist = -const1 * Math.log(1.0 - (const2 * obsdist));

		if (expDist < BranchLimits.MAXARC)
		{
			return expDist;
		}
		else
		{
			return BranchLimits.MAXARC;
		}
	}
}
