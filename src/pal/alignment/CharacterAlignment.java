// CharacterAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;

import pal.misc.*;

/**
 *  This interface is designed to hold quantitative character states.
 *  Each trait (a quantitative character) has two sets of
 * labels.  One is the traitName, and the second is the environmentName.  Obviously any
 * descriptor could be placed in these two labels, however for printing purposes
 * traitName is printed first.  Double.NaN is assumed to be the missing value.
 *
 * @version $Id: CharacterAlignment.java,v 1.2 2001/09/02 13:19:41 korbinian Exp $
 *
 * @author Ed Buckler
 */

public interface CharacterAlignment extends Serializable, IdGroup, Report,
	TableReport {
      double MISSING=Double.NaN;

        /**
	 * Return name of the trait for this trait number
	 */
        String getTraitName(int trait);

        /**
	 * Return name of the environments for this trait number
	 */
        String getEnvironmentName(int trait);

        /**
	 * Return the trait value for a given sequence (taxon) and trait number
	 */
        double getTrait(int seq, int trait);

	/**
	 * Return number of sequences(taxa) in this alignment
	 */
	int getSequenceCount();

      	/** Return number of traits for each sequence in this alignment
	 */
	int getTraitCount();
}
