package org.dataminx.dts.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

public class TransferMixedFilesStep implements DtsJobStep, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Log LOGGER = LogFactory.getLog(TransferMixedFilesStep.class);

    private List<DtsDataTransferUnit> mDataTransferUnits = null;
    private int mStepId = 0;
    private final int mMaxTotalFileNumLimit;
    private final long mMaxTotalByteSizeLimit;
    private long mCurrentTotalByteSize = 0;

    public TransferMixedFilesStep(final int stepId, final int maxTotalFileNumLimit, final long maxTotalByteSizeLimit) {
        // TODO: add jobId as one of the parameters
        mDataTransferUnits = new ArrayList<DtsDataTransferUnit>();
        mMaxTotalFileNumLimit = maxTotalFileNumLimit;
        mMaxTotalByteSizeLimit = maxTotalByteSizeLimit;
        mStepId = stepId;
    }

    public List<DtsDataTransferUnit> getDataTransferUnits() {
        return mDataTransferUnits;
    }

    public int getStepId() {
        return mStepId;
    }

    public boolean addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit) {
        Assert.isTrue(mDataTransferUnits.size() < mMaxTotalFileNumLimit,
                "The new DataTransferUnit has reached the max total file number limit.");
        Assert.isTrue(dataTransferUnit.getSize() + mCurrentTotalByteSize <= mMaxTotalByteSizeLimit,
                "The new DataTransferUnit has exceeded the max total byte size limit.");
        if (mDataTransferUnits.size() < mMaxTotalFileNumLimit
                && dataTransferUnit.getSize() + mCurrentTotalByteSize <= mMaxTotalByteSizeLimit) {
            mDataTransferUnits.add(dataTransferUnit);
            mCurrentTotalByteSize += dataTransferUnit.getSize();
            return true;
        }
        return false;
    }

    public int getCurrentTotalFileNum() {
        return mDataTransferUnits.size();
    }

    public long getCurrentTotalByteSize() {
        return mCurrentTotalByteSize;
    }

    @Override
    public String toString() {
        final StringBuffer strBuff = new StringBuffer();
        strBuff.append("DtsJobStep " + mStepId + " includes transferring...\n");
        for (final DtsDataTransferUnit dataTransferUnit : mDataTransferUnits) {
            strBuff.append("  * " + dataTransferUnit + "\n");
        }
        return strBuff.toString();
    }

}
