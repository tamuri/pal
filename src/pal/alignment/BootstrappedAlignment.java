// BootstrappedAlignment.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.alignment;

import pal.math.*;


/**
 * generates bootstrapped alignments from a raw alignment
 *
 * @version $Id: BootstrappedAlignment.java,v 1.6 2003/03/23 00:12:57 matt Exp $
 *
 * @author Korbinian Strimmer
 */
public class BootstrappedAlignment extends AbstractAlignment
{
	//
	// Public stuff
	//

	/**
	 * Constructor
	 *
	 * @param raw original alignment
	 */
	public BootstrappedAlignment(Alignment raw)
	{
		rawAlignment = raw;

		numSeqs = raw.getSequenceCount();
		idGroup = raw;
		numSites = raw.getSiteCount();
		setDataType(raw.getDataType());

		alias = new int[numSites];
		urn = new UrnModel(numSites);

		bootstrap();
	}

	// Implementation of abstract Alignment method

	/** sequence alignment at (sequence, site) */
	public char getData(int seq, int site)
	{
		return rawAlignment.getData(seq, alias[site]);
	}


	/** bootstrap alignment */
	public void bootstrap()
	{
		for (int i = 0; i < numSites; i++)
		{
			alias[i] = urn.drawPutBack();
		}
	}


	//
	// Private stuff
	//

	private UrnModel urn;
	private Alignment rawAlignment;
	private int[] alias;


}
