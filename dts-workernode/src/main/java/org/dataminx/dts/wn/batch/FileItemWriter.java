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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.WriteFailedException;
import org.springframework.batch.item.WriterNotOpenException;
import org.springframework.batch.item.util.ExecutionContextUserSupport;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * This class is an item writer that writes data to a given VFS file object. The writer also
 * provides restart. The location of the output file is defined by a {@link FileObject} and must
 * represent a writable file.
 * <p>
 * This class does not not use buffering as it delegates that function to the underlying VFS-supplied
 * {@link java.io.OutputStream}.
 * <p>
 * The implementation is *not* thread-safe.
 *
 * @author Alex Arana
 */
public class FileItemWriter extends ExecutionContextUserSupport
        implements ItemStream, ItemWriter<ByteBuffer> {

    /** Key to store the target offset at the last save point. */
    private static final String KEY_RESTART_DATA_OFFSET = "currentOffset";

    /** Key to store the number of written bytes in the execution context. */
    private static final String KEY_WRITTEN_BYTES = "writtenBytes";

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(FileItemWriter.class);

    /** The underlying VFS file object. */
    private final FileObject mFileObject;

    /** Represents saved state between calls to {@link #write(List)}. */
    private OutputState mState;

    /** Flag indicating whether or not to start copying file at the beginning on a restart. */
    private boolean mSaveState = true;

    /** Flag to decide whether to delete an existing target file or not. */
    private boolean mOverwrite = true;

    /** Flag to decide whether to preserve the last modified time of the copied file or not. */
    private boolean mPreserveLastModified = true;

    /**
     * Creates a new instance of {@link FileItemWriter} using the given VFS file object.
     *
     * @param fileObject A VFS file object
     */
    public FileItemWriter(final FileObject fileObject) {
        mFileObject = fileObject;
        setName(ClassUtils.getShortName(getClass()));
    }

    /**
     * Set the flag indicating whether or not up-to-date target files should be overwritten or
     * not.
     *
     * @param overwrite logical true or false
     */
    public void setOverwrite(final boolean overwrite) {
        mOverwrite = overwrite;
    }

    /**
     * Set the flag indicating whether or not to preserve the last modified time of copied files.
     *
     * @param preserveLastModified logical true or false
     */
    public void setPreserveLastModified(final boolean preserveLastModified) {
        mPreserveLastModified = preserveLastModified;
    }

    /**
     * Set the flag indicating whether or not state should be saved in the provided
     * {@link ExecutionContext} during the {@link ItemStream} call to update. Setting this to false
     * means that it will always start at the beginning on a restart.
     *
     * @param saveState logical true or false
     */
    public void setSaveState(final boolean saveState) {
        this.mSaveState = saveState;
    }

    /**
     * {@inheritDoc}
     */
    public void write(List<? extends ByteBuffer> buffers) throws Exception {
        if (!getOutputState().isInitialised()) {
            throw new WriterNotOpenException("Writer must be open before it can be written to");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Writing %d bytes to destination file", buffers.size()));
        }

        final OutputState state = getOutputState();
        try {
            for (final ByteBuffer buffer : buffers) {
                state.write(buffer);
            }
        }
        catch (IOException ex) {
            throw new WriteFailedException("Could not write data.  The file may be corrupt.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (mState != null) {
            getOutputState().close();
            mState = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void open(final ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(mFileObject, "The VFS file object must be set");
        if (!getOutputState().isInitialised()) {
            doOpen(executionContext);
        }
    }

    /**
     * Opens the output channel for writing.  This method will restore the file pointer at the last
     * known save point if supported.
     *
     * @param executionContext Execution context
     * @throws ItemStreamException if an error occurs
     */
    private void doOpen(ExecutionContext executionContext) throws ItemStreamException {
        final OutputState outputState = getOutputState();
        if (executionContext.containsKey(getKey(KEY_RESTART_DATA_OFFSET))) {
            outputState.restoreFrom(executionContext);
        }
        try {
            outputState.initialiseByteChannel();
        }
        catch (IOException ioe) {
            throw new ItemStreamException("Failed to initialize writer", ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update(ExecutionContext executionContext) {
        if (mState == null) {
            throw new ItemStreamException("ItemStream not open or already closed.");
        }

        Assert.notNull(executionContext, "ExecutionContext must not be null");
        if (mSaveState) {
            mState.save(executionContext);
        }
    }

    /**
     * Returns object representing state.
     * @return copy operation state
     */
    private OutputState getOutputState() {
        if (mState == null) {
            final OutputStream output;
            final String uri = mFileObject.getName().getFriendlyURI();
            try {
                Assert.state(!mFileObject.exists() || !mFileObject.isWriteable(),
                    "Target file is not writable: [" + uri + "]");
                output = mFileObject.getContent().getOutputStream();
            }
            catch (FileSystemException ex) {
                throw new ItemStreamException(
                    "Could not open output file: [" + mFileObject + "]", ex);
            }
            mState = new OutputState(output);
            mState.setOverwrite(mOverwrite);
        }
        return mState;
    }

    /**
     * Encapsulates the runtime state of the writer. All state changing
     * operations on the writer go through this class.
     */
    private class OutputState {
        /** A handle to the output stream. */
        private final OutputStream mOutputStream;

        /** The underlying byte channel used to write to the destination. */
        private WritableByteChannel mByteChannel;

        /** Flag to indicate if a file restart operation has been performed. */
        private boolean mRestarted;

        /** Counter for bytes written to the destination. */
        private long mBytesWritten;

        /** Last known offset position. */
        private long mLastMarkedByteOffsetPosition;

        /** Flag to indicate if the structure has been initialised. */
        private boolean mInitialised;

        /**
         * Creates a new instance of this class using the specified output stream.
         *
         * @param outputStream Output stream
         */
        OutputState(final OutputStream outputStream) {
            mOutputStream = outputStream;
        }

        /**
         * Return the byte offset position of the cursor in the output file as a
         * long integer.
         *
         * @return the offset from the beginning of the file, in bytes, at which the next write
         *         will occur.
         * @throws IOException if an I/O error occurs
         */
        public long position() throws IOException {
            long pos = 0;

            if (mByteChannel == null) {
                return 0;
            }

            //TODO implement this method
            return pos;
        }

        /**
         * Restores the last known good position for output from saved state.
         *
         * @param executionContext Execution context for parent Step
         */
        public void restoreFrom(final ExecutionContext executionContext) {
            mLastMarkedByteOffsetPosition = executionContext.getLong(getKey(KEY_RESTART_DATA_OFFSET));
            mRestarted = true;
        }

        /**
         * Set the flag indicating whether or not up-to-date target files should be overwritten or
         * not.
         *
         * @param overwrite logical true or false
         */
        public void setOverwrite(final boolean overwrite) {
            mOverwrite = overwrite;
        }

        /**
         * Close the open resource and reset counters.
         */
        public void close() {
            mInitialised = false;
            mRestarted = false;
            try {
                if (mByteChannel == null) {
                    return;
                }
                mByteChannel.close();
            }
            catch (IOException ioe) {
                throw new ItemStreamException("Unable to close the the ItemWriter", ioe);
            }
        }

        /**
         * Writes the specified byte buffer to the target file channel.
         *
         * @param buffer byte buffer
         * @throws IOException if an I/O error occurs
         */
        public void write(final ByteBuffer buffer) throws IOException {
            if (!mInitialised) {
                initialiseByteChannel();
            }
            while (buffer.hasRemaining()) {
                mBytesWritten += mByteChannel.write(buffer);
            }
            buffer.clear();
        }

        /**
         * Creates the output channel based on configuration information.
         *
         * @throws IOException if an I/O error occurs
         */
        private void initialiseByteChannel() throws IOException {
//            File file = mFileObject.getFile();
//
//            FileUtils.setUpOutputFile(file, restarted, shouldDeleteIfExists);
//
//            os = new FileOutputStream(file.getAbsolutePath(), true);
//            fileChannel = os.getChannel();
//
//            outputBufferedWriter = getBufferedWriter(fileChannel, encoding);
//
//            Assert.state(outputBufferedWriter != null);
//            // in case of restarting reset position to last committed point
//            if (restarted) {
//                checkFileSize();
//                truncate();
//            }
            mByteChannel = Channels.newChannel(mOutputStream);
            mInitialised = true;
            mBytesWritten = 0;
        }

        public boolean isInitialised() {
            return mInitialised;
        }

        /**
         * Saves the current state to the given execution context instance.
         *
         * @param executionContext Execution context of the parent step
         */
        private void save(final ExecutionContext executionContext) {
            try {
                executionContext.putLong(getKey(KEY_RESTART_DATA_OFFSET), mState.position());
            }
            catch (IOException e) {
                throw new ItemStreamException(
                    "ItemStream does not return current position properly", e);
            }

            executionContext.putLong(getKey(KEY_WRITTEN_BYTES), mState.mBytesWritten);
        }

        /**
         * Checks (on setState) to make sure that the current output file's size is not smaller
         * than the last saved commit point. If it is, then the file has been damaged in some way
         * and whole task must be started over again from the beginning.
         *
         * @throws IOException if there is an IO problem
         */
        private void checkFileSize() throws IOException {
            long size = -1;

            //TODO implement this method
            if (mFileObject.exists()) {
                size = mFileObject.getContent().getSize();
            }

            if (size < mLastMarkedByteOffsetPosition) {
                throw new ItemStreamException(
                    "Current file size is smaller than size at last commit");
            }
        }
    }
}
