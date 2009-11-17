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
package org.dataminx.dts.domain.model;

/**
 * The JobStatus enumeration type.
 *
 * @author Gerson Galang
 */
public enum JobStatus {

    /** Created. */
    CREATED(0, "Created"),

    /** Scheduled. */
    SCHEDULED(1, "Scheduled"),

    /** Transferring. */
    TRANSFERRING(2, "Transferring"),

    /** Done. */
    DONE(3, "Done"),

    /** Suspended. */
    SUSPENDED(4, "Suspended"),

    /** Failed. */
    FAILED(5, "Failed"),

    /** Failed cleanly. */
    FAILED_CLEAN(6, "Failed:Clean"),

    /** Failed but did not get to do a proper cleanup. */
    FAILED_UNCLEAN(7, "Failed:Unclean"),

    /** Failed with unknown reason for failure. */
    FAILED_UNKNOWN(8, "Failed:Unknown");

    /** The job status int representation. */
    private int mJobStatusInt;

    /** The job status String representation. */
    private String mJobStatusString;

    /**
     * Instantiates a new job status.
     *
     * @param jobStatusInt the integer representation of the job status
     * @param jobStatusStr the string representation of the job status
     */
    JobStatus(int jobStatusInt, String jobStatusStr) {
        mJobStatusInt = jobStatusInt;
        mJobStatusString = jobStatusStr;
    }

    /**
     * Gets the int value the JobStatus.
     *
     * @return the int value of the JobStatus
     */
    public int getIntValue() {
        return mJobStatusInt;
    }

    /**
     * Gets the string value representation of the JobStatus as specified in the
     * MINX XSD schema. Note that this is not the same as the String returned by
     * Enum's toString method.
     *
     * @return the string value representation of the JobStatus as specified in the
     * MINX XSD schema
     */
    public String getStringValue() {
        return mJobStatusString;
    }
}
