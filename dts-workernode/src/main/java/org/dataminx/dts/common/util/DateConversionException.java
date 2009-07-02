/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.common.util;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs while performing a date manipulation routine.
 *
 * @author Alex Arana
 */
public class DateConversionException extends DtsException {
    /**
     * Constructs an instance of {@link DateConversionException}.
     */
    public DateConversionException() {
        super();
    }

    /**
     * Constructs an instance of {@link DateConversionException} with a specified message.
     *
     * @param message the detail message.
     */
    public DateConversionException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DateConversionException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DateConversionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
