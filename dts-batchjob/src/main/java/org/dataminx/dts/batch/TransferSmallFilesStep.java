package org.dataminx.dts.batch;

import java.io.Serializable;
import java.util.List;

public class TransferSmallFilesStep implements DtsJobStep, Serializable {

    public boolean addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit) {
        // TODO Auto-generated method stub
        return false;
    }

    public long getCurrentTotalByteSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getCurrentTotalFileNum() {
        // TODO Auto-generated method stub
        return 0;
    }

    public List<DtsDataTransferUnit> getDataTransferUnits() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getStepId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getSourceRootFileObjectString() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTargetRootFileObjectString() {
        // TODO Auto-generated method stub
        return null;
    }

}
