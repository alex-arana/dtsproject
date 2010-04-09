/**
 * Copyright (c) 2010, VeRSI Consortium
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

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(ExecutionContextCleaner.class);

    /** The JobRepository. */
    private JobRepository mJobRepository;

    /** The JobExplorer. */
    private JobExplorer mJobExplorer;

    /** The Date formatter. */
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(
        "yyyy.MM.dd G 'at' HH:mm:ss z");

    /**
     * Removes the Job <code>ExecutionContext</code> entry for the given
     * <code>JobExecution</code>.
     *
     * @param jobExecution the <code>JobExecution</code> where the given
     *        ExecutionContext key will be deleted from
     * @param key the key of the JobExecutionContext entry to be deleted
     */
    public void removeJobExecutionContextEntry(final JobExecution jobExecution,
        final String key) {
        if (jobExecution.getExecutionContext().remove(key) != null) {
            LOGGER.debug("Removed " + key
                + " from the JobExecutionContext of '"
                + jobExecution.getJobInstance().getJobName()
                + "' job with start time of "
                + mDateFormat.format(jobExecution.getStartTime()));
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
    public void removeStepExecutionContextEntry(
        final StepExecution stepExecution, final String key) {
        if (stepExecution.getExecutionContext().remove(key) != null) {
            LOGGER.debug("Removed " + key
                + " from the StepExecutionContext of "
                + stepExecution.getStepName() + " step");
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
    public void forceRemoveStepExecutionContextEntries(
        final Collection<StepExecution> stepExecutions,
        final String[] stepExecutionKeys) {
        if (stepExecutionKeys == null) {
            return;
        }
        for (final StepExecution stepExecution : stepExecutions) {
            for (int i = 0; i < stepExecutionKeys.length; i++) {
                removeStepExecutionContextEntry(stepExecution,
                    stepExecutionKeys[i]);
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
    public void forceRemoveJobExecutionContextEntries(
        final Collection<JobExecution> jobExecutions,
        final String[] jobExecutionKeys) {
        if (jobExecutionKeys == null) {
            return;
        }
        for (final JobExecution jobExecution : jobExecutions) {
            for (int i = 0; i < jobExecutionKeys.length; i++) {
                removeJobExecutionContextEntry(jobExecution,
                    jobExecutionKeys[i]);
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
    public void forceRemoveExecutionContextEntries(
        final Collection<JobExecution> jobExecutions,
        final String[] jobExecutionKeys, final String[] stepExecutionKeys) {
        for (final JobExecution jobExecution : jobExecutions) {
            for (int i = 0; i < jobExecutionKeys.length; i++) {
                removeJobExecutionContextEntry(jobExecution,
                    jobExecutionKeys[i]);
            }
            mJobRepository.updateExecutionContext(jobExecution);
            forceRemoveStepExecutionContextEntries(jobExecution
                .getStepExecutions(), stepExecutionKeys);
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
    public void forceRemoveExecutionContextEntries(
        final JobInstance jobInstance, final String[] jobExecutionKeys,
        final String[] stepExecutionKeys) {
        Assert.notNull(mJobExplorer, "JobExplorer has not been set.");
        forceRemoveExecutionContextEntries(mJobExplorer
            .getJobExecutions(jobInstance), jobExecutionKeys, stepExecutionKeys);
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
