package org.dataminx.dts.wn;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.dataminx.dts.batch.DtsJobLauncher;
import org.dataminx.dts.common.ws.InvalidJobDefinitionException;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultType;
import org.dataminx.schemas.dts.x2009.x07.messages.InvalidJobDefinitionFaultDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;

/**
 * A {@link ServiceActivator} class that launches jobs.
 *
 * @author David Meredith
 */
public class SubmitRequestHandler {

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(SubmitRequestHandler.class);

    /** A reference to the DTS specific job launcher */
    private DtsJobLauncher mDtsJobLauncher;


    /**
     * Handles Spring Integration messages that either wrap a valid {@link JobDefinitionDocument}
     * or a {@link InvalidJobDefinitionFaultDocument}. If a valid job is
     * recieved, the job is launched. If an invalid fault doc is recieved, then
     * the {@link InvalidJobDefinitionFaultDocument} is returned. If any problem
     * occurs when submitting the job, then a {@link CustomFaultDocument} is
     * returned.
     *
     * @param message
     * @return a Spring Integration Message used to wrap the returned document type. 
     */
    @ServiceActivator
    public Message<?> handleControlRequest(Message<?> message) {
        final Object submitRequest = message.getPayload();
        final MessageHeaders headers = message.getHeaders();
        // Use the spring integration correlation id as the jobID as this
        // header is agonstic of any particular transport/protocol.
        String jobID;
        // headers.getCorrelationId() == (String)headers.get(org.springframework.integration.core.MessageHeaders.CORRELATION_ID);
        if (headers.getCorrelationId() == null) {
            jobID = UUID.randomUUID().toString();
        } else {
            jobID = (String) headers.getCorrelationId();
        }

        if (submitRequest instanceof JobDefinitionDocument) {
            LOG.debug("Submit job for: " + jobID);
            try {
                // Spring batch job parameters can only be of type String, Date, Double, Long.
                // Therefore convert the SI message headers to these supported types.
                final Map<String, Object> headersAsBatchJobParameters = new HashMap<String, Object>();

                Iterator<String> it = headers.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    Object h = headers.get(key);
                    if (h instanceof String) {
                        headersAsBatchJobParameters.put(key, h);
                    } else if (h instanceof Date) {
                        headersAsBatchJobParameters.put(key, h);
                    } else if (h instanceof Double) {
                        headersAsBatchJobParameters.put(key, h);
                    } else if (h instanceof Long) {
                        headersAsBatchJobParameters.put(key, h);
                    } else {
                        if (h != null) {
                            try {
                                // Unknown/unsupported param type - try to add the unknown as a string.
                                headersAsBatchJobParameters.put(key, h.toString());
                            } catch (Exception ex) {
                                LOG.warn("Unable to add header key ["+key+"] to JobParameters map");
                            }
                        }
                    }
                }
                mDtsJobLauncher.run(jobID, (JobDefinitionDocument) submitRequest, headersAsBatchJobParameters);
                // return JobSubmitResponseDoc
                return null; // lets return null for now until we can send a JobSubmitResponseDoc. 

                // TODO Will prob need to send back different error message types
                // depending on the Exception. Currently, just send back a
                // CustomFaultDocument
            } catch (JobExecutionAlreadyRunningException ex) {
                //java.util.logging.Logger.getLogger(SubmitRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                return this.buildAnErrorMessage(headers, ex.getMessage());
            } catch (JobRestartException ex) {
                //java.util.logging.Logger.getLogger(SubmitRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                return this.buildAnErrorMessage(headers, ex.getMessage());
            } catch (JobInstanceAlreadyCompleteException ex) {
                //java.util.logging.Logger.getLogger(SubmitRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                return this.buildAnErrorMessage(headers, ex.getMessage());
            } catch (InvalidJobDefinitionException ex) {
                //java.util.logging.Logger.getLogger(SubmitRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                return this.buildAnErrorMessage(headers, ex.getMessage());
            }

        } else if (submitRequest instanceof InvalidJobDefinitionFaultDocument) {
            LOG.debug("Invalid job request for " + jobID);
            return message;

        }
        // ok, we have an unknown/unsupported message type here so
        // build an error message and return that.
        return this.buildAnErrorMessage(headers, "Unsupported Message Type");
    }



 
    /**
     * Create a new Spring Integration Message that wraps a {@link CustomFaultDocument}.
     *
     * @param messageHeaders
     * @param error
     * @return
     */
    private Message buildAnErrorMessage(MessageHeaders messageHeaders, String error) {
        final CustomFaultDocument document = CustomFaultDocument.Factory.newInstance();
        final CustomFaultType GetJobStatusRequestReply = document.addNewCustomFault();
        Map<String, Object> SIMsgHeaders = new LinkedHashMap<String, Object>();
        Iterator<String> iterator = messageHeaders.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            SIMsgHeaders.put(key, messageHeaders.get(key).toString());
        }
        // Not sure we need to add the worker id ? 
        //SIMsgHeaders.put(mDtsWorkerNodeInformationService.getWorkerNodeIDMessageHeaderName(), mDtsWorkerNodeInformationService.getInstanceId());
        GetJobStatusRequestReply.setMessage(error);
        MessageBuilder<CustomFaultDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(SIMsgHeaders);
        Message<CustomFaultDocument> msg = msgbuilder.build();
        return msg;
    }




    /**
     * Inject the dts job launcher in order to submit jobs.
     *
     * @param jobLauncher
     */
    public void setDtsJobLauncher(final DtsJobLauncher jobLauncher) {
        this.mDtsJobLauncher = jobLauncher;
    }
}
