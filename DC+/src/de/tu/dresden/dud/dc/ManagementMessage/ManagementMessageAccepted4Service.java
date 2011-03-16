/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Util;

public class ManagementMessageAccepted4Service extends ManagementMessage{

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
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Encoding ACCEPTED4SERVICE MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Server accepted / rejected reason number: " + String.valueOf(a) + " / " , acc, this);
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
				Log.print(Log.LOG_WARN, "Payload does not correspond to the required fixed size", this);
				errorProcessing = true;
		}
		
		accepted			= payload[0];
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Decoding ACCEPTED4SERVICE MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Server accpeted / rejected reason number: " + String.valueOf(accepted), this);
		}
		
	}
	 
	 /**
	  * getter for the accepted variable.
	  * @return 0, or >
	  */
	 public int getAccepted(){
		 return accepted;
	 }
	
	
}
