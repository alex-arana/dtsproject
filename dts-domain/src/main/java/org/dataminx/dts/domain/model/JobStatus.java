package org.dataminx.dts.domain.model;

/**
 * The JobStatus enumeration type.
 *
 * @author Gerson Galang
 */
public enum JobStatus {

    /** Created. */
    CREATED(0),

    /** Scheduled. */
    SCHEDULED(1),

    /** Transferring. */
    TRANSFERRING(2),

    /** Done. */
    DONE(3),

    /** Suspended. */
    SUSPENDED(4),

    /** Failed. */
    FAILED(5),

    /** Failed cleanly. */
    FAILED_CLEAN(6),

    /** Failed but did not get to do a proper cleanup. */
    FAILED_UNCLEAN(7),

    /** Failed with unknown reason for failure. */
    FAILED_UNKNOWN(8);

    /** The job status. */
    private int mkJobStatus;

    /**
     * Instantiates a new job status.
     *
     * @param jobStatus the job status
     */
    JobStatus(int jobStatus) {
        mkJobStatus = jobStatus;
    }

    /**
     * Gets the int value the JobStatus.
     *
     * @return the int value of the JobStatus
     */
    public int getIntValue() {
        return mkJobStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String currentName = name().toString();
        StringBuffer nameWithCorrectCase = new StringBuffer();
        int underscoreIndex = currentName.indexOf("_");

        // workaround so checkstyle doesn't complain with magic number comparisons
        nameWithCorrectCase.append(currentName.substring(0, 1).toUpperCase());

        if (underscoreIndex > 0) {
            // we definitely have a subtype of the Failed status
            nameWithCorrectCase.append(currentName.substring(1, underscoreIndex).toLowerCase());
            nameWithCorrectCase.append(":");
            nameWithCorrectCase.append(currentName.substring(underscoreIndex + 1, underscoreIndex + 2).toUpperCase());

        }
        nameWithCorrectCase.append(currentName.substring(underscoreIndex + 2).toLowerCase());
        return nameWithCorrectCase.toString();
    }
}
