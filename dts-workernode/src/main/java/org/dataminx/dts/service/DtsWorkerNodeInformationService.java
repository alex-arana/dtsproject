/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import java.util.Date;

/**
 * Service that returns DTS Worker Node environment and node information.
 *
 * @author Alex Arana
 */
public interface DtsWorkerNodeInformationService {
    /**
     * Returns the unique identifier string associated with this DTS Worker Node instance.
     * @return DTS Worker Node instance identifier as a <code>String</code>
     */
    String getInstanceId();

    /**
     * Returns the current transaction time.
     *
     * @return Current transaction time
     */
    Date getCurrentTime();
}
