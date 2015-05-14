// TreeParseException.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.xml;


/**
 * exception thrown by ElementParser.
 *
 * @author Alexei Drummond
 */
public class XmlParseException extends Exception
{
	public XmlParseException() {}

	public XmlParseException(String msg)
	{
		super(msg);
	}
}
