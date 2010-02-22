/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.wn.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
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
public class JobEventQueueSender implements InitializingBean {
    /** Remote {@link Queue} definition. */
    @Autowired
    @Qualifier("jmsJobEventQueue")
    private Queue mQueue;

    /** Spring helper class to provide JMS support. */
    @Autowired
    @Qualifier("jmsTemplate")
    private JmsTemplate mJmsTemplate;

    /** A reference to the DTS JMS message converter. */
    @Autowired
    @Qualifier("dtsMessageConverter")
    private MessageConverter mDtsMessageConverter;

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param jobId DTS Job ID
     * @param message JMS message payload
     */
    public void doSend(final String jobId, final Object message) {
        mJmsTemplate.send(mQueue, new MessageCreator() {
            public Message createMessage(final Session session) throws JMSException {
                final Message jmsMessage = mDtsMessageConverter.toMessage(message, session);
                jmsMessage.setJMSCorrelationID(jobId);
                return jmsMessage;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mQueue, "A remote queue must be configured for the JMS Queue sender.");
        Assert.notNull(mJmsTemplate, "An instance of JmsTemplate must be configured for the JMS Queue sender.");
    }
}
