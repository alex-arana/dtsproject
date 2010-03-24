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

public class CheckRequirementsTask implements Tasklet {

    private Resource mJobStepDirectory;

    private static final Log LOGGER = LogFactory.getLog(CheckRequirementsTask.class);

    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext)
            throws Exception {

        if (System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY) != null) {
            LOGGER.debug("Using the user provided " + DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY + " property");
            mJobStepDirectory = new FileSystemResource(System
                    .getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY));
        }
        else {
            // TODO: should we really have this step implemented as a real Spring Batch step?
            // see if we can get the workernode or broker CLI runner be the one doing this bit.

            if (mJobStepDirectory == null || !mJobStepDirectory.exists()) {
                final File jobStepDirectoryFile = new File(System.getProperty("user.home") + "/"
                        + DtsConstants.DEFAULT_DATAMINX_CONFIGURATION_DIR + "/jobsteps/");
                if (jobStepDirectoryFile.mkdir()) {
                    mJobStepDirectory = new FileSystemResource(jobStepDirectoryFile);
                }
            }
        }

        if (!isJobStepDirectoryADirectory(mJobStepDirectory)) {
            throw new UnsatisfiedRequirementsException("Directory to store the job steps does not exist.");
        }

        if (System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY) == null) {
            System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY, mJobStepDirectory.getFile()
                    .getAbsolutePath());
        }
        return RepeatStatus.FINISHED;
    }

    private boolean isJobStepDirectoryADirectory(final Resource jobStepDirectory) {
        Assert.notNull(jobStepDirectory);
        try {
            jobStepDirectory.getFile().isDirectory();
            return true;
        } catch (final IOException ex) {
            return false;
        }
    }

    public void setJobStepDirectory(final Resource jobStepDirectory) {
        mJobStepDirectory = jobStepDirectory;
    }

}
