Maven Build, install, test instructions
=========================================

The dts-ws module contains the Web Service for submitting
dts jobs to the dts broker (and onto a workernode/batchjob). The WS is
built as a standard .war file. The WS requires access to a relational db in order to
store job submission requests and to store updates recieved from the dts-workernode.
A platform independent HSQLDB is provided by default. It is pre-configured and can
be used for testing and lightweight deployments. 


a) Copy/rename sample-ws-dot-dataminx dir
-------------------------------------------
Recursivley copy and rename the 'src/main/resources/sample-ws-dot-dataminx-dir'
dir to you chosen location. Default is '$HOME/.dataminx' e.g:
  cp -r src/main/resources/sample-ws-dot-dataminx-dir ~/.dataminx


b) Edit dts-ws.properties file
-------------------------------
Edit the 'datasource.url' property in your 'dts-ws.properties' file (as copied above in dataminx-dir)
and provide the FULL path to your dataminx dir, e.g:
'datasource.url=jdbc:hsqldb:file:C:/Documents and Settings/<provide-full-path-to-your-dataminx-dir>/ws-hsqlDB/ws-hsqlDB'

    This points to a pre-configured embedded database for use by the WS.
    It is located in the 'ws-hsqlDB' directory and is used for testing and for lightweight WS deployments.
    Of course, you can configure the WS to run against a different database as required
    (we strongly recommend you do this for production deployments). Because the
    db is an embedded db, only the WS will be able to connect to this database
    (no other concurrent connections from different processes are allowed).
    Refer to the dts-ws.properties for instructions on deploying to different databases.



c) Build and install (skip integration tests)
---------------------------------------------
   mvn -DskipTests=true install


e) Run Unit tests (does not require WS running or running DB)
-------------------------------------------------------------
   mvn test


f) Run Integration tests
------------------------
Requires both the ActiveMQ broker and an instance of the WS to be up and running
because the integration test submits a job to the WS which is then routed to the
worker node, optionally via the dts-broker if you have the broker configured and running.

  mvn -Ddataminx.dir=/path-to-dot-datataminx-dir/.dataminx integration-test



g) Deploying the WS
--------------------
The WS can be executed using the integrated Jetty container, or deployed as a standard
.war file in a servlet container (e.g. Tomcat). In either case, you need to provide two
system properties;
    -Djava.security.auth.login.config (the location of the jaas config file used for authentication)
    -Ddataminx.dir                    (the dataminx directory that contains the dts-ws.properties file)

  1) To run using Jetty plugin and maven exec:java use:
  mvn -Djava.security.auth.login.config=/<full-path>/dts-security/src/main/resources/jaas.config.default -Ddataminx.dir=/path-to-dot-datataminx-dir/.dataminx exec:java

  2) To run .war on Tomcat:
  - Copy the target/dts-ws.war to your chosen servlet container's webapps dir.
  - Open $TOMCAT_HOME/bin/catalina.sh (*nix) or $TOMCAT_HOME/bin/catalina.bat (Win) and add these java system
    properties to the start of the script (add as the first line under the comments).
    - catalina.sh
       JAVA_OPTS="$JAVA_OPTS -Ddataminx.dir=/<full-path>/.dataminx -Djava.security.auth.login.config=/<full-path-to>/dts-security/src/main/resources/jaas.config.default"
    - catalina.bat
       JAVA_OPTS=%JAVA_OPTS% -Djava.security.auth.login.config="C:\<full-path-to>\dts-security\src\main\resources\jaas.config.default" -Ddataminx.dir="C:\Documents and Settings\mtd28985\.dataminx"


An alternative to specifying the 'java.security.auth.login.config' is to edit
'$JAVA_HOME/jre/lib/security/java.security' file and uncomment the 'Default login configuration file' property
as shown below. Please note you'll need to copy jaas.config.default in the dts-security module to ~/.java.login.config
    #
    # Default login configuration file
    #
    #login.config.url.1=file:${user.home}/.java.login.config



h) Customising the JAAS authentication module
----------------------------------------------
Plain username and password authentication (default):
The 'jaas.config.default' file uses a clear text password file for
authenticating WS requests. You can edit this file ($HOME/.dataminx/passwd) to
add new un/pw combinations.

MyProxy authentication:
(Optional) The jaas.config.myproxy file defines a login module that uses myproxy.
You need to define the 'jaas.config.myproxy' file as the parameter to 'java.security.auth.login.config'
system property (i.e. when running the WS). Then you'll need to change auth.username and auth.password
to reflect the myproxy login and password you've used when you ran myproxy-init. 