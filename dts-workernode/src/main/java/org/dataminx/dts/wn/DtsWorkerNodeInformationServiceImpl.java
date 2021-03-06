/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.wn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of the {@link DtsWorkerNodeInformationService} interface.
 *
 * @author Alex Arana
 */
public class DtsWorkerNodeInformationServiceImpl implements
    DtsWorkerNodeInformationService {

    /** String returned from {@link #getHostname()} when the localhost cannot be resolved. */
    public static final String UNKNOWN_HOST = "unknown";

    private String mInstanceId;
    
    private String mWorkerNodeIDMessageHeaderName;

    /**
     * Gets the host name for this IP address. If this method fails to resolve the name of the localhost it returns
     * {@value #UNKNOWN_HOST}.
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

    /**
     * {@inheritDoc}
     */
    public Date getCurrentTime() {
        //TODO return transaction start time via TLS?
        return Calendar.getInstance().getTime();
    }

    /**
     * Return the WorkNodeID
     */
    public String getInstanceId() {

        return mInstanceId;
    }
    
    /**
     * Returns the the name of the workernodeID message header.
     * @return The workernodeID message header name as a <code>String</code>
     */
    public String getWorkerNodeIDMessageHeaderName(){

        return mWorkerNodeIDMessageHeaderName;
    }

    /**
     * Set the WorkNodeID that is specified in the workernode properties configuration file
     */
    public void setWorkerNodeID(String workerNodeID){

        mInstanceId=workerNodeID;
    }

    /**
     * Set the name of the workernodeID message header this is specified in the workernode properties configuration file.
     */
    public void setWorkerNodeIDMessageHeaderName(String workerNodeIDMessageHeaderName){

        mWorkerNodeIDMessageHeaderName= workerNodeIDMessageHeaderName;
    }
}
