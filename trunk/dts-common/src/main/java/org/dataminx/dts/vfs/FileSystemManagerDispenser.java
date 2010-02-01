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
	
	private ThreadLocal <DefaultFileSystemManager> mThreadLocalFsManager = 
		new ThreadLocal <DefaultFileSystemManager> () {
            @Override
            protected DefaultFileSystemManager initialValue() {
    			try {
	                return mDtsVfsUtil.createNewFsManager();
                } catch (FileSystemException e) {
	                LOGGER.error("FileSystemException was thrown while creating a new FileSystemManager", e);	                
                }
                return null;
    		}
            
            @Override
            public void remove() {
            	((DefaultFileSystemManager) get()).close();
            }
    };
    
	public DefaultFileSystemManager getFileSystemManager() {
		return mThreadLocalFsManager.get();
	}
	
	public void closeFileSystemManager() {
		mThreadLocalFsManager.remove();
	}
	
	public void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil) {
    	mDtsVfsUtil = dtsVfsUtil;
    }


}
