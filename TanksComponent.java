/**
 *  Program Name : Tanks
 */

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Class TanksComponent - methods for creating Tank objects and displaying them
 * @author Peter Cross, Jeremy Hilliker
 * @version April 5, 2017
 */
// clear; javac *.java; java TanksFrame
public class TanksComponent extends JComponent 
{
	// Frames per Second
	public final static int FPS_MAX = 60;
	// Tick time interval
	public final static int TICK = 1000/FPS_MAX;
  
	// Tank's starting position
	private final static int TANK_START_X = TanksFrame.WIDTH / 8;
	private final static int TANK_START_Y = TanksFrame.HEIGHT / 2;
  
	// Unique player ID
	public final static long playerID = System.currentTimeMillis();

	// Time interval for exchange with Tanks Server
	public final static int EXCH_INTERVAL = 100;
	
	// Timer for local tank
	private final Timer timer;
	// Local tank instance
	private final Tank tank;

	// Timer for exchange with Tanks Server
	private final Timer exchTimer;
	
	// Remote tanks instances
	private static Map<Long, Tank> remoteTanks;
	
	/**
	 * Class constructor
	 */
	public TanksComponent() 
	{
		// Create timer for updating local tank
		timer = new Timer( TICK, null );
	  
		// Create instance of local tank
		tank = new TankImpl( null, TANK_START_X, TANK_START_Y, playerID );
	  
		// Create Tank Driver for local tank
		new TankDriverLocal( tank, this );
		
		// Create timer for exchange with Tanks Server
		exchTimer = new Timer( EXCH_INTERVAL, tank::sendUpdate );
		
		remoteTanks = new HashMap<>();
	}
	
	public static boolean isTankInList( long playerID )
	{
		return remoteTanks.containsKey( playerID );
	}
	
	public void addRemoteTank( long playerID )
	{
		// Create instance of remote tank
		Tank remoteTank = new TankImpl( null, TanksFrame.WIDTH, TanksFrame.HEIGHT, playerID );
		// Create Tank Driver for updating remote tank
		new TankDriverRemote( remoteTank, this );
		
		remoteTanks.put( playerID, remoteTank );
	}
  
	/**
	 * Paints graphics of the component
	 */
	public void paint( Graphics g ) 
	{
		Graphics2D g2 = (Graphics2D) g;
    
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    
		paint(g2);
	}
  
	/**
	 * Draws on the Component local and remote tank
	 */
	private void paint( Graphics2D g ) 
	{
		tank.draw(g);
		
		for ( Long player : remoteTanks.keySet() )
			remoteTanks.get(player).draw(g);
		
	}

	/**
	 * Get invoked in regular intervals by local tank timer
	 * @param e
	 */
	private void tick( ActionEvent e ) 
	{
		// Update local tank state
		tank.update();
		
		// Update remote tanks state
		for ( Long player : remoteTanks.keySet() )
			remoteTanks.get(player).update();
		
		// Repaint the graphics
		repaint();
		// Request the focus on Component
		requestFocus();
	}

	/**
	 * Starts the component
	 */
	public void start() 
	{
		// Add action listener for local tank timer
		timer.addActionListener( this::tick );
		// Start local tank timer
		timer.start();
	  
		// Create bounds object for tanks movement
		Rectangle2D.Float bounds = new Rectangle2D.Float( 0,0, getWidth(), getHeight() );
	
		// Set movement bounds for local tank
		tank.setMovementBounds( bounds );
		// Start timer for exchange with Tanks Server
		exchTimer.start();
		
		// Set movement bounds for remote tanks
		for ( Long player : remoteTanks.keySet() )
			remoteTanks.get(player).setMovementBounds( bounds );
	}
}