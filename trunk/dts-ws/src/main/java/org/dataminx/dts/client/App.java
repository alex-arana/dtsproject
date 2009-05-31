package org.dataminx.dts.client;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main(String[] args) throws Exception {
    	BeanFactory factory =
    	    new XmlBeanFactory(new ClassPathResource("dts-servlet.xml"));
    	DataTransferServiceClient client = (DataTransferServiceClient)factory.getBean("dataTransferServiceClient");
        System.out.println(client.submitJob("DTS Job 01"));
    }
}
