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
package org.dataminx.dts.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A collections of date-oriented methods surrounding the use of the {@link java.util.Calendar} and
 * {@link java.util.Date} classes.
 *
 * @author Alex Arana
 */
public final class DateUtils {

    /**
     * Converts an instance of {@link java.util.Date} object to {@link java.util.Calendar}.
     *
     * @param date
     *            date object to convert.
     * @return Converted <code>Calendar</code> object.
     */
    public static Calendar toCalendar(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Creates an instance of {@link java.util.Calendar} from the given time in milliseconds.
     * <p>
     * The input milliseconds value represents the specified number of milliseconds since the standard base time known
     * as "the epoch", namely January 1, 1970, 00:00:00 GMT.
     *
     * @param millis
     *            A given time corresponding to the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @return Converted <code>Calendar</code> object.
     */
    public static Calendar toCalendar(final long millis) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    /**
     * Converts a given time as an instance of {@link java.util.Date} into a {@link XMLGregorianCalendar} object.
     *
     * @param date
     *            A given time as an instance of <code>java.util.Date</code>
     * @return A new instance of <code>XMLGregorianCalendar</code> representing the input time
     */
    public static XMLGregorianCalendar toXmlGregorianCalendar(final Date date) {
        return toXmlGregorianCalendar(date.getTime());
    }

    /**
     * Converts a given time in milliseconds into a {@link XMLGregorianCalendar} object.
     * <p>
     * The input milliseconds value represents the specified number of milliseconds since the standard base time known
     * as "the epoch", namely January 1, 1970, 00:00:00 GMT.
     *
     * @param date
     *            A given time corresponding to the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * @return A new instance of <code>XMLGregorianCalendar</code> representing the input time
     */
    public static XMLGregorianCalendar toXmlGregorianCalendar(final long date) {
        try {
            final GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(
                calendar);
        }
        catch (final DatatypeConfigurationException ex) {
            throw new DateConversionException(
                String
                    .format(
                        "Unable to convert date '%s' to an XMLGregorianCalendar object",
                        date), ex);
        }
    }
}
