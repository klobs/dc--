/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoRequest;

/**
 * @author klobs
 *
 */
public class ManagementMessageInfoRequest extends ManagementMessage {

	// Logging
	private Logger log = Logger.getLogger(ManagementMessageInfoRequest.class);
	
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
		
		log.debug("Requesting info from Server");
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
				log.warn("Payload length < minimal expected payload Length! Dropping packet!");
				errorProcessing = true;
				return;
		}
		
		inforequest = InfoServiceInfoRequest.infoRequestFactory(payload);
		
	}
	
	public InfoServiceInfoRequest getInfoServiceRequest(){
		return inforequest;
	}
}
