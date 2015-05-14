 // DataType.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


// Known bugs and limitations:
// - all states must have a non-negative value 0..getNumStates()-1
// - ? (unknown state) has value getNumStates()


package pal.datatype;

import java.io.*;

/**
 * interface for sequence data types
 * History: 21 March 2003, Added gap stuff, to counter frustration and not being
 * able to differentiat unknowns from gaps. Gap characters should still be treated
 * as unknowns (for compatibility), but a data type should be able to identify
 * a gap from other unknowns.
 *
 * @version $Id: DataType.java,v 1.24 2004/10/14 02:01:43 matt Exp $
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 */
public interface DataType extends Serializable
{
	//
	// Public stuff
	//

	char UNKNOWN_CHARACTER = '?';
	String UNKNOWN_TLA = "???";

	/**
	 * A suggested Gap character
	 */
	char PRIMARY_SUGGESTED_GAP_CHARACTER = '-';

	char[] SUGGESTED_GAP_CHARACTERS = {PRIMARY_SUGGESTED_GAP_CHARACTER, '_', '.'};

	/**
	 * The gap state that should generally be used (-2),
	 * though the DataType specification makes no requirement that this be the gap state
	 */
	int SUGGESTED_GAP_STATE = -2;

	/**
	 * The gap state that should generally be used (-1). Though in general, the unknown state is
	 * defined to be anystate that isn't a gap state or a normal state (which makes sense)
	 * though the DataType specification makes no requirement that this be the unknown state
	 */
	int SUGGESTED_UNKNOWN_STATE = -1;

	int NUCLEOTIDES = 0;
	int AMINOACIDS = 1;
	int TWOSTATES = 2;
	int IUPACNUCLEOTIDES = 3;
	int CODONS = 4;
	int GAP_BALANCED = 5;
	int NUMERIC = 6;
	int UNKNOWN = 100;

	/**
	 * Name of nucleotide data type. For XML and human reading of data type.
	 * You should do it yourself :-).
	 */
	String NUCLEOTIDE_DESCRIPTION = "nucleotide";

	/** amino acid name (for XML and human readability) */
	String AMINO_ACID_DESCRIPTION = "amino acid";

	/** two state name (for XML and human readability) */
	String TWO_STATE_DESCRIPTION = "binary";

	/** iupac nucleotide name (for XML and human readability) */
	String IUPAC_NUCELOTIDES_DESCRIPTION = "nucleotide";

	/** codon name (for XML and human readability) */
	String CODON_DESCRIPTION = "codon";

	/** generalized codon name (for XML and human readability) */
	String GAP_BALANCED_DESCRIPTION = "gap balanced";


	/**
	 * get number of unique states
	 *
	 * @return number of unique states
	 */
	int getNumStates();

	/**
	 * get state corresponding to a character
	 *
	 * @param c character
	 *
	 * @return state
	 */
	int getState(char c);

	/**
	 * get character corresponding to a given state
	 *
	 * @param state state
	 *
	 * return corresponding character
	 */
	char getChar(int state);

	/**
	 * get the preferred version of a particular character (eg a -> A)
	 * Should not always assume that a DataType only uses Upper case characters!
	 */
	char getPreferredChar(char c);

	/**
	 * description of data type
	 *
	 * @return string describing the data type
	 */
	String getDescription();

	/**
	 * get numerical code describing the data type
	 *
	 * @return numerical code
	 */
	int getTypeID();

	/**
	 * @return true if this state is an unknown state
	 * (the same as check if a state is >= the number of states... but neater)
	 */
	boolean isUnknownState(int state);
	/**
	 * @return true if this character is a gap
	 */
	boolean isUnknownChar(char c);

	int getRecommendedUnknownState();

	/**
	 * @return true if this data type supports having a gap character
	 */
	boolean hasGap();
	/**
	 * @return true if this data type interprets c as a gap
	 */
	boolean isGapChar(char c);

	/**
	 * @return true if this data type interprets state as a gap state
	 */
	boolean isGapState(int state);
	/**
	 * @return the recommended state to use as a gap
	 */
	int getRecommendedGapState();

	boolean isAmbiguous();

	AmbiguousDataType getAmbiguousVersion();
// ============================================================================
// ============= Utils ========================================================
	/**
	 * Some useful methods for implmenting classes and for DataType users
	 */
	public static final class Utils {
		/**
		 * Useful for implementing classes to check if a character is a suggest gap character
		 * Users of datatypes should query the datatype to see if a character is a gap - not
		 * use this method.
		 */
		public static final boolean isSuggestedGap(char c) {
			for(int i = 0 ; i < SUGGESTED_GAP_CHARACTERS.length ; i++) {
				if(c==SUGGESTED_GAP_CHARACTERS[i]) { return true; }
			}
			return false;
		}

		/**
		 * create object according to this code
		 *
		 * @param typeID selected data type
		 *
		 * @return DataType object
		 */
		public static DataType getInstance(int typeID)
		{
			switch(typeID) {
				case DataType.NUCLEOTIDES : {	  return Nucleotides.DEFAULT_INSTANCE;	  }
				case DataType.AMINOACIDS : {  return AminoAcids.DEFAULT_INSTANCE;	  }
				case DataType.TWOSTATES : {	  return TwoStates.DEFAULT_INSTANCE;		}
				case DataType.IUPACNUCLEOTIDES : {  return IUPACNucleotides.DEFAULT_INSTANCE;	  }
				case DataType.CODONS : {	return Codons.DEFAULT_INSTANCE;		}
				case DataType.GAP_BALANCED : {	  return GapBalanced.DEFAULT_INSTANCE;	  }
				case DataType.NUMERIC : {	  return NumericDataType.DEFAULT_INSTANCE;	  }
				default : {
					throw new IllegalArgumentException("typeID " + typeID + " is not recognised.");
				}
			}
		}

		/**
		 * @return true if the character represents a gap in the sequence.
		 * @deprecated use DataType.isGapChar()
		 */
		public final static boolean isGap(DataType d, char c) {
			return d.isGapChar(c);
		}
		/**
		 * Converts a sequence of characters to the preferred form for a data type
		 * @note does not treat the dot '.' specially
		 */
		public final static char[] getPreferredChars(final char[] sequence, final DataType dt) {
			return getPreferredChars(sequence,dt,false);
		}

		/**
		 * Converts a sequence of characters to the preferred form for a data type
		 * @param specialDots if true then the dot (period) '.' is used even if it is not the prefered character by the data type
		 */
		public final static char[] getPreferredChars(final char[] sequence, final DataType dt, boolean specialDots) {
			final char[] result = new char[sequence.length];
			for(int i = 0 ; i < result.length ; i++) {
				if(specialDots&&(sequence[i]=='.')) {
					result[i] = '.';
				} else {
					result[i] = dt.getPreferredChar(sequence[i]);
				}
			}
			return result;
		}
		/**
		 * Converts a sequence of characters to the preferred form for a data type (using Strings)
		 */
		public final static String getPreferredChars(final String sequence, final DataType dt) {
			return new String(getPreferredChars(sequence.toCharArray(),dt));
		}
		/**
		 * Converts a sequence of characters to the preferred form for a data type (using Strings)	 * @param specialDots if true then the dot (period) '.' is used even if it is not the prefered character by the data type
		 * @param specialDots if true then the dot (period) '.' is used even if it is not the prefered character by the data type
		 */
		public final static String getPreferredChars(final String sequence, final DataType dt, boolean specialDots) {
			return new String(getPreferredChars(sequence.toCharArray(),dt,specialDots));
		}

		/** For converting a sequence to an array of bytes where each byte represents the
		 *		state of the corresponding character in sequence
		 */
		public final static byte[] getByteStates(final String sequence , final DataType dt) {
			return getByteStates(sequence.toCharArray(),dt);
		}

		/** For converting a sequence to an array of bytes where each byte represents the
		 *		state of the corresponding character in sequence
		 */
		public final static byte[] getByteStates(final char[] sequence , final DataType dt) {
			final byte[] bs = new byte[sequence.length];
			for(int i = 0 ; i < bs.length ; i++) {
				bs[i] = (byte)dt.getState(sequence[i]);
			}
			return bs;
		}



		/** For converting an array of sequence to arrays of ints where each int represents the
		 *		state of the corresponding character in sequence
		 *    @param unknownState ensures that the state representation is set to this value (like -1)
		 */
		public final static int[][] getStates(final char[][] sequences, final int unknownState, final int gapState, final DataType dt) {
			final int[][] statesSeqs = new int[sequences.length][];
			for(int i = 0  ; i < statesSeqs.length ; i++) {
				statesSeqs[i] = getStates(sequences[i], unknownState,gapState, dt);
			}
			return statesSeqs;
		}
		/** For converting a sequence to an array of ints where each int represents the
		 *		state of the corresponding character in sequence
		 *  Allows user selection of unknown and gap states
		 */
		public final static int[] getStates(final char[] sequence, final int unknownState, int gapState, DataType dt) {
			int[] states= new int[sequence.length];
			for(int i = 0 ; i < states.length ; i++) {
				final int state = dt.getState(sequence[i]);
				if(dt.isGapState(state)) {
					states[i] = gapState;
				} else if(dt.isUnknownState(state)) {
					states[i] = unknownState;
				} else {
					states[i] = state;
				}
			}
			return states;
		}
		/** For converting an array of sequence to arrays of ints where each int represents the
		 *		state of the corresponding character in sequence
		 *   @note, used suggested gap and unknown states (from dt)
		 */
		public final static int[][] getStates(final char[][] sequences, final DataType dt) {
			final int[][] statesSeqs = new int[sequences.length][];
			for(int i = 0  ; i < statesSeqs.length ; i++) {
				statesSeqs[i] = getStates(sequences[i],dt);
			}
			return statesSeqs;
		}
		/** For converting a sequence to an array of ints where each int represents the
		 *		state of the corresponding character in sequence
		 *   @note, used suggested gap and unknown states (from dt)
		 */
		public final static int[] getStates(final char[] sequence, final DataType dt) {
			int[] states= new int[sequence.length];
			for(int i = 0 ; i < states.length ; i++) {
				states[i] = dt.getState(sequence[i]);
			}
			return states;
		}
		/** For converting an array of sequence to arrays of ints where each int represents the
		 *		state of the corresponding character in sequence
		 *    @param unknownChar The character uses for unknown states
		 *    @param gapChar the character to use for gap states (may be the same as the unknownChar)
		 */
		public final static char[][] getChars(final int[][] sequences, final char unknownChar, final char gapChar, final DataType dt) {
			final char[][] charSeqs = new char[sequences.length][];
			for(int i = 0  ; i < charSeqs.length ; i++) {
				charSeqs[i] = getChars(sequences[i], unknownChar, gapChar, dt);
			}
			return charSeqs;
		}
		/** For converting a sequence of ints representing states to an array of chars
		 *  @param unknownChar The character uses for unknown states
		 *  @param gapChar the character to use for gap states (may be the same as the unknownChar)
		 */
		public final static char[] getChars(final int[] sequence, final char unknownChar, final char gapChar, final DataType dt) {
			final char[] chars= new char[sequence.length];
			for(int i = 0 ; i < chars.length ; i++) {
				final int state = sequence[i];
				if(dt.isGapState(state)) {
					chars[i] = gapChar;
				}	else if(dt.isUnknownState(state)) {
					chars[i] = unknownChar;
				} else {
					chars[i] = dt.getChar(state);
				}
			}
			return chars;
		}
		/** For converting a sequence of ints representing states to an array of chars
		 * @note uses suggested characters, from dt
		 */
		public final static char[] getChars(final int[] sequence, final DataType dt) {
			final char[] chars= new char[sequence.length];
			for(int i = 0 ; i < chars.length ; i++) {
				chars[i] = dt.getChar(sequence[i]);
			}
			return chars;
		}

		/**
		 * For converting an array of states into a String of characters, based on a
		 * DataType
		 */
		public final static String toString(DataType dt, int[] states) {
			char[] chars = new char[states.length];
			for(int i = 0 ; i < states.length ; i++) {
				chars[i] = dt.getChar(states[i]);
			}
			return new String(chars);
		}
		/**
		 * Reverses an array of states
		 * @param sequence the sequence of states
		 */
		public static final void reverseSequence(int[] sequence) {
			final int midPoint = sequence.length/2;
			for(int i = 0 ; i < midPoint ; i++) {
				final int j = sequence.length-i-1;		final int temp = sequence[i];
				sequence[i] = sequence[j];	sequence[j] = temp;
			}
		}
		/**
		 * Realigns a sequence of states so that there are no gaps at the beggining (shifts to the left if necessary)
		 * @param sequence the base sequence
		 * @param dt the datatype of the sequence states
		 */
		public static final void leftAlignSequence(int[] sequence, DataType dt) {
			int startingIndex = -1;
			for(int i = 0 ; i < sequence.length ; i++) {
				final int state = sequence[i];
				if(!(dt.isUnknownState(state)||dt.isGapState(state))) {		startingIndex = i;	break;	}
			}
			int length = sequence.length-startingIndex;
			if(startingIndex>0) {
				//>0 is sufficient because 0 doesn't required anything
				System.arraycopy(sequence,startingIndex,sequence,0,length);
			}
			int gapState = dt.getRecommendedGapState();
			for(int i = length ; i< sequence.length ; i++) {
				sequence[i] = gapState;
			}
		}
	}
}
