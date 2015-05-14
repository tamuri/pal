// SimpleLikelihoodCalculator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * Only to be used by one thread. Based on the LikelihoodValue class but does not allow the use of
 * a rate distribution.
 * @author Korbinian Strimmer
 * @author Matthew Goode
 * @deprecated see new likelihood framework
 */

import pal.alignment.*;
import pal.datatype.*;
import pal.substmodel.*;
import pal.tree.*;

public class SimpleLikelihoodCalculator implements LikelihoodCalculator {


	SitePattern sitePattern_;
	Tree tree_;
	RateMatrix model_;
	DataType patternDatatype_;


	boolean modelChanged_ = false;

	/* Work variables */
	private double[][][] partials_; /**  [numberOfNodes]
																				 [numberOfPatterns]
																				 [numberOfStates] */

	/* Cached variables */
	private int numberOfStates_;
	private int numberOfPatterns_;
	//private int numberOfNodes_;
	private double[] frequency_;

	/** log-likelihood for each site pattern */
	private double[] siteLogL_;

	/**
		Need to use setTree(), and setModel() before using compute() if you use this constructor */
	public SimpleLikelihoodCalculator(SitePattern pattern) {
		setPattern(pattern);
	}

	private void setPattern(SitePattern pattern) {
		this.sitePattern_ = pattern;
		this.patternDatatype_ = sitePattern_.getDataType();

		numberOfPatterns_ = sitePattern_.numPatterns;
		siteLogL_ = new double[numberOfPatterns_];
	}

	public SimpleLikelihoodCalculator(SitePattern pattern, Tree tree, RateMatrix model) {
		setPattern(pattern);
		setTree(tree);
		setRateMatrix(model);
	}
	/**
	 * Doesn't do anything...
	 */
	public void release() {	}

	/**
	 * compute log-likelihood for current tree (fixed branch lengths and model)
	 *
	 * return log-likelihood
	 */
	public double calculateLogLikelihood()	{
		return treeLikelihood();
	}

	public SitePattern getSitePattern() {
		return sitePattern_;
	}

	public Tree getTree() {
		return tree_;
	}


	/**
	 * define model
	 * (a site pattern must have been set before calling this method)
	 *
	 * @param m model of substitution (rate matrix + rate distribution)
	 */
	public void setRateMatrix(RateMatrix m) {

		if(m==null) {
			throw new RuntimeException("Assertion error : SetModel called with null model!");
		}

		model_ = m;

		frequency_ = model_.getEquilibriumFrequencies();
		numberOfStates_ = model_.getDataType().getNumStates();

		int maxNodes = 2*sitePattern_.getSequenceCount()-2;

		allocatePartialMemory(maxNodes);
	}

	/**
	 * define tree
	 *,(must only be called only after a site pattern has been defined).
	 *
	 * @param t tree
	 */
	public void setTree(Tree t) {
		tree_ = t;
		if(t==null) {
			throw new RuntimeException("Assertion error : SetTree called with null tree!");
		}

		// Assign sequences to leaves
		int[] alias =
				TreeUtils.mapExternalIdentifiers(sitePattern_, tree_);
		for (int i = 0; i < tree_.getExternalNodeCount(); i++)
		{
			tree_.getExternalNode(i).setSequence(sitePattern_.pattern[alias[i]]);
		}
	}

	public final void modelUpdated() {
		setRateMatrix(model_);
	}

	public final void treeUpdated() {
		setTree(tree_);
	}


//===============================================================================
//======================= Non Public Stuff ======================================
//===============================================================================


	private void allocatePartialMemory(int numberOfNodes) {

		// I love the profiler!
		// This 'if' statement sped my MCMC algorithm up by nearly 300%
		// Never underestimate the time it takes to allocate and de-allocate memory!
		// AD
		if (
			(partials_ == null) ||
			(numberOfNodes != partials_.length) ||
			(numberOfPatterns_ != partials_[0].length) ||
			(numberOfStates_ != partials_[0][0].length)) {

			partials_ = new double[numberOfNodes][numberOfPatterns_][numberOfStates_];
		}
	}

	private int getKey(Node node) 	{
		if (node.isLeaf()) {
			return node.getNumber();
		}
		return node.getNumber() + tree_.getExternalNodeCount();
	}


	/** get partial likelihood of a branch */
	protected double[][] getPartial(Node branch) {
		return partials_[getKey(branch)];
	}


	/** get next branch around a center node
		 (center may be root, and root may also be returned) */
	private Node getNextBranchOrRoot(Node branch, Node center) {
		int numChilds = center.getChildCount();

		int num;
		for (num = 0; num < numChilds; num++)	{
			if (center.getChild(num) == branch)	{
				break;
			}
		}
		// num is now child number (if num = numChilds then branch == center)
		// next node
		num++;

		if (num > numChilds) {
			num = 0;
		}
		if (num == numChilds)	{
			return center;
		}	else {
			return center.getChild(num);
		}
	}


	/** get next branch around a center node
		 (center may be root, but root is never returned) */
	protected Node getNextBranch(Node branch, Node center) {
		Node b = getNextBranchOrRoot(branch, center);
		if (b.isRoot())	{
			b = b.getChild(0);
		}
		return b;
	}

	/** multiply partials into the neighbour of branch */
	protected void productPartials( Node center) {
		int numBranches = NodeUtils.getUnrootedBranchCount(center);
		Node nextBranch = center.getChild(0);
		double[][] partial = getPartial(nextBranch);

		for (int i = 1; i < center.getChildCount(); i++) {
			nextBranch = center.getChild(i);
			double[][] partial2 = getPartial(nextBranch);

			for (int patternIndex = 0; patternIndex < numberOfPatterns_; patternIndex++)	{
				double[] p = partial[patternIndex];
				double[] p2 = partial2[patternIndex];

				for (int state = 0; state < numberOfStates_; state++)	{
					p[state] *= p2[state];
				}
			}
		}
	}


	/** compute partials for branch around center node
			(it is assumed that multiplied partials are available in
			the neighbor branch) */
	protected void partialsInternal( Node center) {
		double[][] partial = getPartial(center);
		double[][] multPartial = getPartial(center.getChild(0));

		model_.setDistance(center.getBranchLength());
		for (int l = 0; l < numberOfPatterns_; l++)	{
			double[] p = partial[l];
			double[] mp = multPartial[l];

			for (int d = 0; d < numberOfStates_; d++)	{
				double sum = 0;
				for (int j = 0; j < numberOfStates_; j++)					{
					sum += model_.getTransitionProbability(d, j)*mp[j];
				}
				p[d] = sum;
			}
		}
	}

	/** compute partials for external branch */
	protected void partialsExternal(Node branch)	{
		double[][] partial = getPartial(branch);
		byte[] seq = branch.getSequence();

		model_.setDistance(branch.getBranchLength());

		for (int patternIndex = 0; patternIndex < numberOfPatterns_; patternIndex++)	{
			double[] p = partial[patternIndex];
			int endState = seq[patternIndex];
			if(patternDatatype_.isUnknownState(endState)) { //A neater way of writing things but it may slow things down...
			//if (endState == numberOfStates_) { //Is this an gap? (A gap should be registered as unknown!)
				for (int startState = 0; startState < numberOfStates_; startState++)	{
					p[startState] = 1;
				}
			}	else {
				for (int startState = 0; startState < numberOfStates_; startState++)	{
					p[startState] = model_.getTransitionProbability( startState, endState);
				}
			}
		}
	}

	private void traverseTree(Node currentNode){
		if(currentNode.isLeaf()){
			partialsExternal(currentNode);
		} else {
			for(int i = 0 ; i < currentNode.getChildCount() ; i++) {
				traverseTree(currentNode.getChild(i));
			}
			if(!currentNode.isRoot()) {
				productPartials(currentNode);
				partialsInternal(currentNode);
			}
		}
	}

	/** returns number of branches centered around an internal node */
	private int getBranchCount(Node center) {
		if (center.isRoot()) 	{
			return center.getChildCount();
		}
		else {
			return center.getChildCount()+1;
		}
	}




	/** calculate likelihood of any tree and infer MAP estimates of rates at a site */
	private double treeLikelihood()
	{
		//initPartials();

		Node center = tree_.getRoot();
		traverseTree(center);
		Node firstBranch = center.getChild(0);
		Node lastBranch = center.getChild(center.getChildCount()-1);

		double[][] partial1 = getPartial(firstBranch);

		productPartials(center);

		double logL = 0;
		for (int patternIndex = 0; patternIndex < numberOfPatterns_; patternIndex++)
		{
			double sum = 0;
			double[] p1 = partial1[patternIndex];

			for (int d = 0; d < numberOfStates_; d++)	{
				sum += frequency_[d]*p1[d];
			}

			siteLogL_[patternIndex] = Math.log(sum);
			logL += siteLogL_[patternIndex]*sitePattern_.weight[patternIndex];
		}
		return logL;
	}
}
