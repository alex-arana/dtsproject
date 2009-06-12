package org.dataminx.dts.domain.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name="job")
public class Job {

	private Integer jobId;
	private String jobResourceKey;
	private String jobName;
	private JobStatus jobStatus;
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
	private Integer volumeTransferred;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="job_id")
	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}

	@Column(name="job_resource_key")
	public String getJobResourceKey() {
		return jobResourceKey;
	}

	public void setJobResourceKey(String jobResourceKey) {
		this.jobResourceKey = jobResourceKey;
	}

	@Column(name="job_name")
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@Enumerated(EnumType.ORDINAL)
	@Column(name="job_status_id")
	public JobStatus getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

	@Column(name="subject_name")
	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	@Column(name="job_description")
	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creation_time")
	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="queued_time")
	public Date getQueuedTime() {
		return queuedTime;
	}

	public void setQueuedTime(Date queuedTime) {
		this.queuedTime = queuedTime;
	}

	@Column(name="success_flag")
	public Boolean isSuccessFlag() {
		return successFlag;
	}

	public void setSuccessFlag(Boolean successFlag) {
		this.successFlag = successFlag;
	}

	@Column(name="finished_flag")
	public Boolean isFinishedFlag() {
		return finishedFlag;
	}

	public void setFinishedFlag(Boolean finishedFlag) {
		this.finishedFlag = finishedFlag;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="active_time")
	public Date getActiveTime() {
		return activeTime;
	}

	public void setActiveTime(Date activeTime) {
		this.activeTime = activeTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="worker_terminated_time")
	public Date getWorkerTerminatedTime() {
		return workerTerminatedTime;
	}

	public void setWorkerTerminatedTime(Date workerTerminatedTime) {
		this.workerTerminatedTime = workerTerminatedTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="job_all_done_time")
	public Date getJobAllDoneTime() {
		return jobAllDoneTime;
	}

	public void setJobAllDoneTime(Date jobAllDoneTime) {
		this.jobAllDoneTime = jobAllDoneTime;
	}

	@Column(name="client_hostname")
	public String getClientHostname() {
		return clientHostname;
	}

	public void setClientHostname(String clientHostname) {
		this.clientHostname = clientHostname;
	}

	@Column(name="execution_host")
	public String getExecutionHost() {
		return executionHost;
	}

	public void setExecutionHost(String executionHost) {
		this.executionHost = executionHost;
	}

	@Column(name="worker_node_host")
	public String getWorkerNodeHost() {
		return workerNodeHost;
	}

	public void setWorkerNodeHost(String workerNodeHost) {
		this.workerNodeHost = workerNodeHost;
	}

	@Column(name="version")
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Column(name="files_total")
	public Integer getFilesTotal() {
		return filesTotal;
	}

	public void setFilesTotal(Integer filesTotal) {
		this.filesTotal = filesTotal;
	}

	@Column(name="files_transferred")
	public Integer getFilesTransferred() {
		return filesTransferred;
	}

	public void setFilesTransferred(Integer filesTransferred) {
		this.filesTransferred = filesTransferred;
	}

	@Column(name="volume_total")
	public Integer getVolumeTotal() {
		return volumeTotal;
	}

	public void setVolumeTotal(Integer volumeTotal) {
		this.volumeTotal = volumeTotal;
	}

	@Column(name="volume_transferred")
	public Integer getVolumeTransferred() {
		return volumeTransferred;
	}

	public void setVolumeTransferred(Integer volumeTransferred) {
		this.volumeTransferred = volumeTransferred;
	}

	public String toString() {
		return "JobName: " +
				jobName + "\n" +
				"JobResourceKey: " +
				jobResourceKey + "\n";
	}



}
