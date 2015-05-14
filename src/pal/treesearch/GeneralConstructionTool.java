// GeneralConstructionTool.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GeneralConstructionTool</p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.alignment.*;
import pal.datatype.*;
import pal.eval.*;
import pal.misc.*;
import pal.tree.*;

public class GeneralConstructionTool {

	private final String[] names_;
	private final int[][] sequences_;

	private final int numberOfStates_;
//	private final int numberOfCategories_;
	private final int numberOfSites_;

	private final DataType dataType_;

	private int nextConnectionIndex_ = 0;

	private final ArrayList allUNodes_ = new ArrayList();

	private final ConstraintModel constraints_;

	private ConditionalProbabilityStore tempConditionals_ = null;

	private final UnconstrainedLikelihoodModel.External freeCalcExternal_;

	/**
	 * The constructor
	 * @param alignment the base alignment
	 * @param numberOfStates the number of states
	 * @param numberOfCategories the number of model classes
	 * @param freeCalculatorGenerator The generator for free calculation (may be null if no free components)
	 * @param constrainedCalcGenerator The generator for constrained calculation (assuming a Molecular Clock, and may be null if no constrained components)
	 */
	public GeneralConstructionTool(ConstraintModel constraints, Alignment alignment) {
		this.constraints_ = constraints;
		this.dataType_ = alignment.getDataType();

		this.numberOfSites_ = alignment.getSiteCount();
		this.numberOfStates_ = dataType_.getNumStates();

		this.names_ = Identifier.getNames(alignment);
		this.sequences_ = pal.alignment.AlignmentUtils.getAlignedStates( alignment,numberOfStates_ );

		freeCalcExternal_ = constraints_.createNewFreeExternal();
	} //End of constructor

	/**
	 * Create an appropriate free node given a peer, and it's parent branch
	 * @param peer The normal PAL node peer
	 * @param parent The parent branch
	 * @return A FreeNode
	 */
	public FreeNode createFreeNode(Node peer, FreeBranch parent, GeneralConstraintGroupManager.Store store) {
		if(peer.isLeaf()) {
			String name = peer.getIdentifier().getName();
			int[] sequence = getSequence(name);
			final String[] leafLabelSet = new String[] { name };
			if(constraints_.getGlobalClockConstraintGrouping(leafLabelSet)!=null) {
			  //Could make this a warning...
				throw new IllegalArgumentException("Being forced to treat node '"+name+"' as unconstrained when constrained (probably a result of incorrectly structured topology");
			}
			return new FreeLeafNode(parent, name,this);
		} else {
			String[] leafLabelSet = getLeafLabelSet(peer);
		  ConstraintModel.GroupManager grouping = constraints_.getGlobalClockConstraintGrouping(leafLabelSet);
		  if(grouping==null) {
				return new FreeInternalNode(peer,parent,this,store);
			} else {
				return new PivotNode(peer,parent,this,store.getConstraintGroupManager(grouping),store);
			}
		}
	}

	public RootAccess createRootAccess(Node baseTree, GeneralConstraintGroupManager.Store store) {
		String[] allLeaves = getLeafLabelSet(baseTree);
		ConstraintModel.GroupManager grouping = constraints_.getGlobalClockConstraintGrouping(allLeaves);
		if(grouping==null) {
			return new FreeBranch(baseTree,this,store);
		} else {
			return new PivotNode(baseTree,this,store.getConstraintGroupManager(grouping),store);
		}
	}

	/**
	 * Create an appropriate constrained node given a peer, and it's parent node
	 * @param peer The normal PAL node peer
	 * @param parent The parent node
	 * @return A ConstrainedNode
	 */
	public ConstrainedNode createConstrainedNode(Node peer, ParentableConstrainedNode parent, GeneralConstraintGroupManager.Store store, GeneralConstraintGroupManager groupManager) {
		ConstraintModel.GroupManager parentGroup = groupManager.getRelatedGroup();
		if(peer.isLeaf()) {
			String name = peer.getIdentifier().getName();
			int[] sequence = getSequence(name);
		  String[] leafLabelSet = new String[] { name };
			ConstraintModel.GroupManager grouping = constraints_.getGlobalClockConstraintGrouping(leafLabelSet);
			if(grouping==null) {
			  //Could make this a warning...
				throw new IllegalArgumentException("Being forced to treat node '"+name+"' as constrained when unconstrained (probably a result of incorrectly structured topology");
			}
			return new ConstrainedLeafNode(parent,peer,parentGroup.getLeafBaseHeight(name), this, parentGroup);
		} else {
		  String[] leafLabelSet = getLeafLabelSet(peer);
			ConstraintModel.GroupManager grouping = constraints_.getGlobalClockConstraintGrouping(leafLabelSet);
			if(grouping==null) {
				throw new RuntimeException("Not implemented - cannont handle the constrained moving to unconstrained case yet!");
			} else {
				return new ConstrainedInternalNode(peer, parent,this,store,groupManager);
			}
		}
	}

// ================================================================================================================
	// - - - - -

	public PatternInfo constructFreshPatternInfo(boolean binaryPattern) {
		return new PatternInfo(numberOfSites_,binaryPattern);
	}

	public final ConditionalProbabilityStore obtainTempConditionalProbabilityStore() {
		if(tempConditionals_==null) {
			tempConditionals_ = newConditionalProbabilityStore(false);
		}
		return tempConditionals_;
	}

	public final ConditionalProbabilityStore newConditionalProbabilityStore(boolean isForLeaf) {
	  return constraints_.createAppropriateConditionalProbabilityStore( isForLeaf );
	}

	public final int allocateNextConnectionIndex() {	return nextConnectionIndex_++;		}
// - - - - -
	public UnconstrainedLikelihoodModel.Internal allocateNewFreeInternalCalculator() {
	  return constraints_.createNewFreeInternal();
	}
// - - - - -
	public UnconstrainedLikelihoodModel.External obtainFreeExternalCalculator() {
		if(freeCalcExternal_!=null) {	return freeCalcExternal_;	}
		throw new RuntimeException("No free calculator");
	}
// - - - - -
// - - - - -
	public UnconstrainedLikelihoodModel.Leaf createNewFreeLeafCalculator(int[] patternStateMatchup, int numberOfPatterns) {
		return constraints_.createNewFreeLeaf(patternStateMatchup,numberOfPatterns);
	}

	public int build(PatternInfo beingBuilt, PatternInfo left, PatternInfo right) {
		return beingBuilt.build(left,right,numberOfSites_);
	}

	public DataType getDataType() { return dataType_; }
	public final int getNumberOfSites() { return numberOfSites_; }
	public int getNumberOfStates() { return numberOfStates_; }

	/**
	 * Get the sequence data for a particular OTU
	 * @param name The name of the OTU
	 * @return the sequence data stored as integer values
	 * @throws IllegalArgumentException if no such OTU with given name
	 */
	public int[] getSequence(String name) {
		if(sequences_==null) {	return null;	}
		for(int i = 0 ; i < names_.length ; i++) {
			if(name.equals(names_[i])) {	return sequences_[i];	}
		}
		throw new IllegalArgumentException("Unknown sequence:"+name);
	}

	/**
	 * A horibly inefficient way of doing things. Finds the leaf index for all leaves from the tree
	 * defined by the PAL node. Returns -1 if more than one index.
	 * @param peer the root of the sub tree
	 * @return the common leaf index, of -1 if no common leaf index
	 * @note assumes bificating tree
	 */
	public String[] getLeafLabelSet(Node peer) {
		ArrayList al = new ArrayList();
		getLeafLabelSet(peer,al);
		String[] result = new String[al.size()];
		al.toArray(result);
		return result;
	}

	public void getLeafLabelSet(Node peer, ArrayList al) {
		if(peer.isLeaf()) {
			al.add(peer.getIdentifier().getName());
		} else {
			getLeafLabelSet(peer.getChild(0), al);
			getLeafLabelSet(peer.getChild(1), al);
		}
	}


}
