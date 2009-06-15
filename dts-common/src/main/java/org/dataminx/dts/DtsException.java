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
package org.dataminx.dts;

/**
 * <p>DtsException</p>
 * <p>Description: Base exception class for the DTS package.</p>
 *
 * @author Alex Arana
 */
public class DtsException extends RuntimeException {
    /**
     * Constructs an instance of {@link DtsException}.
     */
    public DtsException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsException(final Throwable cause) {
        super(cause);
    }
}
