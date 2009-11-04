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
package org.dataminx.dts.wn.batch;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.apache.commons.vfs.FileObject;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.util.Assert;


/**
 * Restartable {@link org.springframework.batch.item.ItemReader} that reads bytes from input
 * VFS file object.
 *
 * @author Alex Arana
 */
public class FileItemReader extends AbstractItemCountingItemStreamItemReader<ByteBuffer> {
    /** A reference to the underlying VFS file object. */
    private final FileObject mFileObject;

    /** A channel to read bytes from the input stream. */
    private ReadableByteChannel mInputChannel;

    /** The number of bytes copied in between calls to {@link #read()}. */
    private int mBytesCopied;

    /**
     * Creates a new instance of {@link FileItemReader} using the given VFS file object.
     *
     * @param fileObject A VFS file object
     */
    public FileItemReader(final FileObject fileObject) {
        mFileObject = fileObject;
        setName(mFileObject.getName().getFriendlyURI());
    }

    @Override
    protected void doClose() throws Exception {
        mBytesCopied = 0;
        mInputChannel.close();
    }

    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(mFileObject, "File source cannot be null");
        final InputStream in = mFileObject.getContent().getInputStream();
        mInputChannel = Channels.newChannel(in);
        //TODO handle bytes to skip in restarted scenario
    }

    @Override
    protected ByteBuffer doRead() throws Exception {
        //TODO implement a buffering strategy
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

        final int bytesRead = mInputChannel.read(buffer);
        if (bytesRead == -1) {
            // we've reached the end of the input file
            return null;
        }
        mBytesCopied += bytesRead;

        // prepare the buffer to be drained
        buffer.flip();
        return buffer;
    }
}
