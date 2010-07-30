package org.dataminx.dts.wn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Handler;
import org.ogf.schemas.dmi.x2008.x05.dmi.StateType;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobResponseDocument.CancelJobResponse;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobResponseDocument.ResumeJobResponse;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobDetailsRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultType;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument.ResumeJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobDetailsRequestDocument.GetJobDetailsRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument.GetJobStatusRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument.GetJobStatusResponse;
import org.ogf.schemas.dmi.x2008.x05.dmi.DetailType;
import org.ogf.schemas.dmi.x2008.x05.dmi.StatusValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;

/**
 * A {@link Handler} class that stops or resumes job execution depending on
 * type of the control request.
 *
 * @author hnguyen
 */
public class ControlRequestHandler {

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlRequestHandler.class);
    /** manager to stop, restart jobs as well as querying batch job related information */
    private WorkerNodeManager mWorkerNodeManager;
    /** job restart strategy */
    private JobRestartStrategy mJobRestartStrategy;
    /** A reference to the DTS Worker Node information service. */
    private DtsWorkerNodeInformationService mDtsWorkerNodeInformationService;

    /**
     * Handles all different control message types sent from the broker or control queue. Depending on
     * which control messages, the handler will call cancel, or restart functionality of {@link WorkerNodeManager}
     * class. It is worth noting that this handler doesn't handle errors at this stage because normal errors
     * will be dealt with via the JobNotificationService route. However, there are error cases that need
     * handling here such as dealing with unknown jobid (implementation needed here)
     * @param message
     * @return
     */
    @ServiceActivator
    public Object handleControlRequest(Message<?> message) {
        // this service activator has null return value, therefore, no confirmations
        // can be sent to the dtsJobEvents !

        final Object controlRequest = message.getPayload();
        final MessageHeaders msgHeaders = message.getHeaders();
        if (controlRequest instanceof CancelJobRequestDocument) {
            final CancelJobRequest cancelRequest = ((CancelJobRequestDocument) controlRequest).getCancelJobRequest();
            final String jobId = cancelRequest.getJobResourceKey();
            LOG.debug("received cancel job request for " + jobId);

            boolean found = false;
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobId)) {
                    found = true;
                    LOG.debug("Found running job requested cancelled");
                    try {
                        boolean stopped = false;
                        for (Long execId : mWorkerNodeManager.getRunningExecutions(jobName)) {
                            mWorkerNodeManager.stop(execId);
                            stopped = true;
                        }
                        if(stopped){
                            // here do we need to send a confirmation message that
                            // the job stopped ok.
                            final CancelJobResponseDocument document = CancelJobResponseDocument.Factory.newInstance();
                            final CancelJobResponse cancelJobResponse = document.addNewCancelJobResponse();
                            Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
                            Iterator<String> iterator = msgHeaders.keySet().iterator();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                SIMsgHeaders.put(key, msgHeaders.get(key).toString());
                            }
                            SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
                            cancelJobResponse.setSuccessFlag(stopped);
                            MessageBuilder<CancelJobResponseDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(SIMsgHeaders);
                            Message<CancelJobResponseDocument> msg = msgbuilder.build();
                            return msg;
                        }
                        else {
                            String errorMsg = "Could not stop job: " + jobId;
                            return buildAnErrorMessage(msgHeaders, errorMsg);
                        }
                    } catch (NoSuchJobException e) {
                        LOG.debug(e.getMessage());
                        return buildAnErrorMessage(msgHeaders, e.getMessage());
                    } catch (JobExecutionNotRunningException e) {
                        LOG.debug(e.getMessage());
                        return buildAnErrorMessage(msgHeaders, e.getMessage());
                    } catch (NoSuchJobExecutionException e) {
                        LOG.debug(e.getMessage());
                        return buildAnErrorMessage(msgHeaders, e.getMessage());
                    }
                }
            }
            if (!found) {
                String errorMsg = "Could not find job: " + jobId;
                return buildAnErrorMessage(msgHeaders, errorMsg);
            }


        } else if (controlRequest instanceof ResumeJobRequestDocument) {
            final ResumeJobRequest resumeRequest = ((ResumeJobRequestDocument) controlRequest).getResumeJobRequest();
            final String jobId = resumeRequest.getJobResourceKey();
            LOG.debug("Received a resume job request for " + jobId);
            boolean found = false;
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobId)) {
                    found = true;
                    LOG.debug("Found running job requested resumsed");
                    try {
                        mJobRestartStrategy.restartJob(jobId);
                        final ResumeJobResponseDocument document = ResumeJobResponseDocument.Factory.newInstance();
                        final ResumeJobResponse resumeJobResponse = document.addNewResumeJobResponse();
                        Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
                        Iterator<String> iterator = msgHeaders.keySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            SIMsgHeaders.put(key, msgHeaders.get(key).toString());
                        }
                        SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
                        resumeJobResponse.setSuccessFlag(true);
                        MessageBuilder<ResumeJobResponseDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(SIMsgHeaders);
                        Message<ResumeJobResponseDocument> msg = msgbuilder.build();
                        return msg;
                    } catch (Exception ex) {
                        return buildAnErrorMessage(msgHeaders, ex.getMessage());
                    }
                }
            }
            if (!found) {
            String errorMsg = "The ResumeJob request is not processed due to a wrong JobResourceKey: " + jobId;
            return buildAnErrorMessage(msgHeaders, errorMsg);
            }
            
        } else if (controlRequest instanceof GetJobStatusRequestDocument) {
            final GetJobStatusRequest getJobStatusRequest = ((GetJobStatusRequestDocument) controlRequest).getGetJobStatusRequest();
            final String jobGotStatus = getJobStatusRequest.getJobResourceKey();
            LOG.debug("Received a GetJobStatus request for " + jobGotStatus);
            boolean found = false;
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobGotStatus)) {
                    found = true;
                    try {
                        if (mWorkerNodeManager.getRunningExecutions(jobName).size()==0) {
                            for (Long execId : mWorkerNodeManager.getRunningExecutions(jobName)) {
                                String dtsJobSummary = mWorkerNodeManager.getSummary(execId);
                                LOG.debug(jobGotStatus + "'s dtsJobSummary: " + dtsJobSummary);
                                final GetJobStatusResponseDocument document = GetJobStatusResponseDocument.Factory.newInstance();
                                final GetJobStatusResponse response = document.addNewGetJobStatusResponse();
                                final StateType stateType = response.addNewState();
                                //COMPLETED, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN;
                                // decide the status from the summary
                                String jobState = parseJobSummary(dtsJobSummary);
                                LOG.debug("parseJobSummary(dtsJobSummary): " + jobState);
                                if (jobState.equals("STARTING")) {
                                    stateType.setValue(StatusValueType.CREATED);
                                } else if (jobState.equals("COMPLETED")) {
                                    stateType.setValue(StatusValueType.DONE);
                                } else if (jobState.equals("FAILED")) {
                                    stateType.setValue(StatusValueType.FAILED_UNCLEAN);
                                } else if (jobState.equals("UNKNOWN")) {
                                    stateType.setValue(StatusValueType.SCHEDULED);
                                } else if (jobState.equals("STOPPED")) {
                                    stateType.setValue(StatusValueType.SUSPENDED);
                                } else if (jobState.equals("STARTED")) {
                                    stateType.setValue(StatusValueType.TRANSFERRING);
                                } else {
                                    stateType.setValue(StatusValueType.FAILED_UNKNOWN);
                                }
                                final DetailType detail = stateType.addNewDetail();
                                stateType.setDetail(detail);
                                response.setState(stateType);
                                Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
                                Iterator<String> iterator = msgHeaders.keySet().iterator();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    SIMsgHeaders.put(key, msgHeaders.get(key).toString());
                                }
                                SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
                                final MessageBuilder<GetJobStatusResponseDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(SIMsgHeaders);
                                final Message<GetJobStatusResponseDocument> msg = msgbuilder.build();
                                return msg;
                            }
                        } else {
                            return buildAnErrorMessage(msgHeaders, "There are no running executions to give a job status. The job may be stopped or completed.");
                        }
                    } catch (NoSuchJobException e) {
                        LOG.debug(e.getMessage());
                        return buildAnErrorMessage(msgHeaders, e.getMessage());
                    } catch (NoSuchJobExecutionException e) {
                        LOG.debug(e.getMessage());
                        return buildAnErrorMessage(msgHeaders, e.getMessage());
                    }
                }
            }
            if (!found) {
                String errorMsg = "The GetJobStatus request is not processed due to a wrong JobResourceKey: " + jobGotStatus;
                return buildAnErrorMessage(msgHeaders, errorMsg);
            }
        } else if (controlRequest instanceof GetJobDetailsRequestDocument) {
            final GetJobDetailsRequest getJobDetailsRequest = ((GetJobDetailsRequestDocument) controlRequest).getGetJobDetailsRequest();
            final String jobGotDetails = getJobDetailsRequest.getJobResourceKey();
            LOG.debug("Received a GetJobDetails request for " + jobGotDetails);
            boolean found = false;
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobGotDetails)) {
                    found = true;
                    LOG.debug("Found running job whose details can be accessed.");
                    //todo
                }
            }
            if (!found) {
            String errorMsg = "The GetJobDetails request is not processed due to a wrong JobResourceKey: " + jobGotDetails;
            return buildAnErrorMessage(msgHeaders, errorMsg);
           }
        }
        return null;
    }

    public void setWorkerNodeManager(final WorkerNodeManager mWorkerNodeManager) {
        this.mWorkerNodeManager = mWorkerNodeManager;
    }

    public void setJobRestartStrategy(final JobRestartStrategy strategy) {
        this.mJobRestartStrategy = strategy;
    }

    /**
     * Set the DtsWorkerNodeInformationService.
     *
     * @param dtsWorkerNodeInformationService
     */
    public void setDtsWorkerNodeInformationService(final DtsWorkerNodeInformationService dtsWorkerNodeInformationService){
        mDtsWorkerNodeInformationService = dtsWorkerNodeInformationService;
    }

    private Message buildAnErrorMessage(MessageHeaders messageHeaders, String error) {
        final CustomFaultDocument document = CustomFaultDocument.Factory.newInstance();
        final CustomFaultType GetJobStatusRequestReply = document.addNewCustomFault();
        Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
        Iterator<String> iterator = messageHeaders.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            SIMsgHeaders.put(key, messageHeaders.get(key).toString());
        }
        SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
        GetJobStatusRequestReply.setMessage(error);
        MessageBuilder<CustomFaultDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(SIMsgHeaders);
        Message<CustomFaultDocument> msg = msgbuilder.build();
        return msg;
    }

    private String parseJobSummary(String jobSummary){
      // jobSummary's format  (", startTime=%s, endTime=%s, lastUpdated=%s, status=%s, exitStatus=%s, job=[%s]", startTime, endTime, lastUpdated, status, exitStatus, jobInstance)
        String s = "status";
        String e = "exitStatus";
        int sstart = jobSummary.indexOf(s);
        int estart = jobSummary.indexOf(e);
        return jobSummary.substring(sstart+7, estart-2);
    }
}
