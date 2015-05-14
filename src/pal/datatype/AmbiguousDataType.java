 // DataType.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


// Known bugs and limitations:
// - all states must have a non-negative value 0..getNumStates()-1
// - ? (unknown state) has value getNumStates()


package pal.datatype;

/**
 * Additional interface information for data types which represent ambiguity in
 * sub types.
 *
 * @version $Id: AmbiguousDataType.java,v 1.1 2002/11/25 05:38:40 matt Exp $
 *
 * @author Matthew Goode
 */
public interface AmbiguousDataType extends DataType {
	//
	// Public stuff
	//

	/**
	 * returns an array containing the non-ambiguous states that this state represents.
	 */
	int[] getSpecificStates(int ambiguousState);

	/**
	 * @return the DataType that this datatype is the Ambiguous Version of.
	 */
	DataType getSpecificDataType();

	/**
	 * Attempts to "resolve" the ambiguity in a state with regard to the specific data type.
	 * @param ambiguousState the state of this data type (the ambiguous one!)
	 * @param specificInclusion An array of length equal to or greater than the number of states of
	 * the specific DataType. Each state of the specific data type is represented by the corresponding
	 * element in this array. The result of this method will be to set the states that the ambiguous state cannot
	 * represent to false, and those states that the ambiguous state might represent to true.
	 */
	void getAmbiguity(int ambiguousState, boolean[] specificInclusion);
	/**
	 * A more accurate attempt to "resolve" the ambiguity in a state with regard to the specific data type.
	 * @param ambiguousState the state of this data type (the ambiguous one!)
	 * @param specificInclusion An array of length equal to or greater than the number of states of
	 * the specific DataType. Each state of the specific data type is represented by the corresponding
	 * element in this array. The result of this method will be to set the states that the ambiguous state cannot
	 * represent to zero, and those states that the ambiguous state might represent to a value representing the frequency that the ambiguous state is actually that specific state. In general this should be one for
	 * each specific state covered by the ambiguous state (result should be suitable for use in likelihood calculations).
	 */
	void getAmbiguity(int ambiguousState, double[] specificInclusion);
}
