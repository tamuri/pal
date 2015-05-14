// SimpleCharacterAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;
import pal.misc.*;

/**
 * This provides a basic implementation of CharacterAlignment.  This class holds
 * quantitative character states.  Each trait (a quantitative character) has two sets of
 * labels.  One is the traitName, and the second is the environmentName.  Obviously any
 * descriptor could be placed in these two labels, however for printing purposes
 * traitName is printed first.  Double.NaN is assumed to be the missing value.
 *
 * @version $Id: SimpleCharacterAlignment.java,v 1
 *
 * @author Ed Buckler
 */

public class SimpleCharacterAlignment implements CharacterAlignment, Serializable, IdGroup, Report {

	/** trait values */
	protected double[][] traitValues;

	/** names of the traits */
	protected String[] traitNames;

		/** names of the traits */
	protected String[] environmentNames;

	/** number of sequences */
	protected int numSeqs;

	/** number of traits */
	protected int numTraits;

	/** sequence identifiers */
	protected IdGroup idGroup;


	 public SimpleCharacterAlignment() {
	}

	/**
	 * Constructor for SimpleCharacterAlignment.  Environment names if be set to default NA.
	 * @param group array of taxa identifiers
	 * @param traitValues matrix of trait values
	 * @param traitNames array of trait names
	 */
	public SimpleCharacterAlignment(Identifier[] ids, double[][] traitValues, String[] traitNames) {
		this(new SimpleIdGroup(ids),traitValues, traitNames);
	}

		/**
	 * Constructor for SimpleCharacterAlignment.  Environment names if be set to default NA.
	 * @param group taxa names
	 * @param traitValues matrix of trait values
	 * @param traitNames array of trait names
	 */
	public SimpleCharacterAlignment(IdGroup group, double[][] traitValues, String[] traitNames) {
		System.out.println("Starting CharacterAlignment(IdGroup group, double[][] traitValues, String[] traitNames)");
		if(group.getIdCount()!=traitValues.length) return;
		idGroup=group;
		this.traitNames=traitNames;
		this.traitValues=traitValues;
		numSeqs=idGroup.getIdCount();
		numTraits=traitValues[0].length;
		environmentNames=new String[numTraits];
		for(int i=0; i<numTraits; i++)
			{environmentNames[i]="NA";}
	}

	/**
	 * Constructor for SimpleCharacterAlignment
	 * @param group taxa names
	 * @param traitValues matrix of trait values
	 * @param traitNames array of trait names
	 * @param environNames array of environment names
	 */
	public SimpleCharacterAlignment(IdGroup group, double[][] traitValues, String[] traitNames, String[] environNames) {
		System.out.println("Starting CharacterAlignment(IdGroup group, double[][] traitValues, String[] traitNames, String[] environNames)"+environmentNames);
		if(group.getIdCount()!=traitValues.length) return;
		idGroup=group;
		this.traitNames=traitNames;
		this.traitValues=traitValues;
		numSeqs=idGroup.getIdCount();
		numTraits=traitValues[0].length;
		this.environmentNames=environNames;
	}
	/**
	 * Constructor for SimpleCharacterAlignment when there is only a single trait.  Environment names if be set to default NA.
	 * @param group holds taxa names
	 * @param traitValue array of trait values
	 * @param traitName trait name
	 */
	public SimpleCharacterAlignment(IdGroup group, double[] traitValue, String traitName) {
		if(group.getIdCount()!=traitValues.length) return;
		idGroup=group;
		traitNames=new String[1];
		environmentNames=new String[1];
		this.traitNames[0]=traitName;
		environmentNames[0]="NA";
		traitValues=new double[traitValue.length][1];
		for (int i = 0; i <traitValue.length; i++) {
			traitValues[i][0]=traitValue[i];
			}
		numSeqs=idGroup.getIdCount();
		numTraits=1;
	}

		/**
		* Return the trait value for a given sequence (taxon) and trait number
		*
		*         */
		public double getTrait(int seq, int trait) {
			return traitValues[seq][trait];
		}

			/**
	 * Return number of traits in this alignment
	 */
	public final int getLength() {
		return numTraits;
	}

	/**
	 * Return number of taxa or sequences in this alignment
	 */
	public final int getSequenceCount() {
		return numSeqs;
	}


				/** Return number of trait for each taxon in this alignment
	 */
	public final int getTraitCount() {
		return numTraits;
	}

				/**
	 * Return name of the trait for this trait number
	 */
				public String getTraitName(int trait) {
						return traitNames[trait];
					}

				/**
	 * Return name of the environments for this trait number
	 */
				public String getEnvironmentName(int trait){
						return environmentNames[trait];
					}

	//IdGroup interface
	public Identifier getIdentifier(int i) {return idGroup.getIdentifier(i);}
	public void setIdentifier(int i, Identifier ident) { idGroup.setIdentifier(i, ident); }
	public int getIdCount() { return idGroup.getIdCount(); }
	public int whichIdNumber(String name) { return idGroup.whichIdNumber(name); }

			/** returns representation of this alignment as a string */
	public String toString() {

		StringWriter sw = new StringWriter();

								PrintWriter out=new PrintWriter(sw);
								out.println("  " + getSequenceCount() + " " + getTraitCount());
								out.print("Taxa/Trait\t");
								for(int j=0; j<getTraitCount(); j++)
										{out.print(traitNames[j]+"\t");}
								out.println();
								if(environmentNames!=null)
									{out.print("Taxa/Environ\t");
									for(int j=0; j<getTraitCount(); j++)
										{out.print(environmentNames[j]+"\t");}
									out.println();
									}
		for(int i=0; i<getSequenceCount(); i++)
									{out.print(idGroup.getIdentifier(i).getName()+"\t");
									for(int j=0; j<getTraitCount(); j++)
										{out.print(getTrait(i,j)+"\t");}
									out.println();
									}
		return sw.toString();
	}

	// interface Report

	public void report(PrintWriter out)
	{
								out.println("Number of sequences: " + getSequenceCount());
		out.println("Number of traits: " + getTraitCount());
	}



						 //Implementation of TableReport Interface
		 /**
		 * Return column names for the table
		 */
		public Object[] getTableColumnNames() {
			String[] basicLabels=new String[getTraitCount()+1];
			basicLabels[0]="Taxa";
			for(int c=0; c<getTraitCount(); c++)
					{basicLabels[c+1]=getTraitName(c)+"."+getEnvironmentName(c);}
			return basicLabels;
			}

		/**
		 * Return data for the table
		 */
		public Object[][] getTableData() {
			Object[][] data;
//      java.text.NumberFormat nf=new java.text.DecimalFormat();
//      nf.setMaximumFractionDigits(8);
			int i=0, labelOffset;
			data=new String[getSequenceCount()][getTraitCount()+1];
			for(int r=0; r<getSequenceCount(); r++)
				{data[r][0]=getIdentifier(r).getName();
				for(int c=0; c<getTraitCount(); c++)
					{data[r][c+1]=""+getTrait(r,c);}
				}
			return data;
			}

		/**
		 * Return the name for the title of the ANOVA
		 */
		public String getTableTitle() {
			return "Phenotypes";
		}

}