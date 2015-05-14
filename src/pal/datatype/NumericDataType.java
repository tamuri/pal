// SimpleDataType.java
//
// (c) 1999-2000 PAL Development Core Team
//
// This package may be distributed under the
// terms of the GNU General Public License (GPL)


// Known bugs and limitations:
// - all states must have a non-negative value 0..getNumStates()-1
// - ? (unknown state) has value getNumStates()
package pal.datatype;

import java.io.Serializable;
import pal.datatype.*;

/**
 * This datatype stores numeric values.  These can be any 2 byte integer between 0-65536.
 * This can be used for SSR alleles or indel sizes.
 *
 *
 * @version $Id:
 *
 * @author Ed Buckler
 */
public class NumericDataType extends SimpleDataType
{
	public static final DataType DEFAULT_INSTANCE = new NumericDataType();

	int numberOfNumericStates=10000;

	public NumericDataType () { }

	public NumericDataType (int numberOfStates) {
		this.numberOfNumericStates = numberOfStates;
	}

				// Get number of bases
	public int getNumStates() {	return numberOfNumericStates;	}

	/**
	* @return true if this state is an unknown state
	*/
	protected boolean isUnknownStateImpl(int state) {
		return((state>=numberOfNumericStates)||(state<0));
	}

	/**
	 * get state corresponding to a character
	 * @param c character
	 * @return state
	 */
	protected int getStateImpl(char c)	{
		if(c==UNKNOWN_CHARACTER) {
			return numberOfNumericStates;
		}
		int state = getNumericIndexFromNumericChar(c);
		if(state<0||state>numberOfNumericStates) {
			state=numberOfNumericStates;
		}
		return state;
	}

	/**
	 * Get character corresponding to a given state
	 */
	protected char getCharImpl(final int state) {
		if(state>=numberOfNumericStates||state<0) {
			return UNKNOWN_CHARACTER;
		}
		return getNumericCharFromNumericIndex(state);
	}

	/**
	 * Returns a unique ascii character for any given numeric size
	 */
	public final char getNumericCharFromNumericIndex(int index) {
		return (char)(index + 64);
	}

	/**
	 * Returns numeric index (size) from a unique ascii character
	 */
	public final int getNumericIndexFromNumericChar(char c) {
		return (int)(c - 64);
	}

	/** String describing the data type */
	public String getDescription()	{
		return "Numeric";
	}

	/** Get numerical code describing the data type */
	public int getTypeID() {
		return NUMERIC;
	}
}
