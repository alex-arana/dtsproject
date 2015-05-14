## Origins of DTS Schema ##

The plan for the DTS is to always be standards compliant. We found two OGF standards that sort of provided the DTS requirements for specifying and handling the data transfer jobs and they are, [OGSA-DMI](http://forge.gridforum.org/sf/projects/ogsa-dmi-wg) and [JSDL](http://forge.gridforum.org/sf/projects/jsdl-wg/).

We found the two schemas complementing each other where in the JSDL schema provided the schema for defining the job while OGSA-DMI specified how the clients would interact with the DTS service. We also found a few issues with the two schemas which we described in here http://code.google.com/p/dtsproject/wiki/DMI_JSDL_Issues. We are currently working with the standards authors to take in our requirements and extend the current schema. And as a first pass, while waiting for the current OGF schemas to cover all our requirements, we've used the two schemas, extended them, and defined the fields/elements missing from both of them.

The schema files that the DTS project can be found at http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources

  * `minx-jsdl.xsd` - the JSDL extensions we've defined on the project
  * `dmi-redef.xsd` - an extension of the DMI and redefinition of one of its elements since the original didn't provide extension points or `xsd:any`
  * `minx-dts-messages.xsd` - the schema that defines the WS operations and messages. It makes use of the elements/fields specified in the `minx-jsdl.xsd` and `dmi-redef.xsd`
  * `minx-jms-messages.xsd` - the schema that defines the JMS layer specific operations/messages. This is the schema that the DTS web service and DTS workernode uses to talk with each other. At the moment the schema only defines most of the operations/messages that the workernode sends back to the web service to update a job of it's status or to flag an error/exception if it occurs.

### DTS Job Definition Schema ###

[jsdl-redef.xsd](http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/jsdl-redef.xsd) is an extension of the original [jsdl schema](http://schemas.ggf.org/jsdl/2005/11/jsdl) with two of its elements (SourceTarget _Type and JobDescription_Type) redefined. The reason why they were redefined is because the extension points (`xsd:any`) had to be defined in [DTS' real schema extension to JSDL](http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/minx-jsdl.xsd). The XML parsers of the OXM (Object to XML Mapping) technologies we used like XMLBeans and JAXB complained if the child schema (the [DTS' real schema extension to JSDL](http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/minx-jsdl.xsd) for our case) had a redefinition of `xsd:any` on the elements that extend from the elements of the parent JSDL schema.


#### Elements/Types we reused from JSDL ####


  * **JobDefinition** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **JobDescription** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **JobIdentification** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **JobName** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **Description** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **JobAnnotation** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **JobProject** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **URI** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


  * **CreationFlag** - as defined in the [JSDL schema specification](http://www.ogf.org/documents/GFD.56.pdf)


#### New Elements/Types defined for the DTS Job Definition to use ####


  * **JobResourceKey** - the UUID that the DTS WS has generated for the job that it received. This key/jobID gets returned to the user so it could be used to query for the status of or suspend/resume/cancel the Job.


  * **TransferRequirements** - holds a few extra options on what the DTS WN should do with the job. An example of a field/element within the TransferRequirement is MaxAttempts which tells the DTS WN how many times it should retry a failed transfer before it gives up and tells the user that the transfer is unsuccessful.


  * **MyProxyToken** - The holder of the user's myproxy credential.


  * **OtherCredentialToken** - Other types of credentials that's supported by the service but not formally defined/named by the schema. At the moment, only MyProxyToken and UserToken (for username and password) are the two supported credential tokens of the DTS.


  * **Credential** - Think of this as the interface/abstraction of the MyProxyToken, UsernameToken, and OtherCredentialToken implemented.


  * **URIProperties** - An abstraction of the extra properties the user has to provide the DTS so the DTS WN can use those properties in accessing the source and/or destination.


  * **!GridFtpURIProperties** - An implementation (sort of) the URIProperties element which is used to provide additional information on how to access a !GridFTP server.


  * **SRBURIProperties** - Another implementation of the URIProperties element to provide SRB related properties.


  * **MinxSourceTargetType** - an extension of JSDL's SourceTarget _Type with the Credential and URIProperties as it's two extra elements._


  * **DataTransfer** - a wrapper element for every Source-Target pair. This is actually very similar to JSDL's DataStaging element. We decided to not use JSDL's DataStaging element as the name might give an idea to users that we're only staging data into the DTS WN and not transferring data from remote source to a remote destination.


  * **MinxJobDescriptionType** - an extension of JSDL's JobDescription _Type with the additional DataTransfer element_

#### References ####
  * http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/minx-jsdl.xsd
  * http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/minx-jsdl-example.xml
  * http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/jsdl-redef.xsd
  * http://schemas.ggf.org/jsdl/2005/11/jsdl
  * http://www.ogf.org/documents/GFD.56.pdf

### DTS WS Messaging Schema ###

The schema that the DTS client uses to talk to the DTS web service. This schema is also imported by the DTS WS' WSDL.
Elements/Types we have defined

  * **submitJobRequest** - the submit job request which takes in the DTS Job Definition document as a parameter and gets back a submitJobResponse which wraps the job ID for the submitted job.

  * **suspendJobRequest** - a request to suspend the job. This doesn't return a response. The only response this might get back is a fault if the job doesn't exist on the server (DTS).

  * **resumeJobRequest**- a request to resume the job.

  * **cancelJobRequest** - a request to cancel the job.

  * **getJobStatusRequest** - a request to get the status of the job.

  * **DtsFault** - the parent class of all DTS related faults

  * **InvalidJobDefinitionFault** - a fault that gets thrown the submitted job (thought its JobDefinition document) is invalid

  * **TransferProtocolNotSupportedFault** - a fault that gets thrown if the requested protocol specified in the Job Definition is not supported

  * **AuthenticationFault** - authentication fault

  * **AuthorisationFault** - authorisation fault

  * **NonExistentJobFault** - a fault that gets thrown if the job doesn't exists on the server

  * **JobStatusUpdateFault** -

  * **CustomFault** - other types of fault that is not covered by the above fault definitions.

### DTS JMS Messaging Schema ###

The schema used by any client (DTS WS, JSDL Portal, !GridSAM, etc) that wants to interact directly with the DTS Workernode. At the moment, this is mostly one way (WN to client client of communication)

  * **jobEventUpdateRequest** - a message that the WN sends its client to update the status of a data transfer job that it is processing. On the implementation side of things, this type of request gets sent to the client the moment the WN receives the job. It sort of lets the client know on the name of the WN host that would be processing the job. When the job successfully finishes, the WN will again send this message to the client. This message can also be used on updating the status of a job for every successful step (or small transfer task) the WN has finished.

  * **fireUpJobErrorEvent** - a message that the WN sends to its client at the end of processing the job only if the job didn't properly.

  * **fireUpStepFailureEvent** - a message that gets sent to the client whenever an error shows up in every step of transferring the job.

  * **JobEventDetail** - holds the detailed information of a status update.

  * **JobErrorEventDetail** - holds the detailed information of the job error event. Details include the class of the exception thrown on the WN side and it's corresponding stacktrace.