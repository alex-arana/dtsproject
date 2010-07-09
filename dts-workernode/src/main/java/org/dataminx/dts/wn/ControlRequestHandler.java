package org.dataminx.dts.wn;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import org.ogf.schemas.dmi.x2008.x05.dmi.StateDocument;
import org.ogf.schemas.dmi.x2008.x05.dmi.StateType;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
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
                        // if the job is stopped ok, we are assuming that the
                        // workernodeJobNotificationService will respond with a
                        // confirmation message ! (ASSUMING)
                        boolean stopped = false;
                        for (Long execId : mWorkerNodeManager.getRunningExecutions(jobName)) {
                            mWorkerNodeManager.stop(execId);
                            stopped = true;
                        }
                        if (!stopped) {
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
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobId)) {
                    LOG.debug("Found running job requested resumsed");
                    mWorkerNodeManager.restartJob(jobId);
                    // do we need to return some confirmation that the job
                    // was stopped ok here ?
                    // Also, do we need to resond if any of the exceptions
                    // are caught below.


                }
            }
            String errorMsg = "The ResumeJob request is not processed due to a wrong JobResourceKey: " + jobId;
            return buildAnErrorMessage(msgHeaders, errorMsg);

            
        } else if (controlRequest instanceof GetJobStatusRequestDocument) {
            final GetJobStatusRequest getJobStatusRequest = ((GetJobStatusRequestDocument) controlRequest).getGetJobStatusRequest();
            final String jobGotStatus = getJobStatusRequest.getJobResourceKey();
            LOG.debug("Received a GetJobStatus request for " + jobGotStatus);
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobGotStatus)) {
                    LOG.debug("Found running job whose status can be accessed.");
                    try {
                        for (Long execId : mWorkerNodeManager.getRunningExecutions(jobName)) {
                            mWorkerNodeManager.getSummary(execId);
                            final GetJobStatusResponseDocument document = GetJobStatusResponseDocument.Factory.newInstance();
                            final GetJobStatusResponse response = document.addNewGetJobStatusResponse();
                            final StateDocument doc = StateDocument.Factory.newInstance();
                            final StateType stateType = doc.addNewState();
                            stateType.setValue(null);
                            response.setState(stateType);
                            Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
                            Iterator<String> iterator = msgHeaders.keySet().iterator();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                SIMsgHeaders.put(key, msgHeaders.get(key).toString());
                            }
                            SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
                            final MessageBuilder<GetJobStatusResponseDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(SIMsgHeaders);
                            final Message<GetJobStatusResponseDocument> msg = msgbuilder.setCorrelationId(msgHeaders.get(org.springframework.integration.jms.JmsHeaders.CORRELATION_ID)).build();
                            return msg;
                        }
                        // do we need to return some confirmation that the job
                        // was stopped ok here ?
                        // Also, do we need to resond if any of the exceptions
                        // are caught below.

                    } catch (NoSuchJobException e) {
                        LOG.debug(e.getMessage());
                    } catch (NoSuchJobExecutionException e) {
                        LOG.debug(e.getMessage());
                    }
                }
            }
            String errorMsg = "The GetJobStatus request is not processed due to a wrong JobResourceKey: " + jobGotStatus;
            return buildAnErrorMessage(msgHeaders, errorMsg);

        } else if (controlRequest instanceof GetJobDetailsRequestDocument) {
            final GetJobDetailsRequest getJobDetailsRequest = ((GetJobDetailsRequestDocument) controlRequest).getGetJobDetailsRequest();
            final String jobGotDetails = getJobDetailsRequest.getJobResourceKey();
            LOG.debug("Received a GetJobDetails request for " + jobGotDetails);
            for (String jobName : mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobGotDetails)) {
                    LOG.debug("Found running job whose details can be accessed.");
                    //todo
                }
            }
            String errorMsg = "The GetJobDetails request is not processed due to a wrong JobResourceKey: " + jobGotDetails;
            return buildAnErrorMessage(msgHeaders, errorMsg);
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
}
