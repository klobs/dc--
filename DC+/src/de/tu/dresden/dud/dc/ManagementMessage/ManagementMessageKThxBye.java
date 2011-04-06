/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 *
 */
public class ManagementMessageKThxBye extends ManagementMessage {

    // Logging
    private static Logger log = Logger.getLogger(ManagementMessageKThxBye.class);

	/**
	 * 	Handles the QUITSERVICE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.13
	 *  
	 *  Fields are:
	 * 		no fields 
	 */	
	public ManagementMessageKThxBye() {

		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.KTHXBYE);
		
		b.add(messagetype 	);
		
		this.message = craftMessage(b);
		
		if(log.isDebugEnabled()){
			log.debug("Encoding QUITSERVICE confirmation message");
		}
	}
	
	
	/**
	 * 	Handles the QUITSERVICE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.13
	 *  
	 *  Fields are:
	 *  	no fields
	 */	
	public ManagementMessageKThxBye(byte[] payload){
		
		
		message = payload;
		
		log.debug("Decoding QUITSERVICE confirmation message");
	}
}
