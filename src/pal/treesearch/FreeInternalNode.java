// FreeInternalNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: FreeInternalNode </p>
 * <p>Description: A free internal node has three FreeBranches connected to it. </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.tree.*;

public class FreeInternalNode implements FreeNode {
	private final FreeBranch[] connections_ = new FreeBranch[3];
	private final FreeBranch[] markConnections_ = new FreeBranch[3];

	private final PatternInfo[] patternInfos_;
	private final boolean[] patternsValid_;

	private static final int[] LEFT_LOOKUP = {	1 , 0, 0	};
	private static final int[] RIGHT_LOOKUP = {	2 , 2, 1	};

//	private final int index_;
	private final UnconstrainedLikelihoodModel.Internal calculator_;
	private boolean topologyChangedSinceLastFlat_ = true;
	private boolean topologyChangedSincleLastExtended_ = true;


	public FreeInternalNode(Node i, FreeBranch parentFreeBranch, GeneralConstructionTool tool, GeneralConstraintGroupManager.Store store) {
		this.connections_[0] = parentFreeBranch;
		this.connections_[1] = new FreeBranch(i.getChild(0),this, tool,store);
		this.connections_[2] = new FreeBranch(i.getChild(1),this, tool,store);
		this.patternsValid_ = new boolean[] { false, false, false};

		this.calculator_ = tool.allocateNewFreeInternalCalculator();
		final int numberOfSites = tool.getNumberOfSites();
		this.patternInfos_ = new PatternInfo[] {
											 new PatternInfo( numberOfSites, true ),
											 new PatternInfo( numberOfSites, true ),
											 new PatternInfo( numberOfSites, true )
			};

	}



	public void mark() {
		markConnections_[0] = connections_[0];
		markConnections_[1] = connections_[1];
		markConnections_[2] = connections_[2];
	}
	public void undoToMark() {
		connections_[0] = markConnections_[0];
		connections_[1] = markConnections_[1];
		connections_[2] = markConnections_[2];
		topologyChanged();
	}
	private final void topologyChanged() {
		this.topologyChangedSinceLastFlat_ = true;
		this.topologyChangedSincleLastExtended_ = true;
	}
	public boolean hasDirectConnection(FreeBranch c) {
		for(int i = 0 ; i < connections_.length ; i++) {
				if(connections_[i]==c) {	return true;	}
			}
			return false;

	}
	public boolean hasConnection(FreeBranch c, FreeBranch caller) {
		for(int i = 0 ; i < connections_.length ; i++) {
			if((connections_[i]==c)||(connections_[i]!=caller&&connections_[i].hasConnection(c,this))) {
				return true;
			}
		}
		return false;
	}

//	public final int getIndex() { return index_; }
	public void testLikelihood(FreeBranch caller, GeneralConstructionTool tool) {
		for(int i = 0 ; i < connections_.length ; i++) {
			if(connections_[i]!=caller) {
				connections_[i].testLikelihood(this,tool);
			}
		}
	}
	public void setConnectingBranches(FreeBranch[] store, int number){
		if(number!=3) {
			throw new IllegalArgumentException("Must be three connections not:"+number);
		}
		System.arraycopy(store,0,connections_,0,3);
		topologyChanged();
	}
	//Interchange related
	public FreeBranch getLeftBranch(FreeBranch caller) {
		return connections_[LEFT_LOOKUP[getCallerIndex(caller)]];
	}

	public FreeBranch getRightBranch(FreeBranch caller) {
		return connections_[RIGHT_LOOKUP[getCallerIndex(caller)]];
	}
	public FreeBranch extract(FreeBranch caller) {
		int callerIndex = getCallerIndex(caller);
		FreeBranch left = connections_[LEFT_LOOKUP[callerIndex]];
		FreeBranch right = connections_[RIGHT_LOOKUP[callerIndex]];
		FreeNode rightNode = right.getOther(this);
		left.swapNode(this,rightNode);
		rightNode.swapConnection(right,left);
		topologyChanged();
		return right;
	}
	public void swapConnection(FreeBranch original, FreeNode nodeToReplace, FreeBranch newConnection) {
		int index = getCallerIndex(original);
		connections_[index] = newConnection;
		newConnection.swapNode(nodeToReplace,this);
		original.swapNode(this,nodeToReplace);

		nodeToReplace.swapConnection(newConnection,original);
		topologyChanged();
	}
	public void swapConnection(FreeBranch original,FreeBranch newConnection) {
		int index = getCallerIndex(original);
		connections_[index] = newConnection;
		topologyChanged();
	}
	public PatternInfo getPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {
	  return getPatternInfo( tool, getCallerIndex(caller));
	}

	private final PatternInfo getPatternInfo(final GeneralConstructionTool tool, final int callerIndex) {

		if(!patternsValid_[callerIndex]) {
			//Need to rebuild
			final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
			final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		  final FreeNode left = leftConnection.getOther(this);
		  final FreeNode right = rightConnection.getOther(this);
		  final PatternInfo leftPattern= left.getPatternInfo(tool,leftConnection);
		  final PatternInfo rightPattern= right.getPatternInfo(tool,rightConnection);
			tool.build(patternInfos_[callerIndex],leftPattern, rightPattern);
		  patternsValid_[callerIndex] = true;
		}
		return patternInfos_[callerIndex];
	}

	public Node buildPALNodeES(double branchLength, FreeBranch caller) {
		final int callerIndex = getCallerIndex(caller);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		Node[] children = new Node[] {
			leftConnection.buildPALNodeES(this), rightConnection.buildPALNodeES(this)
		};
		return NodeFactory.createNodeBranchLength(branchLength,children);
	}
	public Node buildPALNodeBase(double branchLength, FreeBranch caller) {
		final int callerIndex = getCallerIndex(caller);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		Node[] children = new Node[] {
			leftConnection.buildPALNodeBase(this), rightConnection.buildPALNodeBase(this)
		};
		return NodeFactory.createNodeBranchLength(branchLength,children);
	}

	public String toString(FreeBranch caller) {
		StringBuffer sb = new StringBuffer();
		boolean printed = false;
		for(int i = 0 ; i < connections_.length ; i++) {
			if(connections_[i]!=caller) {
				if(printed) {
					sb.append(", ");
				}
				printed = true;
				sb.append(connections_[i].toString(this));
			}
		}
		return sb.toString();
	}
// ==============
	public void getAllComponents(ArrayList store, Class componentType) {
		getAllComponents(store, componentType, null);
	}
	public void getAllComponents(ArrayList store, Class componentType, FreeBranch caller) {
		if(componentType.isAssignableFrom(getClass())) { store.add(this); }
		for(int i = 0 ; i < connections_.length ; i++) {
			if(connections_[i]!=caller) {
				connections_[i].getAllComponents(store,componentType, this);
			}
		}
	}


// ==============

	private final int getCallerIndex(FreeBranch caller) {
		if(caller==null) {
			throw new IllegalArgumentException("getCallerIndex() called on null object");
		}
		if(caller==connections_[0]) { return 0; }
		if(caller==connections_[1]) { return 1; }
		if(caller==connections_[2]) { return 2; }
		throw new IllegalArgumentException("Unknown caller");
	}



	public ConditionalProbabilityStore getLeftExtendedConditionalProbabilities( FreeBranch callingConnection, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool) {
		final int callerIndex = getCallerIndex(callingConnection);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		return leftConnection.getExtendedConditionalProbabilities(this,external, resultStore,tool);
	}
	public ConditionalProbabilityStore getRightExtendedConditionalProbabilities( FreeBranch callingConnection, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool) {
		final int callerIndex = getCallerIndex(callingConnection);
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		return rightConnection.getExtendedConditionalProbabilities( this, external, resultStore,tool);
	}
	public PatternInfo getLeftPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {
		final int callerIndex = getCallerIndex(caller);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeNode other = leftConnection.getOther(this);
		return other.getPatternInfo(tool, leftConnection);
	}

	public PatternInfo getRightPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {
		final int callerIndex = getCallerIndex(caller);
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		final FreeNode other = rightConnection.getOther(this);
		return other.getPatternInfo(tool, rightConnection);
	}


	public ConditionalProbabilityStore getFlatConditionalProbabilities(  final FreeBranch callerConnection, UnconstrainedLikelihoodModel.External externalCalculator, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool) {
		final int callerIndex = getCallerIndex(callerConnection);
		final PatternInfo pi = getPatternInfo(tool, callerIndex);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];

		externalCalculator.calculateFlat(
				 pi,
				 leftConnection.getExtendedConditionalProbabilities(this,tool),
				 rightConnection.getExtendedConditionalProbabilities(this,tool),
				 resultStore
				 );
		return resultStore;

	}
	public ConditionalProbabilityStore getFlatConditionalProbabilities(  final FreeBranch callerConnection, GeneralConstructionTool tool) {
		final int callerIndex = getCallerIndex(callerConnection);
		final PatternInfo pi = getPatternInfo(tool, callerIndex);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		final boolean childrenChanged = topologyChangedSinceLastFlat_;
		topologyChangedSinceLastFlat_ = false;
		return calculator_.calculateFlat( pi,
				 leftConnection.getExtendedConditionalProbabilities(this,tool),
				 rightConnection.getExtendedConditionalProbabilities(this,tool)
				 );
	}
	public ConditionalProbabilityStore getExtendedConditionalProbabilities( final double distance,  final FreeBranch callerConnection, UnconstrainedLikelihoodModel.External externalCalculator, ConditionalProbabilityStore resultStore, final GeneralConstructionTool tool) {
		final int callerIndex = getCallerIndex(callerConnection);
		final PatternInfo pi = getPatternInfo(tool, callerIndex);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];


		externalCalculator.calculateExtended(
			distance, pi,
			leftConnection.getExtendedConditionalProbabilities(this,tool),
			rightConnection.getExtendedConditionalProbabilities(this,tool),
			resultStore
			);
		return resultStore;
	}
	public ConditionalProbabilityStore getExtendedConditionalProbabilities( final double distance, final FreeBranch callerConnection, GeneralConstructionTool tool) {
		final int callerIndex = getCallerIndex(callerConnection);
		final PatternInfo pi = getPatternInfo(tool, callerIndex);
		final FreeBranch leftConnection = connections_[LEFT_LOOKUP[callerIndex]];
		final FreeBranch rightConnection = connections_[RIGHT_LOOKUP[callerIndex]];
		final boolean childrenChanged = topologyChangedSincleLastExtended_;
		topologyChangedSincleLastExtended_ = false;
		return calculator_.calculateExtended(
			distance, pi,
			leftConnection.getExtendedConditionalProbabilities(this,tool),
			rightConnection.getExtendedConditionalProbabilities( this,tool)
		);
	}
}
