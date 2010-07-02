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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * A JobStepAllocator that mixes big and small files together in a step.
 *
 * @author Gerson Galang
 * @author David Meredith 
 */
public class MixedFilesJobStepAllocator implements DtsJobStepAllocator {
    /** The list of DtsJobSteps where DtsTransferUnits will be allocated.
     TODO: remove this list and write the steps to file rather than hold an in-mem collection */
    private final List<DtsJobStep> mSteps;

    /** A reference to the current DtsJobStep that is being constructed. */
    private DtsJobStep mTmpDtsJobStep;

    /** The Source Root FileObject URI string. */
    private String mSourceRootFileObject;

    /** The Target Root FileObject URI string. */
    private String mTargetRootFileObject;

    /** Counts the number of steps generated */
    private int mStepCount = 0;

    /** TODO The directory path jobStep files will be persisted */
    private String mJobStepDir = (new File(System.getProperty("java.io.tmpdir"))).getAbsolutePath();

    /**
     * MixedFilesJobStepAllocator's constructor.
     */
    public MixedFilesJobStepAllocator() {
        mSteps = new ArrayList<DtsJobStep>(0);
    }

    /**
     * {@inheritDoc}
     */
    public void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) throws FileNotFoundException{

        if (mTmpDtsJobStep != null
                && (mTmpDtsJobStep.getCurrentTotalFileNum()  < maxTotalFileNumPerStepLimit
                && (mTmpDtsJobStep.getCurrentTotalByteSize() + dataTransferUnit.getSize()) <= maxTotalByteSizePerStepLimit)) {
            // with the addition of this dtu, we are less than the current
            // step file count limit and will be less than or equal to the step
            // byte limit so we can add the dtu to the current step.
            mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
        } else {
            // otherwise, with the addition of this dtu, either the file count
            // limit or the byte limit for the current step will be exceeded.
            // Therefore, persist the current job step without adding the dtu
            // and create a new jobStep and add the dtu to the new step.
            this.persistCurrentJobStep();
           
            this.createNewDataTransfer(
                    mSourceRootFileObject,
                    mTargetRootFileObject,
                    maxTotalByteSizePerStepLimit,
                    maxTotalFileNumPerStepLimit);
            mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
        }

    }

    /**
     * Persist the current job step 
     * @throws FileNotFoundException
     */
    private void persistCurrentJobStep() throws FileNotFoundException{
        mTmpDtsJobStep.writeToFile();
        // TODO: do not save the step in this.mSteps as this collection
        // will no longer be needed when all of the jobStep state is written
        // to file.
        mTmpDtsJobStep.clearDataTransferUnits();
        mSteps.add(mTmpDtsJobStep);
    }

    /**
     * {@inheritDoc}
     */
    public void closeNewDataTransfer() throws FileNotFoundException {
        if (mTmpDtsJobStep.getCurrentTotalFileNum() > 0) {
            this.persistCurrentJobStep();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createNewDataTransfer(final String sourceRootFileObject,
        final String targetRootFileObject,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) {
        // init member vars
        ++this.mStepCount;
        String jobStepFilePath = (new File(this.mJobStepDir, this.mStepCount + "_jobStep.dts")).getAbsolutePath();
        this.mTmpDtsJobStep = new DtsJobStep(sourceRootFileObject,
            targetRootFileObject, mStepCount, //mSteps.size() + 1,
            maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
            DtsJobStep.Type.MIXED_FILES, jobStepFilePath);
        this.mSourceRootFileObject = sourceRootFileObject;
        this.mTargetRootFileObject = targetRootFileObject;
        
    }

    /**
     * {@inheritDoc}
     */
    public List<DtsJobStep> getAllocatedJobSteps() {
        return mSteps;
    }

    //public int getStepCount(){
    //    return this.mStepCount;
    //}


    /**
     * {@inheritDoc}
     */
    public void setJobStepSaveDir(String jobStepDirPath) {
        this.mJobStepDir = jobStepDirPath;
    }
}