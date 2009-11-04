/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
