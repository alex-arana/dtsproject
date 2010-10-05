Maven Build, install, test instructions
=========================================


mvn clean 
mvn install 


To build the command line client (executable jar file + all dependencies in lib dir)
====================================================================================
mvn clean
mvn assembly:assembly -Pdeploy-assembly1
# Change dir into the following dir 'target/dts-wsclient-0.0.1-SNAPSHOT-dts-agent/lib'
java -Ddataminx.dir=/home/djm76/.dataminxes -jar dts-wsclient-0.0.1-SNAPSHOT.jar


