Maven Build, install, test instructions
=========================================

The dts-batchjob is the long-running batch process that copies data from the different
sources and sinks as defined in a datacopy activity document. The batchjob is multi-threaded
and and can be configured as required (e.g. to restrict the size of the data that can be
copied and the number of parallel threads). The batchjob requires a db in order to store/
checkpoint its state as the job runs. Be default, no credentials are written to the
database (a new persistent credentialStore impl is required for this - TODO).
A platform independent HSQLDB is provided by default. It is pre-configured and can
be used for testing and lightweight deployments.


a) Recursivley copy and rename the 'sample-batchjob-dot-dataminx-dir' dir to your chosen location
--------------------------------------------------------------------------------------------------
Default is '$HOME/.dataminx', e.g:
  cp -r src/main/resources/sample-batchjob-dot-dataminx-dir ~/.dataminx


b) Edit the 'batch.jdbc.url' property in your 'dts-bulkcopyjob.properties' file
-------------------------------------------------------------------------------
Provide the FULL path to your dataminx dir that you copied above, e.g:
  'batch.jdbc.url=jdbc:hsqldb:file:<provide-full-path-to-your-dataminx-dir>/batchjob-hsqlDB/dtsdb'.

    This points to a pre-configured embedded database for use by the batchjob.
    It is located in the 'batchjob-hsqlDB' directory and is used for testing and for lightweight batchjob deployments.
    Of course, you can configure the batchjob to run against a different database as required
    (we strongly recommend you do this for production deployments). Because the
    db is an embedded db, only a single batchjob JVM process will be able to connect to this database
    (no other concurrent connections from different JVMs/processes are allowed - however
    concurrent thread connections spawned from the same JVM/process are allowed such as the multi-threaded worker-node).
    Refer to the dts-bulkcopyjob.properties for instructions on deploying to different databases.



c) Copy the testfiles.zip file to your $HOME directory and unzip
----------------------------------------------------------------
This will create the 'testfiles' directory in your $HOME dir which are required
for the tests.
  cp ./src/test/resources/testfiles.zip ~
  unzip ~/testfiles.zip


d) Build and install (skip integ tests)
----------------------------------------
   mvn -DskipTests=true install


e) Run Unit and Integration tests
---------------------------------
  mvn -e -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx test
  mvn -e -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx integration-test


f) Custom quick copy integration test
-------------------------------------
You can edit 'testjob.xml' in src/test/resources/org/dataminx/dts/batch and add
the source/destination you want to access then run the command below.
Remember to add your credential details to filter.properties in src/test/filters
if you don't want to put your credentials in the actual job document:
  mvn -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx -Dtest=QuickBulkCopyJobIntegrationTest test



[INFO] snapshot org.apache.commons:commons-vfs-project:2.0-SNAPSHOT: checking for updates from apache.org_snapshot
[WARNING] repository metadata for: 'snapshot org.apache.commons:commons-vfs-project:2.0-SNAPSHOT' could not be retrieved from repository: apache.org_snapshot due to an error: Error transferring file: Connection timed out
[INFO] Repository 'apache.org_snapshot' will be blacklisted
