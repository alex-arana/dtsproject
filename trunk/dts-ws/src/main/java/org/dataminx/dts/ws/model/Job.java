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
package org.dataminx.dts.ws.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.dataminx.dts.common.model.JobStatus;

/**
 * The Job Entity.
 *
 * @author Gerson Galang
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "Job.findJobByResourceKey",
                    query = "SELECT j FROM Job j WHERE j.resourceKey = :resourceKey"),
        @NamedQuery(name = "Job.findJobByUser",
                    query = "SELECT j FROM Job j WHERE j.subjectName = :userDN"),
        @NamedQuery(name = "Job.findJobByUserAndStatus",
                    query = "SELECT j FROM Job j WHERE j.subjectName = :userDN AND j.status = :status") })
@Table(name = "job")
public class Job {

    /** The job id. */
    private Integer mJobId;

    /** The job resource key. */
    private String mResourceKey;

    /** The jobname. */
    private String mName;

    /** The job status. */
    private JobStatus mStatus;

    /** The user's distinguished name. */
    private String mSubjectName;

    /** The job description. */
    private String mDescription;

    /** The creation time. */
    private Date mCreationTime;

    /** The queued time. */
    private Date mQueuedTime;

    /** The success flag. */
    private Boolean mSuccessFlag;

    /** The finished flag. */
    private Boolean mFinishedFlag;

    /** The active time. */
    private Date mActiveTime;

    /** The worker terminated time. */
    private Date mWorkerTerminatedTime;

    /** The job all done time. */
    private Date mJobAllDoneTime;

    /** The client hostname. */
    private String mClientHostname;

    /** The execution host. */
    private String mExecutionHost;

    /** The worker node host. */
    private String mWorkerNodeHost;

    /** The version. */
    private String mVersion;

    /** The files total. */
    private Integer mFilesTotal;

    /** The files transferred. */
    private Integer mFilesTransferred;

    /** The volume total. */
    private Long mVolumeTotal;

    /** The volume transferred. */
    private Long mVolumeTransferred;

    /**
     * Gets the job id.
     *
     * @return the job id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "job_id")
    public Integer getJobId() {
        return mJobId;
    }

    /**
     * Sets the job id.
     *
     * @param jobId the new job id
     */
    public void setJobId(final Integer jobId) {
        mJobId = jobId;
    }

    /**
     * Gets the job resource key.
     *
     * @return the job resource key
     */
    @Column(name = "resource_key")
    public String getResourceKey() {
        return mResourceKey;
    }

    /**
     * Sets the job resource key.
     *
     * @param resourceKey the new job resource key
     */
    public void setResourceKey(final String resourceKey) {
        mResourceKey = resourceKey;
    }

    /**
     * Gets the job name.
     *
     * @return the job name
     */
    @Column(name = "name")
    public String getName() {
        return mName;
    }

    /**
     * Sets the job name.
     *
     * @param name the new job name
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status_id")
    public JobStatus getStatus() {
        return mStatus;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(final JobStatus status) {
        mStatus = status;
    }

    /**
     * Gets the distinguished name of the owner of this job.
     *
     * @return the subject name
     */
    @Column(name = "subject_name")
    public String getSubjectName() {
        return mSubjectName;
    }

    /**
     * Sets the distinguished name of the owner of this job.
     *
     * @param subjectName the new subject name
     */
    public void setSubjectName(final String subjectName) {
        mSubjectName = subjectName;
    }

    /**
     * Gets the job description.
     *
     * @return the job description
     */
    @Column(name = "job_description")
    public String getJobDescription() {
        return mDescription;
    }

    /**
     * Sets the job description.
     *
     * @param jobDescription the new job description
     */
    public void setJobDescription(final String jobDescription) {
        mDescription = jobDescription;
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_time")
    public Date getCreationTime() {
        return mCreationTime;
    }

    /**
     * Sets the creation time.
     *
     * @param creationTime the new creation time
     */
    public void setCreationTime(final Date creationTime) {
        mCreationTime = creationTime;
    }

    /**
     * Gets the queued time.
     *
     * @return the queued time
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "queued_time")
    public Date getQueuedTime() {
        return mQueuedTime;
    }

    /**
     * Sets the queued time.
     *
     * @param queuedTime the new queued time
     */
    public void setQueuedTime(final Date queuedTime) {
        mQueuedTime = queuedTime;
    }

    /**
     * Checks if is success flag.
     *
     * @return the boolean
     */
    @Column(name = "success_flag")
    public Boolean getSuccessFlag() {
        return mSuccessFlag;
    }

    /**
     * Sets the success flag.
     *
     * @param successFlag the new success flag
     */
    public void setSuccessFlag(final Boolean successFlag) {
        mSuccessFlag = successFlag;
    }

    /**
     * Checks if is finished flag.
     *
     * @return the boolean
     */
    @Column(name = "finished_flag")
    public Boolean getFinishedFlag() {
        return mFinishedFlag;
    }

    /**
     * Sets the finished flag.
     *
     * @param finishedFlag the new finished flag
     */
    public void setFinishedFlag(final Boolean finishedFlag) {
        mFinishedFlag = finishedFlag;
    }

    /**
     * Gets the active time.
     *
     * @return the active time
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "active_time")
    public Date getActiveTime() {
        return mActiveTime;
    }

    /**
     * Sets the active time.
     *
     * @param activeTime the new active time
     */
    public void setActiveTime(final Date activeTime) {
        mActiveTime = activeTime;
    }

    /**
     * Gets the worker terminated time.
     *
     * @return the worker terminated time
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "worker_terminated_time")
    public Date getWorkerTerminatedTime() {
        return mWorkerTerminatedTime;
    }

    /**
     * Sets the worker terminated time.
     *
     * @param workerTerminatedTime the new worker terminated time
     */
    public void setWorkerTerminatedTime(final Date workerTerminatedTime) {
        mWorkerTerminatedTime = workerTerminatedTime;
    }

    /**
     * Gets the job all done time.
     *
     * @return the job all done time
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "job_all_done_time")
    public Date getJobAllDoneTime() {
        return mJobAllDoneTime;
    }

    /**
     * Sets the job all done time.
     *
     * @param jobAllDoneTime the new job all done time
     */
    public void setJobAllDoneTime(final Date jobAllDoneTime) {
        mJobAllDoneTime = jobAllDoneTime;
    }

    /**
     * Gets the client hostname.
     *
     * @return the client hostname
     */
    @Column(name = "client_hostname")
    public String getClientHostname() {
        return mClientHostname;
    }

    /**
     * Sets the client hostname.
     *
     * @param clientHostname the new client hostname
     */
    public void setClientHostname(final String clientHostname) {
        mClientHostname = clientHostname;
    }

    /**
     * Gets the execution host.
     *
     * @return the execution host
     */
    @Column(name = "execution_host")
    public String getExecutionHost() {
        return mExecutionHost;
    }

    /**
     * Sets the execution host.
     *
     * @param executionHost the new execution host
     */
    public void setExecutionHost(final String executionHost) {
        mExecutionHost = executionHost;
    }

    /**
     * Gets the worker node host.
     *
     * @return the worker node host
     */
    @Column(name = "worker_node_host")
    public String getWorkerNodeHost() {
        return mWorkerNodeHost;
    }

    /**
     * Sets the worker node host.
     *
     * @param workerNodeHost the new worker node host
     */
    public void setWorkerNodeHost(final String workerNodeHost) {
        mWorkerNodeHost = workerNodeHost;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    @Column(name = "version")
    public String getVersion() {
        return mVersion;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(final String version) {
        mVersion = version;
    }

    /**
     * Gets the files total.
     *
     * @return the files total
     */
    @Column(name = "files_total")
    public Integer getFilesTotal() {
        return mFilesTotal;
    }

    /**
     * Sets the files total.
     *
     * @param filesTotal the new files total
     */
    public void setFilesTotal(final Integer filesTotal) {
        mFilesTotal = filesTotal;
    }

    /**
     * Gets the files transferred.
     *
     * @return the files transferred
     */
    @Column(name = "files_transferred")
    public Integer getFilesTransferred() {
        return mFilesTransferred;
    }

    /**
     * Sets the files transferred.
     *
     * @param filesTransferred the new files transferred
     */
    public void setFilesTransferred(final Integer filesTransferred) {
        mFilesTransferred = filesTransferred;
    }

    /**
     * Gets the volume total.
     *
     * @return the volume total
     */
    @Column(name = "volume_total")
    public Long getVolumeTotal() {
        return mVolumeTotal;
    }

    /**
     * Sets the volume total.
     *
     * @param volumeTotal the new volume total
     */
    public void setVolumeTotal(final Long volumeTotal) {
        mVolumeTotal = volumeTotal;
    }

    /**
     * Gets the volume transferred.
     *
     * @return the volume transferred
     */
    @Column(name = "volume_transferred")
    public Long getVolumeTransferred() {
        return mVolumeTransferred;
    }

    /**
     * Sets the volume transferred.
     *
     * @param volumeTransferred the new volume transferred
     */
    public void setVolumeTransferred(final Long volumeTransferred) {
        mVolumeTransferred = volumeTransferred;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "JobName: " + mName + "\n" + "JobResourceKey: " + mResourceKey + "\n" + "JobStatusId: " + mStatus + "\n"
                + "JobSubjectName: " + mSubjectName + "\n";
        /*
        private Integer mJobId;
        private String jobResourceKey;
        private String jobName;
        private JobStatus status;
        private String subjectName;
        private String jobDescription;
        private Date creationTime;
        private Date queuedTime;
        private Boolean successFlag;
        private Boolean finishedFlag;
        private Date activeTime;
        private Date workerTerminatedTime;
        private Date jobAllDoneTime;
        private String clientHostname;
        private String executionHost;
        private String workerNodeHost;
        private String version;
        private Integer filesTotal;
        private Integer filesTransferred;
        private Integer volumeTotal;
        private Integer volumeTransferred;*/
    }

}
