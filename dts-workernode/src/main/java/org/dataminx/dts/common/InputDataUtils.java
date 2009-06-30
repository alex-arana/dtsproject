/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.common;

import java.util.List;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.JobDescriptionType;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.springframework.util.Assert;

/**
 * Collection of convenience methods for dealing with DTS input data structures.
 *
 * @author Alex Arana
 */
public final class InputDataUtils {
    /**
     * Extracts any {@link DataTransferType} elements contained in the input {@link SubmitJobRequest}
     * data structure.
     * <p>
     * It is worth noting that it is <em>not</em> the purpose of this method to "hide" genuine errors
     * that may occur in the process of extracting the underlying data (eg. NPE's etc) but merely to
     * allow application code to provide more meaningful errors in due course...
     *
     * TODO consider removing this method later on as it goes against one of my most sacred development
     *      principles: failing fast
     *
     * @param submitJobRequest Object corresponding to an DTS job request
     * @return A list holding instances of {@link DataTransferType} elements contained in the input
     *         DTS job request.  This method can return <code>null</code> in some circumstances
     */
    public static List<DataTransferType> getDataTransfers(final SubmitJobRequest submitJobRequest) {
        Assert.notNull(submitJobRequest);
        List<DataTransferType> result = null;
        final JobDefinitionType jobDefinition = submitJobRequest.getJobDefinition();
        if (jobDefinition != null) {
            final JobDescriptionType jobDescription = jobDefinition.getJobDescription();
            if (jobDescription != null) {
                result = jobDescription.getDataTransfer();
            }
        }
        return result;
    }
}
