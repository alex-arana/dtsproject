package org.dataminx.dts.wn.batch;

import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public interface JobScoper {
	void setFileSystemManager(FileSystemManager fileSystemManager);
	DtsJobDetails scopeTheJob(JobDefinitionType jobDefinition);
	void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil);
}
