# DMI Discussion Points / Possibilities/ Proposals #

## Intro ##
We are implementing a scalable data transfer service that performs recursive directory copying between different (incompatible) storage/file systems, including, GridFTP, iRODS, SRB, FTP, SFTP, HTTP(S), local FILE, WEBDAV?.
The service is based on an architecture that deploys data transfer worker agents implemented using Apache Virtual File System (VFS) into a fully scalable worker node pool. Remote worker nodes are invoked via asynchronous message queues, which are implemented using JMS (i.e. asynchronous point-to-point message channels). Worker agents can be installed within a particular network topology, the only requirement is that a worker can access the (remote) trusted message broker. In doing this, workers can be strategically deployed at or close to a particular data source and/or sink (i.e. facilitating both access to the data and for improving transfer efficacy).

## Proposed Message Format / Messaging Model ##
To implement this service, we require a message format for describing a data transfer that may consist of multiple data sources and sinks. At present, we have proposed two potential composite schemas, one extends DMI<sup>1</sup>, the other modifies JSDL HPC file staging profile<sup>2</sup> . Ideally, we would like to produce a single standards compliant message format.
This wiki page identifies some issues/discussion points and proposals.

<sup>1</sup>OGSA Data Movement Interface: http://www.ogf.org/documents/GFD.134.pdf
<br />
<sup>2</sup>JSDL HPC File Staging Profile: http://www.ogf.org/documents/GFD.135.pdf


Two key requirements of the message format are:
  1. All messages must be fully self contained and place no assumptions on the underlying transport mechanism so that they are fully transport agnostic. The messaging model should be applicable for use with the following delivery methods:
    * used within the soap bodies of doc-literal Web Service invocations/responses
    * posted to a RESTfull endpoint
    * posted to/from an asynchronous message channel (e.g. JMS queue/topic)
    * email
  1. We need to define multiple data transfers in the same request message.




# Proposals #
To do this, we devised the following proposals which ideally need to be **unified into a single common Bulk Data Copy Activity Document**:
  * **1) DMIB Proposal (DMI for _Bulk_-transfers)** (see below)
<br />
A 'dmi-wrapped' rendering proposal that effectively combines a source and a sink DEPR within a single element which can be defined multiple times in a single request/packet. This is very similar to the `<jsdl:DataStaging>` elements that can be defined multiple times within a single JSDL document. Some other stuff too.


  * **2) JSDL Data _Transfer_ proposal** (see below)
<br />
This proposal modifies the HPC file staging profile and adds some extra information (inc. the MinxTransferRequirementsType which itself extends the DMI TransferRequirementsType, a re-positioned Credential element within the src and target URI element, and an abstract URIProperties element that can be used to define additional information for connecting to the the src/target URI). Some other stuff too.

<br />


&lt;hr/&gt;


<br />

# 1) DMIB Proposal Schema #
(DMIB for _Bulk_-transfers) Note, this proposal does not require modification to the existing DMI spec (other than the addition of some `<xsd:any/>` extension points), rather, it builds on the DMI spec by re-using the existing elements within a new 'wrapped' schema.
This proposal effectively wraps together the SourceDEPR and SinkDEPR elements (both `<wsa:EndpointReferenceTypeS>`) with the `<dmi:TransferRequirements>` into a single wrapping element. This ‘wrapping’ element can be defined multiple times in a single request packet in order to define a transfer that caters for multiple data sources and sinks. (The `<dmi:DataLocations> <dmi:Data>` elements are re-used as per the dmi spec). Semantically, this appears to be similar to defining multiple source and target 

&lt;jsdl:DataStaging/&gt;

 element combination's.

<br />
  * Proposal schema: http://code.google.com/p/dtsproject/source/browse/trunk/dts-jaxb/src/main/resources/archive/dmi-wrappedMessagesProposals.xsd

  * Doc example: http://code.google.com/p/dtsproject/source/browse/trunk/dts-jaxb/src/main/resources/archive/dmi-WrappedDataTransferRequest.xml
<br />
<br />

## DMIB Proposal features ##

### Use of JobID to enable Doc-literal/RESTfull renderings ###
We require a document-literal type approach that does not use WSRF service factories and instances that are accessed via WS-Addressing Endpoint References.

The key difference between this document-centric rendering (above) and the current DMI WSRF approach, is that the doc-centric approach **passes a JobID back to the client rather than a WS-Addressing Endpoint Reference** (which point to a DMI service Instance previously created from a factory). In addition, since there is no separate service instance, the client must submit the JobID to the service on each subsequent status request/query.

### Multiple transfers ###
The current DMI functional spec is used to define a single data transfer between a single source and a single sink. A single data aggregate, such as a directory that resides at one sink can be specified in the [EPR](data.md) (e.g. by appending the '/' char to an EPR to identify a directory). However this appears to account for only one directory. We have a requirement to be able to define multiple files and/or directories, potentially residing at different locations.
In order to accommodate multiple transfers in DMI, the client interacts with a DMI factory to create multiple service instances, each responsible for a separate transfer (please correct me/comment on this if i am wrong!). Potentially, this can place a large communication overhead on the client since many interactions are required between the client and service (e.g. consider the case when copying many ~1000 different files from a number of different sources).

We therefore need to be able to define multiple data transfers within the same request message/packet so that the client can be very thin, i.e. fire a single request and periodically poll for status updates (see http://www.eaipatterns.com/MessagingComponentsIntro.html for the 'atomic' message/packet definition).

To do this, we have devised the following 'dmi-wrapped' rendering proposal that effectively combines a source and a sink DEPR within a single element which can be defined multiple times in a single request/packet. This is very similar to the `<jsdl:DataStaging>` elements that can be defined multiple times within a single JSDL document. See pseudo code-frag below;

```
    <!--
    Pseudo schema request:
    ======================
    -->
    <dmi-msg:SubmitWrappedDataTransferRequestMessage>
        <dmi-msg:Start/>
        <dmi-msg:WrappedSourceSinkDEPRs>
            <!-- 
            <dmi-msg:SubTransferID> 
                client assigned id, see below discussion. should this be client or 
                service assigned.    
            </dmi-msg:SubTransferID>
            -->
            <dmi-msg:SourceDEPR></dmi-msg:SourceDEPR>
            <dmi-msg:SinkDEPR></dmi-msg:SinkDEPR>
            <dmi-msg:TransferRequirements/>
        </dmi-msg:WrappedSourceSinkDEPRs>
        <dmi-msg:WrappedSourceSinkDEPRs>
            <!-- <dmi-msg:SubTransferID> see below discussion </dmi-msg:SubTransferID>-->
            <dmi-msg:SourceDEPR></dmi-msg:SourceDEPR>
            <dmi-msg:SinkDEPR></dmi-msg:SinkDEPR>
            <dmi-msg:TransferRequirements/>
        </dmi-msg:WrappedSourceSinkDEPRs>
        <dmi-msg:WrappedSourceSinkDEPRs>
            <!-- <dmi-msg:SubTransferID> see below discussion </dmi-msg:SubTransferID>-->
            <dmi-msg:SourceDEPR></dmi-msg:SourceDEPR>
            <dmi-msg:SinkDEPR></dmi-msg:SinkDEPR>
            <dmi-msg:TransferRequirements/>
        </dmi-msg:WrappedSourceSinkDEPRs>
        
    </dmi-msg:SubmitWrappedDataTransferRequestMessage>

    <!--
    Pseudo schema response:
    =======================
    -->
   <dmi-msg:GetWrappedDataTransferInstanceResponseMessage>
       <dmi-msg:JobID>jobid-adfafq24-59-4-13</dmi-msg:JobID>
   </dmi-msg:GetWrappedDataTransferInstanceResponseMessage>
```

### Service-Assigned BulkJobID and Client-Assigned sub-transferID ###

**Note this discussion also relates to the modified file staging proposal - both will probably require some form of composite id (bulk and sub-transfer idS) for drilling down to query the status of a single sub-transfer.**

At present the proposal above returns a single JobID for the whole 'bulk' transfer (since a bulk transfer can be made up of multiple sub transfers). This implies that any subsequent request that uses this JobID (e.g. a request for state, or a request for the dmi instance attributes document) refers to the state of the whole 'bulk' transfer. For example, the total number of bytes copied would be calculated across all the sub-transfers. Similarly, if one sub-transfer failed out of ten, then the state of the bulk transfer would be 'failed'. Note, this has similar semantics to defining an aggregate data EPR that represents a single directory (as per current DMI spec).


The proposal could certainly be extended further if it is a (probable) requirement to drill down and query the state of each separate sub-transfer. To do this, the request data model could be extended to incorporate an additional user defined sub-transfer id/alias (i.e. a unique element or attribute) defined for each separate sub-transfer. In doing this, any subsequent request made by the client could use a combination of the JobID and the sub-transfer id to isolate that specific sub-transfer, e.g. `('<JobID>:<subTransferID>')`. The returned state would then represent only that sub-transfer as per the dmi spec.

**Note**, in this scenario, the client should? assign the sub-transfer ids/alias while the service assigns the bulk transfer JobID. This is because:
  * We do not want to place any significance on the ordering of the sub-transfers in the request.
  * Clients can assign meaningful names to sub-transfers (e.g. 'BulkJobIDxyz:myKnownCriticalFile').
  * XSD constraints could be used to ensure that the sub-transfer id is unique within the context of the xml doc (whether element or attribute value).
  * The sub-transferID could be made optional so that the service could fall-back to automatically assigning these ids if not specified by the client.


**Note**, something like this would be a requirement if the multi-transfer request is split into separate message packets by the service and each sub-transfer packet is processed separately, rather than sending the whole composite data transfer request to the JMS queue to be processed by a single worker. See message splitter and aggregator patterns (http://www.eaipatterns.com/Sequencer.html, http://www.eaipatterns.com/Aggregator.html).


### Addition of JSDL CreationOption to DMI TransferRequirements ###
Add the the `<jsdl:CreationFlag>` element, which defines overwrite, dontOverwrite and append enums, to the `<dmi:TransferRequirementsType>`, e.g. something like the following, but define the CreationFlag within the dmi namespace ?

```
        <dmi-msg:TransferRequirements>
            <!--<dmi:StartNotBefore></dmi:StartNotBefore>
            <dmi:EndNoLaterThan>6</dmi:EndNoLaterThan>
            <dmi:StayAliveTime>6</dmi:StayAliveTime>-->
            <dmi:MaxAttempts>2</dmi:MaxAttempts>
            <!-- define CreationFlag in dmi ns ? --> 
            <jsdl:CreationFlag>overwrite</jsdl:CreationFlag>
        </dmi-msg:TransferRequirements>

```



### New Extension points in DMI schema ###
A number of xsd extension points (xsd:any) are required in the current dmi functional spec, e.g in the following DMI elements
  * `<dmi:Data/>` element
  * list elements here - is it a good idea to add `<xsd:any/>` to all dmi complex types much like JSDL ?

<br />


&lt;hr/&gt;


<br />

# 2)JSDL File Transfer proposal #
The JSDL file _staging_ profile could potentially be used to implement a data transfer style service, but this spec is focused mostly on staging to/from an intermediary compute resource rather than transfer between source and sink. We have used `<xsd:redef/>` to extend/modify the file staging profile so that it is more suitable for use in a bulk data transfers.
<br />
<br />
  * Proposal schema: http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/minx-jsdl.xsd

  * Doc example: http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources/minx-jsdl-example.xml
<br />

### Vanilla HPC file staging profile ###

The HPC file staging profile supports a credential nested within the `<DataStaging>` element. However, this supports only one credential, either for the `<Source>` or `<Target>`, not both. This is by design, **it serves the purpose of staging files to an intermediary (i.e. the compute resource). Data can then be optionally staged from the compute resource following job completion. In order to perform a data transfer between a source and sink, a file/dir can be linked to both the source and sink by defining two DataStaging elements that define the same `<jsdl:FileName/> and, optionally <jsdl:FileSystemName/>` elements as below**.

```
<!-- stage to compute -->
<DataStaging>
    <FileName>fileA.txt</FileName>
    <CreationFlag>overwrite</CreationFlag>
    <Source>
       <URI>ftp://server1.inthe.sky:1234</URI>
    </Source>
    <Credential xmlns="http://schemas.ogf.org/hpcp/2007/11/ac">
      <UsernameToken xmlns="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
        <Username>hi</Username>
        <Password>world</Password>
      </UsernameToken>
    </Credential>
</DataStaging>


<!-- stage same file from compute -->
<DataStaging>
    <FileName>fileA.txt</FileName>
    <CreationFlag>overwrite</CreationFlag>
    <Target>
       <URI>ftp://server2.inthe.sky:1234</URI>
    </Target>
    <Credential xmlns="http://schemas.ogf.org/hpcp/2007/11/ac">
      <UsernameToken xmlns="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
        <Username>demo</Username>
        <Password>pass</Password>
      </UsernameToken>
    </Credential>
</DataStaging>
```

## JSDL Data Transfer Proposal features ##
While the vanilla hpc file staging profile serves the purpose of staging data to and from a compute resource, **the notion of the intermediary (i.e. stage to and from the compute resource) is redundant in a service that is focused on data transfer/copying,** i.e. linking the source and target destinations on the intermediary by using `<jsdl:FileName/> and <jsdl:FileSystem/>` elements is unnecessary overhead. Even if a service implementation effectively _does_ perform a 'Get' and a 'Put' using an intermediary, this is an implementation detail and should not be exposed in a service contract. To get round this issue, we re-defined the jsdl hpc file staging schema slightly (using `<xsd:redef/>`) to override and modify the existing jsdl schema to:

  1. redefine `<jsdl:DataStaging>` as `<jsdl:DataTransfer>`
  1. allow the nesting of credentials in the source and target elements (as below)
  1. add an abstract URIProperties element required to define additional necessary information for the service to contact the data source and sink, e.g. srb requires McatZone, MdasDomain information. The URIProperties element is abstract so that it can be implemented for different protocols using a substitution group. So far, we have defined GridFTPURIProperties and SrbProtocolProperties used to define additional information (e.g. PortRange, MdasDomain, McatZone etc).
  1. addition of a modified TransferRequirements element (modified from dmi:TransferRequirements to add the `<jsdl:CreationFlag/>`)
  1. add the client defined sub-transfer id for each sub-transfer (please refer to the discussion above on service-assigned bulkTransferId and client-assigned subTransferId)



**JSDL data transfer example:**
```
   <mjsdl:DataTransfer>

      <!-- source -->
      <mjsdl:Source>
        <jsdl:URI>srb://ng2.vpac.org/etc/termcap</jsdl:URI>
        <mjsdl:Credential>
            <mjsdl:MyProxyToken>
                <mjsdl:MyProxyUsername>${myproxy.username}</mjsdl:MyProxyUsername>
                <mjsdl:MyProxyPassword>${myproxy.password}</mjsdl:MyProxyPassword>
                <mjsdl:MyProxyServer>myproxy2.arcs.org.au</mjsdl:MyProxyServer>
                <mjsdl:MyProxyPort>7512</mjsdl:MyProxyPort>
            </mjsdl:MyProxyToken>
        </mjsdl:Credential>
        <!--
          define protocol specifics by using abstract URIProperties and xsd 
          substitution group
         -->
         <mjsdl:SrbProtocolProperties>
              <mjsdl:McatZone>mymcatZone</mjsdl:mjsdl:McatZone>
              <mjsdl:MdasDomain>my mdas domain </mjsdl:MdasDomain>
              <!--<mjsdl:HomeDirectory>/home/whatever</mjsdl:HomeDirectory>
              <mjsdl:DefaultResource>ng2.vpac.org</mjsdl:DefaultResource>-->
              <mjsdl:PortRange>
                  <mjsdl:portMin>6400</mjsdl:portMin>
                  <mjsdl:portMax>6500</mjsdl:portMax>
              </mjsdl:PortRange>
         </mjsdl:SrbProtocolProperties>       
      </mjsdl:Source>
 
     <!-- 
      no requirement to link source and target on the intermediary compute resource 
      jsdl:FileName 
     --> 

      <!-- target -->
      <mjsdl:Target>
        <jsdl:URI>ftp://dm11.intersect.org.au/upload/pom-from-pub.xml</jsdl:URI>
        <mjsdl:Credential>
            <wsse:UsernameToken>
              <wsse:Username>${ftp.username}</wsse:Username>
              <wsse:PasswordString>${ftp.password}</wsse:PasswordString>
            </wsse:UsernameToken>
        </mjsdl:Credential>
      </mjsdl:Target>

      <!-- 
      DMI redefined transfer requirements 
      -->
      <mjsdl:TransferRequirements>
        <dmi:MaxAttempts>3</dmi:MaxAttempts>
        <jsdl:CreationFlag>overwrite</jsdl:CreationFlag>
      </mjsdl:TransferRequirements>

      <!-- 
      <SubTransferID> 
         client-assigned sub-transfer id? should this be defined in the transfer 
         requirements ? see above discussion 
      </SubTransferID> 
      -->

    </mjsdl:DataTransfer>

```

**Note, the bulk JobID and sub-transferID arguments also applies to this proposal - please see above section**


Please take a look at for more details: <br />
http://code.google.com/p/dtsproject/source/browse/trunk/dts-schema/src/main/resources
