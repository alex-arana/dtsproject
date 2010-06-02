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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.testng.Assert;

/**
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
            // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step
            // has reached the max total number of files per step limit OR the size of the file we are going
            // to add will exceed the max size in bytes of all the files per step limit)
            if (mTmpBigFilesDtsJobStep != null
                && (mTmpBigFilesDtsJobStep.getCurrentTotalFileNum() >= maxTotalFileNumPerStepLimit || mTmpBigFilesDtsJobStep
                    .getCurrentTotalByteSize()
                    + dataTransferUnit.getSize() >= maxTotalByteSizePerStepLimit)) {
                mSteps.add(mTmpBigFilesDtsJobStep);
                mTmpBigFilesDtsJobStep = new DtsJobStep(mSourceRootFileObject,
                    mTargetRootFileObject, mSteps.size() + 1,
                    maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
                    DtsJobStep.Type.BIG_FILES);
                mTmpBigFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
            else {
                // if (!mTmpBigFilesDtsJobStep.isFull())
                mTmpBigFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
        }
        else { // it is a small file
            // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step
            // has reached the max total number of files per step limit OR the size of the file we are going
            // to add will exceed the max size in bytes of all the files per step limit)
            if (mTmpSmallFilesDtsJobStep != null
                && (mTmpSmallFilesDtsJobStep.getCurrentTotalFileNum() >= maxTotalFileNumPerStepLimit || mTmpSmallFilesDtsJobStep
                    .getCurrentTotalByteSize()
                    + dataTransferUnit.getSize() >= maxTotalByteSizePerStepLimit)) {
                mSteps.add(mTmpSmallFilesDtsJobStep);
                mTmpSmallFilesDtsJobStep = new DtsJobStep(
                    mSourceRootFileObject, mTargetRootFileObject,
                    mSteps.size() + 1, maxTotalFileNumPerStepLimit,
                    maxTotalByteSizePerStepLimit, DtsJobStep.Type.SMALL_FILES);
                mTmpSmallFilesDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
            else {
                // if (!mTmpSmallFilesDtsJobStep.isFull())
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

        mTmpBigFilesDtsJobStep = new DtsJobStep(sourceRootFileObject,
            targetRootFileObject, mSteps.size() + 1,
            maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
            DtsJobStep.Type.BIG_FILES);

        mTmpSmallFilesDtsJobStep = new DtsJobStep(sourceRootFileObject,
            targetRootFileObject, mSteps.size() + 1,
            maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
            DtsJobStep.Type.SMALL_FILES);

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
        Assert.assertEquals(mBigFileSize > 0, true,
            "Big file size should have a positive value");

    }
}
