// Parameterized.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

/**
 * interface for classes that can provide summaries
 *
 * @author Alexei Drummond
 * @version $Id: Summarizable.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 */
public interface Summarizable extends java.io.Serializable {
    String[] getSummaryTypes();

    double getSummaryValue(int summaryType);
} 
