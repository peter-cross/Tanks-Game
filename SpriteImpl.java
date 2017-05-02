/**
 *  Program Name : Tanks
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Class SpriteImpl - Implementation of methods from Sprite and SocketExchange interfaces
 * @author Peter Cross
 * @version April 5, 2017
 */
public class SpriteImpl implements Sprite, SocketExchange 
{
	// Logger object - to log event messages
	private final static Logger LOG = Logger.getLogger( SpriteImpl.class.getName() );
	private Rectangle2D bounds; // movement bounds
	
	protected float x; // x
	protected float y; // y
	protected float r; // rotation
	protected float d; // direction
	protected float s; // speed
	private   int   c; // Player color
	private	  long  player; // Player ID

	private final Shape shape; // Shape object that moves
	private final float h; // Height of shape object
	private final float w; // Width of shape object

	private final float acceleration;		// Acceleration rate
	private final float rotateRate;			// Rotation rate
	private final float maxSpeed;			// Maximum speed
	
	private float dx; // velocity in x
	private float dy; // velocity in y
	
	private SpriteState ss;					// Sprite state
	private long lastTimestamp = 0;			// Last timestamp
	
	private static HashMap<Long, SpriteState> spriteState = new HashMap<>();
	
	// Executor service to exchange messages with Tanks Server
	private ExecutorService execServ = Executors.newSingleThreadExecutor();
	
	// Maximum wait limit in milliseconds for the result of executor service
	private final static int MAX_WAIT_LIMIT = 30;
	
	/**
	 * Class constructor
	 * @param bounds Movement bounds
	 * @param aShape Object that moves
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param h Height of moving object
	 * @param w Width of moving object
	 * @param acceleration Acceleration rate
	 * @param rotateRate Rotation rate
	 * @param maxSpeed Maximum speed
	 */
	public SpriteImpl( Rectangle2D bounds, Shape aShape,
					   int x, int y, int h, int w,
					   float acceleration, float rotateRate, float maxSpeed, long player ) 
	{
		this.bounds = bounds;
		this.shape = aShape;
		
		this.x = x;
		this.y = y;
		this.h = h;
		this.w = w;
		this.c = TanksFrame.color.getRGB();

		this.acceleration = assertNonNeg(acceleration);
		this.rotateRate = assertNonNeg(rotateRate);
		this.maxSpeed = assertNonNeg(maxSpeed);
		this.player = player;
	}
	
	/**
	 * Asserts that value is non-negative
	 * @param f Value to check
	 * @return The same value if it's non-negative
	 */
	private static float assertNonNeg( float f ) 
	{
		if ( f < 0 ) 
			throw new IllegalArgumentException();
		
		return f;
	}
	
	/**
	 * Draws graphics
	 */
	public void draw( Graphics2D g ) 
	{
		// Save Transform
		AffineTransform saveAT = g.getTransform();
		
		// Set object color
		g.setColor( new Color(c) );
		// Move to specified coordinates
		g.translate( x,y );
		// Rotate object
		g.rotate( -r, w/2, h/2 );
		// Fill the shape with color
		g.fill( shape );
		// Overwrite the Transform in 2D context 
		g.setTransform( saveAT );
	}

	/**
	 * Rotate shape 
	 * @param direction Direction to rotate
	 */
	public void rotate( Direction direction ) 
	{
		switch (direction) 
		{
			case LEFT: 
				r += rotateRate; 
				break;
				
			case RIGHT: 
				r -= rotateRate; 
				break;
				
			case NONE: 
				break;
		}
	}

	/**
	 * Updates the shape
	 */
	public void update() 
	{
		// Save Sprite State
		ss = new SpriteState();
		
		this.x += this.dx;
		this.y += this.dy;

		enforceBounds();
	}
	
	/**
	 * Enforces bounds for the shape movements
	 */
	private void enforceBounds() 
	{
		if ( bounds == null )
			return;
		
		// Check top left and bottom right corner of shape object
		int outCode = bounds.outcode( x,y ) | bounds.outcode( x+w,y+h ); 
		
		// If they did not cross the bounds of the bounds rectangular
		if ( outCode == 0 ) 
		{
			return;
		}
		
		// If shape object crossed the bounds rectangular to the left
		if ( (outCode & Rectangle2D.OUT_LEFT) != 0 ) 
		{
			// Assign X coordinate of top left corner of the bounds rectangular to the shape object
			x = (float) bounds.getX();
		} 
		// If shape object crossed the bounds rectangular to the right
		else if ( (outCode & Rectangle2D.OUT_RIGHT) != 0 ) 
		{
			// Assign X coordinate of top right corner of the bounds rectangular to the shape object
			x = (float) (bounds.getX() + bounds.getWidth() - w);
		}
		
		// If shape object crossed the bounds rectangular to the top
		if ( (outCode & Rectangle2D.OUT_TOP) != 0 ) 
		{
			// Assign Y coordinate of top left corner of the bounds rectangular to the shape object
			y = (float) bounds.getY();
		} 
		// If shape object crossed the bounds rectangular to the bottom
		else if ( (outCode & Rectangle2D.OUT_BOTTOM) != 0 ) 
		{
			// Assign Y coordinate of bottom right corner of the bounds rectangular to the shape object
			y = (float) (bounds.getY() + bounds.getHeight() - h);
		}

		// Assign speed to zero
		this.s = 0;
	}
	
	/**
	 * Sets movement bounds
	 */
	public void setMovementBounds( Rectangle2D bounds ) 
	{
		this.bounds = bounds;
	}

	/**
	 * Changes the shape velocity
	 * @param direction Direction to move
	 * @param sRel Speed direction
	 */
	public void changeVelocity( Direction direction, SpeedRel sRel ) 
	{
		switch (direction) 
		{
			case LEFT: 
				d += rotateRate; 
				break;
			
			case RIGHT: 
				d -= rotateRate; 
				break;
			
			case NONE: 
				break;
		}
		
		switch (sRel) 
		{
			case FORWARD: 
				s += acceleration; 
				break;
			
			case REVERSE: 
				s -= acceleration; 
				break;
			
			case STOP: 
				// TODO test me
				if ( s > 0 ) 
					s = Math.max(s - acceleration, 0);
				else 
					s = Math.min(s + acceleration, 0);
				break;
		}
		
		// Limit maximum speed
		if ( s > maxSpeed ) 
			s = maxSpeed;
		
		// Limit reverse maximum speed
		else if ( -s > maxSpeed ) 
			s = -maxSpeed;

		// Increment by X coordinate
		this.dx = (float) Math.sin(this.d) * this.s;
		// Increment by Y coordinate
		this.dy = (float) Math.cos(this.d) * this.s;
	}
	
	/**
	 * Sends update to Tanks Server
	 */
	public void sendUpdate( ActionEvent e )
	{
		// If sprite state did not change
		if ( ss.xS == x && ss.yS == y && ss.rS == r && ss.dS == d && ss.sS == s )
			return;
		
		// Get current system's time
		long timestamp = System.currentTimeMillis();
		
		// Prepare a message to send to Tanks Server
		String msgToSend = "" + timestamp + " " + x + " " + y + " " + r + " " + d + " " + s + "\n";
		String msgReceived;
		
		try 
		{
			// Initiate exchange with Tanks Server
			Future res = execServ.submit( new ClientExchange(msgToSend) );
			
			// Try to get the result of message exchange with Tanks Server
			msgReceived = (String) res.get( MAX_WAIT_LIMIT, TimeUnit.MILLISECONDS );
		} 
		catch ( Exception ex ) 
		{	 
			msgReceived = "";
		}	 
		
		if ( !msgReceived.isEmpty() )
		{
			// Transform received message into Scanner object
			Scanner in = new Scanner( msgReceived );
			String[] input;
			long playerID;
			
			while ( in.hasNextLine() )
			{
				input = in.nextLine().split( " " );
				playerID = 0;
				timestamp = 0;
				
				// If it was transformed successfully
				if ( input != null && input.length > 0 )
					try
					{
						playerID = Long.parseLong( input[0] );
						// Get timestamp from received message
						timestamp = Long.parseLong( input[1] );
						
						// Get remote tank data
						float x = Float.parseFloat( input[2] );
						float y = Float.parseFloat( input[3] );
						float r = Float.parseFloat( input[4] );
						float d = Float.parseFloat( input[5] );
						float s = Float.parseFloat( input[6] );
						int c = Integer.parseInt( input[7] );
						
						if ( timestamp == 0l )
							TankDriverRemote.removeTimer( playerID );
						else
							spriteState.put(  playerID, new SpriteState(timestamp, x, y, r, d, s, c) );
					}
					catch ( Exception ex )
					{ }
			}
		}
	}
	
	public static void removePlayer( long playerID )
	{
		spriteState.remove(  playerID );
	}
	
	/**
	 * Captures state of remote Tank
	 */
	public void captureState( ActionEvent e )
	{
		SpriteState ss = spriteState.get(player);
		
		if ( ss != null && ss.timestamp > lastTimestamp )
		{
			x = ss.xS;
			y = ss.yS;
			r = ss.rS;
			d = ss.rS;
			s = ss.sS;
			c = ss.cS;
			
			lastTimestamp = ss.timestamp; 
		}
		else
		{
			x = TanksFrame.WIDTH;
			y = TanksFrame.HEIGHT;
			r = 0;
			d = 0;
			s = 0;
			c = TanksFrame.DEFAULT_REMOTE_COLOR.getRGB();
		}
	}
	
	public long getPlayerID()
	{
		return player;
	}
	
	/**
	 * Class SpriteState - to store Sprite's state
	 * @author Peter Cross
	 * @version April 5, 2017
	 */
	private class SpriteState
	{
		private long 	timestamp; // Timestamp
		
		private float 	xS,	// X
						yS,	// Y
						rS, // Rotation
						dS,	// Direction
						sS;	// Speed
		private int 	cS; // Color
		
		SpriteState()
		{
			timestamp = System.currentTimeMillis();
			xS = x;
			yS = y;
			rS = r;
			dS = d;
			sS = s;
			cS = c;
		}
		
		SpriteState( long timeStamp, float x, float y, float r, float d, float s, int c )
		{
			timestamp = timeStamp;
			xS = x;
			yS = y;
			rS = r;
			dS = d;
			sS = s;
			cS = c;
		}
	}
}