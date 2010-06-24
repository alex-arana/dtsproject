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

/**
 * A JobStepAllocator that mixes big and small files together in a step.
 *
 * @author Gerson Galang
 */
public class MixedFilesJobStepAllocator implements DtsJobStepAllocator {
    /** The list of DtsJobSteps where DtsTransferUnits will be allocated. */
    private final List<DtsJobStep> mSteps;

    /** A reference to the DtsJobStep. */
    private DtsJobStep mTmpDtsJobStep;

    /** The Source Root FileObject URI string. */
    private String mSourceRootFileObject;

    /** The Target Root FileObject URI string. */
    private String mTargetRootFileObject;

    /**
     * MixedFilesJobStepAllocator's constructor.
     */
    public MixedFilesJobStepAllocator() {
        mSteps = new ArrayList<DtsJobStep>();
    }

    /**
     * {@inheritDoc}
     */
    public void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) {

        // if (mTmpDtsJobStep has already been initialised, and the number of DataTransferUnits in the step
        //   has reached the max total number of files per step limit OR the size of the file we are going
        //   to add will exceed the max size in bytes of all the files per step limit, then) {
        //    - add the current/existing mTmpDtsJobStep to the list of mSteps,
        //    - re-init the current mTmpDtsJobStep
        //    - add the given dataTransferUnit to the current mTmpDtsJobStep's dtu list
        // } else
        //    add the given dataTransferUnit to the current mTmpDtsJobStep' dtu list
        //
        if (mTmpDtsJobStep != null
            && (mTmpDtsJobStep.getCurrentTotalFileNum() >= maxTotalFileNumPerStepLimit || mTmpDtsJobStep
                .getCurrentTotalByteSize()
                + dataTransferUnit.getSize() >= maxTotalByteSizePerStepLimit)) {
            mSteps.add(mTmpDtsJobStep);
            mTmpDtsJobStep = new DtsJobStep(mSourceRootFileObject,
                mTargetRootFileObject, mSteps.size() + 1,
                maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
                DtsJobStep.Type.MIXED_FILES);
            mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
        }
        else {
            // if (!tmpDtsJobStep.isFull())
            mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void closeNewDataTransfer() {
        if (mTmpDtsJobStep.getCurrentTotalFileNum() > 0) {
            mSteps.add(mTmpDtsJobStep);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createNewDataTransfer(final String sourceRootFileObject,
        final String targetRootFileObject,
        final long maxTotalByteSizePerStepLimit,
        final int maxTotalFileNumPerStepLimit) {
        mTmpDtsJobStep = new DtsJobStep(sourceRootFileObject,
            targetRootFileObject, mSteps.size() + 1,
            maxTotalFileNumPerStepLimit, maxTotalByteSizePerStepLimit,
            DtsJobStep.Type.MIXED_FILES);
        mSourceRootFileObject = sourceRootFileObject;
        mTargetRootFileObject = targetRootFileObject;
    }

    /**
     * {@inheritDoc}
     */
    public List<DtsJobStep> getAllocatedJobSteps() {
        return mSteps;
    }
}
