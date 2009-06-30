/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

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
