package org.dataminx.dts.batch;

import java.io.File;
import java.util.UUID;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "/org/dataminx/dts/batch/client-context.xml",
        "/org/dataminx/dts/batch/batch-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class BulkCopyJobIntegrationTest {

    private JobDefinitionDocument mDtsJob;

    private static final Log LOGGER = LogFactory.getLog(BulkCopyJobIntegrationTest.class);

    @Autowired
    private DtsJobLauncher mJobLauncher;

    @Before
    public void parseDtsJobDef() throws Exception {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/testjob.xml").getFile();
        mDtsJob = JobDefinitionDocument.Factory.parse(f);
        Assert.assertNotNull(mDtsJob);
    }

    @Test
    public void runJob() throws Exception {
        mJobLauncher.run(UUID.randomUUID().toString(), mDtsJob);
    }

}
