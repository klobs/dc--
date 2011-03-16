/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;

public class ManagementMessageAccepted4Service extends ManagementMessage{

    // Logging
    private Logger log = Logger.getLogger(ManagementMessageAccepted4Service.class);

	
	public static final int ACCEPTED 	= 0;
	public static final int REJECTED 	= 1;
	
	public 				int accepted 	= -1;
	
	/**
	 * 	Generates the ACCEPTED4SERVICE management message. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	ACCEPTED*						1 Byte
	 *   
	 * @param a the reason for accepting, or better not accepting a participant.
	 */
	public ManagementMessageAccepted4Service(int a){
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype	= Util.stuffIntIntoShort(ManagementMessage.ACCEPTED4SERVICE);
		byte[] acc		 	= Util.stuffIntIntoByte(a);
		
		
		b.add(messagetype	);
		b.add(acc 			);
		
		if(log.isDebugEnabled()){
			log.debug("Encoding ACCEPTED4SERVICE MESSAGE");
			log.debug("	Server accepted / rejected reason number: " + String.valueOf(a) );
			log.trace(" / " + Arrays.toString(acc));
		}
		
		message = craftMessage(b);
	}

	
	/**
	 * 	Generates the ACCEPTED4SERVICE management message. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	ACCEPTED*						1 Byte
	 *   
	 */
	 public ManagementMessageAccepted4Service(byte[] payload){
		
		if(payload.length != 1){
				log.warn( "Payload does not correspond to the required fixed size");
				errorProcessing = true;
		}
		
		accepted			= payload[0];
		
		log.debug("Decoding ACCEPTED4SERVICE MESSAGE");
		log.debug("	Server accpeted / rejected reason number: " + String.valueOf(accepted));
	}
	 
	 /**
	  * getter for the accepted variable.
	  * @return 0, or >
	  */
	 public int getAccepted(){
		 return accepted;
	 }
	
	
}
