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

import javax.jms.Message;
import javax.jms.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for incoming DTS job requests in the configured JMS queue.
 *
 * @author Alex Arana
 */
public class DtsJobListener implements MessageListener {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final Message message) {
        LOG.info("JMS message received: " + message);
    }
}
