// Genotype.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.alignment.*;

/**
 * This class provides from genotype to be constructed from separate but paired
 * alignments.  This is just a very basic implementation.  Other suggested approaches would be welcome.
 *
 * @version $Id:
 *
 * @author Ed Buckler
 */

public class Genotype {
  Alignment[] alignment=new Alignment[2];

  public Genotype(Alignment a1, Alignment a2) {
    this.alignment[0]=a1;
    this.alignment[1]=a2;
  }

  public Alignment getAlignment(int i) {
    return alignment[i];
  }

    /** sequence alignment at (sequence, site, allele) */
  public char getData(int seq, int site, int allele) {
    return alignment[allele].getData(seq,site);
    }
}