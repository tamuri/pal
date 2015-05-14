// SearcherUtils.java
//
// (c) 1999-2003 PAL Development Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.treesearch;

/**
 * <p>Title: SearcherUtils </p>
 * <p>Description: Utility Methods used by the Searcher classes</p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.math.*;
public class SearcherUtils {
	/**
	 * Fill in matchup arrays
	 * @param numberOfSites The number of sites
	 * @param numberOfStates The number of states
	 * @param sitePatternMatchup Should be of length numberOfSites
	 * @param patternStateMatchup Should be of length numberOfStates+1
	 * @param sequence the sequence
	 * @return the number of patterns
	 */
	public final static int createMatchups(final int numberOfSites, final int numberOfStates, final int[] sitePatternMatchup, final int[] patternStateMatchup, final int[] sequence) {
		final int[] stateCount  = new int[numberOfStates+1];
		// StatePatternMatchup matches a state to it's new pattern (is undefined if state does not occur)
		final int[] statePatternMatchup = new int[numberOfStates+1];
		int uniqueCount = 0;
		for(int site = 0 ; site < numberOfSites ; site++) {
			final int state = sequence[site];
			if(stateCount[state]==0) {
				stateCount[state] = 1;
				int pattern = uniqueCount++;
				patternStateMatchup[pattern] = state;
				statePatternMatchup[state] = pattern;
			} else {
				stateCount[state]++;
			}
			sitePatternMatchup[site] = statePatternMatchup[state];
		}
		return uniqueCount;
	}

	public static final String[][] split(String[] leafNames, MersenneTwisterFast r) {
		if(leafNames.length==2) {
			return new String[][] {{leafNames[0]}, {leafNames[1]}};
		}
		int split = r.nextInt(leafNames.length-2)+1;
		return new String[][] {
			subset(leafNames,0,split),
			subset(leafNames,split)
		};
	}

	private final static String[] subset(String[] array, int starting, int width) {
		String[] subset = new String[width];
		System.arraycopy(array,starting,subset,0,width);
		return subset;
	}


	private final static String[] subset(String[] array, int starting) {
		return subset(array,starting,array.length-starting);
	}

}