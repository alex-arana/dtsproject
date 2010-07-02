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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * The DTSJobStep is an object representation of the DTS Job Step.
 * The state of a DtsJobStep can be written/read from file by passing an absolute
 * path to the {@link filePath} in the constructor.
 *
 * @author Gerson Galang
 * @author David Meredith
 */
public class DtsJobStep implements Serializable {

    /** The serial version UID needed to serialize this class. */
    private static final long serialVersionUID = 1L;

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(DtsJobStep.class);

    /** A reference to the list of DtsDataTransferUnits. 
     * Note that this is transient so it will not be written to the batch context*/
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

    /** The filename of the job step file which saves the DTUs. */
    private String mFilePath;

    /** The DtsJobStep type. */
    private final Type mStepType;

    /** The enumeration of DtsJobStep types. */
    public enum Type {
        /** Mixture of big and small files. */
        MIXED_FILES,

        /** Big files grouped into a step. */
        BIG_FILES,

        /** Small files grouped into a step. */
        SMALL_FILES
    }

    /**
     * The DtsJobStep constructor.
     *
     * @param sourceRootFileObject the Root of the FileObject referred to by the Source
     * @param targetRootFileObject the Root of the FileObject referred to by the Target
     * @param stepId the job step ID
     * @param maxTotalFileNumLimit the maximum total file number limit that this job step can handle
     * @param maxTotalByteSizeLimit the maximum total size of all the files (in bytes) that this job step can handle
     * @param dtsJobStepType the DtsJobStep type
     * @param filePath the absolute path where this jobStep's information is persisted.
     */
    public DtsJobStep(final String sourceRootFileObject,
        final String targetRootFileObject, final int stepId,
        final int maxTotalFileNumLimit, final long maxTotalByteSizeLimit,
        final Type dtsJobStepType, final String filePath) { 
        mDataTransferUnits = new ArrayList<DtsDataTransferUnit>();
        mMaxTotalFileNumLimit = maxTotalFileNumLimit;
        mMaxTotalByteSizeLimit = maxTotalByteSizeLimit;
        mStepId = stepId;
        mSourceRootFileObject = sourceRootFileObject;
        mTargetRootFileObject = targetRootFileObject;
        mStepType = dtsJobStepType;
        mFilePath = filePath;
    }


    /**
     * TODO if we write all the job step state information to file, provide a
     * static factory method to return a new DtsJobStep instance from the given
     * job step file.
     */
    //public static DtsJobStep loadDtsJobStep(String filePath){
    //}

    /**
     * Add a DtsDataTransferUnit to the list of DtsDataTransferUnits for this step.
     *
     * @param dataTransferUnit the DtsDataTransferUnit to be added to the list of DtsDataTransferUnits for this step
     * @return true if the dataTransferUnit has been successfully added to the list, false otherwise
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
        if (mDataTransferUnits.size() < mMaxTotalFileNumLimit
            && dataTransferUnit.getSize() + mCurrentTotalByteSize <= mMaxTotalByteSizeLimit) {
            mDataTransferUnits.add(dataTransferUnit);
            mCurrentTotalByteSize += dataTransferUnit.getSize();
            return true;
        }
        return false;
    }

    /**
     * Returns the current total size of files (in bytes) to be transferred by this job step.
     *
     * @return the current total size of files (in bytes) to be transferred by this job step
     */
    public long getCurrentTotalByteSize() {
        return mCurrentTotalByteSize;
    }

    /**
     * Returns the current total number of files to be transferred by this job step.
     *
     * @return the current total number of files to be transferred by this job step
     */
    public int getCurrentTotalFileNum() {
        return mDataTransferUnits.size();
    }

    /**
     * Returns the list of DtsDataTransferUnits for this job step.
     *
     * @return the list of DtsDataTransferUnits for this job step
     * @throws IOException if the data transfer units cannot be read from file. 
     */
    public List<DtsDataTransferUnit> getDataTransferUnits() throws IOException {
        // return the in-mem DTUs first
        if (mDataTransferUnits != null && !mDataTransferUnits.isEmpty()) {
            return mDataTransferUnits;
        } 
        else if (mFilePath != null) {
            // else load the DTUs from the stepFile properties.
            //try {
                return loadDataTransferUnitsFromFile(mFilePath);
                // no, lets not re-init the in-mem collection
                //dataTransferUnitToReturn = mDataTransferUnits;
            //} catch (final IOException e) {
            //    LOGGER.error("Exception occurred while loading the DataTransferUnits from the step file.",e);
            //    return new ArrayList<DtsDataTransferUnit>();
            //}
        } else {
            LOGGER.warn("The step file has not been set yet.");
            return new ArrayList<DtsDataTransferUnit>();
        }
    }

    /**
     * Gets the name of the job step file.
     *
     * @return filename the name of the job step file
     */
    public String getJobStepFilePath() {
        return mFilePath;
    }

    /**
     * Returns the Root of the FileObject referred to by the Source of this job step.
     *
     * @return the Root of the FileObject referred to by the Source of this job step
     */
    public String getSourceRootFileObjectString() {
        return mSourceRootFileObject;
    }

    /**
     * Returns the job step ID.
     *
     * @return the job step ID
     */
    public int getStepId() {
        return mStepId;
    }

    /**
     * Returns the Root of the FileObject referred to by the Target of this job step.
     *
     * @return the Root of the FileObject referred to by the Target of this job step
     */
    public String getTargetRootFileObjectString() {
        return mTargetRootFileObject;
    }


    public Type getDtsJobStepType() {
        return mStepType;
    }


    /**
     * Write this jobStep to file at the filePath specifed in the constructor
     * 
     * @throws FileNotFoundException
     */
    public void writeToFile() throws FileNotFoundException {
        // Probabbly better to write the other step data to file also
        PrintWriter writer = null;
        try {
            File newStepFile = new File(this.mFilePath);
            writer = new PrintWriter(newStepFile);
            /*
            writer.println("DtsJobStep");
            writer.println(this.mStepId);
            writer.println(this.mStepType);
            writer.println(this.mCurrentTotalByteSize);
            writer.println(this.mMaxTotalByteSizeLimit);
            writer.println(this.mMaxTotalFileNumLimit);
            writer.println(this.mSourceRootFileObject);
            writer.println(this.mTargetRootFileObject); 
             */
            for (final DtsDataTransferUnit dataTransferUnit : this.mDataTransferUnits) {
                writer.print(dataTransferUnit.getSourceFileUri() + ";");
                writer.print(dataTransferUnit.getDestinationFileUri() + ";");
                writer.print(dataTransferUnit.getDataTransferIndex() + ";");
                writer.println(dataTransferUnit.getSize() + ";");
            }
        } finally {
            writer.close();
        }
    }

    /**
     * Clears this step's in-memory cache of {@link DtsDataTransferUnit} objects.
     * This does not clear the dataTransferUnits that have been written to file.
     */
    public void clearDataTransferUnits(){
        mDataTransferUnits = new ArrayList<DtsDataTransferUnit>(0);
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
        LOGGER.debug("TransferMixedFilesStep loadDataTransferUnitsFromFile(\""+ filename + "\")");
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

    @Override
    public String toString() {
        final StringBuffer strBuff = new StringBuffer();
        strBuff.append("DtsJobStep of type " + mStepType + " and id " + mStepId
            + " includes transferring...\n");
        //for (final DtsDataTransferUnit dataTransferUnit : getDataTransferUnits()) {
        //    strBuff.append("  * " + dataTransferUnit + "\n");
        //}
        return strBuff.toString();
    }
}
