/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.common.xml;

import org.dataminx.dts.DtsException;

/**
 * Exception raised when an XML configuration error is encountered.
 *
 * @author Alex Arana
 */
public class DtsXmlTransformationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsXmlTransformationException}.
     */
    public DtsXmlTransformationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsXmlTransformationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsXmlTransformationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsXmlTransformationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsXmlTransformationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsXmlTransformationException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsXmlTransformationException(final Throwable cause) {
        super(cause);
    }
}
