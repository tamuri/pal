// AlignmentUtils.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

import java.io.*;

import pal.datatype.*;
import pal.io.*;
import pal.misc.*;

/**
 * Helper utilities for alignments.
 *
 * @version $Id: AlignmentUtils.java,v 1.29 2004/10/14 02:01:43 matt Exp $
 *
 * @author Alexei Drummond
 *  * @note
 *     <ul>
 *       <li> 14 August 2003 - Changed call to new SimpleAlignment() to reflect change in construtors (refered to not calculating frequencies but that no longer happens anyhow)
 *     </ul>
 */
public class AlignmentUtils {

	static FormattedOutput format = FormattedOutput.getInstance();


	/**
	 *  Report number of sequences, sites, and data type
	 *  @note does not alter alignment state. If data type not defined
	 *  in alignment will find a suitable instance for report but will
	 *  not change alignment!
	 */
	public static void report(Alignment a, PrintWriter out)
	{
		DataType dt = a.getDataType();
		if (dt == null)		{
			dt = getSuitableInstance(a);
		}

		out.println("Number of sequences: " + a.getSequenceCount());
		out.println("Number of sites: " + a.getSiteCount());
		out.println("Data type: " + dt.getDescription() + " data");
	}

	/** print alignment (default format: INTERLEAVED) */
	public static void print(Alignment a, PrintWriter out)
	{
		printInterleaved(a, out);
	}

	/** print alignment (in plain format) */
	public static void printPlain(Alignment a, PrintWriter out) {
		printPlain(a, out, false);
	}

	/** print alignment (in plain format) */
	public static void printPlain(Alignment a, PrintWriter out, boolean relaxed)
	{
		// PHYLIP header line
		out.println("  " + a.getSequenceCount() + " " + a.getSiteCount());

		for (int s = 0; s < a.getSequenceCount(); s++)
		{
			format.displayLabel(out, a.getIdentifier(s).getName(), (relaxed ? 20 : 10));
			out.print("     ");
			printNextSites(a, out, false, s, 0, a.getSiteCount());
			out.println();
		}
	}

	/** print alignment (in PHYLIP SEQUENTIAL format) */
	public static void printSequential(Alignment a, PrintWriter out)
	{
		// PHYLIP header line
		out.println("  " + a.getSequenceCount() + " " + a.getSiteCount() + "  S");

		// Print sequences
		for (int s = 0; s < a.getSequenceCount(); s++)
		{
			int n = 0;
			while (n < a.getSiteCount())
			{
				if (n == 0)
				{
					format.displayLabel(out,
						a.getIdentifier(s).getName(), 10);
					out.print("     ");
				}
				else
				{
					out.print("               ");
				}
				printNextSites(a, out, false, s, n, 50);
				out.println();
				n += 50;
			}
		}
	}


	/** print alignment (in PHYLIP 3.4 INTERLEAVED format) */
	public static void printInterleaved(Alignment a, PrintWriter out)
	{
		int n = 0;

		// PHYLIP header line
		out.println("  " + a.getSequenceCount() + " " + a.getSiteCount());

		// Print sequences
		while (n < a.getSiteCount())
		{
			for (int s = 0; s < a.getSequenceCount(); s++)
			{
				if (n == 0)
				{
					format.displayLabel(out,
						a.getIdentifier(s).getName(), 10);
					out.print("     ");
				}
				else
				{
					out.print("               ");
				}
				printNextSites(a, out, true, s, n, 50);
				out.println();
			}
			out.println();
			n += 50;
		}
	}

	/** Print alignment (in CLUSTAL W format) */
	public static void printCLUSTALW(Alignment a, PrintWriter out)
	{
		int n = 0;

		// CLUSTAL W header line
		out.println("CLUSTAL W multiple sequence alignment");
		out.println();

		// Print sequences
		while (n < a.getSiteCount())
		{
			out.println();
			for (int s = 0; s < a.getSequenceCount(); s++)
			{
				format.displayLabel(out, a.getIdentifier(s).getName(), 10);
				out.print("     ");

				printNextSites(a, out, false, s, n, 50);
				out.println();
			}
			// Blanks in status line are necessary for some parsers)
			out.println("               ");
			n += 50;
		}
	}

	/**
	 * Returns state indices for a sequence.
	 */
	public static final void getAlignedSequenceIndices(Alignment a, int i, int[] indices, DataType dataType, int unknownState) {

		String sequence = a.getAlignedSequenceString(i);

		for (int j = 0; j < a.getSiteCount(); j++) {
			char c = sequence.charAt(j);
			if(dataType.isUnknownChar(c)) {
				indices[j] = unknownState;
			} else {
				indices[j] = dataType.getState(c);
			}
		}
	}

	/**
	 * Unknown characters are given the state of -1
	 */
	public static final int[][] getAlignedStates(Alignment base) {
		return getAlignedStates(base,-1);
	}
	public static final int[][] getAlignedStates(Alignment base, int unknownState) {
		int numberOfSites = base.getSiteCount();
		DataType dt = base.getDataType();
		int[][] sequences = new int[base.getSequenceCount()][base.getSiteCount()];
		for(int i = 0 ; i < sequences.length ; i++) {
			for(int j = 0 ; j < sequences[i].length ; j++) {
				char c = base.getData(i,j);
				if(dt.isUnknownChar(c)) {
					sequences[i][j] = unknownState;
				} else {
					sequences[i][j] = dt.getState(c);
				}
			}
		}
		return sequences;
	}

	/**
	 * Returns total sum of pairs alignment penalty using gap creation
	 * and extension penalties and transition penalties in the
	 * TransitionPenaltyTable provided. By default this is end-weighted.
	 */
	public static double getAlignmentPenalty(
					Alignment a,
					TransitionPenaltyTable penalties,
					double gapCreation,
					double gapExtension) {

		return getAlignmentPenalty(a, a.getDataType(),
			penalties, gapCreation, gapExtension, false /* end-weighted */);
	}

	/**
	 * Returns total sum of pairs alignment distance using gap creation
	 * and extension penalties and transition penalties as defined in the
	 * TransitionPenaltyTable provided.
	 * Gap cost calculated as follows: given gap of length len => gapCreation + (1en-l)*gapExtension
	 * @param gapCreation the cost of the initial gap opening character
	 * @param gapExtension the cost of the remaining gap characters

	 * @param local true if end gaps ignored, false otherwise
	 */
	public static double getAlignmentPenalty(
					Alignment a,
					DataType dataType,
					TransitionPenaltyTable penalties,
					double gapCreation,
					double gapExtension,
					boolean local) {

		int[][] indices = new int[a.getSequenceCount()][a.getSiteCount()];
		for (int i = 0; i < a.getSequenceCount(); i++) {
			getAlignedSequenceIndices(a, i, indices[i], dataType, dataType.getNumStates());
		}

		CostBag totalBag = new CostBag();
		int numberOfSites = indices[0].length;
		for (int i = 0; i < a.getSequenceCount(); i++) {
			for (int j = i + 1; j < a.getSequenceCount(); j++) {
				totalBag.add(getAlignmentPenalty(dataType, penalties,
					indices[i], indices[j], local,numberOfSites));
			}
		}
		return totalBag.score(gapCreation, gapExtension);
	}

	/**
	 * guess data type suitable for a given sequence data set
	 *
	 * @param alignment alignment
	 *
	 * @return suitable DataType object
	 */
	public static DataType getSuitableInstance(Alignment alignment) {
		return getSuitableInstance(new AlignmentCM(alignment));
	}

		/**
	 * guess data type suitable for a given sequence data set
	 *
	 * @param alignment the alignment represented as an array of strings
	 *
	 * @return suitable DataType object
	 */
	public static DataType getSuitableInstance(String[] sequences) {
		return getSuitableInstance(new StringCM(sequences));
	}
	/**
	 * guess data type suitable for a given sequence data set
	 *
	 * @param alignment the alignment represented as an array of strings
	 *
	 * @return suitable DataType object
	 */
	public static DataType getSuitableInstance(char[][] sequences) {
		return getSuitableInstance(new CharacterCM(sequences));
	}
	private static interface CharacterMatrix {
		public int getSequenceCount();
		public int getSiteCount();
		public char getData(int sequence,int site);
	}
	private static class AlignmentCM implements CharacterMatrix {
		Alignment a_;
		public AlignmentCM(Alignment a) {
			this.a_ = a;
		}
		public int getSequenceCount() {	return a_.getSequenceCount();		}
		public int getSiteCount() { return a_.getSiteCount(); }
		public char getData(int sequence,int site) { return a_.getData(sequence,site); }
	}
	private static class StringCM implements CharacterMatrix {
		String[] sequences_;
		public StringCM(String[] sequences) {
			this.sequences_ = sequences;
		}
		public int getSequenceCount() {	return sequences_.length;		}
		public int getSiteCount() { return sequences_[0].length(); }
		public char getData(int sequence,int site) { return sequences_[sequence].charAt(site); }
	}
	private static class CharacterCM implements CharacterMatrix {
		char[][] sequences_;
		public CharacterCM(char[][] sequences) {
			this.sequences_ = sequences;
		}
		public int getSequenceCount() {	return sequences_.length;		}
		public int getSiteCount() { return sequences_[0].length; }
		public char getData(int sequence,int site) { return sequences_[sequence][site]; }
	}

	private static DataType getSuitableInstance(CharacterMatrix cm) {
		// count A, C, G, T, U, N
		long numNucs = 0;
		long numChars = 0;
		long numBins = 0;
		for (int i = 0; i < cm.getSequenceCount(); i++)
		{
			for (int j = 0; j < cm.getSiteCount(); j++)
			{
				char c = cm.getData(i, j);

				if (c == 'A' || c == 'C' || c == 'G' ||
						c == 'T' || c == 'U' || c == 'N') numNucs++;

				if (c != '-' && c != '?') numChars++;

				if (c == '0' || c == '1') numBins++;
			}
		}

		if (numChars == 0) numChars = 1;

		// more than 85 % frequency advocates nucleotide data
		if ((double) numNucs / (double) numChars > 0.85)
		{
			return new Nucleotides();
		}
		else if ((double) numBins / (double) numChars > 0.2)
		{
			return new TwoStates();
		}
		else
		{
			return new AminoAcids();
		}
	}
	/**
	 * Estimate the frequencies of codons, calculated from the average nucleotide frequencies.
	 * As for CodonFreq = F1X4 (1) in PAML
	 * @param a The base alignment, will be converted to nucleotides
	 * @return The codon frequences as estimated by the average of nuceltoide frequences
	 */
	public static double[] estimateCodonFrequenciesF1X4(Alignment a) {
		Alignment na = new DataTranslator(a).toAlignment(Nucleotides.DEFAULT_INSTANCE,0);
		return Codons.getF1X4CodonFrequencies(estimateFrequencies(na));
	}
	/**
	 * Estimate the frequencies of codons, calculated from the average nucleotide
	 * frequencies at the three codon positions.
	 * As for CodonFreq = F3X4 (2) in PAML
	 * @param a The base alignment, will be converted to nucleotides
	 * @return The codon frequences as estimated by the average of nuceltoide frequences
	 */
	public static double[] estimateCodonFrequenciesF3X4(Alignment a) {
		Alignment na = new DataTranslator(a).toAlignment(Nucleotides.DEFAULT_INSTANCE,0);
		return Codons.getF3X4CodonFrequencies(estimateTupletFrequencies(na,3));
	}

	/** count states
	 *  @note Alignment state does not change! That is,
	 *  does not create a data type for alignment, and does not
	 *  set the frequencies internally for the alignment
	 */
	public static double[] estimateFrequencies(Alignment a)
	{
		DataType dt = a.getDataType();
		if (dt == null)		{
			dt = getSuitableInstance(a);
		}

		int numStates = dt.getNumStates();

		double[] frequency = new double[numStates];

		long[] stateCount = new long[numStates+1];

		for (int i = 0; i < numStates+1; i++)
		{
			stateCount[i] = 0;
		}

		for (int i = 0; i < a.getSequenceCount(); i++)
		{
			for (int j = 0; j < a.getSiteCount(); j++)
			{
				int state = dt.getState(a.getData(i,j));
				if(dt.isUnknownState(state)) {
					state = stateCount.length -1;
				}
				stateCount[state] += 1;
			}
		}

		// Compute frequencies suitable for RateMatrix (sum = 1.0)
		long sumStates = a.getSiteCount()*a.getSequenceCount()-stateCount[numStates];
		for (int i = 0; i < numStates; i++)
		{
			frequency[i] = (double) stateCount[i]/sumStates;
		}

		//a.setFrequency(frequency);

		return frequency;
	}

	/** Estimates frequencies via tuplets. This is most useful for nucleotide data where the frequencies at each codon position are\
	 *  of interest (tuplet size = 3)
	 * @param a The input alignment
	 * @param tupletSize the size of the tuplet
	 *  @note Alignment state does not change! That is,
	 *  does not create a data type for alignment, and does not
	 *  set the frequencies internally for the alignment
	 */
	public static double[][] estimateTupletFrequencies(Alignment a, int tupletSize)	{
		DataType dt = a.getDataType();
		if (dt == null)	{
			dt = getSuitableInstance(a);
		}

		final int numberOfStates = dt.getNumStates();

		final double[][] frequencies = new double[tupletSize][numberOfStates];

		final long[][] stateCounts = new long[tupletSize][numberOfStates+1];
		final long[] tupleStateCounts = new long[tupletSize];
		for(int j = 0 ; j < tupletSize ; j++) {
			tupleStateCounts[j] = 0;
			for( int i = 0; i< numberOfStates+1; i++ ) {
				stateCounts[j][i] = 0;
			}
		}

		final int sequenceCount = a.getSequenceCount();
		final int siteCount = a.getSiteCount();
		for (int j = 0; j < siteCount; j++) {
			int tupleIndex = j%tupletSize;
			for (int i = 0; i < sequenceCount; i++)	{
				int state = dt.getState(a.getData(i,j));
				if(dt.isUnknownState(state)) {
					state = numberOfStates;
				} else {
					tupleStateCounts[tupleIndex]++;
				}
				stateCounts[tupleIndex][state] += 1;
			}
		}

		// Compute frequencies suitable for RateMatrix (sum = 1.0)
		for(int j = 0 ; j < tupletSize ; j++) {
			final long sumStates = tupleStateCounts[j];
			for( int i = 0; i<numberOfStates; i++ ) {
				frequencies[j][i] = ( double )stateCounts[j][i]/(double)sumStates;
			}
		}

		return frequencies;
	}



	public static final boolean isSiteRedundant(Alignment a, int site) {
		int numSeq = a.getSequenceCount();
		final DataType dt = a.getDataType();
		for(int i = 0 ; i < numSeq ; i++) {
			if (!dt.isUnknownChar(a.getData(i,site))) {
				return false;
			}
		}
		return true;
	}

	public static final Alignment removeRedundantSites(Alignment a) {
		boolean[] keep = new boolean[a.getSiteCount()];
		int toKeep = 0;
		for(int i = 0 ; i < keep.length ;i++) {
			keep[i] = !isSiteRedundant(a,i);
			if(keep[i]) {
				toKeep++;
			}
		}
		String[] newSeqs = new String[a.getSequenceCount()];
		int numberOfSites = a.getSiteCount();
		for(int i = 0 ; i < newSeqs.length ; i++) {
			StringBuffer sb = new StringBuffer(toKeep);
			for(int j = 0 ; j < numberOfSites ; j++) {
				if(keep[j]) {
					sb.append(a.getData(i,j));
				}
			}
			newSeqs[i] = sb.toString();
		}
		return new SimpleAlignment(a, newSeqs,a.getDataType());
	}

	/**
	 * Returns true if the alignment has a gap at the site in the
	 * sequence specified.
	 */
	public static final boolean isGap(Alignment a, int seq, int site) {
		return a.getDataType().isGapChar(a.getData(seq, site));
	}

	/**
	 * @param startingCodonPosition from {0,1,2}, representing codon position
	 * of first value in sequences...
		 * @param translator the translator to use for converting codons to
	 * amino acids.
	 * @param removeIncompleteCodons removes end codons that are not complete
	 * (due to startingPosition, and sequence length).
	 */
	public static void getPositionMisalignmentInfo(Alignment a, PrintWriter
	out, int startingCodonPosition,  CodonTable translator, boolean removeIncompleteCodons) {
		int leftGaps, rightGaps; //The gaps to the left and right of the center nucleotide
		DataType dt = a.getDataType();
		for(int i = 0 ; i < a.getSequenceCount() ; i++) {
			int codonPosition = startingCodonPosition;
			String codon = "";
			boolean first = true;
			out.print(a.getIdentifier(i)+":");
			leftGaps = 0;
			rightGaps = 0;
			for(int j = 0 ; j < a.getSiteCount() ; j++) {
				char c = a.getData(i, j);
				if(dt.isUnknownChar(c)) {
					switch(codonPosition) {
						case 1: { leftGaps++; break; }
						case 2: { rightGaps++; break; }
						default: { out.print(c); break; }
					}
				} else {
					codon+=c;
					if(codonPosition==2) {
						if(!first||!(first&&codon.length()!=3&&removeIncompleteCodons)) {
							if(!first||(first&&startingCodonPosition==0)) {
								out.print('[');
							}
							outputChar(out,Alignment.GAP,leftGaps);
							out.print(translator.getAminoAcidChar(codon.toCharArray())); //Translator takes care of wrong length codons!S
							outputChar(out,Alignment.GAP,rightGaps); out.print(']');
						}
						first = false; codon = ""; leftGaps = 0; rightGaps = 0;
					}
					codonPosition = (codonPosition+1)%3;
				}
			}
			//If we finish on an incomplete codon (we ignore the case where a sequence is less than 3 nucleotides
			if(!removeIncompleteCodons && codonPosition!=0) {
				out.print('[');
				outputChar(out,Alignment.GAP,leftGaps);
				out.print('?');
				outputChar(out,Alignment.GAP,rightGaps);
			}
			out.print("\n");
		}
	}

	/**
		@param startingCodonPosition - from {0,1,2}, representing codon position of first value in sequences...
		@note uses middle nucelotide of code to display info...
	 */
	public static void getPositionMisalignmentInfo(Alignment a, PrintWriter out, int startingCodonPosition) {
		DataType dt = a.getDataType();

		for(int i = 0 ; i < a.getSequenceCount() ; i++) {
			int codonPosition = startingCodonPosition;
			out.print(a.getIdentifier(i)+":");
			for(int j = 0 ; j < a.getSiteCount(); j++) {
				char c = a.getData(i, j);
				if(dt.isUnknownChar(c)) {
					out.print(c);
				}
				else {
					switch(codonPosition) {
						case 0 : { out.print('['); 	break; }
						case 1 : { out.print(c); break; }
						case 2 : { out.print(']'); break; }
					}
					codonPosition = (codonPosition+1)%3;
				}

			}
			out.print("\n");
		}
	}
	/** Concatenates an array of alignments such that the resulting alignment is
	 *		all of the sub alignments place along side each other
	 */
	public static final Alignment concatAlignments(Alignment[] alignments, DataType dt) {
		int maxSequence = -1;
		Alignment maxAlignment =null;
		int length = 0;
		for(int i = 0 ; i < alignments.length ; i++) {
			if(alignments[i].getSequenceCount()>maxSequence) {
				maxAlignment = alignments[i];
				maxSequence = alignments[i].getSequenceCount();
			}
			length+=alignments[i].getSiteCount();
		}
		char[][] sequences = new char[maxSequence][length];
		for(int j = 0 ; j < sequences.length ; j++) {
			int base = 0;
			for(int i = 0 ; i < alignments.length ; i++) {
				if(alignments[i].getSequenceCount()<=j) {
					for(int k = 0 ; k < alignments[i].getSiteCount() ; k++) {
						sequences[j][base+k] = Alignment.GAP;
					}
				} else {
					for(int k = 0 ; k < alignments[i].getSiteCount() ; k++) {
						sequences[j][base+k] = alignments[i].getData(j,k);
					}
				}
				base+=alignments[i].getSiteCount();
			}
		}
		SimpleAlignment sa = new SimpleAlignment(maxAlignment, sequences,dt);
		return sa;

	}

	/** Returns a particular sequence of an alignment as a char array */
	public static final char[] getSequenceCharArray(Alignment a, int sequence) {
		char[] cs = new char[a.getSiteCount()];
		for(int i = 0 ; i < cs.length ; i++) {
			cs[i] = a.getData(sequence,i);
		}
		return cs;
	}
	/** Returns a particular sequence of an alignment as a String */
	public static final String getSequenceString(Alignment a, int sequence) {
		return new String(getSequenceCharArray(a,sequence));
	}

		/** Returns an alignment which follows the pattern of the input alignment
			except that all sites which do not contain states in dt (excluding the
			gap character) are removed. The Datatype of the returned alignment is dt
	*/
	public static final Alignment getChangedDataType(Alignment a, DataType dt) {
		int numberOfSites = a.getSiteCount();
		boolean[] include = new boolean[numberOfSites];
		int goodSiteCount = 0;
		for(int i = 0 ; i < numberOfSites ; i++) {
			include[i] = isGoodSite(a,dt,i);
			if(include[i]) {
				goodSiteCount++;
			}
		}
		//Yes, I'm aware it may be slightly faster to nest sequence
		// in site but it's easier to program this way
		String[] sequences = new String[a.getSequenceCount()];
		for(int i = 0 ; i < sequences.length ; i++) {
			char[] seq = new char[goodSiteCount];
			int count = 0;
			for(int j = 0 ; j < numberOfSites ; j++) {
				if(include[j]) {
					seq[count] = a.getData(i,j);
					count++;
				}
			}
			sequences[i] = new String(seq);
		}
		return new SimpleAlignment(new SimpleIdGroup(a), sequences,dt);
	}

	/** Tests the characters of an alignment to see if there are any characters that
			are not within a data type.
			@teturn the number of invalid characters
	*/
	public static final int countUnknowns(Alignment a, DataType dt) {
		int count = 0;
		for(int i = 0 ; i < a.getSequenceCount() ; i++) {
			for(int j = 0 ; j < a.getSiteCount() ; j++) {
				if(dt.isUnknownState(dt.getState(a.getData(i,j)))) {
					count++;
				}
			}
		}
		return count;
	}
	private static final void stripLeadingIncompleteCodon(int[] states, int unknownState) {
		int numberOfCodons = states.length/3;
		final Nucleotides n = Nucleotides.DEFAULT_INSTANCE;
		for(int codon = 0 ; codon < numberOfCodons ; codon++) {
			int unknownCount = 0;
			final int index = codon*3;
			for(int i = 0 ; i < 3 ; i++) {
				if(n.isUnknownState(states[index+i])) {
					unknownCount++;
				}
			}
			if(unknownCount==0) {
				return; //First codon is not incomplete
			} else if(unknownCount!=3) {
				//We have an incomplete codon on our hands!
				for(int i = 0 ; i < 3 ; i++) {
					states[index+i] = unknownState;
				}
			}
		}
	}
	/**
	 * Creates a new nucleotide alignment based on the input that has any leading incomplete
	 * codons (that is, the first codon of the sequence that is not a gap/unknown but is not complete - has
	 * a nucleotide unknown) replaced by a triplet of unknowns
	 * @param base The basis alignment (of any molecular data type)
	 * @return the resulting alignment
	 */
	public static final Alignment getLeadingIncompleteCodonsStripped(Alignment base) {
		DataTranslator dt = new DataTranslator(base);
		Nucleotides n = Nucleotides.DEFAULT_INSTANCE;
		int[][] states = dt.toStates(n,0);
		int unknownState = n.getRecommendedUnknownState();
		for(int i = 0 ; i < states.length ; i++) {
			stripLeadingIncompleteCodon(states[i], unknownState);
		}
		return new SimpleAlignment(base,Nucleotides.DEFAULT_INSTANCE,states);
	}
	// PRIVATE METHODS

	private static final void outputChar(PrintWriter out, char c, int number) {
		for(int i = 0 ; i < number ; i++) {
			out.print(c);
		}
	}

	private static void printNextSites(Alignment a, PrintWriter out, boolean chunked, int seq, int start, int num)
	{
		// Print next num characters
		for (int i = 0; (i < num) && (start + i < a.getSiteCount()); i++)
		{
			// Chunks of 10 characters
			if (i % 10 == 0 && i != 0 && chunked)
			{
				out.print(' ');
			}
			out.print(a.getData(seq, start+i));
		}
	}

	/**
	 * Returns the gap creation costs between sequences x and y from site start to site finish.
	 * @param a alignment
	 * @param x first sequence
	 * @param y second sequence
	 * @param start first site to consider (inclusive)
	 * @param finish last site to consider (inclusive)
	 */
	private static int getNaturalGapCost(Alignment a, int x, int y,
					int start, int finish) {
		DataType dt = a.getDataType();
		int totalCost = 0;
		boolean inGap = false;

		// get gap creation costs
		for (int i = start; i <= finish; i++) {
			// if not a gap in one of them then consider column for x
			if (!(dt.isUnknownChar(a.getData(y, i)) && dt.isUnknownChar(a.getData(x, i))) ) {
				// if gap in x then its the start of gap or already in gap
				if (isGap(a, x, i)) {
					// if not in gap then new gap
					if (!inGap) {
						totalCost += 1;
						inGap = true;
					} // else in gap and no extra cost
				} else {
					// else not null in x therefore not in gap
					inGap = false;
				}
			}
		}

		return totalCost;
	}

//=================================================================
	private static final boolean isGoodSite(Alignment a, DataType dt, int site) {
		int numberOfSequences = a.getSequenceCount();
		for(int i = 0 ; i < numberOfSequences ; i++) {
			char c = a.getData(i,site);
			if(c!=Alignment.GAP&&dt.isUnknownState(dt.getState(c))) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Returns the score of this alignment based on all pairwise distance measures.
	 * @param a alignment to score
	 * @param cdm the character distance matrix
	 * @param x
	 */
	private static CostBag getAlignmentPenalty(DataType dt,
		TransitionPenaltyTable penalties,
		int[] xindices, int[] yindices,
		boolean local, int numberOfSites) {
		int start = 0;
		int finish = numberOfSites - 1;
		if (local) {
			while (dt.isUnknownState(yindices[start]) || dt.isUnknownState(xindices[start])) {
				start += 1;
			}
			while (dt.isUnknownState(yindices[finish]) || dt.isUnknownState(xindices[finish])) {
				finish -= 1;
			}
		}

		// get gap costs (creation penalties)
		int gapCost = 0;

		int gapExCost = 0;

		double subCosts = 0.0;
		boolean inGap = false;
		// get substitution costs (including extension penalties)
		for (int i = start; i <= finish; i++) {
			boolean xIsGap = dt.isUnknownState(xindices[i]) ;
			boolean yIsGap = dt.isUnknownState(yindices[i]) ;
			if(xIsGap) {
				if(!yIsGap) {
					if(inGap) {
						gapExCost += 1;
					} else {
						inGap = true; gapCost++;
					}
				}
			} else {
				if(yIsGap) {
					if(inGap) {
						gapExCost += 1;
					} else {
						inGap = true; gapCost++;
					}
				} else {
					inGap = false;
	  			subCosts += penalties.penalty(xindices[i], yindices[i]);
				}
			}
		}

		return new CostBag(gapCost, gapExCost, subCosts);
	}


	/**
	 * @return the consistency of homology assignment between two alignments.
	 */
	public static double getConsistency(Alignment a, Alignment b) {


		ConsistencyBag curBag, totalBag = new ConsistencyBag();
		totalBag.matches = 0;
		totalBag.overlap = 0;

		for (int i = 0; i < a.getIdCount(); i++) {
			curBag = getConsistency(a, b, a.getIdentifier(i).getName());
			totalBag.matches += curBag.matches;
			totalBag.overlap += curBag.overlap;
		}

		return totalBag.consistency();
	}

	/**
	 * @return the consistency of the two alignments with respect to the named taxa.
	 */
	private static ConsistencyBag getConsistency(Alignment a, Alignment b, String name) {

		int[][] indices1 = getAlignmentIndices(a);
		int[][] indices2 = getAlignmentIndices(b);

		int homeIndex = a.whichIdNumber(name);
		int awayIndex = b.whichIdNumber(name);

		int awayIndex2;

		int totalMatches = 0;
		int totalHomeMatchesPossible = 0;

		for (int i = 0; i < a.getIdCount(); i++) {
			if (i != homeIndex) {
				awayIndex2 = b.whichIdNumber(a.getIdentifier(i).getName());
				if (awayIndex2 != -1) {
					totalMatches += getAlignmentMatches(indices1, indices2, homeIndex, i, awayIndex, awayIndex2);
					totalHomeMatchesPossible += getAlignmentMatches(indices1, homeIndex, i);
				} else throw new RuntimeException("Alignments do not have same taxa!");
			}
		}

		ConsistencyBag bag = new ConsistencyBag(totalMatches, totalHomeMatchesPossible);
		return bag;
	}


	/**
	 * Fills a [length][numsequences] matrix with indices.
	 * Each indices points to a position in the unaligned sequence, -1 means a gap.
	 */
	private static int[][] getAlignmentIndices(Alignment a) {

		int[][] indices = new int[a.getIdCount()][a.getSiteCount()];
		DataType dataType = a.getDataType();

		for (int i = 0; i < a.getIdCount(); i++) {
			int seqcounter = 0;
			for (int j = 0; j < a.getSiteCount(); j++) {
				int index = dataType.getState(a.getData(i, j));
				if (index != dataType.getNumStates()) {
					indices[i][j] = seqcounter;
					seqcounter += 1;
				} else {
					indices[i][j] = -1;
				}
			}
		}

		return indices;
	}

	/**
	 * @return the total number of homology assignments between seqa and seqb in this alignment.
	 */
	private static int getAlignmentMatches(int[][] indices, int seqa, int seqb) {

		int matches = 0;

		for (int i = 0; i < indices[seqa].length; i++) {
			if ((indices[seqa][i] != -1) && (indices[seqb][i] != -1)) {
				matches += 1;
			}
		}

		return matches;
	}

	/**
	 * @return the number of homology matches between two sequences in
	 * two alignments.
	 */
	private static int getAlignmentMatches(int[][] indices1, int[][] indices2,
						int seqa1, int seqb1, int seqa2, int seqb2) {

		int matches = 0;
		int counter2 = 0;

		for (int i = 0; i < indices1[0].length; i++) {
			if ((indices1[seqa1][i] != -1) && (indices1[seqb1][i] != -1)) {
				try {
					while (indices2[seqa2][counter2] != indices1[seqa1][i]) {

						counter2 += 1;
					}
				} catch (ArrayIndexOutOfBoundsException ae) {

					for (int j = 0; j < indices1[0].length; j++) {
						System.out.println("indice1[" + j+"]" + indices1[seqa1][j]);
					}
					for (int j = 0; j < indices2[0].length; j++) {
						System.out.println("indice2[" + j+"]" + indices2[seqa2][j]);
					}
					System.out.println(
						"indices1["+seqa1+"]["+i+"] = " + indices1[seqa1][i] +
						"\tindices2["+seqa2+"]["+(counter2-1)+"] = " +
						indices2[seqa2][counter2-1]);
					System.out.println("counter2 = " + counter2);
				}

				if (indices1[seqb1][i] == indices2[seqb2][counter2]) {
					matches += 1;
				}
			}
		}

		return matches;
	}

}

class CostBag {

	int gc = 0;
	int ge = 0;
	double substitutions = 0.0;

	public CostBag() {}

	public CostBag(int gc, int ge, double subs) {
		this.gc = gc;
		this.ge = ge;
		substitutions = subs;
	}

	public void add(CostBag bag) {
		gc += bag.gc;
		ge += bag.ge;
		substitutions += bag.substitutions;
	}

	/**
	 * Calculate the score, given gap of length len => gapCreation + (1en-l)*gapExtension
	 * @param gapCreation the cost of the initial gap opening character
	 * @param gapExtension the cost of the remaining gap characters
	 * @return
	 */
	public double score(double gapCreation, double gapExtension) {
		return substitutions + (gc * (gapCreation)) + (ge * gapExtension);
	}
}

class ConsistencyBag {

	public ConsistencyBag() {};

	public ConsistencyBag(int o, int m) {matches = m; overlap = o;}

	public String toString() {
		return (Math.rint(consistency() * 100.0) / 100.0) + " (" + overlap + "/" + matches + ")";
	}

	public double consistency() {
		return (double)overlap / (double)matches;
	}

	int matches;
	int overlap;
}
