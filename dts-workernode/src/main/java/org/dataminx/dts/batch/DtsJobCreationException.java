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
package org.dataminx.dts.batch;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs when failing to create a new instance of DTSJob.
 *
 * @author Alex Arana
 */
public class DtsJobCreationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public DtsJobCreationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsJobCreationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsJobCreationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsJobCreationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsJobCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
