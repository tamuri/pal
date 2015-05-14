// TemporalModelChange.java
//
// (c) 1999-2004 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.substmodel;

/**
 * <p>Title: TemporalModelChange </p>
 * <p>Description: Details the temporal intervals around which model parameters change </p>
 * @author Matthew Goode
 * @version 1.0
 */
import pal.misc.*;
public interface TemporalModelChange extends java.io.Serializable {
	public double getChangeHeight(double[] sampleHeights);
// =-=-=-=-=-=-=-==-=-
	public static final class Utils {
	  public static final TemporalModelChange getSampleLinked(int sampleNumber) {
		  return new SampleLinked(sampleNumber);
		}
		public static final TemporalModelChange getSpecificTime(double time, TimeOrderCharacterData tocd) {
		  return new SpecificTime(time,tocd);
		}
		public static final TemporalModelChange getSpecificHeight(double height) {
		  return new SpecificHeight(height);
		}
		// -==--=-=
		private final static class SampleLinked implements TemporalModelChange {
			private final int sampleNumber_;
			//
			// Serialization Code
			//
			private static final long serialVersionUID = 232554478781247854L;

			public SampleLinked(int sampleNumber) {
			  this.sampleNumber_ = sampleNumber;
			}
			public double getChangeHeight(double[] sampleHeights) {
			  return sampleHeights[sampleNumber_];
			}

		}
		// -==--=-=
		private final static class SpecificTime implements TemporalModelChange {
			private final double time_;
			private final double timeDiff_;
			private final int intervalFirstSample_;
			private final double intervalStartTime_;
			private final double intervalWidth_;
			//
			// Serialization Code
			//
			private static final long serialVersionUID = 744728472857724L;

			public SpecificTime(double time, TimeOrderCharacterData tocd) {
			  this.time_ = time;
				int numberOfSamples = tocd.getOrdinalCount();
				double[] ordinalTimes = new double[numberOfSamples];
				for(int i = 0 ; i < numberOfSamples ; i++) {
				  ordinalTimes[i] = tocd.getOrdinalTime(i);
				}
				double intervalStart = ordinalTimes[0];
				double intervalEnd = ordinalTimes[1];
				int intervalSample = 0;
				for(int i = 1 ; i < numberOfSamples-1 ; i++) {
					if(time>=intervalStart&&time<intervalEnd) {
					  break;
					}
					intervalSample = i;
					intervalStart = ordinalTimes[i];
					intervalEnd = ordinalTimes[i+1];
				}

				this.intervalFirstSample_ = intervalSample;
				this.intervalStartTime_ = intervalStart;
				this.intervalWidth_ = intervalEnd-intervalStart;
				this.timeDiff_ = time-intervalStartTime_;
			}

			public double getChangeHeight(double[] sampleHeights) {
				final double startHeight = sampleHeights[intervalFirstSample_];
				final double heightRange = sampleHeights[intervalFirstSample_+1]-startHeight;
				final double rate = heightRange/intervalWidth_;
				double changeHeight = startHeight+(timeDiff_*rate);
				return changeHeight;
			}
		  public String toString() {
			  return "Time split("+time_+", "+timeDiff_+", "+intervalFirstSample_+", "+intervalStartTime_+", "+intervalWidth_+")";
			}
		} //End of class SpecificTime
		// ================================================================================
	  // -==--=-=
		private final static class SpecificHeight implements TemporalModelChange {
			private final double height_;

			//
			// Serialization Code
			//
			private static final long serialVersionUID = 4783748882221L;

			public SpecificHeight(double height) {
				this.height_ = height;
				}
			public double getChangeHeight(double[] sampleHeights) {
				return height_;
			}
			public String toString() {
				return "Height split("+height_+")";
			}
		}
	}


}