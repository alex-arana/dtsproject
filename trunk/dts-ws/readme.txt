Maven Build, install, test instructions
=========================================

The dts-ws module contains the Web Service that provides a platform agnostic interface
to submitting dts jobs to the dts broker (and onto a workernode/batchjob). The WS is
built as a standard .war file. The WS requires access to a relational db in order to
store job submission requests and to store asychronous status updates recieved from the dts-broker.
A platform independent HSQLDB is provided by default. It is pre-configured and can
be used for testing and lightweight deployments. 


a) Recursivley copy and rename the 'src/main/resources/sample-ws-dot-dataminx-dir' dir to you chosen location:
Default is '$HOME/.dataminx' e.g:
  cp -r src/main/resources/sample-ws-dot-dataminx-dir ~/.dataminx

b) Edit the 'datasource.url' property in your 'dts-ws.properties' file (as copied above).
Provide the FULL path to your dataminx dir that you copied above, e.g:
'datasource.url=jdbc:hsqldb:file:C:/Documents and Settings/<provide-full-path-to-your-dataminx-dir>/ws-hsqlDB/ws-hsqlDB'


This points to a pre-configured default database for use by the WS.
It is located in the 'ws-hsqlDB' directory and is used for testing and for lightweight WS deployments.
Of course, you can configure the WS to run against a different database as required.
Refer to the dts-ws.properties for config instructions.



c) Build and install in your local .m2 repo (skip integ tests):
  mvn -DskipTests=true install

e) Run Unit tests (does not require WS running or running DB):
  mvn test


f) Run Integration tests. 
Requires the ActiveMQ broker and an instance of the WS to be up and running (and thus the ws-DB):
  mvn -Ddataminx.dir=/path-to-dot-datataminx-dir/.dataminx integration-test

g) JAAS config and security settings TODO

*) Running the WS TODO
  1) Via jetty plugin:
  mvn -Djava.security.auth.login.config=/home/djm76/programming/java/dts/dtsproject/dts-security/src/main/resources/jaas.config.default -Ddataminx.dir=/path-to-dot-datataminx-dir/.dataminx -DproxyHost=wwwcache.dl.ac.uk -DproxyPort=8080 exec:java
  2) Via .war file

