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
package org.dataminx.dts.batch.service;

import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;

/**
 * Describes the File Copying service behaviour.
 * 
 * @author Alex Arana
 * @author Gerson Galang
 */
public interface FileCopyingService {

    /**
     * Copies the content from a source file to a destination file. This method
     * has a limitation of not being able to apply user provided URI properties
     * and credentials. This method should only be used for transferring normal
     * files ie. the ones that use "file://" protocol.
     * 
     * @param sourceURI Source URI string
     * @param targetURI Target URI string
     * @param fileSystemManager the FileSystemManager
     */
    void copyFiles(String sourceURI, String targetURI, FileSystemManager fileSystemManager);

    /**
     * Copies the source to the destination. This method provides more
     * flexibility in cases where Source and Target URIs come with their own URI
     * Properties.
     * 
     * @param source the source
     * @param target the target
     * @param fileSystemManager the FileSystemManager
     */
    void copyFiles(SourceTargetType source, SourceTargetType target, FileSystemManager fileSystemManager);

    /**
     * Copies the source to the destination. This method is used if a file
     * inside a directory specified in the Source element needs to be transfered
     * and there's no way for the user to get the extra details that only the
     * dataTransferType will be able to provide.
     * 
     * @param sourceURI Source URI string
     * @param targetURI Target URI string
     * @param dataTransferType dataTransferType which will provide the URI
     *        properties of the source and target and the user credentials.
     * @param fileSystemManager the FileSystemManager
     */
    void copyFiles(String sourceURI, String targetURI, DataTransferType dataTransferType,
            FileSystemManager fileSystemManager);

}
