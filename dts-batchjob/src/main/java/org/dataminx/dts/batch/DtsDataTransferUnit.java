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

import java.io.Serializable;

/**
 * This class represents the smallest unit of the DataTransferType which basically is equivalent to a single file
 * transfer.
 *
 * @author Gerson Galang
 */
public class DtsDataTransferUnit implements Serializable {

    /** The serial version UID needed to serialize this class. */
    private static final long serialVersionUID = 1L;

    /** The source URI. */
    private String mSrcUri;

    /** The target URI. */
    private String mDestUri;

    /** The DataTransfer element index. */
    private int mIndex;

    /** The size of the "source" file to be transferred. */
    private long mSize;

    /**
     * The default DtsDataTransferUnit constructor.
     */
    public DtsDataTransferUnit() {

    }

    /**
     * The DtsDataTransferUnit constructor.
     *
     * @param sourceFileUri the source URI
     * @param destinationFileUri the target URI
     * @param dataTransferIndex the index of the DataTransfer element within the JobDescription document
     * @param size the size of the source (file) to be transferred
     */
    public DtsDataTransferUnit(final String sourceFileUri,
        final String destinationFileUri, final int dataTransferIndex,
        final long size) {
        mSrcUri = sourceFileUri;
        mDestUri = destinationFileUri;
        mIndex = dataTransferIndex;
        mSize = size;
    }

    public int getDataTransferIndex() {
        return mIndex;
    }

    public String getDestinationFileUri() {
        return mDestUri;
    }

    public long getSize() {
        return mSize;
    }

    public String getSourceFileUri() {
        return mSrcUri;
    }

    public void setDataTransferIndex(final int dataTransferIndex) {
        mIndex = dataTransferIndex;
    }

    public void setDestinationFileUri(final String destinationFileUri) {
        mDestUri = destinationFileUri;
    }

    public void setSize(final long size) {
        mSize = size;
    }

    public void setSourceFileUri(final String sourceFileUri) {
        mSrcUri = sourceFileUri;
    }

    @Override
    public String toString() {
        return mSrcUri + " to " + mDestUri;
    }

}
