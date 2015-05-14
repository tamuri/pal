// RateHandler.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.supgma;

/**
 * Title:        RateHandler
 * Description:  A class for interfacing with MutationRateModels
 * @author Matthew Goode
 */
import pal.mep.*;
import pal.misc.*;

public interface RateHandler {
	public int getNumberOfParameters(TimeOrderCharacterData tocd);

	public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd);

	public boolean isCICompatible();
	public String getInfo();

	public void fillInLSInfo(double[] mRow, int startingIndex, int minSample, int maxSample, double[] sampleTimes);
	public MutationRateModel.Factory generateRateModelFactory(double[] deltas, TimeOrderCharacterData tocd);
	// ======== Utils =================
	public static final class Utils {
		public static final RateHandler getSingleRateHandler() {
			return SingleRateHandler.INSTANCE;
		}
		/**
		 * @return the number of intervals given the arbitary intervalChangeTimes (assumed to
		 * hold no negative numbers not including zero) and the maxSampleTime
		 */
		public static final int getArbitaryIntervalCount(double[] intervalChangeTimes, double maxSampleTime) {
			int paramCount = 1;
			for(int i = 0 ; i < intervalChangeTimes.length ; i++) {
				if(intervalChangeTimes[i]< maxSampleTime) {
					paramCount++;
				}
			}
			return paramCount;
		}

		public static final RateHandler getSetRateHandler(double rate, int units) {
			return new SetRateHandler(new ConstantMutationRate(rate,units,rate,rate));
		}

		public static final RateHandler getSetRateHandler(MutationRateModel model) {
			return new SetRateHandler(model);
		}
		public static final RateHandler getOneRatePerIntervalHandler() {
			return OneRatePerIntervalHandler.INSTANCE;
		}
		public static final RateHandler getArbitaryIntervalHandler(double[] times) {
			return new ArbitaryIntervalHandler(times);
		}
		/**
		 * @return the interval change times (drops the first time if it is zero essentially...)
		 */
		private static final double[] getIntervalChangeTimes(double[] times) {
			if(times[0]<0.000001) {
				return pal.misc.Utils.getCopy(times,1);
			}
			return pal.misc.Utils.getCopy(times);
		}
		// ===== SingleRateHandle =======
		/**
		 * For a single mutation rate
		 */
		private static final class SingleRateHandler implements RateHandler {
			public static final RateHandler INSTANCE = new SingleRateHandler();
			private SingleRateHandler() {}
			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				return 1;
			}
			public boolean isCICompatible() { return true; }

			/**
			 * Does nothing as distances do not need to be adjusted
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) { }

			public void fillInLSInfo(final double[] mRow, final int startingIndex, final int minSample, final int maxSample, double[] sampleTimes) {
				//m[index][numberOfThetas] = Math.abs(iTime - tocd.getTime(j));
				//mRow[startingIndex] = Math.abs(iTime-tocd.getTime(j));
				mRow[startingIndex] = sampleTimes[maxSample] - sampleTimes[minSample];
			}
			public MutationRateModel.Factory generateRateModelFactory(double[] deltas, TimeOrderCharacterData tocd) {
				return ConstantMutationRate.getFreeFactory(deltas[0],tocd.getUnits(),tocd.getSuggestedMaximumMutationRate());
			}
			public String getInfo() { return "Single Rate"; }

		} //End of SingleRateHandler
		// ===== SetRateHandle =======
		/**
		 * For a set mutation rate that is not estimated (it's just given...)
		 */
		private static final class SetRateHandler implements RateHandler {
			private MutationRateModel rateModel_;
			private SetRateHandler(MutationRateModel rateModel) {
				this.rateModel_ = rateModel;
			}
			public boolean isCICompatible() { return false; }

			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				checkTimes(tocd);
				return 0;
			}
			private final void checkTimes(TimeOrderCharacterData tocd) {
				if(!tocd.hasTimes()) {
					throw new RuntimeException("Assertion error : SetRateHanlder used on untimed data!");
				}
			}
			public String getInfo() { return "Set Rate Model ("+rateModel_.toSingleLine()+")"; }

			/**
			 * Reduces distances by an amount proportional to the rate
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) {
				checkTimes(tocd);
				int numberOfTaxa = tocd.getIdCount();
				if(numberOfTaxa!=matrix.length) {
					throw new RuntimeException("Assertion error! Matrix doesn't look compatible with tocd as sizes different ("+matrix.length+", "+numberOfTaxa+")");
				}
				for (int i = 0; i < numberOfTaxa; i++) {
					//int iOrdinal = tocd.getTimeOrdinal(i);
					double iTime = tocd.getTime(i);
					for(int j = 0 ; j < numberOfTaxa ; j++) {
						if(i!=j) {
							//int jOrdinal = tocd.getTimeOrdinal(j);
							double jTime = tocd.getTime(j);
							matrix[i][j]-=
								rateModel_.
									getExpectedSubstitutions(
										Math.min(iTime,jTime),
										Math.max(iTime,jTime)
									);
						}
					}
				}
			}

			public void fillInLSInfo(final double[] mRow, final int startingIndex, final int minSample, final int maxSample, double[] sampleTimes) {
			}
			public MutationRateModel.Factory generateRateModelFactory(double[] deltas, TimeOrderCharacterData tocd) {
				return rateModel_.generateFactory();
			}
		} //End of SingleRateHandler
		// ===== OneRatePerIntervalHandler =======
		/**
		 * For a separate estimated rate per interval
		 */
		private static final class OneRatePerIntervalHandler implements RateHandler {
			public static final RateHandler INSTANCE = new OneRatePerIntervalHandler();
			private OneRatePerIntervalHandler() {}
			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				return tocd.getSampleCount()-1;
			}
			public boolean isCICompatible() { return false; }


			/**
			 * Does nothing as distances do not need to be adjusted
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) { }
			public void fillInLSInfo(double[] mRow, final int startingIndex, final int minSample, final int maxSample, final double[] sampleTimes) {
				for(int sample = minSample ; sample < maxSample ; sample++) {
					mRow[sample+startingIndex] = sampleTimes[sample+1]-sampleTimes[sample];
				}
			}
			public MutationRateModel.Factory generateRateModelFactory(double[] deltas, TimeOrderCharacterData tocd) {
				return SteppedMutationRate.getFactory(deltas,getIntervalChangeTimes(tocd.getUniqueTimeArray()),tocd.getUnits(),tocd.getSuggestedMaximumMutationRate());
			}
			public String getInfo() { return "One Rate Per Interval"; }

		} //End of OneRatePerIntervalHandler

		// ===== ArbitaryIntervalHandler =======
		/**
		 * For a separate estimated rate per arbitary interval
		 */
		private static final class ArbitaryIntervalHandler implements RateHandler {
			private double[] intervalChangeTimes_;
			private ArbitaryIntervalHandler(double[] times) {
				times = pal.misc.Utils.getCopy(times);
				pal.util.HeapSort.sort(times);
				this.intervalChangeTimes_ = getIntervalChangeTimes(times);
			}
			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				return getArbitaryIntervalCount(intervalChangeTimes_,tocd.getMaximumTime());
			}

			public boolean isCICompatible() { return false; }

			/**
			 * Does nothing as distances do not need to be adjusted
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) { }
			public void fillInLSInfo(double[] mRow, final int startingIndex, final int minSample, final int maxSample, final double[] sampleTimes) {
				double startTime = sampleTimes[minSample];
				double endTime = sampleTimes[maxSample];
				double lowerTime = 0;
				//Initialise
				for(int interval = 0 ; interval < intervalChangeTimes_.length ; interval++) {
					mRow[startingIndex+interval] = 0;
				}

				for(int interval = 0 ; interval < intervalChangeTimes_.length ; interval++) {
					double higherTime = intervalChangeTimes_[interval];
					if(startTime>=lowerTime) {
						if(startTime<higherTime) {
							if(endTime<=higherTime) {
								mRow[startingIndex+interval]
									= endTime-startTime;
							} else {
								mRow[startingIndex+interval]
									= higherTime-startTime;
							}
						}
					} else {
						//StartTime is less than lowerTime
						if(endTime>=lowerTime) {
							if(endTime<=higherTime) {
								mRow[startingIndex+interval]
									= endTime-lowerTime;
							} else {
								mRow[startingIndex+interval]
									= higherTime-lowerTime;
							}
						}
					}
					lowerTime = higherTime;
				}
				//If we go over the edge...
				if(endTime>lowerTime) {
					mRow[startingIndex+intervalChangeTimes_.length] = endTime-lowerTime;
				}
			}
			public String getInfo() { return "Arbitrary Intervals (Change times:"+pal.misc.Utils.toString(intervalChangeTimes_)+")"; }

			public MutationRateModel.Factory generateRateModelFactory(double[] deltas, TimeOrderCharacterData tocd) {
				return SteppedMutationRate.getFactory(deltas,intervalChangeTimes_,tocd.getUnits(),tocd.getSuggestedMaximumMutationRate());
			}
		} //End of AbitaryIntervalHandler
	} //End of Utils
} //End of RateHandler