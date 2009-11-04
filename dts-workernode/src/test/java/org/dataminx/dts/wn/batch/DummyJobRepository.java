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
