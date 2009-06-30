/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

import static org.dataminx.dts.common.DtsWorkerNodeConstants.DTS_DATA_TRANSFER_STEP_KEY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.dataminx.dts.common.InputDataUtils;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.Assert;

/**
 * Implementation of {@link Partitioner} that can split a {@link SubmitJobRequest} into smaller units
 * of work that can subsequently be be either parallelised or delegated to remote chunk processors.
 * <p>
 * Put in another way, it is the responsibility of this class to generate instances of {@link ExecutionContext}
 * that can act as input parameters to new {@link org.springframework.batch.core.Step} executions.
 * <p>
 * It is a requirement that the {@link SubmitJobRequest} input to this class be either injected or set manually
 * prior to its {@link #partition(int)} method being called.
 *
 * @author Alex Arana
 */
public class DtsJobPartitioner implements Partitioner {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobPartitioner.class);

    /** A reference to the input DTS job request. */
    private SubmitJobRequest mSubmitJobRequest;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Assert.state(mSubmitJobRequest != null, "Unable to find DTS Job Request in execution context.");
        final List<DataTransferType> dataTransfers = InputDataUtils.getDataTransfers(mSubmitJobRequest);
        if (CollectionUtils.isEmpty(dataTransfers)) {
            LOG.warn("DTS job request is incomplete as it does not contain any data transfer elements.");
            throw new DtsJobExecutionException("DTS job request contains no data transfer elements.");
        }

        // the current partitioning strategy merely creates a new execution context for every
        // data transfer element in the input.
        // TODO refine partitioning logic so it becomes granular at the individual file transfer
        //      level. ie. currently a single data transfer element could potentially contain a
        //      directory holding hundreds or more files...
        int i = 0;
        final Map<String, ExecutionContext> map = new HashMap<String, ExecutionContext>(gridSize);
        for (final DataTransferType dataTransfer : dataTransfers) {
            ExecutionContext context = new ExecutionContext();
            context.put(DTS_DATA_TRANSFER_STEP_KEY, dataTransfer);
            map.put(String.format("%s:%03d", DTS_DATA_TRANSFER_STEP_KEY, i), context);
            i++;
        }
        return map;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }
}
