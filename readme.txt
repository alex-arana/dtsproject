
DTS Project Developer Guide (configure, compile, build, install)
==================================================================
This readme is not intended to provide deployment instructions of pre-built binaries.
Rather, these instructions are for developers for downloading, configuring, building the src code,
installing artefacts into the local maven repo and running Unit/Integration tests.

We will prove pre-built binaries and config instructions when we provide the first release. 

DTS project is a multi-module maven project. The parent pom.xml defines the
following sub-modules that inherit from the parent pom:

  dts-wsclient        (a WS client with a simple cmnd line wrapper for submitting DTS jobs to a dts WS)
  dts-ws              (the DTS WS - primary entry point for running DTS jobs)
  dts-broker          (a JMS router and delayer for routing and scheduling jobs to appropriate workers)
  dts-workernode      (subscribes to the JMS broker and runs jobs)
  dts-batchjobclient  (a simple way to run DTS jobs directly withough using the WS/JMS/broker)
  dts-batchjob        (the batch job itself that performs the data copy operation)
  dts-schema          (the XSD schemas used to define messages and jobs)
  dts-common          (common code used across the different modules)
  dts-security        (common security code)



Prerequisites
================
Following is a list of software components required to get the DTS components compiled
and running:

    * Java SE JDK version 6
    * Subversion
    * Maven
    * ActiveMQ

Getting The Code
=================
The DTS codebase is currently hosted in the DTS Google Code SVN repository.
You can check out the code by running:

  cd /usr/local
  svn checkout http://dtsproject.googlecode.com/svn/trunk/ minx-dts


Build the Src code and Install
==============================
This will build and install all the sub-modules of the dts project. Note, if you are
behind an Http proxy, you will need to provide proxyHost and proxyPort properties as shown
below (the dts-schema sub-module downloads XSD schemas from the internet and so the
build needs online access - would be better if the build could be done offline - TODO).


  cd minx-dts
  cp dts-batchjob/src/test/filters/filter.properties.template dts-batchjob/src/test/filters/filter.properties
  cp dts-ws/src/test/filters/filter.properties.template dts-ws/src/test/filters/filter.properties
  cp dts-workernode/src/test/filters/filter.properties.template dts-workernode/src/test/filters/filter.properties
  cp dts-broker/src/test/filters/filter.properties.template dts-broker/src/test/filters/filter.properties
  cp dts-ws/src/main/resources/dts-ws.properties.template ~/.dataminx/dts-ws.properties
  mvn -DskipTests=true -DproxyHost=optionalHostName -DproxyPort=8080 install

IMPORTANT:  Before you can run tests and integration tests, you will need to perform some post-install configurations
for each sub-module. Please refer to the 'readme.txt' of each sub-module for instruction.
