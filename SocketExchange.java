/**
 *  Program Name : Tanks
 */

import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;

/**
 * Interface SocketExchange - method signatures for socket exchange methods
 * @author Peter Cross
 * @version April 5, 2017
 */
public interface SocketExchange
{	
	// Sends Tank's update to Tanks Server
	void sendUpdate( ActionEvent e );
	
	// Gets update from remote Tank and displays it
	void captureState( ActionEvent e );
}
