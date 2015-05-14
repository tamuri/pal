// UnconstrainedTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.misc.*;


/**
 * provides parameter interface to an unconstrained tree
 * (parameters are all available branch lengths)
 *
 * @version $Id: UnconstrainedTree.java,v 1.13 2004/04/25 22:53:14 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class UnconstrainedTree extends ParameterizedTree.ParameterizedTreeBase  implements ParameterizedTree
{
	//
	// Public stuff
	//

	/**
	 * take any tree and afford it with an interface
	 * suitable for an unconstrained tree (parameters
	 * are all available branch lengths)
	 */
	public UnconstrainedTree(Tree t)
	{
		setBaseTree(t);

		if (getRoot().getChildCount() < 3)
		{
			throw new IllegalArgumentException(
			"The root node must have at least three childs!");
		}

		// set default values
		for (int i = 0; i < getNumParameters(); i++)
		{
			setParameter(getDefaultValue(i), i);
		}
	}

	protected UnconstrainedTree(UnconstrainedTree toCopy) {
		super(toCopy);
	}
	// interface Parameterized

	public int getNumParameters()
	{
		return getInternalNodeCount()+getExternalNodeCount()-1;
	}

	public void setParameter(double param, int n)
	{
		if (n < getExternalNodeCount())
		{
			getExternalNode(n).setBranchLength(param);
		}
		else
		{
			getInternalNode(n-getExternalNodeCount()).setBranchLength(param);
		}
	}
	public String getParameterizationInfo() {
		return "Unconstrained tree";
	}
	public double getParameter(int n)
	{
		if (n < getExternalNodeCount())
		{
			return getExternalNode(n).getBranchLength();
		}
		else
		{
			return getInternalNode(n-getExternalNodeCount()).getBranchLength();
		}
	}

	public void setParameterSE(double paramSE, int n)
	{
		if (n < getExternalNodeCount())
		{
			getExternalNode(n).setBranchLengthSE(paramSE);
		}
		else
		{
			getInternalNode(n-getExternalNodeCount()).setBranchLengthSE(paramSE);
		}
	}

	public double getLowerLimit(int n)
	{
		return BranchLimits.MINARC;
	}

	public double getUpperLimit(int n)
	{
		return BranchLimits.MAXARC;
	}

	public double getDefaultValue(int n)
	{
		return BranchLimits.DEFAULT_LENGTH;
	}

	public Tree getCopy() {
		return new UnconstrainedTree(this);
	}
// ===========================================================================
// ===== Static stuff =======

	/**
	 * Obtain a ParameterizedTree.Factory for generating Unconstrained trees
	 * @note Factory automatically converts "rooted" trees (bificating root) to "unrooted" trees (trificating...)
	 */
	public static final ParameterizedTree.Factory getParameterizedTreeFactory() {
		return TreeFactory.DEFAULT_INSTANCE;
	}

	private static class TreeFactory implements ParameterizedTree.Factory {
		public static final ParameterizedTree.Factory DEFAULT_INSTANCE = new TreeFactory();
		/**
		 * Automatically unroots rooted trees!
		 */
		public ParameterizedTree generateNewTree(Tree base) {
			if(base.getRoot().getChildCount()==2) {
				base = new TreeManipulator(base).getUnrootedTree();
			}
			return new UnconstrainedTree(base);
		}
	}
}
