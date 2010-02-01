package org.dataminx.dts.wn.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * 
 * @author Gerson Galang
 */
public class MaxStreamCounterTask implements Tasklet, InitializingBean {
	private JobDefinitionType mJobDefinition;
	
	private SubmitJobRequest mSubmitJobRequest;
	
	private int mMaxConnectionsToTry = 0;
	
	private static final Log LOGGER = LogFactory.getLog(JobScoperImpl.class);
	
	@Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {		
		LOGGER.debug("MaxStreamCounterTask execute()");
		
		// TODO: have this step rerun if it fails... use the user's provided info
		
		List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();
				
        final JobDescriptionType jobDescription = mSubmitJobRequest.getJobDefinition().getJobDescription();
        if (jobDescription instanceof MinxJobDescriptionType) {
            final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
            CollectionUtils.addAll(dataTransfers, minxJobDescription.getDataTransferArray());
        }
        if (CollectionUtils.isEmpty(dataTransfers)) {
            LOGGER.warn("DTS job request is incomplete as it does not contain any data transfer elements.");
            throw new DtsJobExecutionException("DTS job request contains no data transfer elements.");
        }
        
        Map<MaxConnectionKey, Integer> maxConnectionsMap = new HashMap<MaxConnectionKey, Integer>();
        for (DataTransferType dataTransfer : dataTransfers) {
        	MaxConnectionKey maxConnectionKey = new MaxConnectionKey(
        			dataTransfer.getSource().getURI(), dataTransfer.getTarget().getURI());
        	//getMaxConnection(dataTransferType);
        	
        }
		return RepeatStatus.FINISHED;
    }
	
	public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }

	@Override
    public void afterPropertiesSet() throws Exception {
		Assert.state(mSubmitJobRequest != null);	    
    }
	
	public void setMaxConnectionsToTry(int maxConnectionsToTry) {
		mMaxConnectionsToTry = maxConnectionsToTry;
	}
	
	private int getMaxConnection(DataTransferType dataTransferType) {
        return 1;
	}

}
