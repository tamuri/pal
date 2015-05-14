// TOCDSampleInformation.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * <p>Title: TOCDSampleInformation </p>
 * <p>Description: A wrapper over the old style TimeOrderCharacterData object for the new SampleInformation object </p>
 * @author Matthew Goode
 * @version 1.0
 */

public class TOCDSampleInformation implements SampleInformation {
  private final TimeOrderCharacterData base_;
	private final boolean hasTimes_;
	public TOCDSampleInformation(TimeOrderCharacterData base) {
		this.base_ = base;
		this.hasTimes_ = base.hasTimes();
  }
	public int getNumberOfSamples() {
	  return base_.getOrdinalCount();
	}
	public int getSampleOrdinal(String leafID) {
	  return base_.getTimeOrdinal(leafID);
	}

	public double getHeight(int sample) {
	  if(hasTimes_) {
			return base_.getOrdinalTime( sample );
		}
		return sample;
	}
	public int getHeightUnits() {
	  if(hasTimes_) {
		  return base_.getUnits();
		}
		return Units.SAMPLE;
	}

	public double getMaxHeight() {
		if(hasTimes_) {
			return base_.getMaximumTime();
		} else {
		  return base_.getOrdinalCount()-1;
		}
	}

}