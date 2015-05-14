// SpecificAminoAcids.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.datatype;


/**
 * implements a MolecularDataType for amino acids, where we specifiy the Codon Table basis of amino acid by
 * supplying the CodonTable which is/was used for converting Nucleotide sequences to Amino Acids.
 * We need this table to do conversion between Nucleotides and Amino Acids (and vice versa)
 *
 * @version $Id: SpecificAminoAcids.java,v 1.8 2004/10/29 01:13:37 matt Exp $
 *
 * @author Matthew Goode
 */

public class SpecificAminoAcids extends AminoAcids implements MolecularDataType {
	private final CodonTable translationTable_;
	/**
	 * Creates with Universal Codon Table
	 */
	public SpecificAminoAcids()  {
		this(CodonTableFactory.createUniversalTranslator());
	}
	public SpecificAminoAcids(CodonTable translationTable) {
		this.translationTable_ = translationTable;
	}
	public SpecificAminoAcids(int organismTypeID) {
		this(CodonTableFactory.createTranslator(organismTypeID));
	}

	/**
	 * @return Organism TypeID as for CodonTable
	 */
	public int getOrganismTypeID() {
		return translationTable_.getOrganismTypeID();
	}
//==========================================================
//================ MolecularDataType stuff ===================
//==========================================================

	/**
	 *
	 */
	public int[] getNucleotideStates(int[] residueStates) {
		int[] nucleotideStates = new int[residueStates.length*3];
		int base = 0;
		for(int i = 0 ; i < residueStates.length ; i++) {
			int[] codon = translationTable_.getStatesFromAminoAcidState(residueStates[i]);
			System.arraycopy(codon,0,nucleotideStates,base,3);
			base+=3;
		}
		return nucleotideStates;
	}
	/**
	 * @note use this method with caution as the process of converting an AA to a IUPAC codon and back to an AA may not yield the same end AA as a starting one
	 */
	public final int[] getMolecularStatesFromIUPACNucleotides(int[] nucleotideStates, int startingIndex) {
		return getMolecularStatesFromSimpleNucleotides(
			IUPACNucleotides.getSimpleStates(nucleotideStates),
			startingIndex
		);
	}
	public final int[] getMolecularStatesFromSimpleNucleotides(int[] nucleotideStates, int startingIndex) {
	  System.out.println("NS:"+pal.misc.Utils.toString(nucleotideStates));
		int[] residueStates = new int[(nucleotideStates.length-startingIndex)/3];
		int base = startingIndex;
		for(int i = 0 ; i < residueStates.length ; i++) {
			int codonState = Codons.getCodonIndexFromNucleotideStates(nucleotideStates,base);
			if(codonState<0||codonState>63) {
				residueStates[i] = -1;
			} else {
				residueStates[i] = translationTable_.getAminoAcidStateFromCodonIndex(codonState);
			}
			base+=3;
		}
		return residueStates;
	}

	/**
	 * @return false
	 * @note change from true
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
}