/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.ArrayList;

/**
 * @author klobs
 *
 */
public class ManagementMessageInfoRequest extends ManagementMessage {

	private InfoServiceInfoRequest inforequest = null;
	
	/**
	 * 	Handles the INFOREQUEST management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	TYPE OF INFO REQUEST			2 Byte
	 *		Arguments. See InfoServiceRequest and subclasses for more info.
	 *   
	 *   @param i an instance of InfoServiceRequest
	 */	
	public ManagementMessageInfoRequest(InfoServiceInfoRequest i){

		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype = Util.stuffIntIntoShort(ManagementMessage.INFOREQ);
		
		b.add(messagetype 				);
		b.add(i.getInfoServicerequest());
	
		this.message = craftMessage(b);
		
		Log.print(Log.LOG_DEBUG, "Requesting info from Server", this);
	}
	
	
	/**
	 * 	Handles the INFOREQUEST management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	TYPE OF INFO REQUEST			2 Byte
	 *		Arguments. See InfoServiceRequest and subclasses for more info.
	 *   
	 * @param payload the payload
	 */	
	public ManagementMessageInfoRequest(byte[] payload){
				
		message = payload;
		
		if(payload.length < 2){
				Log.print(Log.LOG_WARN, "Payload length < minimal expected payload Length! Dropping packet!", this);
				errorProcessing = true;
				return;
		}
		
		inforequest = InfoServiceInfoRequest.infoRequestFactory(payload);
		
	}
	
	public InfoServiceInfoRequest getInfoServiceRequest(){
		return inforequest;
	}
}
