/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 *
 */
public class ManagementMessageTick extends ManagementMessage {

    // Logging
    private Logger log = Logger.getLogger(ManagementMessageTick.class);


	private long workcyclenumber;

	/**
	 * 	Handles the TICK management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	WORK CYCLE NUMBER						8 Bytes
	 *   
	 * @param workCycleNumber the work cycle number
	 */	
	public ManagementMessageTick(long workCycleNumber) {

// TODO
//remove me
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//remove me
	
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.TICK);
		byte[] workcyclenumber		= Util.stuffLongIntoLong(workCycleNumber);
		
		b.add(messagetype 	);
		b.add(workcyclenumber	);
	
		this.message = craftMessage(b);
		
		log.debug("Encoding TICK MESSAGE");
		log.debug("	Work cycle number: " + workCycleNumber  + " / " + Arrays.toString(workcyclenumber));
	}
	
	
	/**
	 * 	Handles the TICK management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	WORK CYCLE NUMBER						8 Bytes
	 *   
	 * @param payload the payload 
	 */	
	public ManagementMessageTick(byte[] payload){
		
		
		message = payload;
		
		if(payload.length != 8){
				log.warn( "Payload length != expected payload Length! Dropping packet!");
				errorProcessing = true;
		}
		
		workcyclenumber = Util.stuffBytesIntoLongUnsigned(payload);
		
		log.debug("Decoding TICK MESSAGE");
		log.debug(" Work cycle number: " + workcyclenumber);
	}
		
	public long getWorkCycleNumber() {
		return workcyclenumber;
	}
}
