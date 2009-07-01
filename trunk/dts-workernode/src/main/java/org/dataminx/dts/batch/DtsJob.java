/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

import org.dataminx.dts.service.JobNotificationService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.StartLimitExceededException;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Base class implementation of the DTS worker node job.
 *
 * @author Alex Arana
 */
@Component
public abstract class DtsJob extends SimpleJob {
    /** The unique DTS job identifier. */
    private final String mJobId;

    /** A reference to the application's job notification service. */
    @Autowired
    private JobNotificationService mJobNotificationService;

    /**
     * Constructs a new instance of {@link DtsJob} using the specified job identifier.
     * <p>
     * The input job ID <em>must</em> be unique across the entire job repository.
     *
     * @param jobId Unique identifier for this job.
     */
    public DtsJob(final String jobId) {
        mJobId = jobId;
    }

    /**
     * {@inheritDoc}
     */
    public abstract void doExecute(JobExecution execution)
        throws JobInterruptedException, JobRestartException, StartLimitExceededException;

    /**
     * Returns the ID that uniquely identifies this job.
     *
     * @return Job ID
     */
    public String getJobId() {
        return mJobId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getName();

    /**
     * Returns a human-readable description of this DTS Job.
     * @return Job description as a {@link String}
     */
    public abstract String getDescription();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRestartable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobParametersIncrementer getJobParametersIncrementer() {
        return null;
    }

    public JobNotificationService getJobNotificationService() {
        return mJobNotificationService;
    }
}
