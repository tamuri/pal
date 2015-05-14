// SampleInformation.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * <p>Title: SampleInformation </p>
 * <p>Description: A replacement for TimeOrderedCharacterData objects </p>
 * @author Matthew Goode
 * @version 1.0
 */

public interface SampleInformation {
	public int getHeightUnits();
	public int getNumberOfSamples();
	public int getSampleOrdinal( String leafID );
	public double getHeight( int sample );
	public double getMaxHeight();

	public static interface Factory {
	  public SampleInformation createSampleInformation(String[] allLeafIDs);
	}

}