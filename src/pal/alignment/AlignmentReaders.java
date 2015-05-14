// AlignmentReaders.java
//
// (c) 1999-2003 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;
import java.util.*;

import pal.datatype.*;
import pal.misc.*;

/**
 *Temporary class for reading alignments... will fix up structure some time soon!
 *
 * @version $Id: AlignmentReaders.java,v 1.11 2004/10/14 02:01:42 matt Exp $
 *
 * @author Matthew Goode
 */


public final class AlignmentReaders {

	private static final String WHITESPACE = " \r\n\t";
	/**
	 * Read an a set of unaligned Fasta sequences
	 */
	public static final Alignment readFastaSequences(Reader r, DataType dt) throws IOException {
		String sequenceName = "Unnamed";
		Vector sequences = new Vector();
		String line;
		StringBuffer currentSequence = new StringBuffer();
		BufferedReader br = new BufferedReader(r);
		while((line = br.readLine())!=null) {
			line = line.trim();
			if(line.length()>0) {
				if(line.startsWith(">")) {
					if(currentSequence.length()!=0) {
						sequences.addElement(new ConstructionSequence(sequenceName,currentSequence.toString(),dt));
						currentSequence.setLength(0);
					}
					sequenceName = line.substring(1).trim();

				} else {
					currentSequence.append(remove(line,WHITESPACE));
				}
			}

		}
		if(currentSequence.length()>0) {
			sequences.addElement(new ConstructionSequence(sequenceName,currentSequence.toString(),dt));
		}
		ConstructionSequence[] seqs = new ConstructionSequence[sequences.size()];
		sequences.copyInto(seqs);
		return
			new UnalignedAlignment(
				ConstructionSequence.getNames(seqs),
				ConstructionSequence.getSequences(seqs),
				dt
			);
	}
	private static final String readNextNonEmptyLine(BufferedReader br) throws IOException {
	  String line = br.readLine();
		if(line==null) {
			return null;
		}
		line = line.trim();
		while(line.length()==0) {

			line = br.readLine();
			if(line==null) {  return null;		}
			line = line.trim();
		}
		return line;
	}
	/**
	 * Read an a set of unaligned Fasta sequences
	 */
	public static final Alignment readNewLineSeperatedSequences(Reader r, DataType dt) throws IOException {
		String sequenceName = "Unnamed";
		ArrayList sequences = new ArrayList();
		String line;
		StringBuffer currentSequence = new StringBuffer();
		BufferedReader br = new BufferedReader(r);
		String name = null;

		while((line = readNextNonEmptyLine(br))!=null) {
			if(name==null) {
				name = line;
			} else {
				sequences.add(new ConstructionSequence(name,line,dt));
				name = null;
			}
		}
		ConstructionSequence[] seqs = new ConstructionSequence[sequences.size()];
		sequences.toArray(seqs);
		return
			new UnalignedAlignment(
				ConstructionSequence.getNames(seqs),
				ConstructionSequence.getSequences(seqs),
				dt
			);
	}
	/**
	 * Read an alignment in phylip/clustal/simple format. Handles interleaved/sequential - with the name repeated, or with the name only given once for each sequence
	 * Aims to be as general as possible (possibly causeing problems with some formats).
	 */
	public static final Alignment readPhylipClustalAlignment(Reader r, DataType dt) throws AlignmentParseException, IOException {
		AlignmentReceiver.SingleReceiver sr = new AlignmentReceiver.SingleReceiver();
		readPhylipClustalAlignment(r,dt,sr);
		return sr.getLastReceivedAlignment();
	}
	/**
	 * Read an alignment in phylip/clustal/simple format. Handles interleaved/sequential - with the name repeated, or with the name only given once for each sequence
	 * Aims to be as general as possible (possibly causeing problems with some formats).
	 */
	public static final Alignment[] readAllPhylipClustalAlignments(Reader r, DataType dt) throws AlignmentParseException, IOException {
		AlignmentReceiver.BucketReceiver br = new AlignmentReceiver.BucketReceiver();
		readPhylipClustalAlignment(r,dt,br);
		return br.getReceivedAlignments();
	}

	private static final boolean processWhiteSpaceStartingLine(String line, AlignmentBuilder builder) {
		String[] components = toWords(line);
		if((builder.hasSequences())&&(!Character.isDigit(components[0].charAt(0)))&&
			((components.length==1)||(compareLength(components,0,components.length-1)==10))
		){
			if(line.indexOf('*')<0) {
				builder.appendToShortest(concat(components));
				return false;
			}
		} //End of If
		if(components.length==2) {
			try{
				int sequences = Integer.parseInt(components[0]);
				int sites = Integer.parseInt(components[1]);
				builder.setSize(sequences,sites);
				return true;
			} catch(NumberFormatException e) { return false; }
		}
		return false;
	}
	/**
	 * @return true if a delimiter line
	 */
	private static final boolean processNormalLine(String line, AlignmentBuilder builder) throws AlignmentParseException {
		//First character is not a space
		String[] components = toWords(line);
		if(components.length==1) {
			if(components[0].length()>10) {

			  if(components[0].length()==builder.getExpectedSequenceSegmentSize()) {
				  builder.appendToShortest(components[0]);
				} else {
					builder.append( components[0].substring( 0, 10 ), components[0].substring( 10 ) );
				}
				return false;
			}
			return true;
		}
		String name = components[0];
		String sequenceSegment = concat(components,1);

		if(name!=null) {
			if(components.length==2) {
				try {
					builder.setSize(Integer.parseInt(name), Integer.parseInt(sequenceSegment));
					return true;
				} catch(NumberFormatException e) { }
			}
			builder.append(name,sequenceSegment);
			return false;
		}
		return true;
//			if(cs!=null) {
//				ConstructionSequence first = (ConstructionSequence)sequences.firstElement();
//				if(cs==first) {
//					cs.appendSequence(sequenceSegment, dt);
//				} else {
//					cs.appendSequence(sequenceSegment, dt, first);
//
//				}
//			}
	}
	/**
	 * @ return true if a delimiter
	 */
	private static final boolean processLine(String line, AlignmentBuilder builder) throws AlignmentParseException {
		if(Character.isWhitespace(line.charAt(0))) {
			return processWhiteSpaceStartingLine(line,builder);
		} else {
			return processNormalLine(line,builder);
		}
	}
	/**
	 * Read an alignment in phylip/clustal/simple format. Handles interleaved/sequential - with the name repeated, or with the name only given once for each sequence
	 * Aims to be as general as possible (possibly causeing problems with some formats).
	 */
	public static final void readPhylipClustalAlignment(Reader r, DataType dt, AlignmentReceiver receiver) throws AlignmentParseException, IOException {
		BufferedReader br = new BufferedReader(r);
		String line = nextNonEmptyLine(br);
		AlignmentBuilder builder = new AlignmentBuilder(dt);
		if(line==null) { throw new IOException("File contains no data!"); }
		boolean foundRepeat = false;
		boolean firstAlignment = true;
		while(line!=null) {
			boolean isDelimiter = false;
			String trimmedLine = line.trim();;
			if(trimmedLine.startsWith("CLUSTAL")) {	isDelimiter = true;		}
			if(trimmedLine.startsWith(">")) { isDelimiter = true; }
//			if(Character.isWhitespace(line.charAt(0))) { isDelimiter = true; }
			if(trimmedLine.toLowerCase().startsWith("#nexus")) {
				throw new AlignmentParseException("Invalid format : can't read NEXUS files!");
			}
			//Line relies on lazy evaluation... shocking!
			isDelimiter = isDelimiter||processLine(line,builder);
			if(isDelimiter) {
				Alignment a = builder.generateAlignment();
				if(a!=null) {
					receiver.newAlignment(a);
				}
				builder.reset(dt);
			}
			line = nextNonEmptyLine(br);
		}
		Alignment a = builder.generateAlignment();
		if(a!=null) {
			receiver.newAlignment(a);
		}
	}
	/**
	 * The current state of an alignment building process
	 */
	private static final class AlignmentBuilder {
		private final Vector sequences_ = new Vector();
		private DataType dt_;
		private boolean foundRepeat_ = false;
		private int suggestedNumberOfSequences_;
		private int suggestNumberOfSites_;
		private int expectedSequenceSegmentSize_;

		public AlignmentBuilder(DataType dt) {
			reset(dt);
		}
		public void setSize(int numberOfSequences, int numberOfSites) {
			this.suggestedNumberOfSequences_ = numberOfSequences;
			this.suggestNumberOfSites_ = numberOfSites;

			this.expectedSequenceSegmentSize_ = numberOfSites;
		}
		public boolean isFoundRepeat() {
		  return foundRepeat_;
		}
		public void appendToShortest(String sequenceSegment) {
			ConstructionSequence cs = ConstructionSequence.findShortest(sequences_);
			cs.appendSequence(sequenceSegment,dt_);

			this.expectedSequenceSegmentSize_ = sequenceSegment.length();
//			foundRepeat_ = true;
		}
		public int getExpectedSequenceSegmentSize() { return expectedSequenceSegmentSize_;}
		/**
		 * @return true if new sequence created
		 */
		public void append(String sequenceName, String sequence) throws AlignmentParseException {
			ConstructionSequence cs = ConstructionSequence.getConstructionSequence(sequences_,sequenceName);
			if(cs==null) {
				cs = new ConstructionSequence(sequenceName,sequence,dt_);
				sequences_.addElement(cs);

				if(foundRepeat_) {
					throw new AlignmentParseException("New sequence found after interleaved sequence - cound mean two sequences with same name ("+sequenceName+")");
				}
			} else {
				cs.appendSequence(sequence,dt_);
				foundRepeat_ = true;

			}
			this.expectedSequenceSegmentSize_ = sequence.length();
		}

		public void reset(DataType dt) {
			this.dt_ = dt;
			this.foundRepeat_ = false;
			this.expectedSequenceSegmentSize_ =-1;
			sequences_.removeAllElements();
		}
		public final boolean hasSequences() { return sequences_.size()!=0; }

		public Alignment generateAlignment() throws AlignmentParseException {
			if(sequences_.size()==0) { return null; }
			ConstructionSequence[] seqs = new ConstructionSequence[sequences_.size()];
			sequences_.copyInto(seqs);
			if(ConstructionSequence.isAllSameLength(seqs)) {
				ConstructionSequence.fillInDots(seqs);
				return new PhylipClustalAlignment(ConstructionSequence.getNames(seqs),	ConstructionSequence.getSequences(seqs),dt_);
			}
			throw new AlignmentParseException("Not all sequences are of the same length");
		}
	}

	private static final String remove(String target, String lint) {
		StringBuffer newString = new StringBuffer();
		for (int i = 0; i < target.length(); i++) {
			if (lint.indexOf(target.charAt(i)) == -1) {
				newString.append(target.charAt(i));
			}
		}
		return new String(newString);
	}

	private static final String nextNonEmptyLine(BufferedReader r) throws IOException {
		String line = r.readLine();
		if(line==null) {
			return null;
		}
		while(line!=null&&line.trim().length()==0) {
			line = r.readLine();
			if(line==null) {
				return null;
			}
		}
		return line;
	}


	private static final int maxLength(String[] ss) {
		int count = 0;
		for(int i = 0 ; i < ss.length ; i++) {
			count = Math.max(count,ss[i].length());
		}
		return count;
	}
	private static final String pad(String s, char toPadWith, int length) {
		StringBuffer sb = new StringBuffer(length);
		sb.append(s);
		while(sb.length()<length) {
			sb.append(toPadWith);
		}
		return sb.toString();
	}
	private static final String[] pad(String[] ss, char toPadWith, int length)  {
		String[] result = new String[ss.length];
		for(int i = 0 ; i < ss.length ; i++) {
			result[i] = pad(ss[i],toPadWith,length);
		}
		return result;
	}

	/**
	 * Concatenate a set of words into one string
	 */
	private static final String concat(String[] words) {
		return concat(words,0,words.length);
	}
	/**
	 * @param text to check if is a number
	 * @return true if text represents an integer number
	 */
	private static final boolean isNumber(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	/**
	 * Concatenate a set of words into one string
	 * @param start the starting index into words
	 */
	private static final String concat(String[] words, int start) {
		return concat(words,start,words.length);
	}

	/**
	 * Concatenate a set of words into one string
	 * @param start the starting index into words
	 * @param end the ending index into words
	 */
	private static final String concat(String[] words, int start, int end) {
		StringBuffer sb =new StringBuffer();
		for(int i = start ; i < end ; i++) { sb.append(words[i]); }
		return sb.toString();
	}

	/** Takes a string of text which may contain new line characters (ie '\n')
			and returns an array of strings where each string is a single word with no
			white space
	*/
	private static final String[] toWords(String text) {
		String[] words = new String[countNonWhiteSpaceBlocks(text)];
		int count = 0;
		boolean inBlock = false;
		int wordStart = 0;
		for(int i = 0 ; i < text.length() ; i++) {
			if(WHITESPACE.indexOf(text.charAt(i))<0) {
				if(!inBlock) {
					wordStart = i;
					inBlock = true;
				}
			} else {
				if(inBlock) {
					words[count] = text.substring(wordStart,i);
					count++;
				}
				inBlock = false;
			}
		}
		if(inBlock) {
			words[count] = text.substring(wordStart);
			count++;
		}
		return words;
	}
	/**
	 *  Returns the number of continuous non white space (space, tabs, or newline characters) blocks in a string
	 *  @param s - the string to check
		*/
	private static final int countNonWhiteSpaceBlocks(String s) {
		int count = 0;
		boolean inBlock = false;
		for(int i = 0 ; i < s.length() ; i++) {
			if(WHITESPACE.indexOf(s.charAt(i))<0) {
				if(!inBlock) {
					inBlock = true;
					count++;
				}
			} else {
				inBlock = false;
			}
		}
		return count;
	}

	/**
	 * @return -1 if all words not the same length, else return length
	 *
	 */
	private static final int compareLength(String[] words) {
		return compareLength(words,0,words.length);
	}/**
	 * @return -1 if all words not the same length, else return length
	 *
	 */
	private static final int compareLength(String[] words, int start) {
		return compareLength(words,start,words.length);
	}
	/**
	 * @return -1 if all words not the same length, else return length
	 *
	 */
	private static final int compareLength(String[] words, int start, int end) {
		int length = words[start].length();
		for(int i = start+1 ; i < end ; i++) {
			if(words[i].length()!=length) { return -1; }
		}
		return length;
	}

// ============================================================================
// === Static classes

	private static class StringAlignment implements Alignment {
		private  static final long serialVersionUID = 2225713077370547221l;
		String[] sequences_;
		Identifier[] ids_;
		int siteCount_;
		DataType dt_;
		protected StringAlignment(String[] names, String[] sequences, DataType dt) {
			siteCount_ = maxLength(sequences);
			this.sequences_ = pad(sequences,Alignment.GAP,siteCount_);
			this.ids_ = Identifier.getIdentifiers(names);
			this.dt_ = dt;
		}
		/** sequence alignment at (sequence, site) */
			public char getData(int seq, int site) {
				return sequences_[seq].charAt(site);
			}

			public int getSiteCount() { return siteCount_; }
			public int getSequenceCount() { return sequences_.length; }
			public DataType getDataType() { return dt_; }
			public void setDataType(DataType dataType) { dt_ = dataType; }
			public String getAlignedSequenceString(int sequence) {	return sequences_[sequence];	}
			public double[] getFrequency() { return null; }
			public void setFrequency(double[] frequencies) {  }
			public int getIdCount() { return ids_.length; }
			public Identifier getIdentifier(int i) { return ids_[i]; }
			public void setIdentifier(int i, Identifier id) { ids_[i] = id;	}
			public int whichIdNumber(String s) { return IdGroup.Utils.whichIdNumber(this,s); }
			public String toString() {
				StringBuffer sb = new StringBuffer();
				sb.append("\t");
				sb.append(getSequenceCount());
				sb.append(" ");
				sb.append(getSiteCount());
				sb.append("\n");
				for(int i = 0 ; i < ids_.length ; i++) {
					sb.append(ids_[i].getName());
					sb.append("\t");
					sb.append(sequences_[i]);
					sb.append("\n");
				}
				return sb.toString();
			}

	} //End of StringAlignmnet

	public static class UnalignedAlignment extends StringAlignment {
		public UnalignedAlignment(String[] names, String[] sequences, DataType dt) {
			super(names,sequences,dt);
		}
	}
	public static class PhylipClustalAlignment extends StringAlignment {
		public PhylipClustalAlignment(String[] names, String[] sequences, DataType dt) {
			super(names,sequences,dt);
		}
	}


	private static class ConstructionSequence {
		private final String name_;
		private String sequence_ = null;
		public ConstructionSequence(String name) {
			this.name_ = name;
		}
		public ConstructionSequence(String name, String sequence, DataType dt) {
			this.name_ = name;
			this.sequence_ = DataType.Utils.getPreferredChars(sequence,dt,true);
		}
		public void appendSequence(String sequence, DataType dt) {
			if(this.sequence_==null) {
				this.sequence_ = DataType.Utils.getPreferredChars(sequence,dt);
			} else {
				this.sequence_+=DataType.Utils.getPreferredChars(sequence,dt);
			}
		}
		public String toString() {
			return name_+":"+sequence_;
		}
		public void appendSequence(String sequenceSegment, DataType dt, ConstructionSequence first) {
			String firstSequence = first.sequence_;
			int start;
			if(this.sequence_==null) {
				this.sequence_ = DataType.Utils.getPreferredChars(sequenceSegment,dt);
				start = 0;
			} else {
				start = sequence_.length();
				this.sequence_+=DataType.Utils.getPreferredChars(sequenceSegment,dt);
			}
			char[] data= sequence_.toCharArray();
			for(int i = 0 ; i < sequenceSegment.length(); i++) {
				if(sequenceSegment.charAt(i)=='.') {
					data[i+start] = firstSequence.charAt(i+start);
				}
			}
			sequence_ = new String(data);
		}
		public final void fillInDotsBasedOn(String template) {
			char[] newSequence = new char[sequence_.length()];
			for(int i = 0; i < template.length() ;i++) {
				char c = sequence_.charAt(i);
				if(c=='.') {
					c = template.charAt(i);
				}
				newSequence[i] = c;
			}
			this.sequence_ = new String(newSequence);
		}
		public final int getSequenceLength() {
			return sequence_==null ? 0 : sequence_.length();
		}
		public final boolean hasSameName(String n) {
			return name_.equals(n);
		}
		public static final String[] getNames(ConstructionSequence[] seqs) {
			String[] ss = new String[seqs.length];
			for(int i = 0 ; i < ss.length ; i++) {
				ss[i] = seqs[i].name_;
			}
			return ss;
		}
		public static final String[] getSequences(ConstructionSequence[] seqs) {
			String[] ss = new String[seqs.length];
			for(int i = 0 ; i < ss.length ; i++) {
				ss[i] = seqs[i].sequence_;
			}
			return ss;
		}
		/**
		 * Fills in the ditto dots things...
		 */
		public static final void fillInDots(ConstructionSequence[] seqs) {
			String template = seqs[0].sequence_;
			for(int i = 1 ; i < seqs.length ; i++) {
				seqs[i].fillInDotsBasedOn(template);
			}
		}
		public static final ConstructionSequence getConstructionSequence(Vector sequences, String sequenceName) {
			ConstructionSequence cs = null;
			for(int i = 0 ; i < sequences.size() ;i++) {
				ConstructionSequence currentCS = (ConstructionSequence)sequences.elementAt(i);
				if(currentCS.hasSameName(sequenceName)) {
					return currentCS;
				}
			}
			return null;
		}
		public static final ConstructionSequence findShortest(final Vector sequences) {
			ConstructionSequence cs = null;
			int minimumLength = -1;
			for(int i = 0 ; i < sequences.size() ;i++) {
				final ConstructionSequence currentCS = (ConstructionSequence)sequences.elementAt(i);
				final int currentLength = currentCS.getSequenceLength();
				if((minimumLength<0)||(currentLength<minimumLength)) {
					cs = currentCS;
					minimumLength = currentLength;
				}
			}
			return cs;
		}
		public static final boolean isAllSameLength(final ConstructionSequence[] seqs) {
			int length = seqs[0].getSequenceLength();
			for(int i = 1 ; i < seqs.length ;i++) {
				if(seqs[i].getSequenceLength()!=length) {
					return false;
				}
			}
			return true;
		}

	}

}