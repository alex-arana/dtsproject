/**
 * Copyright (c) 2010, VeRSI Consortium
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
package org.dataminx.dts.batch;

import org.dataminx.dts.DtsException;

/**
 * The NoAvailableConnectionException is an Exception thrown when there is no available connections (FileSystemManagers)
 * for a given source/destination to use. During the max stream counter task step, connections available to the source
 * or destination are cached and made available to the FileCopyTask during the file transfer process. If by the time
 * the FileCopyTask starts processing a source/destination and finds out that there's no FileSystemManager available
 * for the source/destination in the cache, then this Exception is thrown.
 *
 * @author Gerson Galang
 */
public class NoAvailableConnectionException extends DtsException {
    /**
     * Constructs an instance of {@link NoAvailableConnectionException}.
     */
    public NoAvailableConnectionException() {
        super();
    }

    /**
     * Constructs an instance of {@link NoAvailableConnectionException} with a
     * specified message.
     *
     * @param message the detail message.
     */
    public NoAvailableConnectionException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link NoAvailableConnectionException} with the
     * specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public NoAvailableConnectionException(final String message,
        final Throwable cause) {
        super(message, cause);
    }
}
