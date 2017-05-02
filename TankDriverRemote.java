/**
 *  Program Name : Tanks
 */

import java.awt.Component;
import java.util.HashMap;

import javax.swing.Timer;

/**
 * Class TankDriverRemote - updates state of remote Tank
 * @author Peter Cross
 * @version April 5, 2017
 */
public class TankDriverRemote
{
	private static HashMap<Long, Timer> timers = new HashMap<>();
	
	/**
	 * Class constructor
	 * @param aTank Remote tank
	 * @param anArena Where to display
	 */
	public TankDriverRemote( Tank aTank, Component anArena )
	{
		anArena.requestFocus();
		
		// Create timer that captures state of remote tank and start it
		Timer t = new Timer( TanksComponent.EXCH_INTERVAL, aTank::captureState );
		t.start();
		
		timers.put( ((SpriteImpl) aTank).getPlayerID(), t );
	}
	
	public static void removeTimer( long playerID )
	{
		Timer t = timers.get( playerID );
		
		t.stop();
		
		timers.remove( playerID );
		
		System.out.println( "For player " + playerID + " timer has been removed " );
	}
}