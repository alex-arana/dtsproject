package org.dataminx.dts.batch;

import java.util.Set;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimpleStepExecutionSplitter;
import org.springframework.batch.core.repository.JobRepository;

public class DtsStepExecutionSplitter extends SimpleStepExecutionSplitter {

    private final Partitioner mPartitioner;

    public DtsStepExecutionSplitter(final JobRepository jobRepository, final Step step) {
        this(jobRepository, step, null);
    }

    public DtsStepExecutionSplitter(final JobRepository jobRepository, final Step step, final Partitioner partitioner) {
        super(jobRepository, step, partitioner);
        mPartitioner = partitioner;
    }

    @Override
    public Set<StepExecution> split(final StepExecution stepExecution, final int gridSize) throws JobExecutionException {

        return super.split(stepExecution, gridSize);
    }
}
