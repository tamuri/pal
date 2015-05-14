// AlignmentTool.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;

import pal.datatype.*;
/**
 * Simple access for alignment functions. The purpose of this class is to provide a set
 * interface for doing basic alignment operations.
 *
 * <b>History</b>
 * <ul>
 *  <li> 15/09/2003 - Created </li>
 * </ul>
 *
 * @version $Id: AlignmentTool.java,v 1.2 2004/01/13 22:04:49 matt Exp $
 *
 * @author Matthew Goode
 *
 */

public final class AlignmentTool {
	/**
	 * A simple approach to creating a bootstrap replicate
	 * @param base The original alignment
	 * @return A bootstrap replicate of the input alignment
	 * @note disadvantages - looses ability rebootstrap replicate (see BootstrappedAlignment.bootstrap())
	 */
	public static final Alignment createBootstrapReplicate(Alignment base){
	  return new BootstrappedAlignment(base);
	}
		/**
	 * Create a gap balanced alignment. That is one that removes sites where sequences are out of frame from other sequences in the site.
	 * @param base The original alignment
	 * @param startingIndex The nucleotide position at which to start the translating (counting from zero)
	 * @return The gap balanced version
	 */
	public static final Alignment createGapBalanced(Alignment base, int startingIndex){
	  return new GapBalancedAlignment(base,startingIndex,true);
	}
	/**
	 * Convert an alignment to one of amino acids (using Universal Translation)
	 * @param base The base alignment (in any datatype, but for best results a Nucleotide alignment)
	 * @param startingIndex The nucleotide position at which to start the translating (counting from zero)
	 * @return The converted alignment
	 */
	public static final Alignment convertToUniversalAminoAcids(Alignment base, int startingIndex) {
		DataTranslator dt = new DataTranslator(base);
		return dt.toAlignment(new SpecificAminoAcids(CodonTable.UNIVERSAL),startingIndex);
	}

	/**
	 * Attempt to read a file from a reader object
	 * @param r A reader object
	 * @param dt The datatype of the resulting alignment
   * @return A loaded alignment
	 * @throws IOException if there is a problem reading the alignment
	 */
	public static final Alignment readAlignment(Reader r, DataType dt) throws IOException {
	  BufferedReader br = new BufferedReader(r);
		try {
			return AlignmentReaders.readPhylipClustalAlignment(br,dt);
		} catch (AlignmentParseException e) {
			br.reset();
			return AlignmentReaders.readFastaSequences(br,dt);
		}
	}
}