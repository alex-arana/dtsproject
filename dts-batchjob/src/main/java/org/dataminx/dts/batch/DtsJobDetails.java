package org.dataminx.dts.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

/**
 * This class holds all the details about the DTS Job that Spring Batch will go
 * through to process the data transfer requests.
 * 
 * @author Gerson Galang
 */
public class DtsJobDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DtsJobStep> mJobSteps = null;
    private int mBytesTransferred = 0;
    private long mTotalBytes = 0;
    private int mTotalFiles = 0;
    private JobDefinitionType mJobDefinition = null;
    private String mJobId;
    private List<String> mExcludedFiles = new ArrayList<String>();

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

    public void setJobSteps(final List<DtsJobStep> jobSteps) {
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

}
