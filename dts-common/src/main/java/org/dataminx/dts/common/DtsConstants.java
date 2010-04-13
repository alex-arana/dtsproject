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
package org.dataminx.dts.common;

/**
 * Defines all the constants for the DTS application.
 * 
 * @author Alex Arana
 */
public class DtsConstants {

    /**
     * Name of the configuration property that points to the directory holding
     * configuration artifacts for the application.
     * <p>
     * Normally, this parameter would be passed to the application at startup
     * using the following:
     * 
     * <pre>
     *   java <b>-Ddataminx.dir=</b><i>/home/username/.dataminx</i> ...  &lt;runner_class&gt;
     * </pre>
     */
    public static final String DATAMINX_CONFIGURATION_KEY = "dataminx.dir";

    /** Default name of the dataminx configuration directory. */
    public static final String DEFAULT_DATAMINX_CONFIGURATION_DIR = ".dataminx";

    /** Default log4j configuration filename. */
    public static final String DEFAULT_LOG4J_CONFIGURATION_FILE = "log4j.xml";

    /** Default Spring classpath. */
    public static final String DEFAULT_SPRING_CLASSPATH = "au/org/dataminx/dts/applicationContext.xml";

    /** Default JVM property used to override the log4j configuration source. */
    public static final String DEFAULT_LOG4J_CONFIGURATION_KEY = "log4j.configuration";

    /** Holds the namespace URI of the WS-Security namespace. */
    public static final String WS_SECURITY_NAMESPACE_URI = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    public static final String DTS_JSDL_NAMESPACE_URI = "http://schemas.dataminx.org/dts/2009/07/jsdl";

    public static final String TMP_ROOT_PROTOCOL = "tmp:///";

    public static final String FILE_ROOT_PROTOCOL = "file:///";
}
