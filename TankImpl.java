/**
 *  Program Name : Tanks
 */

import java.awt.*;
import java.awt.geom.*;

/**
 * Class TankImpl - methods for controlling and updating Tank
 * @author Peter Cross
 * @version April 5, 2017
 */
public class TankImpl extends SpriteImpl implements Tank 
{
	// Tank Acceleration rate
	private final static float ACCELERATION = 0.02f;
	// Tank rotation rate
	private final static float ROTATE_RATE = (float) Math.PI/2/TanksComponent.FPS_MAX;
	// Tank maximum speed
	private final static float MAX_SPEED = 2f;

	// Steering directions
	private Direction dir = Direction.NONE;
	private SpeedRel sRel = SpeedRel.NONE;
	
	/**
	 * Class constructor
	 * @param bounds Movement bounds
	 * @param x X-coordinate
	 * @param y Y-coordinate
	 */
	public TankImpl( Rectangle2D bounds, int x, int y, long player ) 
	{
		super( bounds, makeTankShape(), x,y, 20,20, ACCELERATION, ROTATE_RATE, MAX_SPEED, player );
	}
	
	/**
	 * Creates Tank's shape object
	 * @return Created Shape object
	 */
	private static Shape makeTankShape() 
	{
		Path2D shape = new Path2D.Float();
		
		// Drawing of shape
		shape.moveTo(0f, 0f);
		shape.lineTo(10f, 20f);
		shape.lineTo(20f, 0f);
		shape.lineTo(0f, 0f);
		shape.closePath();
		
		return shape;
	}

	/**
	 * Steers the tank according to directions
	 */
	public void steer( Direction dir, SpeedRel sRel ) 
	{
		this.dir = dir;
		this.sRel = sRel;
	}

	/**
	 * Updates tank's state
	 */
	public void update() 
	{
		rotate(dir);
		changeVelocity(dir, sRel);

		super.update();
	}
}