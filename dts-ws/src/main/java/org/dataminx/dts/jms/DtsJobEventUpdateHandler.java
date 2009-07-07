/**
 *
 */
package org.dataminx.dts.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts._2009._05.dts.JobEventUpdateRequest;
import org.springframework.integration.core.Message;


/**
 * The Handler for all the Job Event Update messages coming from the Worker Node
 *
 * @author Gerson Galang
 *
 */
public class DtsJobEventUpdateHandler {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsJobEventUpdateHandler.class);

    public void updateJob(Message<JobEventUpdateRequest> message) {
        JobEventUpdateRequest request = message.getPayload();
        // TODO: do something interesting here..

    }
}
