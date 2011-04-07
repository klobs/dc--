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

    public static final short QUITOK_INVALID		= -1;
    public static final short QUITOK_ALL_OK 		= 0;
    public static final short QUITOK_LEAVE_WC_FIRST	= 1;
    
    private int quitOk = QUITOK_INVALID;
	/**
	 * 	Handles the QUITSERVICE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.13
	 *  
	 *  Fields are:
	 * 		no fields 
	 */	
	public ManagementMessageKThxBye(final short quitOk) {

		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.KTHXBYE);
		
		b.add(messagetype 	);
		b.add(Util.stuffIntIntoShort(quitOk));
		
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
		
		quitOk = Util.stuffBytesIntoUInt(Util.getFirstBytes(payload, 2));
		
		log.debug("Decoding QUITSERVICE confirmation message");
	}
	
	public int getQuitOK(){
		return quitOk;
	}
	
}
