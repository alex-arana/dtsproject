package org.dataminx.dts.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.testng.Assert;

public class CategorizedFilesJobStepAllocator implements DtsJobStepAllocator,
    InitializingBean {

    /** The list of DtsJobSteps where DtsTransferUnits will be allocated. */
    private final List<DtsJobStep> mSteps;

    /** A reference to the DtsJobStep. */
    private DtsJobStep mTmpBigFilesDtsJobStep;

    private DtsJobStep mTmpSmallFilesDtsJobStep;

    /** The Source Root FileObject URI string. */
    private String mSourceRootFileObject;

    /** The Target Root FileObject URI string. */
    private String mTargetRootFileObject;

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
