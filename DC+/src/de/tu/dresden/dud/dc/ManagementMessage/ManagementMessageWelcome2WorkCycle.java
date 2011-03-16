/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Participant;
import de.tu.dresden.dud.dc.Util;

/**
 * Everybody that wants to take part in work cycles, needs to receive this
 * confirmation before.
 * 
 * @author klobs
 * 
 */
public class ManagementMessageWelcome2WorkCycle extends ManagementMessage {

	public static final int ACCEPTED = 0;
	public static final int REJECTED = 1;

	private int accepted = -1;
	private LinkedList<Participant> activeParticipant = new LinkedList<Participant>();
	private long workcycle = -1;
	private int timeout = -1;

	/**
	 * Generates the ManagementMessageWelcome2WorkCycle management message.
	 * Introduced in protocol Version 0.0.11
	 * 
	 * Fields are: 
	 * 
	 * 		ACCEPTED 						1 Byte 
	 * 		WORK CYCLE 						8 Byte 
	 * 		TIMEOUT 						2 Byte 
	 * 		PARTCOUNT 						2 Byte
	 * 		1st PART ID LENGTH 				2 Byte 
	 * 		1st PART ID 					variable  
	 * 		1st PART USERNAME LENGTH 		2 Byte
	 *      1st PART USERNAME				variable 
	 *      1st PART DSA SIGNATURE LENGTH 	2 Byte
	 *      1st PART DSA SIGNATURE 			variable
	 *      1st PART DH PUBLIC PART LENGTH	2 Byte
	 *      1st PART DH PUBLIC PART			variable
	 *      1st PART PUBLIC PART SIGNATURE L. 2 Byte
	 *      1st PART PUBLIC PART SIGNATURE	variable...
	 *      ... and so on for every active participant.
	 * 		 
	 * @param accepted
	 *            accepted / rejected
	 * @param workCycleNumber
	 *            the work cycle number in which the participant is expected to
	 *            participate.
	 * @param timeout
	 *            timeout in ms
	 */
	public ManagementMessageWelcome2WorkCycle(int accepted, long workCycleNumber,
			int timeout, ArrayList<Participant> activeConnections) {
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		Participant p = null;

		byte[] messagetype = Util.stuffIntIntoShort(ManagementMessage.WELCOME2WORKCYCLE);
		byte[] acc = Util.stuffIntIntoByte(accepted);
		byte[] rno = Util.stuffLongIntoLong(workCycleNumber);
		byte[] to = Util.stuffIntIntoShort(timeout);
		byte[] ac = Util.stuffIntIntoShort(activeConnections.size());

		b.add(messagetype);
		b.add(acc);
		b.add(rno);
		b.add(to);
		b.add(ac);

		for (Iterator<Participant> i = activeConnections.iterator(); i
				.hasNext();) {
			p = i.next();
			b.add(Util.stuffIntIntoShort(p.getId().length()));
			b.add(p.getId().getBytes());
			b.add(Util.stuffIntIntoShort(p.getUsername().length()));
			b.add(p.getUsername().getBytes());
			b.add(Util.stuffIntIntoShort(p.getDSAPublicSignature().length));
			b.add(p.getDSAPublicSignature());
			b.add(Util.stuffIntIntoShort(p.getDHPublicPart().length));
			b.add(p.getDHPublicPart());
			b.add(Util.stuffIntIntoShort(p.getDHPublicPartSignature().length));
			b.add(p.getDHPublicPartSignature());
		}

		if (Log.getInstance().getLogLevel() >= Log.LOG_DEBUG) {
			Log.print(Log.LOG_DEBUG, "Encoding WELCOME2WORKCYCLE MESSAGE", this);
			Log.print(Log.LOG_DEBUG,
					"	Server accepted / rejected reason number: "
							+ String.valueOf(accepted) + " / ", acc, this);
			Log.print(Log.LOG_DEBUG, "	Send message for work cycle number: "
					+ String.valueOf(workCycleNumber) + " / ", rno, this);
			Log.print(Log.LOG_DEBUG, "	The timeout in ms has been set to: "
					+ String.valueOf(timeout) + " / ", to, this);
		}

		message = craftMessage(b);
	}

	/**
	 * Generates the ManagementMessageWelcome2WorkCycle management message.
	 * Introduced in protocol Version 0.0.11
	 * 
	 * Fields are: 
	 * 
	 * 		ACCEPTED 						1 Byte 
	 * 		WORK CYCLE 						8 Byte 
	 * 		TIMEOUT 						2 Byte 
	 * 		PARTCOUNT 						2 Byte
	 * 		1st PART ID LENGTH 				2 Byte 
	 * 		1st PART ID 					variable  
	 * 		1st PART USERNAME LENGTH 		2 Byte
	 *      1st PART USERNAME				variable 
	 *      1st PART DSA SIGNATURE LENGTH 	2 Byte
	 *      1st PART DSA SIGNATURE 			variable
	 *      1st PART DH PUBLIC PART LENGTH	2 Byte
	 *      1st PART DH PUBLIC PART			variable
	 *      1st PART PUBLIC PART SIGNATURE L. 2 Byte
	 *      1st PART PUBLIC PART SIGNATURE	variable...
	 *      ... and so on for every active participant.
	 * 		 
	 * @param accepted
	 *            accepted / rejected
	 * @param workcycleNumber
	 *            the work cycle number in which the participant is expected to
	 *            participate.
	 * @param timeout
	 *            timeout in ms
	 */
	public ManagementMessageWelcome2WorkCycle(byte[] payload) {
		int pc = 0;
		int ul = 13;

		ArrayList<byte[]> p = new ArrayList<byte[]>(5);
		
		if (payload.length < 13) {
			Log.print(Log.LOG_WARN,
					"Payload does not correspond to the required min size",
					this);
			errorProcessing = true;
		}

		message = payload;

		accepted = payload[0];
		workcycle = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(payload, 1, 8));
		timeout = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 9, 2));
		pc = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 11, 2));

		for (int i = 0; i < pc; i++) {
			for (int j = 0; j < 5; j++) {
				if (payload.length >= ul + 2) {
					int pl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, ul, 2));
					ul = ul + 2;
					if (payload.length >= ul + pl) {
						p.add(Util.getBytesByOffset(payload, ul, pl));
						ul = ul + pl;
					} else {
						Log
								.print(
										Log.LOG_WARN,
										"Payload has strange differences between indicated and effective length: wrong information about id lengths",
										this);
						errorProcessing = true;
					}
				} else {
					Log
							.print(
									Log.LOG_WARN,
									"Payload has strange differences between indicated and effective length: not enough data for the indicated number of users",
									this);
					errorProcessing = true;
				}
			}
			activeParticipant.add(new Participant(new String(p.get(0)),
					new String(p.get(1)), p.get(2), p.get(3), p.get(4)));
			p.clear();
		}

		if (Log.getInstance().getLogLevel() >= Log.LOG_DEBUG) {
			Log.print(Log.LOG_DEBUG, "Decoding WELCOME2WORKCYCLE MESSAGE", this);
			Log.print(Log.LOG_DEBUG,
					"	Server accpeted / rejected reason number: "
							+ String.valueOf(accepted), this);
			Log.print(Log.LOG_DEBUG,
					"	Server want participant in work cycle number: "
							+ String.valueOf(workcycle), this);
			Log.print(Log.LOG_DEBUG, "	Timeout in ms has been set to: "
					+ String.valueOf(timeout), this);
		}

	}

	/**
	 * getter for the accepted variable.
	 * 
	 * @return 0, or >
	 */
	public boolean isAccepted() {
		if(accepted == ACCEPTED) 
			return true;
		return false;
	}

	public LinkedList<Participant> getActiveParticipantIDs() {
		return activeParticipant;
	}

	/**
	 * @return the work cycle
	 */
	public long getWorkCycle() {
		return workcycle;
	}

	/**
	 * @return the timeout
	 */
	public int getTimeout() {
		return timeout;
	}

}
