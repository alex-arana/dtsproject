We are currently in Development - sorry no releases yet. A number of TODOS remain before we can provide pre-built binaries for more widespread deployment and testing.

# Remaining Developer Todos #

## Broker/message flow/worker node ##

  * Change the format of the job request from the current xsd redef'd JSDL to the new DataCopy document (bulk data copy doc inspired by DMI). This will effect **all** the modules.

  * Complete the message exchange between a worker node client and the worker node. No probs/issues to fix here, rather there are just remaining todos to complete the message flow.

  * Update: jobQueueSender, controlQueueSender in order to pass down custom headers, including the message id.

  * Add an eventQueueListener (to the common module?) for worker node clients.

  * We **really** need a workernode client (such as the WebService) that can submit jobs to the broker, and filter for events/status updates. Currently lacking developer effort to do this !

## Batch Job ##

  * Fix the known commons-vfs-grid api bug associated with copying very large files with gridftp (this is not a bug with VFS or gridftp, rather its a bug with the commons-vfs-grid VFSUtil stuff - Dave knows about this).

  * Update the batch job so that different protocols can be selectively supported.

  * Update the batch job to support gridftp put/get rather than streaming (which is default).

  * Configure the batch job to persist credentials in the credentialStore (false by default). This is required in order to re-start jobs after hardware failure.