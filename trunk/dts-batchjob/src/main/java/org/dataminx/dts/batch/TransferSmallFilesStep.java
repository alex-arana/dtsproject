package org.dataminx.dts.batch;

import java.io.Serializable;
import java.util.List;

public class TransferSmallFilesStep implements DtsJobStep, Serializable {

    @Override
    public boolean addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getCurrentTotalByteSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCurrentTotalFileNum() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<DtsDataTransferUnit> getDataTransferUnits() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStepId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getSourceRootFileObjectString() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTargetRootFileObjectString() {
        // TODO Auto-generated method stub
        return null;
    }

}
