package org.dataminx.dts.batch;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * This class holds all the details about the DTS Job that Spring Batch will go
 * through to process the data transfer requests.
 * 
 * @author Gerson Galang
 */
public class DtsJobDetails implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DtsJobDetails.class);

    private static final long serialVersionUID = 1L;

    private List<DtsJobStep> mJobSteps = null;
    private int mBytesTransferred = 0;
    private long mTotalBytes = 0;
    private int mTotalFiles = 0;
    private JobDefinitionType mJobDefinition = null;
    private String mJobId;
    private List<String> mExcludedFiles = new ArrayList<String>();

    /**
     * Holds the maximum number of files to be transferred from each Source or
     * Target element.
     */
    private final Map<String, Integer> mSourceTargetMaxTotalFilesToTransfer = new HashMap<String, Integer>();

    public List<String> getExcludedFiles() {
        return mExcludedFiles;
    }

    public void setExcludedFiles(final List<String> excludedFiles) {
        mExcludedFiles = excludedFiles;
    }

    public DtsJobDetails() {
        mJobSteps = new ArrayList<DtsJobStep>();
        mExcludedFiles = new ArrayList<String>();
        mJobId = "";
    }

    public String getJobId() {
        return mJobId;
    }

    public void setJobId(final String jobId) {
        mJobId = jobId;
    }

    public int getTotalFiles() {
        return mTotalFiles;
    }

    public void setTotalFiles(final int totalFiles) {
        mTotalFiles = totalFiles;
    }

    public JobDefinitionType getJobDefinition() {
        return mJobDefinition;
    }

    public void setJobDefinition(final JobDefinitionType jobDefinition) {
        mJobDefinition = jobDefinition;
    }

    public List<DtsJobStep> getJobSteps() {
        return mJobSteps;
    }

    public void saveJobSteps(final List<DtsJobStep> jobSteps) {
        LOGGER.debug("DtsJobDetails saveJobSteps()");

        // write the DataTransferUnits held by each of the steps and also add the filename where the DataTransferUnits
        // were written to the JobStep object
        for (final DtsJobStep jobStep : jobSteps) {
            final String filename = System.getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY) + "/" + mJobId
                    + "-" + jobStep.getStepId() + ".dts";
            writeJobStepToFile(filename, jobStep);
            jobStep.setJobStepFilename(filename);
        }

        mJobSteps = jobSteps;
    }

    public int getBytesTransferred() {
        return mBytesTransferred;
    }

    public synchronized void addBytesTransferred(final int bytesTransferred) {
        mBytesTransferred += bytesTransferred;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    public void setTotalBytes(final long totalBytes) {
        mTotalBytes = totalBytes;
    }

    public boolean isCompleted() {
        return mBytesTransferred == mTotalBytes;
    }

    public Map<String, Integer> getSourceTargetMaxTotalFilesToTransfer() {
        return mSourceTargetMaxTotalFilesToTransfer;
    }

    private void writeJobStepToFile(final String filename, final DtsJobStep jobStep) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename);
        } catch (final FileNotFoundException e) {
            Assert.isTrue(true,
                    "FileNotFoundException was thrown while creating a step file to store the DataTransferUnits.");
        }
        for (final DtsDataTransferUnit dataTransferUnit : jobStep.getDataTransferUnits()) {
            writer.print(dataTransferUnit.getSourceFileURI() + ";");
            writer.print(dataTransferUnit.getDestinationFileURI() + ";");
            writer.print(dataTransferUnit.getDataTransferIndex() + ";");
            writer.println(dataTransferUnit.getSize() + ";");
        }
        writer.close();
    }

}
