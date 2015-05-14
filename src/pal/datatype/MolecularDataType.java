// MolecularDataType.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.datatype;

/**
 * An extension to the generic DataType class for DataTypes
 * related to genetic residues (by this it is meant Nucleotides,
 * AminoAcids, and Codons).
 * @author Matthew Goode
 * @version 1.1
 */

public interface MolecularDataType extends DataType {

	/**
	 * @param molecularStates an array of states corresponding to states of <emph>this</emph> datatype
	 * @return the corresponding IUPAC states
	 */
	int[] getNucleotideStates(int[] molecularStates);

	/**
	 * @param the IUPAC nucleotidestates
	 * @returns the input converted to states of <emph>this</emph> data type
	 */
	int[] getMolecularStatesFromIUPACNucleotides(int[] nucleotideStates, int startingIndex);
	/**
	 * @param the Simple nucleotide states (eg 0,1,2,3 or A,C,G,T)
	 * @returns the input converted to states of <emph>this</emph> data type
	 */
	int[] getMolecularStatesFromSimpleNucleotides(int[] nucleotideStates, int startingIndex);

	/**
	 * @return true if this data type will create Nucleotide states using IUPAC states (for example if this
	 * DataType is AminoAcid based, IUPAC states are needed to maintain information on different possible values for a state)
	 */
	boolean isCreatesIUPACNuecleotides();

	/**
	 * @return the number of nucleotides required for a single character of this data typedata
	 */
	int getNucleotideLength();

// ============================================================================
	/**
	 * Utilities relating to MolecularDataType stuff
	 */
	public static final class Utils {
		/**
		 * Converts (if possible) a DataType into a MolecularDataType. This is done by
		 * either casting (if input is already a MolecularDataType) or, if the DataType
		 * represents AminoAcids returning a SpecificAminoAcids object based on the Universal Codon Table
		 * @param dt the base DataType
		 * @return null if not possible to derive a MolecularDataType
		 */
		public static final MolecularDataType getMolecularDataType(DataType dt) {
			if(dt instanceof MolecularDataType) {
				return (MolecularDataType)dt;
			}
			if(dt.getTypeID()==DataType.AMINOACIDS) {
				return new SpecificAminoAcids();
			}
			return null;
		}
	}
}