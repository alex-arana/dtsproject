package org.dataminx.dts.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.service.DataTransferService;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.StatusValueEnumeration;

// TODO: Auto-generated Javadoc
/**
 * The Class DataTransferServiceImpl.
 */
public class DataTransferServiceImpl implements DataTransferService {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceImpl.class);

    /** The pre-generated UUID used for testing purposes. */
    private static final String PRE_GENERATED_UUID = "66296c5e-9a34-4e24-8fa2-4fef329e084e";

    /** The test jobs. */
    private Map<String, TestJob> mDummyJobs;

    /**
     * {@inheritDoc}
     */
    public String submitJob(JobDefinitionType job) {
        String jobName = job.getJobDescription().getJobIdentification().getJobName();

        LOGGER.debug("In DataTransferServiceImpl.submitJob, running job " + jobName + "... ");

        // check to see if we're only interested in testing this WS module..
        if (jobName.equals("__test_this_dtsws_job__")) {

            // create a map of dummy jobs being run by the WS which could be queried later on.
            mDummyJobs = new HashMap<String, TestJob>();

            // first, create the job that has just been submitted and put in the map
            TestJob dummyJob;
            dummyJob = new TestJob();
            dummyJob.setId(PRE_GENERATED_UUID);
            dummyJob.setName(jobName);
            dummyJob.setStatus(StatusValueEnumeration.CREATED);
            mDummyJobs.put(dummyJob.getId(), dummyJob);

            // then, create other dummy jobs to be run by the DTS WS
            // for convenience sake, there will be other 9 dummy jobs
            for (int i = 0; i < 9; i++) {
                dummyJob = new TestJob();
                dummyJob.setId(java.util.UUID.randomUUID().toString());
                dummyJob.setName("job_000" + i);
                dummyJob.setStatus(StatusValueEnumeration.CREATED);
                mDummyJobs.put(dummyJob.getId(), dummyJob);
            }
            return PRE_GENERATED_UUID;
        }
        // else we are doing the real thing..
        // TODO: put the code that integrates this module to the domain layer


        // TODO: make sure that the job definition is valid before the job
        // gets added onto the DB

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.cancelJob, cancelling job " + jobId + "... ");

        if (jobId.equals(PRE_GENERATED_UUID)) {
            // check if the job with jobId is in the map. if it is, update it's status to Done
            TestJob job = mDummyJobs.get(jobId);
            job.setStatus(StatusValueEnumeration.DONE);
        }
        //else {
            // TODO: do the real thing, send a cancel job message to the worker node
            // wait for its response and then update the DB
        //}
    }

    /**
     * {@inheritDoc}
     */
    public void suspendJob(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.suspendJob, suspending job " + jobId + "... ");
        if (jobId.equals(PRE_GENERATED_UUID)) {
            // check if the job with jobId is in the map. if it is, update it's status to Suspended
            TestJob job = mDummyJobs.get(jobId);
            job.setStatus(StatusValueEnumeration.SUSPENDED);
        }
        //else {
            // TODO: do the real thing, send a suspend job message to the worker node
            // wait for its response and then update the DB
        //}
    }

    /**
     * {@inheritDoc}
     */
    public void resumeJob(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.resumeJob, resuming job " + jobId + "... ");
        if (jobId.equals(PRE_GENERATED_UUID)) {
            // check if the job with jobId is in the map. if it is, update it's status to Transferring again...
            TestJob job = mDummyJobs.get(jobId);
            job.setStatus(StatusValueEnumeration.TRANSFERRING);
        }
        //else {
            // TODO: do the real thing, send a resume job message to the worker node
            // wait for its response and then update the DB
        //}
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(String jobId) {
        LOGGER.debug("In DataTransferServiceImpl.cancelJob, getting job status for " + jobId  + "... ");
        if (jobId.equals(PRE_GENERATED_UUID)) {
            LOGGER.debug("job status = " + mDummyJobs.get(jobId).getStatus());
            return mDummyJobs.get(jobId).getStatus().value();
        }
        // else...
        // TODO: need to get this info from the DB.. part of this code need to have the smarts
        // to figure out which status the job is on based on the timing details provided
        // in the DB
        return null;
    }

    /**
     * The wrapper object for a Test Job.
     */
    private class TestJob {

        /** The id. */
        private String mId;

        /** The name. */
        private String mName;

        /** The status. */
        private StatusValueEnumeration mStatus;

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return mId;
        }

        /**
         * Sets the id.
         *
         * @param id the new id
         */
        public void setId(String id) {
            mId = id;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return mName;
        }

        /**
         * Sets the name.
         *
         * @param name the new name
         */
        public void setName(String name) {
            mName = name;
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public StatusValueEnumeration getStatus() {
            return mStatus;
        }

        /**
         * Sets the status.
         *
         * @param status the new status
         */
        public void setStatus(StatusValueEnumeration status) {
            mStatus = status;
        }
    }
}
