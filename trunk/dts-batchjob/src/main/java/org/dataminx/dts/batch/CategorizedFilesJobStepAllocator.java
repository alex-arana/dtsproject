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
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * TODO - Currently broken - needs work.
 * A JobStepAllocator that groups files based on their sizes.
 *
 * @author Gerson Galang
 */
public class CategorizedFilesJobStepAllocator implements DtsJobStepAllocator,
    InitializingBean {

    /** The list of DtsJobSteps where DtsTransferUnits will be allocated. */
    private final List<DtsJobStep> mSteps;

    /** A reference to the DtsJobStep that holds big files. */
    private DtsJobStep mTmpBigFilesDtsJobStep;

    /** A reference to the DtsJobStep that holds small files. */
    private DtsJobStep mTmpSmallFilesDtsJobStep;

    /** The Source Root FileObject URI string. */
    private String mSourceRootFileObject;

    /** The Target Root FileObject URI string. */
    private String mTargetRootFileObject;

    /** The cut off size for a file to be considered big. */
    private long mBigFileSize;

     /** Counts the number of steps generated */
    //private int mStepCount = 0;

    /** The directory path jobStep files will be persisted */
    private String mJobStepDir = (new File(System.getProperty("java.io.tmpdir"))).getAbsolutePath();

    /**
     * MixedFilesJobStepAllocator's constructor.
     */
    public CategorizedFilesJobStepAllocator() {
        mSteps = new ArrayList<DtsJobStep>();
    }

    /**
     * {@inheritDoc}
     */
    public void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) {

        if (dataTransferUnit.getSize() >= mBigFileSize) {
            if (mTmpBigFilesDtsJobStep != null
                    && (mTmpBigFilesDtsJobStep.getCurrentTotalFileNum() < maxTotalFileNumPerStepLimit
                    && mTmpBigFilesDtsJobStep.getCurrentTotalByteSize() + dataTransferUnit.getSize() <= maxTotalByteSizePerStepLimit)) {

                // with the addition of this dtu, we are less than the current
                // step file count limit and will be less than or equal to the step
                // byte limit so we can add the dtu to the current step.
                mTmpBigFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);

            } else {
                // otherwise, with the addition of this dtu, either the file count
                // limit or the byte limit for the current step will be exceeded.
                // Therefore, persist the current job step without adding the dtu
                // and create a new jobStep and add the dtu to the new step.
                mSteps.add(mTmpBigFilesDtsJobStep);
                String jobStepFilePath = (new File(this.mJobStepDir,  mSteps.size() + 1/*this.mStepCount*/ + "_jobStep.dts")).getAbsolutePath();

                mTmpBigFilesDtsJobStep = new DtsJobStep(mSourceRootFileObject,
                        mTargetRootFileObject, mSteps.size() + 1,
                        maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
                        DtsJobStep.Type.BIG_FILES, jobStepFilePath);
                mTmpBigFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);

            }


        } else { // it is a small file
            if (mTmpSmallFilesDtsJobStep != null
                    && (mTmpSmallFilesDtsJobStep.getCurrentTotalFileNum() < maxTotalFileNumPerStepLimit
                    && mTmpSmallFilesDtsJobStep.getCurrentTotalByteSize() + dataTransferUnit.getSize() <= maxTotalByteSizePerStepLimit)) {

                // with the addition of this dtu, we are less than the current
                // step file count limit and will be less than or equal to the step
                // byte limit so we can add the dtu to the current step.
                mTmpSmallFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);

            } else {
                // otherwise, with the addition of this dtu, either the file count
                // limit or the byte limit for the current step will be exceeded.
                // Therefore, persist the current job step without adding the dtu
                // and create a new jobStep and add the dtu to the new step.
                String jobStepFilePath = (new File(this.mJobStepDir,  mSteps.size() + 1/*this.mStepCount*/ + "_jobStep.dts")).getAbsolutePath();
                mSteps.add(mTmpSmallFilesDtsJobStep);

                mTmpSmallFilesDtsJobStep = new DtsJobStep(
                        mSourceRootFileObject, mTargetRootFileObject,
                        mSteps.size() + 1, maxTotalFileNumPerStepLimit,
                        maxTotalByteSizePerStepLimit, DtsJobStep.Type.SMALL_FILES, jobStepFilePath);
                mTmpSmallFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void closeNewDataTransfer() {
        if (mTmpBigFilesDtsJobStep.getCurrentTotalFileNum() > 0) {
            mSteps.add(mTmpBigFilesDtsJobStep);
        }
        if (mTmpSmallFilesDtsJobStep.getCurrentTotalFileNum() > 0) {
            mSteps.add(mTmpSmallFilesDtsJobStep);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createNewDataTransfer(final String sourceRootFileObject,
        final String targetRootFileObject,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) {

        if (mBigFileSize >= maxTotalByteSizePerStepLimit) {
            throw new RuntimeException(
                "The maximum total byte size limit per step should be equal "
                    + "or more than the big files size");
        }

        //++this.mStepCount;
        String jobStepFilePath = (new File(this.mJobStepDir,  mSteps.size() + 1/*this.mStepCount*/ + "_jobStep.dts")).getAbsolutePath();

        mTmpBigFilesDtsJobStep = new DtsJobStep(sourceRootFileObject,
            targetRootFileObject, mSteps.size() + 1,
            maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
            DtsJobStep.Type.BIG_FILES, jobStepFilePath);

        mTmpSmallFilesDtsJobStep = new DtsJobStep(sourceRootFileObject,
            targetRootFileObject, mSteps.size() + 1,
            maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
            DtsJobStep.Type.SMALL_FILES, jobStepFilePath);

        mSourceRootFileObject = sourceRootFileObject;
        mTargetRootFileObject = targetRootFileObject;
    }

    /**
     * {@inheritDoc}
     */
    public List<DtsJobStep> getAllocatedJobSteps() {
        return mSteps;
    }

    public void setBigFileSize(final long bigFileSize) {
        mBigFileSize = bigFileSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(mBigFileSize > 0,
            "Big file size should have a positive value");

    }



    /**
     * {@inheritDoc}
     */
    public void setJobStepSaveDir(String jobStepDirPath) {
        this.mJobStepDir = jobStepDirPath;
    }


    //public int getStepCount(){
    //    return this.mStepCount;
    //}
}


/*
 * public void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) {

        if (dataTransferUnit.getSize() >= mBigFileSize) {
            // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step
            // has reached the max total number of files per step limit OR the size of the file we are going
            // to add will exceed the max size in bytes of all the files per step limit)
            if (mTmpBigFilesDtsJobStep != null
                    && (mTmpBigFilesDtsJobStep.getCurrentTotalFileNum() >= maxTotalFileNumPerStepLimit
                    || mTmpBigFilesDtsJobStep.getCurrentTotalByteSize() + dataTransferUnit.getSize() >= maxTotalByteSizePerStepLimit)) {

                mSteps.add(mTmpBigFilesDtsJobStep);
                mTmpBigFilesDtsJobStep = new DtsJobStep(mSourceRootFileObject,
                        mTargetRootFileObject, mSteps.size() + 1,
                        maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
                        DtsJobStep.Type.BIG_FILES);
                mTmpBigFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);


            } else {
                // if (!mTmpBigFilesDtsJobStep.isFull())
                mTmpBigFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }


        } else { // it is a small file
            // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step
            // has reached the max total number of files per step limit OR the size of the file we are going
            // to add will exceed the max size in bytes of all the files per step limit)
            if (mTmpSmallFilesDtsJobStep != null
                    && (mTmpSmallFilesDtsJobStep.getCurrentTotalFileNum() >= maxTotalFileNumPerStepLimit || mTmpSmallFilesDtsJobStep.getCurrentTotalByteSize()
                    + dataTransferUnit.getSize() >= maxTotalByteSizePerStepLimit)) {
                mSteps.add(mTmpSmallFilesDtsJobStep);
                mTmpSmallFilesDtsJobStep = new DtsJobStep(
                        mSourceRootFileObject, mTargetRootFileObject,
                        mSteps.size() + 1, maxTotalFileNumPerStepLimit,
                        maxTotalByteSizePerStepLimit, DtsJobStep.Type.SMALL_FILES);
                mTmpSmallFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            } else {
                // if (!mTmpSmallFilesDtsJobStep.isFull())
                mTmpSmallFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
        }
    }
 */