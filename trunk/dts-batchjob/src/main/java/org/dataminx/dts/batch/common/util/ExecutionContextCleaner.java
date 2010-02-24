package org.dataminx.dts.batch.common.util;

import java.text.SimpleDateFormat;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The <code>ExecutionContextCleaner</code> is a utility class which provides
 * methods to remove ExecutionContext entries from Job and Step
 * <code>ExecutionContext</code>s.
 * 
 * @author Gerson Galang
 */
public class ExecutionContextCleaner implements InitializingBean {

    /** The JobRepository. */
    private JobRepository mJobRepository;

    /** The JobExplorer. */
    private JobExplorer mJobExplorer;

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(ExecutionContextCleaner.class);

    /** The Date formatter. */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

    /**
     * Removes the Job <code>ExecutionContext</code> entry for the given
     * <code>JobExecution</code>.
     * 
     * @param jobExecution the <code>JobExecution</code> where the given
     *        ExecutionContext key will be deleted from
     * @param key the key of the JobExecutionContext entry to be deleted
     */
    public void removeJobExecutionContextEntry(final JobExecution jobExecution, final String key) {
        if (jobExecution.getExecutionContext().remove(key) != null) {
            LOGGER.debug("Removed " + key + " from the JobExecutionContext of '"
                    + jobExecution.getJobInstance().getJobName() + "' job with start time of "
                    + dateFormat.format(jobExecution.getStartTime()));
        }
        mJobRepository.updateExecutionContext(jobExecution);
    }

    /**
     * Removes the Step <code>ExecutionContext</code> entry for the given
     * <code>StepExecution</code>.
     * 
     * @param stepExecution the <code>StepExecution</code> where the given
     *        ExecutionContext keys will be deleted from
     * @param key the key of the StepExecutionContext entry to be deleted
     */
    public void removeStepExecutionContextEntry(final StepExecution stepExecution, final String key) {
        if (stepExecution.getExecutionContext().remove(key) != null) {
            LOGGER.debug("Removed " + key + " from the StepExecutionContext of " + stepExecution.getStepName()
                    + " step");
        }
        mJobRepository.updateExecutionContext(stepExecution);
    }

    /**
     * Removes all the Step <code>ExecutionContext</code> entries for the given
     * Job executions without any consideration if the steps have successfully
     * completed or failed.
     * 
     * @param stepExecutions the <code>StepExecution</code>s where the given
     *        ExecutionContext keys will be deleted from
     * @param stepExecutionKeys the StepExecutionContext keys to be deleted
     */
    public void forceRemoveStepExecutionContextEntries(final Collection<StepExecution> stepExecutions,
            final String[] stepExecutionKeys) {
        if (stepExecutionKeys == null) {
            return;
        }
        for (final StepExecution stepExecution : stepExecutions) {
            for (int i = 0; i < stepExecutionKeys.length; i++) {
                removeStepExecutionContextEntry(stepExecution, stepExecutionKeys[i]);
            }
        }
    }

    /**
     * Removes all the Job <code>ExecutionContext</code> entries for the given
     * Job executions without any consideration if the job has successfully
     * completed or failed.
     * 
     * @param jobExecutions the <code>JobExecution</code>s where the given
     *        ExecutionContext keys will be deleted from
     * @param jobExecutionKeys the JobExecutionContext keys to be deleted
     */
    public void forceRemoveJobExecutionContextEntries(final Collection<JobExecution> jobExecutions,
            final String[] jobExecutionKeys) {
        if (jobExecutionKeys == null) {
            return;
        }
        for (final JobExecution jobExecution : jobExecutions) {
            for (int i = 0; i < jobExecutionKeys.length; i++) {
                removeJobExecutionContextEntry(jobExecution, jobExecutionKeys[i]);
            }
            mJobRepository.updateExecutionContext(jobExecution);
        }
    }

    /**
     * Removes all the Job and Step <code>ExecutionContext</code> entries for
     * the given Job executions without any consideration if the job and its
     * corresponding steps have successfully completed or failed.
     * 
     * @param jobExecutions the <code>JobExecution</code>s where the given
     *        ExecutionContext keys will be deleted from
     * @param jobExecutionKeys the JobExecutionContext keys to be deleted
     * @param stepExecutionKeys the StepExecutionContext keys to be deleted
     */
    public void forceRemoveExecutionContextEntries(final Collection<JobExecution> jobExecutions,
            final String[] jobExecutionKeys, final String[] stepExecutionKeys) {
        for (final JobExecution jobExecution : jobExecutions) {
            for (int i = 0; i < jobExecutionKeys.length; i++) {
                removeJobExecutionContextEntry(jobExecution, jobExecutionKeys[i]);
            }
            mJobRepository.updateExecutionContext(jobExecution);
            forceRemoveStepExecutionContextEntries(jobExecution.getStepExecutions(), stepExecutionKeys);
        }
    }

    /**
     * Removes all the Job and Step <code>ExecutionContext</code> entries for
     * the given <code>JobInstance</code>.
     * 
     * @param jobInstance the job instance where the given ExecutionContext keys
     *        will be deleted from
     * @param jobExecutionKeys the JobExecutionContext keys to be deleted
     * @param stepExecutionKeys the StepExecutionContext keys to be deleted
     */
    public void forceRemoveExecutionContextEntries(final JobInstance jobInstance, final String[] jobExecutionKeys,
            final String[] stepExecutionKeys) {
        Assert.notNull(mJobExplorer, "JobExplorer has not been set.");
        forceRemoveExecutionContextEntries(mJobExplorer.getJobExecutions(jobInstance), jobExecutionKeys,
                stepExecutionKeys);
    }

    /**
     * Sets the <code>JobRepository</code>.
     * 
     * @param jobRepository a reference to Spring Batch's
     *        <code>JobRepository</code>
     */
    public void setJobRepository(final JobRepository jobRepository) {
        mJobRepository = jobRepository;
    }

    /**
     * Sets the <code>JobExplorer</code>.
     * 
     * @param jobExplorer a reference to Spring Batch's <code>JobExplorer</code>
     */
    public void setJobExplorer(final JobExplorer jobExplorer) {
        mJobExplorer = jobExplorer;
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(mJobRepository != null, "JobRepository has not been set.");
    }
}
