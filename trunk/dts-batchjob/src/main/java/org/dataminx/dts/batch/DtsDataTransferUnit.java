package org.dataminx.dts.batch;

import java.io.Serializable;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;

/**
 * This class represents the smallest unit of the DataTransferType which basically is equivalent to a single file 
 * transfer.
 * 
 * @author Gerson Galang
 */
public class DtsDataTransferUnit implements Serializable {

    private static final long serialVersionUID = 1L;
    
	private String mSourceFileURI;
	private String mDestinationFileURI;
	private DataTransferType mDataTransfer;
	
	public DtsDataTransferUnit() {
		
	}
	
	public DtsDataTransferUnit(String sourceFileURI, String destinationFileURI, DataTransferType dataTransfer) {
		mSourceFileURI = sourceFileURI;
		mDestinationFileURI = destinationFileURI;
		mDataTransfer = dataTransfer;
	}

	public String getSourceFileURI() {
    	return mSourceFileURI;
    }

	public void setSourceFileURI(String sourceFileURI) {
    	mSourceFileURI = sourceFileURI;
    }

	public String getDestinationFileURI() {
    	return mDestinationFileURI;
    }

	public void setDestinationFileURI(String destinationFileURI) {
    	mDestinationFileURI = destinationFileURI;
    }

	public DataTransferType getDataTransfer() {
    	return mDataTransfer;
    }

	public void setDataTransfer(DataTransferType dataTransfer) {
    	mDataTransfer = dataTransfer;
    }
	
	public String toString() {
		return mSourceFileURI + " to " + mDestinationFileURI;
	}
	
}
