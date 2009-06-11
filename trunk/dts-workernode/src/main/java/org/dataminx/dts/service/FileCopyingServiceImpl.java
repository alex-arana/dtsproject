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
package org.dataminx.dts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link FileCopyingService}.
 *
 * @author Alex Arana
 */
@Service("fileCopyingService")
@Scope("singleton")
public class FileCopyingServiceImpl implements FileCopyingService {
    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(FileCopyingServiceImpl.class);

    /**
     * {@inheritDoc}
     */
    public void copyFiles() {
        LOG.debug("Copying files...");
        //TODO implement this method
    }
}
