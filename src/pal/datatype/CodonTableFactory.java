// CodonTableFactory.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * Generates CodonTables
 *
 * @author Matthew Goode
 * @version $Id: CodonTableFactory.java,v 1.14 2004/10/27 22:30:46 matt Exp $
 */


public class CodonTableFactory {



	/**
	 * Creates a translator of a given types
	 * @param type - UNIVERSAL, M_VERTEBRATE
	 */
	public static CodonTable createTranslator(int type) {
		UniversalTranslator ut = new UniversalTranslator();
		ut.setOrganismTypeID(type);
		switch(type) {
			case CodonTable.UNIVERSAL : {
				return ut;
			}
			case CodonTable.VERTEBRATE_MITOCHONDRIAL : {
				/* Tro */	ut.add("UGA", 'W');
				/* Ter */	ut.add("AGA", AminoAcids.TERMINATE_CHARACTER); ut.add("AGG", AminoAcids.TERMINATE_CHARACTER);
				/* Met */	ut.add("AUA", 'M');
				return ut;
			}
			case CodonTable.YEAST : {
				/* Thr */	ut.add("CUA", 'T'); ut.add("CUC", 'T'); ut.add("CUG", 'T'); 	ut.add("CUT", 'T');
				/* Trp */	ut.add("UGA", 'W');
				/* Met */ ut.add("AUA", 'M');
				return ut;
			}
			case CodonTable.MOLD_PROTOZOAN_MITOCHONDRIAL : {
				/* Trp */	ut.add("UGA", 'W');	return ut;
			}
			case CodonTable.MYCOPLASMA : {
				/* Trp */	ut.add("UGA", 'W');		return ut;
			}
			case CodonTable.INVERTEBRATE_MITOCHONDRIAL : {
				/* Ser */ ut.add("AGA", 'S'); ut.add("AGG", 'S');
				/* Trp */	ut.add("UGA", 'W');
				/* Met */	ut.add("AUA", 'M');
				return ut;
			}
			case CodonTable.CILATE : {
				/* Gln */	ut.add("UAA", 'Q'); ut.add("UAG", 'Q');
				return ut;
			}
			case CodonTable.ECHINODERM_MITOCHONDRIAL : {
				/* Trp */	ut.add("UGA", 'W');
				/* Ser */ ut.add("AGA", 'S'); ut.add("AGG", 'S');
				/* Asn */ ut.add("AAA", 'N');
				return ut;
			}
			case CodonTable.EUPLOTID_NUCLEAR : {
				/* Cys */	ut.add("UGA", 'C');	return ut;
			}
			case CodonTable.ASCIDIAN_MITOCHONDRIAL : {
				/* Met */	ut.add("AUA", 'M');
				/* Trp */	ut.add("UGA", 'W');
				/* Gly */	ut.add("AGA", 'G'); ut.add("AGG" , 'G');
				return ut;
			}
			case CodonTable.FLATWORM_MITOCHONDRIAL : {
				/* Asn */ ut.add("AAA", 'N');
				/* Tyr */ ut.add("UAA", 'Y');
				/* Trp */	ut.add("UGA", 'W');
				return ut;
			}
			case CodonTable.BLEPHARISMA_NUCLEAR : {
				/* Gln */	ut.add("UAG", 'Q');
				return ut;
			}
			case CodonTable.BACTERIAL : {
				return ut;
			}
			case CodonTable.ALTERNATIVE_YEAST : {
				/* Ser */ ut.add("CUG", 'S');
				return ut;
			}
			default : {
				ut.setOrganismTypeID(CodonTable.UNIVERSAL);
				return ut;
			}
		}
	}

	public static final CodonTable createUniversalTranslator() {
		return createTranslator(CodonTable.UNIVERSAL);
	}
	public static final void main(String[] args) {
		CodonTable universal = createUniversalTranslator();
		AminoAcids dt = new AminoAcids();
		for(int i = 0 ; i < 20 ; i++) {
			int[] codon = universal.getIUPACStatesFromAminoAcidState(i);
			System.out.println("AA:"+i+"  "+dt.getChar(i));
			System.out.println("Codon:"+codon[0]+" "+codon[1]+" "+codon[2]);
			int back = universal.getAminoAcidStateFromStates(codon);
			System.out.println("Back:"+back+"  "+dt.getChar(back)+" "+dt.getChar(i));
			char[][] codons = universal.getCodonsFromAminoAcidState(i);
			System.out.println("Codon Check");
			for(int c = 0 ; c < codons.length ; c++) {
				int s = universal.getAminoAcidState(codons[c]);
				System.out.println("C:"+c+":"+s+"  "+dt.getChar(s)+" "+dt.getChar(i));
			}
		}
	}
}

// ========================================================================


/**
 * A concrete implementation of a NucleotideTranslator for
 * the Standard/Universal set of codes.
 */
class UniversalTranslator implements CodonTable {
	int[] translations_ = new int[64];
	int[] terminatorIndexes_ = null;

	//IUPAC translation stuff, not initialised until required
	int[][] iupacCodons_ = null;
	private final int[] terminateCodon_ = new int[3];

	int[][] simpleCodons_ = null;

	private static final int[] UNKNOWN_CODON = {-1,-1,-1};
	private final DataType aminoAcids_ = AminoAcids.DEFAULT_INSTANCE;
	private final DataType nucleotides_ = Nucleotides.DEFAULT_INSTANCE;
	//GeneralizedCodons generalizedCodons_ = GeneralizedCodons.DEFAULT_INSTANCE;
	int organismTypeID_ = UNIVERSAL;

	private void setupIUPACCodon(int aminoAcidState, int[] codonStore) {
		codonStore[0] = 0;
		codonStore[1] = 0;
		codonStore[2] = 0;
		for(int i = 0 ; i < translations_.length ; i++) {
			if(translations_[i] == aminoAcidState) {
				int[] states = Codons.getNucleotideStatesFromCodonIndex(i);
				for(int j = 0 ; j < 3 ; j++) {
					switch(states[j]) {
						case 0: {	codonStore[j]|=1; break; }
						case 1: {	codonStore[j]|=2; break; }
						case 2: {	codonStore[j]|=4; break; }
						case 3: {	codonStore[j]|=8; break; }
					}
				}
			}
		}
		for(int i = 0 ; i < 3 ; i++) {
			codonStore[i] = IUPACNucleotides.getIUPACState(codonStore[i]);
		}
	}
	public int getOrganismTypeID() {		return organismTypeID_;	}
	protected void setOrganismTypeID(int id) { this.organismTypeID_ = id; }
	private final int[] createSimpleStates(int aminoAcidState) {
		for(int i = 0 ; i < translations_.length ; i++) {
			if(translations_[i] == aminoAcidState) {
				return Codons.getNucleotideStatesFromCodonIndex(i);
			}
		}
		throw new RuntimeException("Assertion error: I'm not ment to get here");
	}
	private void buildIUPACCodons() {
		iupacCodons_ = new int[20][3];
		for(int i = 0 ; i < 20 ; i++) {

			setupIUPACCodon(i,iupacCodons_[i]);
		}
		setupIUPACCodon(AminoAcids.TERMINATE_STATE, terminateCodon_);
	}
	private void buildSimpleCodons() {
		simpleCodons_ = new int[20][];
		for(int i = 0 ; i < 20 ; i++) {
			simpleCodons_[i] = createSimpleStates(i);
		}
	}

	private void addTerminalIndex(int index) {
		int[] ts = new int[terminatorIndexes_.length+1];

		for(int i = 0 ; i < terminatorIndexes_.length ; i++) {
			ts[i] = terminatorIndexes_[i];
		}
		ts[ts.length-1] = index;
		terminatorIndexes_ = ts;
	}

	/**
	 * state must already be in set!
	 */
	private void removeTerminalIndex(int index) {

		int[] ts = new int[terminatorIndexes_.length-1];
		int toAddIndex = 0;

		for(int i = 0 ; i < terminatorIndexes_.length ; i++) {
			if(index!=terminatorIndexes_[i]) {
				ts[toAddIndex] = terminatorIndexes_[i];
				toAddIndex++;
			}
		}
	}

	/*
	 * @return all the possible codons for a given amino acid
	 */
	public final char[][] getCodonsFromAminoAcidChar(char aminoAcidChar) {
		return getCodonsFromAminoAcidState(aminoAcids_.getState(aminoAcidChar));
	}

	/*
	 * @return all the possible codons for a given amino acid
	 */
	public final char[][] getCodonsFromAminoAcidState(int aminoAcid) {
		int count = 0;
		for(int i = 0 ; i < translations_.length ; i++) {
			if(translations_[i]==aminoAcid) {
				count++;
			}
		}
		char[][] results = new char[count][];
		count = 0;
		for(int i = 0 ; i < translations_.length; i++) {
			if(translations_[i]==aminoAcid) {
				results[count]=Codons.getNucleotidesFromCodonIndex(i);
				count++;
			}
		}
		return results;
	}
	/**
	 * @returns three IUPAC states representing the given amino acid
	 */
	public final int[] getIUPACStatesFromAminoAcidState(int aminoAcid) {
		if(iupacCodons_==null) {		buildIUPACCodons();		}
		if(aminoAcid == AminoAcids.TERMINATE_STATE) {
			return terminateCodon_;
		}
		if(aminoAcid<0||aminoAcid>=iupacCodons_.length) {
			return UNKNOWN_CODON;
		}
		return iupacCodons_[aminoAcid];
	}
	/**
		 * @returns three IUPAC states representing the given amino acid
		 */
	public final int[] getStatesFromAminoAcidState(int aminoAcid) {
		if(simpleCodons_==null) { buildSimpleCodons(); }
		if(aminoAcid == AminoAcids.TERMINATE_STATE) {
			return terminateCodon_;
		}
		if(aminoAcid<0||aminoAcid>=simpleCodons_.length) {
			return UNKNOWN_CODON;
		}
		return simpleCodons_[aminoAcid];
	}

	/** Add a codon/aminoAcid translation into table.
	 * Overwrites previous translation for that codon.
	 * If the codon is invalid will throw a runtime exception!
	 */
	protected void add(String codon, char aminoAcid) {
		int index = Codons.getCodonIndexFromNucleotides(codon.toCharArray());
		if(index<0) {
			throw new RuntimeException("Assertion error: Adding invalid Codon:"+codon);
		}
		if(aminoAcid!= AminoAcids.TERMINATE_CHARACTER) {
			//Are we obscuring a terminate?
			if(translations_[index]==AminoAcids.TERMINATE_STATE) {
				removeTerminalIndex(index);
			}
		} else {
			addTerminalIndex(index);
		}
		translations_[index] = aminoAcids_.getState(aminoAcid);
	}

	/**
	 * Returns the char associated with AminoAcid represented by 'codon'.
	 * @note char is as defined by AminoAcids.java
	 * @see AminoAcids
	 * @return state for '?' if codon unknown or wrong length
	 */
	public char getAminoAcidChar(char[] codon) {
		return aminoAcids_.getChar(getAminoAcidState(codon));
	}


	/**
	 * Returns the state associated with AminoAcid represented by 'codon'.
	 * @note state is as defined by AminoAcids.java
	 * @return '?' if codon unknown or wrong length
	 * @see AminoAcids
	 */
	public int getAminoAcidState(char[] codon) {
		int index = Codons.getCodonIndexFromNucleotides(codon);
		if(index<0) {
			return aminoAcids_.getState(DataType.UNKNOWN_CHARACTER);
		}
		return translations_[index];
	}
	public int getAminoAcidStateFromStates(int[] states) {
		int index = Codons.getCodonIndexFromNucleotideStates(states);

		if(index<0) {
			return aminoAcids_.getState(DataType.UNKNOWN_CHARACTER);
		}
		return translations_[index];
	}

	public char getAminoAcidCharFromCodonIndex(int codonIndex) {
		return aminoAcids_.getChar(getAminoAcidStateFromCodonIndex(codonIndex));
	}
	public final boolean isSynonymous(int codonIndexOne, int codonIndexTwo) {
		return translations_[codonIndexOne]==translations_[codonIndexTwo];
	}
	public final  int getAminoAcidStateFromCodonIndex(int codonIndex) {
		return translations_[codonIndex];
	}

	/**
	 * Reset all values of the translation tables to -1 to
	 * signify they have not be initialised.
	 */
	private void clearTranslationTables() {
		for(int i = 0 ; i < translations_.length ; i++) {
			translations_[i] = -1;
		}
		terminatorIndexes_ = new int[0];
	}

	public UniversalTranslator() {
		clearTranslationTables();
		//Phe
		add("UUU", 'F'); add("UUC", 'F');

		//Leu
		add("UUA", 'L'); add("UUG", 'L'); add("CUU", 'L'); add("CUC", 'L'); add("CUA", 'L'); add("CUG", 'L');

		//Ile
		add("AUU", 'I'); add("AUC", 'I'); add("AUA", 'I');

		//Met
		add("AUG", 'M');

		//Val
		add("GUU", 'V'); add("GUC", 'V'); add("GUA", 'V'); add("GUG", 'V');

		//Ser
		add("UCU", 'S'); add("UCC", 'S'); add("UCA", 'S'); add("UCG", 'S');

		//Pro
		add("CCU", 'P'); add("CCC", 'P'); add("CCA", 'P'); add("CCG", 'P');

		//Thr
		add("ACU", 'T'); add("ACC", 'T'); add("ACA", 'T'); add("ACG", 'T');

		//Ala
		add("GCU", 'A'); add("GCC", 'A'); add("GCA", 'A'); add("GCG", 'A');

		//Tyr
		add("UAU", 'Y'); add("UAC", 'Y');

		//Ter
		add("UAA", AminoAcids.TERMINATE_CHARACTER); add("UAG", AminoAcids.TERMINATE_CHARACTER);

		//His
		add("CAU", 'H'); add("CAC", 'H');

		//Gln
		add("CAA", 'Q'); add("CAG", 'Q');

		//Asn
		add("AAU", 'N'); add("AAC", 'N');

		//Lys
		add("AAA", 'K'); add("AAG", 'K');

		//Asp
		add("GAU", 'D'); add("GAC", 'D');

		//Glu
		add("GAA", 'E'); add("GAG", 'E');

		//Cys
		add("UGU", 'C'); add("UGC", 'C');

		//Ter
		add("UGA", AminoAcids.TERMINATE_CHARACTER);

		//Trp
		add("UGG", 'W');

		//Arg
		add("CGU", 'R'); add("CGC" , 'R'); add("CGA", 'R'); add("CGG", 'R');

		//Ser
		add("AGU", 'S'); add("AGC" , 'S');

		//Arg
		add("AGA", 'R'); add("AGG", 'R');

		//Gly
		add("GGU", 'G'); add("GGC" , 'G'); add("GGA", 'G'); add("GGG", 'G');

		//check();
	}

	public void check() {
		System.out.println("Checking");
		for(int i = 0 ; i < translations_.length ; i++) {
			if(translations_[i] == -1) {
				System.out.println("No Translation for codon:"+i+"("+(new String(Codons.getNucleotidesFromCodonIndex(i)))+")");
			}
		}
	}

	public int[] getTerminatorIndexes() {
		return terminatorIndexes_;
	}
	public int getNumberOfTerminatorIndexes() {
		return terminatorIndexes_.length;
	}
}




