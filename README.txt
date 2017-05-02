There are 2 components in this program : Server part and Client part.

To start a Server part - run TanksServer.java. 
It may have command line parameter in which port number would be specified.
Default port number is 8000.

To start client part - run TanksFrame.java.
In command line can be specified hostname of server computer, port on server computer and color for local tank.
By default server hostname is localhost, port on server 8000 and color is black.

Ideally, server part should be started first. After server part has started, then clients can be launched and see each other's tanks.
If a client is started before the server started, it will not be able to get messages from another player.

The program was tested with launching server part on localhost, but it can be launched on any other Java server.