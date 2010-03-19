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

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_DATA_TRANSFER_STEP_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Implementation of {@link Partitioner} that can split a
 * {@link SubmitJobRequest} into smaller units of work that can subsequently be
 * be either parallelised or delegated to remote chunk processors.
 * <p>
 * Put in another way, it is the responsibility of this class to generate
 * instances of {@link ExecutionContext} that can act as input parameters to new
 * {@link org.springframework.batch.core.Step} executions.
 * <p>
 * It is a requirement that the {@link SubmitJobRequest} input to this class be
 * either injected or set manually prior to its {@link #partition(int)} method
 * being called.
 * 
 * @author Alex Arana
 * @author Gerson Galang
 */
public class DtsJobPartitioner implements Partitioner, InitializingBean {
    /** Internal logger object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DtsJobPartitioner.class);

    private DtsJobDetails mDtsJobDetails;

    /**
     * {@inheritDoc}
     */
    public Map<String, ExecutionContext> partition(final int gridSize) {
        final List<DtsJobStep> jobSteps = mDtsJobDetails.getJobSteps();
        int i = 0;

        final Map<String, ExecutionContext> map = new HashMap<String, ExecutionContext>(gridSize);
        for (final DtsJobStep jobStep : jobSteps) {
            final ExecutionContext context = new ExecutionContext();
            context.put(DTS_DATA_TRANSFER_STEP_KEY, jobStep);
            map.put(String.format("%s:%03d", DTS_DATA_TRANSFER_STEP_KEY, i), context);
            i++;
        }

        return map;
    }

    public void setDtsJobDetails(final DtsJobDetails dtsJobDetails) {
        mDtsJobDetails = dtsJobDetails;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(mDtsJobDetails != null, "Unable to find DtsJobDetails in execution context.");
    }
}
