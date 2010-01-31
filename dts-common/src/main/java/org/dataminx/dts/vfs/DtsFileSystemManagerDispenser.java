package org.dataminx.dts.vfs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;

public class DtsFileSystemManagerDispenser {

	private static final Log LOGGER = LogFactory.getLog(DtsFileSystemManagerDispenser.class);
	
	private DtsVfsUtil mDtsVfsUtil;
	
	private ThreadLocal <DtsFileSystemManager> mThreadLocalFsManager = 
		new ThreadLocal <DtsFileSystemManager> () {
            @Override
            protected DtsFileSystemManager initialValue() {
    			try {
	                return mDtsVfsUtil.createNewDtsFsManager();
                } catch (FileSystemException e) {
	                LOGGER.error("FileSystemException was thrown by ThreadLocalFsManager", e);
	                
                }
                return null;
    		}
            
            @Override
            public void remove() {
            	((DtsFileSystemManager) get()).close();
            }
    };
    
	public DtsFileSystemManager getFileSystemManager() {
		return mThreadLocalFsManager.get();
	}
	
	public void closeFileSystemManager() {
		mThreadLocalFsManager.remove();
	}
	
	public void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil) {
    	mDtsVfsUtil = dtsVfsUtil;
    }


}
