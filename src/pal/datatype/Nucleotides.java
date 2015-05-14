// NucleotideData.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for nucleotides
 *
 * @version $Id: Nucleotides.java,v 1.20 2004/09/13 05:20:47 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public class Nucleotides extends SimpleDataType implements MolecularDataType {

	public static final int A_STATE = 0;
	public static final int C_STATE = 1;
	public static final int G_STATE = 2;
	public static final int UT_STATE = 3;

	private static final long serialVersionUID=-497943046234232204L;

	//
	// Variables
	//

	private static final char[] DNA_CONVERSION_TABLE = {'A', 'C', 'G', 'T', UNKNOWN_CHARACTER};
	private static final char[] RNA_CONVERSION_TABLE = {'A', 'C', 'G', 'T', UNKNOWN_CHARACTER};

	//For faster conversion!
	boolean isRNA_;
	char[] conversionTable_;

	//Must stay after static CONVERSION_TABLE stuff!
	public static final Nucleotides DEFAULT_INSTANCE = new Nucleotides();
	//
	// Serialization code
	//

	//serialver -classpath ./classes cebl.ceblet.AbstractCeblet
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeBoolean(isRNA_);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				 isRNA_ = in.readBoolean();
				 conversionTable_ = (isRNA_ ? RNA_CONVERSION_TABLE : DNA_CONVERSION_TABLE );
				 break;
			}
		}
	}


	public Nucleotides() {
		this(false);
	}

	/** If isRNA is true than getChar(state) will return a U instead of a T */
	public Nucleotides(boolean isRNA) {
		this.isRNA_ = isRNA;
		conversionTable_ = (isRNA_ ? RNA_CONVERSION_TABLE : DNA_CONVERSION_TABLE );
	}

	// Get number of bases
	public int getNumStates()
	{
		return 4;
	}

	/**
	 * @return true if this state is an unknown state
	 */
	protected final boolean isUnknownStateImpl(final int state) {
		return(state>=4)||(state<0);
	}

	/**
	 * Get state corresponding to character c <BR>
	 * <B>NOTE</B>: IF YOU CHANGE THIS IT MAY STOP THE NUCLEOTIDE TRANSLATOR FROM WORKING!
	 * - It relies on the fact that all the states for 'ACGTU' are between [0, 3]
	 */
	protected int getStateImpl(char c)
	{
		switch (c)
		{
			case 'A':
				return A_STATE;
			case 'C':
				return C_STATE;
			case 'G':
				return G_STATE;
			case 'T':
				return UT_STATE;
			case 'U':
				return UT_STATE;
			case UNKNOWN_CHARACTER:
				return 4;
			case 'a':
				return A_STATE;
			case 'c':
				return C_STATE;
			case 'g':
				return G_STATE;
			case 't':
				return UT_STATE;
			case 'u':
				return UT_STATE;
			default:
				return 4;
		}
	}

	/**
	 * Get character corresponding to a given state
	 */
	protected char getCharImpl(final int state) {
		if(state<conversionTable_.length&&state>=0){
			return conversionTable_[state];
		}
		return UNKNOWN_CHARACTER;
	}

	/**
	 * @return a string describing the data type
	 */
	public String getDescription()	{
		return NUCLEOTIDE_DESCRIPTION;
	}

	/**
	 * @return the unique numerical code describing the data type
	 */
	public int getTypeID() {
		return 0;
	}

	/**
	 * @return true if A->G, G->A, C->T, or T->C
	 * if firstState equals secondState returns FALSE!
	 */
	public final boolean isTransitionByState(int firstState, int secondState) {
		switch(firstState) {
			case A_STATE: {
				if(secondState==G_STATE) {
					return true;
				}
				return false;
			}
			case C_STATE : {
				if(secondState==UT_STATE) {
					return true;
				}
				return false;
			}
			case G_STATE : {
				if(secondState==A_STATE) {
					return true;
				}
				return false;
			}
			case UT_STATE : {
				if(secondState==C_STATE) {
					return true;
				}
				return false;
			}
		}
		return false;
	}

	/**
	 * @return true if A->G, G->A, C->T, or T->C
	 * if firstState equals secondState returns FALSE!
	 * (I've renamed things to avoid confusion between java typing of ints and chars)
	*/
	public final boolean isTransitionByChar(char firstChar, char secondChar) {
		//I'm leaving it open to a possible optimisation if anyone cares.
		return isTransitionByState(getState(firstChar), getState(secondChar));
	}

//==========================================================
//================ ResidueDataType stuff ===================
//==========================================================

	/**
	 * @return a copy of the input
	 */
	public int[] getNucleotideStates(int[] residueStates) {
		return pal.misc.Utils.getCopy(residueStates);
	}

	/**
	 * @return the input
	 */
	public int getRelavantLength(int numberOfStates) {
		return numberOfStates;
	}

	/**
	 * @return a copy of the input
	 */
	public int[] getMolecularStatesFromSimpleNucleotides(int[] nucleotideStates, int startingIndex) {
		return pal.misc.Utils.getCopy(nucleotideStates,startingIndex);
	}
	/**
	 * @return a copy of the input
	 */
	public int[] getMolecularStatesFromIUPACNucleotides(int[] nucleotideStates, int startingIndex) {
		return pal.misc.Utils.getCopy(nucleotideStates, startingIndex);
	}

	/**
	 * @return false Nucleotide data will suffice
	 */
	public boolean isCreatesIUPACNuecleotides() {
		return false;
	}


	/**
	 * @return 1
	 */
	public final int getNucleotideLength() {
		return 1;
	}
// ====================================================================
// === Static utility methods
	/**
	 * Obtain the complement state
	 * @param baseState the base state to complement (may be IUPAC but IUPACness is lost)
	 * @return the complement state
	 */
	public static final int getComplementState(int baseState) {
		switch(baseState) {
		  case A_STATE : return UT_STATE;
			case UT_STATE : return A_STATE;
			case G_STATE : return C_STATE;
			case C_STATE : return G_STATE;
			default: return UNKNOWN;
		}
	}
	/**
	 * Obtain the complement of a sequence of nucleotides (or IUPACNucleotides - but IUPAC ness is lost)
	 * @param sequence the sequence (of nucleotide states)
	 * @return the complement
	 */
	public static final int[] getSequenceComplement(int[] sequence) {
		final int[] result  = new int[sequence.length];
		for(int i = 0 ; i < sequence.length ; i++) {
		  result[i] = getComplementState(sequence[i]);
		}
		return result;
	}

	/**
	 * Complement of a sequence of nucleotides (or IUPACNucleotides - but IUPAC ness is lost)
	 * @param sequence the sequence (of nucleotide states) (is modified)
	 */
	public static final void complementSequence(int[] sequence) {
		for(int i = 0 ; i < sequence.length ; i++) {
		  sequence[i] = getComplementState(sequence[i]);
		}
	}
}
