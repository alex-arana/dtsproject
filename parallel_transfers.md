# Thoughts on performing parallel transfers - Implications for VFS #
**(note, VFS is not the issue here, rather its the VFS protocol implementations that required improvement/fine tuning and the subsequent copy operations)**

# Parallel transfer considerations #
_(i.e. don't share a single session context/connection over multiple threads)_

Each connection to a file system (e.g. SRB) spawns a server process listening on a port in the range you have specified to the file system (ephemeral port range). That server process is connected to by the client and receives commands and data from that client. It is a request/response communication protocol where each side waits for the other to finish before issuing another instruction. If one were to have multiple client threads sharing the same security context (session) and server process, then things would get very confused because the server would be receiving concurrent requests when it is meant to be processing a response for another thread etc.

In terms of how many authenticated sessions a server can manage, this is usually limited by two factors:
a) Firstly, the number of high ports available for use by a server process, and
b) Secondly any protocol specific issues, e.g. for srb, the number of permitted back-end DB connections since usually one srbServer process ultimately means 1 DB connection by the MCAT server i.e. SRB does not do connection pooling and reuse.

With these issues in mind, parallelism using threads should occur by:
  * each thread owning its own thread-local security context/session
  * NOT by each thead sharing a security context/session

The overhead of having a thread-local security session is minimal. Typically, this would allow a transfer to be parallelized into say ~50 threads (certainly no more than 100?? i guess this depends on your network and the number of available high ports). Special consideration should also be taken when using more than a certain number of threads as the channel/link between two sites/states might be used by other non-DTS users as well. In ARCS, 1000 ports have been opened to be used as ephimeral ports. So if a transfer requires 2 ports, the DTS will have a maximum of 500 threads to use.

### Parallel Apache Commons VFS Example ###

Create a new `DefaultFileSystemManager` instance **per thread** (note, if using the VFSUtil helper class, this requires a new call to _createNewFsManager_ for each thread e.g.
```
 DefaultFileSystemManager fsManager = new DefaultFileSystemManager();
 fsManager.addProvider("gsiftp", new GridFtpFileProvider());
```

### Protocol native Get/Put rather than byte streaming ###

Commons VFS Grid currently uses a byte streaming approach using an in-memory buffer to transferring files from the source to the destination. In doing this, the bytes are not written to the disk and so disk IO is not an issue. Nevertheless, we plan to investigate potential advantages/disadvantages if we use a native Get/Put approach. This approach effectively stages files in to the workernode first and subsequently stages them out to the target. Disk IO is then an issue (a potential bottleneck, unless bytes can be get/put into an in-mem buffer rather than get/put from disk).

Another issue to be aware of if using an approach that 'shells-out' to the command line (in order to use a native get/put command line client), is that user credentials will (probably) have to be written to disk or passed to the command line client in a format that can be understood by the command line client.

### Optimizing the in-memory buffer ###
If using an in-memory buffer to read and write bytes using input and output streams for example (e.g. in VFSUtil.copy), then there is scope to optimize the memory buffer that is used. Refer the following link for more details. This would certainly improve the performance of the VFSUtil.copy streaming operation:
http://fasterdata.es.net/TCP-tuning/background.html

Also, not sure if the input and output streams that are returned by the VFS implementation are optimized for that particular protocol ?? e.g. VFS defines the following methods to get an input and output stream:
```
 java.io.InputStream inStream = someVFS_FileObject_A.getContent().getInputStream(); 
 java.io.OutputStream outStream = someVFS_FileObject_B.getContent().getOutputStream(); 
```
For the gridftp VFS implementation, the returned streams are the following objects from jglobus (http://dev.globus.org/wiki/CoG_jglobus):
```
  org.globus.ftp.extended.GridFTPInputStream
```
and
```
  org.globus.ftp.extended.GridFTPOutputStream
```

The question is do these input/output streams perform any protocol native optimizations ?

### Useful/good links ###
http://fasterdata.es.net/