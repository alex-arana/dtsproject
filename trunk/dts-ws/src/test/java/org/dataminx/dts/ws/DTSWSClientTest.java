package org.dataminx.dts.ws;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.Assert;
import org.dataminx.dts.client.DataTransferServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

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
    private Document mDtsJob;

    /**
     * Parses the test job and convert to a org.w3c.dom.Document object
     * @throws Exception
     */
    @Before
    public void parseDtsJobDef() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        File f = new ClassPathResource("ws-minx-dts.xml").getFile();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        mDtsJob = builder.parse(f);
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
    @Test
    public void testGetJobStatus() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        Assert.assertEquals("Created", mClient.getJobStatus(jobResourceKey));
    }

    /**
     * Test suspend job.
     * @throws Exception the exception
     */
    @Test
    public void testSuspendJob() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        mClient.suspendJob(jobResourceKey);
        Assert.assertEquals("Suspended", mClient.getJobStatus(jobResourceKey));
    }

    /**
     * Test resume job.
     * @throws Exception the exception
     */
    @Test
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
    @Test
    public void testCancelJob() throws Exception {
        String jobResourceKey = mClient.submitJob(mDtsJob);
        mClient.cancelJob(jobResourceKey);
        Assert.assertEquals("Done", mClient.getJobStatus(jobResourceKey));

    }

}
