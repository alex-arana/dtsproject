package org.dataminx.dts.wn.batch;

import java.io.Serializable;

/**
 * A class used to hold URI information of the source and target.
 * 
 * @author Gerson Galang
 */
public class MaxConnectionKey implements Serializable {
	
	private String mSourceURI;
	private String mTargetURI;
	
	public MaxConnectionKey() {
		mSourceURI = null;
		mTargetURI = null;
	}
	
	public MaxConnectionKey(String sourceURI, String targetURI) {
		mSourceURI = sourceURI;
		mTargetURI = targetURI;
	}
	
	public String getSourceURI() {
    	return mSourceURI;
    }
	
	public void setSourceURI(String sourceURI) {
    	mSourceURI = sourceURI;
    }
	
	public String getTargetURI() {
    	return mTargetURI;
    }
	
	public void setTargetURI(String targetURI) {
    	mTargetURI = targetURI;
    }
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof MaxConnectionKey)) {
			return false;
		}
	
		MaxConnectionKey otherMaxConnectionKey = (MaxConnectionKey)obj;
		if ((mSourceURI.equals(otherMaxConnectionKey.getSourceURI()) 
					&& mTargetURI.equals(otherMaxConnectionKey.getTargetURI())) || 
				(mSourceURI.equals(otherMaxConnectionKey.getTargetURI()) 
					&& mTargetURI.equals(otherMaxConnectionKey.getSourceURI()))) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return mSourceURI.hashCode() + mTargetURI.hashCode();
	}	

}
