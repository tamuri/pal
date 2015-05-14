// DataTypeTools.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.datatype;

/**
 * Simple access for data type functions. The purpose of this class is to provide a set
 * interface for constructing basic data types operations.
 *
 * <b>History</b>
 * <ul>
 *  <li> 15/09/2003 - Created </li>
 * </ul>
 *
 * @version $Id: DataTypeTool.java,v 1.2 2004/01/15 01:18:32 matt Exp $
 *
 * @author Matthew Goode
 *
 */


public final class DataTypeTool {
	/**
	 * A set access point for a data type object describing nucleotides (DNA)
	 * @return a data type object representing nucleotides
	 */
	public static final MolecularDataType getNucleotides() {
		return Nucleotides.DEFAULT_INSTANCE;
	}
	/**
	 * A set access point for a data type object describing amino acids (used with relation to the Universal codon table)
	 * @return a data type object representing Amino Acids
	 */
	public static final MolecularDataType getUniverisalAminoAcids() {
		return new SpecificAminoAcids(CodonTableFactory.createUniversalTranslator());
	}
	/**
	 * A set access point for a data type object describing nucleotides (RNA)
	 * @return a data type object representing nucleotides, using RNA characters (ACGU)
	 */
	public static final MolecularDataType getRNANucleotides() {
		return new Nucleotides(true);
	}

}