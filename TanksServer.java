/**
 *  Program Name : Tanks
 */

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class TanksServer - UDP Server to exchange messages between Tanks client computers
 * @author Peter Cross
 * @version April 5, 2017
 */
public class TanksServer
{
	// Default port number to listen to
	public static final int DEFAULT_PORT = 8000;
	// Logger object to log progress messages
    private static final Logger LOG = Logger.getLogger( TanksServer.class.getName() );
    
    private static final int PACKET_SIZE = 50;

    // Socket through which to exchange messages
    private static DatagramSocket socket;
    // Map object to store players and Tanks's data for the players
    private static Map<Long, Packet> players;
    
    /**
     * Method to start the exchange with client computers
     * @param n Port number for exchange
     * @throws SocketException
     * @throws IOException
     */
    private static void start( int n ) throws SocketException, IOException
    {
    	// Create socket object for exchange
    	socket = new DatagramSocket(n);
    	// Create Map object for players and their tanks
    	players = new HashMap<>();
    	
    	// Buffer for received and sent messages
    	final byte[] buf = new byte[50];
    	// Datagram packet object
        final DatagramPacket packet = new DatagramPacket( buf, buf.length );
        
        // Get IP address of the server
        SocketAddress localSocketAddress = socket.getLocalSocketAddress();
        
        LOG.info("Listening at: " + localSocketAddress );
        
        try 
        {
            // Loop while thread is not interrupted
        	while ( !Thread.currentThread().isInterrupted() ) 
            {
                LOG.info( "Awaiting request." );
                // Receive a packet through socket
                socket.receive( packet );
                
                // Get IP address where packet came from
                SocketAddress socketAddress = packet.getSocketAddress();
                //LOG.info( "Received datagram from: " + socketAddress );
                
                // Handle the received packet and prepare the response packet
                DatagramPacket resp = handle( packet );
                
                LOG.info( "Sending response to: " + resp.getSocketAddress() );
            	// Send message through the socket
            	socket.send( resp );
            }
        }
        finally 
        {
            LOG.info( "Shutting down." );
            // Close the socket
            socket.close();
        }
    }

    /**
     * Handles received packet and prepares packet to send
     * @param packet Received packet
     * @return List of packets to send
     * @throws IOException
     */
    private static DatagramPacket handle( DatagramPacket packet ) throws IOException 
    {
    		  // Create data input data stream object
    	try ( DataInputStream dataInputStream = new DataInputStream( new ByteArrayInputStream(packet.getData()) ) ) 
        {
			// Get IP address where packet came from
    		SocketAddress socketAddress = packet.getSocketAddress();

			LOG.info( "Reading from: " + socketAddress );
            
			// Get data that came with packet 
			long playerID  = dataInputStream.readLong();
			long timestamp = dataInputStream.readLong();
			float x = dataInputStream.readFloat();
			float y = dataInputStream.readFloat();
			float r = dataInputStream.readFloat();
			float d = dataInputStream.readFloat();
			float s = dataInputStream.readFloat();
			int c = dataInputStream.readInt();
			
			// Create Packet object for data that came with packet and add to the map with player's ID key
			players.put( playerID, new Packet(timestamp, x, y, r, d, s, c) );
            
			// Create response packet
			packet = writeResponse( socketAddress, playerID );
			
			// If came packet with timestamp zero - this player closed the program
			//if ( timestamp == 0 )
				// Remove player's ID from the map
            //	players.remove( playerID );
            
			// Return packet to send
			return packet;
		}
    }

    
    private static DatagramPacket writeResponse( SocketAddress socketAddress, long playerID ) throws IOException
    {
    	int playersSize = players.size();
    	int arraySize = PACKET_SIZE * Math.max(1, playersSize-1);
    	
    	try ( ByteArrayOutputStream byteArray = new ByteArrayOutputStream( arraySize );
      		  DataOutputStream outStream = new DataOutputStream( byteArray ) )
      	{
    		outStream.writeInt( playersSize-1 );
    		
    		if ( playersSize > 1 )
	        	for ( long player : players.keySet() )
	    			// If player is not the player from whom packet came from
	            	if ( player != playerID )
	            		writeToOutStream( outStream, player );
	         
    		outStream.close();
    		
    		if ( players.get(playerID).timestamp == 0 )
				// Remove player's ID from the map
            	players.remove( playerID );
    		
    		// Convert output stream into array
			byte[] buf = byteArray.toByteArray();
			
			// Create datagram packet to send
			return new DatagramPacket( buf, buf.length, socketAddress );
        }
    }
    
    /**
     * Writes to output stream object
     * @param outStream Output Stream
     * @param player Player's ID
     * @throws IOException
     */
    private static void writeToOutStream( DataOutputStream outStream, long player ) throws IOException
    {
    	writeToOutStream( outStream, player, players.get(player) );
    }
    
    /**
     * Writes to output stream object
     * @param outStream Output Stream
     * @param playerID Player's ID
     * @param p Packet data to send
     * @throws IOException
     */
    private static void writeToOutStream( DataOutputStream outStream, long player, Packet pck ) throws IOException
    {
    	outStream.writeLong( player );
    	outStream.writeLong( pck.timestamp );
    	outStream.writeFloat( pck.x );
    	outStream.writeFloat( pck.y );
    	outStream.writeFloat( pck.r );
    	outStream.writeFloat( pck.d );
    	outStream.writeFloat( pck.s );
    	outStream.writeInt( pck.c );
    }
    
    /**
     * Starts the program
     * @param args Command line arguments
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception 
    {
        // Get default port number from constant
    	int port = DEFAULT_PORT;
        
    	// If another port is specified through command line
        if ( args.length > 0 ) 
        	// Get port number from command line
        	port = Integer.parseInt( args[0] );
        
        // Start the exchange process
        start( port );
    }
    
    /**
     * Class Packet - Stores Tank data
     * @author Peter Cross
     * @version April 5, 2017
     */
    private static class Packet
    {
    	private long timestamp;	// Time stamp
    	private float x;		// X coordinate
    	private float y;		// Y coordinate
    	private float r;		// Radius
    	private float d;		// Direction
    	private float s;		// Speed 
    	private int c;			// Color
    	
    	/**
    	 * Class constructor
    	 * @param timestamp Time stamp
    	 * @param x X coordinate
    	 * @param y Y coordinate
    	 * @param r Radius
    	 * @param d Direction
    	 * @param s Speed 
    	 * @param c Color
    	 */
    	public Packet( long timestamp, float x, float y, float r, float d, float s, int c )
    	{
    		this.timestamp = timestamp;
    		this.x = x;
    		this.y = y;
    		this.r = r;
    		this.d = d;
    		this.s = s;
    		this.c = c;
    	}
    }
}