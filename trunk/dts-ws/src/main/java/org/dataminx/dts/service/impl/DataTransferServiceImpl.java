package org.dataminx.dts.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dataminx.dts.service.*;

public class DataTransferServiceImpl implements DataTransferService {

	protected final Log logger = LogFactory.getLog(getClass());

	public String submitJob(String dtsJob) {
		logger.info("In DataTransferServiceImpl, running " + dtsJob + "... ");
		return "finished running " + dtsJob;
	}

}
