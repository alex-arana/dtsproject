package org.dataminx.dts.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts._2009._05.dts.*;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;


public class DataTransferServiceClientImpl implements DataTransferServiceClient {

	private WebServiceTemplate webServiceTemplate;
	protected final Log logger = LogFactory.getLog(getClass());
	private XMLOutputter xmlOut;

	public DataTransferServiceClientImpl() {
		xmlOut = new XMLOutputter();
	}

	public String submitJob(org.w3c.dom.Document dtsJob) throws IOException {
		Namespace minxNamespace = Namespace.getNamespace("minx", "http://schemas.dataminx.org/dts/2009/05/dts");
		Element requestElement =
		    new Element("submitJobRequest", minxNamespace);
		Document submitJobRequestDoc = new Document(requestElement);

		DOMBuilder domBuilder = new DOMBuilder();
		Element dtsJobElement = domBuilder.build(dtsJob.getDocumentElement());

		Element jobDefEl = new Element("JobDefinition", minxNamespace);

		// get the detached (non-live) contents of the DTS JobDefinition document
		// and attach them to the newly created JobDefition element of the
		// submitJobRequest document
		jobDefEl.addContent(dtsJobElement.cloneContent());
		submitJobRequestDoc.getRootElement().addContent(jobDefEl);

		JDOMSource request = new JDOMSource(submitJobRequestDoc);
		JDOMResult response = new JDOMResult();

		// do the actual WS call here...
		webServiceTemplate.sendSourceAndReceiveToResult(request, response);

		// and here's the response...
		Document resultDocument = response.getDocument();

		logger.debug(xmlOut.outputString(resultDocument));

		Element responseElement = resultDocument.getRootElement();
		Element jobIdElement = responseElement.getChild("JobId", minxNamespace);

		return jobIdElement.getText();
	}

	public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
		this.webServiceTemplate = webServiceTemplate;
	}


}
