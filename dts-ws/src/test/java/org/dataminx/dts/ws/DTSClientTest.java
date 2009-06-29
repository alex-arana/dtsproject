package org.dataminx.dts.ws;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.Assert;
import org.dataminx.dts.client.DataTransferServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

// TODO: Auto-generated Javadoc
/**
 * The Class DTSClientTest.
 */
@ContextConfiguration(locations = { "/client-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class DTSClientTest {

    /** The pre-generated UUID used for testing purposes. */
    private static final String PRE_GENERATED_UUID = "66296c5e-9a34-4e24-8fa2-4fef329e084e";

    /** The DTS WS client. */
    @Autowired
    private DataTransferServiceClient mClient;

    /**
     * Test submit job.
     */
    @Test
    public void testSubmitJob() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        File f = new ClassPathResource("minx-dts.xml").getFile();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document dtsJob = builder.parse(f);
        Assert.assertNotNull(mClient.submitJob(dtsJob));
    }

    /**
     * Test get job status.
     */
    @Test
    public void testGetJobStatus() {
        Assert.assertEquals("Created", mClient.getJobStatus(PRE_GENERATED_UUID));
    }

    /**
     * Test suspend job.
     */
    @Test
    public void testSuspendJob() {
        mClient.suspendJob(PRE_GENERATED_UUID);
        Assert.assertEquals("Suspended", mClient.getJobStatus(PRE_GENERATED_UUID));
    }

    /**
     * Test resume job.
     */
    @Test
    public void testResumeJob() {
        mClient.resumeJob(PRE_GENERATED_UUID);
        Assert.assertEquals("Transferring", mClient.getJobStatus(PRE_GENERATED_UUID));
    }

    /**
     * Test cancel job.
     */
    @Test
    public void testCancelJob() {
        Assert.assertEquals("Transferring", mClient.getJobStatus(PRE_GENERATED_UUID));
        mClient.cancelJob(PRE_GENERATED_UUID);
        Assert.assertEquals("Done", mClient.getJobStatus(PRE_GENERATED_UUID));

    }

}
