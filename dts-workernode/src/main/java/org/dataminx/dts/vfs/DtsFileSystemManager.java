/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.dataminx.dts.DtsException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * DTS File System Manager implementation.
 *
 * @author Alex Arana
 */
@Component
@Scope("singleton")
public class DtsFileSystemManager extends StandardFileSystemManager implements InitializingBean {
    /** Configuration URL. */
    private Resource mConfigurationResource;

    /** Default set of options for most file systems. */
    private final FileSystemOptions mDefaultFileSystemOptions = new FileSystemOptions();

    public void setConfigurationResource(Resource resource) {
        mConfigurationResource = resource;
    }

    /**
     * Locates a file by URI using the default FileSystemOptions for file-system creation.
     *
     * @param uri File unique resource identifier string
     * @return A new file object instance
     */
    public FileObject resolveFile(final String uri) {
        try {
            return resolveFile(uri, mDefaultFileSystemOptions);
        }
        catch (FileSystemException ex) {
            throw new DtsException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mConfigurationResource, "VFS file system providers have not yet been configured.");
        setConfiguration(mConfigurationResource.getURL());
    }
}
