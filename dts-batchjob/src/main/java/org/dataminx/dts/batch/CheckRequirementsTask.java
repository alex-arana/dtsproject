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
package org.dataminx.dts.batch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.dataminx.dts.common.DtsConstants;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * The CheckRequirementsTask will check for the existence of the Job Step directory where the list of files to be
 * transferred will be written to.
 *
 * @author Gerson Galang
 */
public class CheckRequirementsTask implements Tasklet {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(CheckRequirementsTask.class);

    /** The Job Step directory. */
    private Resource mJobStepDirectory;

    /**
     * {@inheritDoc}
     */
    public RepeatStatus execute(final StepContribution stepContribution,
        final ChunkContext chunkContext) throws Exception {

        if (System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY) != null) {
            LOGGER
                .debug("Using the user provided "
                    + DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY
                    + " property");
            mJobStepDirectory = new FileSystemResource(new File(System
                .getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY)));
        }
        else {
            // TODO: should we really have this step implemented as a real Spring Batch step?
            // see if we can get the workernode or broker CLI runner be the one doing this bit.

            if ((mJobStepDirectory == null) || !mJobStepDirectory.exists()) {
                final File jobStepDirectoryFile = new File(System
                    .getProperty("user.home")
                    + "/"
                    + DtsConstants.DEFAULT_DATAMINX_CONFIGURATION_DIR
                    + "/jobsteps/");
                if (jobStepDirectoryFile.mkdir()) {
                    mJobStepDirectory = new FileSystemResource(
                        jobStepDirectoryFile);
                }
            }
        }
        if (!isJobStepDirectoryFolder(mJobStepDirectory)) {
            throw new UnsatisfiedRequirementsException("The directory \""
                + mJobStepDirectory.getFile().getAbsolutePath()
                + "\"to store the job steps is not a directory.");
        }

        if (System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY) == null) {
            System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY,
                mJobStepDirectory.getFile().getAbsolutePath());
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * Checks if the job step directory is an actual folder.
     *
     * @param jobStepDirectory the jobStepDirectory resource
     * @return true if the jobStepDirectory is an actual folder, false if it's only a normal file
     */
    private boolean isJobStepDirectoryFolder(final Resource jobStepDirectory) {
        Assert.notNull(jobStepDirectory);
        try {
            return jobStepDirectory.getFile().isDirectory();
        }
        catch (final IOException ex) {
            return false;
        }
    }

    public void setJobStepDirectory(final Resource jobStepDirectory) {
        mJobStepDirectory = jobStepDirectory;
    }

}
