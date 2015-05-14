A guide to getting the src code, building and testing.



This version of user guide is for Linux OS. In Windows, some changes are needed. For example, the user home directory is in C:\Documents and Settings\userxyz, the file path is "\" rather than "/" , and file copy command is "copy" rather than "cp". Use -Ddataminx.dir="C:/Documents and Settings/userxyz/.dataminx" format for command line input.

## Data Transfer Service ##

The Data Transfer Service is composed of a number of components and the sections below will show you how each of the DTS modules can be installed and used.


### Preinstall ###
#### Prerequisites ####
Following is a list of software components required to get the Data Transfer Service components running. Each item in the list contains a link to the product website where you can download the required distribution files:

  * [Java SE JDK](http://java.sun.com/javase/downloads/) Current release is [JDK 6 Update 18](http://java.sun.com/javase/6/webnotes/6u14.html)
  * [Subversion](http://subversion.tigris.org/getting.html)
  * [Maven](http://maven.apache.org/)
  * [ActiveMQ](http://activemq.apache.org/)


#### Getting The Code ####
The DTS codebase is currently hosted in the DTS Google Code SVN repository. You can check out the code by running:
```
  > cd /usr/local
  > svn checkout http://dtsproject.googlecode.com/svn/trunk/ minx-dts
```


### Install ###
#### Build the project ####
```
 > cd minx-dts
 > cp dts-batchjob/src/test/filters/filter.properties.template dts-batchjob/src/test/filters/filter.properties
 > cp dts-ws/src/test/filters/filter.properties.template dts-ws/src/test/filters/filter.properties
 > cp dts-workernode/src/test/filters/filter.properties.template dts-workernode/src/test/filters/filter.properties
 > cp dts-broker/src/test/filters/filter.properties.template dts-broker/src/test/filters/filter.properties
 > cp dts-ws/src/main/resources/dts-ws.properties.template ~/.dataminx/dts-ws.properties
 > mvn -DskipTests=true -DproxyHost=optionalHostName -DproxyPort=8080 install
```

### Postinstall ###
#### DTS Batch Job ####

Run the following commands...
```
 > cd dts-batchjob
 > cp -r src/main/resources/sample-batchjob-dot-dataminx-dir ~/.dataminx
```

Edit the values of the following fields in ~/.dataminx/dts-bulkcopyjob.properties
  * batch.jdbc.url (specify full path to your '~/.dataminx/batchjob-hsqlDB/dtsdb' directory)

Copy the testfiles.zip file to your `$HOME` directory, unzip and run tests:

```
 > cp ./src/test/resources/testfiles.zip ~
 > unzip ~/testfiles.zip
 > mvn -e -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx test
 > mvn -e -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx integration-test
```

You can edit 'testjob.xml' in src/test/resources/org/dataminx/dts/batch and add the source/destination you want to access then run the command below. Remember to add your credential details to filter.properties in src/test/filters if you don't want to put your credentials in the actual job document.
```
 > mvn -Ddataminx.dir=/<path-to-your-dot-dataminx-dir>/.dataminx -Dtest=QuickBulkCopyJobIntegrationTest test
```

#### DTS Batch Job Client ####
There might be times when you would only deploy the DTS Batch Job module and not worry about the other DTS modules (eg scheduler/broker, webservice, and workernode manager). There's an option for you as a user to just run the DTS Batch Job as a standalone application by building the client module. Here's how you can build and run it.
```
 > cd dts-batchjobclient
 > mvn clean
 > mvn assembly:assembly -Pdeploy-assembly1 -DskipTests=true
 > cd target/dts-batchjobclient-0.0.1-SNAPSHOT-dts-agent/lib
 > java -jar dts-batchjobclient-0.0.1-SNAPSHOT.jar <path-to-dts-job-def-doc>
```

#### DTS Workernode ####

Start ActiveMQ
```
 > $ACTIVEMQ_HOME/bin/activemq
```

Configure the dts-workernode
```
 > cd dts-workernode
 > cp src/main/resources/dts-workernode.properties.template ~/.dataminx/dts-workernode.properties
 > # edit ~/.dataminx/dts-workernode.properties if you need to. by default the values in the properties file should just work
 > # set value of wn.id to the name of the host running the dts-workernode module. This should be unique.
 > mvn -Ddataminx.dir=/home/dts-user/.dataminx exec:java
```

#### DTS Broker ####
Configure the broker by running the following commands:
```
 > cd dts-broker
 > cp src/main/resources/dts-broker.properties.template ~/.dataminx/dts-broker.properties
 > # edit si.jms.brokerDefaultOutputQueue's value so it reflects the queue which the workernode reads DTS jobs from
 > # for our case, the value of si.jms.brokerDefaultOutputQueue is dts.job.submit.queue
```

Assuming the ActiveMQ instance is still running from the configuration and deployment of the dts-workernode stage, run the following command to start Broker
```
 > mvn -Ddataminx.dir=/home/dts-user/.dataminx exec:java
```

To run a test against the broker simply type:
```
 > mvn -Ddataminx.dir=/home/dts-user/.dataminx test 
 > mvn -Ddataminx.dir=/home/dts-user/.dataminx integration-test
```

#### DTS WS ####

Edit src/test/filter/filter.properties and add the following lines
```
auth.username=test
auth.password=test
```
The two lines above is used to authenticate to the web service.

Setup the DB for DTS WS
```
 > cd dts-ws/src/main/resources/db
 > # the create-accts.sql probably won't have to be run anymore if the dts database
 > # has been precreated by going through the dts-batchjob module section above
 > mysql -u root < create-accts.sql
 >
 > mysql -u dts -D dts -p < create-tables.sql
```

Edit ~/.dataminx/dts-ws.properties and set the value of si.jms.jobSubmitQueueName to broker.job.submission.queue if you want the DTS-WS to use the broker in managing the scheduling of jobs or to dts.job.submit.queue if you want the job to go straight to the workernode.

Build the DTS Web Service
```
 > mvn -Ddataminx.dir=/home/dts-user/.dataminx -DskipTests=true package
```

Deploying the WS on Tomcat
  * Before you go ahead and follow the succeeding instructions, you might consider just running the DTS-WS as an embedded Jetty web application for test purposes. If you do, have a look at the "Running the DTS-WS as an embedded application inside Jetty" section on this page.
  * Open catalina.sh and add the following java system property to the script  `-Djava.security.auth.login.config="/<thedirectory-on-where-your-jaas-config-is-located>/jaas.config" -Ddataminx.dir=/<your-dataminx-config-directory"` for me, on my development box, `jaas.config` is in `/usr/local/minx-dts/dts-security/src/main/resources/jaas.config.default` and `dataminx.dir` points to `/home/dts-user`. Now start the tomcat server.
  * An alternative to specifying the `java.security.auth.login.config` is to edit `/usr/lib/jvm/java-1.6.0-sun-1.6.0.11/jre/lib/security/java.security` and add `url.1` as `${user.home}/.java.login.config`. Please note you'll need to copy `jaas.config.default` in the dts-security module to `~/.java.login.config`
  * Check if you can see the WSDL by going to http://localhost:18080/dts-ws/dts.wsdl. If you are not getting any 404 error message, the DTS-WS deployment worked for you.
  * Note that jaas.config.default file uses a clear text password file for authentication. Before running a test job on the web service, you'll need to put a file, `passwd` into your `$HOME/.dataminx` directory. passwd should contain this line:
```
test:test
```
  * (Optional) If you have an X509 cert and also have an access to the ARCS MyProxy repository, you can try using the `jaas.config.myproxy`. You need to start up the Tomcat server using `jaas.config.myproxy` as the parameter to `java.security.auth.login.config` system property. Then you'll need to change auth.username and auth.password to reflect the myproxy login and password you've used when you ran myproxy-init

Testing the DTS-WS
This test will submit a job to the WS which will then pass on the work to the broker. It is important that you have the WN and the Broker running before you try out this integration test. You can have a look at the DTS Workernode and Broker sections of this wiki to get more info on how to configure and run the workernode manager and broker
```
   > cd dts-ws
   > mvn -Ddataminx.dir=/home/dts-user/.dataminx integration-test
```

Running the DTS-WS as an embedded application inside Jetty (Optional).
You can use try this option if you don't feel like deploying the WS in Tomcat
```
  > cd ~/Workspaces/dataminx/minx-dts/dts-ws
  > mvn -Djava.security.auth.login.config="/<path-to>/jaas.config.default" -Ddataminx.dir=/home/gerson/.dataminx exec:java
```

#### FAQ ####
  * I'm getting this exception (`com.sun.xml.internal.messaging.saaj.soap.LocalStrings != com.sun.xml.messaging.saaj.soap.LocalStrings`) if I run both the portal and ws on a single tomcat instance http://forums.java.net/jive/thread.jspa?threadID=41696 use a newer version of saaj (> 1.3.1) and put in tomcat's common/endorsed directory





use the jaas.config.default in starting it up