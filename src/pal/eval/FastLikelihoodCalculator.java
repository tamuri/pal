// FastLikelihoodCalculator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

import pal.tree.*;
import pal.alignment.*;
import pal.substmodel.*;
import pal.datatype.*;
import pal.misc.*;

import java.util.*;

/**
 * <B>Title:</B> Fast Likelihood Calculator <BR>
 * <B>Description:</B>  A fast likelihood calculator<BR>
 * Original code by Matthew Goode. This calculates the likelihood of similar trees
 * (or a single changing tree) on the same site pattern fast by remembering
 * partial likelihoods of invariant subtrees.
 *
 * This class should be avoided if the underlying tree doesn't change (use GeneralLikelihoodCalculator).
 * A new framework will be added one day to accomodate optimising tree topologies.
 *
 * @author Matthew Goode
 * @author Alexei Drummond
 * @version $Id: FastLikelihoodCalculator.java,v 1.11 2002/09/08 03:46:12 matt Exp $
 */

public class FastLikelihoodCalculator implements PalObjectListener, LikelihoodCalculator {

	/** the root node of the cached tree. */
	RootNode root_;

	/** true if the rate matrix has changed. */
	boolean modelChanged_ = false;

	/** the number of sites in the site pattern. */
	int numberOfSites_;
	int numberOfStates_;

	/** the rate matrix currently being used. */
	RateMatrix model_;

	/** the site pattern for which likelihood is calculated. */
	SitePattern sitePattern_;

	/**
	 * the difference threshold within which
	 * different branch lengths are deemed the same.
	 */
	private static double THRESHOLD = 1e-12;

	/**
	 * Constructor taking only site pattern. <BR>
	 * <B>NOTE:</B> setTree and setRateMatrix must both be called (in that order) before
	 * computeLikelihood.
	 */
	public FastLikelihoodCalculator(SitePattern pattern) {
		sitePattern_ = pattern;
		numberOfSites_ = pattern.getNumberOfPatterns();
		numberOfStates_ = pattern.getDataType().getNumStates();
	}

	/**
	 * Constructor taking site pattern, tree and model.
	 */
	public FastLikelihoodCalculator(SitePattern pattern, Tree tree, RateMatrix model) {
		this(pattern);
		setTree(tree);
		setRateMatrix(model);
	}

	public void parametersChanged(PalObjectEvent pe) {
		modelChanged_ = true;
	}

	public void structureChanged(PalObjectEvent pe) {
		modelChanged_ = true;
	}

	public final void setRateMatrix(RateMatrix rateMatrix) {

		if ((model_ == null) || (model_ != rateMatrix)) {
			this.model_ = rateMatrix;
			model_.addPalObjectListener(this);
			root_.setModel(model_);
			modelChanged_ = true;
		} // same rate matrix, do nothing
	}

	public void release() {
		try{
			model_.removePalObjectListener(this);
			model_ = null;
		} catch(NullPointerException e) {}
	}

	public final void setTree(Tree t) {

		if (root_ == null) {
			root_ = new RootNode(t.getRoot());
		} else {
			NNode newNode = root_.switchNodes(t.getRoot());
			if(newNode!=root_) {
				throw new RuntimeException("Assertion error : new tree generates new Root NNode (tree probably contains only one branch)");
			}

			// must call this to generate transition prob arrays for new nodes!
			root_.setModel(model_);
		}
		// could be more efficient
		root_.setupSequences(sitePattern_);
	}

	public final void updateSitePattern(SitePattern pattern) {
		sitePattern_ = pattern;
		root_.setupSequences(pattern);
		if(pattern.numPatterns!=numberOfSites_) {
			numberOfSites_ = pattern.numPatterns;
			root_.setModel(model_);
			modelChanged_ = true;
		}
	}

	/**
	 * @return the likelihood of this tree under the given model and data.
	 */
	public double calculateLogLikelihood() {
		double lkl = root_.computeLikelihood();
		return lkl;
	}

	final NNode create(Node peer) {
		if(peer.getChildCount()==0) {
			return new LeafNode(peer);
		}
		return new InternalNode(peer);
	}

	//=====================================================================
	//
	// Abstract NNODE
	//
	//=====================================================================

	abstract class NNode {

		private double[][] transitionProbs_;
		double lastLength_ = Double.NEGATIVE_INFINITY;
		Node peer_;
		private byte[] sequence_;

		private double[][] siteStateProbabilities_;/** Site/State */

		public NNode(Node peer) {

			this.peer_ = peer;
		}

		public void setModel(RateMatrix rm) {
			// only create these arrays if they are null
			// or the wrong size
			if ((transitionProbs_ == null) || (numberOfStates_ != transitionProbs_.length)) {
				transitionProbs_ = new double[numberOfStates_][numberOfStates_];

				siteStateProbabilities_ = new double[numberOfSites_][numberOfStates_];
			}
		}

		protected void setPeer(Node newPeer) {
			this.peer_ = newPeer;
		}

		public final boolean isBranchLengthChanged() {
			return Math.abs(peer_.getBranchLength()-lastLength_) > THRESHOLD;
			//return peer_.getBranchLength()!=lastLength_;
		}

		protected final double[][] getSiteStateProbabilities() {
			return siteStateProbabilities_;
		}

		public final void setSequence(byte[] sequence) {
			this.sequence_ = pal.misc.Utils.getCopy(sequence);
			for(int i = 0 ; i < sequence_.length ; i++) {
				if(sequence[i]>=numberOfStates_) {
					sequence_[i] = -1;
				}
			}
		}

		public final boolean hasSequence() {
			return this.sequence_!=null;
		}
		public final byte[] getSequence() {
			return this.sequence_;
		}

		protected double[][] getTransitionProbabilities() {

			if(modelChanged_||isBranchLengthChanged()) {
				double distance = peer_.getBranchLength();
				model_.setDistance(distance);
				model_.getTransitionProbabilities(transitionProbs_);
				lastLength_ = distance;
			}
			return transitionProbs_;
		}
		protected double[][] getTransitionProbabilitiesReverse() {
			if(modelChanged_||isBranchLengthChanged()) {
				double distance = peer_.getBranchLength();
				model_.setDistance(distance);
				model_.getTransitionProbabilities(transitionProbs_);
				lastLength_ = distance;
			}
			return transitionProbs_;
		}

		private String toString(byte[] bs) {
			char[] cs = new char[bs.length];
			for(int i = 0 ; i < cs.length ; i++) {
				cs[i] = (char)('A'+bs[i]);
			}
			return new String(cs);
		}

		public void setupSequences(SitePattern sp) {
			Identifier id = peer_.getIdentifier();
			if(id!=null) {
				int number = sp.whichIdNumber(id.getName());
				if(number>=0) {
					if (sequence_ == null) {
						sequence_ = new byte[sp.pattern[number].length];
					}
					//System.arraycopy(sp.pattern[number],0,sequence_,0,sequence_.length);
					byte[] pattern = sp.pattern[number];
					for(int i = 0 ; i < sequence_.length ; i++) {
						if(pattern[i]>=numberOfStates_) {
							sequence_[i] = -1;
						} else {
							sequence_[i] = pattern[i];
						}
					}
				}
			}
		}
		/** Can return null if no change from previous call */
		abstract public double[][] calculateSiteStateProbabilities();

		abstract public LeafNode[] getLeafNodes();
		/** For dynamically switching tree */
		abstract public NNode switchNodes(Node n);

	}

//==========================================================================================
//
// LEAF NODE
//
//==============================
	class LeafNode extends NNode {

		public LeafNode(Node peer) {
			super(peer);
		}

		public double computeLikelihood() {
			return 0;
		}
		public boolean isLeaf() {
			return true;
		}

		protected final void setPeer(Node newPeer) {
			// if this is a different tip then force recalculate!
			if (!peer_.getIdentifier().getName().equals(newPeer.getIdentifier().getName())) {
				lastLength_ = Double.NEGATIVE_INFINITY;
			}
			this.peer_ = newPeer;
		}

		public NNode switchNodes(Node n) {
			if(n.getChildCount()==0) {
				setPeer(n);
				return this;
			}
			return create(n);
		}
		/**
		 * Return all the leaf nodes in the tree defined by this node as the root (including this node)
		 */
		public LeafNode[] getLeafNodes() {
			return new LeafNode[] {this};
		}

		public double[][] calculateSiteStateProbabilities() {
			if(!modelChanged_&&!isBranchLengthChanged()) {
				return null;
			}

			byte[] sequence = getSequence();
			double[][] probs = getTransitionProbabilitiesReverse();
			double[][] siteStateProbs = getSiteStateProbabilities();
			for(int site = 0 ; site < sequence.length ; site++) {
				int endState = sequence[site];
				if(endState<0) {
					for(int startState = 0 ; startState < numberOfStates_ ; startState++) {
						siteStateProbs[site][startState] = 1;
					}
				} else {
					//System.arraycopy(probs[eState],0,siteStateProbs[site],0,numberOfStates_);
					for(int startState = 0 ; startState < numberOfStates_ ; startState++) {
						siteStateProbs[site][startState] = probs[startState][endState];
					}
				}
			}
			return siteStateProbs;
		 }
	}

	//=====================================================================
	//
	// Internal NODE
	//
	//=====================================================================
	class InternalNode extends NNode{
		private NNode[] children_;
		private double[][][] childSiteStateProbs_;
		double[] endStateProbs_;

		public InternalNode(Node peer) {
			super(peer);
			this.children_ = new NNode[peer.getChildCount()];
			for(int i = 0 ; i < children_.length ; i++) {
				children_[i] = create(peer.getChild(i));
			}
			childSiteStateProbs_ = new double[children_.length][][];
		}

		public void setModel(RateMatrix rm) {
			super.setModel(rm);
			if ((endStateProbs_ == null) || (numberOfStates_ != endStateProbs_.length)) {
				endStateProbs_ = new double[numberOfStates_];
			}
			for(int i = 0 ; i < children_.length ; i++) {
				children_[i].setModel(rm);
			}
		}

		public void setupSequences(SitePattern sp) {
			super.setupSequences(sp);
			for(int i= 0 ; i < children_.length ; i++) {
				children_[i].setupSequences(sp);
			}
		}

		public boolean isLeaf() {
			return false;
		}

		private final boolean populateChildSiteStateProbs() {
			double[][] ss;
			boolean allNull = true;
			for(int i = 0; i < children_.length ; i++) {
				ss = children_[i].calculateSiteStateProbabilities();
				if(ss!=null) {
					childSiteStateProbs_[i] = ss;
					allNull = false;
				} else {
					if(childSiteStateProbs_[i]==null) {
						throw new RuntimeException("Assertion error : Not as should be!");
					}
				}
			}
			return allNull;
		}

		protected final int getNumberOfChildren() {
			return children_.length;
		}

		public NNode switchNodes(Node n) {
			int nc = n.getChildCount();
			if (nc == 0) {
				//We become a leaf!
				return create(n);
			}
			if(nc != children_.length) {
				NNode[] newChildren = new NNode[nc];
				for(int i = 0 ; i < nc ; i++) {
					if(i<children_.length) {
						newChildren[i] = children_[i].switchNodes(n.getChild(i));
					} else {
						newChildren[i] = create(n.getChild(i));
					}
				}
				children_ = newChildren;
			} else {
				for(int i = 0 ; i < nc ; i++) {
					children_[i] = children_[i].switchNodes(n.getChild(i));
				}
			}
			setPeer(n);
			return this;
		}

		public double[][] calculateSiteStateProbabilities() {

			if(populateChildSiteStateProbs()&&!modelChanged_&&!isBranchLengthChanged()) {
				return null;
			}
			double[][] probs = getTransitionProbabilities();
			double[][] siteStateProbs = getSiteStateProbabilities();

			for(int site = 0; site < siteStateProbs.length; site++) {
				for(int endState = 0; endState < numberOfStates_; endState++) {
					double probOfEndState = childSiteStateProbs_[0][site][endState];
					for(int i = 1; i < childSiteStateProbs_.length; i++) {
						probOfEndState *=childSiteStateProbs_[i][site][endState];
					}
					endStateProbs_[endState] = probOfEndState;
				}
				for(int startState = 0 ; startState < numberOfStates_ ; startState++) {
					double probOfStartState = 0;
					for(int endState = 0; endState < numberOfStates_ ; endState++) {
						probOfStartState +=probs[startState][endState]*endStateProbs_[endState];
					}
					siteStateProbs[site][startState] = probOfStartState;
				}
			}
			return siteStateProbs;
		}

		public double calculateFinal(double[] equilibriumProbs, int[] siteWeights) {
			populateChildSiteStateProbs();
			double logSum = 0;
			for(int site = 0 ; site < numberOfSites_ ; site++) {
				double total = 0;
				for(int state = 0 ; state < numberOfStates_ ; state++) {
					double stateProb = childSiteStateProbs_[0][site][state];
					for(int i = 1 ; i< childSiteStateProbs_.length ; i++) {
						stateProb *= childSiteStateProbs_[i][site][state];
					}

					total+=equilibriumProbs[state]*stateProb;
				}
				logSum+=Math.log(total)*siteWeights[site];
			}
			return logSum;
		}
		/**
		 * Not the most efficient way of doing things... shouldn't be called often!
		 */
		public LeafNode[] getLeafNodes() {
			Vector v = new Vector();
			for(int i = 0 ; i < children_.length ; i++) {
				LeafNode[] clns = children_[i].getLeafNodes();
				for(int j = 0 ; j < clns.length ; j++) {
					v.addElement(clns[j]);
				}
			}
			LeafNode[] lns = new LeafNode[v.size()];
			v.copyInto(lns);
			return lns;
		}
	}

	//=====================================================================
	//
	// ROOT NODE
	//
	//=====================================================================

	class RootNode extends InternalNode {
		double[] equilibriumProbabilities_;
		int[] siteWeightings_;

		public RootNode(Node peer) {
			this(peer,null);
		}

		public RootNode(Node peer, double[] equilibriumProbabilities) {
			super(peer);
			this.equilibriumProbabilities_ = equilibriumProbabilities;
		}
		public double computeLikelihood() {
			double lh = calculateFinal(equilibriumProbabilities_,siteWeightings_);
			modelChanged_ = false;
			return lh;
		}

		public void setModel(RateMatrix rm) {
			super.setModel(rm);
			this.equilibriumProbabilities_ = rm.getEquilibriumFrequencies();
		}

		public void setupSequences(SitePattern sp) {
			super.setupSequences(sp);
			this.siteWeightings_ = sp.weight;
		}

	}

}
