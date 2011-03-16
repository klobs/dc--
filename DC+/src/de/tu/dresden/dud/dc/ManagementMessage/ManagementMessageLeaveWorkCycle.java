/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 *
 */
public class ManagementMessageLeaveWorkCycle extends ManagementMessage {


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
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Encoding LEAVEWORKCYCLE message", this);
			Log.print(Log.LOG_DEBUG, "	Work cycle number: " + workCycleNumber  + " / " , workcyclenumber, this);
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
				Log.print(Log.LOG_WARN, "Payload length != expected payload Length! Dropping packet!", this);
				errorProcessing = true;
		}
		
		workcyclenumber = Util.stuffBytesIntoLongUnsigned(payload);
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Decoding LEAVEWORKCYCLE message", this);
			Log.print(Log.LOG_DEBUG, "	Work cycle number: " + workcyclenumber, this);
		}
		
	}
		
	public long geWorkCycleNumber() {
		return workcyclenumber;
	}
}
