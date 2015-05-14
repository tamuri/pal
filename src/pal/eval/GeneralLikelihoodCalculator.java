// GeneralLikelihoodCalculator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of  Lesser GNU General Public License (LGPL)

package pal.eval;

import pal.tree.*;
import pal.alignment.*;
import pal.substmodel.*;
import pal.datatype.*;
import pal.misc.*;

import java.util.*;

/**
 * <B>Title:</B> General Likelihood Calculator <BR>
 * <B>Description:</B>  A General likelihood calculator<BR>
 * This calculates the likelihood of an topologically invariant tree
 * (on an unchanging alignment) quickly by remembering partial likelihoods of invariant subtrees, and
 * by essentially generating site patterns for each sub tree. Works with Rate Distributions and other more complex SubstitutionModels.
 * Will optimise (as in computational time) itself when using Nucleotide based data.
 *
 * @author Matthew Goode
 * @version $Id: GeneralLikelihoodCalculator.java,v 1.10 2004/01/12 09:30:27 matt Exp $
 */

public class GeneralLikelihoodCalculator implements PalObjectListener, LikelihoodCalculator {

	/** the root node of the cached tree. */
	NNode root_;

	/** true if the rate matrix has changed. */
	boolean modelChanged_ = false;

	/** the number of sites in the site pattern. */
	int numberOfSites_;
	int numberOfStates_;
	int numberOfTransitionCategories_;
	int[] patternWeightWorkingStore_;

	/** the Substitution Model currently being used. */
	SubstitutionModel model_;

	double[] equilibriumFrequencies_;

	/** the base alignment for which likelihood is calculated. */
	Alignment baseAlignment_;

	/**
	 * the difference threshold within which
	 * different branch lengths are deemed the same.
	 */
	private static double THRESHOLD = 1e-12;

	double[] gapPriors_;

	/**
	 * Constructor taking site pattern, tree and a rate matrix.
	 * @note giving a SitePattern is not going to make anything faster
	 */
	public GeneralLikelihoodCalculator(Alignment baseAlignment, Tree tree, RateMatrix model) {
		this(baseAlignment,tree, SubstitutionModel.Utils.createSubstitutionModel(model));
	}

	/**
	 * Constructor taking site pattern, tree rate matrix, and a rate distribution
	 * @note giving a SitePattern is not going to make anything faster
	 */
	public GeneralLikelihoodCalculator(Alignment baseAlignment, Tree tree, RateMatrix model, RateDistribution distribution) {
		this(baseAlignment,tree, SubstitutionModel.Utils.createSubstitutionModel(model,distribution));
	}

	/**
	 * Constructor taking site pattern, tree and a general substitution model.
	 * @note giving a SitePattern is not going to make anything faster
	 */
	public GeneralLikelihoodCalculator(Alignment baseAlignment, Tree tree, SubstitutionModel model) {
		this.baseAlignment_ = baseAlignment;
		this.numberOfTransitionCategories_ = model.getNumberOfTransitionCategories();
		numberOfSites_ = baseAlignment.getSiteCount();
		this.patternWeightWorkingStore_ = new int[numberOfSites_];
		numberOfStates_ = baseAlignment.getDataType().getNumStates();
		buildGapPriors();
		setup(tree, model);
	}

	public void parametersChanged(PalObjectEvent pe) {
		modelChanged_ = true;
	}

	public void structureChanged(PalObjectEvent pe) {
		modelChanged_ = true;
	}
	private void buildGapPriors() {
		if(gapPriors_==null||gapPriors_.length!=numberOfStates_) {
			gapPriors_ = new double[numberOfStates_];
			for(int i = 0 ; i < numberOfStates_ ; i++) {
				gapPriors_[i] = 1;
			}
		}
	}
	public final void setup(Tree t, SubstitutionModel model) {
		if ((model_ == null) || (model_ != model)) {

			if(model_!=null) {
				model_.removePalObjectListener(this);
			}
			this.model_ = model;
			model_.addPalObjectListener(this);
			modelChanged_ = true;
			this.equilibriumFrequencies_ = pal.misc.Utils.getCopy(model_.getEquilibriumFrequencies());
		} // else the same rate matrix, do nothing

		if (root_ == null) {
			root_ = create(t.getRoot());
		}
		// could be more efficient
		root_.setupSequences(patternWeightWorkingStore_, AlignmentUtils.getAlignedStates(baseAlignment_),baseAlignment_);
		//root_.printPatternInfo();
	}

	public void release() {
		try{
			model_.removePalObjectListener(this);
			model_ = null;
		} catch(NullPointerException e) {}
	}

	/**
	 * @return the likelihood of this tree under the given model and data.
	 */
	public double calculateLogLikelihood() {
		double lkl = root_.computeLikelihood();
		return lkl;
	}

	/**
	 * @return the LikelihoodSummary of this tree under the given model and data.
	 */
	public LikelihoodSummary calculateLogLikelihoodSummary() {
		return root_.computeLikelihoodSummary();
	}

	final NNode create(Node peer) {
		switch(peer.getChildCount()) {
			case 0 : {
				return new LeafNode(peer);
			}
			case 2 : {
				if(numberOfStates_==4) {
					return new BificatingFourStateInternalNode(peer);
				}
				return new BificatingInternalNode(peer);
			}
			default : {
				if(numberOfStates_==4) {
					return new FourStateInternalNode(peer);
				}
				return new InternalNode(peer);
			}
		}
	}
	/**
	 * Static implementation of calculateFinalSummary (for use by InternalNode, and BificatingInternal node)
	 */
	private static final LikelihoodSummary calculateFinalSummaryImpl(DataType dt, double[] equilibriumProbabilities, int numberOfPatterns, double[] categoryProbabilities, int[] patternWeights, double[][][][] childPatternProbs, int[] patterns, int[] sitePatternMatchup) {
		final int numberOfTransitionCategories = categoryProbabilities.length;
		double[][] individualLikelihoods = new double[numberOfPatterns][numberOfTransitionCategories];
		final int numberOfChildren = childPatternProbs.length;
		final int numberOfStates = dt.getNumStates();
		double logSum = 0;
		int patternReadPoint = 0;
		for(int pattern = 0 ; pattern < numberOfPatterns ; pattern++) {
			double probabilitySum = 0;
			double[] patternCategoryLikelihoods = individualLikelihoods[pattern];
			for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories; transitionCategory++) {
				double total = 0;
				for(int state = 0 ; state < numberOfStates ; state++) {
					double stateProb = childPatternProbs[0][transitionCategory][patterns[patternReadPoint]][state];
					for(int i = 1 ; i<numberOfChildren ; i++) {
						stateProb *=childPatternProbs[i][transitionCategory][patterns[patternReadPoint+i]][state];
					}
					total+=equilibriumProbabilities[state]*stateProb;
				}
				patternCategoryLikelihoods[transitionCategory] = total;
				probabilitySum+=total*categoryProbabilities[transitionCategory];
			}
			patternReadPoint+=numberOfChildren;
			logSum+=Math.log(probabilitySum)*patternWeights[pattern];
		}
		return new LikelihoodSummary(dt,logSum,categoryProbabilities,individualLikelihoods,sitePatternMatchup);
	}
	//=====================================================================
	//
	// Abstract NNODE
	//
	//=====================================================================

	abstract class NNode {

		private double lastLength_ = Double.NEGATIVE_INFINITY;

		private Node peer_;

		protected double[][][] transitionProbs_;
		protected double[][][] patternStateProbabilities_;
		protected int[] patternWeights_;
		protected int[] sitePatternMatchup_;

		public NNode(Node peer) {
			this.peer_ = peer;
			transitionProbs_ = new double[numberOfTransitionCategories_][numberOfStates_][numberOfStates_];
		}

		public final boolean isBranchLengthChanged() {
			return Math.abs(peer_.getBranchLength()-lastLength_) > THRESHOLD;
			//return peer_.getBranchLength()!=lastLength_;
		}
		protected final double getBranchLength() {
			return peer_.getBranchLength();
		}

		protected boolean updateTransitionProbabilities() {
			if(modelChanged_||isBranchLengthChanged()) {
				double distance = peer_.getBranchLength();
				model_.getTransitionProbabilities(distance,transitionProbs_);
				lastLength_ = distance;
				return true;
			}
			return false;
		}
		protected boolean updateTransitionProbabilitiesTranspose() {
			if(modelChanged_||isBranchLengthChanged()) {
				double distance = peer_.getBranchLength();
				model_.getTransitionProbabilitiesTranspose(distance,transitionProbs_);
				lastLength_ = distance;
				return true;

			}
			return false;
		}
		public final Identifier getIdentifier() {
			return peer_.getIdentifier();
		}
		private String toString(byte[] bs) {
			char[] cs = new char[bs.length];
			for(int i = 0 ; i < cs.length ; i++) {
				cs[i] = (char)('A'+bs[i]);
			}
			return new String(cs);
		}
		abstract 	public void printPatternInfo();
		abstract public boolean calculatePatternProbabilities();
		abstract public void setupSequences(int[] patternWeightStore, int[][] alignment, Alignment base);
		abstract public double computeLikelihood();
		abstract public LikelihoodSummary computeLikelihoodSummary();
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

		public void setupSequences(int[] patternWeightStore, int[][] states, Alignment base) {
			Identifier id = getIdentifier();
			if(id!=null) {
				int number = base.whichIdNumber(id.getName());
				if(number>=0) {
					setSequence(states[number],base.getDataType());
				}
			} else {
				throw new RuntimeException("Assertion error - leaf node has no matching sequence in base alignment");
			}
		}
		public double computeLikelihood() {		return 0;		}
		public LikelihoodSummary computeLikelihoodSummary() {		throw new RuntimeException("Cannot generate Likelihood Summary from leaf node");		}

		private final void setSequence(int[] sequence, final DataType dt) {
			final int numberOfStates = dt.getNumStates();
			sequence = normalise(sequence,dt);
			int[] stateCount = new int[numberOfStates+1];
			int uniqueCount = 0;
			sitePatternMatchup_ = new int[numberOfSites_];
			for(int i = 0 ; i < sequence.length ; i++) {
				int state = sequence[i];
				if(stateCount[state]==0) {	uniqueCount++;	}
				stateCount[state]++;
			}
			patternStateProbabilities_ = new double[numberOfTransitionCategories_][uniqueCount][];
			patternWeights_ = new int[uniqueCount];
			int index = 0;
			int[] statePatternMatchup = new int[numberOfStates+1];

			for(int i = 0 ; i < numberOfStates ; i++) {
				if(stateCount[i]>0) {
					for(int transitionCategory = 0 ;  transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
						patternStateProbabilities_[transitionCategory][index] = transitionProbs_[transitionCategory][i];
					}
					patternWeights_[index] = stateCount[i];
					statePatternMatchup[i] = index;
					index++;
				}
			}

			int gapCount = stateCount[numberOfStates];
			if(gapCount>0) {
				for(int transitionCategory = 0 ;  transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
					patternStateProbabilities_[transitionCategory][index] = gapPriors_;
				}
				patternWeights_[index] = gapCount;
				statePatternMatchup[numberOfStates] = index;
			}
			for(int i = 0 ; i < numberOfSites_ ; i++) {
				sitePatternMatchup_[i] = statePatternMatchup[sequence[i]];
			}
		}
		private final int[] normalise(final int[] sequence, final DataType dt) {
			int[] normal = new int[sequence.length];
			int numberOfStates = dt.getNumStates();
			for(int i = 0 ; i < normal.length ; i++) {
				if(dt.isUnknownState(sequence[i])) {
					normal[i] =numberOfStates;
				} else {
					normal[i] = sequence[i];
				}
			}
			return normal;
		}

		public void printPatternInfo() {	System.out.print(patternWeights_.length); 	}
		public boolean calculatePatternProbabilities() {
			return  updateTransitionProbabilitiesTranspose();
		}
	}
	private static final boolean matches(final int[] patternStore, final int[] pattern, int patternIndex, final int patternSize) {
		for(int i = 0 ; i < patternSize ; i++) {
			if(patternStore[patternIndex++]!=pattern[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Bificating Internal Node
	 */
	class BificatingInternalNode extends NNode{
		protected NNode left_, right_;
		protected double[][][] leftChildPatternProbs_;
		protected double[][][] rightChildPatternProbs_;
		protected final double[] endStateProbs_;
		protected int[] patterns_;
		protected int numberOfPatterns_;

		public BificatingInternalNode(Node peer) {
			super(peer);
			endStateProbs_ = new double[numberOfStates_];
			left_ = create(peer.getChild(0));
			right_ = create(peer.getChild(1));
		}

		public void setupSequences(int[] patternWeightStore, int[][] states, Alignment base) {
			left_.setupSequences(patternWeightStore, states,base);
			right_.setupSequences(patternWeightStore, states,base);
			leftChildPatternProbs_ = left_.patternStateProbabilities_;
			rightChildPatternProbs_ = right_.patternStateProbabilities_;

			int[] patternStore = new int[numberOfSites_*2];
			int numberOfPatterns = 0;
			int insertionPoint = 0;
			int[] currentPattern = new int[2];
			boolean patternFound;

			sitePatternMatchup_ = new int[numberOfSites_];
			for(int site = 0 ; site < numberOfSites_; site++) {
				currentPattern[0] = left_.sitePatternMatchup_[site];
				currentPattern[1] = right_.sitePatternMatchup_[site];
				int patternIndex = 0;
				int patternInsertionIndex = -1;
				for(int pattern = 0 ; pattern < numberOfPatterns ; pattern++) {
					if(matches(patternStore,currentPattern,patternIndex,2)) {
						patternInsertionIndex = pattern;
						patternWeightStore[pattern]++;
						break;
					}
					patternIndex+=2;
				}
				if(patternInsertionIndex<0) {
					patternStore[insertionPoint++] = currentPattern[0];
					patternStore[insertionPoint++] = currentPattern[1];
					patternInsertionIndex = numberOfPatterns;
					patternWeightStore[numberOfPatterns] = 1;
					numberOfPatterns++;
				}
				sitePatternMatchup_[site] = patternInsertionIndex;
			}
			this.numberOfPatterns_ = numberOfPatterns;
			patterns_ = new int[insertionPoint];
			System.arraycopy(patternStore,0,patterns_,0,insertionPoint);
			patternWeights_ = new int[numberOfPatterns];
			System.arraycopy(patternWeightStore,0,patternWeights_,0,numberOfPatterns);
			this.patternStateProbabilities_ = new double[numberOfTransitionCategories_][numberOfPatterns][numberOfStates_];
		}


		protected final boolean populateChildPatternProbs() {
			if(left_.calculatePatternProbabilities()) {
				right_.calculatePatternProbabilities();
				return true;
			}
			return right_.calculatePatternProbabilities();
		}
		public LikelihoodSummary computeLikelihoodSummary() {
			LikelihoodSummary ls = calculateFinalSummary(equilibriumFrequencies_);
			modelChanged_ = false;
			return ls;
		}
		public double computeLikelihood() {
			double lh = calculateFinal(equilibriumFrequencies_);
			modelChanged_ = false;
			return lh;
		}

		protected final int getNumberOfChildren() {
			return 2;
		}
		public void printPatternInfo() {
			System.out.print(numberOfPatterns_+":(");
			left_.printPatternInfo();
			System.out.print(", ");
			right_.printPatternInfo();
			System.out.print(")");
		}
		/**
		 * Populates child pattern probs and updates transition probabilities (if necessary respectively), and
		 * returns true if we need to so stuff else false (if nothing in this sub tree has changed)
		 */
		protected boolean setupProbabilityCalculate() {
			boolean a = populateChildPatternProbs();
			boolean b = updateTransitionProbabilities();
			return (a||b);
		}

		public boolean calculatePatternProbabilities() {
			if(!setupProbabilityCalculate()) { return false; }
			int patternReadPoint;
			for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
				final double[][] leftProbs = leftChildPatternProbs_[transitionCategory];
				final double[][] rightProbs = rightChildPatternProbs_[transitionCategory];
				final double[][] patternStateProb = patternStateProbabilities_[transitionCategory];
				final double[][] transProbs = transitionProbs_[transitionCategory];
				patternReadPoint = 0;
				for(int pattern = 0; pattern < numberOfPatterns_; pattern++) {
					for(int endState = 0; endState < numberOfStates_; endState++) {
						endStateProbs_[endState] =
								leftProbs[patterns_[patternReadPoint]][endState]*
								rightProbs[patterns_[patternReadPoint+1]][endState];
					}
					patternReadPoint+=2;
					final double[] probabilityStore = patternStateProb[pattern];
					for(int startState = 0 ; startState < numberOfStates_ ; startState++) {
						double probOfStartState = 0;
						for(int endState = 0; endState < numberOfStates_ ; endState++) {
							probOfStartState += transProbs[startState][endState]*endStateProbs_[endState];
						}
						probabilityStore[startState] = probOfStartState;
					}
				}
			}
			return true;
		}

		public double calculateFinal(double[] equilibriumProbs) {
			populateChildPatternProbs();
			double logSum = 0;
			int patternReadPoint = 0;
			double[] categoryProbabilities = model_.getTransitionCategoryProbabilities();
			for(int pattern = 0 ; pattern < numberOfPatterns_ ; pattern++) {
				double probabilitySum = 0;
				for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
					double total = 0;
					final double[][] leftProbs = leftChildPatternProbs_[transitionCategory];
					final double[][] rightProbs = rightChildPatternProbs_[transitionCategory];
					for(int state = 0 ; state < numberOfStates_ ; state++) {
						total+=
							equilibriumProbs[state]*
							leftProbs[patterns_[patternReadPoint]][state]*
							rightProbs[patterns_[patternReadPoint+1]][state];
					}
					probabilitySum+=total*categoryProbabilities[transitionCategory];
				}
				patternReadPoint+=2;
				logSum+=Math.log(probabilitySum)*patternWeights_[pattern];
			}
			return logSum;
		}
		/**
		 * Calculates the final Log Likelihood, and fills in align
		 */
		public LikelihoodSummary calculateFinalSummary(double[] equilibriumProbs) {
			populateChildPatternProbs();
			return calculateFinalSummaryImpl(
				model_.getDataType(),
				equilibriumProbs,
				numberOfPatterns_,
				model_.getTransitionCategoryProbabilities(),
				patternWeights_,
				new double[][][][] {leftChildPatternProbs_,rightChildPatternProbs_},
				patterns_,
				sitePatternMatchup_
				);
		}

	} //End of class BifactingInternalNode
	class BificatingFourStateInternalNode extends BificatingInternalNode{
		public BificatingFourStateInternalNode(Node peer) {
			super(peer);
		}

		public boolean calculatePatternProbabilities() {
			if(!setupProbabilityCalculate()) { return false; }
			int patternReadPoint;
			for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
				final double[][] leftProbs = leftChildPatternProbs_[transitionCategory];
				final double[][] rightProbs = rightChildPatternProbs_[transitionCategory];
				final double[][] patternStateProb = patternStateProbabilities_[transitionCategory];
				final double[][] transProbs = transitionProbs_[transitionCategory];
				patternReadPoint = 0;
				for(int pattern = 0; pattern < numberOfPatterns_; pattern++) {
					directProduct4(leftProbs[patterns_[patternReadPoint]],rightProbs[patterns_[patternReadPoint+1]],endStateProbs_);
					patternReadPoint+=2;
					final double[] probabilityStore = patternStateProb[pattern];
					probabilityStore[0] = dotProduct4(transProbs[0],endStateProbs_);
					probabilityStore[1] = dotProduct4(transProbs[1],endStateProbs_);
					probabilityStore[2] = dotProduct4(transProbs[2],endStateProbs_);
					probabilityStore[3] = dotProduct4(transProbs[3],endStateProbs_);
				}
			}
			return true;
		}

		public double calculateFinal(double[] equilibriumProbs) {
			populateChildPatternProbs();

			double logSum = 0;
			int patternReadPoint = 0;
			double[] categoryProbabilities = model_.getTransitionCategoryProbabilities();
			for(int pattern = 0 ; pattern < numberOfPatterns_ ; pattern++) {
				double probabilitySum = 0;
				final int patternLeft = patterns_[patternReadPoint];
				final int patternRight = patterns_[patternReadPoint+1];

				for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
					probabilitySum+=
						dotProduct4(
							equilibriumProbs,
							leftChildPatternProbs_[transitionCategory][patternLeft],
							rightChildPatternProbs_[transitionCategory][patternRight]
						)*categoryProbabilities[transitionCategory];
				}
				patternReadPoint+=2;
				logSum+=Math.log(probabilitySum)*patternWeights_[pattern];
			}
			return logSum;
		}
	} //End of class BifactingFourStateInternalNode
	/**
	 * The InternalNode class is a basic non optimised methods for a polyficating node
	 * where any number of states in the underlying datatype is catered for.
	 */
	class InternalNode extends NNode{
		protected NNode[] children_;
		protected double[][][][] childPatternProbs_;
		protected final double[] endStateProbs_;
		protected int[] patterns_;
		protected int numberOfPatterns_;

		public InternalNode(Node peer) {
			super(peer);
			endStateProbs_ = new double[numberOfStates_];
			this.children_ = new NNode[peer.getChildCount()];
			for(int i = 0 ; i < children_.length ; i++) {
				children_[i] = create(peer.getChild(i));
			}
			childPatternProbs_ = new double[children_.length][][][];
		}

		public void setupSequences(int[] patternWeightStore, int[][] states, Alignment base) {
			for(int i= 0 ; i < children_.length ; i++) {
				children_[i].setupSequences(patternWeightStore, states,base);
			}
			for(int i = 0 ; i < children_.length ; i++) {
				childPatternProbs_[i] = children_[i].patternStateProbabilities_;
			}
			final int numberOfChildren = children_.length;
			int[] patternStore = new int[numberOfSites_*numberOfChildren];
			int numberOfPatterns = 0;
			int insertionPoint = 0;
			int[] currentPattern = new int[numberOfChildren];
			boolean patternFound;

			sitePatternMatchup_ = new int[numberOfSites_];
			for(int site = 0 ; site < numberOfSites_; site++) {
				for(int child = 0 ; child<numberOfChildren ; child++) {
					currentPattern[child] = children_[child].sitePatternMatchup_[site];
				}
				int patternIndex = 0;
				int patternInsertionIndex = -1;
				for(int pattern = 0 ; pattern < numberOfPatterns ; pattern++) {
					if(matches(patternStore,currentPattern,patternIndex,numberOfChildren)) {
						patternInsertionIndex = pattern;
						patternWeightStore[pattern]++;
						break;
					}
					patternIndex+=numberOfChildren;
				}
				if(patternInsertionIndex<0) {
					for(int i = 0 ; i < numberOfChildren ; i++) {
						patternStore[insertionPoint++] = currentPattern[i];
					}
					patternInsertionIndex = numberOfPatterns;
					patternWeightStore[numberOfPatterns] = 1;
					numberOfPatterns++;
				}
				sitePatternMatchup_[site] = patternInsertionIndex;
			}
			this.numberOfPatterns_ = numberOfPatterns;
			patterns_ = new int[insertionPoint];
			System.arraycopy(patternStore,0,patterns_,0,insertionPoint);
			patternWeights_ = new int[numberOfPatterns];
			System.arraycopy(patternWeightStore,0,patternWeights_,0,numberOfPatterns);
			this.patternStateProbabilities_ = new double[numberOfTransitionCategories_][numberOfPatterns][numberOfStates_];
			//Add code to manage pattern stuff
		}

		protected final boolean populateChildPatternProbs() {
			boolean changed = false;
			for(int i = 0; i < children_.length ; i++) {
				if(children_[i].calculatePatternProbabilities()) {
					changed=true;
				}
			}
			return changed;
		}

		public LikelihoodSummary computeLikelihoodSummary() {
			LikelihoodSummary ls = calculateFinalSummary(equilibriumFrequencies_);
			modelChanged_ = false;
			return ls;
		}

		public double computeLikelihood() {
			double lh = calculateFinal(equilibriumFrequencies_);
			modelChanged_ = false;
			return lh;
		}

		protected final int getNumberOfChildren() {
			return children_.length;
		}
		public void printPatternInfo() {
			System.out.print(numberOfPatterns_+":(");
			for(int i = 0 ; i < children_.length ; i++) {
				children_[i].printPatternInfo();
				if(i!=children_.length-1) {
					System.out.print(", ");
				}
			}
			System.out.print(")");
		}
		/**
		 * Populates child pattern probs and updates transition probabilities (if necessary respectively), and
		 * returns true if we need to so stuff else false (if nothing in this sub tree has changed)
		 */
		protected boolean setupProbabilityCalculate() {
			boolean a = populateChildPatternProbs();
			boolean b = updateTransitionProbabilities();
			return (a||b);
		}

		public boolean calculatePatternProbabilities() {
			if(!setupProbabilityCalculate()) { return false; }
			int patternReadPoint;
			for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
				final double[][] transProbs = transitionProbs_[transitionCategory];
				final double[][] patternStateProbs = patternStateProbabilities_[transitionCategory];
				patternReadPoint = 0;
				for(int pattern = 0; pattern < numberOfPatterns_; pattern++) {
					for(int endState = 0; endState < numberOfStates_; endState++) {
						double probOfEndState =
							childPatternProbs_[0][transitionCategory][patterns_[patternReadPoint]][endState];
						for(int i = 1; i < childPatternProbs_.length; i++) {
							probOfEndState *=childPatternProbs_[i][transitionCategory][patterns_[patternReadPoint+i]][endState];
						}
						endStateProbs_[endState] = probOfEndState;
					}
					patternReadPoint+=childPatternProbs_.length;
					final double[] probabilityStore = patternStateProbs[pattern];
					for(int startState = 0 ; startState < numberOfStates_ ; startState++) {
						double probOfStartState = 0;
						for(int endState = 0; endState < numberOfStates_ ; endState++) {
							probOfStartState +=transProbs[startState][endState]*endStateProbs_[endState];
						}
						probabilityStore[startState] = probOfStartState;
					}
				}
			}
			return true;
		}

		public double calculateFinal(double[] equilibriumProbs) {
			populateChildPatternProbs();
			double logSum = 0;
			int patternReadPoint = 0;
			final double[] categoryProbabilities = model_.getTransitionCategoryProbabilities();
			final int numberOfChildren = childPatternProbs_.length;
			for(int pattern = 0 ; pattern < numberOfPatterns_ ; pattern++) {
				double probabilitySum = 0;
				for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
					double total = 0;
					for(int state = 0 ; state < numberOfStates_ ; state++) {
						double stateProb = childPatternProbs_[0][transitionCategory][patterns_[patternReadPoint]][state];
						for(int i = 1 ; i<numberOfChildren ; i++) {
							stateProb *=childPatternProbs_[i][transitionCategory][patterns_[patternReadPoint+i]][state];
						}
						total+=equilibriumProbs[state]*stateProb;
					}
					probabilitySum+=total*categoryProbabilities[transitionCategory];
				}
				patternReadPoint+=numberOfChildren;
				logSum+=Math.log(probabilitySum)*patternWeights_[pattern];
			}
			return logSum;
		}
		/**
		 * Calculates the final Log Likelihood, and fills in align
		 */
		public LikelihoodSummary calculateFinalSummary(double[] equilibriumProbs) {
			populateChildPatternProbs();
			return calculateFinalSummaryImpl(
				model_.getDataType(),
				equilibriumProbs,
				numberOfPatterns_,
				model_.getTransitionCategoryProbabilities(),
				patternWeights_,
				childPatternProbs_,
				patterns_,
				sitePatternMatchup_
				);
		}
	} //End of class InternalNode

	/**
	 *  FOUR STATE INTERNAL NODE
	 */
	class FourStateInternalNode extends InternalNode{

		public FourStateInternalNode(Node peer) {
			super(peer);
		}
		public boolean calculatePatternProbabilities() {

			if(!setupProbabilityCalculate()) { return false; }
			int patternReadPoint;
			for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
				final double[][] transProbs = transitionProbs_[transitionCategory];
				final double[][] patternStateProbs = patternStateProbabilities_[transitionCategory];
				patternReadPoint = 0;
				for(int pattern = 0; pattern < numberOfPatterns_; pattern++) {
					directProduct4(
						childPatternProbs_[0][transitionCategory][patterns_[patternReadPoint]],
						childPatternProbs_[1][transitionCategory][patterns_[patternReadPoint+1]],
						endStateProbs_);

					for(int i = 2; i < childPatternProbs_.length; i++) {
						directProduct4(
							endStateProbs_,
							childPatternProbs_[i][transitionCategory][patterns_[patternReadPoint+i]],
							endStateProbs_);
					}

					patternReadPoint+=childPatternProbs_.length;
					final double[] probabilityStore = patternStateProbs[pattern];
					probabilityStore[0] =dotProduct4(transProbs[0],endStateProbs_);
					probabilityStore[1] =dotProduct4(transProbs[1],endStateProbs_);
					probabilityStore[2] =dotProduct4(transProbs[2],endStateProbs_);
					probabilityStore[3] =dotProduct4(transProbs[3],endStateProbs_);
				}
			}
			return true;
		}

		public double calculateFinal(double[] equilibriumProbs) {
			populateChildPatternProbs();
			double logSum = 0;
			int patternReadPoint = 0;
			final double[] categoryProbabilities = model_.getTransitionCategoryProbabilities();
			final int numberOfChildren = childPatternProbs_.length;

			for(int pattern = 0 ; pattern < numberOfPatterns_ ; pattern++) {
				double probabilitySum = 0;
				for(int transitionCategory = 0 ; transitionCategory < numberOfTransitionCategories_ ; transitionCategory++) {
					directProduct4(
						childPatternProbs_[0][transitionCategory][patterns_[patternReadPoint]],
						childPatternProbs_[1][transitionCategory][patterns_[patternReadPoint+1]],
						endStateProbs_);
					for(int i = 2 ; i<numberOfChildren ; i++) {
						directProduct4(
							endStateProbs_,
							childPatternProbs_[i][transitionCategory][patterns_[patternReadPoint+i]],
							endStateProbs_);
					}
					directProduct4(
						endStateProbs_,
						equilibriumProbs,
						endStateProbs_);
					probabilitySum+=sum4(endStateProbs_)*categoryProbabilities[transitionCategory];
				}
				patternReadPoint+=numberOfChildren;
				logSum+=Math.log(probabilitySum)*patternWeights_[pattern];
			}
			return logSum;
		}
	} //End of class InternalNode
	protected static final double dotProduct4(final double[] v1, final double[] v2) {
		return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2]+v1[3]*v2[3];
	}
	protected static final double dotProduct4(final double[] v1, final double[] v2, final double[] v3) {
		return v1[0]*v2[0]*v3[0]+v1[1]*v2[1]*v3[1]+v1[2]*v2[2]*v3[2]+v1[3]*v2[3]*v3[3];
	}
	protected static final void directProduct4(final double[] v1, final double[] v2,final double[] store) {
		store[0] = v1[0]*v2[0];
		store[1] = v1[1]*v2[1];
		store[2] = v1[2]*v2[2];
		store[3] = v1[3]*v2[3];
	}
	protected static final double sum4(final double[] v) {
		return v[0]+v[1]+v[2]+v[3];
	}

}

