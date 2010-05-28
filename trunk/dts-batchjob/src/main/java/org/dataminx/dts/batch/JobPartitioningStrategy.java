/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.batch;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;

/**
 * The JobPartitioningStrategy is an interface to be implemented by different partitioning strategies to be used
 * in allocating the DataTransferUnits to their corresponding DtsJobSteps.
 *
 * @author Gerson Galang
 */
public interface JobPartitioningStrategy {

    /**
     * Partitions the job and returns the details of the partitioned job as DtsJobDetails.
     *
     * @param jobDefinition the job definition document
     * @param jobResourceKey the job resource key
     * @return the details of the partitioned job as DtsJobDetails
     * @throws JobScopingException when any issues arise while the job is being partitioned
     */
    DtsJobDetails partitionTheJob(JobDefinitionType jobDefinition,
        String jobResourceKey) throws JobScopingException;

    /**
     * Sets the maximum total number of files to be applied as the limit to each job step by this partitioning strategy.
     *
     * @param maxTotalFileNumPerStepLimit the maximum total number of files to be applied as the limit to each job step
     * by this partitioning strategy
     */
    void setMaxTotalFileNumPerStepLimit(int maxTotalFileNumPerStepLimit);

    /**
     * Sets the maximum size of all the files to be applied as the limit to each job step by this partitioning strategy.
     *
     * @param maxTotalByteSizePerStepLimit the maximum size of all the files to be applied as the limit to each job step
     * by this partitioning strategy
     */
    void setMaxTotalByteSizePerStepLimit(long maxTotalByteSizePerStepLimit);

    DtsJobStepAllocator createDtsJobStepAllocator();

}
