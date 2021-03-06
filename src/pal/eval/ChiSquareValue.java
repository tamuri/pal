// ChiSquareValue.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.eval;

import pal.distance.*;
import pal.math.*;
import pal.misc.*;
import pal.tree.*;


/**
 * computes chi-square value of a (parameterized) tree for
 * its set of parameters (e.g., branch lengths)
 * and a given distance matrix
 *
 * @version $Id: ChiSquareValue.java,v 1.14 2002/12/05 04:27:28 matt Exp $
 *
 * @author Korbinian Strimmer
 */
public class ChiSquareValue implements MultivariateFunction
{
	//
	// Public stuff
	//

	/**
	 * initialization
	 *
	 * @param m distance matrix
	 * @param w determines whether weighted or
	 *        unweighted chi-squares are computed
	 */
	public ChiSquareValue(DistanceMatrix m, boolean w)
	{
		givenMat = m;
		weighted = w;
	}


	/**
	 * define (parameterized) tree
	 *
	 * @param t tree
	 */
	public void setTree(Tree t)
	{
		tree = t;

		//changed so that smaller trees could be easily handled
		//for tree-search purposes
		inducedMat = new TreeDistanceMatrix(tree);

		if (tree instanceof ParameterizedTree)
		{
			ptree = (ParameterizedTree) tree;
			numParams = ptree.getNumParameters();
		}
		else
		{
			ptree = null;
			numParams = 0;
		}
	}


	/**
	 * Returns the (parameterized) tree of this likelihood value.
	 */
	public Tree getTree()
	{
		return tree;
	}


	/**
	 * compute (weighted) least-square value
	 * for current tree (fixed branch lengths)
	 *
	 * return chi-square value
	 */
	public double compute()
	{
		inducedMat.recompute(tree);

		return DistanceMatrixUtils.squaredDistance(inducedMat,
			givenMat, weighted);
	}

	/**
	 * optimise parameters of a tree by minimising its chi-square value
	 * (tree must be a ParameterizedTree)
	 *
	 * @return minimimum chi-square value
	 */
	public double optimiseParameters()
	{
		return optimiseParameters(null);
	}


	/**
	 * optimise parameters of a tree by minimising its chi-square value
	 * (tree must be a ParameterizedTree)
	 *
	 * @param mm optimiser for ParameterizedTree
	 *
	 * @return minimum chi-square value
	 */
	public double optimiseParameters(MultivariateMinimum mm)
	{
		if (!(tree instanceof ParameterizedTree))
		{
			// we need a ParameterizedTree here!
			new IllegalArgumentException("ParameterizedTree required");
		}

		if (mm == null)
		{
			if (mvm == null) mvm = new DifferentialEvolution(numParams);
		}
		else
		{
			mvm = mm;
		}

		// first guess are the default parameters of the tree
		double[] estimate = new double[numParams];
		for (int i = 0; i < numParams; i++)
		{
			estimate[i] = ptree.getDefaultValue(i);
		}

		mvm.findMinimum(this, estimate, BranchLimits.FRACDIGITS, BranchLimits.FRACDIGITS);

		return evaluate(estimate);
	}



	// interface MultivariateFunction

	/**
	 * compute (weighted) least-squares value
	 *
	 * @param params parameters (branch lengths) of the tree
	 */
	public double evaluate(double[] params)
	{
		for (int i = 0; i < numParams; i++)
		{
			ptree.setParameter(params[i], i);
		}

		return compute();
	}

	/**
	 * get number of parameters in tree
	 *
	 * @return number of parameters
	 */
	public int getNumArguments()
	{
		return ptree.getNumParameters();
	}

	public double getLowerBound(int n)
	{
		return ptree.getLowerLimit(n);
	}

	public double getUpperBound(int n)
	{
		return ptree.getUpperLimit(n);
	}

	/**
	 * @return null
	 */
	public OrthogonalHints getOrthogonalHints() { return null; }

	//
	// Private stuff
	//

	private int numParams;
	private Tree tree;
	private ParameterizedTree ptree;
	private DistanceMatrix givenMat;
	private TreeDistanceMatrix inducedMat;
	private boolean weighted;
	private MultivariateMinimum mvm = null;


}
