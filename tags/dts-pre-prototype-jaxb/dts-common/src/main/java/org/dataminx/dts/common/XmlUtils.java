/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.common;

import java.io.ByteArrayOutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.dataminx.dts.DtsException;
import org.w3c.dom.Document;

/**
 * @author Alex Arana
 */
public final class XmlUtils {
    /**
     * The indent amount of characters to use when indenting is enabled.
     * <p>Defaults to "2".
     */
    public static final int DEFAULT_INDENT_AMOUNT = 2;

    /**
     * Prevent public instantiation of this class.
     */
    protected XmlUtils() {

    }

    /**
     * Returns a String representation of the given XML document.
     *
     * @param document An XML document as an instance of {@link Document}
     * @return a String representation of the given XML document
     * @throws DtsException if an error occurs serialising the input document
     */
    public static String documentToString(final Document document) throws DtsException {
        if (document == null) {
            throw new NullPointerException("Document is null in call to documentToString()");
        }

        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Xalan-specific, but this it will simply be ignored by other implementations
            transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", String.valueOf(DEFAULT_INDENT_AMOUNT));
            transformer.transform(new DOMSource(document), new StreamResult(stream));
            return stream.toString();
        }
        catch (final TransformerException ex) {
            // this is fatal, just dump the stack and throw a runtime exception
            throw new DtsException(
                "A transformation error occurred serialising the input Document to a String: " + ex, ex);
        }
    }
}
