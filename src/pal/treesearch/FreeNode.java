// FreeNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: FreeNode </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;

import pal.eval.*;
import pal.tree.*;


public interface FreeNode extends GeneralTreeComponent {
	public PatternInfo getPatternInfo(GeneralConstructionTool tool, FreeBranch caller);
	public boolean hasConnection(FreeBranch c, FreeBranch caller);
	public FreeBranch getLeftBranch(FreeBranch caller);
	public FreeBranch getRightBranch(FreeBranch caller);

	/**
	 * Recurse to all neighbours but caller
	 * @return the maximum number of patterns from any neighbour
	 */
//	public int rebuildPattern( GeneralConstructionTool tool, FreeBranch caller, boolean firstPass);
//
//	public int rebuildPattern(GeneralConstructionTool tool);

	public void getAllComponents(ArrayList store, Class componentType, FreeBranch connection);

	public void testLikelihood(FreeBranch caller, GeneralConstructionTool tool);

	/**
	 * This should only be called by another leaf node on the other end of the connection.
	 * In this case we don't have to do much (tree is two node tree)
	 */
//	public int redirectRebuildPattern(GeneralConstructionTool tool);

//	public ConditionalProbabilityStore getLeftExtendedConditionalProbabilities(FreeBranch callingConnection, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore);
//	public ConditionalProbabilityStore getRightExtendedConditionalProbabilities( FreeBranch callingConnection, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore);

	/**

	 * @param caller
	 * @return Get the pattern info for the relative left (from the caller's perspective), or null if not left pattern info
	 */
	public PatternInfo getLeftPatternInfo(GeneralConstructionTool tool, FreeBranch caller);
	/**
	 * @param caller
	 * @return Get the pattern info for the relative right (from the caller's perspective), or null if not right pattern info
	 */
	public PatternInfo getRightPatternInfo(GeneralConstructionTool tool, FreeBranch caller);

	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, FreeBranch callingConnection, GeneralConstructionTool tool);
	public ConditionalProbabilityStore getExtendedConditionalProbabilities( double distance, FreeBranch callingConnection, UnconstrainedLikelihoodModel.External external, ConditionalProbabilityStore resultStore, GeneralConstructionTool tool);

	/**
	 * Instruct the node to extract itself from the two connections that aren't the caller
	 * One of the other two connections will become redunant.
	 * @return the redundant connection, or null of this node can't extract
	 */
	public FreeBranch extract(FreeBranch caller);


	public Node buildPALNodeES(double branchLength_,FreeBranch caller);
	public Node buildPALNodeBase(double branchLength_,FreeBranch caller);

	public ConditionalProbabilityStore getFlatConditionalProbabilities(FreeBranch caller, GeneralConstructionTool tool);

	public String toString(FreeBranch caller);

	public void setConnectingBranches(FreeBranch[] store, int number);

	public boolean hasDirectConnection(FreeBranch query);

	/**
	 * Should not do anything but swap branches around
	 */
	public void swapConnection(FreeBranch original,FreeBranch newConnection);

	/**
	 * Should preserve tree integrity
	 */
	public void swapConnection(FreeBranch original, FreeNode nodeToReplace, FreeBranch newConnection);

}