/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Util;

/**
 * The implementation for the JOINWORKCYCLE Management Message
 * 
 * @author klobs
 */
public class ManagementMessageJoinWorkCycle extends ManagementMessage {
	
	/**
	 * 	Handles the JOINWORKCYCLE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	none
	 */	
	public ManagementMessageJoinWorkCycle(){

		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.JOINWORKCYCLE);

		b.add(messagetype);
		
		this.message = craftMessage(b);
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Encoding JOIN WORK CYCLE MESSAGE (no arguments)", this);
		}
	}
	
	
	/**
	 *  Handles the JOINSERVICE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	none
	 *   
	 * @param payload the payload
	 */	
	public ManagementMessageJoinWorkCycle(byte[] payload){
		
		message = payload;
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Decoding JOIN WORK CYCLE MESSAGE (no argument)", this);
		}
		
	}
}
