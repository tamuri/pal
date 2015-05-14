// GapBalancedAcidAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import pal.datatype.*;
import pal.misc.*;

/**
 * Creates a "Gap-Balanced" alignment.
 *
 * @version $Id: GapBalancedAlignment.java,v 1.14 2003/04/10 05:53:47 matt Exp $
 *
 * @author Matthew Goode
 */
public class GapBalancedAlignment extends AbstractAlignment implements java.io.Serializable{

	final static int DEFAULT_CODON_LENGTH = 3; /** Just in case we find some Alien DNA */
	//
	// Private stuff
	//
	private char[][] data_;

	//
	// Serialization code
	//

	private static final long serialVersionUID = -1042105658996999207L;

	//serialver -classpath ./classes pal.alignment.AbstractAlignment
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(data_);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				data_ = (char[][])in.readObject();
				break;
			}
		}
	}


	//
	// Public stuff
	//

	/**
	 * The standard GapBalanced constructor
	 *
	 * @param Alignment on which to based this gap balanced alignment
	 * @param the estimated startingCodonPosition of the alignment
	 */
	public GapBalancedAlignment(Alignment base, int startingCodonPosition, boolean alignGap) {
		calculateData(base,startingCodonPosition,alignGap, DEFAULT_CODON_LENGTH);
	}
	/**
	 * GapBalanced power user constructor
	 *
	 * @param Alignment on which to based this gap balanced alignment
	 * @param startingCodonPosition the starting codon position of the alignment
	 * @param codonLength the length of a codon (to make things general,
	 * - the author is a Computer Scientist)
	 * @note Gaps, are "aligned"
	 */
	public GapBalancedAlignment(Alignment base, int startingCodonPosition, int codonLength) {
		calculateData(base,startingCodonPosition,true, codonLength);
	}
	/**
	 * GapBalanced power user constructor
	 *
	 * @param Alignment on which to based this gap balanced alignment
	 * @param startingCodonPosition the starting codon position of the alignment
	 * @param alignGap sometimes a large cap may occur in the middle of a sequence's codon. If this is true than no
	 * columns can match up in this area (it's hard to explain - for safety choose true!)
	 * @param codonLength the length of a codon (to make things general,
	 * - the author is a Computer Scientist)
	 */
	public GapBalancedAlignment(Alignment base, int startingCodonPosition,boolean alignGap, int codonLength) {
		calculateData(base,startingCodonPosition,alignGap, codonLength);
	}


	/** Generates Alignment information by removing sites that have misaligned codon positions */
	private void calculateData(Alignment base, int startingCodonPosition, boolean alignGap, int codonLength) {
		GapIterator gi = new GapIterator(base,startingCodonPosition, alignGap, codonLength);
		gi.processAllSites();
		data_ = gi.getData();


		this.numSeqs = gi.getNumberOfSequences();
		this.numSites = gi.getNumberOfAcceptedSites();
		this.idGroup = new SimpleIdGroup(base);
		setDataType(base.getDataType());


	}

	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return data_[seq][site];
	}
}


/** An statefull algorithmic solution.
*/
class GapIterator {

	Alignment base_;
	DataType dataType_;
	int startingCodonPosition_;

	int numberOfSites_ = -1;
	int numberOfSequences_ = -1;

	boolean[] siteAcceptance_ = null;
	int numberOfAcceptedSites_ = -1;
	int[] currentSequenceCodonPosition_ = null;
	int[] gapCount_ = null;
	int currentSite_ = -1;

	int codonLength_ = -1;

	int currentCodonPosition_ = -1;
	int[] currentCodonSites_;
	int numberOfCurrentCodonSites_ = -1;

	transient boolean acceptSite_ = false;
	transient int predominateCodonPosition_ = -1;
	boolean alignGaps_;
	public GapIterator(Alignment base, int startingCodonPosition, boolean alignGaps, int codonLength ) {
		this.base_ = base;
		this.dataType_ = base.getDataType();
		this.alignGaps_ = alignGaps;
		this.startingCodonPosition_ = startingCodonPosition;
		this.codonLength_ = codonLength;
		setup();
		reset();

	}

	private void setup() {
		this.numberOfSites_ = base_.getSiteCount();
		this.numberOfSequences_ = base_.getSequenceCount();
		siteAcceptance_ = new boolean[numberOfSites_];
		currentSequenceCodonPosition_ = new int[numberOfSequences_];
		gapCount_ = new int[numberOfSequences_];
		currentCodonSites_ = new int[codonLength_];
	}

	public void reset() {
		for(int i = 0 ; i < siteAcceptance_.length ; i++) {
			siteAcceptance_[i] = false;
		}
		numberOfAcceptedSites_ = 0;
		for(int i = 0 ; i < currentSequenceCodonPosition_.length ; i++) {
			currentSequenceCodonPosition_[i] = startingCodonPosition_;
			gapCount_[i] = startingCodonPosition_;
		}
		currentSite_ = 0;
		currentCodonPosition_ = startingCodonPosition_;
	}

	public final void processAllSites() {
		while(hasMoreSites()) {
			processAnotherSite();
		}
	}

	public boolean hasMoreSites() {
		return (currentSite_ < numberOfSites_);
	}

	public int getCurrentSite() {
		return currentSite_;
	}

	/** Proccess a site and sets up instance variables acceptSite_, predominateCodonPosition_, allGaps_
	*/
	private synchronized void processSite(int siteNumber) {
		acceptSite_ = true;
		predominateCodonPosition_ = -1;
		for(int currentSequence = 0 ; currentSequence < numberOfSequences_ ; currentSequence++) {
			char c = base_.getData(currentSequence,currentSite_);
			if(!dataType_.isUnknownChar(c)) {
				predominateCodonPosition_ = currentSequenceCodonPosition_[currentSequence];
				break;
			}
		}
		if(predominateCodonPosition_<0) {
			acceptSite_ = false;
			return;
		}
		for(int currentSequence = 0 ; currentSequence < numberOfSequences_ ; currentSequence++) {
			char c = base_.getData(currentSequence,currentSite_);

			if(!dataType_.isUnknownChar(c)) {
				if(currentSequenceCodonPosition_[currentSequence]!=predominateCodonPosition_) {
					acceptSite_ = false;
				}
				currentSequenceCodonPosition_[currentSequence] = (currentSequenceCodonPosition_[currentSequence]+1)%codonLength_;
				gapCount_[currentSequence] = currentSequenceCodonPosition_[currentSequence];

			} else {
				if((alignGaps_&&currentSequenceCodonPosition_[currentSequence]!=0)||
					(gapCount_[currentSequence]<codonLength_&&
						gapCount_[currentSequence]!=predominateCodonPosition_)
				) {
					acceptSite_ = false;
				}
				gapCount_[currentSequence]++;
			}

		}
	}

	private synchronized void acceptCurrentCodon() {
		for(int i = 0 ; i < 3 ; i ++ ) {
			siteAcceptance_[currentCodonSites_[i]] = true;
		}
		numberOfAcceptedSites_+=3;
		removeCurrentCodon();
	}

	private synchronized void removeCurrentCodon() {
		currentCodonPosition_ = 0;
	}
	public synchronized void processAnotherSite() {

		processSite(currentSite_);
		if(acceptSite_) {
			if(predominateCodonPosition_!=currentCodonPosition_) {  // If things don't line up we remove the related sites...
				acceptSite_=false;
				removeCurrentCodon();
			} else if(predominateCodonPosition_>=0) {
				currentCodonSites_[currentCodonPosition_] = currentSite_;
				currentCodonPosition_ = currentCodonPosition_+1;
				if(currentCodonPosition_==3) {
					acceptCurrentCodon();
				}
			}
		} else {
			if(predominateCodonPosition_>=0) {
				removeCurrentCodon();
			}
		}
		currentSite_++;
	}

	public int getNumberOfSequences() {
		return numberOfSequences_;
	}

	public int getNumberOfAcceptedSites() {
		return numberOfAcceptedSites_;
	}

	public synchronized char[][] getData() {
		char[][] data = new char[numberOfSequences_][numberOfAcceptedSites_];
		int currentAcceptedSiteIndex = 0;
		for(int j = 0 ; j < numberOfSites_ ; j++) {
			if(siteAcceptance_[j]) {
				for(int i = 0 ; i < numberOfSequences_; i++) {
					data[i][currentAcceptedSiteIndex] = base_.getData(i,j);
				}
				currentAcceptedSiteIndex++;
			}
		}
		return data;
	}


}
