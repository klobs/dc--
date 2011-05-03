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
public class ManagementMessageAdd extends ManagementMessage {
	
    // Logging
    private static Logger log = Logger.getLogger(ManagementMessageAdd.class);
	
	private byte[]	payload;
	private int		payloadLength 	= 0;
	private long 	workcyclenumber	= 0; //Long.MIN_VALUE;
	private int		roundnumber	= 0; //is transmitted as 2 bytes

	/**
	 * 	Handles the ADD management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	WORK CYCLE NUMBER				8 Byte
	 *  	ROUND NUMBER					2 Byte
	 *  	PAYLOAD							variable (is defined in the WELCOME2SERVICE Message)
	 *   
	 * @param wcn the destination work cycle number
	 * @param wcn the round number
	 * @param payload the payload that the participant intends to send.
	 */	
	public ManagementMessageAdd(long wcn, int rn, byte[] payload){
		realtime = true;
		
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.ADD);
		byte[] workcycle		= Util.stuffLongIntoLong(wcn);
		byte[] round			= Util.stuffIntIntoShort(rn);
		
		b.add(messagetype	);
		b.add(workcycle			);
		b.add(round		);
		b.add(payload		);
	
		this.message = craftMessage(b);
		
		log.debug("Encoding ADD MESSAGE");
	}
	
	
	/**
	 * 	Handles the ADD management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.5
	 *  
	 *  Fields are:
	 *  	WORK CYCLE NUMBER				8 Byte
	 *  	ROUND NUMBER					2 Byte
	 *  	PAYLOAD							variable (is defined in the WELCOME2SERVICE Message)
	 *   
	 */	
	public ManagementMessageAdd(byte[] payload){
		realtime = true;
		message = payload;
		
		if(payload.length < 8){
				log.warn( "Payload length < minimal expected payload Length! Dropping packet!");
				errorProcessing = true;
		}
		
		this.workcyclenumber 	=  Util.stuffBytesIntoLongUnsigned(Util.getFirstBytes(payload, 8));
		this.roundnumber =  Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 8, 2));
		this.payload 		=  Util.getBytesByOffset(payload, 10, payload.length - 10); 
		this.payloadLength 	=  this.payload.length;
		
				
		if(log.isDebugEnabled()){
			log.debug("Decoding ADD MESSAGE");
			log.debug("	Work cycle number: " + this.workcyclenumber);
			log.debug("	Round number: " + this.roundnumber);
			log.debug("	Payload: " + Arrays.toString(this.payload));
		}
		
	}
		
	public long getWorkCycleNumber() {
		return workcyclenumber;
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public int getPayloadLength(){
		return payloadLength;
	}

	public int getRoundNumber(){
		return roundnumber;
	}

}
