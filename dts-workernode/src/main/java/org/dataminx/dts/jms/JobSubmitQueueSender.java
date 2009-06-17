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

import javax.jms.Queue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Sends a {@link javax.jms.Message} to a configured JMS {@link Queue}.
 * <p>
 * A {@link org.springframework.jms.support.converter.MessageConverter} would need to be registered to automatically
 * convert to the desired type.
 *
 * @author Alex Arana
 */
@Component
public class JobSubmitQueueSender implements InitializingBean {
    /** Remote {@link Queue} definition. */
    @Autowired
    @Qualifier("jmsJobSubmitQueue")
    private Queue mQueue;

    /** Spring helper class to provide JMS support. */
    @Autowired
    private JmsTemplate mJmsTemplate;

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param msg Message to send
     */
    public void doSend(final Object msg) {
        mJmsTemplate.convertAndSend(mQueue, msg);
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mQueue, "A remote queue must be configured for the JMS Queue sender.");
        Assert.notNull(mJmsTemplate, "An instance of JmsTemplate must be configured for the JMS Queue sender.");
    }
}
