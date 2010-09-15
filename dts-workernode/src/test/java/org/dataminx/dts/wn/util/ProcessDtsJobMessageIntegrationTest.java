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
package org.dataminx.dts.wn.util;

import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dataminx.dts.common.jms.JobQueueSender;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * Test that a DTS job is launched when a JMS message is posted on the DTS Job
 * Submission queue.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
@ContextConfiguration
public class ProcessDtsJobMessageIntegrationTest extends
    AbstractTestNGSpringContextTests {
    @Autowired
    @Qualifier("jobQueueSender")
    private JobQueueSender mJmsQueueSender;

    @Autowired
    @Qualifier("eventQueueListenerContainer")
    private DefaultMessageListenerContainer mDefaultMessageListenerContainer;

    @Test
    public void submitDtsJobAsDocument() throws Exception {
        mDefaultMessageListenerContainer.start();
        final File f = new ClassPathResource(
            "/org/dataminx/dts/wn/util/minx-dts" + getTestFilePostfix()
                + ".xml").getFile();
        final SubmitJobRequestDocument jobRequest = SubmitJobRequestDocument.Factory
            .parse(f);
        Map<String, Object> jmsproperties = new HashMap<String, Object>();
        jmsproperties.put("ClientID","DtsClient001");
        jmsproperties.put("DTSWorkerNodeID","DtsWorkerNodemyhostname001");
        //jmsproperties.put("groupID", "physics");
        mJmsQueueSender.doSend(generateNewJobId(), jmsproperties, jobRequest);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException iex) {
        }
        // TODO: add a few lines of assert in here to make sure that the job really is running
        // or has completed
        mDefaultMessageListenerContainer.destroy();
    }

    private String generateNewJobId() {
        return "DTSJob_" + UUID.randomUUID();
    }
}
