// SimpleAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.alignment;

import pal.io.*;
import pal.datatype.*;
import pal.misc.*;

import java.io.*;


/**
 * An alignment class that can be efficiently constructed
 * from an array of strings.
 *
 * @version $Id: SimpleAlignment.java,v 1.26 2003/08/16 23:48:26 matt Exp $
 *
 * @author Alexei Drummond
 * @note
 *     <ul>
 *       <li> 14 August 2003 - Removed estimate frequencies stuff (removed a constructor with pointless boolean)
 *     </ul>
 */
public class SimpleAlignment extends AbstractAlignment
{
	//
	// Public stuff
	//

	/** The sequences */
	String[] sequences;

	//
	// Serialization code
	//
	private static final long serialVersionUID=4303224913340358191L;

	//serialver -classpath ./classes pal.alignment.SimpleAlignment
	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		out.writeByte(1); //Version number
		out.writeObject(sequences);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException{
		byte version = in.readByte();
		switch(version) {
			default : {
				sequences = (String[])in.readObject();
				break;
			}
		}
	}

	/**
	 * parameterless constructor.
	 */
	public SimpleAlignment() {}

	/**
	 * Clone constructor.
	 */
	public SimpleAlignment(Alignment a) {
		this(a,(LabelMapping)null);
	}
	/**
	 * Clone constructor.
	 */
	public SimpleAlignment(Alignment a, LabelMapping lm) {

		String[] sequences = new String[a.getIdCount()];
		setDataType(a.getDataType());
		for (int i = 0; i < sequences.length; i++) {
			sequences[i] = a.getAlignedSequenceString(i);
		}

		if(lm==null) {
			init(new SimpleIdGroup(a), sequences, GAPS);
		} else {
			init(lm.getMapped(a), sequences, GAPS);
		}
	}
	/**
	 * Clone constructor.
	 * @param sequenceToIgnore Will not copy across specified sequence
	 */
	public SimpleAlignment(Alignment a, int sequenceToIgnore) {
		int numberOfOriginalSequences = a.getIdCount();
		setDataType(a.getDataType());
		String[] sequences =
			(
				sequenceToIgnore<0||sequenceToIgnore>=numberOfOriginalSequences ?
					new String[numberOfOriginalSequences] :
					new String[numberOfOriginalSequences-1]
				);
		int index = 0;
		for (int i = 0; i < numberOfOriginalSequences ; i++) {
			if(i!=sequenceToIgnore) {
				sequences[index++] = a.getAlignedSequenceString(i);
			}
		}

		init(new SimpleIdGroup(a,sequenceToIgnore), sequences, GAPS);
	}

	public SimpleAlignment(Identifier[] ids, String[] sequences, String gaps, DataType dt) {
		setDataType(dt);
		init(new SimpleIdGroup(ids), sequences, gaps);
	}
	public SimpleAlignment(IdGroup ids, String[] sequences, DataType dt) {
		this(ids,sequences,null,dt);
	}

	public SimpleAlignment(IdGroup ids, String[] sequences, String gaps, DataType dt) {
		setDataType(dt);
		init(new SimpleIdGroup(ids), sequences, gaps);
	}

	public SimpleAlignment(Identifier[] ids, String[] sequences,DataType dt) {
		this(ids,sequences,null,dt);
	}

	/**
	 * @param cSequences sequences as character matrix (assumes rectangular), laid out as cSequences[sequence][site]
	 */
	public SimpleAlignment(IdGroup group, char[][] cSequences, DataType dt) {
		this(group,cSequences,GAPS,dt);
	}
	/**
	 * @param cSequences sequences as character matrix (assumes rectangular), laid out as cSequences[sequence][site]
	 */
	public SimpleAlignment(IdGroup group, DataType dt, int[][] sSequences) {
		this(group,DataType.Utils.getChars(sSequences,Alignment.UNKNOWN, Alignment.GAP,dt),""+Alignment.GAP,dt);
	}
	/**
	 * @param cSequences sequences as character matrix (assumes rectangular), laid out as cSequences[sequence][site]
	 */
	public SimpleAlignment(IdGroup group, char[][] cSequences, String gaps, DataType dt) {
		setDataType(dt);
		String[] sequences = new String[cSequences.length];
		for(int i = 0 ; i < sequences.length ; i++) {
			sequences[i] = new String(cSequences[i]);
		}
		init(group, sequences, gaps);
	}

	private void init(IdGroup group, String[] sequences, String gaps) {
		sequences = getPadded(sequences);
		numSeqs = sequences.length;
		numSites = sequences[0].length();

		this.sequences = sequences;
		idGroup = group;

		if (gaps != null) {
			convertGaps(gaps);
		}
	}

	/**
	 * Constructor taking single identifier and sequence.
	 */
	public SimpleAlignment(Identifier id, String sequence, DataType dataType) {

		setDataType(dataType);
		numSeqs = 1;
		numSites = sequence.length();

		sequences = new String[1];
		sequences[0] = sequence;

		Identifier[] ids = new Identifier[1];
		ids[0] = id;
		idGroup = new SimpleIdGroup(ids);
	}

	/**
	 * This constructor combines to alignments given two guide strings.
	 */
	public SimpleAlignment(Alignment a, Alignment b,
		String guide1, String guide2, char gap) {

		sequences = new String[a.getSequenceCount() + b.getSequenceCount()];
		numSeqs = sequences.length;

		for (int i = 0; i < a.getSequenceCount(); i++) {
			sequences[i] = getAlignedString(a.getAlignedSequenceString(i), guide1, gap, GAP);
				}
		for (int i = 0; i < b.getSequenceCount(); i++) {
			sequences[i + a.getSequenceCount()] =
			getAlignedString(b.getAlignedSequenceString(i), guide2, gap, GAP);
		}

		numSites = sequences[0].length();
		idGroup = new SimpleIdGroup(a, b);
	}

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site) {
		return sequences[seq].charAt(site);
	}

	/**
	 * Returns a string representing a single sequence (including gaps)
	 * from this alignment.
	 */
	public String getAlignedSequenceString(int seq) {
		return sequences[seq];
	}

	// PRIVATE STUFF

	private String getAlignedString(String original, String guide,
		char guideChar, char gapChar) {

		StringBuffer buf = new StringBuffer(guide.length());
		int seqcounter = 0;
		for (int j = 0; j < guide.length(); j++) {
			if (guide.charAt(j) != guideChar) {
				buf.append(original.charAt(seqcounter));
				seqcounter += 1;
			} else {
				buf.append(gapChar);
			}
		}
		return new String(buf);
	}
	private static final String getPadded(String s, int length) {
		StringBuffer sb = new StringBuffer();
		sb.append(s);
		for(int i = s.length() ; i < length ; i++) {
			sb.append(Alignment.GAP);
		}
		return sb.toString();
	}
	private static final String[] getPadded(String[] sequences) {
		String[] padded = new String[sequences.length];
		int maxLength = 0;
		for(int i = 0 ; i < sequences.length ; i++) {
			maxLength = Math.max(maxLength,sequences[i].length());
		}
		for(int i = 0 ; i < sequences.length ; i++) {
			padded[i] = getPadded(sequences[i],maxLength);
		}
		return padded;

	}
	/**
	 * Converts all gap characters to Alignment.GAP
	 */
	private void convertGaps(String gaps) {
		for (int i = 0; i < sequences.length; i++) {
			for (int j = 0; j < gaps.length(); j++) {
				sequences[i] = sequences[i].replace(gaps.charAt(j), Alignment.GAP);
			}
		}
	}
}
