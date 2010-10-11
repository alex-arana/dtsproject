Maven Build, install, test instructions
=========================================

The dts-workernode subscribes to the JMS broker's job-submit and job-control queues 
and publishes status updates/events to the broker's event queue. 
The workernode invokes a batch job when a job request is recieved. It can be configured
to run 'n' jobs concurrently (scaling-up). Multipe worker nodes can be subscribed 
to a single broker in order to scale-out.


a) Copy 'src/main/resources/dts-workernode.properties.template' to your .dataminx dir:
  mkdir ~/.dataminx  (if not aready created)
  cd dts-workernode
  cp src/main/resources/dts-workernode.properties.template ~/.dataminx/dts-workernode.properties


b) Edit '~/.dataminx/dts-workernode.properties'.
  By default, the values in the properties file should just work (see file for more info)


c) Unit Tests
--------------
Note, an ActiveMQ broker instance does NOT need to be running to run the unit tests
as the tests do not submit messages to the broker.

  mvn -Ddataminx.dir=/home/<path-to-dot-dataminx-dir>/.dataminx test



d) Integration Tests
---------------------
Note, a working ActiveMQ instance needs to be up and running so that the integration
tests can submit messages to the configured job-submit and job-control queues. At
this point, the integ-tests do not test for responses on the event.queue.

  mvn -Ddataminx.dir=/home/<path-to-dot-dataminx-dir>/.dataminx integration-test


-) Start ActiveMQ
---------------------
  $ACTIVEMQ_HOME/bin/activemq

-) Run the worker node
  1) Via maven exec plugin (for testing/dev)
     mvn -Ddataminx.dir=/home/dts-user/.dataminx exec:java

  2) Assembly 
     TODO

  3) As .war file 
     TODO




