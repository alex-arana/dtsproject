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
package org.dataminx.dts;

import java.util.Date;

/**
 * <p>DtsException</p>
 * <p>Description: Base exception class for the DTS package.</p>
 *
 * @author Alex Arana
 */
public class DtsException extends RuntimeException {

    /** The timestamp. */
    private Date mTimestamp;

    /**
     * Constructs an instance of {@link DtsException}.
     */
    public DtsException() {
        super();
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsException(final String message) {
        super(message);
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsException(final String message, final Throwable cause) {
        super(message, cause);
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsException(final Throwable cause) {
        super(cause);
        setTimestamp(new Date());
    }

    /**
     * Sets the timestamp on when the fault occurred.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(final Date timestamp) {
        mTimestamp = timestamp;
    }

    /**
     * Returns the date on when the fault/exception was thrown on the DTS Web Service.
     *
     * @return the date on when the fault/exception was thrown on the DTS Web Service
     */
    public Date getTimestamp() {
        return mTimestamp;
    }
}
