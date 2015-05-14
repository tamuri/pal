// Codons.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for all Codons (including terminators).
 * Characters are defined as
 * 'A-Z' followed by 'a-z' followed by '0-9' followed by '@' and '%'
 * Now incorporates the ungeneral code from GeneralizedCodons (which has died due to it's limited use)
 * @version $Id: Codons.java,v 1.21 2004/03/10 04:42:54 matt Exp $
 *
 * @author Matthew Goode
 * @author Alexei Drummond
 */
public class Codons extends SimpleDataType implements MolecularDataType
{
	public static final Codons DEFAULT_INSTANCE = new Codons();
	private static final Nucleotides NUCLEOTIDES = Nucleotides.DEFAULT_INSTANCE;
	private static final int CODON_LENGTH = 3;
	private static final int NUMBER_OF_NUCLEOTIDE_STATES = NUCLEOTIDES.getNumStates();

	private static final long serialVersionUID = -2779857947044354950L;

	private static final String[] TLA_NAMES_BY_STATE =
		{"AAA", "AAC", "AAG", "AAT", "ACA", "ACC", "ACG","ACT",
		 "AGA", "AGC", "AGG", "AGT", "ATA", "ATC", "ATG","ATT",
		 "CAA", "CAC", "CAG", "CAT", "CCA", "CCC", "CCG","CCT",
		 "CGA", "CGC", "CGG", "CGT", "CTA", "CTC", "CTG","CTT",
		 "GAA", "GAC", "GAG", "GAT", "GCA", "GCC", "GCG","GCT",
		 "GGA", "GGC", "GGG", "GGT", "GTA", "GTC", "GTG","GTT",
		 "TAA", "TAC", "TAG", "TAT", "TCA", "TCC", "TCG","TCT",
		 "TGA", "TGC", "TGG", "TGT", "TTA", "TTC", "TTG","TTT"};
	private static final char[] STATE_CHARS = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
		'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
		'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
		'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9',	'@', '%'
	};
	private static final int[] CHAR_STATES = new int[256];
	//Set up CHAR_STATES based on STATE_CHARS
	static {
		for(int i = 0; i < CHAR_STATES.length ; i++) {
			CHAR_STATES[i] = 64;
		}
		for(int i = 0 ; i < STATE_CHARS.length ; i++) {
			CHAR_STATES[(int)STATE_CHARS[i]] = i;
		}
	}

	// Get number of bases
	public int getNumStates()
	{
		return 64;
	}

	public int getStateImpl(char c)
	{
		int ic = (int)c;
		if(ic>=256) { return 64; }
		return CHAR_STATES[ic];
	}

	/**
	 * Get character corresponding to a given state
	 */
	protected char getCharImpl(final int state)
	{

		if(state>=64||state<0) {
			return UNKNOWN_CHARACTER;
		}
		return STATE_CHARS[state];
	}

	// String describing the data type
	public String getDescription()
	{
		return CODON_DESCRIPTION;
	}

	/**
	 * @retrun true if this state is an unknown state
	 */
	protected final boolean isUnknownStateImpl(final int state) {
		return(state>=64)||(state<0);
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return DataType.CODONS;
	}

//==========================================================
//================ MoleuclarDataType stuff ===================
//==========================================================

	/**
	 * @param residue states an array of states corresponding to states of <emph>this</emph> datatype
	 * @return the corresponding IUPAC states
	 */
	public int[] getNucleotideStates(int[] residueStates) {
		int[] result = new int[residueStates.length*3];
		int base = 0;
		for(int j = 0 ; j < residueStates.length ; j++) {
			int index = residueStates[j];
			if(index<0||index>=64) {
				result[base] = -1;	result[base+1] = -1;	result[base+2] = -1;
			} else{
				for(int i = 2 ; i >=0 ; i--) {
					result[base+i] = index%4;
					index/=4;
				}
			}
			base+=3;
		}
		return result;

	}

	/**
	 */
	public final int[] getMolecularStatesFromIUPACNucleotides(int[] nucleotideStates, int startingIndex) {
		return getMolecularStatesFromSimpleNucleotides(
			nucleotideStates,	startingIndex
		);
	}
	/**
	 * @return
	 */
	public final int[] getMolecularStatesFromSimpleNucleotides(int[] nucleotideStates, int startingIndex) {
		int[] result = new int[(nucleotideStates.length-startingIndex)/3];
		int base = startingIndex;
		for(int j = 0 ; j < result.length ; j++) {
			int index = 0;
			for(int i = 0 ; i < 3 ; i++) {
				index*=4;
				int state = nucleotideStates[base+i];
				if(NUCLEOTIDES.isUnknownState(state)) {
					index = -1;
					break;
				}
				index+=state;
			}
			result[j] = index;
			base+=3;
		}
		return result;
	}

	/**
	 * @return false
	 */
	public boolean isCreatesIUPACNuecleotides() {
		return false;
	}
	/**
	 * @return 3
	 */
	public final int getNucleotideLength() {
		return 3;
	}
	//======================================================================
	//========================= Static Utility Methods =====================
	//======================================================================

	/**
	 * @return a three letter acronym for an AminoAcid, according to state
	 */
	public static final String getTLA(final int state) {
		if(state<0||state>=TLA_NAMES_BY_STATE.length) {
			return UNKNOWN_TLA;
		}
		return TLA_NAMES_BY_STATE[state];
	}
	/**
	 * The codon index is a number between 0 and 64 assigned to each different codon type
	 * @param codon a 3 element array of characters which contain Nucleotide characters
	 * @return -1 if the codon has unknowns, or gaps in it, or is length is less than 3
	 */
	public static final int getCodonIndexFromNucleotides(char[] codon) {
		if(codon.length<CODON_LENGTH) {
			return -1;
		}
		int index = 0;
		for(int i = 0 ; i < CODON_LENGTH ; i++) {
			index*=NUMBER_OF_NUCLEOTIDE_STATES;
			int state = NUCLEOTIDES.getState(codon[i]);
			if(NUCLEOTIDES.isUnknownState(state)) {	return -1; }
			index+=state;
		}
		return index;
	}

	/**
	 * The codon index is a number between 0 and 64 assigned to each different codon type
	 * @param codon a 3 element array of characters which contain Nucleotide states
	 * @return -1 if the codon has unknowns, or gaps in it, or is length is less than 3
	 */
	public static final int getCodonIndexFromNucleotideStates(int[] codon) {
		return getCodonIndexFromNucleotideStates(codon,0);
	}
	/**
	 * The codon index is a number between 0 and 64 assigned to each different codon type
	 * @param codon a 3 element array of characters which contain Nucleotide states
	 * @param startingPosition an offset into the array to start examining
	 * @return -1 if the codon has unknowns, or gaps in it, or is length is less than 3
	 */
	public static final int getCodonIndexFromNucleotideStates(int[] codon, int startingPosition) {
		if((codon.length-startingPosition)<CODON_LENGTH) {
			return -1;
		}
		int index = 0;
		for(int i = 0 ; i < CODON_LENGTH ; i++) {
			index*=NUMBER_OF_NUCLEOTIDE_STATES;
			int state = codon[i+startingPosition];
			if(NUCLEOTIDES.isUnknownState(state)) {
				return -1;
			}
			index+=state;
		}
		return index;
	}
	/**
	 * The codon index is a number between 0 and 64 assigned to each different codon type
	 * @param codon a 3 element array of characters which contain Nucleotide states
	 * @param startingPosition an offset into the array to start examining
	 * @return -1 if the codon has unknowns, or gaps in it, or is length is less than 3
	 */
	public static final int getCodonIndexFromIUPACNucleotideStates(int[] codon) {
		return getCodonIndexFromIUPACNucleotideStates(codon,0);
	}
	/**
	 * The codon index is a number between 0 and 64 assigned to each different codon type
	 * @param codon a 3 element array of characters which contain Nucleotide states
	 * @param startingPosition an offset into the array to start examining
	 * @return -1 if the codon has unknowns, or gaps in it, or is length is less than 3
	 */
	public static final int getCodonIndexFromIUPACNucleotideStates(int[] codon, int startingPosition) {
		if((codon.length-startingPosition)<CODON_LENGTH) {
			return -1;
		}
		int index = 0;
		for(int i = 0 ; i < CODON_LENGTH ; i++) {
			index*=NUMBER_OF_NUCLEOTIDE_STATES;
			int state = codon[i+startingPosition];
			state = IUPACNucleotides.getSimpleState(state);
			if(NUCLEOTIDES.isUnknownState(state)) {
				return -1;
			}
			index+=state;
		}
		return index;
	}

	/**
	 * Translates an index into a codon
	 * @param index the codon index
	 * @return a char array contain 3 nucleotide characters
	 */
	public static final char[] getNucleotidesFromCodonIndex(int index) {
		if(index<0||index>=64) {
		  return new char[] { Nucleotides.UNKNOWN_CHARACTER, Nucleotides.UNKNOWN_CHARACTER, Nucleotides.UNKNOWN_CHARACTER };
		}
		char[] cs = new char[CODON_LENGTH];
		for(int i = CODON_LENGTH-1 ; i >=0 ; i--) {
			cs[i] = NUCLEOTIDES.getChar(index%NUMBER_OF_NUCLEOTIDE_STATES);
			index/=NUMBER_OF_NUCLEOTIDE_STATES;
		}
		return cs;
	}
	/**
	 * Translates an index into a codon
	 * @param index the codon index
	 * @return an int array contain 3 nucleotide states
	 */
	public static final int[] getNucleotideStatesFromCodonIndex(int codonIndex) {
		if(codonIndex<0) {
		  return new int[] { -1, -1, -1 };
		}
		if(codonIndex>=64) {
	    return new int[] { 4, 4, 4 };
		}

		int[] cs = new int[CODON_LENGTH];
		for(int i = CODON_LENGTH-1 ; i >=0 ; i--) {
			cs[i] = codonIndex%NUMBER_OF_NUCLEOTIDE_STATES;
			codonIndex/=NUMBER_OF_NUCLEOTIDE_STATES;
		}
		return cs;
	}
	public final static double[] getF1X4CodonFrequencies(double[] nucleotideFrequencies) {
		final double[] cfs = new double[64];
		for(int i = 0  ;  i < 64 ; i++) {
			double freq = 1;
			int index = i;
			for(int j = 0 ; j < 3 ; j++) {
				freq *= nucleotideFrequencies[index%NUMBER_OF_NUCLEOTIDE_STATES];
				index /= NUMBER_OF_NUCLEOTIDE_STATES;
			}
			cfs[i] = freq;
		}
		return cfs;
	}
	public final static double[] getF3X4CodonFrequencies(double[][] nucleotideTripletFrequencies) {
	  final double[] cfs = new double[64];
		for(int i = 0  ;  i < 64 ; i++) {
			double freq = 1;
			int index = i;
			for(int j = 0 ; j < 3 ; j++) {
				freq *= nucleotideTripletFrequencies[j][index%NUMBER_OF_NUCLEOTIDE_STATES];
				index /= NUMBER_OF_NUCLEOTIDE_STATES;
			}
			cfs[i] = freq;
		}
		return cfs;
	}

}
