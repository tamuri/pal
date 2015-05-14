// DataTranslator.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.alignment;

/**
 * A general method for translating an alignment between Nucleotides, AminoAcids, Codons, and GapBalanced representations.
 * An attempt to clean up the mess conversion code that's floating around
 *
 * @version $Id: DataTranslator.java,v 1.10 2004/10/14 02:01:43 matt Exp $
 *
 * @author Matthew Goode
 * @note
 * <ul>
 *   <li> 19 August 2003 Changed constructor to check if data type is AminoAcids and converts to SpecificAminoAcids(Universal) if that is the case (remember AminoAcids is not a MolecularDataType)
 * </ul>
 */
import pal.datatype.*;/**
 * A general method for translating an alignment between Nucleotides, AminoAcids, Codons, and GapBalanced representations.
 * An attempt to clean up the mess conversion code that's floating around
 *
 * @version $Id: DataTranslator.java,v 1.10 2004/10/14 02:01:43 matt Exp $
 *
 * @author Matthew Goode
 * @note
 * <ul>
 *   <li> 19 August 2003 Changed constructor to check if data type is AminoAcids and converts to SpecificAminoAcids(Universal) if that is the case (remember AminoAcids is not a MolecularDataType)
 * </ul>
 */
import pal.misc.*;
public class DataTranslator {
	int[][] nucleotideStateData_;
	MolecularDataType dataType_;
	IdGroup ids_;

	/**
	 * @param base the base alignment that will be translated. The data type of this
	 * alignment must be of type MolecularDataType
	 * @throws IllegalArgumentException if base DataType not of type MolecularDataType
	 */
	public DataTranslator(Alignment base) {
		DataType baseDataType = base.getDataType();
		if(baseDataType instanceof AminoAcids) {
			baseDataType = new SpecificAminoAcids(CodonTable.UNIVERSAL);
		}
		if(baseDataType instanceof MolecularDataType) {
			dataType_ = (MolecularDataType)baseDataType;
			nucleotideStateData_ = toNucleotides(toStates(base), dataType_);
			ids_ = new SimpleIdGroup(base);
		} else{
			throw new IllegalArgumentException("Alignment does not have a molecular based data type");
		}
		//nucleotideStateData_;
	}
	/**
	 * Base DataType is assumed to be IUPAC
	 */
	public DataTranslator(int[][] stateData) {
		this(stateData,IUPACNucleotides.DEFAULT_INSTANCE);
	}

	public DataTranslator(int[][] stateData, MolecularDataType dt) {
		this(stateData,dt,null);
	}

	public DataTranslator(int[][] stateData, MolecularDataType dt, IdGroup ids) {
		dataType_ = dt;
		this.ids_ = ids;
		nucleotideStateData_ = toNucleotides(stateData,dt);
	}

	public DataTranslator(MolecularDataType dt, char[][] charData) {
		dataType_ = dt;
		nucleotideStateData_ = toNucleotides(toStates(charData,dt),dt);
	}

	/**
	 * @return an array of states, where the states are of the form dictated by
	 * dt, and based on the base alignment
	 */
	public int[][] toStates(MolecularDataType dt, int startingIndex) {
		int[][] newStates = new int[nucleotideStateData_.length][];
		for(int i = 0 ; i < newStates.length ; i++) {
			newStates[i] = dt.getMolecularStatesFromIUPACNucleotides(nucleotideStateData_[i],startingIndex);
		}
		return newStates;
	}

	public double[] getFrequencies(MolecularDataType dt, int startingIndex) {
		int[][] states = toStates(dt,startingIndex);
		int[] counts = new int[dt.getNumStates()];
		for(int i = 0 ;i < counts.length ; i++) { counts[i] = 0; }
		int total = 0;
		for(int i = 0 ; i < states.length ; i++) {
			for(int j = 0 ; j < states[i].length ; j++) {
				int state = states[i][j];
				if(!dt.isUnknownState(state)) {
					total++;
					counts[state]++;
				}
			}
		}
		double[] frequencies = new double[counts.length];
		double dTotal = total;
		for(int i = 0 ; i < frequencies.length ; i++) {
			frequencies[i] = counts[i]/dTotal;
		}
		return frequencies;
	}

	/**
	 * Ensures that all states that are "unknown" get set to the value of 'unknownState'
	 */
	public void ensureUnknownState(int[] states, int unknownState) {
		ensureUnknownState(dataType_,states,unknownState);
	}


	/**
	 * @return an array of characters, where the characters are of the form dictated by
	 * dt, and based on the base alignment
	 */
	public char[][] toChars(MolecularDataType dt, int startingIndex) {
		return toChars(toStates(dt,startingIndex),dt);
	}

	private final IdGroup generateIdGroup() {
		String[] ids = new String[nucleotideStateData_.length];
		for(int i = 0 ; i < ids.length ; i++) {
			ids[i] = "Sequence:"+i;
		}
		return new SimpleIdGroup(ids);
	}

	/**
	 * @note if DataTranslator was constructed without reference to an IdGroup a
	 * default id group will be used where the sequences are labelled "Sequence:1", "Sequence:2", etc...
	 */
	public Alignment toAlignment(MolecularDataType dt, int startingIndex) {
		IdGroup idGroup;
		if(ids_==null) {
			idGroup = generateIdGroup();
		} else {
			idGroup = ids_;
		}
		return new SimpleAlignment(idGroup, toChars(dt,startingIndex),dt);
	}
	public Alignment toReverseComplementNucleotides(int startingIndex) {
		final IdGroup idGroup;
		if(ids_==null) { idGroup = generateIdGroup();	} else {	idGroup = ids_;		}
	  int[][] baseSequences = toStates(Nucleotides.DEFAULT_INSTANCE,startingIndex);
		for(int i= 0 ; i < baseSequences.length ; i++) {
		   Nucleotides.complementSequence(baseSequences[i]);
			 DataType.Utils.reverseSequence(baseSequences[i]);
		}
		return new SimpleAlignment(idGroup, Nucleotides.DEFAULT_INSTANCE, baseSequences);
	}
	public Alignment toLeftAlignedReverseComplementNucleotides(int startingIndex) {
		final IdGroup idGroup;
		if(ids_==null) { idGroup = generateIdGroup();	} else {	idGroup = ids_;		}
	  int[][] baseSequences = toStates(Nucleotides.DEFAULT_INSTANCE,startingIndex);
		for(int i= 0 ; i < baseSequences.length ; i++) {
		   Nucleotides.complementSequence(baseSequences[i]);
			 DataType.Utils.reverseSequence(baseSequences[i]);
			 DataType.Utils.leftAlignSequence(baseSequences[i],Nucleotides.DEFAULT_INSTANCE);
		}
		return new SimpleAlignment(idGroup, Nucleotides.DEFAULT_INSTANCE, baseSequences);
	}


// ============================================================================
// ====== Static methods ======================================================
	/**
	 * Converts an alignment to a state matrix
	 * Stored as [sequnce][site]
	 * @note uses -1 as gap/unknown state
	 */
	public static final int[][] toStates(Alignment a) {
	  return toStates(a,-1);
	}
	/**
	 * Converts an alignment to a state matrix
	 * Stored as [sequnce][site]
	 */
	 public static final int[][] toStates(Alignment a, int gapUnknownState) {
			DataType dt = a.getDataType();
			int sequenceCount = a.getSequenceCount();
			int siteCount = a.getSiteCount();
			int[][] states = new int[sequenceCount][siteCount];
			for(int sequence = 0 ; sequence < sequenceCount ; sequence++) {
				for(int site = 0 ; site < siteCount ; site++) {
					char c = a.getData(sequence,site);
					if(dt.isUnknownChar(c)) {
						states[sequence][site] = gapUnknownState;
				  } else {
						states[sequence][site] = dt.getState(c);
					}
				}
			}
			return states;
	 }
	 /**
	 * Converts an alignment to a state matrix
	 * Stored as [sequnce][site]
	 */
	 public static final int[][] toNucleotides(int[][] dtStates, MolecularDataType dt) {
		 int[][] states = new int[dtStates.length][];
			for(int sequence = 0 ; sequence < states.length ; sequence++) {
				states[sequence] = dt.getNucleotideStates(dtStates[sequence]);
			}
			return states;
	 }
	/**
	 * Converts an alignment to a state matrix
	 * Stored as [sequnce][site]
	 */
	 public static final int[][] toStates(char[][] dtChars, DataType dt) {
			int[][] states = new int[dtChars.length][];
			for(int sequence = 0 ; sequence < states.length ; sequence++) {
				states[sequence] = new int[dtChars[sequence].length];
				for(int i = 0 ; i < states[sequence].length ; i++) {
					states[sequence][i] = dt.getState(dtChars[sequence][i]);
				}
			}
			return states;
	 }
	 /**
	 * Converts an state matrix to a char matrix
	 * Stored as [sequnce][site]
	 */
	 public static final char[][] toChars(int[][] dtStates, DataType dt) {
			char[][] chars = new char[dtStates.length][];
			for(int sequence = 0 ; sequence < chars.length ; sequence++) {
				chars[sequence] = new char[dtStates[sequence].length];
				for(int i = 0 ; i < chars[sequence].length ; i++) {
					chars[sequence][i] = dt.getChar(dtStates[sequence][i]);
				}
			}
			return chars;
	 }

	/**
	 * Ensures that all states that are "unknown" (according to a certain DataType) get set to the value of 'unknownState'
	 */
	public static final void ensureUnknownState(DataType dt, int[] states, int unknownState) {
		for(int i = 0 ; i < states.length ; i++) {
			if(dt.isUnknownState(states[i])) {
				states[i] = unknownState;
			}
		}
	}

}
