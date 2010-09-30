Maven Build, install, test instructions
=========================================

a) Recursivley copy the 'src/main/resources/sample-ws-dot-dataminx-dir' directory 
to your chosen location (default is '$HOME/.dataminx'), e.g:
  cp -r src/main/resources/sample-ws-dot-dataminx-dir ~/.dataminx

b) Edit 'dts-ws.properties' (copied in above dir copy).
As a minimum, Change the 'datasource.url' property to point to the location of your
'.dataminx' directory.


b) Edit the 'datasource.url' property in your 'dts-ws.properties' file (as copied above):
Provide the FULL path to your dataminx dir that you copied above, e.g:
  'datasource.url=jdbc:hsqldb:file:<provide-full-path-to-your-dataminx-dir>/ws-hsqlDB/ws-hsqlDB'.

The datasource.url points to a pre-configured default database for use by the WS.
It is located in the 'batchjob-hsqlDB' directory and is used for testing and for lightweight WS deployments.
Of course, you can configure the WS to run against a different database as required.
Refer to the dts-ws.properties for config instructions.



c) Build and install in your local .m2 repo (skip integ tests)
  mvn -DskipTests=true install

e) Run Unit tests (does not require WS running or running DB):
  mvn test


f) Run Integration tests. 
Requires the ActiveMQ broker and an instance of the WS to be up and running (and thus the ws-DB):
  mvn -Ddataminx.dir=/path-to-dot-datataminx-dir/.dataminx integration-test

g) Running the WS:

  1) Via jetty plugin
  mvn -Djava.security.auth.login.config=/home/djm76/programming/java/dts/dtsproject/dts-security/src/main/resources/jaas.config.default -Ddataminx.dir=/path-to-dot-datataminx-dir/.dataminx -DproxyHost=wwwcache.dl.ac.uk -DproxyPort=8080 exec:java

  2) Via .war file

