/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfo;


/**
 * @author klobs
 *
 */

public class ManagementMessageInfo extends ManagementMessage {
	
    // Logging
    private static Logger log = Logger.getLogger(ManagementMessageInfo.class);
	
	private InfoServiceInfo info = null;
		
		/**
		 * 	Handles the INFO management message.
		 *  Handling means interpreting the payload and setting corresponding 
		 *  internal flags. 
		 *  Introduced in protocol Version 0.0.5
		 *  
		 *  Fields are:
		 *  	TYPE OF INFO				2 Byte
		 *		Arguments. See InfoServiceRequest and subclasses for more info.
		 *   
		 *   @param i an instance of InfoServiceRequest
		 */	
		public ManagementMessageInfo(InfoServiceInfo i){

			ArrayList<byte[]> b = new ArrayList<byte[]>();
			
			byte[] messagetype = Util.stuffIntIntoShort(ManagementMessage.INFO);
			
			b.add(messagetype 				);
			b.add(i.getInfo() 				);
		
			this.message = craftMessage(b);
			
			log.debug("Sending INFO");
		}
		
		
		/**
		 * 	Handles the INFO management message.
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
		public ManagementMessageInfo(byte[] payload){
					
			message = payload;
			
			if(payload.length < 2){
					log.warn( "Payload length < minimal expected payload Length! Dropping packet!");
					errorProcessing = true;
					return;
			}
			
			info = InfoServiceInfo.infoFactory(payload);
			
		}
		
		public InfoServiceInfo getInfo(){
			return info;
		}

	
	
}
