// ReadDistanceMatrix.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.distance;

import pal.io.FormattedInput;
import pal.io.InputSource;
import pal.misc.IdGroup;
import pal.misc.Identifier;
import pal.misc.SimpleIdGroup;

import java.io.IOException;
import java.io.PushbackReader;


/**
 * reads pairwise distance matrices in PHYLIP format
 * (full matrix)
 *
 * @author Korbinian Strimmer
 * @author Alexei Drummond
 * @version $Id: ReadDistanceMatrix.java,v 1.4 2002/12/05 04:27:28 matt Exp $
 */
public class ReadDistanceMatrix extends DistanceMatrix {
    //
    // Public stuff
    //

    /**
     * read from stream
     */
    public ReadDistanceMatrix(PushbackReader input)
            throws DistanceParseException {
        readSquare(input);
    }

    /**
     * read from file
     */
    public ReadDistanceMatrix(String file)
            throws DistanceParseException, IOException {
        PushbackReader input = InputSource.openFile(file);
        readSquare(input);
        input.close();
    }


    //
    // Private stuff
    //

    // Read square matrix
    private void readSquare(PushbackReader in)
            throws DistanceParseException {
        FormattedInput fi = FormattedInput.getInstance();
        try {
            // Parse PHYLIP header line
            int numSeqs = fi.readInt(in);
            fi.nextLine(in);

            // Read distance and sequence names
            double[][] distance = new double[numSeqs][numSeqs];
            IdGroup idGroup = new SimpleIdGroup(numSeqs);
            for (int i = 0; i < numSeqs; i++) {
                idGroup.setIdentifier(i, new Identifier(fi.readLabel(in, 10)));
                for (int j = 0; j < numSeqs; j++) {
                    distance[i][j] = fi.readDouble(in);
                }
                fi.nextLine(in);
            }
            setIdGroup(idGroup);
            setDistances(distance);
        } catch (IOException e) {
            throw new DistanceParseException("IO error");
        } catch (NumberFormatException e) {
            throw new DistanceParseException("Number format error");
        }
    }
}
