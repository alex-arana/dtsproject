Maven Build, install, test instructions
=========================================

The dts-schema module contains the XSD schema defining the WS messages and the data copy activity
job description.

IMPORTANT: If you are behind a Http proxy, you will have to specify the proxy host and port
as below so that the maven build can access the internet.
This is required because certain XSD schemas (WS-Security stuff) are downloaded from the internet
during the build.

  mvn clean
  mvn -DproxyHost=wwwcache.dl.ac.uk -DproxyPort=8080 test
  mvn -DproxyHost=wwwcache.dl.ac.uk -DproxyPort=8080 install