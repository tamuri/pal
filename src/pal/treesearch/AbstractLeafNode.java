// AbstractLeafNode.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: AbstractLeafNode </p>
 * <p>Description: </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.eval.*;

public abstract class AbstractLeafNode {
	private final String id_;
	private final int[] patternStateMatchup_;
	private final int uniqueCount_;

	private final int[] sequence_;
	private final PatternInfo pattern_;
	public AbstractLeafNode(String id, GeneralConstructionTool tool) {
	  this.id_ = id;
		this.sequence_ = tool.getSequence(id);
		final int numberOfStates = tool.getNumberOfStates();
		final int numberOfSites = tool.getNumberOfSites();

		patternStateMatchup_ = new int[numberOfStates+1];
		final int[] sitePatternMatchup = new int[numberOfSites];

		this.uniqueCount_ = SearcherUtils.createMatchups( numberOfSites, numberOfStates, sitePatternMatchup, patternStateMatchup_, sequence_ );
		this.pattern_ = new PatternInfo( sitePatternMatchup, uniqueCount_	);
  }
	protected final UnconstrainedLikelihoodModel.Leaf createNewFreeLeafCalculator( GeneralConstructionTool tool) {
		return tool.createNewFreeLeafCalculator(patternStateMatchup_,uniqueCount_);
	}
	protected final MolecularClockLikelihoodModel.Leaf createNewConstrainedLeafCalculator(ConstraintModel.GroupManager parentGroup) {
		return parentGroup.createNewClockLeaf(pattern_, patternStateMatchup_);
	}

	public final String getLabel() { return id_; }
	public final PatternInfo getPatternInfo() { return pattern_; }
	public final int getNumberOfPatterns() { return pattern_.getNumberOfPatterns(); }

}