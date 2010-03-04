package org.dataminx.dts.batch;

import java.util.List;

interface DtsJobStepAllocator {

    void createNewDataTransfer(final String sourceRootFileObject, final String targetRootFileObject);

    void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit);

    void closeNewDataTransfer();

    List<DtsJobStep> getAllocatedJobSteps();

}
