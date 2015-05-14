// TreeDistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.tree;

import pal.distance.*;
import pal.misc.*;


/**
 * computes distance matrix induced by a tree
 * (needs only O(n^2) time, following algorithm DistanceInTree by
 * D.Bryant and P. Wadell. 1998. MBE 15:1346-1359)
 *
 *
 * @version $Id: TreeDistanceMatrix.java,v 1.9 2002/12/05 04:27:28 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class TreeDistanceMatrix extends DistanceMatrix
{
	//
	// Public stuff
	//

	/**
	 * compute induced distance matrix
	 *
	 * @param idGroup  sequence order for the matrix
	 * @param t tree
	 * @param countEdges boolean variable deciding whether the actual
	 *                   branch lengths are used in computing the distance
	 *                   or whether simply all edges larger or equal a certain
	 *                   threshold length are counted (each with weight 1.0)
	 * @param epsilon    minimum branch length for a which an edge is counted
	 */
	public TreeDistanceMatrix(Tree t, IdGroup idGroup,  boolean countEdges, double epsilon)
	{
		super(computeDistances( t, idGroup,countEdges, epsilon), idGroup);
	}

	/**
	 * compute induced distance matrix using actual branch lengths
	 *
	 * @param idGroup  sequence order for the matrix
	 * @param t tree
	 */
	public TreeDistanceMatrix( Tree t, IdGroup idGroup)
	{
		this(t, idGroup,  false, 0.0);
	}

	/**
	 * compute induced distance matrix
	 * (using tree-induced order of sequences)
	 *
	 * @param t tree
	 * @param countEdges boolean variable deciding whether the actual
	 *                   branch lengths are used in computing the distance
	 *                   or whether simply all edges larger or equal a certain
	 *                   threshold length are counted (each with weight 1.0)
	 * @param epsilon    minimum branch length for a which an edge is counted
	 */
	public TreeDistanceMatrix(Tree t, boolean countEdges, double epsilon)
	{
		this( t, TreeUtils.getLeafIdGroup(t), countEdges, epsilon);
	}

	/**
	 * compute induced distance matrix using actual branch lengths
	 * (using tree-induced order of sequences)
	 *
	 * @param t tree
	 */
	public TreeDistanceMatrix(Tree t)
	{
		this(t, false, 0.0);
	}



	/** recompute distances (actual branch lengths) */
	private static final double[][] computeDistances(Tree tree, IdGroup idGroup)
	{
		return computeDistances(tree,  idGroup, false, 0.0);
	}

	public void recompute(Tree t) {
		IdGroup idGroup = TreeUtils.getLeafIdGroup(t);
		setIdGroup(idGroup);
		setDistances(computeDistances(t,idGroup));
	}

	/** recompute distances
	 * @param countEdges boolean variable deciding whether the actual
	 *                   branch lengths are used in computing the distance
	 *                   or whether simply all edges larger or equal a certain
	 *                   threshold length are counted (each with weight 1.0)
	 * @param epsilon    minimum branch length for a which an edge is counted
	 */
	private static final double[][] computeDistances(Tree tree, IdGroup idGroup,  boolean countEdges, double epsilon)
	{
		int numSeqs = idGroup.getIdCount();
		double[][] distance = new double[numSeqs][numSeqs];

		int[] alias = TreeUtils.mapExternalIdentifiers(idGroup, tree);

		double[] dist = new double[tree.getExternalNodeCount()];
		double[] idist = new double[tree.getInternalNodeCount()];

		// fast O(n^2) computation of induced distance matrix
		for (int i = 0; i < tree.getExternalNodeCount(); i++)
		{
			TreeUtils.computeAllDistances(tree, i, dist, idist, countEdges, epsilon);
			int ai = alias[i];

			for (int j = 0; j < tree.getExternalNodeCount(); j++)
			{
				distance[ai][alias[j]] = dist[j];
			}
		}
		return distance;

	}

}
