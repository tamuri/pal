// SimpleDataType.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


// Known bugs and limitations:
// - all states must have a non-negative value 0..getNumStates()-1
// - ? (unknown state) has value getNumStates()


package pal.datatype;

import java.io.Serializable;

/**
 * interface for sequence data types
 *
 * @version $Id: SimpleDataType.java,v 1.14 2003/11/30 05:29:22 matt Exp $
 *
 * @author Alexei Drummond
 */
public abstract class SimpleDataType implements DataType
{

	private static final long serialVersionUID=7902613264354545217L;

	//serialver -classpath ./classes pal.datatype.SimpleDataType

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				break;
			}
		}
	}

	/**
	 * Handles gap char and then passes on to getStateImpl
	 */
	public final int getState(char c) {
		if(DataType.Utils.isSuggestedGap(c)) { return SUGGESTED_GAP_STATE; }
		return getStateImpl(c);
	}
	/**
	 * Handles gap state and then passes on to getStateImpl
	 */
	public final char getChar(final int state) {
		if(state==SUGGESTED_GAP_STATE) { return PRIMARY_SUGGESTED_GAP_CHARACTER; }
		if(state<0) { return UNKNOWN_CHARACTER; }
		return getCharImpl(state);
	}
	/**
	 * For sub classes to implement main functionality of getState. Gaps
	 * do not need to be considered
	 */
	abstract protected int getStateImpl(char c);

	abstract protected char getCharImpl(int state);

	/**
	 * Automatically handles Gaps for sub classes
	 */
	public final char getPreferredChar(final char c) {
		if(isGapChar(c)) {
			return PRIMARY_SUGGESTED_GAP_CHARACTER;
		}
		return getPreferredCharImpl(c);
	}

	/**
	 * Can be overidden by subclasses. Default implementation
	 * get's character's state and that get's the character for that state
	 */
	protected char getPreferredCharImpl(final char c) {
		return getChar(getState(c));
	}


	/**
	 * @return true if this state is unknown (or a gap)
	 */
	public final boolean isUnknownChar(final char c) {
		return isUnknownState(getState(c));
	}
		/**
	 * Checks if state is a gap state (then returns true), otherwise passes on
	 * to isUnknownStateImpl
	 * @retrun true if this state is an unknown state
	 *
	 */
	public final boolean isUnknownState(final int state) {
		return(state==SUGGESTED_GAP_STATE||isUnknownStateImpl(state));
	}
	/**
	 * For subclasses to handle, without regard for gaps
	 */
	abstract protected boolean isUnknownStateImpl(int state);
	public String toString() { return getDescription(); }

	/**
	 * @return -1 (not getNumStates())
	 */
	public int getRecommendedUnknownState() { return SUGGESTED_UNKNOWN_STATE; }

	/**
	 * @return false
	 */
	public boolean isAmbiguous() { return false; }
	/**
	 * @return null
	 */
	public AmbiguousDataType getAmbiguousVersion() { return null; }

// ========== Gap Stuff =========

	/**
	 * @return true
	 */
	public final boolean hasGap() { return true; }
	/**
	 * @return true if this character is a '.' or a '_'
	 */
	public final boolean isGapChar(final char c) {
		return DataType.Utils.isSuggestedGap(c);
	}

	/**
	 * @return true if state is gap state (-2), false other wise
	 */
	public final boolean isGapState(final int state) { return state==SUGGESTED_GAP_STATE; }

	/**
	 * @return GAP_STATE (-2)
	 */
	public final int getRecommendedGapState() { return SUGGESTED_GAP_STATE; }

}
