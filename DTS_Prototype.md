

## DTS Prototype ##

A prototype of the Data Transfer Service has been commissioned and is currently being developed within the DataMINX project. Such prototype will allow the team to explore design alternatives and technologies that best meet the project goals.

### DTS Workernode ###
The prototype of the DTS Worker Node part of the DTS component implements some core technologies that have already been discussed in the project documentation including the [Java Messaging Service](http://en.wikipedia.org/wiki/Java_Message_Service) (JMS) and XML. In addition, the initial prototype of the DTS-WN uses elements of the [Spring Framework](http://www.springsource.org/) at its core. Most notably, the DTS-WN uses the following Spring modules:
  * [Spring Batch](http://static.springsource.org/spring-batch/) Spring Batch is a lightweight, comprehensive batch framework designed to enable the development of robust batch applications vital for the daily operations of enterprise systems. Spring Batch builds upon the productivity, POJO-based development approach, and general ease of use capabilities people have come to know from the Spring Framework, while making it easy for developers to access and leverage more advance enterprise services when necessary. Amongst other things, the Spring Batch framework allows developers to:
    * Handle large data by splitting it in small chunks
    * Restart a batch exactly where it has stopped
    * Define how many handled chunks per commit steps
    * Automatic Rollback / Retry in case of error
  * [Spring Integration](http://www.springsource.org/spring-integration) Spring Integration enables simple messaging within Spring-based applications and integrates with external systems via simple adapters. Those adapters provide a higher-level of abstraction over Spring's support for remoting, messaging, and scheduling.
  * Spring OXM This is a subproject of the larger [Spring Web Services](http://static.springsource.org/spring-ws/sites/1.5/) project and it provides bi-directional Object/XML Mapping (OXM) support.

#### Software Prerequisites ####
Following is a list of software components required as part of the DataMINX development environment. Each item in the list contains a link to the product website where you can download the required distribution files:

  * [Java SE JDK](http://java.sun.com/javase/downloads/) Current release is [JDK 6 Update 14](http://java.sun.com/javase/6/webnotes/6u14.html)
  * [Subversion](http://subversion.tigris.org/getting.html)
  * [Maven](http://maven.apache.org/)
  * [Pulse](http://www.poweredbypulse.com/download.php)

In addition, you will need access to an Oracle database. It is recommended that Oracle XE be installed in all development environments:

  * [Oracle XE](http://www.oracle.com/technology/software/products/database/xe/index.html)

#### Getting The Code ####
The DTS codebase is currently hosted in the DTS Google Code SVN repository. You can check out the code by running:
```
  > svn checkout http://dtsproject.googlecode.com/svn/trunk/ dtsproject
```

#### Building the project ####
Open a command prompt and change to the directory containing the working copy of the DTS checked out in the previous step. You can build the entire project using Maven with the following:
```
  > mvn -Dmaven.test.skip=true clean install
```

If your site uses a web proxy, you'll probably need to add the proxy related properties before you start the JVM..
```
  > mvn -Dmaven.test.skip=true -DproxyHost=hostname -DproxyPort=8080 clean install
```

#### Configuring the DTS-WN service ####
In order to configure the DTS Worker Node the following resources must be configured:

##### Application Directory #####
A directory containing specific configuration files that the application requires in order to launch. The application resolves the location of this directory by first looking for the Java system property `dataminx.dir`. Such a system property can be passed to the application as a command line parameter as demonstrated below:
```
  java -Ddataminx.dir=/home/alex/dataminx/config org.etc.DtsWorkerNodeCommandLineRunner
```
If this flag is not set, the application looks for the default dataminx configuration directory at the following location: `${user.home}/.dataminx`. The location of the `user.home` directories varies with the platform but in Linux this would be equivalent to `~/.dataminx`.

The application directory must contain the following files:
  * **`dts-workernode.properties`** This file contains a set of all properties required to configure the dts-workernode module including the spring batch module and its associated data source as well as the spring integration module and its associated JMS infrastructure. The following file (dts-workernode.properties) can be used as a template:
```
#########################################################################
#                       DTS-WorkerNode configuration
#

#
# Spring Batch configuration

# Oracle JDBC driver
#batch.jdbc.driver = oracle.jdbc.driver.OracleDriver

# HSQLDB JDBC driver
batch.jdbc.driver = org.hsqldb.jdbcDriver

# Oracle Database URL
#batch.jdbc.url = jdbc:oracle:thin:@localhost:1521:XE

# HSQLDB Database URL (standalone mode -- file)
batch.jdbc.url = jdbc:hsqldb:file:${java.io.tmpdir}/dtsdb

# Schema details
batch.jdbc.user = sa
batch.jdbc.password = 
batch.schema = 


#
# Spring Integration configuration

# URL for ActiveMQ running in stand-alone mode
si.jms.brokerURL = tcp://localhost:61616

# URL for ActiveMQ running in embedded mode
#si.jms.brokerURL = vm://localhost

# JMS listener container caching settings
si.jms.sessionCacheSize = 10
si.jms.cacheProducers = false

# DTS JMS Queue/Topic names
si.jms.jobSubmitQueueName = dts.job.submit.queue
si.jms.jobEventQueueName = dts.job.event.queue
si.jms.jobControlTopicName = dts.job.control.topic

#
# Additional configuration

# lifetime of MyProxy credentials (in hours, 0 for maximum)
default.myproxy.lifetime = 43200

# The number of parallel connections the job will try to use.
# Note that the value provided below might not be achieved if
# the server has connection restriction to the service that the
# job is trying to use
default.max.parallel.connections = 4

# Maximum size of all the files that will be transferred by the step
max.total.bytes.perstep = 10485760

# Maximum number of files a step will transfer
max.total.num.files.perstep = 3


# Anything equal to or higher than this value is considered a big file
min.bigfiles.bytes.size = 1024

# control logic for workernode manager
wn.manager.maxBatchJobNumber = 1

#default worker node ID in format: DtsWorkerNode + hostname + UUID
wn.id=DtsWorkerNodemyhostname001

#default message header name of worker node ID
wn.id.message.header.name=DTSWorkerNodeID


```

  * **`log4j.xml`** This file is optional and can be used to customise the amount and type of logging output by the application. Here's the example log4j.xml file included with the dts-common module:
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE log4j:configuration SYSTEM "http://jakarta.apache.org/log4j/dtd/log4j.dtd">

<!-- ===================================== -->
<!--          Log4j Configuration          -->
<!-- ===================================== -->
<log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">

  <!-- ============================== -->
  <!--        Console Appender        -->
  <!-- ============================== -->

  <appender name="CONSOLE" class="org.apache.log4j.ConsoleAppender">
    <param name="Target" value="System.out"/>
    <!--<param name="Threshold" value="INFO"/>-->

    <layout class="org.apache.log4j.PatternLayout">
      <param name="ConversionPattern" value="%d{ABSOLUTE} %-5p [%t] %C{1}: %m%n"/>
    </layout>
  </appender>

  <!-- ============================== -->
  <!--          DTS Log File          -->
  <!-- ============================== -->

  <!-- A time/date based rolling appender -->
  <appender name="FILE" class="org.apache.log4j.RollingFileAppender">
    <param name="File" value="minx-dts.log"/>
    <param name="Append" value="false"/>
    <param name="MaxFileSize" value="10MB"/>
    <param name="MaxBackupIndex" value="0"/>

    <layout class="org.apache.log4j.PatternLayout">
      <!-- The default pattern: Date Priority [Thread] Logger Message\n -->
      <param name="ConversionPattern" value="%d %-5p [%t] %C{1} - %m%n"/>
    </layout>
  </appender>


  <!-- ================ -->
  <!--    Thresholds    -->
  <!-- ================ -->

  <logger name="org.dataminx.dts">
    <level value="DEBUG"/>
  </logger>

  <logger name="org.springframework">
    <level value="INFO"/>
  </logger>


  <!-- ======================= -->
  <!--  Setup the Root logger  -->
  <!-- ======================= -->
  <root>
    <level value="INFO"/>
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
  </root>

</log4j:configuration>
```

##### Data Source #####
Currently, DTS-WN supports the following types of databases: Oracle, MySQL and HSQLDB. The relevant Spring Batch schema creation scripts for each of those database types are part of the DTS-WN distribution and must be run prior to attempting to run the application for the first time. You will find the database creation scripts in the following resource folder of DTS-WN: `src/main/resources/db`.

##### JMS Provider #####
Prior to running the application, all the required JMS infrastructure and its associated Queues/Topics must first be configured. Currently, DTS-WN supports only [ActiveMQ](http://activemq.apache.org/) as a JMS provider but that list will be expanded in due course. You can download the latest [ActiveMQ](http://activemq.apache.org/) distribution [here](http://activemq.apache.org/download.html).

Running [ActiveMQ](http://activemq.apache.org/) is extremely simple. Just download the latest distribution archive, unzip to a local directory and then run the startup script provided in the bin folder, `activemq.sh` or `activemq.bat`. By default, [ActiveMQ](http://activemq.apache.org/) will create the required Queues and Topics on first use if they do not yet exist (very convenient :).

#### Running the DTS-WN service ####
Just to re-emphasise what has already been said earlier, you need required infrastructure components to be configured and operational prior to running the DTS Worker Node. Those required components are:

  * Database with Spring Batch schema already created
  * JMS Provider (eg. ActiveMQ)

If you've already taken care of the prerequisites, let's get on with the show... In order to run the DTS Worker Node you must first deploy all required dependencies to the local repository. You can do this by running the following command at the root of the DTS project directory structure:
```
  > mvn install
```
Then, change to the dts-workernode folder and launch the DTS Worker Node using the following command:
```
  > cd dts-workernode
  > mvn exec:java
```
If you would like to specify the location of the application directory, otherwise known as `dataminx.dir`, you would specify it on the command line as follows:
```
  > mvn -Ddataminx.dir=/home/alex/dataminx/config exec:java
```
Finally, you can also specify additional options to Maven, such as heap thresholds etc., using the special environment variable `MAVEN_OPTS` as shown in the following example:
```
  > set MAVEN_OPTS=-Xms128m -Xmx512m -XX:MaxPermSize=128m   (Windows)
  > export MAVEN_OPTS=-Xms128m -Xmx512m -XX:MaxPermSize=128m   (Linux bash)
```

### DTS Web Service ###
#### Building and Developing the DTS WS Prototype ####

Please note that I make a few assumptions here that people looking and reading this page already is familiar with the tools/apps (Subversion, Maven, Eclipse, etc) we are using on the MINX project. Most or probably all MINX developers even have most of the prerequisite apps to get the WS prototype running. So only basic information will be provided here to get the WS interface up running on your environment.

Main Prerequisites
  * JDK 6 - in ubuntu, `apt-get install sun-java6-jdk`
  * [Pulse](http://www.poweredbypulse.com/download.php)
  * Maven - in ubuntu again, `apt-get install maven2`
  * Tomcat 6
  * !MySQL

Once Pulse is installed..
  1. install Eclipse 3.5 JEE + the following plugins (subclipse, checkstyle, m2eclipse, and others you feel like installing)
  1. The first time you run Eclipse (if you haven't installed any of the other MINX projects (ie ICAT, etc) yet, you need to use ~/Workspaces/dataminx as your workspace
  1. Get Tomcat integrated with Eclipse. Have a look [here](http://www.eclipse.org/webtools/jst/components/ws/1.0/tutorials/InstallTomcat/InstallTomcat.html) if you want a quick reference on how to get Tomcat running from within Eclipse.

#### Checking out the source from source code repository ####
Assuming you have Eclipse up and running, download the entire DTS project (even if we're only interested with WS bit) from the SVN repository using the maven plugin. Use the "checkout maven projects from SCM" and provide the URL below
```
http://dtsproject.googlecode.com/svn/trunk
```


#### One time installation of the JTA library ####
```
download jta-1.0.1B classes zipped file
unzip
jar

then..
mvn install:install-file -DgroupId=javax.transaction -DartifactId=jta -Dversion=1.0.1B -Dpackaging=jar -Dfile=jta-1.0.1B.jar
```

#### Integrating with Eclipse ####
Right click on each of the modules, go to maven, then select "update project configuration". The web enabled projects like dts-ws and dts-portal still will need to be wtp enabled. You can do it by...
```
  > cd ~/Workspaces/dataminx/minx-dts/dts-ws
  > mvn eclipse:eclipse -Dwtpversion=2.0
  > cd ~/Workspaces/dataminx/minx-dts/dts-portal
  > mvn eclipse:eclipse -Dwtpversion=2.0
```

#### Configuring the DTS-WS ####
DB setup for the WS
```
cd dts-domain/src/main/resources/db
mysql -u root < create-accts.sql
mysql -u dts -D dts -p < create-tables.sql
```

DB setup for the workernode (this is specific to mysql since it's the one I'm using on my desktop)
```
cd dts-workernode/src/main/resources/db
mysql -u dts -D dts -p < schema-mysql.sql
```

Edit the parameters of the filter.properties which the DTS-WS will use on the test job
```
cd minx-dts
cp dts-ws/src/test/filters/filter.properties.default dts-ws/src/test/filters/filter.properties
```

Copy the other config files into the ~/.dataminx directory
```
copy dts.properties to .dataminx
copy dts-workernode.properties to .dataminx
copy log4j.xml to .dataminx
```

#### Deploying the WS on Tomcat ####
  * Before you go ahead and follow the succeeding instructions, you might consider just running the DTS-WS as an embedded Jetty web application for test purposes. If you do, have a look at the "Running the DTS-WS as an embedded application inside Jetty" section on this page.
  * On Eclipse, from the servers view, add (with "Add Remove Projects") the dts-ws module (which is an Eclipse project now)
  * Run the server once, then stop it. Now go to run configurations of your tomcat server and add `-Djava.security.auth.login.config="/<thedirectory-on-where-your-jaas-config-is-located>/jaas.config" -Ddataminx.dir=/<your-dataminx-config-directory"` for me, on my development box, `jaas.config` is in `/home/gerson/Workspaces/dataminx-new/minx-dts/dts-security/src/main/resources/jaas.config.default` and `dataminx.dir` points to `/home/gerson.dataminx`. Now run it again.
  * An alternative to specifying the `java.security.auth.login.config` is to edit `/usr/lib/jvm/java-1.6.0-sun-1.6.0.11/jre/lib/security/java.security` and add `url.1` as `${user.home}/.java.login.config`. Please note you'll need to copy `jaas.config.default` in the dts-security module to `~/.java.login.config`
  * Check if you can see the WSDL by going to http://localhost:18080/dts-ws/dts.wsdl. If you are not getting any 404 error message, the DTS-WS deployment worked for you.
  * Note that jaas.config.default file uses a clear text password file for authentication. Before running a test job on the web service, you'll need to put a file, `passwd` into your `$HOME/.dataminx` directory. passwd should contain this line:
```
test:test
```
  * (Optional) If you have an X509 cert and also have an access to the ARCS MyProxy repository, you can try using the `jaas.config.myproxy`. You need to start up the Tomcat server using `jaas.config.myproxy` as the parameter to `java.security.auth.login.config` system property.

#### Testing the DTS-WS ####
This test will submit a job to the WS which will then pass on the work to the WN. It is important that you have the WN running before you try out this integration test. You can have a look on the DTSWorkerNodePrototype for more info on how to configure and get the DTS-WN module running.
```
   > cd dts-ws
   > mvn integration-test
```

#### Running the DTS-WS as an embedded application inside Jetty (Optional). ####
You can use try this option if you don't feel like configuring Tomcat from within Eclipse or you just want to quickly test the Data Transfer Service.
```
  > cd ~/Workspaces/dataminx/minx-dts/dts-ws
  > mvn -Djava.security.auth.login.config="/<path-to>/jaas.config.default" -Ddataminx.dir=/home/gerson/.dataminx exec:java
```

#### FAQ ####
  * I'm getting this exception (`com.sun.xml.internal.messaging.saaj.soap.LocalStrings != com.sun.xml.messaging.saaj.soap.LocalStrings`) if I run both the portal and ws on a single tomcat instance http://forums.java.net/jive/thread.jspa?threadID=41696 use a newer version of saaj (> 1.3.1) and put in tomcat's common/endorsed directory