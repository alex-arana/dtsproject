package org.dataminx.dts.client;

import java.io.IOException;
import org.dataminx.schemas.dts._2009._05.dts.*;

import org.w3c.dom.Document;

public interface DataTransferServiceClient {
	public abstract String submitJob(Document dtsJob) throws IOException;
	public abstract void cancelJob(String jobId);
	public abstract void suspendJob(String jobId);
	public abstract void resumeJob(String jobId);
	public abstract String getJobStatus(String jobId);
}
