// StrippedAnnotatedAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.alignment.*;
import pal.datatype.*;

import java.io.PrintWriter;

/**
 *  This is the stripped implementation of the Annotation interface, which is designed to
 *  provide stripped and annotated alignments.  This annotation can
 *  include information on chromosomal location, site positions, locus name, and the
 *  type of position (exon, intron, etc.)
 *
 * This class also add some methods for stripping sites based on frequency, count, and a
 * range of positions.
 *
 * @version $Id: StrippedAnnotatedAlignment.java,v 1
 *
 * @author Ed Buckler
 */

public class StrippedAnnotatedAlignment extends StrippedAlignment implements AnnotationAlignment{
	//Note I made alias protected in StrippedAlignment so this subclass can access it

	/**
	 * Simple constructor
	 */
	public StrippedAnnotatedAlignment(AnnotationAlignment a) {
		super(a);
		setDataType(a.getDataType());
		rawAlignment=a;
		firstSite=0;
		lastSite=rawAlignment.getSiteCount();
	}

	/** Return the position along chromosome */
	 public float getChromosomePosition(int site) {return rawAlignment.getChromosomePosition(alias[site]);}


	 /** Returns chromosome */
	 public int getChromosome(int site) {return rawAlignment.getChromosome(alias[site]);}

	 /** Return the weighted position along the gene (handles gaps) */
	 public float getWeightedLocusPosition(int site) { return rawAlignment.getWeightedLocusPosition(alias[site]);}

			/** Return the position along the locus (ignores gaps) */
	 public int getLocusPosition(int site) {return rawAlignment.getLocusPosition(alias[site]);}

	 /** Returns position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
	 public char getPositionType(int site) {return rawAlignment.getPositionType(alias[site]);}

	 /** Returns the name of the locus */
	 public String getLocusName(int site) {return rawAlignment.getLocusName(alias[site]);}

	 /** Returns the datatype */
	 public DataType getDataType(int site) {return rawAlignment.getDataType(alias[site]);}

	 /**
		* Remove sites based on site position (excluded sites are <firstSite and >lastSite)
		* This not effect any prior exclusions.
		*
		* @param firstSite first site to keep in the range
		* @param lastSite last site to keep in the range
		*/
	 public void removeSitesOutsideRange(int firstSite, int lastSite) {
			this.firstSite=firstSite;
			this.lastSite=lastSite;
			for (int i = 0; i <rawAlignment.getSiteCount(); i++)
				{if((rawAlignment.getLocusPosition(i)<firstSite)||(rawAlignment.getLocusPosition(i)>lastSite)) dropSite(i);}
	 }

	 /**
	 * remove constant sites but ignore gaps and missing data (- and ?)
	 */
	public void removeConstantSitesIgnoreGapsMissing()
	{
		int[] charCount = new int[numSeqs];

		for (int i = 0; i < rawAlignment.getSiteCount(); i++)
		{
			for (int j = 0; j < numSeqs; j++)
			{
				charCount[j] = 0;
			}

			// count how often each character appears in this column
			for (int j = 0; j < numSeqs; j++)
			{char c = rawAlignment.getData(j, i);
												if ((c!='-')&&(c!='?')&&(charCount[j] == 0))
				{
																	charCount[j] = 1;
																	//char c = rawAlignment.getData(j, i);
																	for (int k = j+1; k < numSeqs; k++)
																	{
																					if (c == rawAlignment.getData(k, i))
																					{
																									charCount[j]++;
																									charCount[k] = -1;
																					}
																	}
				}
			}

			// number of different characters that appear more than 1 time
			int num = 0;
			for (int j = 0; j < numSeqs; j++)
			{
				if (charCount[j] > 1)
				{
					num++;
				}
			}

			// drop uninformative sites
			if (num < 2) dropSite(i);
		}
	}
		/**
	 * remove sites based on minimum frequency (the count of good bases)
	 *   and based on the proportion of good sites different from consensus
	 *
	 * @param minimumProportion minimum proportion of sites different from the consensus
	 * @param minimumCount minimum number of sequences with a good bases (not - or ?)
	 */
		public void removeSitesBasedOnFreqIgnoreGapsMissing(double minimumProportion, int minimumCount)
	{       this.minimumProportion=minimumProportion;
								this.minimumCount=minimumCount;
		int[] charCount = new int[numSeqs];

		for (int i = 0; i < rawAlignment.getSiteCount(); i++)
		{
			for (int j = 0; j < numSeqs; j++)
			{
				charCount[j] = 0;
			}

			// count how often each character appears in this column
			for (int j = 0; j < numSeqs; j++)
			{char c = rawAlignment.getData(j, i);
												if ((charCount[j] == 0)&&(c!='-')&&(c!='?'))
				{
																	charCount[j] = 1;
																	//char c = rawAlignment.getData(j, i);
																	for (int k = j+1; k < numSeqs; k++)
																	{
																					if (c == rawAlignment.getData(k, i))
																					{
																									charCount[j]++;
																									charCount[k] = -1;
																					}
																	}
				}
			}

			// number of different characters that appear more than 1 time
			int num = 0;
												//total number of good sites
												int totalGood=0;
												int maxProportion=0;
			for (int j = 0; j < numSeqs; j++)
			{
				if (charCount[j] > 0)
				{
					num++;
																				totalGood+=charCount[j];
																				if(charCount[j]>maxProportion) {maxProportion=charCount[j];}
				}
			}

			// drop uninformative sites
												double obsMinProp=1.0-((double)maxProportion/(double)totalGood);
			if ((totalGood==0)||(totalGood<minimumCount)||(obsMinProp<minimumProportion)) dropSite(i);
		}
	}


		// interface Report

	public void report(PrintWriter out)
	{
					AlignmentUtils.report(this, out);
					out.println("Locus: " + this.getLocusName(0));
					out.println("Chromsome: " + this.getChromosome(0) + " Position: "+this.getChromosomePosition(0));
					out.println("Minimum Allele Frequency: " + minimumProportion);
					out.println("Polymorphism included if in >"+ minimumCount+" good sequences");
					out.println("Range Start: " + firstSite+" End: "+lastSite);
	}
		private AnnotationAlignment rawAlignment = null;
		private int rawNumSites;
		protected double minimumProportion=0;
		protected int minimumCount=0;
		protected int firstSite=0, lastSite=0;
//    private int[] alias;
//    private boolean[] notDropped;
}