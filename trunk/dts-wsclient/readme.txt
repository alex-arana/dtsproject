Maven Build, install, test instructions
=========================================


mvn clean 
mvn install 


To build and run the command line client
(this builds an executable jar file + all dependencies in a singl lib dir)
===========================================================================
mvn clean
mvn assembly:assembly -Pdeploy-assembly1
# Change dir into the following dir 'target/dts-wsclient-0.0.1-SNAPSHOT-dts-agent/lib'
java -Ddataminx.dir=/home/djm76/.dataminxes -Dauth.username=test -Dauth.password=test -jar dts-wsclient-0.0.1-SNAPSHOT.jar

#Run the commandline:
java -Ddataminx.dir=/home/djm76/.dataminxes -jar dts-wsclient-0.0.1-SNAPSHOT.jar -submit -url http://localhost:18080/dts-ws/ -un
 test -pw test -submitJobRequestFile path_to_your_job_submission_file

or for more usage information of the commandline, run

java -Ddataminx.dir=/home/djm76/.dataminxes -jar dts-wsclient-0.0.1-SNAPSHOT.jar -help