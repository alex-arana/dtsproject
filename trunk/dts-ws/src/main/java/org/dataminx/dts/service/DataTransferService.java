package org.dataminx.dts.service;

import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;

public interface DataTransferService {

	public abstract String submitJob(JobDefinitionType job);

}
