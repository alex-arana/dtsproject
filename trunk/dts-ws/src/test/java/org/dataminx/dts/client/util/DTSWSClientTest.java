/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
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
package org.dataminx.dts.client.util;

import java.io.File;
import junit.framework.Assert;
import org.dataminx.dts.client.sws.DataTransferServiceClient;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The Unit Test for the integrated DTS WS, DTS Domain, and DTS Worker Node modules.
 *
 * @author Gerson Galang
 */
@ContextConfiguration(locations = { "/client-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class DTSWSClientTest {

    /** The DTS WS client. */
    @Autowired
    private DataTransferServiceClient mClient;

    /** The DTS Job XML document. */
    private JobDefinitionDocument mDtsJob;

    /**
     * Parses the test job and convert to a org.w3c.dom.Document object
     * @throws Exception
     */
    @Before
    public void parseDtsJobDef() throws Exception {
        File f = new ClassPathResource("ws-minx-dts.xml").getFile();
        mDtsJob = JobDefinitionDocument.Factory.parse(f);
        Assert.assertNotNull(mDtsJob);
    }

    /**
     * Test submit job.
     * @throws Exception the exception
     */
    @Test
    public void testSubmitJob() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        Assert.assertNotNull(jobResourceKey);
    }

    /**
     * Test get job status.
     * @throws Exception the exception
     */
    //@Test
    public void testGetJobStatus() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        Assert.assertEquals("Created", mClient.getJobStatus(jobResourceKey));
    }

    /**
     * Test suspend job.
     * @throws Exception the exception
     */
    //@Test
    public void testSuspendJob() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        mClient.suspendJob(jobResourceKey);
        Assert.assertEquals("Suspended", mClient.getJobStatus(jobResourceKey));
    }

    /**
     * Test resume job.
     * @throws Exception the exception
     */
    //@Test
    public void testResumeJob() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        mClient.suspendJob(jobResourceKey);
        mClient.resumeJob(jobResourceKey);
        Assert.assertEquals("Transferring", mClient.getJobStatus(jobResourceKey));
    }

    /**
     * Test cancel job.
     * @throws Exception the exception
     */
    //@Test
    public void testCancelJob() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        mClient.cancelJob(jobResourceKey);
        Assert.assertEquals("Done", mClient.getJobStatus(jobResourceKey));

    }

}
