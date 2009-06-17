/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.service;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs while executing a file copy operation.
 *
 * @author Alex Arana
 */
public class DtsFileCopyOperationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsFileCopyOperationException}.
     */
    public DtsFileCopyOperationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsFileCopyOperationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsFileCopyOperationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsFileCopyOperationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsFileCopyOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsFileCopyOperationException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsFileCopyOperationException(final Throwable cause) {
        super(cause);
    }
}
