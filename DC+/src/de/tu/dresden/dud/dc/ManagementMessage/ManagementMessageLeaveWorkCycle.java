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
public class ManagementMessageLeaveWorkCycle extends ManagementMessage {

    // Logging
    private Logger log = Logger.getLogger(ManagementMessageLeaveWorkCycle.class);

	private long workcyclenumber;

	/**
	 * 	Handles the LEAVWORKCYCLE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.12
	 *  
	 *  Fields are:
	 *  	WORK CYCLE NUMBER						8 Bytes
	 *   
	 * @param	workCycleNumber the work cycle number 
	 */	
	public ManagementMessageLeaveWorkCycle(long workCycleNumber) {

		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.LEAVEWORKCYCLE);
		byte[] workcyclenumber		= Util.stuffLongIntoLong(workCycleNumber);
		
		b.add(messagetype 	);
		b.add(workcyclenumber	);
	
		this.message = craftMessage(b);
		
		if(log.isDebugEnabled()){
			log.debug("Encoding LEAVEWORKCYCLE message");
			log.debug("	Work cycle number: " + workCycleNumber); 
			log.trace(" / " + Arrays.toString(workcyclenumber));
		}
	}
	
	
	/**
	 * 	Handles the LEAVEWORKCYCLE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.12
	 *  
	 *  Fields are:
	 *  	WORK CYCLE NUMBER						8 Bytes
	 *   
	 * @param payload the payload 
	 */	
	public ManagementMessageLeaveWorkCycle(byte[] payload){
		
		
		message = payload;
		
		if(payload.length != 8){
				log.warn( "Payload length != expected payload Length! Dropping packet!");
				errorProcessing = true;
		}
		
		workcyclenumber = Util.stuffBytesIntoLongUnsigned(payload);
		
		log.debug("Decoding LEAVEWORKCYCLE message");
		log.debug("	Work cycle number: " + workcyclenumber);
	}
		
	public long geWorkCycleNumber() {
		return workcyclenumber;
	}
}
