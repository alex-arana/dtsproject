Maven Build, install, test instructions
=========================================

There might be times when you would only deploy the DTS Batch Job module and
not worry about the other DTS modules (eg scheduler/broker, webservice, and
workernode manager). There's an option for you to just run the DTS Batch Job as
a standalone command line application by building this client module.
Here's how you can build and run it: 

 cd dts-batchjobclient
 mvn clean
 mvn assembly:assembly -Pdeploy-assembly1 -DskipTests=true
 cd target/dts-batchjobclient-0.0.1-SNAPSHOT-dts-agent/lib
 java -jar dts-batchjobclient-0.0.1-SNAPSHOT.jar <path-to-dts-data-copy-activity-document>

IMPORTANT: By default, the batch-job module comes with an embedded HSQLDB database. Only
a single JVM process may connect to this embedded database. Therefore, you can't
run multiple executions of the same batch-job application while the batch-job application is running.