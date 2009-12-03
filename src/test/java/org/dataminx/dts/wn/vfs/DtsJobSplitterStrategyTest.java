/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.vfs;

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.collections.MapUtils;
import org.dataminx.dts.wn.batch.DtsFileTransferDetails;
import org.dataminx.dts.wn.batch.DtsFileTransferDetailsPlan;
import org.dataminx.dts.wn.batch.DtsJobSplitterStrategy;
import org.dataminx.dts.wn.common.util.StopwatchTimer;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * Integration test for {@link DtsSingleFileSplitterStrategy}.
 *
 * @author Alex Arana
 */
@ContextConfiguration
public class DtsJobSplitterStrategyTest extends AbstractTestNGSpringContextTests {
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobSplitterStrategyTest.class);

    @Autowired
    @Qualifier("scopingStrategy")
    private DtsJobSplitterStrategy mScopingStrategy;

    @Test
    public void testLocale() {
        final Locale locale = Locale.JAPAN;
        assertNotNull(locale);
        LOG.info("Default Locale: " + locale);

        final ResourceBundle resource = ResourceBundle.getBundle("org.dataminx.dts.wn.vfs.codepoint", locale);
        final String warning = resource.getString("warning");
        assertNotNull(warning);
        LOG.info("WARNING: " + warning);

        final String error = resource.getString("error");
        assertNotNull(error);
        LOG.info("ERROR: " + error);
    }

    /**
     * Test method for <code>scopeFileTransfer</code>.
     */
    @Test
    public void testSplitJobRequest() throws Exception {
        assertNotNull(mScopingStrategy);
        final File file = new ClassPathResource("/org/dataminx/dts/wn/util/minx-dts.xml", getClass()).getFile();
        final SubmitJobRequestDocument document = SubmitJobRequestDocument.Factory.parse(file);
        final SubmitJobRequest jobRequest = document.getSubmitJobRequest();
        final StopwatchTimer stopwatch = new StopwatchTimer();
        final DtsFileTransferDetailsPlan fileTransfer = mScopingStrategy.splitJobRequest(jobRequest);
        final String elapsedTime = stopwatch.getFormattedElapsedTime();
        assertTrue(MapUtils.isNotEmpty(fileTransfer));

        if (LOG.isDebugEnabled() && MapUtils.isNotEmpty(fileTransfer)) {
            int i = 0;
            final StringBuffer buffer = new StringBuffer("File Transfer details" + LINE_SEPARATOR);
            for (final DtsFileTransferDetails details : fileTransfer.values()) {
                buffer.append(String.format("%03d: '%s' => '%s'%s", ++i,
                    details.getSourceUri(), details.getTargetUri(), LINE_SEPARATOR));
            }
            LOG.debug(buffer.toString());
        }

        LOG.info(String.format("Scoped %d files in %s", fileTransfer.size(), elapsedTime));
    }
}
