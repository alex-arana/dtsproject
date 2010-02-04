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

    public MaxConnectionKey(final String sourceURI, final String targetURI) {
        mSourceURI = sourceURI;
        mTargetURI = targetURI;
    }

    public String getSourceURI() {
        return mSourceURI;
    }

    public void setSourceURI(final String sourceURI) {
        mSourceURI = sourceURI;
    }

    public String getTargetURI() {
        return mTargetURI;
    }

    public void setTargetURI(final String targetURI) {
        mTargetURI = targetURI;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MaxConnectionKey)) {
            return false;
        }

        final MaxConnectionKey otherMaxConnectionKey = (MaxConnectionKey) obj;
        if ((mSourceURI.equals(otherMaxConnectionKey.getSourceURI()) && mTargetURI.equals(otherMaxConnectionKey
                .getTargetURI()))
                || (mSourceURI.equals(otherMaxConnectionKey.getTargetURI()) && mTargetURI.equals(otherMaxConnectionKey
                        .getSourceURI()))) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return mSourceURI.hashCode() + mTargetURI.hashCode();
    }

}
