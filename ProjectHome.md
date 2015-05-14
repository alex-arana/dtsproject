## DTS was shelved due to resource/staffing issues and was not completed. ##

### DTS Overview ###
DTS WAS an open-source project. We were developing a) a document-centric message model for describing a 'bulk data transfer activity,' b) an accompanying set of platform independent components that can be scaled according to circumstance (worker nodes). The worker nodes broker the transfer of data between different storage resources as scheduled, fault-tolerant batch jobs. The architecture scales from small embedded deployments on a single computer to large distributed deployments through an expandable ‘worker-node pool’ controlled through message orientated middleware.

<table align='center;'>
<tr align='center;'>
<td align='center;'>
<img src='http://dtsproject.googlecode.com/files/andsArcs.jpg' align='center' width='400' height='100' />
</td>
</tr>
</table>

We would gratefully like to acknowledge the support of OMII-UK, the Australian Research Collaboration Service (ARCS), the Australian National Collaborative Research Infrastructure Strategy (NCRIS)and the UK National Grid Service.
<br />
<br />


### Requirement ###
Data are frequently spread across different administrative domains and often reside on a range of different resources such as GridFTP, SRB/iRODS, SRM, SFTP, FTP, WEBDAV, HTTP(s), and local FILE. When third party transfer is not available, copying potentially large quantities of data in a scheduled, scalable way between different resources is a challenge. Existing technologies do go some way to providing the required capabilities (e.g. 3rd party transfer between two GridFTP severs). However, support for the wide range of protocols that are currently in use across different institutions and scientific facilities remains limited.


### DTS Service (Asynchronous messaging and worker node pool) ###
The service being developed is based on well established enterprise message broker technologies and the Apache Commons Virtual File System<sup>1,2</sup>. VFS provides data streaming between incompatible protocols via byte IO and is highly extensible for new and emerging protocols<sup>2</sup>. Message brokering provides asynchronous communication and routing of transfer request messages to an expandable cloud of dedicated worker nodes implemented with VFS. If the service is being overburden and falls behind in its processing, all that is needed is to turn-up a few more worker instances to listen to the queue. Importantly, workers may be deployed at different institutions and within different network domains to a) improve resilience, since workers can be added and removed without affecting each other, and b) maximize transfer efficiency, since workers can be strategically placed to serve a particular data source and/or sink. In doing this, the DTS design meets geographical-topological concerns that may exist from a deployment perspective by allowing the hosting of such services to be either centralized (across multiple facilities) or confined to a single institution / network.

<sup>1</sup> http://commons.apache.org/vfs/ <br />
<sup>2</sup> http://sourceforge.net/projects/commonsvfsgrid/

### DTS Message Model ###
For the service messaging model, we have (for now) resorted to a composite schema for defining our message formats that combines both JSDL<sup>1</sup> and DMI<sup>2</sup> elements with our own proprietary extensions. Ideally, we would like to produce a standards compliant message format (rather than resorting to our proprietary format). We are currently working in this area to hopefully address this.
<br />
Please refer to our wiki for details: http://code.google.com/p/dtsproject/wiki/DMI_JSDL_Issues


### DTS Architecture ###

The basic components of the proposed system can be summed up as follows:<br /><br />

<img src='http://dtsproject.googlecode.com/files/dtsArch.png' height='600' width='750' />


#### DTS Client ####
Interacts with the user to allow the submission of data transfer operations. It allows the user to browse the source and target URIs so as to select the data staging elements that make a up a data transfer operation.

#### DTS Web Service ####
This is the external interface of the DTS system. It allows DTS clients to submit jobs, obtain progress information on existing jobs as well as to issue control requests (pause a job, abort a job etc). This module is also responsible for updating the job data store to reflect changes in the status of a given job.

#### DTS Workernode ####
This is the workhorse of the system. Its primary function is to process an incoming DTS message and to provide feedback to higher levels in the system on the status of such an operation. It is anticipated the DTS cloud will consist of 1 to ‘n’ DTS worker nodes.

### The Prototype ###
We have implemented a prototype for the DTS and [this wiki page](http://code.google.com/p/dtsproject/wiki/DTS_Prototype) will show you have to build and run it.

The DtsModules wiki lists all the modules that the DTS application uses.