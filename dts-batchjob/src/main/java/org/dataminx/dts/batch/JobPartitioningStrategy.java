package org.dataminx.dts.batch;

import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public interface JobPartitioningStrategy {
    DtsJobDetails partitionTheJob(JobDefinitionType jobDefinition, FileSystemManager fileSystemManager,
            String jobResourceKey);

    void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil);

    void setMaxTotalFileNumPerStepLimit(int maxTotalFileNumPerStepLimit);

    void setMaxTotalByteSizePerStepLimit(long maxTotalByteSizePerStepLimit);
}
