/**
 *  Program Name : Tanks
 */

import java.awt.*;
import java.awt.geom.*;

/**
 * Interface Sprite - method signatures for game components
 * @author Jeremy Hilliker
 * @version April 5, 2017
 */
public interface Sprite 
{
	// Enumeration for controlling speed direction
	enum SpeedRel 
	{
		NONE, FORWARD, REVERSE, STOP;
	}
	
	// Enumeration for moving to the left and to the right
	enum Direction 
	{
		NONE, LEFT, RIGHT;
	}
	
	// Displays shape
	void draw( Graphics2D g );

	// Updates game screen
	void update();
	
	// Controls velocity
	void changeVelocity( Direction direction, SpeedRel sRel );
	
	// Sets movement bounds
	void setMovementBounds( Rectangle2D bounds );
}