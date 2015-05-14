// SimulatedAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.tree;

import pal.datatype.*;
import pal.substmodel.*;
import pal.alignment.*;
import pal.math.*;
import pal.misc.*;
import pal.util.AlgorithmCallback;


/**
 * generates an artificial data set
 *
 * @version $Id: SimulatedAlignment.java,v 1.19 2003/03/23 00:21:33 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class SimulatedAlignment extends AbstractAlignment
{
	//
	// Public stuff
	//


	//
	// Private stuff
	//

	private Tree tree;
	private SubstitutionModel model;
	private double[] cumFreqs;
	private int[] rateAtSite;
	private double[] cumRateProbs;
	private int numStates;
	private byte[][] stateData;
	private MersenneTwisterFast rng;

		//
	// Serialization
	//


	//private static final long serialVersionUID = -5197800047652332969L;

	//serialver -classpath ./classes pal.tree.SimulatedAlignment
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(tree);
		out.writeObject(model);
		out.writeObject(cumFreqs);
		out.writeObject(rateAtSite);
		out.writeObject(cumRateProbs);
		out.writeObject(stateData);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				tree = (Tree)in.readObject();
				model = (SubstitutionModel)in.readObject();
				cumFreqs = (double[])in.readObject();
				rateAtSite = (int[])in.readObject();
				cumRateProbs = (double[])in.readObject();
				stateData = (byte[][])in.readObject();
				numStates = getDataType().getNumStates();
				rng = new MersenneTwisterFast();
				break;
			}
		}
	}

	/**
	 * Inititalisation
	 *
	 * @param sites number of sites
	 * @param t     tree relating the sequences
	 * @param m     model of evolution
	 */
	public SimulatedAlignment(int sites, Tree t, SubstitutionModel m) {
		rng = new MersenneTwisterFast();
		setDataType(m.getDataType());
		numStates = getDataType().getNumStates();
		model = m;

		tree = t;
		tree.createNodeList();

		numSeqs = tree.getExternalNodeCount();
		numSites = sites;
		idGroup = new SimpleIdGroup(numSeqs);

		for (int i = 0; i < numSeqs; i++)
		{
			idGroup.setIdentifier(i, tree.getExternalNode(i).getIdentifier());
		}

		stateData = new byte[numSeqs][numSites];

		for (int i = 0; i < tree.getExternalNodeCount(); i++)
		{
			tree.getExternalNode(i).setSequence(stateData[i]);
		}
		for (int i = 0; i < tree.getInternalNodeCount()-1; i++)
		{
			tree.getInternalNode(i).setSequence(new byte[numSites]);
		}


		rateAtSite = new int[numSites];
		cumFreqs = new double[numStates];
		cumRateProbs = new double[m.getNumberOfTransitionCategories()];
	}


	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return getChar(stateData[seq][site]);
	}


	/** generate new artificial data set (random root sequence) */
	public void simulate() {
		simulate(makeRandomRootSequence());
	}

	/** generate new artificial data set (random root sequence) */
	public void simulate(String givenRootSequence)	{
		simulate(DataType.Utils.getByteStates(givenRootSequence, model.getDataType()));
	}
	/** generate new artificial data set (specified root sequence) */
	public void simulate(byte[] rootSeq)
	{
		double[][][] transitionStore = SubstitutionModel.Utils.generateTransitionProbabilityTables(model);
		// Check root sequence
		for (int i = 0; i < numSites; i++)
		{
			if (rootSeq[i] >= numStates || rootSeq[i] < 0)
			{
				throw new IllegalArgumentException("Root sequence contains illegal state (?,-, etc.)");
			}
		}

		tree.getInternalNode(tree.getInternalNodeCount()-1).setSequence(rootSeq);

		// Assign new rate categories
		assignRates();

		// Visit all nodes except root
		Node node = NodeUtils.preorderSuccessor(tree.getRoot());
		do
		{
			determineMutatedSequence(node,transitionStore);
			node = NodeUtils.preorderSuccessor(node);
		}
		while (node != tree.getRoot());
	}

	private void determineMutatedSequence(Node node, double[][][] transitionStore)
	{
		if (node.isRoot()) throw new IllegalArgumentException("Root node not allowed");

		model.getTransitionProbabilities(node.getBranchLength(),transitionStore);

		byte[] oldS = node.getParent().getSequence();
		byte[] newS = node.getSequence();

		for (int i = 0; i < numSites; i++)
		{
			double[] freqs = transitionStore[rateAtSite[i]][oldS[i]];
			cumFreqs[0] = freqs[0];
			for (int j = 1; j < numStates; j++)
			{
				cumFreqs[j] = cumFreqs[j-1] + freqs[j];
			}

			newS[i] = (byte) randomChoice(cumFreqs);
		}
	}

	private byte[] makeRandomRootSequence()	{
		double[] frequencies = model.getEquilibriumFrequencies();
		cumFreqs[0] = frequencies[0];
		for (int i = 1; i < numStates; i++)	{
			cumFreqs[i] = cumFreqs[i-1] + frequencies[i];
		}
		byte[] rootSequence = new byte[numSites];
		for (int i = 0; i < numSites; i++)
		{
			rootSequence[i] = (byte) randomChoice(cumFreqs);
		}
		return rootSequence;
	}

	private void assignRates()	{
		double[] categoryProbabilities = model.getTransitionCategoryProbabilities();

		cumRateProbs[0] = categoryProbabilities[0];
		for (int i = 1; i < categoryProbabilities.length ; i++)	{
			cumRateProbs[i] = cumRateProbs[i-1] + categoryProbabilities[i];
		}

		for (int i = 0; i < numSites; i++) {
			rateAtSite[i] = randomChoice(cumRateProbs);
		}


	}

	// Chooses one category if a cumulative probability distribution is given
	private int randomChoice(double[] cf)
	{

		double rnd = rng.nextDouble();

		int s;
		if (rnd <= cf[0])
		{
			s = 0;
		}
		else
		{
			for (s = 1; s < cf.length; s++)
			{
				if (rnd <= cf[s] && rnd > cf[s-1])
				{
					break;
				}
			}
		}

		return s;
	}
// ============================================================================
// SimulatedAlignment.Factory
	/**
	 * A utility class that can be used to generate Simulated alignments based on
	 * a tree with predefined sequence length and substitution model
	 */
	public static final class Factory {
		private int sequenceLength_;
		private SubstitutionModel model_;
		public Factory(int sequenceLength, SubstitutionModel model) {
			if(sequenceLength<1) {
				throw new IllegalArgumentException("Invalid sequence length:"+sequenceLength);
			}
			this.sequenceLength_ = sequenceLength;
			this.model_ = model;
		}
		/**
		 * Generate a simulated alignment based on input tree
		 * @param tree The tree, with branchlengths set appropriately.
		 * @note Units should be expected substitutions
		 * @throws IllegalArgumentException if trees units are not EXPECTED SUBSTITUTIONS, or UNKNOWN
		 */
		public final SimulatedAlignment generateAlignment(final Tree tree) {
			if(
					(tree.getUnits()!=Units.EXPECTED_SUBSTITUTIONS)&&
					(tree.getUnits()!=Units.UNKNOWN)
				) {
				throw new IllegalArgumentException("Tree units must be Expected Substitutions (or reluctantly Unknown)");
			}
			//System.out.println("Simulating:"+model_);
			SimulatedAlignment sa = new SimulatedAlignment(sequenceLength_,tree,model_);
			sa.simulate();
			return sa;
		}
		/**
		 * Generate an array of simulated alignments based on an array of input trees
		 * @param trees The tree, with branchlengths set appropriately.
		 * @param callback An AlgorithmCallback for monitoring progress and premature stopping
		 * @note Units should be expected substitutions
		 * @note if AlgorithmCallback indicates premature stopping will return an array of
		 *  alignments created so far.
		 * @throws IllegalArgumentException if trees units are not EXPECTED SUBSTITUTIONS, or UNKNOWN
		 */
		public final SimulatedAlignment[] generateAlignments(final Tree[] trees, final AlgorithmCallback callback) {
			SimulatedAlignment[] as = new SimulatedAlignment[trees.length];
			for(int i = 0 ; i < trees.length ; i++) {
				if(callback.isPleaseStop()) {
					SimulatedAlignment[] partial = new SimulatedAlignment[i];
					System.arraycopy(as,0,partial,0,i);
					return partial;
				}
				as[i] = generateAlignment(trees[i]);
				as[i].simulate();
				callback.updateProgress(i/(double)trees.length);
			}
			callback.clearProgress();
			return as;
		}
	}
}
