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
package org.dataminx.dts.batch;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * This is the factory class for all Spring Batch jobs included with the DTS implementation.
 * <p>
 * All DTS Jobs designed to handle client requests are to be created via this class. A list of all known jobs and their
 * corresponding Spring components are maintained within its internal registry. Add new ones as required.
 *
 * @author Alex Arana
 */
public class DtsJobFactoryImpl implements DtsJobFactory, BeanFactoryAware {
    /** Internal logger object. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(DtsJobFactoryImpl.class);

    /**
     * Maps DTS job request types to DTS jobs (by their ID).
     */
    @SuppressWarnings("unchecked")
    private static final Map<String, String> DTS_JOB_REGISTRY = MapUtils
        .putAll(new HashMap(), new String[][] {{
            SubmitJobRequestDocument.class.getName(), "dtsFileTransferJob"}});

    /** A reference to the Spring Batch Job repository. */
    private JobRepository mJobRepository;

    /** A reference to the Spring bean factory. */
    private BeanFactory mBeanFactory;

    /**
     * {@inheritDoc}
     */
    public DtsJob createJob(final String jobId, final Object criteria) {
        // obtain the name of the DTS job that is going to handle this request
        Assert.notNull(criteria,
            "Criteria element cannot be null in call to DTS job factory.");
        final String dtsJobName = lookupJobName(criteria);

        // if we don't have a job capable of handling this type of request raise an exception
        if (StringUtils.isEmpty(dtsJobName)) {
            final String message = String
                .format(
                    "Unsupported DTS job request type.  A DTS Worker Node job capable"
                        + " of handling requests of type '%s' is not yet registered.",
                    criteria.getClass().getName());
            LOGGER.error(message);
            throw new DtsJobCreationException(message);
        }

        // create a new instance of the job using the spring bean factory
        return (DtsJob) mBeanFactory.getBean(dtsJobName, new Object[] {jobId,
            criteria, mJobRepository});
    }

    /**
     * Returns the DTS Job name corresponding to the given request object instance.
     *
     * @param instance
     *            DTS Job input request object
     * @return A fully-qualified class name of the corresponding DTS Job to handle the input request or
     *         <code>null</code> if one has not yet been defined.
     */
    private String lookupJobName(final Object instance) {
        for (final Class<?> definition : ClassUtils.getAllInterfaces(instance)) {
            final String key = definition.getName();
            if (DTS_JOB_REGISTRY.containsKey(key)) {
                return DTS_JOB_REGISTRY.get(key);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setBeanFactory(final BeanFactory beanFactory)
        throws BeansException {
        mBeanFactory = beanFactory;
    }

    public void setJobRepository(final JobRepository jobRepository) {
        mJobRepository = jobRepository;
    }
}
