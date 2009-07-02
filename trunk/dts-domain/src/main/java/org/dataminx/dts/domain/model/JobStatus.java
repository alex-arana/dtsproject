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
