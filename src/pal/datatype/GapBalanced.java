// GapBalanced.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements a ResidueDataType for GapBalanced notation. The terminate character IS included as a state
 *
 * @version $Id: GapBalanced.java,v 1.6 2003/03/23 00:04:23 matt Exp $
 *
 * @author Matthew Goode
 */

public class GapBalanced extends SimpleDataType implements MolecularDataType {
	CodonTable translationTable_;
	public static final GapBalanced DEFAULT_INSTANCE = new GapBalanced(CodonTableFactory.createUniversalTranslator());

	public GapBalanced(CodonTable translationTable) {
		this.translationTable_ = translationTable;
	}

	//If you changed this becuase of error you should also changed AminoAcids
	static final char[] CONVERSION_TABLE=
		{'A','R','N','D','C','Q','E','G','H','I','L','K','M','F', 'P', 'S','T','W','Y','V','[',AminoAcids.TERMINATE_CHARACTER,']',UNKNOWN_CHARACTER};
	static final int LEFT_BRACKET_STATE = 20;
	static final int RIGHT_BRACKET_STATE = 22;


	// Get number of amino acids
	public int getNumStates()
	{
		return 23;
	}

	// Get state corresponding to character c
	public int getStateImpl(char c)
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
			case '[':
				return LEFT_BRACKET_STATE;
			case ']':
				return RIGHT_BRACKET_STATE;
			case AminoAcids.TERMINATE_CHARACTER:
				return AminoAcids.TERMINATE_STATE; //Terminate
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
			default:
				return -1;
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
	protected final boolean isUnknownStateImpl(final int state) {
		return(state>=22||state<0);
	}

	// String describing the data type
	public String getDescription()
	{
		return GAP_BALANCED_DESCRIPTION;
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return GAP_BALANCED;
	}


//==========================================================
//================ MolecularDataType stuff ===================
//==========================================================

	/**
	 *
	 */
	public int[] getNucleotideStates(int[] residueStates) {
		throw new RuntimeException("NOT IMPLEMENTED YET!");
	}

	/**
	 * Currently converts ambiguous states to a simple version (IE takes the first possible simple state for the ambiguous state)
	 */
	public final int[] getMolecularStatesFromIUPACNucleotides(int[] nucleotideStates, int startingIndex) {
		return getMolecularStatesFromSimpleNucleotides(
			IUPACNucleotides.getSimpleStates(nucleotideStates),
			startingIndex
		);
	}
	/**
	 * @return
	 */
	public final int[] getMolecularStatesFromSimpleNucleotides(int[] nucleotideStates, int startingIndex) {
		int[] residueStates = new int[nucleotideStates.length-startingIndex];
		int codonIndex = 0;
		int codonCount = 0;
		int placementIndex = -1;
		for(int i = 0 ; i < residueStates.length ; i++) {
			int n = nucleotideStates[i+startingIndex];
			if(n<0||n>3) {
				residueStates[i] = -1;
			} else {
				codonIndex=codonIndex*4+n;
				switch(codonCount) {
					case 0 : {
						residueStates[i] = LEFT_BRACKET_STATE;
						codonCount =1;
						break;
					}
					case 1 : {
						placementIndex = i;
						codonCount = 2;
						break;

					}
					case 2 : {
						residueStates[i] = RIGHT_BRACKET_STATE;
						residueStates[placementIndex] =  translationTable_.getAminoAcidStateFromCodonIndex(codonIndex);
						codonIndex = 0;
						codonCount = 0;
						break;
					}
				}
			}
		}
		if(codonIndex!=2) {
			residueStates[placementIndex] = -1;
		}
		return residueStates;
	}

	/**
	 * @return true
	 */
	public boolean isCreatesIUPACNuecleotides() {
		return true;
	}

	/**
	 * @return 1
	 */
	public final int getNucleotideLength() {
		return 1;
	}
}