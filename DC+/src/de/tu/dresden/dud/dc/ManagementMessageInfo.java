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

public class ManagementMessageInfo extends ManagementMessage {
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
			
			Log.print(Log.LOG_DEBUG, "Sending INFO", this);
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
					Log.print(Log.LOG_WARN, "Payload length < minimal expected payload Length! Dropping packet!", this);
					errorProcessing = true;
					return;
			}
			
			info = InfoServiceInfo.infoFactory(payload);
			
		}
		
		public InfoServiceInfo getInfo(){
			return info;
		}

	
	
}
