/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Util;

public class WorkCycleReservationPayload {

	// Logging
	Logger log = Logger.getLogger(WorkCycleReservationPayload.class);

	public static final int RESERVATION_PAYLOAD_SIZE	= 12;
	
	private int		desiredPayloadLength 	= 0;
	private int 	participants			= 0;
	private byte[] 	payload 				= null;
	private long	rand					= 0;
	
	/**
	 * @param payloadlength Which length shall the payload during the sending phase have?
	 * @param rand the random number
	 *	
	 * @return
	 */
	public WorkCycleReservationPayload(int payloadlength, short rand){
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		byte r[] = new byte[0];
		
		byte[] participate		= Util.stuffIntIntoInt(1);
		byte[] pllength			= Util.stuffIntIntoInt(payloadlength);
		byte[] randomnumber		= Util.stuffIntIntoInt(rand);
		
		b.add(participate	);
		b.add(pllength		);
		b.add(randomnumber  );
	
		for (int i = 0; i < b.size(); i++) {
			r = Util.concatenate(r, b.get(i));
		}

		payload = r;
	}
	

	public WorkCycleReservationPayload(byte[] payload) {
		this.payload = payload;

		if (payload.length != RESERVATION_PAYLOAD_SIZE) {
			log.warn("Payload length != minimal expected payload Length! Dropping packet!");
		}

		this.participants = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 0, 4));
		this.desiredPayloadLength = Util.stuffBytesIntoUInt(Util.getBytesByOffset(
				payload, 4, 4));
		this.rand = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(payload, 8, 4));
	}

	public long getAverage(){
		if (participants == 0) return 0;
		else return rand / participants;
	}
	
	public int getDesiredPayloadLength(){
		return desiredPayloadLength;
	}
	
	public int getParticipantCount(){
		return participants;
	}
	
	public byte[] getPayload(){
		return payload;
	}

	public long getRandom(){
		return rand;
	}
}
