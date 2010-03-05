package org.dataminx.dts.vfs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

/**
 * A ThreadLocal FileSystemManager dispenser.
 * 
 * @author Gerson Galang
 */
public class FileSystemManagerDispenser {

    private static final Log LOGGER = LogFactory.getLog(FileSystemManagerDispenser.class);

    private DtsVfsUtil mDtsVfsUtil;

    private final ThreadLocal<DefaultFileSystemManager> mThreadLocalFsManager = new ThreadLocal<DefaultFileSystemManager>() {
        @Override
        protected DefaultFileSystemManager initialValue() {
            try {
                return mDtsVfsUtil.createNewFsManager();
            } catch (final FileSystemException e) {
                LOGGER.error("FileSystemException was thrown while creating a new FileSystemManager", e);
            }
            return null;
        }

        @Override
        public void remove() {
            (get()).close();
        }
    };

    public DefaultFileSystemManager getFileSystemManager() {
        LOGGER.debug("Instantiating a new FileSystemManager");
        return mThreadLocalFsManager.get();
    }

    public void closeFileSystemManager() {
        LOGGER.debug("Closing FileSystemManager");
        mThreadLocalFsManager.remove();
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

}
