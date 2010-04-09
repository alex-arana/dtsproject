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
package org.dataminx.dts.batch.common;

/**
 * Defines a common set of constants global to the DTS Worker Node application.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public final class DtsBatchJobConstants {

    /** A job execution context key used to hold a DTS job request. */
    public static final String DTS_SUBMIT_JOB_REQUEST_KEY = "SUBMIT_JOB_REQUEST";

    /** A job execution context key used to hold the Job Resource Key. */
    public static final String DTS_JOB_RESOURCE_KEY = "JOB_RESOURCE_KEY";

    /** A step execution context key used to hold a data staging element. */
    public static final String DTS_DATA_TRANSFER_STEP_KEY = "DATA_TRANSFER_STEP";

    /** A job execution context used to hold the JobDetails object. */
    public static final String DTS_JOB_DETAILS = "JOB_DETAILS";

    /** A system properties key to the job step directory/folder. */
    public static final String DTS_JOB_STEP_DIRECTORY_KEY = "job.step.dir";

}
