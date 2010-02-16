package org.dataminx.dts.batch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.Assert;

public class DtsJobStep implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<DtsDataTransferUnit> mDataTransferUnits = null;
    private int mBatchSize = 0;
    private int mStepId = 0;

    public DtsJobStep(final int stepId, final int batchSize) {
        // TODO: add jobId as one of the parameters
        mDataTransferUnits = new ArrayList<DtsDataTransferUnit>(batchSize);
        mBatchSize = batchSize;
        mStepId = stepId;
    }

    public List<DtsDataTransferUnit> getDataTransferUnits() {
        return mDataTransferUnits;
    }

    public int getStepId() {
        return mStepId;
    }

    public void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit) {
        Assert.isTrue(mDataTransferUnits.size() < mBatchSize);
        mDataTransferUnits.add(dataTransferUnit);
    }

    public boolean isFull() {
        return mDataTransferUnits.size() == mBatchSize;
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
