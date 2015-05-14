// PatternInfo.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.eval;

/**
 * <p>Title: PatternInfo </p>
 * <p>Description: Describes particular site pattern information based on upto two children patterns, and can adapt.  </p>
 * @author Matthew Goode
 * @version 1.0
 */

public final class PatternInfo {
  private final int[] sitePatternMatchup_;
  private final int[] patternWeights_;
  private final int[] patternLookup_;
  private int numberOfPatterns_;
	/**
	 * Cloning constructor
	 * @param toCopy The PatternInfo to copy
	 */
	private PatternInfo(PatternInfo toCopy) {
	  this.sitePatternMatchup_ = pal.misc.Utils.getCopy(toCopy.sitePatternMatchup_);
		this.patternWeights_ = pal.misc.Utils.getCopy(toCopy.patternWeights_);
		this.patternLookup_ = pal.misc.Utils.getCopy(toCopy.patternLookup_);
		this.numberOfPatterns_ = toCopy.numberOfPatterns_;

	}
  public PatternInfo(int numberOfSites, boolean binaryPattern) {
    this.sitePatternMatchup_ = new int[numberOfSites];
    this.patternWeights_ = new int[numberOfSites];
    this.numberOfPatterns_ = 0;
    patternLookup_ = new int[(binaryPattern ? numberOfSites*2 : numberOfSites)];
  }
  public PatternInfo(int[] sitePatternMatchup, int[] patternWeights, int[] patternLookup, int initialNumberOfPatterns) {
    this.sitePatternMatchup_ = sitePatternMatchup;
    this.patternWeights_ = patternWeights;
    this.patternLookup_ = patternLookup;
    this.numberOfPatterns_ = initialNumberOfPatterns;
  }
  public PatternInfo(int[] sitePatternMatchup,  int initialNumberOfPatterns) {
    this(sitePatternMatchup,null,initialNumberOfPatterns);
  }
  public PatternInfo(int[] sitePatternMatchup, int[] patternWeights, int initialNumberOfPatterns) {
    this(sitePatternMatchup,patternWeights,null, initialNumberOfPatterns);
  }
	/**
	 * Obtain an exact copy of this pattern info
	 * @return the required copy
	 */
	public PatternInfo getCopy() {
	  return new PatternInfo(this);
	}
  public String toString() {
    return pal.misc.Utils.toString(patternLookup_,numberOfPatterns_*2);
  }
  public String sitePatternMatchupToString() {
    return pal.misc.Utils.toString(sitePatternMatchup_);
  }
  public final int[] getPatternLookup() { return patternLookup_; }
  public int[] getPatternWeights() {		return patternWeights_; 	}
  public final int getNumberOfSites() { return sitePatternMatchup_.length; }
  public final int[] getSitePatternMatchup() {	return sitePatternMatchup_;	}
  public final int getNumberOfPatterns() {	return numberOfPatterns_;		}
  public void setNumberOfPatterns(int n) {	this.numberOfPatterns_ = n;		}
  public int build(PatternInfo leftPattern, PatternInfo rightPattern,  final int numberOfSites ) {
    if(rightPattern.getNumberOfPatterns()==0) {
      System.out.println("Error: right has zero patterns");
			Thread.dumpStack();
    }
    if(leftPattern.getNumberOfPatterns()==0) {
      System.out.println("Error: left has zero patterns");
    }

    final int numberOfLeftPatterns = leftPattern.getNumberOfPatterns();
    final int numberOfRightPatterns = rightPattern.getNumberOfPatterns();

    final int[] leftSitePatternMatchup = leftPattern.getSitePatternMatchup();
    final int[] rightSitePatternMatchup = rightPattern.getSitePatternMatchup();
    int uniqueCount = 0;
    //	table.clear();
    int uniqueCountTimesTwo = 0;
    for(int i = 0 ; i < numberOfSites ; i++) {
      final int leftPatternIndex = leftSitePatternMatchup[i];
      final int rightPatternIndex = rightSitePatternMatchup[i];
      final int patternIndex = getMatchingPattern(leftPatternIndex,rightPatternIndex,patternLookup_, uniqueCount);
      if(patternIndex<0) {
        sitePatternMatchup_[i] = uniqueCount;
        patternLookup_[uniqueCountTimesTwo++] = leftPatternIndex;
        patternLookup_[uniqueCountTimesTwo++] = rightPatternIndex;
        patternWeights_[uniqueCount++]=1;
      } else {
        patternWeights_[patternIndex]++;
        sitePatternMatchup_[i] = patternIndex;
      }
    }
    numberOfPatterns_ = uniqueCount;

    return uniqueCount;
  } //End of buildPatternInfo()
  /**
   * @return the index of mathcing pattern (if already found), or -1 otherwise.
   */
  private static final int getMatchingPattern(final int leftPattern, final int rightPattern, final int[] patternLookup, final int numberOfPatternsFoundSoFar) {
    int index = 0;
    for(int i = 0 ; i < numberOfPatternsFoundSoFar ; i++) {
      boolean matchLeft = patternLookup[index++]==leftPattern;
      boolean matchRight = patternLookup[index++]==rightPattern;
      if(matchLeft&&matchRight) { return i; }
    }
    return -1;
  }

}
