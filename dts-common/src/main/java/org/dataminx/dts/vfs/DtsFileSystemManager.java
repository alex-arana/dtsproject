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
package org.dataminx.dts.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.DtsException;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * DTS File System Manager implementation.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public class DtsFileSystemManager extends DefaultFileSystemManager {    

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsFileSystemManager.class);
    
    /** Configuration URL. */
    private Resource mConfigurationResource;
    
    private DtsVfsUtil mDtsVfsUtil;


    /**
     * Locates a file by URI using the default FileSystemOptions for file-system creation.
     *
     * @param sourceOrTarget JAXB entity containing details about a file transfer source/destination
     * @return A new file object instance
     */
    public FileObject resolveFile(final SourceTargetType sourceOrTarget) {
        Assert.notNull(sourceOrTarget);
        try {
            return resolveFile(sourceOrTarget.getURI(), mDtsVfsUtil.createFileSystemOptions(sourceOrTarget));
        }
        catch (final FileSystemException ex) {
            throw new DtsException(ex);
        }
    }
    
    public void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil) {
    	mDtsVfsUtil = dtsVfsUtil;
    }

}
