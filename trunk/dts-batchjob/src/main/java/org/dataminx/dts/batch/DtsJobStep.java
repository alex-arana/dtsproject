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

import java.util.List;

/**
 * The DTSJobStep is an object representation of the DTS Job Step.
 *
 * @author Gerson Galang
 */
public interface DtsJobStep {

    /**
     * Returns the list of DtsDataTransferUnits for this job step.
     *
     * @return the list of DtsDataTransferUnits for this job step
     */
    List<DtsDataTransferUnit> getDataTransferUnits();

    /**
     * Returns the job step ID.
     *
     * @return the job step ID
     */
    int getStepId();

    /**
     * Add a DtsDataTransferUnit to the list of DtsDataTransferUnits for this step.
     *
     * @param dataTransferUnit the DtsDataTransferUnit to be added to the list of DtsDataTransferUnits for this step
     * @return true if the dataTransferUnit has been successfully added to the list, false otherwise
     */
    boolean addDataTransferUnit(DtsDataTransferUnit dataTransferUnit);

    /**
     * Returns the current total number of files to be transferred by this job step.
     *
     * @return the current total number of files to be transferred by this job step
     */
    int getCurrentTotalFileNum();

    /**
     * Returns the current total size of files (in bytes) to be transferred by this job step.
     *
     * @return the current total size of files (in bytes) to be transferred by this job step
     */
    long getCurrentTotalByteSize();

    /**
     * Returns the Root of the FileObject referred to by the Source of this job step.
     *
     * @return the Root of the FileObject referred to by the Source of this job step
     */
    String getSourceRootFileObjectString();

    /**
     * Returns the Root of the FileObject referred to by the Target of this job step.
     *
     * @return the Root of the FileObject referred to by the Target of this job step
     */
    String getTargetRootFileObjectString();

    /**
     * Returns the name of the job step file.
     *
     * @return the name of the job step file
     */
    String getJobStepFilename();

    /**
     * Sets the name of the job step file.
     *
     * @param filename the name of the job step file
     */
    void setJobStepFilename(String filename);

}
