/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link DtsWorkerNodeInformationService} interface.
 *
 * @author Alex Arana
 */
@Service("dtsWorkerNodeInformationService")
@Scope("singleton")
public class DtsWorkerNodeInformationServiceImpl implements DtsWorkerNodeInformationService {
    /** String returned from {@link #getHostname()} when the localhost cannot be resolved. */
    public static final String UNKNOWN_HOST = "unknown";

    /**
     * The String which uniquely identifies this DTS Worker Node instance.  The returned string contains
     * the format: "DtsWorkerNode + hostname + UUID".
     */
    private final String mInstanceId = String.format("DtsWorkerNode-%s-%s", getHostname(), UUID.randomUUID());

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCurrentTime() {
        //TODO return transaction start time via TLS?
        return Calendar.getInstance().getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInstanceId() {
        return mInstanceId;
    }

    /**
     * Gets the host name for this IP address.  If this method fails to resolve the name of the localhost it
     * returns {@value #UNKNOWN_HOST}.
     * <p>
     * TODO move this method to a helper class later on..
     *
     * @return host name for the current IP address
     */
    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (final UnknownHostException ex) {
            return UNKNOWN_HOST;
        }
    }
}
