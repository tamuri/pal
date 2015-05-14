// SerialCoalescentGenerator.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.coalescent;

/**
 * Title:        SerialCoalescentGenerator
 * Description:  A utility class for generating large numbers of Serail coalescent derived trees (and simulated alignments)
 * @author Cow
 * @version 1.0
 * @note I'm not too sure where to put this class, or if it is of any use to anyone (outside of sUPGMA). It may jump packages.
 */
import pal.misc.*;
import pal.coalescent.*;
import pal.alignment.*;
import pal.tree.*;
import pal.util.*;

public class SerialCoalescentGenerator implements java.io.Serializable {
	private TimeOrderCharacterData tocd_;
	private DemographicModel demographicModel_;
	private int numberOfTreesToGenerate_;
	private SimulatedAlignment.Factory alignmentFactory_;
	private final TreeOperation treeFinisher_;
	/**
	 * Results will not contain alignments
	 */
	public SerialCoalescentGenerator(TimeOrderCharacterData tocd, DemographicModel demographicModel, int numberOfTreesToGenerate) {
		this(tocd,demographicModel,numberOfTreesToGenerate,TreeOperation.Utils.getNoOperation(), null);

	}
	/**
	 * Results will not contain alignments
	 */
	public SerialCoalescentGenerator(TimeOrderCharacterData tocd, DemographicModel demographicModel, int numberOfTreesToGenerate, TreeOperation treeFinisher) {
		this(tocd,demographicModel,numberOfTreesToGenerate,treeFinisher, null);

	}
	public SerialCoalescentGenerator(TimeOrderCharacterData tocd, DemographicModel demographicModel, TreeOperation treeFinisher, SimulatedAlignment.Factory alignmentFactory) {
		this(tocd,demographicModel,1,treeFinisher, alignmentFactory);
	}
	/**
	 * @param alignmentFactory Can be null if no alignments to be generated (otherwise results will contain alignments as well as trees)
	 */
	public SerialCoalescentGenerator(TimeOrderCharacterData tocd, DemographicModel demographicModel, int numberOfTreesToGenerate , TreeOperation treeFinisher, SimulatedAlignment.Factory alignmentFactory) {
		this.tocd_ = tocd;
		this.treeFinisher_ = treeFinisher;
		this.demographicModel_ = demographicModel;
		this.numberOfTreesToGenerate_ = numberOfTreesToGenerate;
		this.alignmentFactory_ = alignmentFactory;
	}
	private final Tree generateNewTree() {
		SerialCoalescentSimulator scs = new SerialCoalescentSimulator();
		scs.simulateIntervals(tocd_, demographicModel_, true);
		return treeFinisher_.operateOn(scs.getTree());
	}
	public final Tree generateTree() {
		return generateNewTree();
	}
	/**
	 * If callback request stop then returns trees creating thus far
	 */
	public final Tree[] generateTrees(AlgorithmCallback callback) {
		Tree[] trees = new Tree[numberOfTreesToGenerate_];
		callback.updateStatus("Simulating trees");
		for(int i = 0 ; i < numberOfTreesToGenerate_ ; i++) {
			if(callback.isPleaseStop()) {
				Tree[] toReturn = new Tree[i];
				System.arraycopy(trees,0,toReturn,0,i);
				return toReturn;
			}
			trees[i] = generateNewTree();
			callback.updateProgress(i/((double)numberOfTreesToGenerate_));
		}
		callback.clearProgress();

		return trees;
	}
	/**
	 * If callback request stop then returns results creating thus far
	 */
	private final Results generateTreeAndAlignmentResults(AlgorithmCallback callback) {
		Tree[] trees = new Tree[numberOfTreesToGenerate_];
		Alignment[] alignments = new Alignment[numberOfTreesToGenerate_];
		callback.clearProgress();
		double total = trees.length*2;
		for(int i = 0 ; i < trees.length ; i++) {
			if(callback.isPleaseStop()) {
				Tree[] ts = new Tree[i];
				Alignment[] as = new Alignment[i];
				System.arraycopy(trees,0,ts,0,i);
				System.arraycopy(alignments,0,as,0,i);
				return new Results(ts,as);
			}
			trees[i] = generateNewTree();
			callback.updateProgress((2*i)/total);
			alignments[i] = alignmentFactory_.generateAlignment(trees[i]);
			callback.updateProgress((2*i+1)/total);
		}
		callback.clearProgress();
		return new Results(trees,alignments);
	}
	/**
	 * If callback request stop then returns results creating thus far
	 */
	private final Results generateTreeOnlyResults(AlgorithmCallback callback) {
		Tree[] trees = new Tree[numberOfTreesToGenerate_];
		callback.clearProgress();
		double total = trees.length;
		for(int i = 0 ; i < trees.length ; i++) {
			if(callback.isPleaseStop()) {
				Tree[] ts = new Tree[i];
				System.arraycopy(trees,0,ts,0,i);
				return new Results(ts);
			}
			trees[i] = generateNewTree();
			callback.updateProgress(i/total);
		}
		callback.clearProgress();
		return new Results(trees);
	}
	public final Results generateResults(AlgorithmCallback callback) {
		if(alignmentFactory_!=null) {
			return generateTreeAndAlignmentResults(callback);
		}
		return generateTreeOnlyResults(callback);
	}
// ============================================================================
// ==== Results class
	/**
	 * A simple wrapper class for containing the results which may either be
	 * a number of trees, or a number of trees and alignments (in parallel arrays)
	 */
	public final static class Results {
		private Tree[] trees_;
		private Alignment[] alignments_;
		public Results(Tree[] trees) {
			this(trees,null);
		}
		public Results(Tree[] trees, Alignment[] alignments) {
			this.trees_ = trees;
			this.alignments_ = alignments;
		}
		public final Tree[] getTrees() { return trees_; }
		public final Alignment[] getAlignments() { return alignments_; }
		public final boolean hasAlignments() { return alignments_!=null; }
		/**
		 * @return the number of trees, or the number of tree/alignment pairs
		 */
		public final int getNumberOfPopulations() { return trees_.length; }
	}
}