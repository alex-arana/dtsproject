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

import org.dataminx.dts.wn.service.DtsWorkerNodeInformationService;
import org.dataminx.dts.wn.service.JobNotificationService;
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

    /** The time in milliseconds when the job started executing. */
    private long mStartTime;

    /** The time in milliseconds when the job completed executing. */
    private long mCompletedTime;

    /** A reference to the application's environment information service. */
    @Autowired
    private DtsWorkerNodeInformationService mDtsWorkerNodeInformationService;

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
     * Returns the time when this job started executing.
     *
     * @return job's execution start time in milliseconds
     */
    public long getStartTime() {
        return mStartTime;
    }

    /**
     * Convenience method that sets the job start time using the DTS WorkerNode's current time.
     */
    public void registerStartTime() {
        mStartTime = mDtsWorkerNodeInformationService.getCurrentTime().getTime();
    }

    public long getCompletedTime() {
        return mCompletedTime;
    }

    /**
     * Convenience method that sets the job completed time using the DTS WorkerNode's current time.
     */
    public void registerCompletedTime() {
        mCompletedTime = mDtsWorkerNodeInformationService.getCurrentTime().getTime();
    }

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

    public DtsWorkerNodeInformationService getDtsWorkerNodeInformationService() {
        return mDtsWorkerNodeInformationService;
    }

    public JobNotificationService getJobNotificationService() {
        return mJobNotificationService;
    }
}
