/**
 *  Program Name : Tanks
 */

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Class TankDriverLocal - methods for driving Tank on local computer
 * @author Peter Cross
 * @version April 5, 2017
 */
public class TankDriverLocal 
{
	// Tank to control
	private final Tank tank;

	/**
	 * Class constructor
	 * @param aTank Tank to control
	 * @param anArena Component area where tank us located
	 */
	public TankDriverLocal( Tank aTank, Component anArena ) 
	{
		this.tank = aTank;

		// Create Key Listener and add it to Arena object
		anArena.addKeyListener( new KeyDriver() );
		anArena.requestFocus();
	}
	
	/**
	 * Class KeyDriver - implementation of key listener
	 * @author Peter Cross
	 * @version April 5, 2017
	 */
	private class KeyDriver extends KeyAdapter 
	{
		private boolean up;
		private boolean down;
		private boolean left;
		private boolean right;

		/**
		 * Sets direction of movement and steers the tank
		 */
		private void steer() 
		{
			// Tank turn direction
			Tank.Direction d;
      
			if (left == right) 
			{
				d = Tank.Direction.NONE;
			} 
			else if (left) 
			{
				d = Tank.Direction.LEFT;
			} 	
			else if (right) 
			{
				d = Tank.Direction.RIGHT;
			} 
			else 
			{
				d = Tank.Direction.NONE;
			}
      
			// Tank Speed direction
			Tank.SpeedRel dv;
      
			if (up && down) 
			{
				dv = Tank.SpeedRel.STOP;
			} 
			else if (up) 
			{
				dv = Tank.SpeedRel.FORWARD;
			} 
			else if (down) 
			{
				dv = Tank.SpeedRel.REVERSE;
			} 
			else 
			{
				dv = Tank.SpeedRel.NONE;
			}
			
			// Steer the tank using method implemented for Tank interface
			tank.steer( d, dv );
		}
		
		/**
		 * Implementation of Key Pressed event for Key Adapter
		 * @param e Key Event
		 */
		@Override
		public void keyPressed( KeyEvent e ) 
		{
			// Get key code of pressed button and switch to appropriate action
			switch( e.getKeyCode() ) 
			{
				case KeyEvent.VK_UP:
				case KeyEvent.VK_KP_UP:
				case KeyEvent.VK_W:
					up = true;
					break;
        
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
				case KeyEvent.VK_S:
					down = true;
					break;
        
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_KP_LEFT:
				case KeyEvent.VK_A:
					left = true;
					break;
        
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_KP_RIGHT:
				case KeyEvent.VK_D:
					right = true;
					break;
			}
      
			// Steer the tank
			steer();
		}
    
		/**
		 * Implementation of Key Released event for Key Adapter
		 * @param e Key Event
		 */
		@Override
		public void keyReleased( KeyEvent e ) 
		{
			// Get key code of released button and switch to appropriate action
			switch( e.getKeyCode() ) 
			{
				case KeyEvent.VK_UP:
				case KeyEvent.VK_KP_UP:
				case KeyEvent.VK_W:
					up = false;
					break;
        
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
				case KeyEvent.VK_S:
					down = false;
					break;
        
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_KP_LEFT:
				case KeyEvent.VK_A:
					left = false;
					break;
        
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_KP_RIGHT:
				case KeyEvent.VK_D:
					right = false;
					break;
			}
      
			// Steer the tank
			steer();
		}
	}
}