package org.dataminx.dts.batch;

import java.util.List;

interface DtsJobStepAllocator {

    void createNewDataTransfer();

    void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit);

    void closeNewDataTransfer();

    List<DtsJobStep> getAllocatedJobSteps();

}
