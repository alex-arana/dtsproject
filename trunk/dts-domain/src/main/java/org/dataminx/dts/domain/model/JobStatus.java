package org.dataminx.dts.domain.model;

public enum JobStatus {
	CREATED(0),
	SCHEDULED(1),
	TRANSFERRING(2),
	DONE(3),
	SUSPENDED(4),
	FAILED(5),
	FAILED_CLEAN(6),
	FAILED_UNCLEAN(7),
	FAILED_UNKNOWN(8);

	private int jobStatus;
	JobStatus(int jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String toString() {
		switch (jobStatus) {
		case 0:
			return "Created";
		case 1:
			return "Scheduled";
		case 2:
			return "Transferring";
		case 3:
			return "Done";
		case 4:
			return "Suspended";
		case 5:
			return "Failed";
		case 6:
			return "Failed:Clean";
		case 7:
			return "Failed:Unclean";
		default:
			return "Failed:Unknown";
		}
	}
}
