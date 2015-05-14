// ThetaHandler.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.supgma;

/**
 * Title:        ThetaHandler
 * Description:  A class for abstracting information on within sample divergence
 * @author Matthew Goode
 */
import pal.mep.*;
import pal.misc.*;
import pal.coalescent.*;

public interface ThetaHandler {
	public int getNumberOfParameters(TimeOrderCharacterData tocd);

	public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd);

	public boolean isCICompatible();
	public String getInfo();

	public void fillInLSInfo(double[] mRow, int startingIndex, int minSample, int maxSample);
	/**
	 * May return null if not possible to infer demographic model
	 * @note most if not all models will use units of Expected Substitutions
	 */
	public DemographicModel generateDemographicModel(double[] deltas, double[] thetas, TimeOrderCharacterData tocd);
	public boolean canGenerateDemogrpahicModel();

	// ======== Utils =================
	public static final class Utils {
		public static final ThetaHandler getSingleThetaHandler() {
			return SingleThetaHandler.INSTANCE;
		}
		public static final ThetaHandler getSingleThetaHandler(boolean isHaploid) {
			return isHaploid ? SingleThetaHandler.HAPLOID_INSTANCE : SingleThetaHandler.DIPLOID_INSTANCE;
		}

		public static final ThetaHandler getSetThetaHandler(double theta) {
			return new SetThetaHandler(theta);
		}
		public static final ThetaHandler getSetThetaHandler(double theta, boolean isHaploid) {

			SetThetaHandler sth = new SetThetaHandler(theta);
			sth.setPloidyNumber(isHaploid ? SetThetaHandler.HAPLOID_PLOIDY_NUMBER : SetThetaHandler.DIPLOID_PLOIDY_NUMBER);
			return sth;
		}

		public static final ThetaHandler getOneThetaPerSampleHandler() {
			return OneThetaPerSampleHandler.INSTANCE;
		}
		// ===== AbstractThetaHandler =======
		abstract private static class AbstractThetaHandler implements ThetaHandler {
			private int ploidyNumber_ = HAPLOID_PLOIDY_NUMBER;
			public static final int HAPLOID_PLOIDY_NUMBER = 2;
			public static final int DIPLOID_PLOIDY_NUMBER = 4;
			public final void setPloidyNumber(final int number) { this.ploidyNumber_ = number; }
			protected final int getPloidyNumber() { return ploidyNumber_; }
			protected final String getPloidyType() {
				return (ploidyNumber_==HAPLOID_PLOIDY_NUMBER ? "Haploid" : "Diploid");
			}
		} //End of AbstractThetaHandler
		// ===== SingleThetaHandler =======
		/**
		 * For a constant population
		 */
		private static final class SingleThetaHandler extends AbstractThetaHandler implements ThetaHandler {
			public static final ThetaHandler INSTANCE = new SingleThetaHandler();
			public static final ThetaHandler HAPLOID_INSTANCE = new SingleThetaHandler(HAPLOID_PLOIDY_NUMBER);
			public static final ThetaHandler DIPLOID_INSTANCE = new SingleThetaHandler(DIPLOID_PLOIDY_NUMBER);
			private SingleThetaHandler() {}
			private SingleThetaHandler(int ploidyNumber) {
				setPloidyNumber(ploidyNumber);
			}
			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				return 1;
			}
			/**
			 * Does nothing as distances do not need to be adjusted
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) { }

			public boolean isCICompatible() { return true; }

			public void fillInLSInfo(final double[] mRow, final int startingIndex, final int minSample, final int maxSample) {
				mRow[startingIndex] = 1;
			}
			/**
			 * @return ConstantPopulationModel
			 */
			public DemographicModel generateDemographicModel(double[] deltas, double[] thetas, TimeOrderCharacterData tocd) {
				//throw new RuntimeException("Not implemented yet!");
				return new ConstantPopulation(thetas[0]/getPloidyNumber(), Units.EXPECTED_SUBSTITUTIONS);
			}
			public boolean canGenerateDemogrpahicModel() { return true;	}
			public String getInfo() { return "Single Theta ("+getPloidyType()+")"; }

		} //End of SingleThetaHandler
		// ===== SetThetaHandler =======
		/**
		 * For a set  theta that is not estimated (it's just given...)
		 */
		private static final class SetThetaHandler extends AbstractThetaHandler implements ThetaHandler {
			private double setTheta_;
			private SetThetaHandler(double theta) {	this.setTheta_ = theta;	}
			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				return 0;
			}
			public boolean isCICompatible() { return false; }

			/**
			 * Reduces distances by an amount proportional to the rate
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) {
				int numberOfTaxa = tocd.getIdCount();
				if(numberOfTaxa!=matrix.length) {
					throw new RuntimeException("Assertion error! Matrix doesn't look compatible with tocd as sizes different ("+matrix.length+", "+numberOfTaxa+")");
				}
				for (int i = 0; i <matrix.length; i++) {
					for(int j = 0 ; j < matrix.length ; j++) {
						if(i!=j) {
							matrix[i][j]-=setTheta_;
						}
					}
				}
			}
			public void fillInLSInfo(final double[] mRow, final int startingIndex, final int minSample, final int maxSample) { }
			/**
			 * @return ConstantPopulationModel
			 */
			public DemographicModel generateDemographicModel(double[] deltas, double[] thetas, TimeOrderCharacterData tocd) {
				//throw new RuntimeException("Not implemented yet!");
				//Need Ploidy info...
				return new ConstantPopulation(setTheta_/getPloidyNumber(), Units.EXPECTED_SUBSTITUTIONS);
			}
			public boolean canGenerateDemogrpahicModel() { return true;	}
			public String getInfo() { return "Set Theta ("+setTheta_+", "+getPloidyType()+")"; }
		} //End of SingleThetaHandler

		private static final class OneThetaPerSampleHandler implements ThetaHandler {
			public static final ThetaHandler INSTANCE = new OneThetaPerSampleHandler();
			private OneThetaPerSampleHandler() {}
			public final int getNumberOfParameters(TimeOrderCharacterData tocd) {
				return tocd.getSampleCount();
			}
			public boolean isCICompatible() { return false; }
			/**
			 * Does nothing as distances do not need to be adjusted
			 */
			public void adjustDistances(double[][] matrix, TimeOrderCharacterData tocd) { }
			public void fillInLSInfo(double[] mRow, final int startingIndex, final int minSample, final int maxSample) {
				mRow[startingIndex+maxSample] = 1;

			}
			/**
			 * @return null
			 */
			public DemographicModel generateDemographicModel(double[] deltas, double[] thetas, TimeOrderCharacterData tocd) {
				return null;
			}
			/**
			 * @return false
			 */
			public boolean canGenerateDemogrpahicModel() { return false;	}
			public String getInfo() { return "Multiple Theta"; }

		} //End of OneRatePerIntervalHandler
	} //End of Utils
} //End of RateHandler