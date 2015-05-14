// AminoAcids.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for amino acids
 *
 * @version $Id: AminoAcids.java,v 1.19 2003/03/23 00:04:23 matt Exp $
 *
 * @note The Terminate state is not part of the "true" states of this DataType.
 * It exists for terms of translating but is regarded as a unknown character
 * in general. (but using getChar('*') will not return getNumStates(),
 * but isUnknown() will classify it as unknown)
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 * @author Matthew Goode
 */
public class AminoAcids extends SimpleDataType
{

	private static final long serialVersionUID=-466623855742195043L;

	public static final AminoAcids DEFAULT_INSTANCE = new AminoAcids();

	public static final char TERMINATE_CHARACTER = '*';
	public static final int TERMINATE_STATE = 21;
	//If you changed this because you should also changed GapBalanced
	static final char[] CONVERSION_TABLE=
		{'A','R','N','D','C','Q','E','G','H','I','L','K','M','F', 'P', 'S','T','W','Y','V',UNKNOWN_CHARACTER,TERMINATE_CHARACTER};
	private static final String TERMINATE_TLA = ""+TERMINATE_CHARACTER+TERMINATE_CHARACTER+TERMINATE_CHARACTER;
	private static final String[] TLA_NAMES_BY_STATE =
		{"ALA", "ARG", "ASN", "ASP", "CYS", "GLN", "GLU","GLY",
		 "HIS", "ILE", "LEU", "LYS", "MET", "PHE", "PRO", "SER",
		 "THR", "TRP", "TYR", "VAL", UNKNOWN_TLA, TERMINATE_TLA};

	// Get number of amino acids
	public int getNumStates()
	{
		return 20;
	}

	// Get state corresponding to character c
	protected int getStateImpl(char c)
	{
		switch (c)
		{
			case 'A':
				return 0;
			case 'C':
				return 4;
			case 'D':
				return 3;
			case 'E':
				return 6;
			case 'F':
				return 13;
			case 'G':
				return 7;
			case 'H':
				return 8;
			case 'I':
				return 9;
			case 'K':
				return 11;
			case 'L':
				return 10;
			case 'M':
				return 12;
			case 'N':
				return 2;
			case 'P':
				return 14;
			case 'Q':
				return 5;
			case 'R':
				return 1;
			case 'S':
				return 15;
			case 'T':
				return 16;
			case 'V':
				return 19;
			case 'W':
				return 17;
			case 'Y':
				return 18;
			case UNKNOWN_CHARACTER:
				return 20;
			case TERMINATE_CHARACTER:
				return 21; //Terminate
			case 'a':
				return 0;
			case 'c':
				return 4;
			case 'd':
				return 3;
			case 'e':
				return 6;
			case 'f':
				return 13;
			case 'g':
				return 7;
			case 'h':
				return 8;
			case 'i':
				return 9;
			case 'k':
				return 11;
			case 'l':
				return 10;
			case 'm':
				return 12;
			case 'n':
				return 2;
			case 'p':
				return 14;
			case 'q':
				return 5;
			case 'r':
				return 1;
			case 's':
				return 15;
			case 't':
				return 16;
			case 'v':
				return 19;
			case 'w':
				return 17;
			case 'y':
				return 18;
			default:
				return 20;
		}
	}


	// Get character corresponding to a given state
	protected char getCharImpl(final int state)
	{
		if(state<CONVERSION_TABLE.length&&state>=0){
			return CONVERSION_TABLE[state];
		}
		return UNKNOWN_CHARACTER;
	}

	/**
	 * @retrun true if this state is an unknown state
	 */
	protected boolean isUnknownStateImpl(final int state) {
		return(state>=20||state<0);
	}

	// String describing the data type
	public String getDescription()
	{
		return AMINO_ACID_DESCRIPTION;
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return 1;
	}

//======================================================================
//========================= Static Utility Methods =====================
//======================================================================

	/**
	 * @return a three letter acronym for an AminoAcid, according to state
	 */
	public static final String getTLA(int state) {
		if(state<0||state>=TLA_NAMES_BY_STATE.length) {
			return UNKNOWN_TLA;
		}
		return TLA_NAMES_BY_STATE[state];
	}
}
