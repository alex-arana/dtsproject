/**
 *
 */
package org.dataminx.dts.broker.si;

import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;

import org.apache.xmlbeans.XmlObject;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.TestedObject;

/**
 * Tests for {@link XmlPayloadDelayExtractor} implementation.
 *
 * @author hnguyen
 */
@Test(groups = "testng-unit-tests")
public class XmlPayloadDelayExtractorTest extends UnitilsTestNG {

    private static final String QUERY_STR = "declare namespace dmi2='http://schemas.dmi.proposal.org/dts/2010/dmi-common';$this//dmi2:StartNotBefore";
    private static final String EXPECTED = "2010-03-11T17:50:00";
    @TestedObject
    private XmlPayloadDelayExtractor mExtractor;

    @Test
    public void testExtractDelay() throws Exception {
        mExtractor = new XmlPayloadDelayExtractor(QUERY_STR);
        final Resource xml = new ClassPathResource("/job"
            + getTestFilePostfix() + ".xml");
        final XmlObject targetXml = SubmitJobRequestDocument.Factory.parse(
            xml.getInputStream()).getSubmitJobRequest();
        final String delay = mExtractor.extractDelay(targetXml);
        Assert.assertEquals(delay, EXPECTED);
    }
}
