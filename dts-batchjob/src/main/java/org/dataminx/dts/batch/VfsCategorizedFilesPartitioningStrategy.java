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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.util.Assert;

public class VfsCategorizedFilesPartitioningStrategy implements
    JobPartitioningStrategy {

    private long mTotalSize = 0;
    private int mTotalFiles = 0;

    private long mMaxTotalByteSizePerStepLimit = 0;

    private int mMaxTotalFileNumPerStepLimit = 0;

    private ArrayList<String> mExcluded = new ArrayList<String>();

    private DtsVfsUtil mDtsVfsUtil;

    private static final Log LOGGER = LogFactory
        .getLog(VfsCategorizedFilesPartitioningStrategy.class);

    private DtsJobStepAllocator mDtsJobStepAllocator;

    public DtsJobDetails partitionTheJob(final JobDefinitionType jobDefinition,
        final String jobResourceKey) throws JobScopingException {
        Assert.hasText(jobResourceKey,
            "JobResourceKey should not be null or empty.");
        Assert.notNull(jobDefinition, "JobDefinitionType should not be null.");
        if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new DtsException(
                "MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new DtsException(
                "MaxTotalFileNumPerStepLimit should be a positive number.");
        }

        FileSystemManager fileSystemManager = null;
        try {
            fileSystemManager = mDtsVfsUtil.createNewFsManager();
        }
        catch (final FileSystemException e) {
            throw new JobScopingException(
                "FileSystemException was thrown while creating new FileSystemManager in the job scoping task.",
                e);
        }

        final DtsJobDetails jobDetails = new DtsJobDetails();
        jobDetails.setJobDefinition(jobDefinition);
        jobDetails.setJobId(jobResourceKey);

        mDtsJobStepAllocator = new CategorizedFilesJobStepAllocator();
        mExcluded = new ArrayList<String>();
        mTotalSize = 0;
        mTotalFiles = 0;

        final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

        final JobDescriptionType jobDescription = jobDefinition
            .getJobDescription();
        if (jobDescription instanceof MinxJobDescriptionType) {
            final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
            CollectionUtils.addAll(dataTransfers, minxJobDescription
                .getDataTransferArray());
        }
        if (CollectionUtils.isEmpty(dataTransfers)) {
            LOGGER
                .warn("DTS job request is incomplete as it does not contain any data transfer elements.");
            throw new DtsJobExecutionException(
                "DTS job request contains no data transfer elements.");
        }

        return null;
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setMaxTotalByteSizePerStepLimit(
        final long maxTotalByteSizePerStepLimit) {
        mMaxTotalByteSizePerStepLimit = maxTotalByteSizePerStepLimit;
    }

    public void setMaxTotalFileNumPerStepLimit(
        final int maxTotalFileNumPerStepLimit) {
        mMaxTotalFileNumPerStepLimit = maxTotalFileNumPerStepLimit;
    }

    private class CategorizedFilesJobStepAllocator implements
        DtsJobStepAllocator {

        public void addDataTransferUnit(
            final DtsDataTransferUnit dataTransferUnit) {
            // TODO Auto-generated method stub

        }

        public void closeNewDataTransfer() {
            // TODO Auto-generated method stub

        }

        public void createNewDataTransfer(final String sourceRootFileObject,
            final String targetRootFileObject) {
            // TODO Auto-generated method stub

        }

        public List<DtsJobStep> getAllocatedJobSteps() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
