/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.common.util;

import java.io.ByteArrayOutputStream;
import javax.xml.transform.stream.StreamResult;

/**
 * Convenient subclass of <code>StreamResult</code> that writes to a {@link ByteArrayOutputStream} output
 * stream.
 * <p>
 * The resulting stream can be retrieved as a byte array via the {@link ByteArrayResult#toBytes()}
 * method.
 */
public class ByteArrayResult extends StreamResult {
    /** The underlying output stream managed by this class. */
    private final ByteArrayOutputStream mByteArrayOutputStream;

    /**
     * Default constructor for instances of <code>ByteArrayResult</code>.
     */
    public ByteArrayResult() {
        super();
        mByteArrayOutputStream = new ByteArrayOutputStream();
        setOutputStream(mByteArrayOutputStream);
    }

    /**
     * Returns a newly allocated byte array holding the current contents of the underlying output stream.
     *
     * @return  the current contents of this output stream, as a byte array.
     */
    public byte[] toBytes() {
        return mByteArrayOutputStream.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return mByteArrayOutputStream.toString();
    }
}
