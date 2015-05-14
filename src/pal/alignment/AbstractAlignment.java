// AbstractAlignment.java
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
 * abstract base class for any alignment data.
 *
 * @version $Id: AbstractAlignment.java,v 1.7 2003/03/23 00:12:57 matt Exp $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
abstract public class AbstractAlignment implements Alignment, Serializable, IdGroup, Report
{
	//
	// Public stuff
	//

	//
	// Protected stuff
	//
	/** number of sequences */
	protected int numSeqs;

	/** length of each sequence */
	protected int numSites;

	/** sequence identifiers */
	protected IdGroup idGroup;

	/** data type */
	private DataType dataType;

	//
	// Serialization code
	//

	private static final long serialVersionUID = -5197800047652332969L;

	//serialver -classpath ./classes pal.alignment.AbstractAlignment
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(2); //Version number
		out.writeInt(numSeqs);
		out.writeInt(numSites);
		out.writeObject(idGroup);
		out.writeObject(dataType);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			case 1 : {
				numSeqs = in.readInt();
				numSites = in.readInt();
				idGroup = (IdGroup)in.readObject();
				double[] frequencyDummy = (double[])in.readObject();
				dataType = (DataType)in.readObject();
				break;
			}
			default : {
				numSeqs = in.readInt();
				numSites = in.readInt();
				idGroup = (IdGroup)in.readObject();
				dataType = (DataType)in.readObject();
				break;
			}
		}
	}

	public AbstractAlignment(){	}

	// Abstract method

	/** sequence alignment at (sequence, site) */
	abstract public char getData(int seq, int site);

	/**
	 * returns true if there is a gap in the give position.
	 */
	public boolean isGap(int seq, int site) {
		return dataType.isGapChar(getData(seq, site));
	}

	/** Guess data type */
	public void guessDataType()
	{
		dataType = AlignmentUtils.getSuitableInstance(this);
	}
	/**
	 * Same as getDataType().getChar(state)
	 */
	protected final char getChar(int state) {		return dataType.getChar(state); 	}

	/**
	 * Same as getDataType().getState(char)
	 */
	protected final int getState(char c) {		return dataType.getState(c); 	}
	/**
	 * Same as getDataType().isUnknownState(state)
	 */
	protected final boolean isUnknownState(int state) {		return dataType.isUnknownState(state); 	}

	/** Returns the datatype of this alignment */
	public final DataType getDataType()
	{
		return dataType;
	}

	/** Sets the datatype of this alignment */
	public final void setDataType(DataType d)
	{
		dataType = d;
	}

	/** returns representation of this alignment as a string */
	public String toString() {

		StringWriter sw = new StringWriter();
		AlignmentUtils.print(this, new PrintWriter(sw));

		return sw.toString();
	}

	// interface Report

	public void report(PrintWriter out)
	{
		AlignmentUtils.report(this, out);
	}


	/**
	 * Fills a [numsequences][length] matrix with indices.
	 * Each index represents the sequence state, -1 means a gap.
	 */
	public int[][] getStates() {

		int[][] indices = new int[numSeqs][numSites];

		for (int i = 0; i < numSeqs; i++) {
			int seqcounter = 0;

			for (int j = 0; j < numSites; j++) {

				indices[i][j] = dataType.getState(getData(i, j));

				if (indices[i][j] >= dataType.getNumStates()) {
					indices[i][j] = -1;
				}
			}
		}

		return indices;
	}

		/**
	 * Return number of sites in this alignment
	 */
	public final int getLength() {
		return numSites;
	}

	/**
	 * Return number of sequences in this alignment
	 */
	public final int getSequenceCount() {
		return numSeqs;
	}

	/**
	 * Return number of sites for each sequence in this alignment
	 * @note for people who like accessor methods over public instance variables...
	 */
	public final int getSiteCount() {
		return numSites;
	}
	/**
	 * Returns a string representing a single sequence (including gaps)
	 * from this alignment.
	 */
	public String getAlignedSequenceString(int seq) {
		char[] data = new char[numSites];
		for (int i = 0; i < numSites; i++) {
			data[i] = getData(seq, i);
		}
		return new String(data);
	}

	//IdGroup interface
	public Identifier getIdentifier(int i) {return idGroup.getIdentifier(i);}
	public void setIdentifier(int i, Identifier ident) { idGroup.setIdentifier(i, ident); }
	public int getIdCount() { return idGroup.getIdCount(); }
	public int whichIdNumber(String name) { return idGroup.whichIdNumber(name); }

}
