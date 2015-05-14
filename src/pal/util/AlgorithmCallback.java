// AlgorithmCallback.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.util;

/**
 * An AlgorithmCallback is an interface for any algorithm that wishes to communicate back with
 * it's caller while running (eg, to check if should stop)
 * To be intergrated into the use of algorithms that take substantial ammounts of time.
 * <b>History </b>
 *  <ul>
 *   <li> 14/10/2003 - Added PrintWriter stuff </li>
 *  </ul>
 * @version $Id: AlgorithmCallback.java,v 1.4 2004/04/25 22:53:15 matt Exp $
 * @author Matthew Goode
 */
import java.io.*;

public interface AlgorithmCallback {
	/**
	 * Should be called intermittedly to check if algorithm should stop (should return null if has output)
	 */
	public boolean isPleaseStop();
	/**
	 * @param progress between 0..1
	 */
	public void updateProgress(double progress);
	public void clearProgress();
	/**
	 * Inform caller of current status
	 */
	public void updateStatus(String statusString);

	// ==========================================================================
	// ==== Static utility class
	/**
	 * A Utility class that provides some simple implementations of AlgorithmCallback
	 * that can be used for manipulating callback results
	 */
	public static final class Utils {
		/**
		 * @return an AlgorithmCallback object that never says it is time to stop,
		 * and ignores all status/progress calls
		 */
		public static final AlgorithmCallback getNullCallback() {
			return NullCallback.INSTANCE;
		}
		/**
		 * Construct an algorithm callback that redirects status reports to a print writer
		 * @param pw A print writer object to direct status reports to
		 * @return An algorithm callback
		 */
		public static final AlgorithmCallback getPrintWriterCallback(PrintWriter pw) {
			return new PrintWriterCallback(pw);
		}

		public static final AlgorithmCallback getSystemOutCallback() {
		  return new PrintWriterCallback(new PrintWriter(System.out));
		}
		/**
		 * @return an AlgorithmCallback object that is tied to the parent callback object such that
		 * setting the progress on the sub callback is translated to updating the progress on the parent
		 * callback but adjust to be between minProgress and maxProgress. Also any calls to updateStatus are
		 * altered to include a prefix.
		 *
		 */
		public static final AlgorithmCallback getSubCallback(AlgorithmCallback parent, String id, double minProgress, double maxProgress) {
			return new SubCallback(parent,id,minProgress,maxProgress);
		}
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - -- -  -- - - - - -
		private static final class NullCallback implements AlgorithmCallback {
			static final AlgorithmCallback INSTANCE = new NullCallback();
			public void updateStatus(String statusString) {}
			public boolean isPleaseStop() { return false; }
			public void updateProgress(double progress) {}
			public void clearProgress() {}
		}
		// - - - - - - -- - - -- - - - - -- - - - - - - -- - - - -- - - - - -- - -
		private static final class PrintWriterCallback implements AlgorithmCallback {
			private final PrintWriter pw_;
			public PrintWriterCallback(PrintWriter pw) {
				this.pw_ = pw;
			}
			public void updateStatus(String statusString) { pw_.println("Status:"+statusString); }
			public void log(Object logInfo){ pw_.println("Log:"+logInfo); }
			public void logNNL(Object logInfo){  pw_.print(logInfo);}
			public void debug(Object logInfo) {pw_.println("Debug:"+logInfo); }
			public boolean isPleaseStop() { return false; }
			public void updateProgress(double progress) {pw_.println("Progress:"+progress);}
			public void clearProgress() {pw_.println("Clear Progress"); }
		}
		// - - - - - - -- - - -- - - - - -- - - - - - - -- - - - -- - - - - -- - -
		private static final class SubCallback implements AlgorithmCallback {
			private final String id_;
			private final double minProgress_;
			private final double progressRange_;
			private final AlgorithmCallback parent_;
			public SubCallback(AlgorithmCallback parent, String id, double minProgress, double maxProgress) {
				this.id_ = id;
				this.minProgress_ = minProgress;
				this.progressRange_ = maxProgress - minProgress;
				this.parent_ = parent;
			}
			public void updateStatus(String statusString) {		parent_.updateStatus(id_+statusString);			}
			public boolean isPleaseStop() { return parent_.isPleaseStop(); }
			public void updateProgress(double progress) {
				if(progress>=0&&progress<=1) {
					parent_.updateProgress(progressRange_*progress+minProgress_);
				} else {
					System.out.println("Warning: strange usage of progress:"+progress);
					Thread.dumpStack();
				}
			}
			public void clearProgress() {	parent_.clearProgress(); }
		}
	}
}
