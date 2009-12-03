/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.dataminx.dts.wn.common.DtsWorkerNodeConstants.DTS_DATA_TRANSFER_STEP_KEY;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.dataminx.dts.wn.common.util.StopwatchTimer;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
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

    /** A reference to the strategy used to split the job into its finer parts. */
    private DtsJobSplitterStrategy mSplitterStrategy;

    /** A reference to the input DTS job request. */
    private SubmitJobRequest mSubmitJobRequest;

    /** The identifier for the job corresponding to this partitioner. */
    private String mJobId;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ExecutionContext> partition(final int gridSize) {
        Assert.state(mSubmitJobRequest != null, "Unable to find DTS Job Request in execution context.");

        // the current partitioning strategy merely creates a new execution context for every
        // file transfer details object returned by the associated job splitter
        int i = 0;
        final StopwatchTimer timer = new StopwatchTimer();
        final DtsFileTransferDetailsPlan fileTransfers = mSplitterStrategy.splitJobRequest(mSubmitJobRequest);
        LOG.info(String.format("Scoped DTS job '%s' in %s", mJobId, timer.getFormattedElapsedTime()));
        final Map<String, ExecutionContext> map = new HashMap<String, ExecutionContext>(gridSize);
        for (final DtsFileTransferDetails transferDetails : fileTransfers.values()) {
            final ExecutionContext context = new ExecutionContext();
            context.put(DTS_DATA_TRANSFER_STEP_KEY, transferDetails);
            map.put(String.format("%s:%03d", DTS_DATA_TRANSFER_STEP_KEY, i), context);
            i++;
        }

        if (LOG.isDebugEnabled() && MapUtils.isNotEmpty(map)) {
            final StringBuilder buffer = new StringBuilder();
            buffer.append(String.format("Split DTS job '%s' into %d individual step(s):", mJobId, map.size()));
            for (final Map.Entry<String, ExecutionContext> entry : map.entrySet()) {
                final DtsFileTransferDetails transferDetails =
                    (DtsFileTransferDetails) entry.getValue().get(DTS_DATA_TRANSFER_STEP_KEY);
                buffer.append(LINE_SEPARATOR);
                buffer.append(String.format("%s: '%s' => '%s'",
                    entry.getKey(), transferDetails.getSourceUri(), transferDetails.getTargetUri()));
            }
            LOG.debug(buffer.toString());
        }
        return map;
    }

    public DtsJobSplitterStrategy getSplitterStrategy() {
        return mSplitterStrategy;
    }

    public void setSplitterStrategy(final DtsJobSplitterStrategy splitterStrategy) {
        mSplitterStrategy = splitterStrategy;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }

    public void setJobId(final String jobId) {
        mJobId = jobId;
    }
}
