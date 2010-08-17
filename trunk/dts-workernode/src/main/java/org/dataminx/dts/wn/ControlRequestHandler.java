package org.dataminx.dts.wn;

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
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultType;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument.ResumeJobRequest;
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
 * @author David Meredith
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
     * Handles all different control message types sent from the broker's control queue. Depending on
     * which control messages are given, the handler will call cancel, restart or getSummary 
     * functionality of {@link WorkerNodeManager}.
     *
     * @param message A Spring Integration message that wraps either a
     *  {@link CancelJobRequestDocument}, a {@link ResumeJobRequestDocument} or
     *  a {@link GetJobStatusRequestDocument} as its payload.
     * @return a Spring Integration message that wraps either a
     * {@link CancelJobResponseDocument}, a {@link ResumeJobResponseDocument},
     *  a {@link GetJobStatusResponseDocument} or a {@link CustomFaultDocument}. 
     */
    @ServiceActivator
    public Object handleControlRequest(Message<?> message) {
        final Object controlRequest = message.getPayload();
        final MessageHeaders msgHeaders = message.getHeaders();
        try {
            if (controlRequest instanceof CancelJobRequestDocument) {
                return this.handleCancelJobRequestDocument((CancelJobRequestDocument) controlRequest, msgHeaders);

            } else if (controlRequest instanceof ResumeJobRequestDocument) {
                return this.handleJobResumeDocument((ResumeJobRequestDocument) controlRequest, msgHeaders);

            } else if (controlRequest instanceof GetJobStatusRequestDocument) {
                return this.handleGetJobStatusRequestDocument((GetJobStatusRequestDocument) controlRequest, msgHeaders);
            }
            /* else if (controlRequest instanceof GetJobDetailsRequestDocument) {
            // not yet supported
            }*/
        } catch (Exception ex) {
            return buildAnErrorMessage(msgHeaders, ex.getMessage());
        }
        return buildAnErrorMessage(msgHeaders, "Invalid message payload. One of CancelJobRequestDocument, ResumeJobRequestDocument, GetJobStatusRequestDocument is expected");
    }

    /**
     * Handle the GetJobStatusRequest and return a Spring Integration response {@link Message}. 
     * 
     * @param jobStatusRequestDocument
     * @param msgHeaders will be added to the returned Spring Integration message headers. 
     * @return a Spring Integration {@link Message} that wraps either a 
     *  {@link GetJobStatusResponseDocument} if the status request was handled successfully or a 
     *  {@link CustomFaultDocument} if it failed. 
     */
    private Message handleGetJobStatusRequestDocument(final GetJobStatusRequestDocument jobStatusRequestDocument,
            final MessageHeaders msgHeaders) throws NoSuchJobException, NoSuchJobExecutionException {
        final GetJobStatusRequest getJobStatusRequest = jobStatusRequestDocument.getGetJobStatusRequest();
        final String jobGotStatus = getJobStatusRequest.getJobResourceKey();
        LOG.debug("Received a GetJobStatus request for " + jobGotStatus);
        for (String jobName : mWorkerNodeManager.getJobNames()) {
            if (jobName.equals(jobGotStatus)) {
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
                    } else if (jobState.equals("STOPPED") || jobState.equals("STOPPING")) {
                        stateType.setValue(StatusValueType.SUSPENDED);
                    } else if (jobState.equals("STARTED")) {
                        stateType.setValue(StatusValueType.TRANSFERRING);
                    } else {
                        stateType.setValue(StatusValueType.FAILED_UNKNOWN);
                    }
                    final DetailType detail = stateType.addNewDetail();
                    stateType.setDetail(detail);
                    response.setState(stateType);
                    Message<GetJobStatusResponseDocument> msg = this.getResponseMessage(document, msgHeaders);
                    return msg;
                }
                // we have not yet returned, so return the error message.
                return buildAnErrorMessage(msgHeaders, "There are no running executions to give a job status. The job may be stopped or completed.");

            }
        }
        return buildAnErrorMessage(msgHeaders, "The GetJobStatus request is not processed due to a wrong JobResourceKey: " + jobGotStatus);
    }

    /**
     * Handle the ResumeJobRequest and return a Spring Integration response {@link Message}.
     *
     * @param resumeJobRequestDocument
     * @param msgHeaders will be added to the returned Spring Integration message headers.
     * @return a Spring Integration {@link Message} that wraps either a
     *  {@link ResumeJobResponseDocument} if the resume was handled successfully or a
     *  {@link CustomFaultDocument} if the resume failed.
     */
    private Message handleJobResumeDocument(final ResumeJobRequestDocument resumeJobRequestDocument,
            final MessageHeaders msgHeaders) throws Exception {
        final ResumeJobRequest resumeRequest = resumeJobRequestDocument.getResumeJobRequest();
        final String jobId = resumeRequest.getJobResourceKey();
        LOG.debug("Received a resume job request for " + jobId);
        for (String jobName : mWorkerNodeManager.getJobNames()) {
            if (jobName.equals(jobId)) {
                LOG.debug("Found job, requesting restartJob");
                mJobRestartStrategy.restartJob(jobId);
                final ResumeJobResponseDocument document = ResumeJobResponseDocument.Factory.newInstance();
                final ResumeJobResponse resumeJobResponse = document.addNewResumeJobResponse();
                resumeJobResponse.setSuccessFlag(true);
                Message<ResumeJobResponseDocument> msg = this.getResponseMessage(document, msgHeaders);
                return msg;

            }
        }
        return buildAnErrorMessage(msgHeaders, "The ResumeJob request is not processed due to a wrong JobResourceKey: " + jobId);
    }

    /**
     * Handle the CancelJobRequest and return a Spring Integration response {@link Message}.
     *
     * @param cancelRequest a valid Cancel request document
     * @param msgHeaders will be added to the returned Spring Integration message headers.
     * @return a Spring Integration {@link Message} that wraps either a
     *  {@link CancelJobResponseDocument} if the cancel was handled successfully or a
     *  {@link CustomFaultDocument} if the cancel failed.
     */
    private Message handleCancelJobRequestDocument(final CancelJobRequestDocument cancelRequestDocument,
            final MessageHeaders msgHeaders) throws NoSuchJobException, NoSuchJobExecutionException, JobExecutionNotRunningException {
        final CancelJobRequest cancelRequest = cancelRequestDocument.getCancelJobRequest();
        final String jobId = cancelRequest.getJobResourceKey();
        LOG.debug("received cancel job request for " + jobId);
        for (String jobName : mWorkerNodeManager.getJobNames()) {
            if (jobName.equals(jobId)) {
                boolean stopped = false;
                for (Long execId : mWorkerNodeManager.getRunningExecutions(jobName)) {
                    LOG.debug("Found running job, requesting cancelled");
                    mWorkerNodeManager.stop(execId);
                    stopped = true;
                }
                if (stopped) {
                    final CancelJobResponseDocument document = CancelJobResponseDocument.Factory.newInstance();
                    final CancelJobResponse cancelJobResponse = document.addNewCancelJobResponse();
                    cancelJobResponse.setSuccessFlag(true);
                    Message<CancelJobResponseDocument> msg = this.getResponseMessage(document, msgHeaders);
                    return msg;
                } else {
                    return buildAnErrorMessage(msgHeaders, "There are no running executions to cancel");
                }

            }
        }
        return buildAnErrorMessage(msgHeaders, "Could not find or stop job: " + jobId);
    }

    /*private Message handleGetJobDetailsReqeustDocument(){
    final GetJobDetailsRequest getJobDetailsRequest = ((GetJobDetailsRequestDocument) controlRequest).getGetJobDetailsRequest();
    final String jobGotDetails = getJobDetailsRequest.getJobResourceKey();
    LOG.debug("Received a GetJobDetails request for " + jobGotDetails);
    boolean found = false;
    for (String jobName : mWorkerNodeManager.getJobNames()) {
    if (jobName.equals(jobGotDetails)) {
    found = true;
    LOG.debug("Found running job whose details can be accessed.");
    final GetJobDetailsResponse document = GetJobDetailsResponse.Factory.newInstance();
    final JobDetailsType details = document.addNewJobDetails();
    //details.se
    //todo
    return null;
    }
    }
    // we have not yet returned, so build the error message.
    //return buildAnErrorMessage(msgHeaders, "There are no running executions to give a job status. The job may be stopped or completed.");
    
    
    if (!found) {
    String errorMsg = "The GetJobDetails request is not processed due to a wrong JobResourceKey: " + jobGotDetails;
    return buildAnErrorMessage(msgHeaders, errorMsg);
    }
    }*/
    private Message buildAnErrorMessage(MessageHeaders messageHeaders, String error) {
        final CustomFaultDocument document = CustomFaultDocument.Factory.newInstance();
        final CustomFaultType GetJobStatusRequestReply = document.addNewCustomFault();
        GetJobStatusRequestReply.setMessage(error);
        return this.getResponseMessage(document, messageHeaders);
    }

    private String parseJobSummary(String jobSummary) {
        // jobSummary's format  (", startTime=%s, endTime=%s, lastUpdated=%s, status=%s, exitStatus=%s, job=[%s]", startTime, endTime, lastUpdated, status, exitStatus, jobInstance)
        String s = "status";
        String e = "exitStatus";
        int sstart = jobSummary.indexOf(s);
        int estart = jobSummary.indexOf(e);
        return jobSummary.substring(sstart + 7, estart - 2);
    }

    @SuppressWarnings("unchecked")
    private <T> Message<T> getResponseMessage(T payload, Map<String, Object> messageHeaders) {
        Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
        Iterator<String> iterator = messageHeaders.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            SIMsgHeaders.put(key, messageHeaders.get(key).toString());
        }
        SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
        final MessageBuilder<T> msgbuilder = MessageBuilder.withPayload(payload).copyHeaders(SIMsgHeaders);
        final Message<T> msg = msgbuilder.build();
        return msg;
    }

    /**
     * Set the WorkerNodeManager
     *
     * @param mWorkerNodeManager
     */
    public void setWorkerNodeManager(final WorkerNodeManager mWorkerNodeManager) {
        this.mWorkerNodeManager = mWorkerNodeManager;
    }

    /**
     * Set JobRestartStrategy
     *
     * @param strategy
     */
    public void setJobRestartStrategy(final JobRestartStrategy strategy) {
        this.mJobRestartStrategy = strategy;
    }

    /**
     * Set the DtsWorkerNodeInformationService.
     *
     * @param dtsWorkerNodeInformationService
     */
    public void setDtsWorkerNodeInformationService(final DtsWorkerNodeInformationService dtsWorkerNodeInformationService) {
        mDtsWorkerNodeInformationService = dtsWorkerNodeInformationService;
    }
}
