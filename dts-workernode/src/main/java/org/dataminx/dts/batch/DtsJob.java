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
