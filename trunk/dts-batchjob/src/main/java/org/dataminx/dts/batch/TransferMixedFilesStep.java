package org.dataminx.dts.batch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class TransferMixedFilesStep implements DtsJobStep, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransferMixedFilesStep.class);

    private transient List<DtsDataTransferUnit> mDataTransferUnits = null;
    private int mStepId = 0;
    private final int mMaxTotalFileNumLimit;
    private final long mMaxTotalByteSizeLimit;
    private long mCurrentTotalByteSize = 0;
    private final String mSourceRootFileObject;
    private final String mTargetRootFileObject;
    private String mFilename;

    public TransferMixedFilesStep(final String sourceRootFileObject, final String targetRootFileObject,
            final int stepId, final int maxTotalFileNumLimit, final long maxTotalByteSizeLimit) {
        // TODO: add jobId as one of the parameters
        mDataTransferUnits = new ArrayList<DtsDataTransferUnit>();
        mMaxTotalFileNumLimit = maxTotalFileNumLimit;
        mMaxTotalByteSizeLimit = maxTotalByteSizeLimit;
        mStepId = stepId;
        mSourceRootFileObject = sourceRootFileObject;
        mTargetRootFileObject = targetRootFileObject;
    }

    public List<DtsDataTransferUnit> getDataTransferUnits() {
        if (mDataTransferUnits != null && !mDataTransferUnits.isEmpty()) {
            return mDataTransferUnits;
        }
        else if (mFilename != null) {
            try {
                mDataTransferUnits = loadDataTransferUnitsFromFile(mFilename);
                return mDataTransferUnits;
            } catch (final IOException e) {
                LOGGER.error("Exception occurred while loading the DataTransferUnits from the step file.", e);
                return new ArrayList<DtsDataTransferUnit>();
            }
        }
        else {
            LOGGER.error("The step file has not been set yet.");
            return new ArrayList<DtsDataTransferUnit>();
        }
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
        for (final DtsDataTransferUnit dataTransferUnit : getDataTransferUnits()) {
            strBuff.append("  * " + dataTransferUnit + "\n");
        }
        return strBuff.toString();
    }

    public String getSourceRootFileObjectString() {
        return mSourceRootFileObject;
    }

    public String getTargetRootFileObjectString() {
        return mTargetRootFileObject;
    }

    public String getJobStepFilename() {
        return mFilename;
    }

    public void setJobStepFilename(final String filename) {
        mFilename = filename;
    }

    private List<DtsDataTransferUnit> loadDataTransferUnitsFromFile(final String filename) throws IOException {
        LOGGER.debug("TransferMixedFilesStep loadDataTransferUnitsFromFile(\"" + filename + "\")");
        final List<DtsDataTransferUnit> dataTransferUnits = new ArrayList<DtsDataTransferUnit>();
        final BufferedReader reader = new BufferedReader(new FileReader(filename));
        String lineRead = reader.readLine();
        while (lineRead != null) {
            dataTransferUnits.add(parseDataTransferUnitLine(lineRead));
            lineRead = reader.readLine();
        }
        reader.close();
        return dataTransferUnits;
    }

    private DtsDataTransferUnit parseDataTransferUnitLine(final String dataTransferUnitLine) {
        final StringTokenizer sTok = new StringTokenizer(dataTransferUnitLine, ";");
        final DtsDataTransferUnit dataTransferUnit = new DtsDataTransferUnit(sTok.nextToken(), sTok.nextToken(),
                Integer.parseInt(sTok.nextToken()), Long.parseLong(sTok.nextToken()));
        return dataTransferUnit;
    }

}
