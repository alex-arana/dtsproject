/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

/**
 * A dummy implementation of the Spring Batch {@link JobRepository} interface that can be used while auto-wiring
 * in test configurations.
 *
 * @author Alex Arana
 */
@Component("jobRepository")
public class DummyJobRepository implements JobRepository {

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final StepExecution stepExecution) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution createJobExecution(final String jobName, final JobParameters jobParameters)
        throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobExecution getLastJobExecution(final String jobName, final JobParameters jobParameters) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StepExecution getLastStepExecution(final JobInstance jobInstance, final String stepName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStepExecutionCount(final JobInstance jobInstance, final String stepName) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isJobInstanceExists(final String jobName, final JobParameters jobParameters) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final JobExecution jobExecution) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final StepExecution stepExecution) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateExecutionContext(final StepExecution stepExecution) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateExecutionContext(final JobExecution jobExecution) {

    }
}
