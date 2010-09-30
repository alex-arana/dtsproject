Maven Build, install, test instructions
=========================================

The dts-workernode subscribes to the JMS broker's job-submit and job-control queues 
and publishes status updates/events to the broker's event queue. 
The workernode does not contain the actual batchjob, but rather it invokes a 
batch job when a job is recieved. It can be configured 
to run 'n' jobs concurrently (scaling-up). Multipe worker nodes can be subscribed 
to a single broker in order to scale (scaling-out).


a) Copy 'src/main/resources/dts-workernode.properties.template' to your .dataminx dir:
  mkdir ~/.dataminx  (if not aready created)
  cd dts-workernode
  cp src/main/resources/dts-workernode.properties.template ~/.dataminx/dts-workernode.properties


b) Edit '~/.dataminx/dts-workernode.properties'.
  By default, the values in the properties file should just work (see file for more info)


c) Start ActiveMQ
  $ACTIVEMQ_HOME/bin/activemq

d) Run the worker node
  1) Via maven exec plugin (for testing/dev)
     mvn -Ddataminx.dir=/home/dts-user/.dataminx exec:java

  2) Assembly 
     TODO

  3) As .war file 
     TODO




