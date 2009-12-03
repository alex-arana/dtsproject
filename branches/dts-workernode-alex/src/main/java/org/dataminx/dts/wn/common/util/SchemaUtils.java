/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.common.util;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.dataminx.dts.common.xml.XmlUtils.DEFAULT_TRANSFORMER_OUTPUT_PROPERTIES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.common.xml.XmlUtils;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringResult;

/**
 * Collection of convenience methods for dealing with DTS schema data types.
 *
 * @author Alex Arana
 */
public final class SchemaUtils {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(SchemaUtils.class);

    /** URI of the stylesheet used to output DTS schemas for logging or display purposes. */
    private static final String AUDIT_OUTPUT_STYLESHEET =
        "/org/dataminx/dts/common/xml/identity-copy-no-credentials.xslt";

    /**
     * Cached <code>Templates</code> object used to create instances of <code>Transformer</code> able
     * to convert DTS schema entities into a format suitable for audit logging.
     */
    private static final Templates AUDIT_LOGGING_TEMPLATE =
        XmlUtils.newTemplates(new ClassPathResource(AUDIT_OUTPUT_STYLESHEET));

    /**
     * Returns a serialised form of the input schema entity that can be used for logging purposes by removing
     * all security credentials contained within it.  If the input object is not a valid DTS schema entity
     * this method will return an empty <code>String</code>.
     * <p>
     * <em>NOTE</em>: This method will not remove authentication credentials embedded within a URL
     * specification. ie. <code>ftp:username:passwd@ftp.internet.com</code>.
     *
     * TODO optimise this (XMLBeans XQuery/Update or straight cursor navigation?)
     *
     * @param schemaObject An instance of the DTS schema
     * @return A <code>String</code> suitable for logging or display purposes
     */
    public static String getAuditableString(final Object schemaObject) {
        if (schemaObject instanceof XmlObject) {
            final XmlObject xmlObject = (XmlObject) schemaObject;
            try {
                final StringResult result = new StringResult();
                final Transformer transformer = AUDIT_LOGGING_TEMPLATE.newTransformer();
                // do we want to format the logging output?
                for (final Map.Entry<String, String> entry : DEFAULT_TRANSFORMER_OUTPUT_PROPERTIES.entrySet()) {
                    transformer.setOutputProperty(entry.getKey(), entry.getValue());
                }
                transformer.transform(new DOMSource(xmlObject.getDomNode()), result);
                return result.toString();
            }
            catch (final TransformerException ex) {
                LOG.warn("An XML transformation error occurred while processing DTS Schema entity: " + xmlObject, ex);
            }
        }
        return EMPTY;
    }

    /**
     * Extracts any {@link DataTransferType} elements contained in the input {@link SubmitJobRequest}
     * data structure.
     * <p>
     * It is worth noting that it is <em>not</em> the purpose of this method to "hide" genuine errors
     * that may occur in the process of extracting the underlying data (eg. NPE's etc) but merely to
     * allow application code to provide more meaningful errors in due course...
     *
     * @param submitJobRequest Object corresponding to an DTS job request
     * @return A list holding instances of {@link DataTransferType} elements contained in the input
     *         DTS job request.  This method will return an empty list when there are no data staging
     *         elements in the input request
     */
    public static List<DataTransferType> getDataTransfers(final SubmitJobRequest submitJobRequest) {
        Assert.notNull(submitJobRequest);
        final List<DataTransferType> result = new ArrayList<DataTransferType>();
        final JobDefinitionType jobDefinition = submitJobRequest.getJobDefinition();
        if (jobDefinition != null) {
            final JobDescriptionType jobDescription = jobDefinition.getJobDescription();
            if (jobDescription instanceof MinxJobDescriptionType) {
                final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
                CollectionUtils.addAll(result, minxJobDescription.getDataTransferArray());
            }
        }
        return result;
    }

    /**
     * Extracts any {@link DataStagingType} elements contained in the input {@link SubmitJobRequest}
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
     * @return A list holding instances of {@link DataStagingType} elements contained in the input
     *         DTS job request.  This method will return an empty list when there are no data staging
     *         elements in the input request
     */
    public static List<DataStagingType> getDataStagingList(final SubmitJobRequest submitJobRequest) {
        Assert.notNull(submitJobRequest);
        final List<DataStagingType> result = new ArrayList<DataStagingType>();
        final JobDefinitionType jobDefinition = submitJobRequest.getJobDefinition();
        if (jobDefinition != null) {
            final JobDescriptionType jobDescription = jobDefinition.getJobDescription();
            if (jobDescription != null) {
                CollectionUtils.addAll(result, jobDescription.getDataStagingArray());
            }
        }
        return result;
    }

    /**
     * Extracts the description pertaining to the specified {@link SubmitJobRequest} schema entity or an
     * empty <code>String</code> if one was not present as it is an optional attribute.
     *
     * @param submitJobRequest A DTS schema entity corresponding to a submit job request
     * @return A description corresponding to a DTS submit job request (or blank)
     */
    public static String extractJobDescription(final SubmitJobRequest submitJobRequest) {
        Assert.notNull(submitJobRequest);
        final JobIdentificationType jobIdentification =
            submitJobRequest.getJobDefinition().getJobDescription().getJobIdentification();
        return jobIdentification == null ? EMPTY : jobIdentification.getDescription();
    }
}
