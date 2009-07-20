package org.dataminx.dts.domain.model;

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
                    query = "SELECT j FROM Job j WHERE j.subjectName = :userDN AND j.status = :status")
})
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
    private Integer mVolumeTotal;

    /** The volume transferred. */
    private Integer mVolumeTransferred;

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
    public void setJobId(Integer jobId) {
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
    public void setResourceKey(String resourceKey) {
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
    public void setName(String name) {
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
    public void setStatus(JobStatus status) {
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
    public void setSubjectName(String subjectName) {
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
    public void setJobDescription(String jobDescription) {
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
    public void setCreationTime(Date creationTime) {
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
    public void setQueuedTime(Date queuedTime) {
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
    public void setSuccessFlag(Boolean successFlag) {
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
    public void setFinishedFlag(Boolean finishedFlag) {
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
    public void setActiveTime(Date activeTime) {
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
    public void setWorkerTerminatedTime(Date workerTerminatedTime) {
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
    public void setJobAllDoneTime(Date jobAllDoneTime) {
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
    public void setClientHostname(String clientHostname) {
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
    public void setExecutionHost(String executionHost) {
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
    public void setWorkerNodeHost(String workerNodeHost) {
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
    public void setVersion(String version) {
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
    public void setFilesTotal(Integer filesTotal) {
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
    public void setFilesTransferred(Integer filesTransferred) {
        mFilesTransferred = filesTransferred;
    }

    /**
     * Gets the volume total.
     *
     * @return the volume total
     */
    @Column(name = "volume_total")
    public Integer getVolumeTotal() {
        return mVolumeTotal;
    }

    /**
     * Sets the volume total.
     *
     * @param volumeTotal the new volume total
     */
    public void setVolumeTotal(Integer volumeTotal) {
        mVolumeTotal = volumeTotal;
    }

    /**
     * Gets the volume transferred.
     *
     * @return the volume transferred
     */
    @Column(name = "volume_transferred")
    public Integer getVolumeTransferred() {
        return mVolumeTransferred;
    }

    /**
     * Sets the volume transferred.
     *
     * @param volumeTransferred the new volume transferred
     */
    public void setVolumeTransferred(Integer volumeTransferred) {
        mVolumeTransferred = volumeTransferred;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "JobName: " + mName + "\n" + "JobResourceKey: " + mResourceKey + "\n"
            + "JobStatusId: " + mStatus + "\n" + "JobSubjectName: " + mSubjectName + "\n";
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
