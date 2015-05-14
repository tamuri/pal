// FreeLeafNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: FreeLeafNode </p>
 * <p>Description: Free Leaf Node is a leaf node on the tree attached to a free branch</p>
 * @author not attributable
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.misc.*;
import pal.tree.*;

public class FreeLeafNode extends AbstractLeafNode implements FreeNode {

	private FreeBranch parentFreeBranch_ = null;
	private FreeBranch markedParentFreeBranch_ = null;

	private final UnconstrainedLikelihoodModel.Leaf leafCalculator_;

	public FreeLeafNode(FreeBranch parentBranch, String id, GeneralConstructionTool tool) {
		super(id,tool);
		this.parentFreeBranch_ = parentBranch;
		this.leafCalculator_ = createNewFreeLeafCalculator( tool );
	}

	public boolean hasDirectConnection(FreeBranch fb) {	return parentFreeBranch_==fb;	}
	public void mark() {		this.markedParentFreeBranch_ = parentFreeBranch_;		}
	public void undoToMark() {	this.parentFreeBranch_ = markedParentFreeBranch_;		}

	public boolean hasConnection(FreeBranch fb, FreeBranch caller) {
		if(caller!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown caller!");	}
		return parentFreeBranch_==fb;
	}
	public FreeBranch extract(FreeBranch caller) {
		if(caller!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown caller!");	}
		return null;
	}

	public ConditionalProbabilityStore getLeftExtendedConditionalProbabilities( FreeBranch caller, UnconstrainedLikelihoodModel.External externalCalculator, ConditionalProbabilityStore resultStore){
		throw new RuntimeException("Assertion error : Not applicable for leaf nodes!");
	}
	public ConditionalProbabilityStore getRightExtendedConditionalProbabilities( FreeBranch caller, UnconstrainedLikelihoodModel.External externalCalculator, ConditionalProbabilityStore resultStore){
		throw new RuntimeException("Assertion error : Not applicable for leaf nodes!");
	}
	public PatternInfo getLeftPatternInfo(GeneralConstructionTool tool, FreeBranch caller){		return null;	}
	public PatternInfo getRightPatternInfo(GeneralConstructionTool tool, FreeBranch caller) {	return null;	}

	public void setConnectingBranches(FreeBranch[] store, int number){
		if(number!=1) {		throw new IllegalArgumentException("Must be one connection not:"+number);		}
		this.parentFreeBranch_ = store[0];
	}

	public void testLikelihood(FreeBranch caller, GeneralConstructionTool tool) {
		if(caller!=parentFreeBranch_) {	throw new IllegalArgumentException("Unknown caller!");		}
	}
	public void swapConnection(FreeBranch original,  FreeBranch newConnection) {
		if(original!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown original");		}
		this.parentFreeBranch_ = newConnection;
	}
	public void swapConnection(FreeBranch original, FreeNode nodeToReplace, FreeBranch newConnection) {
		swapConnection(original,newConnection);
		newConnection.swapNode(nodeToReplace,this);
		original.swapNode(this,nodeToReplace);
	}

	/**
	 * @return null (as not possible)
	 */
	public FreeBranch getLeftBranch(FreeBranch caller) {	return null;	}
	/**
	 * @return null (as not possible)
	 */
	public FreeBranch getRightBranch(FreeBranch caller) {	return null;	}

	public void getAllConnections(ArrayList store, FreeBranch caller) {
		if(caller!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown caller!");		}
	}
	public PatternInfo getPatternInfo(GeneralConstructionTool tool, FreeBranch caller){
		if(caller!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown caller!");		}
		return getPatternInfo();
	}
	public void rebuildConnectionPatterns(GeneralConstructionTool tool, FreeBranch caller) {
		if(caller!=parentFreeBranch_){	throw new IllegalArgumentException("Unknown caller!");			}
	}


	/**
	 * This should only be called by another leaf node on the other end of the connection.
	 * In this case we don't have to do much (tree is two node tree)
	 */
	public int redirectRebuildPattern(GeneralConstructionTool tool) {		return getNumberOfPatterns();		}

	public final ConditionalProbabilityStore getFlatConditionalProbabilities(final FreeBranch callingBranch, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool) {
		if(callingBranch!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown calling connection");			}
		return leafCalculator_.getFlatConditionalProbabilities();
	}
	public final ConditionalProbabilityStore getFlatConditionalProbabilities(final FreeBranch caller, GeneralConstructionTool tool) {
		if(caller!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown calling connection");			}
		return leafCalculator_.getFlatConditionalProbabilities();
	}

	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance,  FreeBranch callingBranch, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool) {
		if(callingBranch!=parentFreeBranch_) {	throw new IllegalArgumentException("Unknown calling connection");		}
		return leafCalculator_.getExtendedConditionalProbabilities(distance);
	}
	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance,FreeBranch callingBranch, GeneralConstructionTool tool) {
		if(callingBranch!=parentFreeBranch_) {	throw new IllegalArgumentException("Unknown calling connection");			}
		return leafCalculator_.getExtendedConditionalProbabilities(distance);
	}
	public final Node buildPALNode(double branchLength, FreeBranch caller) {
		if(caller!=parentFreeBranch_) {		throw new IllegalArgumentException("Unknown calling connection"); 	}
		return NodeFactory.createNodeBranchLength(branchLength, new Identifier(getLabel()));
	}
	public final Node buildPALNodeES(double branchLength, FreeBranch caller) {
		return buildPALNode(branchLength,caller);
	}
	public final Node buildPALNodeBase(double branchLength, FreeBranch caller) {
		return buildPALNode(branchLength,caller);
	}

	public String toString(FreeBranch caller) {		return getLabel(); 	}

	public void getAllComponents(ArrayList store, Class componentType, FreeBranch caller) {
		if(componentType.isAssignableFrom(getClass())) { store.add(this); }
		if(caller!=parentFreeBranch_) { throw new RuntimeException("Assertion error : unexpected caller"); }
	}

	public final void getAllComponents(ArrayList store, Class componentType) {
		if(componentType.isAssignableFrom(getClass())) { store.add(this); }
		parentFreeBranch_.getAllComponents(store, componentType, this);
	}

} //End of class Leaf