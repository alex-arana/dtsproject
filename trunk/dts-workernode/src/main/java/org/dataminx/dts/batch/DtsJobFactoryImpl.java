/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * This is the factory class for DTS jobs.
 *
 * @author Alex Arana
 */
@Component("dtsJobFactory")
@Scope("singleton")
public class DtsJobFactoryImpl implements DtsJobFactory, BeanFactoryAware {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobFactoryImpl.class);

    /**
     * Maps DTS job request types to DTS jobs (by their ID).
     */
    @SuppressWarnings("unchecked")
    private static final Map<String, String> DTS_JOB_REGISTRY = MapUtils.putAll(new HashMap(), new String[][] {
        {SubmitJobRequest.class.getName(), "dtsFileTransferJob"}
    });

    /** A reference to the Spring bean factory. */
    private BeanFactory mBeanFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        mBeanFactory = beanFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DtsJob createJob(Object criteria) {
        // obtain the name of the DTS job that is going to handle this request
        Assert.notNull(criteria, "Criteria element cannot be null in call to DTS job factory.");
        final String dtsJobName = DTS_JOB_REGISTRY.get(criteria.getClass().getName());

        // if we don't have a job capable of handling this type of request raise an exception
        if (StringUtils.isEmpty(dtsJobName)) {
            final String message = String.format("Unsupported DTS job request type.  A DTS Worker Node job capable"
                + " of handling requests of type '%s' is not yet registered.", criteria.getClass().getName());
            LOG.error(message);
            throw new DtsJobCreationException(message);
        }

        // create a new instance of the job using the spring bean factory
        return (DtsJob) mBeanFactory.getBean(dtsJobName, new Object[] {criteria});
    }
}
