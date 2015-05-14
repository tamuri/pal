// AbstractAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.alignment.*;
import pal.io.*;
import pal.datatype.*;
import pal.misc.*;
import java.io.PrintWriter;

/**
 *  This is the basic implementation of the Annotation interface, which is designed to
 *  provide annotation for an alignment.  This annotation can
 *  include information on chromosomal location, site positions, locus name, and the
 *  type of position (exon, intron, etc.)  This class is designed for alignments
 *  with a single locus but multiple sites within the locus.
 *  This class does not permit multiple datatypes per alignment.
 *
 * @version $Id: SimpleAnnotatedAlignment.java,v 1
 *
 * @author Ed Buckler
 */

public class SimpleAnnotatedAlignment extends SimpleAlignment implements AnnotationAlignment{
	/** used to designate position along chromosome */
	 public float chromosomePosition;
	 /** used to designate chromosome */
	 public int chromosome;
	 /** used to designate weighted position; accounts for gaps */
	 public float weightedPosition[];
	 /** used to designate position Type */
	 public char positionType[];
	 /** used to designate locus name */
	 public String locusName;


	/**
	 * Clone constructor from an unannotated alignment.  All annotation is set to defaults
	 */
	public SimpleAnnotatedAlignment(Alignment a) {
		super(a);
		initWhenNoAnnotation();
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
	}

	/**
	 * Clone constructor.
	 */
	public SimpleAnnotatedAlignment(AnnotationAlignment a) {
		super(a);
		chromosomePosition=a.getChromosomePosition(0);
		chromosome=a.getChromosome(0);
		locusName=a.getLocusName(0);
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
		for (int i = 0; i <getSiteCount(); i++) {
			weightedPosition[i]=a.getWeightedLocusPosition(i);
			positionType[i]=a.getPositionType(i);
			}
	}

	/**
	 * This constructor will subset the alignment based on the taxa in IdGroup
	 */
	public SimpleAnnotatedAlignment(AnnotationAlignment a, IdGroup newGroup) {
		int intersectionCount=0;
		for (int i = 0; i <newGroup.getIdCount(); i++) {
			int oldI=a.whichIdNumber(newGroup.getIdentifier(i).getName());
			if(oldI>=0) intersectionCount++;
			}
		sequences=new String[intersectionCount];
		intersectionCount=0;
		for (int i = 0; i <newGroup.getIdCount(); i++) {
			int oldI=a.whichIdNumber(newGroup.getIdentifier(i).getName());
			if(oldI>=0)
				{sequences[intersectionCount]=a.getAlignedSequenceString(oldI);
				intersectionCount++;
				}
			}
		init(newGroup,sequences);
		chromosomePosition=a.getChromosomePosition(0);
		chromosome=a.getChromosome(0);
		locusName=a.getLocusName(0);
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
		for (int i = 0; i <numSites; i++) {
			weightedPosition[i]=a.getWeightedLocusPosition(i);
			positionType[i]=a.getPositionType(i);
			}
	}

		/**
	 * This constructor creates a basic SimpleAnnotatedAlignment.  The annotation should be added with
	 * the set commands.
	 */
	public SimpleAnnotatedAlignment(Identifier[] ids, String[] sequences, String gaps, DataType dt) {
		super(ids, sequences, gaps,dt);
		initWhenNoAnnotation();
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
	}

			/**
	 * This constructor creates a basic SimpleAnnotatedAlignment.  The annotation should be added with
	 * the set commands.
	 */
	public SimpleAnnotatedAlignment(IdGroup group, String[] sequences, DataType dt) {
		super(group, sequences,dt);
		initWhenNoAnnotation();
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
	}
		/**
	 * This constructor creates a basic SimpleAnnotatedAlignment.  The annotation should be added with
	 * the set commands.
	 */
	public SimpleAnnotatedAlignment(IdGroup group, String[] sequences, String gaps, DataType dt) {
		super(group, sequences, gaps,dt);
		initWhenNoAnnotation();
		weightedPosition=new float[numSites];
		positionType=new char[numSites];
	}

	private void init(IdGroup group, String[] sequences) {
					numSeqs = sequences.length;
					numSites = sequences[0].length();

					this.sequences = sequences;
					idGroup = group;

					AlignmentUtils.estimateFrequencies(this);
	}

	private void initWhenNoAnnotation() {
			locusName="Unknown";
			chromosome=-9;
			chromosomePosition=-9;
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
	 public float getWeightedLocusPosition(int site) { return weightedPosition[site];}

		 /** Sets the weighted position along the gene (handles gaps) */
	 public void setWeightedLocusPosition(int site, float weightedPos) {weightedPosition[site]=weightedPos;}

			/** Return the position along the locus (ignores gaps) */
	 public int getLocusPosition(int site) {return site;}


	 /** Returns position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
	 public char getPositionType(int site) {return positionType[site];}

	 /** Set thes position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
	 public void setPositionType(int site, char posType) {positionType[site]=posType;}

	 /** Returns the name of the locus */
	 public String getLocusName(int site) {return locusName;}

			/** Sets the name of the locus */
	 public void setLocusName(String locusName) {this.locusName=locusName;}

			 /** Returns the datatype (for SimpleAnnotatedAlignment there is only one datatype) */
	 public DataType getDataType(int site) {return getDataType(); }

		// interface Report

	public void report(PrintWriter out)
	{
					AlignmentUtils.report(this, out);
					out.println("Locus: " + locusName);
					out.println("Chromsome: " + chromosome + " Position:"+chromosomePosition);
	}
}