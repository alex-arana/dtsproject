package org.dataminx.dts.service.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Result;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.common.util.JobContentValidator;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.dts.jms.JobSubmitQueueSender;
import org.dataminx.dts.service.DataTransferService;
import org.dataminx.dts.ws.InvalidJobDefinitionException;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.transform.StringResult;

/**
 * The Data Transfer Service Implementation. This class interacts with the DTS domain layer and hands over
 * the submitted jobs to the DTS Messaging System which then forwards them to the DTS Worker Nodes.
 *
 * @author Gerson Galang
 */
public class DataTransferServiceImpl implements DataTransferService, InitializingBean {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceImpl.class);

    /** the constant variable to be used in finding the value of the common name. */
    private static final String CN_EQUALS = "CN=";

    /** The submit job message sender. */
    @Autowired
    private JobSubmitQueueSender mMessageSender;

    /** The job repository for this DTS implementation. */
    @Autowired
    private JobDao mJobRepository;

    /** The xmlbeans marshaller. */
    @Autowired
    private XmlBeansMarshaller mMarshaller;

    /** The Job definition validator. */
    @Autowired
    private JobContentValidator mJobValidator;

    /**
     * {@inheritDoc}
     */
    public String submitJob(SubmitJobRequestDocument submitJobRequest) {
        String jobName = submitJobRequest.getSubmitJobRequest()
            .getJobDefinition().getJobDescription().getJobIdentification().getJobName();
        String subjectName = "NEW_USER";

        TransportContext txContext = TransportContextHolder.getTransportContext();
        HttpServletConnection connection = (HttpServletConnection) txContext.getConnection();
        HttpServletRequest request = connection.getHttpServletRequest();
        Subject subject = (Subject) request.getSession().getAttribute("subject");

        if (subject != null) {
            String distinguishedName = subject.getPrincipals().toArray()[0].toString();
            subjectName = distinguishedName.substring(distinguishedName.indexOf(
                    CN_EQUALS) + CN_EQUALS.length());
        }

        LOGGER.debug("DataTransferServiceImpl submitJob()");
        LOGGER.debug("Running job '" + jobName + "' submitted by user '" + subjectName + "'");

        // we'll assume that once we get to this point, the job definition that the user
        // submitted is valid (in XML terms) or conforms to the schema

        // TODO: add the WSFault definition in the schema file, if possible

        // now let's check for semantic issues..
        // assume we require the following fields to be filled up in the job definition document
        //   * jobname - can't be an empty string
        //   * uri - can't be an empty string
        //mJobValidator.validate(submitJobRequest.getSubmitJobRequest().getJobDefinition());

        // we now know that at this point, all the required fields from the job definition has
        // been provided by the user. let's give the job a resource key and save it in the DB
        Job newJob = new Job();
        String newJobResourceKey = UUID.randomUUID().toString();
        newJob.setName(jobName);
        newJob.setResourceKey(newJobResourceKey);
        newJob.setStatus(JobStatus.CREATED);
        newJob.setSubjectName(subjectName);
        newJob.setCreationTime(new Date());
        try {
            newJob.setExecutionHost(InetAddress.getLocalHost().getCanonicalHostName());
        }
        catch (UnknownHostException ex) {
            // TODO: Auto-generated catch block
            LOGGER.error(ex.getMessage());
        }
        mJobRepository.saveOrUpdate(newJob);

        // Get the message payload ready for consumption by the JMS layer

        // After the switch to XMLBeans, the Result object has always been a StreamResult instead
        // of a DomResult. So we'll just hand a StringResult object as a parameter to marshal()
        Result result = new StringResult();

        // TODO: consider rework on this try-catch block
        try {
            mMarshaller.marshal(submitJobRequest, result);
        }
        catch (IOException e) {
            throw new InvalidJobDefinitionException(e.fillInStackTrace());
        }

        // TODO: filter out the credential info from the logs using the one that WN uses
        LOGGER.debug(result.toString());
        mMessageSender.doSend(newJobResourceKey, result.toString());

        return newJobResourceKey;
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(CancelJobRequestDocument cancelJobRequest) {
        String jobResourceKey = cancelJobRequest.getCancelJobRequest().getJobResourceKey();

        LOGGER.debug("DataTransferServiceImpl cancelJob()");
        LOGGER.debug("Cancelling job: " + jobResourceKey);

        // TODO: let's send a cancel message via JMS to the worker node

        // after that, let's update the status of this job..
        Job job = mJobRepository.findByResourceKey(jobResourceKey);
        job.setStatus(JobStatus.DONE);
        mJobRepository.saveOrUpdate(job);

    }

    /**
     * {@inheritDoc}
     */
    public void suspendJob(SuspendJobRequestDocument suspendJobRequest) {
        String jobResourceKey = suspendJobRequest.getSuspendJobRequest().getJobResourceKey();

        LOGGER.debug("DataTransferServiceImpl suspendJob()");
        LOGGER.debug("Suspending job: " + jobResourceKey);

        // after that, let's update the status of this job..
        Job job = mJobRepository.findByResourceKey(jobResourceKey);
        job.setStatus(JobStatus.SUSPENDED);
        mJobRepository.saveOrUpdate(job);

        // TODO: send the suspend job to the JMS layer...
    }

    /**
     * {@inheritDoc}
     */
    public void resumeJob(ResumeJobRequestDocument resumeJobRequest) {
        String jobResourceKey = resumeJobRequest.getResumeJobRequest().getJobResourceKey();

        LOGGER.debug("DataTransferServiceImpl resumeJob()");
        LOGGER.debug("Resuming job " + jobResourceKey);

        // after that, let's update the status of this job..
        Job job = mJobRepository.findByResourceKey(jobResourceKey);
        job.setStatus(JobStatus.TRANSFERRING);
        mJobRepository.saveOrUpdate(job);

        // TODO: send the resume job to the JMS layer...
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(GetJobStatusRequestDocument getJobStatusRequest) {
        String jobResourceKey = getJobStatusRequest.getGetJobStatusRequest().getJobResourceKey();
        LOGGER.debug("DataTransferServiceImpl getJobStatus()");
        LOGGER.debug("Getting job status of job " + jobResourceKey);

        // TODO: need to get this info from the DB.. part of this code need to have the smarts
        // to figure out which status the job is on based on the timing details provided
        // in the DB. if the smarts is not going to be put here, we need to have a way of having
        // the status get updated every time a new job event gets triggered
        return mJobRepository.findByResourceKey(jobResourceKey).getStatus().getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mMessageSender, "A JobSubmitQueueSender needs to be configured for the DataTransferService.");
        Assert.notNull(mJobRepository, "A JobDao needs to be configured for the DataTransferService.");
        Assert.notNull(mMarshaller, "A JaxbMarshaller needs to be configured for the DataTransferService");
    }

}
