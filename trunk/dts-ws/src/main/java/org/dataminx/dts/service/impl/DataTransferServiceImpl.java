package org.dataminx.dts.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dataminx.dts.service.*;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;

public class DataTransferServiceImpl implements DataTransferService {

	protected final Log logger = LogFactory.getLog(getClass());

	public String submitJob(JobDefinitionType job) {
		logger.info("In DataTransferServiceImpl, running " +
				job.getJobDescription().getJobIdentification().getJobName() + "... ");
		return "http://1234randomness";
	}

}
