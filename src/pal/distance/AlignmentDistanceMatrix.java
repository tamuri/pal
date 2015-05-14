// AlignmentDistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

// Known bugs and limitations:
// - computational complexity of order O(numSeqs^2)


package pal.distance;

import java.io.*;

import pal.alignment.*;
import pal.substmodel.*;
import pal.util.*;


/**
 * compute distance matrix (observed and ML) from alignment (SitePattern)
 *
 * @version $Id: AlignmentDistanceMatrix.java,v 1.10 2003/03/23 00:13:36 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class AlignmentDistanceMatrix extends DistanceMatrix implements Serializable {
	//
	// Public stuff
	//
	/**
	 * compute observed distances
	 *
	 * @param sp site pattern
	 */
	public AlignmentDistanceMatrix(SitePattern sp)
	{
		this(sp,null,null);
	}
	/**
	 * compute observed distances
	 *
	 * @param sp site pattern
	 * @param callback An algorithm callback to monitor progress
	 */
	public AlignmentDistanceMatrix(SitePattern sp, AlgorithmCallback callback)
	{
		this(sp,null,callback);
	}
	/**
	 * compute observed distances
	 *
	 * @param sp site pattern
	 * @param immediateCompute - signifies whether to calculate distances from within constructor
	 *	(if no should call recompute() at some point!)
	 *
	 */
//	public AlignmentDistanceMatrix(SitePattern sp, boolean immediateCompute)
//	{
//		this(sp,null,immediateCompute);
//	}
	/**
	 * compute maximum-likelihood distances
	 *
	 * @param sp site pattern
	 * @param m  evolutionary model
	 */
//	public AlignmentDistanceMatrix(SitePattern sp, SubstitutionModel m) {
//		this(sp,m,true);
//	}
	/**
	 * compute maximum-likelihood distances
	 *
	 * @param sp site pattern
	 * @param m  evolutionary model
	 * @param immediateCompute - signifies whether to calculate distances from within constructor
	 *	(if no should call recompute() at some point!)
	 */
	public AlignmentDistanceMatrix(SitePattern sp, SubstitutionModel m)
	{
		this(sp,m,null);
	}
	/**
	 * compute maximum-likelihood distances
	 *
	 * @param sp site pattern
	 * @param m  evolutionary model
	 * @param callback An algorithm callback to monitor progress
	 *
	 */
	public AlignmentDistanceMatrix(SitePattern sp, SubstitutionModel m, AlgorithmCallback callback) {
		super(computeDistances(sp,m,callback),sp);
	}



	/**
	 * recompute observed distances under new site pattern
	 *
	 * @param sp site pattern
	 * @note no longer maintains previous model!
	 */
	public void recompute(SitePattern sp, AlgorithmCallback callback)	{
		recompute(sp,null,callback);
	}
	/**
	 * recompute maximum-likelihood distances under new site pattern
	 *
	 * @param sp site pattern
	 */
	public void recompute(SitePattern sp, SubstitutionModel model) {
		recompute(sp,model,null);
	}

	/**
	 * recompute maximum-likelihood distances under new site pattern
	 *
	 * @param sp site pattern
	 */
	public void recompute(SitePattern sp, SubstitutionModel model,  AlgorithmCallback callback)
	{
		setIdGroup(sp);
		setDistances(computeDistances(sp,model, callback));
	}

	private static final double[][] computeDistances(SitePattern sp, SubstitutionModel m, AlgorithmCallback callback) {
		int numSeqs = sp.getSequenceCount();
		double[][] distance = new double[numSeqs][numSeqs];
		PairwiseDistance pwd;
		if(m!=null) {
			pwd = new PairwiseDistance(sp, m);
		} else {
			pwd = new PairwiseDistance(sp);
		}

		for (int i = 0; i < numSeqs; i++) {
			distance[i][i] = 0;
			for (int j = i + 1; j < numSeqs; j++)
			{
				if(callback!=null) {
					if(callback.isPleaseStop()) { return null; }
					callback.updateProgress(2*(i*numSeqs+j)/((double)(2*numSeqs*numSeqs)));
				}
				distance[i][j] = pwd.getDistance(i, j);
				distance[j][i] = distance[i][j];
			}
		}
		if(callback!=null) { 	callback.clearProgress();  }
		return distance;
	}
}
