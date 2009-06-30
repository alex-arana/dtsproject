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
    /** A reference to the application's job notification service. */
    @Autowired
    private JobNotificationService mJobNotificationService;

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
    public abstract String getJobId();

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
