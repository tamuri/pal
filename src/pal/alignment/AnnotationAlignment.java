// AnnotationAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;

import pal.datatype.*;
import pal.misc.*;

/**
 *  The AnnotationAlignment interface is designed to provide annotation for an alignment.
 *  This annotation can
 *  include information on chromosomal location, site positions, names of loci, and the
 *  type of position (exon, intron, etc.)  This interface also permits multiple datatypes per
 *  alignment.
 *
 * @version $Id: AnnotationAlignment.java,v 1.2 2001/09/02 13:19:41 korbinian Exp $
 *
 * @author Ed Buckler
 */
public interface AnnotationAlignment extends Alignment, Report {

   /** Return the position along chromosome */
   float getChromosomePosition(int site);

   /** Returns chromosome */
   int getChromosome(int site);

   /** Return the weighted position along the locus (handles gaps) */
   float getWeightedLocusPosition(int site);

   /** Return the position along the locus (ignores gaps) */
   int getLocusPosition(int site);

   /** Returns position type (eg.  I=intron, E=exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
   char getPositionType(int site);

   /** Returns the name of the locus */
   String getLocusName(int site);

    /** Returns the datatype for a specific site, which could differ by site in complex alignments */
   DataType getDataType(int site);

   /** Returns a report for the alignment */
   void report(PrintWriter out);
}
