/**
 *
 */
package org.dataminx.dts.broker.si;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import org.dataminx.dts.security.crypto.DummyEncrypter;
import org.dataminx.dts.security.crypto.Encrypter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.ChannelInterceptor;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.message.MessageBuilder;

/**
 * <p>A {@link ChannelInterceptor} that encrypts sensitive information in the message. The encryption
 * algorithm can be configured in an external configuration file</p>
 * <p>It should be noted that no actions will take place if sensitive information has been encrypted such
 * as those with prefix cleartext:xxxxx (@see DummyEncrypter)</p>
 * @author hnguyen
 */
public class DtsSecurityInterceptor extends ChannelInterceptorAdapter  implements InitializingBean{

    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsSecurityInterceptor.class);

    private static final String CREDENTIAL_QUERY="//oas:PasswordString";

    private Encrypter mEncrypter;

    public void setEncrypter(Encrypter encrypter) {
        this.mEncrypter = encrypter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        String payload = (String)message.getPayload();
        SAXBuilder builder = new SAXBuilder();
        Reader reader = new StringReader(payload);
        try {
            Document doc = builder.build(reader);
            XPath xpath = XPath.newInstance(CREDENTIAL_QUERY);
            List nodes = xpath.selectNodes(doc);
            for (java.util.Iterator i = nodes.iterator();i.hasNext();) {
                Element node = (Element)i.next();
                String value = node.getValue();
                node.setText(mEncrypter.encrypt(value));
            }
            XMLOutputter out = new XMLOutputter();
            StringWriter writer = new StringWriter();
            out.output(doc, writer);
            return MessageBuilder.fromMessage(message).withPayload(writer.getBuffer().toString()).build();
        }
        catch (Exception e) {
            // TODO handle exception
        }
        return message;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
    }
}
