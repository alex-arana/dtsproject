Maven Build, install, test instructions
=========================================

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




