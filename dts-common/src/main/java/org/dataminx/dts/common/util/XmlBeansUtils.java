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
     * Extracts the text content of an XML element.
     *
     * @param source
     *            the XML element
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

    /**
     * Selects the first child element of the specified context element matching the given qualified name.
     *
     * @param context
     *            Context element to query
     * @param name
     *            The qualified name of the elements to be selected
     * @return Returns the contents of the selected child element
     */
    public static XmlObject selectAnyElement(final XmlObject context,
        final QName name) {
        final List<XmlObject> elements = selectAnyElements(context, name);
        return CollectionUtils.isEmpty(elements) ? null : elements.get(0);
    }

    /**
     * Selects all child elements of the specified context element matching the given qualified name.
     *
     * @param context
     *            Context element to lookup children elements of
     * @param name
     *            The qualified name of the elements to be selected
     * @return Returns the contents of the selected elements in a <code>List</code>
     */
    public static List<XmlObject> selectAnyElements(final XmlObject context,
        final QName name) {
        Assert.notNull(context);
        Assert.notNull(name);
        final List<XmlObject> list = new ArrayList<XmlObject>();
        final XmlObject[] elements = context.selectChildren(name);
        if (!ArrayUtils.isEmpty(elements)) {
            CollectionUtils.addAll(list, elements);
        }
        return list;
    }

}
