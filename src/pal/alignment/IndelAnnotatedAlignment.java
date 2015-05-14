// AbstractAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.alignment;

import pal.datatype.*;

/**
 * an extension of the IndelAlignment that includes annotation.  This should only extract
 * indels from a single locus.
 *
 * @version $Id:
 *
 * @author Ed Buckler
 */
public class IndelAnnotatedAlignment extends IndelAlignment implements AnnotationAlignment{
	/** used to designate position along chromosome */
	 public float chromosomePosition;
	 /** used to designate chromosome */
	 public int chromosome;
	 /** used to designate weighted position; accounts for gaps */
	 public float weightedLocusPosition[];
	 /** used to designate position; do not account for gaps */
	 public int locusPosition[];
	 /** used to designate position Type */
	 public char positionType[];
	 /** used to designate locus name */
	 public String locusName;

	/**
	 * Basic constructor.  All annotation is based off the first site in the AnnotationAlignment.
	 * This Alignment should not span multiple loci.
	 * @param anchored sets to score anchored indels as same position
	 */
	public IndelAnnotatedAlignment(AnnotationAlignment a, boolean anchored) {
		super((Alignment)a,anchored);
		setDataType(new NumericDataType());
		chromosomePosition=a.getChromosomePosition(0);
		chromosome=a.getChromosome(0);
		locusName=a.getLocusName(0);
		locusPosition=new int[numSites];
		weightedLocusPosition=new float[numSites];
		positionType=new char[numSites];
		IndelPosition ip;
		for(int i=0; i<numSites; i++)
			{ip=getIndelPosition(i);
			locusPosition[i]=ip.start;  //the start of the indel is used for the position
			weightedLocusPosition[i]=a.getWeightedLocusPosition(ip.start);
			positionType[i]=a.getPositionType(ip.start);
			}
	}

	/** Return the position along chromosome */
	 public float getChromosomePosition(int site) {return chromosomePosition;}

	 /** Set the position along chromosome */
	 public void setChromosomePosition(float position)
		{this.chromosomePosition=position;}

	 /** Returns chromosome */
	 public int getChromosome(int site) {return chromosome;}

		/** Sets chromosome */
	 public void setChromosome(int chromosome)
		{this.chromosome=chromosome;}

	 /** Return the weighted position along the gene (handles gaps) */
	 public float getWeightedLocusPosition(int site) { return weightedLocusPosition[site];}

			/** Return the position along the locus (ignores gaps) */
	 public int getLocusPosition(int site) {return locusPosition[site];}

	 /** Returns position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
	 public char getPositionType(int site) {return positionType[site];}

	 /** Returns the name of the locus */
	 public String getLocusName(int site) {return locusName;}

			/** Sets the name of the locus */
	 public void setLocusName(String locusName) {this.locusName=locusName;}

	 /** Returns the datatype */
	 public DataType getDataType(int site) {return getDataType();}

}