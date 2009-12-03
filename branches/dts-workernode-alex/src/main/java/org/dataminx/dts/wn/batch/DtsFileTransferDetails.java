/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.springframework.util.Assert;

/**
 * A data envelope containing details about a single data transfer element within a DTS job request.
 *
 * @author Alex Arana
 */
public class DtsFileTransferDetails implements Serializable {
    /** Universal version identifier for serialisation purposes. */
    private static final long serialVersionUID = 1L;

    /** URI of source file. */
    private final String mSourceUri;

    /** Schema entity that includes additional parameters for this data transfer. */
    private MinxSourceTargetType mSourceParameters;

    /** URI of target file. */
    private final String mTargetUri;

    /** Schema entity that includes additional parameters for this data transfer. */
    private MinxSourceTargetType mTargetParameters;

    /** Holds the total number of bytes to be transferred for this file. */
    private long mTotalBytes;

    /**
     * Holds the total number of bytes that have been transferred so far.
     * NOTE: this field is currently unused.
     */
    private long mBytesTransferred;

    /** Flag that indicates whether the file transfer has already been completed or not. */
    private boolean mCompleted;

    /**
     * Constructs a new instance of {@link DtsFileTransferDetails} using the specified source and target
     * URIs.
     *
     * @param sourceUri Source URI
     * @param targetUri Target URI
     */
    public DtsFileTransferDetails(final String sourceUri, final String targetUri) {
        Assert.notNull(sourceUri, "Source URI must not be null");
        Assert.notNull(targetUri, "Target URI must not be null");
        mSourceUri = sourceUri;
        mTargetUri = targetUri;
    }

    public String getSourceUri() {
        return mSourceUri;
    }

    public String getTargetUri() {
        return mTargetUri;
    }

    public MinxSourceTargetType getSourceParameters() {
        return mSourceParameters;
    }

    public void setSourceParameters(final MinxSourceTargetType schemaSource) {
        mSourceParameters = schemaSource;
    }

    public MinxSourceTargetType getTargetParameters() {
        return mTargetParameters;
    }

    public void setTargetParameters(final MinxSourceTargetType schemaTarget) {
        mTargetParameters = schemaTarget;
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void setCompleted(final boolean transferred) {
        mCompleted = transferred;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    /**
     * @param totalBytes the totalBytes to set
     */
    public void setTotalBytes(final long totalBytes) {
        mTotalBytes = totalBytes;
    }

    public long getBytesTransferred() {
        return mBytesTransferred;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mSourceUri == null) ? 0 : mSourceUri.hashCode());
        result = prime * result + ((mTargetUri == null) ? 0 : mTargetUri.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DtsFileTransferDetails other = (DtsFileTransferDetails) obj;
        if (mSourceUri == null) {
            if (other.mSourceUri != null) {
                return false;
            }
        }
        else if (!mSourceUri.equals(other.mSourceUri)) {
            return false;
        }
        if (mTargetUri == null) {
            if (other.mTargetUri != null) {
                return false;
            }
        }
        else if (!mTargetUri.equals(other.mTargetUri)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("sourceUri", mSourceUri)
            .append("targetUri", mTargetUri)
            .append("bytesTransferred", mBytesTransferred)
            .append("totalBytes", mTotalBytes)
            .append("completed", mCompleted)
            .toString();
    }
}
