/**
 *  Program Name : Tanks
 */

/**
 * Interface Tank - method signature for controlling Tank
 * @author Jeremy Hilliker
 * @version April 5, 2017
 */
public interface Tank extends Sprite, SocketExchange 
{
	// Steers the tank
	void steer( Direction dir, SpeedRel sRel );
}
