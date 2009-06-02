package org.dataminx.dts.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Hello world!
 *
 */
public class App
{
	protected final Log logger = LogFactory.getLog(getClass());

    public static void main(String[] args) throws Exception {
    	BeanFactory factory =
    	    new XmlBeanFactory(new ClassPathResource("dts-servlet.xml"));
    	DataTransferServiceClient client = (DataTransferServiceClient)factory.getBean("dataTransferServiceClient");

    	App app = new App();
        app.logger.info(client.submitJob("DTS Job 01"));
    }
}
