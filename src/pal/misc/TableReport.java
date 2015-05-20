// TableReport.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.misc;

/**
 * interface for classes with data that can be presented in tables
 *
 * @author Ed Buckler
 * @version $Id: TableReport.java,v 1.3 2001/11/20 19:58:45 alexi Exp $
 */

public interface TableReport {

    /**
     * get the names of the columns
     *
     * @return columns names
     */
    Object[] getTableColumnNames();

    /**
     * get the data elements
     *
     * @return the data elements
     */
    Object[][] getTableData();

    /**
     * get the title of the table
     *
     * @return a String title
     */
    String getTableTitle();

}
