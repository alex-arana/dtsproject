/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.wn;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument.GetJobStatusRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument.ResumeJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
//import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyLong;

import static org.mockito.Matchers.anyString;
import org.testng.Assert;

/**
 * Test the ControlRequestHandler Service.
 *
 * @author David Meredith
 */
@Test(groups = "testng-unit-tests")
public class ControlRequestHandlerTest {

    private DtsWorkerNodeInformationService mockDtsWorkerNodeInfoService;
    private WorkerNodeManager mockWorkerNodeManager;
    private JobRestartStrategy mockJobRestartStrategy;

    private final Map<String, Object> msgHeaders = new LinkedHashMap<String, Object>();
    private final ControlRequestHandler mControlRequestHandler = new ControlRequestHandler();

    private final String mTestJobNameKey = "job1";

    @BeforeClass
    public void init() throws Exception {
        // mock up the DtsWorkerNodeInfoService
        mockDtsWorkerNodeInfoService = mock(DtsWorkerNodeInformationService.class);
        when(mockDtsWorkerNodeInfoService.getInstanceId()).thenReturn("mockInstanceID");
        when(mockDtsWorkerNodeInfoService.getWorkerNodeIDMessageHeaderName()).thenReturn("mockWorkerIdHeaderName");
        when(mockDtsWorkerNodeInfoService.getCurrentTime()).thenReturn(new Date());

        // mock up the worker node manager
        mockWorkerNodeManager = mock(WorkerNodeManager.class);
        Set<String> jobNames = new HashSet<String>(0);
        jobNames.add(mTestJobNameKey);
        when(mockWorkerNodeManager.getJobNames()).thenReturn(jobNames);
        Set<Long> jobIDs = new HashSet<Long>(0);
        jobIDs.add(Long.MIN_VALUE);
        when(mockWorkerNodeManager.getRunningExecutions(anyString())).thenReturn(jobIDs);
        when(mockWorkerNodeManager.stop(anyLong())).thenReturn(true);
        String mockStatusReport = String.format(", startTime=%s, endTime=%s, lastUpdated=%s, status=%s, exitStatus=%s, job=[%s]",
                new Date().toString(), new Date().toString(), new Date().toString(), BatchStatus.COMPLETED, ExitStatus.COMPLETED, "jobInstance.toString");
        when(mockWorkerNodeManager.getSummary(anyLong())).thenReturn(mockStatusReport);

        // mock up the restart strategy
        mockJobRestartStrategy = mock(JobRestartStrategy.class);
        doNothing().when(mockJobRestartStrategy).restartJob(this.mTestJobNameKey); 
        

        // add the mocks to the mControlRequestHandler
        mControlRequestHandler.setDtsWorkerNodeInformationService(mockDtsWorkerNodeInfoService);
        mControlRequestHandler.setWorkerNodeManager(mockWorkerNodeManager);
        mControlRequestHandler.setJobRestartStrategy(mockJobRestartStrategy);

        msgHeaders.put("stringSample", "hello world");
        msgHeaders.put("doubleSample", new Double(26));
        msgHeaders.put("longSample", new Long(24));
        msgHeaders.put("dateSample", new Date());
    }


    @Test(enabled=true)
    public void testHandleControlRequest_ResumeJobRequestDocument() {
        final ResumeJobRequestDocument document = ResumeJobRequestDocument.Factory.newInstance();
        final ResumeJobRequest resumeReq = document.addNewResumeJobRequest();
        resumeReq.setJobResourceKey(mTestJobNameKey);
        MessageBuilder<ResumeJobRequestDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(msgHeaders);
        Message<ResumeJobRequestDocument> msg = msgbuilder.build();
        Object responseObj = this.mControlRequestHandler.handleControlRequest(msg);
        Assert.assertTrue(responseObj instanceof Message);
        Message response = (Message) responseObj;
        Assert.assertTrue(response.getPayload() instanceof ResumeJobResponseDocument);
    }

    @Test(enabled=true)
    public void testHandleControlRequest_GetJobStatusRequestDocument() {
        final GetJobStatusRequestDocument document = GetJobStatusRequestDocument.Factory.newInstance();
        final GetJobStatusRequest jobStatusReq = document.addNewGetJobStatusRequest();
        jobStatusReq.setJobResourceKey(mTestJobNameKey);
        MessageBuilder<GetJobStatusRequestDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(msgHeaders);
        Message<GetJobStatusRequestDocument> msg = msgbuilder.build();
        Object responseObj = this.mControlRequestHandler.handleControlRequest(msg);
        Assert.assertTrue(responseObj instanceof Message);
        Message response = (Message) responseObj;
        Assert.assertTrue(response.getPayload() instanceof GetJobStatusResponseDocument);
    }

    /*@Test
    public void testHandleControlRequest_GetJobDetailsRequestDocument() {
        final GetJobDetailsRequestDocument document = GetJobDetailsRequestDocument.Factory.newInstance();
        GetJobDetailsRequest jobDetailsReq = document.addNewGetJobDetailsRequest();
        jobDetailsReq.setJobResourceKey(mTestJobNameKey);
        MessageBuilder<GetJobDetailsRequestDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(msgHeaders);
        Message<GetJobDetailsRequestDocument> msg = msgbuilder.build();
        Object responseObj = this.mControlRequestHandler.handleControlRequest(msg);
        Assert.assertTrue(responseObj instanceof Message);
        Message response = (Message) responseObj;
        Assert.assertTrue(response.getPayload() instanceof GetJobDetailsResponseDocument);
    }*/


    @Test(enabled=true)
    public void testHandleControlRequest_CancelJobRequestDocument() {
        final CancelJobRequestDocument document = CancelJobRequestDocument.Factory.newInstance();
        final CancelJobRequest cancelJobReq = document.addNewCancelJobRequest();
        cancelJobReq.setJobResourceKey(mTestJobNameKey);
        MessageBuilder<CancelJobRequestDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(msgHeaders);
        Message<CancelJobRequestDocument> msg = msgbuilder.build(); 
        //Message<CancelJobRequestDocument> msg = mControlRequestHandler.getResponseMessage(document, msgHeaders);
        Object responseObj = this.mControlRequestHandler.handleControlRequest(msg);
        Assert.assertTrue(responseObj instanceof Message);
        Message response = (Message) responseObj;
        Assert.assertTrue(response.getPayload() instanceof CancelJobResponseDocument);
    }


    @Test(enabled=true)
    public void testHandleControlRequest_InvalidDocument() {
        final CancelJobRequestDocument document = CancelJobRequestDocument.Factory.newInstance();
        // Do not add the cancelJobRequest to make it invalid
        //final CancelJobRequest cancelJobReq = document.addNewCancelJobRequest();
        //cancelJobReq.setJobResourceKey(mTestJobNameKey);
        MessageBuilder<CancelJobRequestDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(msgHeaders);
        Message<CancelJobRequestDocument> msg = msgbuilder.build();
        //Message<CancelJobRequestDocument> msg = mControlRequestHandler.getResponseMessage(document, msgHeaders);
        Object responseObj = this.mControlRequestHandler.handleControlRequest(msg);
        Assert.assertTrue(responseObj instanceof Message);
        Message response = (Message) responseObj;
        Assert.assertTrue(response.getPayload() instanceof CustomFaultDocument);
    }


    @Test(enabled=true)
    public void testHandleControlRequest_UnsupportedTypeDocument() {
        // the handleControlRequest method does not handle SubmitJobRequestDocument/s 
        final SubmitJobRequestDocument document = SubmitJobRequestDocument.Factory.newInstance();
        MessageBuilder<SubmitJobRequestDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(msgHeaders);
        Message<SubmitJobRequestDocument> msg = msgbuilder.build();
        //Message<CancelJobRequestDocument> msg = mControlRequestHandler.getResponseMessage(document, msgHeaders);
        Object responseObj = this.mControlRequestHandler.handleControlRequest(msg);
        Assert.assertTrue(responseObj instanceof Message);
        Message response = (Message) responseObj;
        Assert.assertTrue(response.getPayload() instanceof CustomFaultDocument);
    }


}
