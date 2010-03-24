package org.dataminx.dts.batch;

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_DATA_TRANSFER_STEP_KEY;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_DETAILS;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_SUBMIT_JOB_REQUEST_KEY;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.dataminx.dts.batch.common.util.ExecutionContextCleaner;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The <code>DtsFileTransferJobListener</code> is the
 * {@link JobExecutionListener} to the {@link DtsFileTransferJob}.
 * 
 * @author Gerson Galang
 */
public class DtsFileTransferJobListener implements JobExecutionListener, InitializingBean {

    private FileSystemManagerCache mFileSystemManagerCache;

    private JobExplorer mJobExplorer;

    private ExecutionContextCleaner mExecutionContextCleaner;

    private static final Log LOGGER = LogFactory.getLog(DtsFileTransferJobListener.class);

    public void afterJob(final JobExecution jobExecution) {
        // we'll close the connections as the next time we restart the job, we'll need to check for the
        // max number of connections again.
        mFileSystemManagerCache.clear();

        // TODO: need to give a little more thought if this is really the right way of handling credentials
        // in Spring Batch...

        // let's try and remove entries from the last job and its corresponding step executions 
        // that have failed last time
        final List<JobExecution> prevJobExecutions = mJobExplorer.getJobExecutions(jobExecution.getJobInstance());

        for (final JobExecution jobEx : prevJobExecutions) {

            // we'll only remove the following attributes from the JobExecutionContext if the jobEx is not the latest
            // JobExecution. we still need to keep JOB_DETAILS and SUBMIT_JOB_REQUEST in the latest JobExecution so 
            // future restarts to a failed job will still be able to hand these attributes to the new JobExecution.
            if (!jobEx.equals(jobExecution)) {

                mExecutionContextCleaner.removeJobExecutionContextEntry(jobEx, DTS_JOB_DETAILS);
                mExecutionContextCleaner.removeJobExecutionContextEntry(jobEx, DTS_SUBMIT_JOB_REQUEST_KEY);

                // now let's see if any of the steps belonging to this job execution have some of the above 
                // properties persisted in their ExecutionContext
                mExecutionContextCleaner.forceRemoveStepExecutionContextEntries(jobEx.getStepExecutions(),
                        new String[] { DTS_DATA_TRANSFER_STEP_KEY });

            }
        }

        // let's do a clean up of the ExecutionContexts having details of the users credentials
        // when the job completes successfully
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            mExecutionContextCleaner.removeJobExecutionContextEntry(jobExecution, DTS_JOB_DETAILS);
            mExecutionContextCleaner.removeJobExecutionContextEntry(jobExecution, DTS_SUBMIT_JOB_REQUEST_KEY);
            removeJobStepFiles(jobExecution.getExecutionContext().getString(DtsBatchJobConstants.DTS_JOB_RESOURCE_KEY));
        }
    }

    private void removeJobStepFiles(final String jobResourceKey) {
        LOGGER.debug("Deleting job step files.");
        final File jobStepDirectory = new File(System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY));
        final File[] jobStepFiles = jobStepDirectory.listFiles(new FilenameFilter() {
            public boolean accept(final File dir, final String name) {
                if (name.startsWith(jobResourceKey) && name.endsWith("dts")) {
                    return true;
                }
                return false;
            }
        });
        for (final File jobStepFile : jobStepFiles) {
            jobStepFile.delete();
        }
    }

    public void beforeJob(final JobExecution jobExecution) {
        // do nothing
    }

    public void setFileSystemManagerCache(final FileSystemManagerCache fileSystemManagerCache) {
        mFileSystemManagerCache = fileSystemManagerCache;
    }

    public void setJobExplorer(final JobExplorer jobExplorer) {
        mJobExplorer = jobExplorer;
    }

    public void setExecutionContextCleaner(final ExecutionContextCleaner executionContextCleaner) {
        mExecutionContextCleaner = executionContextCleaner;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(mFileSystemManagerCache != null, "FileSystemManagerCache has not been set.");
        Assert.state(mJobExplorer != null, "JobExplorer has not been set.");
        Assert.state(mExecutionContextCleaner != null, "ExecutionContextCleaner has not been set.");
    }
}
