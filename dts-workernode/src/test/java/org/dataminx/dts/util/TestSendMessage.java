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
package org.dataminx.dts.util;

import org.dataminx.dts.jms.JobEventQueueSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test that a DTS job is launched when a JMS message is posted on the DTS Job Submission queue.
 *
 * @author Alex Arana
 */
@ContextConfiguration(locations = { "/applicationContext.xml", "/dts-job.xml", "/activemq-jms.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSendMessage {
    @Autowired
    private JobEventQueueSender mJmsQueueSender;

    @Test
    public void submitDtsJob() {
        mJmsQueueSender.doSend("Hello world!");
    }
}
