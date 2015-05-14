// AbstractAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.util.*;

import pal.datatype.*;
import pal.misc.*;



/**
 * This class extracts indels (insertion/deletion) out of an alignment, and creates
 * an alignment of indel polymorphisms.  It uses the NumericDataType, so that the length
 * of each indel is recorded in the alignment.  If anchored is true, then SSR-like
 * (microsatellites) indels
 * will be identified as the same indel locus.  If anchored is false, then the same indel
 * start and end at the exact same position.
 *
 * @version $Id: IndelAlignment.java,v 1.4 2002/10/14 06:54:24 matt Exp $
 *
 * @author Ed Buckler
 */

public class IndelAlignment extends AbstractAlignment {
	/** The sequences */
	String[] sequences;
	/**
	 * Indels anchored is true, then indels that share either a left
	 * or right side will be combined in the same locus.  If false, an indel must share
	 * both sides to be scored in this alleles.
	 */
	boolean anchored;

	/**
	 * Vector that contains IndelPositions, which has start, end and anchored information.
	 */
	Vector indel;


	private Alignment rawAlignment = null;

	/**
	 * Basic constructor.
	 * @param anchored sets to score anchored indels as same position
	 */
	public IndelAlignment(Alignment a, boolean anchored) {
					rawAlignment=a;
					this.anchored=anchored;
					setDataType(new NumericDataType());
					sequences = new String[a.getIdCount()];
					findIndels();
					String gaps = a.GAP + "";

					init(new SimpleIdGroup(a), gaps);
	}

	private void findIndels() {
		indel=new Vector();
		StringBuffer[] tempSeq=new StringBuffer[rawAlignment.getSequenceCount()];
		char c0,c1,cc;
		int rawNumSites=rawAlignment.getSiteCount();
		for(int i=0; i<rawAlignment.getSequenceCount(); i++)
			{tempSeq[i]=new StringBuffer();}
		for(int j=1; j<rawNumSites; j++)
				{for(int i=0; i<rawAlignment.getSequenceCount(); i++)
					{
					if(rawAlignment.getData(i,j)==GAP)
						{
						if(rawAlignment.getData(i,j-1)!=GAP)  //this is the beginning of a gap
							{int p=j+1;
							while((rawAlignment.getData(i,p)==GAP)&&(p<(rawNumSites-1))) {p++;}
							IndelPosition currIndel=new IndelPosition(j,p-1,anchored);
							if(!indel.contains(currIndel))
								{indel.addElement(currIndel);
								scoreIndelsInAllSequence(currIndel, tempSeq);
								}
							}
						}//end c1==gap
					}//end of i
				}//end of j
		NumericDataType theNumericDataType=new NumericDataType();
		for (int i = 0; i < sequences.length; i++) {
			sequences[i] = tempSeq[i].toString();}
	}

	private void scoreIndelsInAllSequence(IndelPosition currIndel, StringBuffer[] tempSeq) {
		int j, forwardSize, backwardSize,size;
		int nSites=rawAlignment.getSiteCount()-1;
		NumericDataType theNumericDataType=new NumericDataType();
		if(anchored)
			//this finds anchored indels, indels must only share a flanking end
			{for(int i=0; i<rawAlignment.getSequenceCount(); i++)
				{forwardSize=backwardSize=0;
				if((rawAlignment.getData(i,currIndel.start-1)!=GAP)&&(rawAlignment.getData(i,currIndel.start)==GAP))
					{//System.out.println("currIndel s="+currIndel.start+" e="+currIndel.end+" i="+i+" shares the start at "+currIndel.start);
					j=currIndel.start;
					while((rawAlignment.getData(i,j)==GAP)&&(j<nSites))
						{forwardSize++; j++;}
					if(rawAlignment.getData(i,j)=='?')
						{tempSeq[i].append('?'); continue;}
						else {tempSeq[i].append(theNumericDataType.getNumericCharFromNumericIndex(forwardSize));}
					}
				else
				if((rawAlignment.getData(i,currIndel.end+1)!=GAP)&&(rawAlignment.getData(i,currIndel.end)==GAP))
					{//System.out.println("currIndel s="+currIndel.start+" e="+currIndel.end+" i="+i+" shares the end at "+currIndel.end);
					j=currIndel.end;
					while((rawAlignment.getData(i,j)==GAP)&&(j>0))
						{backwardSize++; j--;}
					if(rawAlignment.getData(i,j)=='?')
						{tempSeq[i].append('?'); continue;}
						else {tempSeq[i].append(theNumericDataType.getNumericCharFromNumericIndex(backwardSize));}
					}
				else
				if((rawAlignment.getData(i,currIndel.start-1)==GAP)||(rawAlignment.getData(i,currIndel.end+1)==GAP)||
					(rawAlignment.getData(i,currIndel.start)=='?')||(rawAlignment.getData(i,currIndel.end)=='?'))
					{//System.out.println("currIndel s="+currIndel.start+" e="+currIndel.end+" i="+i+" is beyond gap");
					tempSeq[i].append('?'); continue;}
				else
					{//System.out.println("currIndel s="+currIndel.start+" e="+currIndel.end+" i="+i+" has no gap");
					tempSeq[i].append(theNumericDataType.getNumericCharFromNumericIndex(0));}
				}
			}
		else
			//This finding perfectly matched indels
			{for(int i=0; i<rawAlignment.getSequenceCount(); i++)
				{forwardSize=backwardSize=0;
				if((rawAlignment.getData(i,currIndel.start-1)==GAP)&&(rawAlignment.getData(i,currIndel.end+1)==GAP))
					//if the GAP extend beyond both then this is not the same gap
					{tempSeq[i].append('?'); continue;}
				j=currIndel.start;
				while((rawAlignment.getData(i,j)==GAP)&&(j<nSites))
					{forwardSize++; j++;}
				if(rawAlignment.getData(i,j)=='?')
					{tempSeq[i].append('?'); continue;}  //if missing data within set to missing
				j=currIndel.end;
				while((rawAlignment.getData(i,j)==GAP)&&(j>0))
					{backwardSize++; j--;}
				if(rawAlignment.getData(i,j)=='?')
					{tempSeq[i].append('?'); continue;}
				size=(forwardSize!=backwardSize)?0:backwardSize;
				tempSeq[i].append(theNumericDataType.getNumericCharFromNumericIndex(size));
				}
			}
	}

	private void init(IdGroup group, String gaps) {
				numSeqs = sequences.length;
				numSites = sequences[0].length();

				idGroup = group;

				AlignmentUtils.estimateFrequencies(this);
	}

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site) {
		return sequences[seq].charAt(site);
		}

	 IndelPosition getIndelPosition(int i) {
		return (IndelPosition)indel.elementAt(i);
		}

}

class IndelPosition  implements java.io.Serializable {
	int start,end;
	boolean anchored;

	IndelPosition(int s, int e, boolean a) {
		start=s;
		end=e;
		anchored=a;
	}

		public boolean equals(Object obj) {
	if (obj instanceof IndelPosition) {
			IndelPosition pt = (IndelPosition)obj;
						if(anchored)
							{return (start == pt.start) || (end == pt.end);}
						else
				{return (start == pt.start) && (end == pt.end);}
	}
	return super.equals(obj);
		}
}
