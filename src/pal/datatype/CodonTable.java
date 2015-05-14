// CodonTable.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * Describes a device for translating Nucleotide triplets
 * or codon indices into amino acid codes.
 * Codon Indexes (or states) are defined as in GeneralizedCodons
 *
 * @author Matthew Goode
 * @author Alexei Drummond
 *
 * @note
 *   <ul>
 *     <li> 19 August 2003 - Added getAminoAcidStateFromIUPACStates()
 *   </ul>
 *
 * @version $Id: CodonTable.java,v 1.10 2003/09/04 03:22:34 matt Exp $
 */

public interface CodonTable extends java.io.Serializable {

	/** TypeID for Universal */
	static final int UNIVERSAL = 0;
	/** TypeID for Vertebrate Mitochondrial*/
	static final int VERTEBRATE_MITOCHONDRIAL = 1;
	/** TypeID for Yeast */
	static final int YEAST = 2;
	/** TypeID for Mold Protozoan Mitochondrial */
	static final int MOLD_PROTOZOAN_MITOCHONDRIAL = 3;
	/** TypeID for Mycoplasma */
	static final int MYCOPLASMA = 4;
	/** TypeID for Invertebrate Mitochondrial */
	static final int INVERTEBRATE_MITOCHONDRIAL = 5;
	/** TypeID for Cilate */
	static final int CILATE = 6;
	/** TypeID for Echinoderm Mitochondrial */
	static final int ECHINODERM_MITOCHONDRIAL = 7;
	/** TypeID for Euplotid Nuclear */
	static final int EUPLOTID_NUCLEAR = 8;
	/** TypeID for Ascidian Mitochondrial */
	static final int ASCIDIAN_MITOCHONDRIAL = 9;
	/** TypeID for Flatworm Mitochondrial */
	static final int FLATWORM_MITOCHONDRIAL = 10;
	/** TypeID for Blepharisma Nuclear */
	static final int BLEPHARISMA_NUCLEAR = 11;
	/** TypeID for Bacterial */
	static final int BACTERIAL = 12;
	/** TypeID for Alternative Yeast */
	static final int ALTERNATIVE_YEAST = 13;

	/**
	 * A textual version of an organism type - type is index into array
	 */
	static final String[] ORGANISM_TYPE_NAMES = {
		"Universal",
		"Vertebrate Mitochondrial",
		"Yeast",
		"Mold Protozoan Mitochondrial",
		"Mycoplasma",
		"Invertebrate Mitochondrial",
		"Cilate",
		"Echinoderm Mitochondrial",
		"Euplotid Nuclear",
		"Ascidian Mitochondrial",
		"Flatworm Mitochondrial",
		"Blepharisma Nuclear",
		"Bacterial",
		"Alternative Yeast"
	};

	/**
	 * Returns the char associated with AminoAcid represented by 'codon'
	 * @note char is as defined by AminoAcids.java
	 * @see AminoAcids
	 * @return state for '?' if codon unknown or wrong length
	 */
	char getAminoAcidChar(char[] codon);

	/**
	 * Returns the state associated with AminoAcid represented by 'codon'
	 * @note state is as defined by AminoAcids.java
	 * @see AminoAcids
	 * @return '?' if codon unknown or wrong length
	 */
	int getAminoAcidState(char[] codon);

	/**
	 * @return all the possible codons for a given amino acid
	 */
	char[][] getCodonsFromAminoAcidState(int aminoAcidState);

	/*
	 * @return all the possible codons for a given amino acid
	 */
	char[][] getCodonsFromAminoAcidChar(char aminoAcidChar);

	/** Returns the amino acid char at the corresponding codonIndex */
	char getAminoAcidCharFromCodonIndex(int codonIndex);

	/** Returns the amino acid state at the corresponding codonIndex */
	int getAminoAcidStateFromCodonIndex(int codonIndex);

	/*
	 * @returns three IUPAC states representing the given amino acid
	 * @note The returned array should not be altered, and implementations
	 *       should attempt to implement this as efficiently as possible
	 * @note the returned array may not be enough to accurately reconstruct the amino acid (as it may be too ambiguous)
	*/
	int[] getIUPACStatesFromAminoAcidState(int aminoAcid);
	int[] getStatesFromAminoAcidState(int aminoAcid);

	/**
	 * @return The AminoAcid states given the nucleotides states (array should be of size 3)
	 */
	int getAminoAcidStateFromStates(int[] states);


	/**
	 * @return the codon states of terminator amino acids.
	 */
	int[] getTerminatorIndexes();

	/**
	 * Returns the number of terminator amino acids.
	 */
	int getNumberOfTerminatorIndexes();

	/**
	 * @return the type of this organism (see defined type constants)
	 */
	int getOrganismTypeID();

	/**
	 * @return true if the amino acids that map to two codons are the same (synonymous). False otherwise
	 */
	boolean isSynonymous(int codonIndexOne, int codonIndexTwo);
}
