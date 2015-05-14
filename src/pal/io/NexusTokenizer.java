/*
 *  NexusTokenizer.java
 *
 *  a.d.moore@ex.ac.uk
 *
 *  This package may be distributed under the
 *  terms of the Lesser GNU General Public License (LGPL)
 *	
 *	http://www.gnu.org/copyleft/lesser.html
 */
package pal.io;

// core
import java.io.*;


/**
*	<h3>Comments</h3>
 *  <p>A simple token pull-parser for the NEXUS file format as specified in:</p>
 *	<p>Maddison, D. R., Swofford, D. L., &amp; Maddison, W. P., <i>Systematic Biology</i>,
 *	<b>46(4)</b>, pp. 590 - 621.</p>
 *	<p>The parser is designed to break a NEXUS file into tokens which are read
 *	individually. Tokens come in four different types:</p>
 *	<ul>
 *		<li>Punctuation: any of the punctuation characters (see constants)</li>
 *		<li>Whitespace:	sequences of characters composed of <code>' '</code> or 
 			<code>'\t'</code>. Whitespace is only returned if the option is set</li>
 *		<li>Word: any string of characters delimited by whitespace 
 *			or punctuation</li>
 *		<li>Newline: <code>'\r'</code>, <code>'\n'</code> or <code>'\r\n'</code>.
 *			The parser will return the character unless <code>convertNL</code> is 
 *			set, in which case it will replace the token with the user specified 
 *			new line character</li>
 *	</ul>
 *	<p>The parser has a set of options allowing tokens to be modified before they
 *	are returned (such as case modification or newline substitution).</p>
 *	<p>Each read by the parser moves forward in the stream, at present there is no
 *	support for unreading tokens or for moving bi-directionally through the stream</p>
 *	<p>NB: in this implementation, the token #NEXUS is considered special and when
 *	read by the parser, it will return one token: '#NEXUS' not two: '#' and 'NEXUS'.
 *	This token has special meaning and is reflected in it having its own token type<br/></p>
 *	
 *	<h3>Usage</h3>
 *	<code>
 *	NexusTokenizer ntp = new NexusTokenizer(new PushbackReader(new FileReader("afile")));<br/>
 *	ntp.setReadWhiteSpace(false);<br/>&nbsp;&nbsp;&nbsp;&nbsp;// ignore whitespace
 *	ntp.setIgnoreComments(true);<br/>&nbsp;&nbsp;&nbsp;&nbsp; // ignore comments
 *	ntp.setWordModification(NexusTokenizer.WORD_UPPERCASE);<br/> // all tokens in uppercase
 *	String nToken = ntp.readToken();<br/>
 *	<br/>
 *	while(nToken != null) {<br/>
 *	&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Token: " + nToken);<br/>
 *	&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Col:   " + ntp.getCol());<br/>
 *	&nbsp;&nbsp;&nbsp;&nbsp;System.out.println("Row:   " + ntp.getRow());<br/>
 *	}<br/>
 *	</code>
 *	
 *
 * @author     $Author$
 * @version    $Id$, $Name$
 */
public final class NexusTokenizer {

	////////////////////////////////////////////////////////////////////////////
	// 								PARSER SYMBOLS							  //
	////////////////////////////////////////////////////////////////////////////

	
	public final static char L_PARENTHESIS 	= '(';
	public final static char R_PARENTHESIS 	= ')';
	public final static char L_BRACKET 		= '[';
	public final static char R_BRACKET 		= ']';
	public final static char L_BRACE 		= '{';
	public final static char R_BRACE 		= '}';
	public final static char F_SLASH 		= '/';
	public final static char B_SLASH 		= '\\';
	public final static char COMMA 			= ',';
	public final static char SEMI_COLON 	= ';';
	public final static char COLON 			= ':';
	public final static char EQUALS 		= '=';
	public final static char ASTERIX 		= '*';
	public final static char S_QUOTE 		= '\'';
	public final static char D_QUOTE 		= '"';
	public final static char B_TICK 		= '`';
	public final static char ADDITION 		= '+';
	public final static char DASH 			= '-';
	public final static char L_THAN 		= '<';
	public final static char G_THAN 		= '>';
	public final static char HASH 			= '#';
	public final static char PERIOD			= '.';
	public final static char L_FEED 		= '\n';
	public final static char C_RETURN 		= '\r';
	public final static char TAB 			= '\t';
	public final static char SPACE 			= ' ';

	////////////////////////////////////////////////////////////////////////////
	// 								INSTANCE VARIABLES						  //
	////////////////////////////////////////////////////////////////////////////

	/**  EOF indicator */
	private final static int EOF = -1;

	/**  Reader stream */
	private PushbackReader pr;
	/**  Column in the file */
	private int col;
	/**  Row in the file - delimited by '\r', '\n' or '\r\n' */
	private int row;
	/**  Current token (i.e. last read token) */
	private String cToken;

	////////////////////////////////////////////////////////////////////////////
	// 								PARSER OPTIONS							  //
	////////////////////////////////////////////////////////////////////////////
	
	/**  Flag indicating words should be converted to uppercase */
	public final static int WORD_UPPERCASE 	= 0;
	/**  Flag indicating words should be converted to lowercase */
	public final static int WORD_LOWERCASE 	= 1;
	/**  Flag indicating words should be untouched */
	public final static int WORD_UNMODIFIED = 2;
	
	/**  Flag indicating last token read was undefined */
	public final static int UNDEFINED_TOKEN 	= 10;
	/**  Flag indicating last token read was a word */
	public final static int WORD_TOKEN 			= 11;
	/**  Flag indicating last token read was a punctuation symbol */
	public final static int PUNCTUATION_TOKEN 	= 12;
	/**  Flag indicating last token read was a newline symbol/word */
	public final static int NEWLINE_TOKEN 		= 13;
	/**  Flag indicating last token read was whitespace */
	public final static int WHITESPACE_TOKEN 	= 14;
	/**  Flag indicating last token read was the header token #NEXUS */
	public final static int HEADER_TOKEN 		= 15;
	
	/**  Flag indicating whether or not to read whitespace */
	private boolean 		readWS;
	/**  Flag indicating whether or not the tokenizer should ignore comments */
	private boolean 		ignoreComments;
	/**  Flag indicating whether or not to convert new line symbols */
	private boolean 		convertNL;
	/**  Character with which to replace new line symbols */
	private char 			nlChar;
	/**  Flag value indicating whether the parser should modify word case */
	private int 			wordMod;
	/**  Flag indicating the type of the last token read */
	private int 			lastReadTokenType;


	////////////////////////////////////////////////////////////////////////////
	// 								CONSTRUCTORS							  //
	////////////////////////////////////////////////////////////////////////////
	/**
	 *  Constructor for a <code>NexusTokenParser</code>
	 *
	 * @param  file				File name for the NEXUS file
	 * @exception  IOException  I/O errors
	 */
	public NexusTokenizer(String file) throws IOException {
		this(new PushbackReader(new FileReader(file)));
	}


	/**
	 *  Constructor for a <code>NexusTokenParser</code>
	 *
	 * @param  pr               PushbackReader
	 * @exception  IOException  I/O errors
	 */
	public NexusTokenizer(PushbackReader pr) throws IOException {
		this.pr = pr;
		readWS = false;
		convertNL = false;
		nlChar = '\n';
		wordMod = WORD_UNMODIFIED;
		lastReadTokenType = UNDEFINED_TOKEN;
	}


	////////////////////////////////////////////////////////////////////////////
	// 								PUBLIC METHODS							  //
	////////////////////////////////////////////////////////////////////////////

	/**
	 *  Get the flag indicating whether or not this parser object is reading 
	 *	(and returning) whitespace
	 *
	 * @return    returns the <code>readWS</code> flag
	 */
	public synchronized boolean readWhiteSpace() {
		return readWS;
	}


	/**
	 *  Gets the flag indicating whether this parser instance should convert 
	 *	newline characters. As the specification says (see link in class description 
	 *  above), newline characters may be '\r', '\n', '\r\n'. To provide some 
	 *	kind of uniformity, the parser can convert these symbols into one specified. 
	 *	As a default, this feature is off.
	 *
	 * @return    returns	the <code>convertNL</code> flag
	 */
	public synchronized boolean convertNewLine() {
		return convertNL;
	}


	/**
	 *  Sets the <code>readWS</code> flag. True means that the parser will return
	 *	whitespace characters as a token (where whitespace = ' ' or '\t').
	 *
	 * @param  b  flag value for <code>readWS</code>
	 */
	public synchronized void setReadWhiteSpace(boolean b) {
		readWS = b;
	}


	/**
	 *  Sets the <code>convertNL</code> flag. True means that the the parser will
	 *	convert newline characters ('\r', '\n' or '\r\n') into either the default
	 *	('\n' if <code>setNewLineChar()</code> is not called) or to a user specified
	 *	newline char
	 *
	 * @param  b  flag value for <code>convertNL</code>
	 */
	public synchronized void setConvertNewLine(boolean b) {
		convertNL = b;
	}


	/**
	 *  Sets the <code>ignoreComments</code> flag. True means that the the tokenizer
	 *	will ignore comments (i.e. sections of a nexus file delimited by '[...]'.
	 *  When set to true, the tokenizer will return the first token available after 
	 *	a comment.
	 *
	 * @param  b  flag value for <code>ignoreComments</code>
	 */
	public synchronized void setIgnoreComments	(boolean b) {
		ignoreComments = b;
	}


	/**
	 *  Sets the character to be convert newline characters into
	 *
	 * @param  nl  Replacement newline character
	 */
	public synchronized void setNewLineChar(char nl) {
		nlChar = nl;
	}


	/**
	 *  Gets the current column position of the cursor. Changed after each read.
	 *
	 * @return    Column number (zero indexed)
	 */
	public synchronized int getCol() {
		return col;
	}


	/**
	 *  Gets the current row position of the cursor. Changed after each read.
	 *
	 * @return    Row number (zero indexed)
	 */
	public synchronized int getRow() {
		return row;
	}
	
	
	/**
	 *  Gets the word modification flag currently in use
	 *
	 * @return    	Flag value for word modification
	 */
	public synchronized int getWordModification() {
		return wordMod;
	}
	
	
	/**
	 *  Sets the flag value for word modification. The token case can be changed
	 *	to lowercase or uppercasse once it has been read from the stream (depending
	 *	on the set flag). <code>WORD_UNMODIFIED</code> indicates that the tokens should be 
	 *	returned in the case that they are read from the stream. This value can
	 *	be set at any time between token reads and thus the next token read will
	 *	be altered depending on this value. The default is <code>WORD_UNMODIFIED.</code>
	 *
	 * @param	flag Flag value, one of <code>WORD_LOWERCASE</code>, 
	 *			<code>WORD_UPPERCASE</code> or <code>WORD_UNMODIFIED</code>
	 */
	public synchronized void setWordModification(int flag) {
		switch(flag) {
			case WORD_LOWERCASE:
				wordMod = WORD_LOWERCASE;
				break;
			case WORD_UPPERCASE:
				wordMod = WORD_UPPERCASE;
				break;
			case WORD_UNMODIFIED:
				wordMod = WORD_UNMODIFIED;
				break;
			default:
				wordMod = WORD_UNMODIFIED;
				break;
		}
	}


	/**
	 *  Reads a token in from the underlying stream. Tokens are individual chunks
	 *	read from the underlying stream. Each token is one of the four basic types:
	 *	<ul>
	 *		<li>Word: any string of characters delimited by whitespace 
	 *			or punctuation</li>
	 *		<li>Punctuation: any of the punctuation characters (see constants)</li>
	 *		<li>Whitespace:	sequences of characters composed of ' ' or '\t'. 
	 *  		Whitespace is only returned if the option is set</li>
	 *		<li>Newline: '\r', '\n' or '\r\n'. The parser will return the character 
	 *			unless <code>convertNL</code> is set, in which case it will replace
	 *			the token with the user specified new line character</li>
	 *	</ul>
	 *
	 * @return                          returns	a <code>String</code> token or 
	 *									<code>null</code> if EOF is reached 
	 *									(i.e. no more tokens to read)
	 * @exception  IOException			I/O errors
	 * @exception  NexusParseException	Parsing errors
	 */
	public synchronized String readToken() throws IOException, NexusParseException {
		// saftey check for EOF
		int r = pr.read();
		
		// peculiar behaviour: EOF sometimes returns 65535
		if(r == EOF || r == 65535) {
			return null;
		}
		 
		char c = (char)r;
		
		//should be at start of file or start of the next token
		if(isPunctuation(c)) {
			if(c == HASH) {
				pr.unread(c);
				cToken = readHeaderToken();
				lastReadTokenType = HEADER_TOKEN;
				return cToken;
			}
			else if(c == L_BRACKET & ignoreComments) {
				// possible start of comment
				pr.unread(c);
				readComment();
				
				return readToken();
			}
			else {
				col++;
				cToken = "" + c;
				lastReadTokenType = PUNCTUATION_TOKEN;
				return cToken;
			}
		}
		else if(isNewLine(c)) {
			pr.unread(c);
			cToken = readNewLineToken();
			lastReadTokenType = NEWLINE_TOKEN;
			return cToken;
		}
		else if(isWhiteSpace(c)) {
			if(readWS) {
				// read and return whitespace
				pr.unread(c);
				cToken = readWhiteSpaceToken();
				lastReadTokenType = WHITESPACE_TOKEN;
				return cToken;
			}
			else {
				// read but don't return whitespace
				pr.unread(c);
				readWhiteSpaceToken();
				
				// recursive call
				return readToken();
			}
		}
		else if(isWordChar(c)) {
			pr.unread(c);
			cToken = readWordToken();
			lastReadTokenType = WORD_TOKEN;
			return cToken;
		}
		else {
			// we should _NEVER_ get here
			throw new NexusParseException("Error: unknown character at col:" + col + ", row:" + row);
		}
					
	}
	
	
	/**
	 *  Determine the type of the last read token. After <code>readToken()</code>
	 *	has been called, the type of token returned can be determined by calling
	 *	<code>getLastTokenType()</code>. This returns one of five different constants:
	 *	<ul>
	 *		<li><code>UNDEFINED_TOKEN</code> : default before anything is read 
	 *			from the stream</li>
	 *		<li><code>WORD_TOKEN</code> 	: word token was read</li>
	 *		<li><code>PUNCTUATION_TOKEN</code> : punctuation token was read</li>
	 *		<li><code>NEWLINE_TOKEN</code> : newline token was read</li>
	 *		<li><code>WHITESPACE_TOKEN</code> : whitespace token was read (never 
	 *			returned unless whitespace is being returned) </li>
	 *		<li><code>HEADER_TOKEN</code> : last token was the special word #NEXUS</li>
	 *	</ul>
	 *
	 * @return    	Last token read.
	 */
	public synchronized int getLastTokenType() {
		return lastReadTokenType;
	}
	
	
	/**
	 *	Seeks through the stream to find the next token of the specified type. The
	 *	type value can be one of:
	 *	<ul>
	 *		<li>WORD_TOKEN</li>
	 *		<li>PUNCTUATION_TOKEN</li>
	 *		<li>NEWLINE_TOKEN</li>
	 *		<li>WHITESPACE_TOKEN</li>
	 *		<li>HEADER_TOKEN</li>
	 *	</ul>
	 *
	 * @return                          returns	a <code>String</code> token or 
	 *									<code>null</code> if EOF is reached 
	 *									(i.e. no more tokens to read)
	 * @exception  IOException			I/O errors
	 * @exception  NexusParseException	Thrown by parsing errors or if 
	 *									tokenType == WHITESPACE_TOKEN && 
	 *									readWhiteSpace() == false
	 */
	public synchronized String seek(int tokenType) throws IOException, NexusParseException {
		if(tokenType == WHITESPACE_TOKEN && !readWhiteSpace()) {
			throw new NexusParseException("Error: not returning whitespace tokens");
		}
		
		// check to make sure we have proper value passed in
		switch(tokenType) {
			case WHITESPACE_TOKEN:
			case PUNCTUATION_TOKEN:
			case NEWLINE_TOKEN:
			case HEADER_TOKEN:
			case WORD_TOKEN:
				break;
			default:
				throw new NexusParseException("Error: unknown token type:" + tokenType);
		}
		
		String nToken = readToken();
		while(nToken != null && getLastTokenType() != tokenType) {
			nToken = readToken();
		}
		
		return nToken;
	}
	
	
	/**
	 *	Seeks through the stream to find the token argument.
	 *
	 * @return                          returns	a <code>String</code> token or 
	 *									<code>null</code> if token is not found 
	 *									(i.e. EOF is reached)
	 * @exception  IOException			I/O errors
	 * @exception  NexusParseException	Thrown by parsing errors or if 
	 *									token is whitespace && 
	 *									readWhiteSpace() == false
	 */
	public synchronized String seek(String token) throws IOException, NexusParseException {
		if(!readWhiteSpace()) {
			// assume token is ws unless find otherwise
			// NB - this does assume that string is not 'mixed'
			// i.e. both ws and dark chars
			boolean wsStr = true;	
			for(int i = 0; i < token.length(); i++) {
				if(!isWhiteSpace(token.charAt(i))) {
					wsStr = false;
					break;
				}
			}
			if(wsStr) {
				throw new NexusParseException("Error: not returning whitespace tokens");
			}
		}
		
		String nToken = readToken();
		while(nToken != null && !nToken.equals(token)) {
			nToken = readToken();
		}
		
		return nToken;
	}
	
	
	/**
	 *	Returns the last read token. Each call to <code>readToken()</code> stores the
	 *	returned token so that it can be retrieved again. However, each consuming
	 *	<code>readToken()</code> call replaces this buffer with the new token.
	 *
	 * @return    return the last read token
	 */
	public synchronized String getLastReadToken() {
		return cToken;
	}


	////////////////////////////////////////////////////////////////////////////
	// 								PRIVATE METHODS							  //
	////////////////////////////////////////////////////////////////////////////

	/**
	 *  Utility method to read the NEXUS header
	 *
	 * @return    	Nexus header <code>#NEXUS</code>
	 */
	private synchronized String readHeaderToken() throws IOException, NexusParseException {
		StringBuffer sb = new StringBuffer();
		char c;
		sb.append((char)pr.read());	// #
		col++;
		c = (char)pr.read();
		if(c != 'N' && c != 'n') { throw new NexusParseException("Error: malformed NEXUS header at col:" + col + ", row:" + row); }
		else { sb.append(c); col++; }
		c = (char)pr.read();
		if(c != 'E' && c != 'e') { throw new NexusParseException("Error: malformed NEXUS header at col:" + col + ", row:" + row); }
		else { sb.append(c); col++; }
		c = (char)pr.read();
		if(c != 'X' && c != 'x') { throw new NexusParseException("Error: malformed NEXUS header at col:" + col + ", row:" + row); }
		else { sb.append(c); col++; }
		c = (char)pr.read();
		if(c != 'U' && c != 'u') { throw new NexusParseException("Error: malformed NEXUS header at col:" + col + ", row:" + row); }
		else { sb.append(c); col++; }
		c = (char)pr.read();
		if(c != 'S' && c != 's') { throw new NexusParseException("Error: malformed NEXUS header at col:" + col + ", row:" + row); }
		else { sb.append(c); col++; }
		
		
		return (modWord(sb)).toString();
	}
	
	
	/**
	 *  Utility method to read new line characters. If specified, this method will
	 *	perform character substitution on the newline characters.
	 *
	 * @return    	new line character(s), \n, \r or \r\n. 
	 */
	private synchronized String readNewLineToken() throws IOException {
		char c = (char)pr.read();
		StringBuffer sb = new StringBuffer();
		
		row++;		// move cursor down a row
		col = 0;	// reset col cursor to line beginning
		if(c == C_RETURN) {
			sb.append(C_RETURN);
			
			// read ahead one character
			char t = (char)pr.read();
			if(t == L_FEED) {
				// got \r\n
				sb.append(L_FEED);
			}
			else {
				// next char was not L_FEED, so return to stream
				pr.unread(t);
			}
		}
		else {
			// must have a \n
			sb.append(L_FEED);
		}
		
		if(convertNL) {
			// convert to different new line character
			return "" + nlChar;
		}
		else {
			return sb.toString();
		}
	}
	
	
	/**
	 *  Utility method to read whitespace tokens.
	 *
	 * @return    	String containing whitespace characters 
	 */
	private synchronized String readWhiteSpaceToken() throws IOException {
		StringBuffer sb = new StringBuffer();
		char c = (char)pr.read();
		col++;
		
		while(isWhiteSpace(c)) {
			sb.append(c);
			c = (char)pr.read();
			col++;
		}
		
		// unread last character
		pr.unread(c);
		col--;
		
		return sb.toString();
	}
	
	
	/**
	 *  Utility method to read word tokens.
	 *
	 * @return    	String containing word characters 
	 */
	private synchronized String readWordToken() throws IOException {
		StringBuffer sb = new StringBuffer();
		char c = (char)pr.read();
		col++;
		
		while(isWordChar(c)) {
			sb.append(c);
			c = (char)pr.read();
			col++;
		}
		
		// unread last character
		pr.unread(c);
		col--;
		
		return (modWord(sb)).toString();
	}
	
	
	/**
	 *  Utility method to read comments. At present, this method's return value
	 *	is not output, but just ignored.
	 *
	 * @return    	String containing a comment section 
	 */
	private synchronized String readComment() throws IOException, NexusParseException  {
		StringBuffer sb = new StringBuffer();
		int unbalanced = 0;
		char c = (char)pr.read();
		
		if(c == L_BRACKET) {
			unbalanced++;
			sb.append(c);
		}
		else {
			// this should not happen as we unread first... but just in case
			throw new NexusParseException("Error: first read character was not start of comment");
		}
		
		while(unbalanced != 0) {
			c = (char)pr.read();
			switch(c) {
				case L_BRACKET:
					unbalanced++;
					break;
				case R_BRACKET:
					unbalanced--;
					break;
				default:
					break;
			}
			
			sb.append(c);
		}
		
		return sb.toString();
	}
	
	
	/**
	 *  Utility method to alter case of a word
	 *
	 * @param  s	Input <code>StringBuffer</code>
	 * @return    	Modified s (lowercase, uppercase, or unmodified)
	 */
	private synchronized StringBuffer modWord(StringBuffer sb) {
		String temp = sb.toString();
		
		switch(wordMod) {
			case WORD_LOWERCASE:
				temp = temp.toLowerCase();
				break;
			case WORD_UPPERCASE:
				temp = temp.toUpperCase();
				break;
			default:
				break;
		}
		
		return new StringBuffer(temp);
	}
	
	
	/**
	 *  Utility method to alter case of a word
	 *
	 * @param  s	Input <code>String</code>
	 * @return    	Modified s (lowercase, uppercase, or unmodified)
	 */
	private synchronized String modWord(String s) {
		switch(wordMod) {
			case WORD_LOWERCASE:
				s = s.toLowerCase();
				break;
			case WORD_UPPERCASE:
				s = s.toUpperCase();
				break;
			default:
				break;
		}
		
		return s;
	}
	
	
	/**
	 *  Utility method to determine if the argument character is punctuation
	 *
	 * @param  c	Input character
	 * @return    	True if <code>c</code> is any of the punctuation characters 
	 */
	private synchronized boolean isPunctuation(char c) {
		switch (c) {
			case L_PARENTHESIS:
			case R_PARENTHESIS:
			case L_BRACKET:
			case R_BRACKET:
			case L_BRACE:
			case R_BRACE:
			case F_SLASH:
			case B_SLASH:
			case COMMA:
			case SEMI_COLON:
			case COLON:
			case EQUALS:
			case ASTERIX:
			case S_QUOTE:
			case D_QUOTE:
			case B_TICK:
			case ADDITION:
			case DASH:
			case L_THAN:
			case G_THAN:
			case HASH:
				return true;
			default:
				return false;
		}
	}


	/**
	 *  Utility method to determine if the input character is whitespace (i.e.
	 *	' ' or '\t')
	 *
	 * @param  c	Input character
	 * @return    	True if <code>c</code> is whitespace
	 */
	private synchronized boolean isWhiteSpace(char c) {
		switch (c) {
			case TAB:
			case SPACE:
				return true;
			default:
				return false;
		}
	}


	/**
	 *  Utility method to determine if the input character is a newline char (i.e.
	 *	'\r' or '\n')
	 *
	 * @param  c	Input character
	 * @return    	True if <code>c</code> is a new line char
	 */
	private synchronized boolean isNewLine(char c) {
		switch (c) {
			case L_FEED:
			case C_RETURN:
				return true;
			default:
				return false;
		}
	}
	
	
	/**
	 *  Utility method to determine if the input character a word char. Basically,
	 *	if its not punctuation, new line or whitespace then its a word character.
	 *
	 * @param  c	Input character
	 * @return    	True if <code>c</code> is a word character
	 */
	private synchronized boolean isWordChar(char c) {
		if(isPunctuation(c)) { return false; }
		if(isWhiteSpace(c))  { return false; }
		if(isNewLine(c)) 	 { return false; }
		
		return true;
	}

}

