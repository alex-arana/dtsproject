package org.dataminx.dts.client;

import java.io.IOException;
import org.dataminx.schemas.dts._2009._05.dts.*;

import org.w3c.dom.Document;

public interface DataTransferServiceClient {
	public String submitJob(Document dtsJob) throws IOException;
}
