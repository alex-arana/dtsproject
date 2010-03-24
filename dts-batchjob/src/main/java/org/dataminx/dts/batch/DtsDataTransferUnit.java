package org.dataminx.dts.batch;

import java.io.Serializable;

/**
 * This class represents the smallest unit of the DataTransferType which
 * basically is equivalent to a single file transfer.
 * 
 * @author Gerson Galang
 */
public class DtsDataTransferUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mSrcURI;
    private String mDestURI;

    /** The DataTransfer element index. */
    private int mIndex;
    private long mSize = 0;

    public DtsDataTransferUnit() {

    }

    public DtsDataTransferUnit(final String sourceFileURI, final String destinationFileURI,
            final int dataTransferIndex, final long size) {
        mSrcURI = sourceFileURI;
        mDestURI = destinationFileURI;
        mIndex = dataTransferIndex;
        mSize = size;
    }

    public String getSourceFileURI() {
        return mSrcURI;
    }

    public void setSourceFileURI(final String sourceFileURI) {
        mSrcURI = sourceFileURI;
    }

    public String getDestinationFileURI() {
        return mDestURI;
    }

    public void setDestinationFileURI(final String destinationFileURI) {
        mDestURI = destinationFileURI;
    }

    public int getDataTransferIndex() {
        return mIndex;
    }

    public void setDataTransferIndex(final int dataTransferIndex) {
        mIndex = dataTransferIndex;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(final long size) {
        mSize = size;
    }

    @Override
    public String toString() {
        return mSrcURI + " to " + mDestURI;
    }

}
