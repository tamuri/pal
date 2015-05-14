// GroupLeader.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: GroupLeader </p>
 * <p>Description: A group leader controls a group of constrained clades in a tree</p>
 * @author Matthew Goode
 * @version 1.0
 */

public interface GroupLeader {

	public void obtainLeafInformation(HeightInformationUser user);

	/**
	 * Tell the group leader that the groupConstraints have been set up (parameter wise),
	 * and that internal node heights for example may be calculated
	 */
	public void postSetupNotify(ConstraintModel.GroupManager groupConstraints);

//	public void recursivelyMarkHeights(double[] currentHeightComponents);
//
//	public void recursivelyUpdateHeightFromMark(double[] heightComponentsDifferences);
//
//	public void setLeafHeightsAndValidateInternalHeights(double[] heightComponents);

}