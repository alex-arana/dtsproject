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
package org.dataminx.dts.common.xml;

import java.io.ByteArrayOutputStream;
import javax.xml.transform.stream.StreamResult;

/**
 * Convenient subclass of <code>StreamResult</code> that writes to a {@link ByteArrayOutputStream} output
 * stream.
 * <p>
 * The resulting stream can be retrieved as a byte array via the {@link ByteArrayResult#toBytes()}
 * method.
 *
 * @author Alex Arana
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
