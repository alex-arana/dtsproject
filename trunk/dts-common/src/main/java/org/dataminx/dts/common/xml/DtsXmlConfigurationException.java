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
public class DtsXmlConfigurationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsXmlConfigurationException}.
     */
    public DtsXmlConfigurationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsXmlConfigurationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsXmlConfigurationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsXmlConfigurationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsXmlConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsXmlConfigurationException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsXmlConfigurationException(final Throwable cause) {
        super(cause);
    }
}
