package org.dataminx.dts.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * The Data Transfer Service WS Client Implementation.
 *
 * @author Gerson Galang
 */
public class DataTransferServiceClientImpl implements DataTransferServiceClient {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceClientImpl.class);

    /** The Spring-WS web service template. */
    private WebServiceTemplate mWebServiceTemplate;

    /** The XML outputter/printer. */
    private final XMLOutputter mXmlOut;

    private static final Namespace MINX_DTS_MESSAGES_NS =
        Namespace.getNamespace("mjsdl-msg", "http://schemas.dataminx.org/dts/2009/07/messages");

    private static final Namespace MINX_JSDL_NS =
        Namespace.getNamespace("mjsdl", "http://schemas.dataminx.org/dts/2009/07/jsdl");

    private static final Namespace JSDL_NS =
        Namespace.getNamespace("jsdl", "http://schemas.ggf.org/jsdl/2005/11/jsdl");

    private static final Namespace DMI_NS =
        Namespace.getNamespace("dmi", "http://schemas.ogf.org/dmi/2008/05/dmi");

    /**
     * Instantiates a new data transfer service client impl.
     */
    public DataTransferServiceClientImpl() {
        mXmlOut = new XMLOutputter();
    }

    /**
     * {@inheritDoc}
     */
    public String submitJob(org.w3c.dom.Document dtsJob) {
        Element requestElement = new Element("submitJobRequest", MINX_DTS_MESSAGES_NS);
        Document submitJobRequestDoc = new Document(requestElement);

        DOMBuilder domBuilder = new DOMBuilder();
        Element dtsJobElement = domBuilder.build(dtsJob.getDocumentElement());

        Element jobDefEl = new Element("JobDefinition", MINX_DTS_MESSAGES_NS);

        // get the detached (non-live) contents of the DTS JobDefinition document
        // and attach them to the newly created JobDefition element of the
        // submitJobRequest document
        jobDefEl.addContent(dtsJobElement.cloneContent());
        submitJobRequestDoc.getRootElement().addContent(jobDefEl);

        JDOMSource request = new JDOMSource(submitJobRequestDoc);
        JDOMResult response = new JDOMResult();

        // do the actual WS call here...
        mWebServiceTemplate.sendSourceAndReceiveToResult(request, response);

        // and here's the response...
        Document resultDocument = response.getDocument();

        LOGGER.debug(mXmlOut.outputString(resultDocument));

        Element responseElement = resultDocument.getRootElement();
        Element jobResourceKeyElement = responseElement.getChild("JobResourceKey", MINX_JSDL_NS);

        return jobResourceKeyElement.getText();
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(String jobResourceKey) {
        Element requestElement = new Element("cancelJobRequest", MINX_DTS_MESSAGES_NS);
        Document cancelJobRequestDoc = new Document(requestElement);
        Element jobResourceKeyEl = new Element("JobResourceKey", MINX_JSDL_NS);
        jobResourceKeyEl.setText(jobResourceKey);
        cancelJobRequestDoc.getRootElement().addContent(jobResourceKeyEl);

        JDOMSource request = new JDOMSource(cancelJobRequestDoc);

        // do the actual WS call here...
        // we'll set response to null as we know cancelJob will not really
        // return any response
        mWebServiceTemplate.sendSourceAndReceiveToResult(request, null);
    }

    /**
     * {@inheritDoc}
     */
    public void suspendJob(String jobResourceKey) {

        Element requestElement = new Element("suspendJobRequest", MINX_DTS_MESSAGES_NS);
        Document suspendJobRequestDoc = new Document(requestElement);
        Element jobResourceKeyEl = new Element("JobResourceKey", MINX_JSDL_NS);
        jobResourceKeyEl.setText(jobResourceKey);
        suspendJobRequestDoc.getRootElement().addContent(jobResourceKeyEl);

        JDOMSource request = new JDOMSource(suspendJobRequestDoc);

        // do the actual WS call here...
        // we'll set response to null as we know suspendJob will not really
        // return any response
        mWebServiceTemplate.sendSourceAndReceiveToResult(request, null);
    }

    /**
     * {@inheritDoc}
     */
    public void resumeJob(String jobResourceKey) {
        Element requestElement = new Element("resumeJobRequest", MINX_DTS_MESSAGES_NS);
        Document resumeJobRequestDoc = new Document(requestElement);
        Element jobResourceKeyEl = new Element("JobResourceKey", MINX_JSDL_NS);
        jobResourceKeyEl.setText(jobResourceKey);
        resumeJobRequestDoc.getRootElement().addContent(jobResourceKeyEl);

        JDOMSource request = new JDOMSource(resumeJobRequestDoc);

        // do the actual WS call here...
        // we'll set response to null as we know resumeJob will not really
        // return any response
        mWebServiceTemplate.sendSourceAndReceiveToResult(request, null);
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(String jobResourceKey) {
        Element requestElement = new Element("getJobStatusRequest", MINX_DTS_MESSAGES_NS);
        Document cancelJobRequestDoc = new Document(requestElement);
        Element jobResourceKeyEl = new Element("JobResourceKey", MINX_JSDL_NS);
        jobResourceKeyEl.setText(jobResourceKey);
        cancelJobRequestDoc.getRootElement().addContent(jobResourceKeyEl);

        JDOMSource request = new JDOMSource(cancelJobRequestDoc);
        JDOMResult response = new JDOMResult();

        // do the actual WS call here...
        mWebServiceTemplate.sendSourceAndReceiveToResult(request, response);

        // and here's the response...
        Document resultDocument = response.getDocument();

        LOGGER.debug(mXmlOut.outputString(resultDocument));

        Element responseElement = resultDocument.getRootElement();
        Element stateElement = responseElement.getChild("State", DMI_NS);

        return stateElement.getAttributeValue("value");
    }

    /**
     * Sets the web service template.
     *
     * @param webServiceTemplate the new web service template
     */
    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        mWebServiceTemplate = webServiceTemplate;
    }

}
