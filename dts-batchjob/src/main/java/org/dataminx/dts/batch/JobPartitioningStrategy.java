package org.dataminx.dts.batch;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

public interface JobPartitioningStrategy {

    DtsJobDetails partitionTheJob(JobDefinitionType jobDefinition, String jobResourceKey);

    void setMaxTotalFileNumPerStepLimit(int maxTotalFileNumPerStepLimit);

    void setMaxTotalByteSizePerStepLimit(long maxTotalByteSizePerStepLimit);

}
