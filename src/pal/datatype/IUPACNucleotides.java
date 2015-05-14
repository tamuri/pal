// IUPACNucleotides.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements DataType for nucleotides with ambiguous characters
 *
 * @version $Id: IUPACNucleotides.java,v 1.19 2004/09/13 05:20:47 matt Exp $
 *
 * @author Alexei Drummond
 */
public class IUPACNucleotides extends SimpleDataType implements MolecularDataType, AmbiguousDataType
{
	/*
		0 - A (A), 1 - C (C), 2 - G (G), 3 -  T (T)
		4 - GT (K), 5 - AC (M), 6 - AG (R), 7 -  CG (S),
		8 - AT (W), 9 - CT (Y), 10 CGT (B), 11 AGT (D),
		12 -  ACT (H), 13 - AGT (V), 14 - ACGT (N), 15 - UNKNOWN */



	private static final char[] RNA_CONVERSION_TABLE= { 'A','C','G','U','K','M','R','S','W','Y','B','D','H','V','N',UNKNOWN_CHARACTER};
	private static final char[] DNA_CONVERSION_TABLE= { 'A','C','G','T','K','M','R','S','W','Y','B','D','H','V','N',UNKNOWN_CHARACTER};


	//Must stay after static CONVERSION_TABLE stuff!
	public static final IUPACNucleotides DEFAULT_INSTANCE = new IUPACNucleotides();
	public static final IUPACNucleotides DNA_INSTANCE = new IUPACNucleotides(false);
	public static final IUPACNucleotides RNA_INSTANCE = new IUPACNucleotides(true);


	boolean isRNA_;
	char[] conversionTable_;

	//
	// Serialization Stuff
	//
	private static final long serialVersionUID=8863411606027017687L;

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

	public IUPACNucleotides() {
		this(false);
	}
	public IUPACNucleotides(boolean isRNA) {
		this.isRNA_ = isRNA;
		conversionTable_ = (isRNA_ ? RNA_CONVERSION_TABLE : DNA_CONVERSION_TABLE );
	}
	/**
	 * Get number of states.
	 */
	public int getNumStates()
	{
		return 15;
	}

	/**
	 * @retrun true if this state is an unknown state
	 */
	protected boolean isUnknownStateImpl(final int state) {
		return((state>=getNumStates())||(state<0));
	}

	// Get state corresponding to character c
	protected final int getStateImpl(final char c) {
		switch (c) {
			case 'A': return  0;
			case 'C': return  1;
			case 'G': return  2;
			case 'T': return  3;
			case 'U': return  3;
			case 'K': return  4;
			case 'M': return  5;
			case 'R': return  6;
			case 'S': return  7;
			case 'W': return  8;
			case 'Y': return  9;
			case 'B': return 10;
			case 'D': return 11;
			case 'H': return 12;
			case 'V': return 13;
			case 'N': return 14;

			case 'a': return  0;
			case 'c': return  1;
			case 'g': return  2;
			case 't': return  3;
			case 'u': return  3;
			case 'k': return  4;
			case 'm': return  5;
			case 'r': return  6;
			case 's': return  7;
			case 'w': return  8;
			case 'y': return  9;
			case 'b': return 10;
			case 'd': return 11;
			case 'h': return 12;
			case 'v': return 13;
			case 'n': return 14;
		}
		return SUGGESTED_UNKNOWN_STATE;
	}
	// Get character corresponding to a given state
	protected char getCharImpl(final int state) {
		if(state<conversionTable_.length&&state>=0){
			return conversionTable_[state];
		}
		return UNKNOWN_CHARACTER;
	}

	// String describing the data type
	public String getDescription()
	{
		return "IUPACNucleotide";
	}

	// Get numerical code describing the data type
	public int getTypeID()
	{
		return DataType.IUPACNUCLEOTIDES;
	}

//==========================================================
//================ Static Utility Methods ===================
//==========================================================
	private static final boolean[]
		A_STATE_COMPS = {true, false, false, false,
										 false, true, true, false,
										 true, false, false, true,
										 true, true, true, true};
	private static final boolean[]
		C_STATE_COMPS = {false, true, false, false,
										 false, true, false, true,
										 false, true, true, false,
										 true, true, true, true};
	private static final boolean[]
		G_STATE_COMPS = {false, false, true, false,
										 true, false, true, true,
										 false, false, true, true,
										 false, true, true, true};
	private static final boolean[]
		T_STATE_COMPS = {false, false, false, true,
										 true, false, false, false,
										 true,  true, true, true,
										 true, false, true, true};
	private static final boolean[][]
		ALL_STATE_COMPS = {
			A_STATE_COMPS, C_STATE_COMPS, G_STATE_COMPS, T_STATE_COMPS
		};
	private static final int[] IUPAC_CONV = {
		-1, //Nothing... eh?
		0 /* A */, 1 /* C */, 5 /* AC */, 2 /* G */,
		6 /* AG */, 7 /* CG */,13 /* AGT */, 3 /* T */,
		8 /* AT */, 9 /* CT */,12 /* ACT */ ,4 /* GT */,
		11 /* AGT */, 10 /* CGT */,14 /* ACGT */
	};
	private static final int[] SIMPLE_STATE_CONV = {
		0,1,2,3,
		2,0,0,1,
		0,1,1,0,
		0,0,0
	};

	private static final boolean[][] BOOLEAN_NUCLEOTIDE_STATE_AMBIGUITY;
	private static final double[][] DOUBLE_NUCLEOTIDE_STATE_AMBIGUITY;
	private static final int[][] SPECIFIC_STATE_LOOKUP;

	static {
		Nucleotides n = Nucleotides.DEFAULT_INSTANCE;
		BOOLEAN_NUCLEOTIDE_STATE_AMBIGUITY = new boolean[15][n.getNumStates()];
		DOUBLE_NUCLEOTIDE_STATE_AMBIGUITY = new double[15][n.getNumStates()];
		SPECIFIC_STATE_LOOKUP = new int[15][];
		int s[][] = SPECIFIC_STATE_LOOKUP;
		double[][] d = DOUBLE_NUCLEOTIDE_STATE_AMBIGUITY;
		boolean[][] b = BOOLEAN_NUCLEOTIDE_STATE_AMBIGUITY;
		//Yes, I know this is pointless, but it just doesn't feel right not initialising things!
		for(int i = 0 ; i < d.length ; i++) {
			for(int j = 0 ; j < d[i].length ; j++) {	d[i][j] = 0; b[i][j] = false;	}
		}
		final int A = Nucleotides.A_STATE;	final int C = Nucleotides.C_STATE;
		final int G = Nucleotides.G_STATE;	final int T = Nucleotides.UT_STATE;
		/* 0, A */	d[0][A] = 1.0;	b[0][A] = true; s[0] = new int[] { A };
		/* 1, C */	d[1][C] = 1.0;	b[1][C] = true; s[1] = new int[] { C };
		/* 2, G */	d[2][G] = 1.0;	b[2][G] = true; s[2] = new int[] { G };
		/* 3, T */	d[3][T] = 1.0;	b[3][T] = true; s[3] = new int[] { T };
		/* 4, GT */
			d[4][G] = d[4][T] = 1.0;	b[4][G] = b[4][T] = true;
			s[4] = new int[] { G, T };
		/* 5, AC */
			d[5][A] = d[5][C] = 1.0;	b[5][A] = b[5][C] = true;
			s[5] = new int[] { A, C };
		/* 6, AG */
			d[6][A] = d[6][G] = 1.0;	b[6][A] = b[6][G] = true;
			s[6] = new int[] { A, G };
		/* 7, CG */
			d[7][C] = d[7][G] = 1.0;	b[7][C] = b[7][G] = true;
			s[7] = new int[] { C, G };
		/* 8, AT */
			d[8][A] = d[8][T] = 1.0;	b[8][A] = b[8][T] = true;
			s[8] = new int[] { A, T };
		/* 9, CT */
			d[9][C] = d[9][T] = 1.0;	b[9][C] = b[9][T] = true;
			s[9] = new int[] { C, T };
		/* 10, CGT */
			d[10][C] = d[10][G] = d[10][T] = 1.0;	b[10][C] = b[10][G] = b[10][T] = true;
			s[10] = new int[] { C, G, T };
		/* 11, AGT */
			d[11][A] = d[11][G] = d[11][T] = 1.0;	b[11][A] = b[11][G] = b[11][T] = true;
			s[11] = new int[] { A, G, T };
		/* 12, ACT */
			d[12][A] = d[12][C] = d[12][T] = 1.0;	b[12][A] = b[12][C] = b[12][T] = true;
			s[12] = new int[] { A, C, T };
		/* 13, ACG */
			d[13][A] = d[13][C] = d[13][G] = 1.0;	b[13][A] = b[13][C] = b[13][G] = true;
			s[13] = new int[] { A, C, G };
		/* 14, ACGT */
			d[14][A] = d[14][C] = d[14][G] = d[14][T] = 1.0;
			b[14][A] = b[14][C] = b[14][G] = b[14][T] = true;
			s[14] = new int[] { A, C, G, T };

	}

	/**
	 * @return true if the iupacState is an state which includes the
	 * possibility of being of a nucleotide state
	 */
	public static final boolean isNucleotideState(int iupacState, int nucleotideState) {
		return ALL_STATE_COMPS[nucleotideState][iupacState];
	}
	public int getRecommendedUnknownState() { return -1; }
	/**
	 * @param inclusion should be a number constructed as follows
	 * 1. start at zero
	 * 2. if maybe A add 1
	 * 3. if maybe C add 2
	 * 4. if maybe G add 4
	 * 5. if maybe T add 8
	 *
	 */
	public static final int getIUPACState(int inclusion) {
		return IUPAC_CONV[inclusion];
	}
	public static final int getIUPACState(boolean maybeA, boolean maybeC, boolean maybeG, boolean maybeT) {
		int index = 0;
		if(maybeA) { index+=1; }
		if(maybeC) { index+=2; }
		if(maybeG) { index+=4; }
		if(maybeT) { index+=8; }
		return IUPAC_CONV[index];
	}
	/**
	 * Converts an IUPAC State to either a A,T,G,C state (eg. if state represents either C or G, state becomes C - an arbitary choice is made to take "lowest" letter)
	 */
	public static final int getSimpleState(int state) {
		if(state<0||state>=SIMPLE_STATE_CONV.length) {
			return -1;
		}
		return SIMPLE_STATE_CONV[state];
	}
	/**
	 * Converts an IUPAC State array to either a A,T,G,C state (eg. if state represents either C or G, state becomes C - an arbitary choice is made to take "lowest" letter)
	 * @return new array containing only simple states (orignal is not altered)
	 */
	public static final int[] getSimpleStates(int[] states) {
		int[] newStates = new int[states.length];
		int state;
		for(int i = 0 ; i < states.length ; i++) {
			state = states[i];
			if(state<0||state>=SIMPLE_STATE_CONV.length) {
				newStates[i] =-1;
			} else {
				newStates[i] =SIMPLE_STATE_CONV[state];
			}
		}
		return newStates;
	}
	/**
	 * Converts an IUPAC State array to either a A,T,G,C state (eg. if state represents either C or G, state becomes C - an arbitary choice is made to take "lowest" letter)
	 * @param staringIndex amount to skip at beginning of input array
	 * @return new array containing only simple states (orignal is not altered)
	 */
	public static final int[] getSimpleStates(int[] states, int startingIndex) {
		int[] newStates = new int[states.length-startingIndex];
		int state;
		for(int i = 0 ; i < newStates.length ; i++) {
			state = states[i+startingIndex];
			if(state<0||state>=SIMPLE_STATE_CONV.length) {
				newStates[i] =-1;
			} else {
				newStates[i] =SIMPLE_STATE_CONV[state];
			}
		}
		return newStates;
	}
	public String toString() { return getDescription(); }

//==========================================================
//================ MolecularDataType stuff ===================
//==========================================================

	/**
	 * @return a copy of the input
	 */
	public int[] getNucleotideStates(int[] residueStates) {
		return pal.misc.Utils.getCopy(residueStates);
	}


	/**
	 * @return a copy of the input
	 */
	public int[] getMolecularStatesFromSimpleNucleotides(int[] nucleotideStates, int startingIndex) {
		return getSimpleStates(nucleotideStates, startingIndex);
	}
	/**
	 * @return a copy of the input
	 */
	public final int[] getMolecularStatesFromIUPACNucleotides(int[] nucleotideStates, int startingIndex) {
		return pal.misc.Utils.getCopy(nucleotideStates,startingIndex);
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

// ============================================================================
// ====== Ambiguity Stuff
	/**
	 * returns an array containing the non-ambiguous states
	 * that this state represents.
	 */
	public int[] getSpecificStates(final int state) {
		return SPECIFIC_STATE_LOOKUP[state];
	}
	/**
	 * @return true
	 */
	public boolean isAmbiguous() { return true; }

	/**
	 * @return this!
	 */
	public AmbiguousDataType getAmbiguousVersion() { return this; }

		/**
	 * @return Nucleotides
	 */
	public DataType getSpecificDataType() { return Nucleotides.DEFAULT_INSTANCE; }

	/**
	 * Attempts to "resolve" the ambiguity in a state with regard to the specific data type.
	 * @param ambiguousState the state of this data type (the ambiguous one!)
	 * @param specificInclusion An array of length equal to or greater than the number of states of
	 * the specific DataType. Each state of the specific data type is represented by the corresponding
	 * element in this array. The result of this method will be to set the states that the ambiguous state cannot
	 * represent to false, and those states that the ambiguous state might represent to true.
	 */
	public void getAmbiguity(int ambiguousState, boolean[] specificInclusion) {
		System.arraycopy(BOOLEAN_NUCLEOTIDE_STATE_AMBIGUITY[ambiguousState],0,specificInclusion,0,4);
	}
	/**
	 * A more accurate attempt to "resolve" the ambiguity in a state with regard to the specific data type.
	 * @param ambiguousState the state of this data type (the ambiguous one!)
	 * @param specificInclusion An array of length equal to or greater than the number of states of
	 * the specific DataType. Each state of the specific data type is represented by the corresponding
	 * element in this array. The result of this method will be to set the states that the ambiguous state cannot
	 * represent to zero, and those states that the ambiguous state might represent to a value representing the frequency that the ambiguous state is actually that specific state. In general this should be one for
	 * each specific state covered by the ambiguous state (result should be suitable for use in likelihood calculations).
	 */
	public void getAmbiguity(int ambiguousState, double[] specificInclusion) {
		System.arraycopy(DOUBLE_NUCLEOTIDE_STATE_AMBIGUITY[ambiguousState],0,specificInclusion,0,4);
	}
}
