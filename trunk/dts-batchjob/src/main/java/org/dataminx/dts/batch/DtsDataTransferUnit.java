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

    private String mSourceFileURI;
    private String mDestinationFileURI;
    private int mDataTransferIndex;
    private long mSize = 0;

    public DtsDataTransferUnit() {

    }

    public DtsDataTransferUnit(final String sourceFileURI, final String destinationFileURI,
            final int dataTransferIndex, final long size) {
        mSourceFileURI = sourceFileURI;
        mDestinationFileURI = destinationFileURI;
        mDataTransferIndex = dataTransferIndex;
        mSize = size;
    }

    public String getSourceFileURI() {
        return mSourceFileURI;
    }

    public void setSourceFileURI(final String sourceFileURI) {
        mSourceFileURI = sourceFileURI;
    }

    public String getDestinationFileURI() {
        return mDestinationFileURI;
    }

    public void setDestinationFileURI(final String destinationFileURI) {
        mDestinationFileURI = destinationFileURI;
    }

    public int getDataTransferIndex() {
        return mDataTransferIndex;
    }

    public void setDataTransferIndex(final int dataTransferIndex) {
        mDataTransferIndex = dataTransferIndex;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(final long size) {
        mSize = size;
    }

    @Override
    public String toString() {
        return mSourceFileURI + " to " + mDestinationFileURI;
    }

}
