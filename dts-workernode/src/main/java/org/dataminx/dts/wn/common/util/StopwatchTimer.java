/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;


/**
 * Implements a simple stop watch timer class.
 * <p>
 * The basic usage is:
 * <pre>
 *   final StopwatchTimer stopwatch = new StopwatchTimer();
 *   // do something....
 *   stopwatch.getElapsedTime();
 * </pre>
 *
 * @author Alex Arana
 */
public class StopwatchTimer {
    /** Conversion factor from nanoseconds to milliseconds. */
    private static final int MILLI_TO_NANO = 1000000;

    /** The initial wall time to use in elapsed time calculations. */
    private final long mStartTime = System.nanoTime();

    /**
     * Returns the elapsed time from when the instance was created, and when the method was called.
     *
     * @return elapsed time in milliseconds.
     */
    public int getElapsedTime() {
        // Cast to int as there is no way the elapsed time in milliseconds exceeds 2^31.
        return (int) ((System.nanoTime() - mStartTime) / MILLI_TO_NANO);
    }

    /**
     * Returns the total running time formatted as a String as returned by
     * <code>DateUtils.formatElapsedTime()</code>.
     *
     * @return Total running time formatted as a String
     */
    public String getFormattedElapsedTime() {
        String duration = DurationFormatUtils.formatDuration(getElapsedTime(),
            "d' days 'H' hours 'm' minutes 's' seconds 'S' millis'");

        // this is a temporary marker on the front. Like ^ in regexp.
        duration = " " + duration;
        String tmp = StringUtils.replaceOnce(duration, " 0 days", "");
        if (tmp.length() != duration.length()) {
            duration = tmp;
            tmp = StringUtils.replaceOnce(duration, " 0 hours", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                tmp = StringUtils.replaceOnce(duration, " 0 minutes", "");
                if (tmp.length() != duration.length()) {
                    duration = StringUtils.replaceOnce(tmp, " 0 seconds", "");
                }
            }
        }

        // handle plurals
        duration = StringUtils.replaceOnce(duration, " 1 seconds", " 1 second");
        duration = StringUtils.replaceOnce(duration, " 1 minutes", " 1 minute");
        duration = StringUtils.replaceOnce(duration, " 1 hours", " 1 hour");
        duration = StringUtils.replaceOnce(duration, " 1 days", " 1 day");
        return duration.trim();
    }
}
