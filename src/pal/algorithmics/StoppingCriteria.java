// StoppingCriteria.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.algorithmics;

/**
 * Title:        StoppingCriteria
 * Description:  A means of deciding when to stop
 * @author Matthew Goode
 * @version 1.0
 */
import pal.util.AlgorithmCallback;
public interface StoppingCriteria extends java.io.Serializable {
	public boolean isTimeToStop();
	/**
	 * Get an indication of how close to stopping we currently are
	 * @return a value between 0 and 1 where zero means not likely to stop soon, and a value of one means likely to stop very soon
	 */
	public double getRelativeStoppingRatio();

	/**
	 * @param externalStablized if true than other factors have stablized
	 */
	public void newIteration(double currentScore, double bestScore, boolean maximising, boolean externalStablized, AlgorithmCallback callback);
	public void reset();
//===========================================
//=========== Static Factory Class =============
//===========================================
	public static interface Factory extends java.io.Serializable {
		public StoppingCriteria newInstance();
	}
//===========================================
//=========== Static Util Class =============
//===========================================
	public static class Utils {
		/**
		 * A stopping criteria that stops after a set number of iterations
		 * @param maxIterationCount the maximum number of iterations.
		 */
		public static final StoppingCriteria.Factory getIterationCount(int maxIterationCount) {
			return new IterationCountSC.SCFactory(maxIterationCount);
		}
		/**
		 * A stopping criteria that works by counting how many iterations occur at a given score (either the best score or the
		 * current score) and stopping when score does not change after a set number of generations
		 * @param maxIterationCountAtCurrentScore the number of iterations to wait at the current score before stopping
		 * @param matchBestScore if true will examine the best score so far, else will examine the current score so far.
		 */
		public static final StoppingCriteria.Factory getUnchangedScore(int maxIterationCountAtCurrentScore, boolean matchBestScore) {
			return new UnchangedScoreSC.SCFactory(maxIterationCountAtCurrentScore, matchBestScore);
		}
		/**
		 * A stopping criteria that works by counting how many iterations occur at a given score (either the best score or the
		 * current score) and stopping when score does not change after a set number of generations
		 * @param maxIterationCountAtCurrentScore the number of iterations to wait at the current score before stopping
		 * @param matchBestScore if true will examine the best score so far, else will examine the current score so far.
		 */
		public static final StoppingCriteria.Factory getNonExactUnchangedScore(int maxIterationCountAtCurrentScore, boolean matchBestScore, double tolerance) {
			return new NonExactUnchangedScoreSC.SCFactory(maxIterationCountAtCurrentScore, matchBestScore, tolerance);
		}

		/**
		 * A stopping criteria that is a composite of a set of criteria, stops when at least one
		 * sub criteria wants to stop
		 * @param subCriteria an array of StoppingCriteria to combine
		 */
		public static final StoppingCriteria.Factory getCombined(Factory[] subCriteria) {
			return new CombinedSC.SCFactory(subCriteria);
		}

		//Has Serialization code
		private static class IterationCountSC implements StoppingCriteria {
			int count_ = 0;
			int maxIterationCount_;

			//
			// Serialization code
			//
			private static final long serialVersionUID= -883722345529L;

			private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
				out.writeByte(1); //Version number
		    out.writeInt(count_);
				out.writeInt(maxIterationCount_);
			}

			private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
				byte version = in.readByte();
				switch(version) {
					default : {
						count_ = in.readInt();
						maxIterationCount_ = in.readInt();
						break;
					}
				}
			}

			public IterationCountSC(int maxIterationCount) {
				this.maxIterationCount_ = maxIterationCount;
			}

			public void reset() {
				count_ = 0;
			}
			/**
			 * Goes up as the count nears maximum
			 * @return
			 */
			public double getRelativeStoppingRatio() {
				return count_/(double)maxIterationCount_;
			}
			public boolean isTimeToStop() {
				return count_>=maxIterationCount_;
			}
			/**
			 * @param externalStablized if true than other factors have stablized
			 */
			public void newIteration(double currentScore, double bestScore, boolean maximising, boolean externalStablized, AlgorithmCallback callback) {
				count_++;
				callback.updateProgress(count_/(double)maxIterationCount_);
			}
			// ===== Factory ==========
			private static class SCFactory implements Factory {
				private int maxIterationCount_;
				//
				// Serialization code
				//
				private static final long serialVersionUID = -552478345529L;

				private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
					out.writeByte( 1 ); //Version number
					out.writeInt(maxIterationCount_);
				}

				private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
					byte version = in.readByte();
					switch( version ) {
						default: {
							maxIterationCount_ = in.readInt();
							break;
						}
					}
				}

				public SCFactory(int maxIterationCount) {
					this.maxIterationCount_ = maxIterationCount;
				}
				public StoppingCriteria newInstance() {
					return new IterationCountSC(maxIterationCount_);
				}
			}
		}
		//Has Serialization code
		private static class CombinedSC implements StoppingCriteria {
			private StoppingCriteria[] subCriteria_;
			//
			// Serialization code
			//
			private static final long serialVersionUID= -847823472529L;

			private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
				out.writeByte(1); //Version number
				out.writeObject(subCriteria_);
		  }

			private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
				byte version = in.readByte();
				switch(version) {
					default : {
						subCriteria_ = (StoppingCriteria[])in.readObject();
						break;
					}
				}
			}

			public CombinedSC(StoppingCriteria[] subCriteria) {
				this.subCriteria_ = subCriteria;
			}
			public void reset() {
				for(int i = 0 ; i < subCriteria_.length ; i++) {
					subCriteria_[i].reset();
				}
			}
			public double getRelativeStoppingRatio() {
				double max = 0;
				for(int i = 0 ; i < subCriteria_.length ; i++) {
					max = Math.max(max,subCriteria_[i].getRelativeStoppingRatio());
				}
				return max;
			}


			public boolean isTimeToStop() {
				for(int i = 0 ; i < subCriteria_.length ; i++) {
					if(subCriteria_[i].isTimeToStop()) {
						return true;
					}
				}
				return false;
			}
			/**
			 * @param externalStablized if true than other factors have stablized
			 */
			public void newIteration(double currentScore, double bestScore, boolean maximising, boolean externalStablized, AlgorithmCallback callback) {
				for(int i = 0 ; i < subCriteria_.length ; i++) {
					subCriteria_[i].newIteration(currentScore,bestScore,maximising,externalStablized, callback);
				}
			}
			// ===== Factory ==========
			static class SCFactory implements Factory {
				Factory[] subCriteria_;
				//
				// Serialization code
				//
				private static final long serialVersionUID = -525566345529L;

				private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
					out.writeByte( 1 ); //Version number
					out.writeObject(subCriteria_);
				}

				private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
					byte version = in.readByte();
					switch( version ) {
						default: {
							subCriteria_ = (Factory[])in.readObject();
							break;
						}
					}
				}

				public SCFactory( Factory[] subCriteria ) {
					this.subCriteria_ = subCriteria;
				}
				public StoppingCriteria newInstance() {
					StoppingCriteria[] subs = new StoppingCriteria[subCriteria_.length];
					for(int i = 0 ; i < subs.length ; i++) {
						subs[i] = subCriteria_[i].newInstance();
					}
					return new CombinedSC(subs);
				}
			}
		}
// -=-=-=-=
		//Has Serialization code
		private static class UnchangedScoreSC implements StoppingCriteria {
			private int count_ = 0;
			private int maxIterationCountAtCurrentScore_;
			private double lastScore_;
			private boolean matchBestScore_;
			//
			// Serialization code
			//
			private static final long serialVersionUID= -3242345529L;

			private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
				out.writeByte(1); //Version number
		    out.writeInt(count_);
				out.writeInt(maxIterationCountAtCurrentScore_);
				out.writeDouble(lastScore_);
				out.writeBoolean(matchBestScore_);
			}

			private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
				byte version = in.readByte();
				switch(version) {
					default : {
						count_ = in.readInt();
						maxIterationCountAtCurrentScore_ = in.readInt();
						lastScore_ = in.readDouble();
						matchBestScore_ = in.readBoolean();
						break;

					}
				}
			}

			public UnchangedScoreSC(int maxIterationCountAtCurrentScore, boolean matchBestScore) {
				this.maxIterationCountAtCurrentScore_ = maxIterationCountAtCurrentScore;
				this.matchBestScore_ = matchBestScore;
			}

			public void reset() {
				count_ = 0;
			}
			/**
			 * Goes up as the count nears maximum
			 * @return
			 */
			public double getRelativeStoppingRatio() {
				return count_/(double)maxIterationCountAtCurrentScore_;
			}

			public boolean isTimeToStop() {
				return count_>=maxIterationCountAtCurrentScore_;
			}

			/**
			 * @param externalStablized if true than other factors have stablized
			 */
			public void newIteration(double currentScore, double bestScore, boolean maximising, boolean externalStablized, AlgorithmCallback callback){
				if(!externalStablized) {
					return;
				}
				if(count_==0) {
					lastScore_ = (matchBestScore_ ? bestScore : currentScore);
				} else {
					if(matchBestScore_) {
						if((!maximising&&(bestScore<lastScore_))||(maximising&&(bestScore>lastScore_))) {
							lastScore_ = bestScore;
							count_ = 0;
						}
					} else {
						if(lastScore_!=currentScore) {
							lastScore_ = currentScore;
							count_ = 0;
							callback.updateStatus("Restarting count...");
						}
					}
				}
				count_++;
			}
			// ===== Factory ==========
			static class SCFactory implements Factory {
				private int maxIterationCountAtCurrentScore_;
				private boolean matchBestScore_;
				//
				// Serialization code
				//
				private static final long serialVersionUID = -1234567785529L;

				private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
					out.writeByte( 1 ); //Version number
					out.writeInt(maxIterationCountAtCurrentScore_);
					out.writeBoolean(matchBestScore_);
				}

				private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
					byte version = in.readByte();
					switch( version ) {
						default: {
					  maxIterationCountAtCurrentScore_ = in.readInt();
						matchBestScore_ = in.readBoolean();
							break;
						}
					}
				}

				public SCFactory( int maxIterationCountAtCurrentScore, boolean matchBestScore ) {
					this.maxIterationCountAtCurrentScore_ = maxIterationCountAtCurrentScore;
					this.matchBestScore_ = matchBestScore;
				}
				public StoppingCriteria newInstance() {
					return new UnchangedScoreSC(maxIterationCountAtCurrentScore_,matchBestScore_);
				}
			}
		}
		// -==-=--=

		private static class NonExactUnchangedScoreSC implements StoppingCriteria {
			private int count_ = 0;
			private int maxIterationCountAtCurrentScore_;
			private double lastScore_;
			private boolean matchBestScore_;
			private double tolerance_;

			//
			// Serialization Code
			//
			private static final long serialVersionUID = -56982234429L;

			private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
				out.writeByte( 1 ); //Version number
				out.writeInt( count_ );
				out.writeInt( maxIterationCountAtCurrentScore_ );
				out.writeDouble( lastScore_ );
				out.writeBoolean( matchBestScore_ );
				out.writeDouble( tolerance_ );
			}

			private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
				byte version = in.readByte();
				switch( version ) {
					default: {
						count_ = in.readInt();
						maxIterationCountAtCurrentScore_ = in.readInt();
						lastScore_ = in.readDouble();
						matchBestScore_ = in.readBoolean();
						tolerance_ = in.readDouble();
						break;
					}
				}
			}


			public NonExactUnchangedScoreSC(int maxIterationCountAtCurrentScore, boolean matchBestScore,  double tolerance) {
				this.maxIterationCountAtCurrentScore_ = maxIterationCountAtCurrentScore;
				this.tolerance_ = tolerance;
				this.matchBestScore_ = matchBestScore;
			}

			public void reset() {
				count_ = 0;
			}

			public boolean isTimeToStop() {
				return count_>=maxIterationCountAtCurrentScore_;
			}
			/**
			 * Goes up as the count nears maximum
			 * @return
			 */
			public double getRelativeStoppingRatio() {
				return count_/(double)maxIterationCountAtCurrentScore_;
			}

			/**
			 * @param externalStablized if true than other factors have stablized
			 */
			public void newIteration(double currentScore, double bestScore, boolean maximising, boolean externalStablized, AlgorithmCallback callback){
				if(!externalStablized) {
					return;
				}
				if(count_==0) {
					lastScore_ = (matchBestScore_ ? bestScore : currentScore);
				} else {
					if(matchBestScore_) {
						if((!maximising&&(bestScore<lastScore_-tolerance_))||(maximising&&(bestScore>lastScore_+tolerance_))) {
							lastScore_ = bestScore;
							count_ = 0;
						}
					} else {
						if(Math.abs(lastScore_-currentScore)>tolerance_) {
							lastScore_ = currentScore;
							count_ = 0;
						}
					}
				}
				count_++;
			}
			// ===== Factory ==========
			static class SCFactory implements Factory {
				private int maxIterationCountAtCurrentScore_;
				private boolean matchBestScore_;
				private double tolerance_;
				//
			// Serialization Code
			//
			private static final long serialVersionUID = -4523982234429L;

			private void writeObject( java.io.ObjectOutputStream out ) throws java.io.IOException {
				out.writeByte( 1 ); //Version number
				out.writeInt( maxIterationCountAtCurrentScore_ );
				out.writeBoolean( matchBestScore_ );
				out.writeDouble( tolerance_ );
			}

			private void readObject( java.io.ObjectInputStream in ) throws java.io.IOException, ClassNotFoundException {
				byte version = in.readByte();
				switch( version ) {
					default: {
						maxIterationCountAtCurrentScore_ = in.readInt();
						matchBestScore_ = in.readBoolean();
						tolerance_ = in.readDouble();
						break;
					}
				}
			}
				public SCFactory(int maxIterationCountAtCurrentScore, boolean matchBestScore, double tolerance) {
					this.maxIterationCountAtCurrentScore_ = maxIterationCountAtCurrentScore;
					this.matchBestScore_ = matchBestScore;
					this.tolerance_ =tolerance;
				}
				public StoppingCriteria newInstance() {
					return new NonExactUnchangedScoreSC(maxIterationCountAtCurrentScore_,matchBestScore_, tolerance_);
				}
			}
		}

	}
}