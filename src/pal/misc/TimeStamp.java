// TimeStamp.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.PrintWriter;
import java.util.Date;


/**
 * keeps track of program runtime
 *
 * @author Korbinian Strimmer
 */
public class TimeStamp {
    //
    // Public stuff
    //

    /**
     * create object and start timer
     */
    public TimeStamp() {
        startDate = new Date();
        startTime = startDate.getTime();
        stopDate = null;
    }

    /**
     * stop timer
     */
    public void stop() {
        stopDate = new Date();
        stopTime = stopDate.getTime();

        runSecs = Math.round((stopTime - startTime) / 100.0) / 10.0;

    }

    /**
     * report on runtime
     *
     * @param out output stream
     */
    public void report(PrintWriter out) {
        if (stopDate == null) return;

        out.println("Date: " + startDate.toString());
        out.println("Runtime: " + runSecs + " seconds");
    }

    //
    // Private stuff
    //

    private double runSecs;
    private int status;
    private Date startDate, stopDate;
    private long startTime, stopTime;
}
