package org.dataminx.dts.service;

import java.util.UUID;
import javax.xml.transform.dom.DOMResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.dts.jms.JobSubmitQueueSender;
import org.dataminx.dts.ws.DtsJobDefinitionException;
import org.dataminx.schemas.dts._2009._05.dts.CancelJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.ResumeJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SuspendJobRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

/**
 * The Data Transfer Service Implementation. This class interacts with the DTS domain layer and hands over
 * the submitted jobs to the DTS Messaging System which then forwards them to the DTS Worker Nodes.
 *
 * @author Gerson Galang
 */
public class DataTransferServiceImpl implements DataTransferService, InitializingBean {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceImpl.class);

    /** The submit job message sender. */
    @Autowired
    private JobSubmitQueueSender mMessageSender;

    /** The job repository for this DTS implementation. */
    @Autowired
    private JobDao mJobRepository;

    @Autowired
    private Jaxb2Marshaller mMarshaller;


    /**
     * {@inheritDoc}
     */
    public String submitJob(JobDefinitionType job) {
        String jobName = job.getJobDescription().getJobIdentification().getJobName();

        LOGGER.debug("In DataTransferServiceImpl.submitJob, running job " + jobName + "... ");

        // TODO: put the code that integrates this module to the domain layer


        // TODO: move the XML document checking capability to a separate class

        // we'll assume that once we get to this point, the job definition that the user
        // submitted is valid or conforms to the schema

        // TODO: add the WSFault definition in the schema file, if possible

        // now let's check for semantic issues..
        // assume we require the following fields to be filled up in the job definition document
        //   * jobname - can't be an empty string
        //   * uri - can't be an empty string
        if (jobName.trim().equals("")) {
            throw new DtsJobDefinitionException("Invalid request. Empty job name.");
        }
        for (DataTransferType transfer : job.getJobDescription().getDataTransfer()) {
            if (transfer.getSource().getURI().trim().equals("")) {
                throw new DtsJobDefinitionException("Invalid request. Empty SourceURI.");
            }
            if (transfer.getTarget().getURI().trim().equals("")) {
                throw new DtsJobDefinitionException("Invalid request. Empty TargetURI.");
            }
        }

        // we now know that at this point, all the required fields from the job definition has
        // been provided by the user. let's give the job a resource key and save it in the DB
        Job newJob = new Job();
        String newJobResourceKey = UUID.randomUUID().toString();
        newJob.setName(jobName);
        newJob.setResourceKey(newJobResourceKey);
        newJob.setStatus(JobStatus.CREATED);
        newJob.setSubjectName("NEW_USER");
        mJobRepository.saveOrUpdate(newJob);

        // TODO: fix the way the message gets sent to the JMS layer. talk to alex about the
        // interface

        return newJobResourceKey;
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.cancelJob, cancelling job " + jobId + "... ");

        // TODO: let's send a cancel message via JMS to the worker node

        // after that, let's update the status of this job..
        Job job = mJobRepository.findByResourceKey(jobId);
        job.setStatus(JobStatus.DONE);
        mJobRepository.saveOrUpdate(job);
    }

    /**
     * {@inheritDoc}
     */
    public void suspendJob(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.suspendJob, suspending job " + jobId + "... ");

        // TODO: let's send a suspend message via JMS to the worker node

        // after that, let's update the status of this job..
        Job job = mJobRepository.findByResourceKey(jobId);
        job.setStatus(JobStatus.SUSPENDED);
        mJobRepository.saveOrUpdate(job);
    }

    /**
     * {@inheritDoc}
     */
    public void resumeJob(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.resumeJob, resuming job " + jobId + "... ");

        // TODO: let's send the resume message via JMS to the worker node..

        // after that, let's update the status of this job..
        Job job = mJobRepository.findByResourceKey(jobId);
        job.setStatus(JobStatus.TRANSFERRING);
        mJobRepository.saveOrUpdate(job);
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.cancelJob, getting job status for " + jobId  + "... ");

        // TODO: need to get this info from the DB.. part of this code need to have the smarts
        // to figure out which status the job is on based on the timing details provided
        // in the DB. if the smarts is not going to be put here, we need to have a way of having
        // the status get updated every time a new job event gets triggered
        return mJobRepository.findByResourceKey(jobId).getStatus().getStringValue();
    }

    // *** TEMPORARY METHODS JUST SO INTEGRATION WITH ALEX WOULD WORK FOR NOW
    public String submitJob(SubmitJobRequest submitJobRequest) {
        String jobId = submitJob(submitJobRequest.getJobDefinition());

        DOMResult result = new DOMResult();
        mMarshaller.marshal(submitJobRequest, result);
        Document document = (Document) result.getNode();

        // now submit the job to the DTS WN
        mMessageSender.doSend(jobId, document);
        return jobId;
    }

    public void cancelJob(CancelJobRequest cancelJobRequest) {
        cancelJob(cancelJobRequest.getJobId());

        // TODO: send the cancel job to the JMS layer..

    }
    public void suspendJob(SuspendJobRequest suspendJobRequest) {
        suspendJob(suspendJobRequest.getJobId());

        // TODO: send the suspend job to the JMS layer...
    }

    public void resumeJob(ResumeJobRequest resumeJobRequest) {
        resumeJob(resumeJobRequest.getJobId());

        // TODO: send the resume job to the JMS layer...
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
