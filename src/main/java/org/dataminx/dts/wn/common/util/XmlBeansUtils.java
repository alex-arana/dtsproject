/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.common.util;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.springframework.util.Assert;

/**
 * Collection of helper methods to manipulate XMLBeans data types.
 *
 * @author Alex Arana
 */
public final class XmlBeansUtils {

    /**
     * Selects all child elements of the specified context element matching the given qualified name.
     *
     * @param context Context element to lookup children elements of
     * @param name The qualified name of the elements to be selected
     * @return Returns the contents of the selected elements in a <code>List</code>
     */
    public static List<XmlObject> selectAnyElements(final XmlObject context, final QName name) {
        Assert.notNull(context);
        Assert.notNull(name);
        final List<XmlObject> list = new ArrayList<XmlObject>();
        final XmlObject[] elements = context.selectChildren(name);
        if (!ArrayUtils.isEmpty(elements)) {
            CollectionUtils.addAll(list, elements);
        }
        return list;
    }

    /**
     * Selects the first child element of the specified context element matching the given qualified name.
     *
     * @param context Context element to query
     * @param name The qualified name of the elements to be selected
     * @return Returns the contents of the selected child element
     */
    public static XmlObject selectAnyElement(final XmlObject context, final QName name) {
        final List<XmlObject> elements = selectAnyElements(context, name);
        return CollectionUtils.isEmpty(elements) ? null : elements.get(0);
    }


    /**
     * Extracts the text content of an XML element.
     *
     * @param source the XML element
     * @return the text content, or an empty string if the input element has no content
     */
    public static String extractElementTextAsString(final XmlObject source) {
        XmlCursor cursor = null;
        try {
            cursor = source.newCursor();
            while (cursor.hasNextToken()) {
                if (cursor.toNextToken().equals(XmlCursor.TokenType.TEXT)) {
                    //TODO handle whitespace?
                    return cursor.getChars();
                }
            }
            return EMPTY;
        }
        finally {
            cursor.dispose();
        }
    }
}
