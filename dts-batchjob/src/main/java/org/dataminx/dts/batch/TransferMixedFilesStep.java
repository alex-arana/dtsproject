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

/**
 * The TransferMixedFilesStep is a job step that will handle a mixture of big and small files.
 *
 * @author Gerson Galang
 */
public class TransferMixedFilesStep implements DtsJobStep, Serializable {

    /** The serial version UID needed to serialize this class. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(TransferMixedFilesStep.class);

    /** A reference to the list of DtsDataTransferUnits. */
    private transient List<DtsDataTransferUnit> mDataTransferUnits;

    /** The job step ID. */
    private final int mStepId;

    /** The maximum total file number limit that this job step can handle. */
    private final int mMaxTotalFileNumLimit;

    /** The maximum total size of all the files in bytes that this job step can handle. */
    private final long mMaxTotalByteSizeLimit;

    /** The current size of all the files in bytes for this job step. */
    private long mCurrentTotalByteSize;

    /** The Root of the FileObject referred to by the Source. */
    private final String mSourceRootFileObject;

    /** The Root of the FileObject referred to by the Target. */
    private final String mTargetRootFileObject;

    /** The filename of the job step file. */
    private String mFilename;

    /**
     * The TransferMixedFileStep constructor.
     *
     * @param sourceRootFileObject the Root of the FileObject referred to by the Source
     * @param targetRootFileObject the Root of the FileObject referred to by the Target
     * @param stepId the job step ID
     * @param maxTotalFileNumLimit the maximum total file number limit that this job step can handle
     * @param maxTotalByteSizeLimit the maximum total size of all the files (in bytes) that this job step can handle
     */
    public TransferMixedFilesStep(final String sourceRootFileObject,
        final String targetRootFileObject, final int stepId,
        final int maxTotalFileNumLimit, final long maxTotalByteSizeLimit) {
        // TODO: add jobId as one of the parameters
        mDataTransferUnits = new ArrayList<DtsDataTransferUnit>();
        mMaxTotalFileNumLimit = maxTotalFileNumLimit;
        mMaxTotalByteSizeLimit = maxTotalByteSizeLimit;
        mStepId = stepId;
        mSourceRootFileObject = sourceRootFileObject;
        mTargetRootFileObject = targetRootFileObject;
    }

    /**
     * {@inheritDoc}
     */
    public boolean addDataTransferUnit(
        final DtsDataTransferUnit dataTransferUnit) {
        Assert
            .isTrue(mDataTransferUnits.size() < mMaxTotalFileNumLimit,
                "The new DataTransferUnit has reached the max total file number limit.");
        Assert
            .isTrue(
                dataTransferUnit.getSize() + mCurrentTotalByteSize <= mMaxTotalByteSizeLimit,
                "The new DataTransferUnit has exceeded the max total byte size limit.");
        if ((mDataTransferUnits.size() < mMaxTotalFileNumLimit)
            && (dataTransferUnit.getSize() + mCurrentTotalByteSize <= mMaxTotalByteSizeLimit)) {
            mDataTransferUnits.add(dataTransferUnit);
            mCurrentTotalByteSize += dataTransferUnit.getSize();
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public long getCurrentTotalByteSize() {
        return mCurrentTotalByteSize;
    }

    /**
     * {@inheritDoc}
     */
    public int getCurrentTotalFileNum() {
        return mDataTransferUnits.size();
    }

    /**
     * {@inheritDoc}
     */
    public List<DtsDataTransferUnit> getDataTransferUnits() {
        List<DtsDataTransferUnit> dataTransferUnitToReturn;
        if ((mDataTransferUnits != null) && !mDataTransferUnits.isEmpty()) {
            dataTransferUnitToReturn = mDataTransferUnits;
        }
        else if (mFilename != null) {
            try {
                mDataTransferUnits = loadDataTransferUnitsFromFile(mFilename);
                dataTransferUnitToReturn = mDataTransferUnits;
            }
            catch (final IOException e) {
                LOGGER
                    .error(
                        "Exception occurred while loading the DataTransferUnits from the step file.",
                        e);
                dataTransferUnitToReturn = new ArrayList<DtsDataTransferUnit>();
            }
        }
        else {
            LOGGER.error("The step file has not been set yet.");
            dataTransferUnitToReturn = new ArrayList<DtsDataTransferUnit>();
        }

        return dataTransferUnitToReturn;
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStepFilename() {
        return mFilename;
    }

    /**
     * {@inheritDoc}
     */
    public String getSourceRootFileObjectString() {
        return mSourceRootFileObject;
    }

    /**
     * {@inheritDoc}
     */
    public int getStepId() {
        return mStepId;
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetRootFileObjectString() {
        return mTargetRootFileObject;
    }

    /**
     * Loads the list of DataTransferUnits from the job step file.
     *
     * @param filename the name of the job step file
     * @return the list of DataTransferUnits from the job step file
     * @throws IOException if IOException is thrown while accessing the job step file
     */
    private List<DtsDataTransferUnit> loadDataTransferUnitsFromFile(
        final String filename) throws IOException {
        LOGGER.debug("TransferMixedFilesStep loadDataTransferUnitsFromFile(\""
            + filename + "\")");
        final List<DtsDataTransferUnit> dataTransferUnits = new ArrayList<DtsDataTransferUnit>();
        final BufferedReader reader = new BufferedReader(new FileReader(
            filename));
        String lineRead = reader.readLine();
        while (lineRead != null) {
            dataTransferUnits.add(parseDataTransferUnitLine(lineRead));
            lineRead = reader.readLine();
        }
        reader.close();
        return dataTransferUnits;
    }

    /**
     * Parse the DataTransferUnit line.
     *
     * @param dataTransferUnitLine a line in the job step file
     * @return the DtsTransferUnit equivalent of the given line in the job step file
     */
    private DtsDataTransferUnit parseDataTransferUnitLine(
        final String dataTransferUnitLine) {
        final StringTokenizer sTok = new StringTokenizer(dataTransferUnitLine,
            ";");
        final DtsDataTransferUnit dataTransferUnit = new DtsDataTransferUnit(
            sTok.nextToken(), sTok.nextToken(), Integer.parseInt(sTok
                .nextToken()), Long.parseLong(sTok.nextToken()));
        return dataTransferUnit;
    }

    public void setJobStepFilename(final String filename) {
        mFilename = filename;
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

}
