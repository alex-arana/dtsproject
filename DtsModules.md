## DTS Modules ##

### build-tools ###

This module contains build related utilities that the project depends on. For example, it defines the checkstyle requirements that we DTS developers need to conform to in developing the project.

### dts-schema ###

This module defines the schema that the DTS uses in specifying a data transfer job and communications happening between the dts-wsclient and dts-ws AND dts-ws and dts-workernode modules. We used the OGF JSDL and OGSA-DMI schemas as references in defining the dts schema. To be standards compliant, we've extended from the two OGF schemas and defined our own elements/fields that the two standards didn't provide.

We've chosen to use XMLBeans as the Object XML mapping technology for this module as JAXB couldn't handle some of the complicated element definitions we have in the schema files. We first started with JAXB but when we realised that it wouldn't support our schema files, we moved into using XMLBeans. If you're interested to see the JAXB implementation of the dts-schema, you can find it in the dts-jaxb module.

The OriginsOfTheDtsSchema discusses more information about the DTS schema definition.

### dts-common ###

This module holds the commonly used classes that the DTS modules use. It includes XML processors, JMS message senders and receiveers, etc.

### dts-security ###

This module contains the security classes and configuration that the dts-ws and dts-portal use. The dts-workernode currently doesn't use the dts-security but it'll be a good idea to have it use this module instead of its own security related calls that it does when it processes a !GridFTP related job.

This module uses JAAS to make it easy for any of the other DTS modules using the dts-security to plugin other types of authentication and authorisation modules. As a part of this module, I've defined a MyProxy implementation of the AA module for JAAS to use.

### dts-workernode ###

The current version of the workernode that's committed in the google code repository can handle simple data transfer jobs. It supports !GridFTP, FTP, and file protocols. It can do file-to-directory, directory-to-directory, and file-to-file transfers.

It also has the capability to status updates of the job it is running. When the dts-workernode receives a job, it lets the dts-ws know which workernode ended up with the job. It then runs the job and sends back another message to the dts-ws once it's finished. A job may fail as well so the workernode will send step failure even message to the dts-ws at the time it first picked up an error in the job it is running. It'll try and continue running the job until a certain stage that it couldn't run it anymore. If this happens, it then sends a job failure event message to the dts-ws.

### dts-wsclient ###

The classes in this module used to be in dts-ws' module's test directory. I thought that it would be better to have this in a separate module as other DTS modules like the dts-portal can use it. The dts-ws can also import it and get it's test classes use it. The classes in this module is very specific to spring WS but should work if imported by any java project wanting to talk to the DTS WS.

### dts-ws ###

This module is the web service implementation of the DTS. Jobs submitted to the web service gets checked if it conforms to the dts schema. It also uses a credential interceptor to check if the message (SOAP) header contains the myproxy authentication information it requires from teh user submitting the job. If the user is allowed to submit a job, the dts-ws validates if the user's job definition document. It's possible for a job to be validate against a schema but not to the more specific requirements we have set to each of the elements in the schema. The dts-ws then tries and submit the job to the dts-workernode. If it didn't get any exception at the time of submission, it logs the new info about the job into the job DB and returns the job's id to the user submitting the job.

The dts-ws can also be queried for the status of the jobs submitted to it. It cannot support job suspension, resumption, and cancellation yet as the workernode will need to do this as well.

The dts-ws also has an embedded jetty implementation which will let you run it as a standalone server without having to deploy it as a war on a tomcat or any web application server.

### dts-portal ###

There was a need for the DTS to support shibboleth. I've written a struts 2 application that can submit a job to the DTS WS. It's not pretty, but it does its job, which is to demonstrate that the whole DTS system works. We've started work on AJAXifying the dts-portal using Ext GWT (or GXT) which is also on this same module.