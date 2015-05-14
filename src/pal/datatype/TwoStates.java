// TwoStateData.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for two-state data
 *
 * @version $Id: TwoStates.java,v 1.9 2003/03/23 00:04:23 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class TwoStates extends SimpleDataType
{
	public static final TwoStates DEFAULT_INSTANCE = new TwoStates();

	// Get number of bases
	public int getNumStates()
	{
		return 2;
	}

	// Get state corresponding to character c
	public int getStateImpl(char c)
	{
		switch (c)
		{
			case '0':
				return 0;
			case '1':
				return 1;

			case UNKNOWN_CHARACTER:
				return 2;

			default:
				return 2;
		}
	}

	/**
		* @retrun true if this state is an unknown state
		*/
	protected final boolean isUnknownStateImpl(final int state) {
		return(state>=2);
	}

	// Get character corresponding to a given state
	protected char getCharImpl(final int state)
	{
		switch (state)
		{
			case 0:
				return '0';
			case 1:
				return '1';

			case 2:
				return UNKNOWN_CHARACTER;

			default:
				return UNKNOWN_CHARACTER;
		}
	}

	// String describing the data type
	public String getDescription()
	{
		return TWO_STATE_DESCRIPTION;
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return 2;
	}
}
