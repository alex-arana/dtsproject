/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

import org.dataminx.dts.service.FileCopyingService;
import org.dataminx.dts.service.JobNotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Base class implementation of the DTS worker node job.
 *
 * @author Alex Arana
 */
@Component
public abstract class DtsJob implements Job {
    /** A reference to the application's file copying service. */
    @Autowired
    private FileCopyingService mFileCopyingService;

    /** A reference to the application's job notification service. */
    @Autowired
    private JobNotificationService mJobNotificationService;

    /**
     * {@inheritDoc}
     */
    public abstract void execute(final JobExecution execution);

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

    public FileCopyingService getFileCopyingService() {
        return mFileCopyingService;
    }

    public JobNotificationService getJobNotificationService() {
        return mJobNotificationService;
    }
}
