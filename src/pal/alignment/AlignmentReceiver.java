// AlignmentReceiver.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

/**
 * <p>Title: AlignmentReceiver</p>
 * <p>Description: An interface for objects that receive alignments from some source. For use in callback situations.</p>
 * @author Matthew Goode
 * @version 1.0
 */
import java.util.*;
public interface AlignmentReceiver {
	public void newAlignment(Alignment a);

	/**
	 * The SingleReceiver only keeps track of one alignment, the last one it received
	 */
	public static final class SingleReceiver implements AlignmentReceiver {
		private Alignment lastReceivedAlignment_ = null;
		public void newAlignment(Alignment a) {
			this.lastReceivedAlignment_ = a;
		}
		/**
		 * @return last received alignment, or null if no alignments have been received
		 */
		public Alignment getLastReceivedAlignment() { return lastReceivedAlignment_; }
	}
	/**
	 * The SingleReceiver only keeps track of one alignment, the last one it received
	 */
	public static final class BucketReceiver implements AlignmentReceiver {
		private Vector receivedAlignments_ = new Vector();
		public void newAlignment(Alignment a) {
			receivedAlignments_.addElement(a);
		}
		public void clear() { receivedAlignments_.removeAllElements(); }
		/**
		 * @return last received alignment, or null if no alignments have been received
		 */
		public Alignment[] getReceivedAlignments() {
			Alignment[] as = new Alignment[receivedAlignments_.size()];
			receivedAlignments_.copyInto(as);
			return as;
		}
	}

}