// AbstractAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;
import java.util.*;

import pal.datatype.*;
import pal.misc.*;

/**
 * This is an annotation version of the ConcatenatedAlignment
 *
 * Unlike normal ConcatenatedAlignment, it permits for merges with different numbers
 * of sequences.  It will merge by either union or intersection.  Missing taxa will return
 * missing values on getData.
 *
 * @version $Id:
 *
 * @author Ed Buckler
 */

public class ConcatenatedAnnotatedAlignment extends AbstractAlignment implements AnnotationAlignment {

  	//
	// Private stuff
	//

	private AnnotationAlignment[] alignmentList;
	private int numAlignments;
	private int[] alignmentIndex;
	private int[] siteIndex;
        private int[][] sequenceIndex;
        private boolean mergeByID, union;
 /**
	 * concatenate alignments
	 *
	 * @param list array with alignments to concatenate
         * @param mergeByID if true it will be by ID name
         * @param union if true it will create the union of the list, if false the intersection will be produced
	 */
	public ConcatenatedAnnotatedAlignment(AnnotationAlignment[] list, boolean mergeByID, boolean union)
		throws IllegalArgumentException
	{
		alignmentList = list;

		numAlignments = alignmentList.length;
		if (numAlignments == 0)
		{
			throw new IllegalArgumentException("NO ALIGNMENT");
		}

                this.mergeByID=mergeByID;
                this.union=union;
                if(mergeByID)
                  {initMergeByID(union);}
                 else
                  {initStraightMerge();}
        }

        private void initStraightMerge() {
		numSeqs = alignmentList[0].getSequenceCount();
		idGroup = alignmentList[0];

		numSites = 0;
		for (int i = 0; i < numAlignments; i++)
		{
			numSites += alignmentList[i].getSiteCount();

			if (alignmentList[i].getSequenceCount() != numSeqs)
			{
				throw new IllegalArgumentException("INCOMPATIBLE ALIGNMENTS");
			}
		}

		// Create indices
		alignmentIndex = new int[numSites];
		siteIndex = new int[numSites];

		int s = 0;
		for (int i = 0; i < numAlignments; i++)
		{
			for (int j = 0; j < alignmentList[i].getSiteCount(); j++)
			{
                          alignmentIndex[s+j] = i;
                          siteIndex[s+j] = j;
			}
			s += alignmentList[i].getSiteCount();
		}
	}

        private void initMergeByID(boolean union) {
		numSites = 0;
                Vector idVector=new Vector();

                //make a combined list of all Identifiers
		for (int i = 0; i < numAlignments; i++)
		  {numSites += alignmentList[i].getSiteCount();
                  for (int j = 0; j <alignmentList[i].getSequenceCount(); j++) {
                    if(!idVector.contains(alignmentList[i].getIdentifier(j)))
                      {idVector.addElement(alignmentList[i].getIdentifier(j));}
                    }
		  }
                Identifier[] ids;
                if(!union)
                  //this is not the most efficient way, but it works well with other code I use and heck it is plenty fast
                  {boolean inAll;
                  Vector intersectionVector=new Vector();
                  for (int i = 0; i < idVector.size(); i++)
		    {inAll=true;
                    String id=((Identifier)idVector.elementAt(i)).getName();
                    for (int j = 0; j <numAlignments; j++)
                      {if(alignmentList[j].whichIdNumber(id)<0) inAll=false;}
                    if(inAll) intersectionVector.addElement(idVector.elementAt(i));
                    }
                  ids=new Identifier[intersectionVector.size()];
                  for(int i=0; i<ids.length; i++) {ids[i]=(Identifier)intersectionVector.elementAt(i);}
                  }
                 else
                  {ids=new Identifier[idVector.size()];
                  for(int i=0; i<ids.length; i++) {ids[i]=(Identifier)idVector.elementAt(i);}
                  }
//                Identifier[] ids=new Identifier[idVector.size()];
//                for(int i=0; i<ids.length; i++) {ids[i]=(Identifier)idVector.get(i);}
                idGroup=new SimpleIdGroup(ids);
                numSeqs=idGroup.getIdCount();
                sequenceIndex=new int[numAlignments][numSeqs];
                //set seq index to -1 for all, if sequence missing for alignment then missing will be returned
                for (int i = 0; i <numAlignments; i++) {
                  for (int j = 0; j <numSeqs; j++) {sequenceIndex[i][j]=-1;}
                  }
		// Create indices
		alignmentIndex = new int[numSites];
		siteIndex = new int[numSites];

		int s = 0;
		for (int i = 0; i < numAlignments; i++)
		{
			for (int j = 0; j < alignmentList[i].getSiteCount(); j++)
			{
                          alignmentIndex[s+j] = i;
                          siteIndex[s+j] = j;
			}
			s += alignmentList[i].getSiteCount();
                        for (int k = 0; k < alignmentList[i].getSequenceCount(); k++)
			  {if(idGroup.whichIdNumber(alignmentList[i].getIdentifier(k).getName())>-1)
                            sequenceIndex[i][idGroup.whichIdNumber(alignmentList[i].getIdentifier(k).getName())]=k;
                          }
		}
	}
	// Implementation of abstract Alignment method

    /** sequence alignment at (sequence, site) */
    public char getData(int seq, int site)
      {     if(mergeByID)
              {if(sequenceIndex[alignmentIndex[site]][seq]==-1) return '?';
               else
              return alignmentList[alignmentIndex[site]].getData(sequenceIndex[alignmentIndex[site]][seq], siteIndex[site]);}
            else
              {return alignmentList[alignmentIndex[site]].getData(seq, siteIndex[site]);}
      }

  /** Return the datatype for a given site, which can differ between source alignments */
  public DataType getDataType(int site)
    {return alignmentList[alignmentIndex[site]].getDataType();}

  /** Return the position along chromosome */
   public float getChromosomePosition(int site)
    {return alignmentList[alignmentIndex[site]].getChromosomePosition(siteIndex[site]);}

   /** Returns chromosome */
   public int getChromosome(int site) {return alignmentList[alignmentIndex[site]].getChromosome(siteIndex[site]);}

  /** Return the weighted position along the gene (handles gaps) */
   public float getWeightedLocusPosition(int site) { return 0.0f;}

   /** Return the position along the locus (ignores gaps) */
   public int getLocusPosition(int site) {return alignmentList[alignmentIndex[site]].getLocusPosition(siteIndex[site]);}

   /** Returns position type (eg.  I=intron, E-exon, P=promoter, 1=first, 2=second, 3=third, etc.*/
   public char getPositionType(int site) {return 0;}

   /** Returns the name of the locus */
   public String getLocusName(int site) {return alignmentList[alignmentIndex[site]].getLocusName(siteIndex[site]);}

   /**
    * sort the sites by chromosome, then by chromosomal location, and final locusPosition
    */
  public void sortSites() {
    //this will implement a bubble sort
    boolean flag=true;
    double[] sortPos=new double[getSiteCount()];
    double tempSortPos;
    int tempAlignmentIndex,tempSiteIndex;
    do{flag=false;
      for(int i=1; i<getSiteCount(); i++)
        {if(isSiteIBeforeSiteJ(i-1,i)==false)
          {flag=true;
          tempSortPos=sortPos[i];
          sortPos[i]=sortPos[i-1];
          sortPos[i-1]=tempSortPos;
          tempAlignmentIndex=alignmentIndex[i];
          alignmentIndex[i]=alignmentIndex[i-1];
          alignmentIndex[i-1]=tempAlignmentIndex;
          tempSiteIndex=siteIndex[i];
          siteIndex[i]=siteIndex[i-1];
          siteIndex[i-1]=tempSiteIndex;
          }
        }
    } while(flag);
    System.err.println("Sites sorted");
  }

  private boolean isSiteIBeforeSiteJ(int siteI, int siteJ) {
    if(getChromosome(siteI)<getChromosome(siteJ)) return true;
      else if(getChromosome(siteI)>getChromosome(siteJ)) return false;
    if(getChromosomePosition(siteI)<getChromosomePosition(siteJ)) return true;
      else if(getChromosomePosition(siteI)>getChromosomePosition(siteJ)) return false;
    if(getLocusPosition(siteI)<getLocusPosition(siteJ)) return true;
      else if(getLocusPosition(siteI)>getLocusPosition(siteJ)) return false;
    //for ties return true
    return true;

  }

 // interface Report
    public void report(PrintWriter out)
	{
          AlignmentUtils.report(this, out);
          out.println("Number of Alignments: "+alignmentList.length);
          out.println();
          for(int i=0; i<alignmentList.length; i++)
            {out.println("Alignment: "+i);
            alignmentList[i].report(out);
            out.println();
            }
	}
}