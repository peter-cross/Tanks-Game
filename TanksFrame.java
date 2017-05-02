/**
 *  Program Name : Tanks
 */

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Socket;
import javax.swing.*;

/**
 * Class TankFrame - Creates windows interface for the game
 * @author Peter Cross, Jeremy Hilliker
 * @version April 5, 2017
 */
public class TanksFrame extends JFrame 
{
	public final static int WIDTH = 800;			 // Window width
	public final static int HEIGHT = 600;			 // Window height
	private final static int DEFAULT_PORT = 8000;	 // Default port for exchange with Tanks server
	private static String DEFAULT_HOST = "localhost";// Default host name
	public static Color DEFAULT_REMOTE_COLOR = new Color(235, 235, 235); // Default color for remote tank
	public static Color DEFAULT_LOCAL_COLOR = new Color(0, 0, 0); 		 // Default color for local tank
	
	public static String host;	// Tanks server host
	public static int 	 port;	// Tanks server port
	public static Socket socket;// Socket to communicate with web-server
	public static Color  color;	// Color for current tank
	
	// TanksComponent object
	public static TanksComponent comp;

	/**
	 * Class constructor
	 */
	public TanksFrame() 
	{
		// Make frame non-resizable
		setResizable( false );
		
		// Create Tanks Component object
		comp = new TanksComponent();
		// Add created component object to the frame
		add( comp );
		
		// Add Event handler for closing window event
		addWindowListener( atCloseProgram() );
	}

	/**
	 * Starts the program
	 * @param args Command line arguments
	 */
	public static void main( String[] args ) 
	{
		// Check command line arguments
		checkCommandLine( args );
		
		// Create Frame object for tanks
		TanksFrame frame = new TanksFrame();
		
		// Set dimensions for the window
		frame.setSize( WIDTH, HEIGHT );
		// Set default close operation for the window
		frame.setDefaultCloseOperation( EXIT_ON_CLOSE );
		// Make frame visible
		frame.setVisible( true );
		// Put frame in the middle of the screen
		frame.setLocationRelativeTo( null );
		
		// Start Tanks Component
		frame.comp.start();
	}
	
	/**
	 * Event handler for window closing event
	 * @return WindowAdapter object
	 */
	private static WindowAdapter atCloseProgram()
	{
		// Return anonymous class object created from WindowAdapter interface
		return new WindowAdapter()
		{
			/**
			 * Gets invoked when window is closing
			 */
			public void windowClosing( WindowEvent e )
			{
				try 
				{
					// Send to Tanks Server a packet with all zeros to remove player ID from the list
					new ClientExchange( "0 0 0 0 0 0" ).call();
				} 
				catch ( Exception ex ) 
				{ }
				finally
				{
					// Exit the program
					System.exit(0);
				}
			}
		};
	}
	
	/**
	 * Checks program's command line arguments
	 * @param args Command line arguments
	 */
	private static void checkCommandLine( String[] args )
	{
		port = DEFAULT_PORT;
		color = DEFAULT_LOCAL_COLOR;
		
		// If there is at least one argument in command line
		if ( args.length > 0 )
		{
			// Get host name from the 1st argument
			host = args[0];
			
			// If there is a second argument specified in command line
			if ( args.length > 1 )
				try
				{
					// Get port number from the 2nd argument
					port = Integer.parseInt( args[1] );
				}
				catch ( Exception e )
				{
					port = DEFAULT_PORT;
				}
			
			// If there is a third argument specified in command line
			if ( args.length > 2 )
				try
				{
					// Get color specified in 3rd argument
					color = (Color)Color.class.getField( args[2] ).get(null);
				}
				catch ( Exception e )
				{
					color = DEFAULT_LOCAL_COLOR;
				}
		}
		// Otherwise, if no arguments in command line are specified
		else
			// Assign default host name
			host = DEFAULT_HOST;
	}
}