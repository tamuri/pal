// SingleSplitDistribution.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: SingleSplitDistribution </p>
 * <p>Description: A SingleSplitDistribution determines how a TACS analysis with only one split distributes the before and after classes (deliberately cryptic awaiting related paper!)</p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.misc.NeoParameterized;
public interface SingleSplitDistribution extends NeoParameterized{
	public int getNumberOfBaseTransitionCategories();
	/**
	 * Obtain the distribution information stored in the following format: [beforeclass][afterclass]
	 * For example, the probability of evolving in class 0 before the split, and class 1 after the split - [0][1]
	 *
	 * @return an appropriate matrix representing the probability of evolving in the different combinations of before and after classes
	 */
	public double[][] getDistributionInfo();
}