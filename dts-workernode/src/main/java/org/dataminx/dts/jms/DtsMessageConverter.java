/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.jms;

import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import org.dataminx.dts.batch.DtsJob;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

/**
 * Converts an incoming JMS message into a DTS Job definition.
 *
 * @author Alex Arana
 */
@Component("dtsMessageConverter")
public class DtsMessageConverter implements MessageConverter, BeanFactoryAware {
    /** A reference to the Spring bean factory. */
    private BeanFactory mBeanFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        final Object[] jobId = new Object[] {message.getJMSMessageID()};
        final DtsJob dtsJob = (DtsJob) mBeanFactory.getBean("dtsFileTransferJob", jobId);

        final Properties properties = new Properties();
        return new JobLaunchRequest(dtsJob, new DefaultJobParametersConverter().getJobParameters(properties));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        mBeanFactory = beanFactory;
    }
}
