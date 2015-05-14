// MultiLocusAnnotatedAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.datatype.*;
import pal.misc.*;

/**
 * MultiLocusAnnotatedAlignment is an extension of the SimpleAlignment that includes Annotation, and is designed for multiple
 * loci.  Separate annotation information is stored from each sites.  This would be good SNP
 * SSR type of data, but it would be inefficient for single gene data.
 *
 * @version $Id:
 *
 * @author Ed Buckler
 */

public class MultiLocusAnnotatedAlignment extends SimpleAlignment implements AnnotationAlignment{
	/** used to designate position along chromosome */
	 public float chromosomePosition[];
	 /** used to designate chromosome */
	 public int chromosome[];
	 /** used to designate weighted position; accounts for gaps */
	 public float weightedPosition[];
	 /** used to designate position; do not account for gaps */
	 public int locusPosition[];
	 /** used to designate position Type */
	 public char positionType[];
	 /** used to designate locus name */
	 public String locusName[];

	 /** provides datatype for each locus separately*/
	 public DataType[] siteDataType=null;

	/**
	 * Basic constructor.
	 */
	public MultiLocusAnnotatedAlignment(Alignment a) {
		super(a);
		initMatrices();
	}

		/**
	 * null constructor.
	 */
	public MultiLocusAnnotatedAlignment() {
	}

		/**
	 * Clone constructor for Annotated alignment
	 */
	public MultiLocusAnnotatedAlignment(AnnotationAlignment a) {
		super(a);
		initMatrices();
		for (int i = 0; i <getSiteCount(); i++) {
			chromosomePosition[i]=a.getChromosomePosition(i);
			chromosome[i]=a.getChromosome(i);
			locusName[i]=a.getLocusName(i);
			weightedPosition[i]=a.getWeightedLocusPosition(i);
			positionType[i]=a.getPositionType(i);
			}
	}

	public MultiLocusAnnotatedAlignment(Identifier[] ids, String[] sequences, String gaps, DataType dt) {
		super(ids, sequences, gaps,dt);
		initMatrices();
	}

	public MultiLocusAnnotatedAlignment(IdGroup group, String[] sequences, DataType dt) {
		super(group, sequences,dt);
		initMatrices();
	}

	public MultiLocusAnnotatedAlignment(IdGroup group, String[] sequences, String gaps, DataType dt) {
		super(group, sequences, gaps,dt);
		initMatrices();
	}

		/**
	 * This constructor will subset the alignment based on the taxa in IdGroup
	 */
	public MultiLocusAnnotatedAlignment(AnnotationAlignment a, IdGroup newGroup) {
		sequences=new String[newGroup.getIdCount()];
		for (int i = 0; i <newGroup.getIdCount(); i++) {
			int oldI=a.whichIdNumber(newGroup.getIdentifier(i).getName());
			sequences[i]=a.getAlignedSequenceString(oldI);
			}
		init(newGroup,sequences);
		initMatrices();
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
		for (int i = 0; i <numSites; i++) {
			chromosomePosition[i]=a.getChromosomePosition(i);
			chromosome[i]=a.getChromosome(i);
			locusName[i]=a.getLocusName(i);
			weightedPosition[i]=a.getWeightedLocusPosition(i);
			positionType[i]=a.getPositionType(i);
			}
	}

	protected void initMatrices() {
		chromosomePosition=new float[getSiteCount()];
		chromosome=new int[getSiteCount()];
		locusName=new String[getSiteCount()];
		locusPosition=new int[getSiteCount()];
		weightedPosition=new float[getSiteCount()];
		positionType=new char[getSiteCount()];
	}

		protected void init(IdGroup group, String[] sequences) {
					numSeqs = sequences.length;
					numSites = sequences[0].length();

					this.sequences = sequences;
					idGroup = group;

					AlignmentUtils.estimateFrequencies(this);
	}

	/** Return the position along chromosome */
	 public float getChromosomePosition(int site) {return chromosomePosition[site];}

	 /** Set the position along chromosome */
	 public void setChromosomePosition(float position, int site)
		{this.chromosomePosition[site]=position;}

	 /** Returns chromosome */
	 public int getChromosome(int site) {return chromosome[site];}

		/** Sets chromosome */
	 public void setChromosome(int chromosome, int site)
		{this.chromosome[site]=chromosome;}

	/** Return the weighted position along the gene (handles gaps) */
	 public float getWeightedLocusPosition(int site) { return weightedPosition[site];}

		 /** Sets the weighted position along the gene (handles gaps) */
	 public void setWeightedLocusPosition(int site, float weightedPos) {weightedPosition[site]=weightedPos;}

			/** Return the position along the locus (ignores gaps) */
	 public int getLocusPosition(int site) {return locusPosition[site];}

				/** Set the position within the locus */
	 public void setLocusPosition(int position, int site) {locusPosition[site]=position;}

	 /** Returns position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
	 public char getPositionType(int site) {return positionType[site];}

			/** Set thes position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
	 public void setPositionType(int site, char posType) {positionType[site]=posType;}

	 /** Returns the name of the locus */
	 public String getLocusName(int site) {return locusName[site];}

			/** Sets the name of the locus */
	 public void setLocusName(String locusName, int site) {this.locusName[site]=locusName;}

	 /** Returns the datatype */
	 public DataType getDataType(int site) {
		if(siteDataType==null) { return getDataType(); }
		else { return siteDataType[site]; }
	}

}