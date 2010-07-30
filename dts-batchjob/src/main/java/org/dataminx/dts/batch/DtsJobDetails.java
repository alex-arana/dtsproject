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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds all the details about the DTS Job that Spring Batch will go through to process the data transfer
 * requests.
 *
 * @author Gerson Galang
 * @author David Meredith (modifications)
 */
public class DtsJobDetails implements Serializable {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(DtsJobDetails.class);

    /** The serial version UID needed to serialize this class. */
    private static final long serialVersionUID = 1L;

    /** The list of DtsJobSteps to be processed by the job.
     TODO: remove this list and use the steps written to file rather than rely on an in-mem collection */
    private List<DtsJobStep> mJobSteps;

    /** The current size of files in bytes that have been transferred at a given time. */
    private int mBytesTransferred;

    /** The total size of files to be transferred by the job. */
    private long mTotalBytes;

    /** The total number of files to be transferred by the job. */
    private int mTotalFiles;

    /** A reference to the JobDefinitionType. */
    private JobDefinitionType mJobDefinition;

    /** The job resource key. */
    private String mJobId;

    /** The job tag. */
    private String mJobTag;

    /** The directory where all this job's job steps are written */
    private String mRootJobDir;

    /** The list of files that have been excluded from the job and won't be transferred. */
    private List<String> mExcludedFiles = new ArrayList<String>();

    /**
     * For every distinct ROOT URI that appears in all the DataTransfer elements
     * in a document (both sources and sinks), the element that defines the MAX
     * number of files to copy is stored against the ROOT URI (the key). 
     *
     * <br/>
     * For example, if 2 source elements are defined which
     * have a common ROOT URI (gridftp://host1.dl.ac.uk), then the element
     * that has the highest file count value will have its
     * file count stored in the map against the common ROOT URI. The common sink
     * ROOT URI (gridftp://host2.dl.ac.uk) will also have this maximum value
     * (illustrated below):
     *
     * <pre>
     * Given the following job document:
     *
     *   <DataTransfer>
     *    <sourceURI> gridftp://host1.dl.ac.uk/some/dir/200files </sourceURI>
     *    <targetURI> gridftp://host2.dl.ac.uk/sink/dir </targetURI>
     *   </DataTransfer>
     *   <DataTransfer>
     *     <sourceURI> gridftp://host1.dl.ac.uk/some/other/dir/400files </sourceURI>
     *     <targetURI> gridftp://host2.dl.ac.uk/sink/dir </targetURI>
     *   </DataTransfer>
     *
     * Results in two map entries:
     * Map entry 1 (<gridftp://host1.dl.ac.uk> , <400>)   [400 files max from DataTransfer element 2]
     * Map entry 2 (<gridftp://host2.dl.ac.uk> , <400>)   [400 files max to sink]
     *
     * Important: The map DOES NOT hold the TOTAL number of files to copied
     * from each source/sink ROOT URI (this is very different - which would result in
     * the following map entires):
     * Erronous Map entry 1 (<gridftp://host1.dl.ac.uk> , <600>)   [200+400 = 600 files max from DataTransfer element 1 + 2]
     * Erronous Map entry 2 (<gridftp://host2.dl.ac.uk> , <600>)   [600 files max to sink]
     *
     * </pre>
     */
    private final Map<String, Integer> mSourceTargetMaxTotalFilesToTransfer = new HashMap<String, Integer>();

    /**
     * The DtsJobDetails constructor.
     */
    public DtsJobDetails() {
        mJobSteps = new ArrayList<DtsJobStep>();
        mExcludedFiles = new ArrayList<String>();
        mJobId = "";
    }

    /**
     * Increment the current size of files that have been tranferred by bytesTransferred.
     *
     * @param bytesTransferred the size of file in bytes
     */
    public synchronized void addBytesTransferred(final int bytesTransferred) {
        mBytesTransferred += bytesTransferred;
    }

    public int getBytesTransferred() {
        return mBytesTransferred;
    }

    public List<String> getExcludedFiles() {
        return mExcludedFiles;
    }

    public JobDefinitionType getJobDefinition() {
        return mJobDefinition;
    }

    public String getJobId() {
        return mJobId;
    }

    public String getJobTag() {
        return mJobTag;
    }

    public List<DtsJobStep> getJobSteps() {
        return mJobSteps;
    }

    public Map<String, Integer> getSourceTargetMaxTotalFilesToTransfer() {
        return mSourceTargetMaxTotalFilesToTransfer;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    public int getTotalFiles() {
        return mTotalFiles;
    }

    public boolean isCompleted() {
        return mBytesTransferred == mTotalBytes;
    }

    /**
     * Saves the List of DtsJobSteps into a number of job step files in the job step folder.
     *
     * @param jobSteps the list of DtsJobSteps to save
     */
    /*public void saveJobSteps(final List<DtsJobStep> jobSteps) {
        LOGGER.debug("DtsJobDetails saveJobSteps()");

        // write the DataTransferUnits held by each of the steps and also add the filename where the DataTransferUnits
        // were written to the JobStep object
        for (final DtsJobStep jobStep : jobSteps) {
            final String filename = System
                .getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY)
                + "/" + mJobTag + "-" + jobStep.getStepId() + ".dts";
            writeJobStepToFile(filename, jobStep);
            jobStep.setJobStepFilename(filename);
        }

        // set this classes list of DtsJobSteps (should we do this because
        // each step holds a List referencing many DTUs ! and this can (potentially) require lots of memory to hold these collections)
        mJobSteps = jobSteps;
    }*/

    public void setJobSteps(final List<DtsJobStep> jobSteps){
        // set this classes list of DtsJobSteps (should we do this because
        // each step holds a List referencing many DTUs ! and this can (potentially) require lots of memory to hold these collections)
        mJobSteps = jobSteps;
    }

    public void setExcludedFiles(final List<String> excludedFiles) {
        mExcludedFiles = excludedFiles;
    }

    public void setJobDefinition(final JobDefinitionType jobDefinition) {
        mJobDefinition = jobDefinition;
    }

    public void setJobId(final String jobId) {
        mJobId = jobId;
    }

    public void setJobTag(final String jobTag) {
        mJobTag = jobTag;
    }

    public void setTotalBytes(final long totalBytes) {
        mTotalBytes = totalBytes;
    }

    public void setTotalFiles(final int totalFiles) {
        mTotalFiles = totalFiles;
    }

    /**
     * Writes the DtsJobStep to file.
     *
     * @param filename the name of the file to write the DtsJobStep to
     * @param jobStep the DtsJobStep to be serialized
     */
    /*private void writeJobStepToFile(final String filename,
        final DtsJobStep jobStep) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filename);
        }
        catch (final FileNotFoundException e) {
            LOGGER.debug("FileNotFoundException was thrown while creating a "
                + "step file to store the DataTransferUnits.");
        }
        for (final DtsDataTransferUnit dataTransferUnit : jobStep
            .getDataTransferUnits()) {
            writer.print(dataTransferUnit.getSourceFileUri() + ";");
            writer.print(dataTransferUnit.getDestinationFileUri() + ";");
            writer.print(dataTransferUnit.getDataTransferIndex() + ";");
            writer.println(dataTransferUnit.getSize() + ";");
        }
        writer.close();
    }*/



    public void setRootJobDir(String rootJobDir){
        this.mRootJobDir = rootJobDir; 
    }

    public String getRootJobDir(){
        return this.mRootJobDir;
    }

}
