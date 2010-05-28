package org.dataminx.dts.batch;

import java.util.ArrayList;
import java.util.List;

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

        // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step
        // has reached the max total number of files per step limit OR the size of the file we are going
        // to add will exceed the max size in bytes of all the files per step limit)
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
