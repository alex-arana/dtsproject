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
package org.dataminx.dts.wn.batch;

import java.util.List;
import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.vfs.FileSystemManagerDispenser;
import org.dataminx.dts.wn.service.FileCopyingService;
import org.dataminx.dts.wn.service.JobNotificationService;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * {@link Tasklet}-oriented version of the DTS file transfer job implementation.
 * It is expected that the item-oriented version of this operation will become
 * the preferred implementation.
 * <p>
 * It is a requirement that the {@link DataTransferType} input to this class be
 * either injected or set manually prior to its
 * {@link #execute(StepContribution, ChunkContext)} method being called.
 * 
 * @author Alex Arana
 */
public class FileCopyTask implements Tasklet, StepExecutionListener {
	/** A reference to the internal logger object. */
	private static final Logger LOG = LoggerFactory.getLogger(FileCopyTask.class);

	/** A reference to the application's file copying service. */
	@Autowired
	private FileCopyingService mFileCopyingService;

	/** A reference to the application's job notification service. */
	@Autowired
	private JobNotificationService mJobNotificationService;

	private FileSystemManagerDispenser mFileSystemManagerDispenser;

	private DtsJobStep mJobStep;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		StepContext stepContext = chunkContext.getStepContext();
		LOG.info("Executing copy step: " + stepContext.getStepName());

		Assert.state(mJobStep != null, "Unable to find data transfer input data in step context.");
		LOG.info(mJobStep.toString());

		List<DtsDataTransferUnit> dataTransferUnits = mJobStep.getDataTransferUnits();
		FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();

		// TODO reimplement this with threadpool
		for (DtsDataTransferUnit dataTransferUnit : dataTransferUnits) {

			// TODO when we start to run this by threads.. we'll need to get
			// each thread to use one ThreadLocal
			// instance of the FileSystemManager
			mFileCopyingService.copyFiles(dataTransferUnit.getSourceFileURI(),
			        dataTransferUnit.getDestinationFileURI(), dataTransferUnit.getDataTransfer(), fileSystemManager);
		}

		// TODO figure out when we should close fileSystemManager

		// TODO handle failures by returning ...

		return RepeatStatus.FINISHED;
	}

	public void setJobStep(DtsJobStep jobStep) {
		mJobStep = jobStep;
	}

	public void setFileSystemManagerDispenser(FileSystemManagerDispenser fileSystemManagerDispenser) {
		mFileSystemManagerDispenser = fileSystemManagerDispenser;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeStep(final StepExecution stepExecution) {
		// perform any preliminary steps here
	}

	/**
	 * Extracts the ID of this Step's parent DTS Job from the specifiec Step
	 * execution context.
	 * 
	 * @param stepExecution
	 *            A reference to this Step's execution context
	 * @return The parent DTS Job identifier
	 */
	private String extractDtsJobId(final StepExecution stepExecution) {
		Assert.state(stepExecution != null);
		return stepExecution.getJobExecution().getJobInstance().getJobName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		final ExitStatus exitStatus = stepExecution.getExitStatus();
		if (stepExecution.getStatus().isUnsuccessful()) {
			final String dtsJobId = extractDtsJobId(stepExecution);
			mJobNotificationService.notifyStepFailures(dtsJobId, stepExecution);
		}
		return exitStatus;
	}
}
