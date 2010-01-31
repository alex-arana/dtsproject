package org.dataminx.dts.wn.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

/**
 * This class holds all the details about the DTS Job that Spring Batch will go through to process the
 * data transfer requests.
 * 
 * @author Gerson Galang
 */
public class DtsJobDetails implements Serializable {
	
    private static final long serialVersionUID = 1L;
    
	private List<DtsJobStep> mJobSteps = null;
	private int mBytesTransferred = 0;
	private int mTotalBytes = 0;
	private JobDefinitionType mJobDefinition = null;
	private String mJobId;
	private List<String> mExcludedFiles = new ArrayList<String>();
	
	public List<String> getExcludedFiles() {
    	return mExcludedFiles;
    }

	public void setExcludedFiles(List<String> excludedFiles) {
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

	public void setJobId(String jobId) {
    	mJobId = jobId;
    }

	public JobDefinitionType getJobDefinition() {
    	return mJobDefinition;
    }

	public void setJobDefinition(JobDefinitionType jobDefinition) {
    	mJobDefinition = jobDefinition;
    }

	public List<DtsJobStep> getJobSteps() {
    	return mJobSteps;
    }
	
	public void setJobSteps(List<DtsJobStep> jobSteps) {
    	mJobSteps = jobSteps;
    }
	
	public int getBytesTransferred() {
    	return mBytesTransferred;
    }
	
	public synchronized void addBytesTransferred(int bytesTransferred) {
    	mBytesTransferred += bytesTransferred;
    }
	
	public int getTotalBytes() {
    	return mTotalBytes;
    }
	
	public void setTotalBytes(int totalBytes) {
    	mTotalBytes = totalBytes;
    }
	
	public boolean isCompleted() {
    	return mBytesTransferred == mTotalBytes;
    }
	
}
