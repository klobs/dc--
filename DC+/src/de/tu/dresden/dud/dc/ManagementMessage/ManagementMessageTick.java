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
public class ManagementMessageTick extends ManagementMessage {


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
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Encoding TICK MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Work cycle number: " + workCycleNumber  + " / " , workcyclenumber, this);
		}
	
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
				Log.print(Log.LOG_WARN, "Payload length != expected payload Length! Dropping packet!", this);
				errorProcessing = true;
		}
		
		workcyclenumber = Util.stuffBytesIntoLongUnsigned(payload);
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Decoding TICK MESSAGE", this);
			Log.print(Log.LOG_DEBUG, " Work cycle number: " + workcyclenumber, this);
		}
		
	}
		
	public long getWorkCycleNumber() {
		return workcyclenumber;
	}
}
