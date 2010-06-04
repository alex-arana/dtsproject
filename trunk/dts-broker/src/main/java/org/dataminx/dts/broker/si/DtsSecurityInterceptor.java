/**
 *
 */
package org.dataminx.dts.broker.si;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.dataminx.dts.common.DtsConstants.WS_SECURITY_NAMESPACE_URI;
import static org.dataminx.dts.common.util.XmlBeansUtils.extractElementTextAsString;

import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.common.util.XmlBeansUtils;
import org.dataminx.dts.security.crypto.DummyEncrypter;
import org.dataminx.dts.security.crypto.Encrypter;
import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.PasswordString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;

/**
 * <p>A {@link org.springframework.integration.channel.ChannelInterceptor} that encrypts sensitive information in the message. The encryption
 * algorithm can be configured in an external configuration file</p>
 * <p>It should be noted that no actions will take place if sensitive information has been encrypted such
 * as those with prefix cleartext:xxxxx (@see DummyEncrypter)</p>
 * @author hnguyen
 */
public class DtsSecurityInterceptor extends ChannelInterceptorAdapter  implements InitializingBean{

    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsSecurityInterceptor.class);

    private static final QName PASSWORD_STRING_QNAME = new QName(
        WS_SECURITY_NAMESPACE_URI, "PasswordString");

    private Encrypter mEncrypter;

    public void setEncrypter(Encrypter encrypter) {
        this.mEncrypter = encrypter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        XmlObject payload = (XmlObject)message.getPayload();
        if (payload instanceof SubmitJobRequestDocument) {
            SubmitJobRequestDocument document = (SubmitJobRequestDocument)payload;
            SubmitJobRequest request = document.getSubmitJobRequest();
            List<SourceTargetType> sourceAndTargets = getAllSourceOrTarget(request);
            for (SourceTargetType sourceOrTarget:sourceAndTargets) {
                if (sourceOrTarget instanceof MinxSourceTargetType) {
                    MinxSourceTargetType minxSourceTarget = (MinxSourceTargetType)sourceOrTarget;
                    CredentialType credential = minxSourceTarget.getCredential();
                    if (credential != null) {
                        if (credential.getMyProxyToken() != null) {
                            throw new UnsupportedOperationException("MyProxy not yet supported");

                        }
                        // at the moment we're only supporting u/p credentials
                        else if (credential.getUsernameToken() != null) {
                            XmlObject element = XmlBeansUtils.selectAnyElement(credential.getUsernameToken(), PASSWORD_STRING_QNAME);
                            final String encryptedPassword = element == null ? EMPTY : mEncrypter
                                .encrypt(extractElementTextAsString(element));
                            LOG.debug("Encrypted password: " + encryptedPassword);
                            final PasswordString password = PasswordString.Factory.newInstance();
                            password.setStringValue(encryptedPassword);
                            element.set(password);
                        }
                        else if (credential.getOtherCredentialToken() != null) {
                            throw new UnsupportedOperationException("Other Credential Type not yet supported");
                        }

                    }
                }
            }
        }


        return message;
    }

    /*
     *
     */
    private List<SourceTargetType> getAllSourceOrTarget(SubmitJobRequest request) {

        final DataTransferType[] dataTransfers = ((MinxJobDescriptionType) request
            .getJobDefinition().getJobDescription()).getDataTransferArray();
        List<SourceTargetType> sourceOrTarget = new LinkedList<SourceTargetType>();

        for (DataTransferType transfer:dataTransfers) {
            if (transfer.getSource()!=null) {
                sourceOrTarget.add(transfer.getSource());
            }
            if (transfer.getTarget()!=null) {
                sourceOrTarget.add(transfer.getTarget());
            }

        }
        return sourceOrTarget;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
    }
}
