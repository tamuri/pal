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
 *
 * @author Matthew Goode
 * @version 1.0
 */

public interface SampleInformation {
    int getHeightUnits();

    int getNumberOfSamples();

    int getSampleOrdinal(String leafID);

    double getHeight(int sample);

    double getMaxHeight();

    interface Factory {
        SampleInformation createSampleInformation(String[] allLeafIDs);
    }

}