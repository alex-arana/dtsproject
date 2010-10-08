Maven Build, install, test instructions
=========================================

The dts-batchjob is the long-running batch process that copies data from the different
sources and sinks as defined in a datacopy activity document. The batchjob is multi-threaded
and and can be configured as required (e.g. to restrict the size of the data that can be
copied and the number of parallel threads). The batchjob requires a db in order to store/
checkpoint its state as the job runs. By default, no credentials are written to the
database (a new persistent credentialStore impl is required for this - TODO).


- A platform independent in-memory HSQLDB is configured by default for testing purposes
  (Do not use this in production ! Please configure a db for use in production)

- Note, you can use the pre-packaged platform independent [file-based] HSQL
  Database provided in this download (see below for configruation instructions).



1) Recursivley copy + rename 'src/main/resources/sample-batchjob-dot-dataminx-dir' directory to your chosen location
====================================================================================================================
Default is '$HOME/.dataminx' (this dir will be refered to as '$DATAMINXDIR') e.g:

  cp -r src/main/resources/sample-batchjob-dot-dataminx-dir ~/.dataminx




2) DB configuration (optional - only required for production deployments)
=========================================================================
(note, configuring an external db is not required for testing as an in-memory HSQL db is configured
 by default - but Do Not use the in-mem db for production use !)

  i) Edit your '$DATAMINXDIR/batch-job-db-context.xml'file and un-comment either the
     'Pooled DB connection' dataSource or the 'Single DB connection' dataSource.

  ii) Edit your '$DATAMINXDIR/dts-bulkcopyjob.properties' and modify the DB
      config properties as required (please refer to 'dts-bulkcopyjob.properties'
      for more instructions).

      To use the pre-packaged, platform independent [file-based] HSQL db:
      -------------------------------------------------------------------
      You can use a pre-packaged 'embedded' HSQL database for the batchjob.
      It is located in the '$DATAMINXDIR/batchjob-hsqlDB' directory and is used for testing and for lightweight batchjob deployments.
      Of course, you can configure the batchjob to run against a different database as required
      (we recommend you do this for production deployments). Because the
      db is an embedded db, only a single batchjob JVM process will be able to connect to this database
      (no other concurrent connections from different JVMs/processes/tools are allowed.
      Refer to the dts-bulkcopyjob.properties for instructions on deploying to different databases.

      - Do i) above.
      - Edit the 'batch.jdbc.url' property in '$DATAMINXDIR/dts-bulkcopyjob.properties'
        and provide the FULL path to your $DATAMINXDIR directory.
      - Make sure 'batch.jdbc.driver = org.hsqldb.jdbcDriver' property is set.




3) Copy the testfiles.zip file to your $HOME directory and unzip
=================================================================
This will create the 'testfiles' directory in your $HOME dir which are required
for the tests.
  cp ./src/test/resources/testfiles.zip ~
  unzip ~/testfiles.zip



4) Build and install (skip integration tests)
=============================================
   mvn -DskipTests=true install



5) Run Unit and Integration tests
==================================
  mvn -e -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx test
  mvn -e -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx integration-test



6) Customize and run quick copy integration test
================================================
You can edit 'testjob.xml' in src/test/resources/org/dataminx/dts/batch and add
the source/destination you want to access then run the command below.
Remember to add your credential details to filter.properties in src/test/filters
if you don't want to put your credentials in the actual job document:

  mvn -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx -Dtest=QuickBulkCopyJobIntegrationTest test


