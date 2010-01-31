package org.dataminx.dts.wn.batch;

import org.dataminx.dts.vfs.DtsFileSystemManager;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public interface JobScoper {
	void setFileSystemManager(DtsFileSystemManager fileSystemManager);
	DtsJobDetails scopeTheJob(JobDefinitionType jobDefinition);
}
