/**
 *
 */
package org.dataminx.dts.broker.si;

import org.dataminx.dts.common.util.XmlBeansUtils;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementing {@link DelayExtractor} to extract delay information from an {@link XmlObject}
 * using XPath query on the targeted object.
 *
 * @author hnguyen
 */
public class XmlPayloadDelayExtractor implements DelayExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlPayloadDelayExtractor.class);

    /** XPath query string used to extract delay info in the targeted xml object */
    private final String mQueryStr;

    public XmlPayloadDelayExtractor(String mQueryStr) {
        this.mQueryStr = mQueryStr;
    }

    @Override
    public String extractDelay(Object object) {
        if (object instanceof XmlObject) {
            XmlObject payload = (XmlObject) object;

            XmlObject[] transferReqs = payload.selectPath(mQueryStr);
            if (transferReqs.length>0) {
                XmlObject transferReq = transferReqs[0];
                return XmlBeansUtils.extractElementTextAsString(transferReq);
            }
        }
        else {
            throw new RuntimeException("Object has to be a type of XmlObject");
        }
        return null;

    }
}
