// Version.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.util;

/**
 * This class provides a mechanism for returning the version number of the
 * pal library. It relies on the administrator of the pal library using the
 * module tagging system in CVS. The method getVersionString() will return
 * the version of the pal library under the following conditions: <BR>
 * 1. the pal library has been tagged with an identifier in the format: 
 * pal-#-# or pal-#-#-#, e.g. pal-1-3-0 for version 1.3.0. 
 * 2. the pal library has been checked out *by tag* before packaged for
 * distribution.
 *
 * @author Alexei Drummond
 * @version $Id: Version.java,v 1.1 2001/09/10 01:58:27 alexi Exp $
 */
public class Version {


	/** 
	 * Version string: assumed to be in format projectname-#-#-# 
	 * surrounded by standard CVS junk. 
	 */
	private static String VERSION = "$Name:  $";

	private static String ID = "$Id: Version.java,v 1.1 2001/09/10 01:58:27 alexi Exp $";

	public static String getVersionString() {
	
		if (VERSION.indexOf('-') != -1) {
			String version = 
				VERSION.substring(VERSION.indexOf('-') + 1, VERSION.length() - 1);
			version = version.replace('-', '.');
			return version;
		} else return "unknown";
	}
}
