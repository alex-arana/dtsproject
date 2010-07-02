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

import java.io.FileNotFoundException;
import java.util.List;

/**
 * The DtsJobStepAllocator allocates each DataTransferUnit to be transferred per job step.
 *
 * @author Gerson Galang
 */
interface DtsJobStepAllocator {

    /**
     * This method needs to be called before a new DataTransferType instance gets processed by
     * {@link org.dataminx.dts.batch.JobPartitioningStrategy#prepare() prepare} method. This will make sure that new
     * DataTransferUnits gets added to a new DtsJobStep.
     *
     * @param sourceRootFileObject the Root of the FileObject referred to by the source
     * @param targetRootFileObject the Root of the FileObject referred to by the target
     * @param maxTotalByteSizePerStepLimit the total number in bytes per step limit
     * @param maxTotalFileNumPerStepLimit the total number of files per step limit
     */
    void createNewDataTransfer(final String sourceRootFileObject,
        final String targetRootFileObject,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit);

    /**
     * Adds the new DtsDataTransferUnit to the current DtsJobStep.
     *
     * @param dataTransferUnit the DtsDataTransferUnit to be added to the DtsJobStep
     * @param maxTotalByteSizePerStepLimit the total number in bytes per step limit
     * @param maxTotalFileNumPerStepLimit the total number of files per step limit
     * @throws FileNotFoundException if the given dataTransferUnit could not be persisted
     */
    void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) throws FileNotFoundException;

    /**
     * This method had to be called before a new DataTransfer element is processed.
     * @throws FileNotFoundException if the given dataTransferUnit could not be persisted when
     * closing the data transfer. 
     */
    void closeNewDataTransfer() throws FileNotFoundException;

    /**
     * Path to a directory where the allocator will write all the job step files .
     *
     * @param jobStepDir the directory path where job steps files are persited.
     */
    void setJobStepSaveDir(final String jobStepDir);

    /**
     * Returns the list of DtsJobSteps that has been allocated by the DtsJobStepAllocator.
     *
     * @return the list of DtsJobSteps that has been allocated by the DtsJobStepAllocator
     */
    List<DtsJobStep> getAllocatedJobSteps();

}
