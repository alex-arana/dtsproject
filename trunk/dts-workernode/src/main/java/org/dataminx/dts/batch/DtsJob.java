/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.batch;

import org.dataminx.dts.service.FileCopyingService;
import org.dataminx.dts.service.JobNotificationService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Base class implementation of the DTS worker node job.
 *
 * @author Alex Arana
 */
@Component("dtsFileTransferJob")
@Scope("prototype")
public class DtsJob implements Job {
    /** A unique identifier for this job instance. */
    private final String mJobId;

    /** A reference to the application's file copying service. */
    @Autowired
    private FileCopyingService mFileCopyingService;

    /** A reference to the application's job notification service. */
    @Autowired
    private JobNotificationService mJobNotificationService;

    /**
     * Constructs a new instance of {@link DtsJob} using the supplied Job ID.
     *
     * @param jobId A unique job ID
     */
    public DtsJob(final String jobId) {
        mJobId = jobId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final JobExecution execution) {
        //TODO break the job into steps
        mJobNotificationService.notifyJobStatus(getJobId(), "STARTED");
        mFileCopyingService.copyFiles();
        mJobNotificationService.notifyJobStatus(getJobId(), "FINISHED");
    }

    public String getJobId() {
        return mJobId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getJobId();
    }

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
}
