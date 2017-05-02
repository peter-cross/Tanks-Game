/**
 *  Program Name : Tanks
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Class ClientExchange - exchanges messages with Tanks Server
 * @author Peter Cross
 * @version April 5, 2017
 */
public class ClientExchange implements Callable<String>
{
	// Logger object to log event messages
	private static final Logger LOG = Logger.getLogger( ClientExchange.class.getName() );
	
    private final String host = TanksFrame.host;// Tank Server host
    private final int port = TanksFrame.port;	// Tank Server port
    
    private String toSendMsg;	// Message to send to Tanks Server
	private String receivedMsg;	// Received from remote player message
	
	/**
	 * Default class constructor
	 */
	public ClientExchange()
	{
		this( "" );
	}
	
	/**
	 * Class constructor
	 * @param msg Message to send to Tanks Server
	 */
	public ClientExchange( String msg )
	{
		toSendMsg = msg;
		receivedMsg = "";
	}
	
	/**
	 * Gets invoke to initiate message exchange with Tanks Server
	 */
	@Override
    public String call() throws IOException, UnknownHostException 
    {
    	receivedMsg = "";
			  // Create datagram socket object
    	try ( final DatagramSocket datagramSocket = new DatagramSocket() ) 
        {
            // If there is a message to send
    		if ( !toSendMsg.isEmpty() )
    			// Invoke method to send the message and receive a reply from Tanks Server
            	receivedMsg = sendMsg( datagramSocket );
		}
    	
    	// Return received from Tanks Server message
    	return receivedMsg;
    }
    
	/**
	 * Sends message to Tanks Server
	 * @param socket Socket to send message through
	 * @return Received reply from the server
	 * @throws IOException
	 */
    private String sendMsg( DatagramSocket socket ) throws IOException
	{
		// Create output stream
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream( toSendMsg.length() );
		
		// Get current player ID from the constant in another class
    	long playerID = TanksComponent.playerID;
		
		// Convert message to send into String array
    	String[] str = toSendMsg.split( " " );
		
		// Convert data in message to send into primitive types
    	long timestamp = Long.parseLong( str[0] );
		float x = Float.parseFloat( str[1] );
		float y = Float.parseFloat( str[2] );
		float r = Float.parseFloat( str[3] );
		float d = Float.parseFloat( str[4] );
		float s = Float.parseFloat( str[5] );
		// Get current player's tank color as RGB number
		int c = TanksFrame.color.getRGB();
		
			  // Create data output stream
		try ( DataOutputStream dataOutputStream = new DataOutputStream(outputStream) )
		{
			// Write data to send into output stream
			dataOutputStream.writeLong( playerID );
			dataOutputStream.writeLong( timestamp );
			dataOutputStream.writeFloat( x );
			dataOutputStream.writeFloat( y );
			dataOutputStream.writeFloat( r );
			dataOutputStream.writeFloat( d );
			dataOutputStream.writeFloat( s );
			dataOutputStream.writeInt( c );
			dataOutputStream.close();
		}
		
		// Convert output stream into array
		byte[] buffer = outputStream.toByteArray();
		
		// Create datagram packet to send
		DatagramPacket packet = new DatagramPacket( buffer, 
													buffer.length, 
													InetAddress.getByName(host), 
													port );
		// Send packet to Tanks Server
		socket.send( packet );
		toSendMsg = "";
		
		// If sent message was marked with non-zero timestamp
		if ( timestamp != 0 )
			// Invoke method to receive reply from the Server and return it
			return receiveMsg( socket );
		// If it's zero timestamp message - special message to Tanks Server
		else
			return "";
	}
	
    /**
     * Receives message from Tanks Server
     * @param socket Socket through which to receive a message
     * @return Received message
     * @throws IOException
     */
	private String receiveMsg( DatagramSocket socket ) throws IOException
	{
		// Buffer array for receiving a message
		byte[] buffer = new byte[500];
		
		// Create datagram packet with specified buffer
		DatagramPacket packet = new DatagramPacket( buffer, buffer.length );
		
		// Receive a message from the server
		socket.receive( packet );
		
		// Create input stream for received packet
		ByteArrayInputStream inputStream = new ByteArrayInputStream( packet.getData() );
		
		String res = "";
		
			  // Create data input stream
		try ( DataInputStream dataInputStream = new DataInputStream( inputStream ) )
		{
			// Read data from data input stream
			int numPlayers = dataInputStream.readInt();
			
			while ( numPlayers-- > 0 )
			{
				long playerID = dataInputStream.readLong();
				long timestamp = dataInputStream.readLong();
				float x = dataInputStream.readFloat();
				float y = dataInputStream.readFloat();
				float r = dataInputStream.readFloat();
				float d = dataInputStream.readFloat();
				float s = dataInputStream.readFloat();
				int c = dataInputStream.readInt();
				
				// Pack data into string
				res += "" + playerID + " " + timestamp + " " + x + " " + y + " " + r + " " + d + " " + s + " " + c + "\n";
				
				if ( playerID != TanksComponent.playerID && !TanksComponent.isTankInList( playerID ) )
					TanksFrame.comp.addRemoteTank( playerID );
			}
			
			// Return reply string
			return res;
		}		
	}
}