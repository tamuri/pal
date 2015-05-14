// Units.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * interface holding unit constants
 *
 * @version $Id: Units.java,v 1.11 2004/08/02 05:22:04 matt Exp $
 *
 * @author Alexei Drummond
 * @author Matthew Goode
 */
public interface Units
{
		int EXPECTED_SUBSTITUTIONS = 0;
		int GENERATIONS = 1;
		int DAYS = 2;
		int MONTHS = 3;
		int YEARS = 4;
		int SAMPLE = 5;
		int UNKNOWN = 6;


		String[] UNIT_NAMES = {"Expected Substitutions per Site", "Generations", "Days", "Months", "Years", "Sample", "Unknown"};
		String[] SHORT_UNIT_NAMES = {"Expected Substitutions", "Generations", "Days", "Months", "Years", "Sample", "Unknown"};



}
