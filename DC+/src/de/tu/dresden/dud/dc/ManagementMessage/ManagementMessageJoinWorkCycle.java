/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;

/**
 * The implementation for the JOINWORKCYCLE Management Message
 * 
 * @author klobs
 */
public class ManagementMessageJoinWorkCycle extends ManagementMessage {
	
    // Logging
    private static Logger log = Logger.getLogger(ManagementMessageJoinWorkCycle.class);
	
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
		
		log.debug("Encoding JOIN WORK CYCLE MESSAGE (no arguments)");
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
		
		log.debug("Decoding JOIN WORK CYCLE MESSAGE (no argument)");
	}
}
