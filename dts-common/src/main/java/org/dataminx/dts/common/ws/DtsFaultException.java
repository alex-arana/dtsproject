/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
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
package org.dataminx.dts.common.ws;

import java.util.Date;

/**
 * The DtsFaultException is the base class of all the fault related exceptions that can be thrown by the DTS WS.
 *
 * @author Gerson Galang
 */
public class DtsFaultException extends RuntimeException {

    /** The m timestamp. */
    private Date mTimestamp;

    /**
     * Constructs an instance of {@link DtsFaultException}.
     */
    public DtsFaultException() {
        super();
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsFaultException} given the specified message.
     *
     * @param msg the exception message
     */
    public DtsFaultException(String msg) {
        super(msg);
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsFaultException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public DtsFaultException(String msg, Throwable cause) {
        super(msg, cause);
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link CustomException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public DtsFaultException(Throwable cause) {
        super(cause);
        setTimestamp(new Date());
    }

    /**
     * Sets the timestamp on when the fault occurred.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(Date timestamp) {
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
