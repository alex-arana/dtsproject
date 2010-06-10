/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.ws.service;

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
import org.dataminx.dts.common.jms.JobQueueSender;
import org.dataminx.dts.common.model.JobStatus;
import org.dataminx.dts.common.util.SchemaUtils;
import org.dataminx.dts.common.ws.InvalidJobDefinitionException;
import org.dataminx.dts.common.ws.JobStatusUpdateException;
import org.dataminx.dts.common.ws.NonExistentJobException;
import org.dataminx.dts.ws.model.Job;
import org.dataminx.dts.ws.repo.JobDao;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobDetailsRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.springframework.util.Assert;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.transform.StringResult;

/**
 * The Data Transfer Service Implementation. This class interacts with the DTS
 * Job repository and hands over the submitted jobs to the DTS Messaging System
 * which then forwards them to the DTS Worker Nodes.
 *
 * @author Gerson Galang
 */
public class DataTransferServiceImpl implements DataTransferService,
    InitializingBean {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(DataTransferServiceImpl.class);

    /**
     * the constant variable to be used in finding the value of the common name.
     */
    private static final String CN_EQUALS = "CN=";

    /** The submit job message sender. */
    private JobQueueSender mJobSubmitMessageSender;

    private JobQueueSender mJobControlMessageSender;

    /** The job repository for this DTS implementation. */
    private JobDao mJobRepository;

    /** The xmlbeans marshaller. */
    private XmlBeansMarshaller mMarshaller;

    /**
     * {@inheritDoc}
     */
    public String submitJob(final SubmitJobRequestDocument submitJobRequest) {
        final String jobName = submitJobRequest.getSubmitJobRequest()
            .getJobDefinition().getJobDescription().getJobIdentification()
            .getJobName();
        String subjectName = "NEW_USER";

        final TransportContext txContext = TransportContextHolder
            .getTransportContext();
        final HttpServletConnection connection = (HttpServletConnection) txContext
            .getConnection();
        final HttpServletRequest request = connection.getHttpServletRequest();
        final Subject subject = (Subject) request.getSession().getAttribute(
            "subject");

        if (subject != null) {
            final String distinguishedName = subject.getPrincipals().toArray()[0]
                .toString();
            subjectName = distinguishedName.substring(distinguishedName
                .indexOf(CN_EQUALS)
                + CN_EQUALS.length());
        }

        LOGGER.debug("DataTransferServiceImpl submitJob()");
        LOGGER.debug("Running job '" + jobName + "' submitted by user '"
            + subjectName + "'");

        // we'll assume that once we get to this point, the job definition that the user
        // submitted is valid (in XML terms) or conforms to the schema

        // we now know that at this point, all the required fields from the job definition has
        // been provided by the user. let's give the job a resource key and save it in the DB
        final Job newJob = new Job();
        final String newJobResourceKey = UUID.randomUUID().toString();
        newJob.setName(jobName);
        newJob.setResourceKey(newJobResourceKey);
        newJob.setStatus(JobStatus.CREATED);
        newJob.setSubjectName(subjectName);
        newJob.setCreationTime(new Date());
        try {
            newJob.setExecutionHost(InetAddress.getLocalHost()
                .getCanonicalHostName());
        }
        catch (final UnknownHostException ex) {
            // TODO: Auto-generated catch block
            LOGGER.error(ex.getMessage());
        }
        mJobRepository.saveOrUpdate(newJob);

        // Get the message payload ready for consumption by the JMS layer

        // After the switch to XMLBeans, the Result object has always been a StreamResult instead
        // of a DomResult. So we'll just hand a StringResult object as a parameter to marshal()
        final Result result = new StringResult();

        // TODO: consider rework on this try-catch block
        try {
            mMarshaller.marshal(submitJobRequest, result);
        }
        catch (final IOException e) {
            throw new InvalidJobDefinitionException(e.fillInStackTrace());
        }

        // TODO: filter out the credential info from the logs using the one that WN uses
        final String auditableRequest = SchemaUtils
            .getAuditableString(submitJobRequest);
        LOGGER.debug(auditableRequest);

        // TODO: decide if we are going to add any JMS Properties to the message we are sending
        // to the queue like routingHeader
        mJobSubmitMessageSender.doSend(newJobResourceKey, result.toString());

        // let's also set the status and queued time if we reach this point as we can safely assume that
        // no fault was sent back to the client at this point
        newJob.setStatus(JobStatus.SCHEDULED);
        newJob.setQueuedTime(new Date());
        mJobRepository.saveOrUpdate(newJob);

        return newJobResourceKey;
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(final CancelJobRequestDocument cancelJobRequest) {
        final String jobResourceKey = cancelJobRequest.getCancelJobRequest()
            .getJobResourceKey();

        LOGGER.debug("DataTransferServiceImpl cancelJob()");
        LOGGER.debug("Cancelling job: " + jobResourceKey);

        // TODO: let's send a cancel message via JMS to the worker node

        // after that, let's update the status of this job..
        Job foundJob = null;
        foundJob = mJobRepository.findByResourceKey(jobResourceKey);
        if (foundJob == null) {
            throw new NonExistentJobException("Job doesn't exist.");
        }

        if (!(foundJob.getStatus().equals(JobStatus.CREATED)
            || foundJob.getStatus().equals(JobStatus.SCHEDULED)
            || foundJob.getStatus().equals(JobStatus.SUSPENDED) || foundJob
            .getStatus().equals(JobStatus.TRANSFERRING))) {
            throw new JobStatusUpdateException(
                "Job "
                    + jobResourceKey
                    + " cannot be cancelled as it has already completed, failed, or cancelled.");
        }

        foundJob.setStatus(JobStatus.DONE);
        mJobRepository.saveOrUpdate(foundJob);

        // Get the message payload ready for consumption by the JMS layer

        // After the switch to XMLBeans, the Result object has always been a StreamResult instead
        // of a DomResult. So we'll just hand a StringResult object as a parameter to marshal()
        final Result result = new StringResult();

        // TODO: consider rework on this try-catch block
        try {
            mMarshaller.marshal(cancelJobRequest, result);
        }
        catch (final IOException e) {
            throw new InvalidJobDefinitionException(e.fillInStackTrace());
        }

        // TODO: decide if we are going to add any JMS Properties to the message we are sending
        // to the queue like routingHeader
        mJobControlMessageSender.doSend(jobResourceKey, result.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void suspendJob(final SuspendJobRequestDocument suspendJobRequest) {
        final String jobResourceKey = suspendJobRequest.getSuspendJobRequest()
            .getJobResourceKey();

        LOGGER.debug("DataTransferServiceImpl suspendJob()");
        LOGGER.debug("Suspending job: " + jobResourceKey);

        // after that, let's update the status of this job..
        Job foundJob = null;
        foundJob = mJobRepository.findByResourceKey(jobResourceKey);
        if (foundJob == null) {
            throw new NonExistentJobException("Job doesn't exist.");
        }

        if (foundJob.getStatus().equals(JobStatus.SUSPENDED)) {
            throw new JobStatusUpdateException(
                "Job "
                    + jobResourceKey
                    + " cannot be suspended as it is already in the suspended state.");
        }
        else if (!(foundJob.getStatus().equals(JobStatus.CREATED)
            || foundJob.getStatus().equals(JobStatus.SCHEDULED) || foundJob
            .getStatus().equals(JobStatus.TRANSFERRING))) {
            throw new JobStatusUpdateException(
                "Job "
                    + jobResourceKey
                    + " cannot be suspended as it is has already completed, failed, or cancelled.");
        }

        foundJob.setStatus(JobStatus.SUSPENDED);
        mJobRepository.saveOrUpdate(foundJob);

        // Get the message payload ready for consumption by the JMS layer

        // After the switch to XMLBeans, the Result object has always been a StreamResult instead
        // of a DomResult. So we'll just hand a StringResult object as a parameter to marshal()
        final Result result = new StringResult();

        // TODO: consider rework on this try-catch block
        try {
            mMarshaller.marshal(suspendJobRequest, result);
        }
        catch (final IOException e) {
            throw new InvalidJobDefinitionException(e.fillInStackTrace());
        }

        // TODO: decide if we are going to add any JMS Properties to the message we are sending
        // to the queue like routingHeader
        mJobControlMessageSender.doSend(jobResourceKey, result.toString());
    }

    /**
     * {@inheritDoc}
     */
    public void resumeJob(final ResumeJobRequestDocument resumeJobRequest) {
        final String jobResourceKey = resumeJobRequest.getResumeJobRequest()
            .getJobResourceKey();

        LOGGER.debug("DataTransferServiceImpl resumeJob()");
        LOGGER.debug("Resuming job " + jobResourceKey);

        // after that, let's update the status of this job..
        Job foundJob = null;
        foundJob = mJobRepository.findByResourceKey(jobResourceKey);
        if (foundJob == null) {
            throw new NonExistentJobException("Job doesn't exist.");
        }

        if (!foundJob.getStatus().equals(JobStatus.SUSPENDED)) {
            throw new JobStatusUpdateException("Job " + jobResourceKey
                + " cannot be resumed as it has not been suspended.");
        }

        foundJob.setStatus(JobStatus.TRANSFERRING);
        mJobRepository.saveOrUpdate(foundJob);

        // Get the message payload ready for consumption by the JMS layer

        // After the switch to XMLBeans, the Result object has always been a StreamResult instead
        // of a DomResult. So we'll just hand a StringResult object as a parameter to marshal()
        final Result result = new StringResult();

        // TODO: consider rework on this try-catch block
        try {
            mMarshaller.marshal(resumeJobRequest, result);
        }
        catch (final IOException e) {
            throw new InvalidJobDefinitionException(e.fillInStackTrace());
        }

        // TODO: decide if we are going to add any JMS Properties to the message we are sending
        // to the queue like routingHeader
        mJobControlMessageSender.doSend(jobResourceKey, result.toString());
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(
        final GetJobStatusRequestDocument getJobStatusRequest) {
        final String jobResourceKey = getJobStatusRequest
            .getGetJobStatusRequest().getJobResourceKey();
        LOGGER.debug("DataTransferServiceImpl getJobStatus()");
        LOGGER.debug("Getting job status of job " + jobResourceKey);

        // TODO: need to get this info from the DB.. part of this code need to have the smarts
        // to figure out which status the job is on based on the timing details provided
        // in the DB. if the smarts is not going to be put here, we need to have a way of having
        // the status get updated every time a new job event gets triggered

        Job foundJob = null;
        foundJob = mJobRepository.findByResourceKey(jobResourceKey);
        if (foundJob == null) {
            throw new NonExistentJobException("Job doesn't exist.");
        }
        return foundJob.getStatus().getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    public Job getJobDetails(
        final GetJobDetailsRequestDocument getJobDetailsRequest) {
        final String jobResourceKey = getJobDetailsRequest
            .getGetJobDetailsRequest().getJobResourceKey();
        LOGGER.debug("DataTransferServiceImpl getJobDetails()");
        LOGGER.debug("Getting job details of job " + jobResourceKey);

        Job foundJob = null;
        foundJob = mJobRepository.findByResourceKey(jobResourceKey);
        if (foundJob == null) {
            throw new NonExistentJobException("Job doesn't exist.");
        }

        return foundJob;

    }

    public void setJobSubmitQueueSender(
        final JobQueueSender jobSubmitQueueSender) {
        mJobSubmitMessageSender = jobSubmitQueueSender;
    }

    public void setJobControlQueueSender(
        final JobQueueSender jobControlQueueSender) {
        mJobControlMessageSender = jobControlQueueSender;
    }

    public void setJobRepository(final JobDao jobRepository) {
        mJobRepository = jobRepository;
    }

    public void setMarshaller(final XmlBeansMarshaller marshaller) {
        mMarshaller = marshaller;
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert
            .notNull(mJobSubmitMessageSender,
                "A JobSubmitQueueSender needs to be configured for the DataTransferService.");
        Assert
            .notNull(mJobControlMessageSender,
                "A JobControlMessageSender needs to be configured for the DataTransferService.");
        Assert.notNull(mJobRepository,
            "A JobDao needs to be configured for the DataTransferService.");
        Assert
            .notNull(mMarshaller,
                "A JaxbMarshaller needs to be configured for the DataTransferService");
    }

}
