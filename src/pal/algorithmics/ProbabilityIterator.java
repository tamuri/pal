// ProbabilityIterator.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.algorithmics;
/**
 * A function for obtaining probabilities (that may change over time)
 * @author Matthew Goode
*/

public interface ProbabilityIterator {
	public double getNextProbability(double currentBest, double testValue, boolean maximising);
	public boolean isStablised();
	public static interface Factory {
		public ProbabilityIterator newInstance();
	}
	public static class Utils {
		public static final Factory getConstant(double value) {
			return new Constant.PIFactory(value);
		}

		public static final Factory getHillClimb() {
			return new HillClimb.PIFactory();
		}


		public static final Factory getBoltzman(double initialTemperature, double temperatureDecay, int chainLength) {
			return new Boltzman.PIFactory(initialTemperature,temperatureDecay,chainLength);
		}
		//=============================================================

		private static class Constant implements ProbabilityIterator {
			double value_;
			public Constant(double value ) { this.value_ = value;	}

			public double getNextProbability(double currentBest, double testValue, boolean maximising) {
				return value_;
			}
			public boolean isStablised() { return true; }

			static class PIFactory implements Factory {
				Constant intstance_;
				public PIFactory(double value ) { this.intstance_ = new Constant(value);	}
				public ProbabilityIterator newInstance() { return intstance_; }
			}
		}
		private static class HillClimb implements ProbabilityIterator {

			public double getNextProbability(double currentBest, double testValue, boolean maximising) {
				if(maximising) {
				return(testValue>=currentBest ? 1 : 0);
				}
				return(testValue<=currentBest ? 1 : 0);
			}
			public boolean isStablised() { return true; }

			static class PIFactory implements Factory {
				private static final HillClimb INSTANCE = new HillClimb();
					public ProbabilityIterator newInstance() { return INSTANCE; }
			}
		}
		// === Boltzman ====
		private static class Boltzman implements ProbabilityIterator{
			double initialTemperature_;
			double temperatureDecay_;
			int chainLength_;
			double k_ = 1;
			double currentTemperature_;
			int chainPosition_;
			public Boltzman(double initialTemperature, double temperatureDecay, int chainLength) {
				this.initialTemperature_ = initialTemperature;
				this.temperatureDecay_ = temperatureDecay;
				this.chainLength_ = chainLength;
				this.currentTemperature_ = initialTemperature_;
				this.chainPosition_ = 0;
			}
			public boolean isStablised() { return currentTemperature_<0.005; }

			public double getNextProbability(double currentValue, double newValue, boolean maximising) {
				double toReturn;
				if(maximising) {
					if(newValue>currentValue) {
						toReturn = 1;
					} else {
						toReturn = Math.exp(-(currentValue-newValue)/(k_*currentTemperature_));
					}
				} else {
					if(newValue<currentValue) {
						toReturn = 1;
					} else {
						toReturn =  Math.exp(-(newValue- currentValue)/(k_*currentTemperature_));
					}
				}
				chainPosition_++;
				if(chainPosition_==chainLength_) {
					chainPosition_ = 0;
					currentTemperature_*=temperatureDecay_;
					System.out.println("****:"+currentTemperature_);
				}
				return toReturn;
			}
			static class PIFactory implements Factory {
				double initialTemperature_;
				double temperatureDecay_;
				int chainLength_;
				public PIFactory(double initialTemperature, double temperatureDecay, int chainLength) {
					this.initialTemperature_ = initialTemperature;
					this.temperatureDecay_ = temperatureDecay;
					this.chainLength_ = chainLength;
				}
				public ProbabilityIterator newInstance() {
					return new Boltzman(initialTemperature_,temperatureDecay_,chainLength_);
				}
			}
		}//End of boltzman
	}
}