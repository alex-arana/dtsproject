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

import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.JobDescriptionType;
import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.springframework.batch.core.JobExecution;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Processes a DTS Job.
 *
 * @author Alex Arana
 */
@Component("dtsFileTransferJob")
@Scope("prototype")
public class DtsFileTransferJob extends DtsJob {
    /** Holds the information about the job request. */
    private final SubmitJobRequest mJobRequest;

    /**
     * Constructs a new instance of <code>DtsSubmitJob</code> using the specified job request details.
     *
     * @param jobRequest Job request details
     */
    public DtsFileTransferJob(final SubmitJobRequest jobRequest) {
        Assert.notNull(jobRequest, "Cannot construct a DTS submit job without the required job details.");
        mJobRequest = jobRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJobId() {
        return mJobRequest.getJobDefinition().getJobDescription().getJobIdentification().getJobName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getJobId();
    }

    /**
     * Returns the job description, containing all details about the underlying job.
     *
     * @return Job request details
     */
    public JobDescriptionType getJobDescription() {
        return mJobRequest.getJobDefinition().getJobDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final JobExecution execution) {
        //TODO break the job into steps
        getJobNotificationService().notifyJobStatus(getJobId(), "STARTED");

        // transfer all files that are part of this request
        for (final DataTransferType dataTransfer : getJobDescription().getDataTransfer()) {
            final SourceTargetType source = dataTransfer.getSource();
            final SourceTargetType target = dataTransfer.getTarget();

            //TODO pass the creation flags to the file copying service
            //final CreationFlagEnumeration creationFlag = dataTransfer.getTransferRequirements().getCreationFlag();
            getFileCopyingService().copyFiles(source.getURI(), target.getURI());
        }

        getJobNotificationService().notifyJobStatus(getJobId(), "FINISHED");
    }
}
