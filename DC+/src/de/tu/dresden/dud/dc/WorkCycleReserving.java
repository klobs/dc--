/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author klobs
 * 
 *         In this class the reservation phase is managed. It is just a special
 *         case of the normal sending phase.
 * 
 */
public class WorkCycleReserving extends WorkCycleSending {

	private int 	actualRoundsCalculated	= 0;
	private short	myRandomNumber	 			= 0;
	
	/**
	 * @param r
	 *            the work cycle which the reservation belongs to
	 */
	public WorkCycleReserving(WorkCycle r) {
		super(r);
	
		expectedRounds		= -1;
		
		// do we actually need to reserve?
		if (r.hasPayload()) {
			SecureRandom n 		= new SecureRandom();
			myRandomNumber 	= (short) n.nextInt(Short.MAX_VALUE);
		} 	
	}

	@Override
	protected void addUp() {
		byte[] b = null;
		b = new byte[WorkCycleReservationPayload.RESERVATION_PAYLOAD_SIZE];

		Iterator<byte[]> i = payloads.iterator();

		while (i.hasNext()) {
			byte[] c = i.next();
			b = mergeReserving(b, c);
		}

		payloadSend = b;
	}
	
	@Override
	public byte[] calcKeys(){
		// wenn richtig implementiert wird,
		// check einbauen, dass nicht aus versehen mit 5 keys summiert wird
		byte[] b = new byte[WorkCycleReservationPayload.RESERVATION_PAYLOAD_SIZE];
		Arrays.fill(b, (byte) 0);
		return b;
	}

	/**
	 * Merges down two equally sized byte arrays and apply magical reservation-modul logic:
	 * each 4 bytes are interpreted as short.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] mergeReserving(byte[] a, byte[] b) {

		if (a.length != b.length)
			throw new IllegalArgumentException(
					"The arrays a, b do not have the same length.");

		byte[] c = new byte[a.length];

		for (int i = 0; i< a.length; i = i+4){
			int d = (Util.stuffBytesIntoUInt(Util.getBytesByOffset(a, i, 4)) 
				+ Util.stuffBytesIntoUInt(Util.getBytesByOffset(b, i, 4))) % Integer.MAX_VALUE;
			c[i]   = (byte) (d >>> 24);
			c[i+1] = (byte) (d >>> 16);
			c[i+2] = (byte) (d >>> 8 );
			c[i+3] = (byte) d;
		}
		
		return c;
	}

	private void performDCReservationParticipantSide() {
		WorkCycleReservationPayload		 	a = null;
		WorkCycleReservationPayload 		rp = new WorkCycleReservationPayload(getSystemPayloadLength(), myRandomNumber);
		ManagementMessageAdd 			m = null;
		
		// collision detection
		boolean collisiondetected		= false;
		int 	collisionsuspected		= 0;
		WorkCycleReservationPayload collisionpayload = null;
		
		//	Rounds, wait and waited counter
		int 	rc						= 0;
		int 	wait 					= 0;
		int 	waited					= 0;
		
		while (!finished) {
			
			// Do we want to send our random number in this reservation?
			// This is only the case if we have actually something to send at all,
			// and (it is the first step of reservation or our random number is <= the mean value).
			if ((wait == 0) 
					&& (relativeRound < 0) 
					&& (this.assocWorkCycle.hasPayload())
					&& !collisiondetected) {
				// Yes, we do want, so prepare the message with the right keys.
				byte[] p = mergeSending(rp.getPayload(), calcKeys());  // calcKeys() is @Overwritten in this class
				m = new ManagementMessageAdd(workcycleNumber, rc, p);
				waited = 0;
			} else {
				// no, so only an empty message with the keys has to be sent.
				byte[] p = calcKeys();
				m = new ManagementMessageAdd(workcycleNumber, rc, p);
			}
			
			// send the message.
			try {
				getAssocParticipantManager().getMyInfo().getAssocConnection().sendMessage(m.getMessage());
			} catch (IOException o) {
				Log.print(Log.LOG_ERROR, o.toString(), this);
			}

			// lock the semaphore, because we need to wait for the reaction
			// to continue 
			try{
			assocWorkCycle.getSemaphore().acquire();
			} catch (InterruptedException e) {
				Log.print(Log.LOG_ERROR, e.toString(), this);
			}
			
			// The semaphore has be unlocked form a different thread (the producer, the work cycle)
			// Now we can fetch the long awaited response and continue the algorithm.
			
			a =  new WorkCycleReservationPayload(assocWorkCycle.getAddedMessages().removeFirst().getPayload());
			
			// get the expected count of steps for the reservation, if not done, yet
			// (so only the 1st time.
			if (expectedRounds < 0) expectedRounds = a.getParticipantCount();
			
			if(!assocWorkCycle.hasPayload()){
				wait = 1;
				if(a.getParticipantCount() == 1){
					expectedRounds--;
					actualRoundsCalculated++;
				}
			}
			else if (myRandomNumber <= a.getAverage() &&  relativeRound < 0) { // left side of the branch
				
				// Current average is my random number and I am the only sender
				if ((a.getParticipantCount() == 1) && (myRandomNumber == a.getAverage()))
				{
					relativeRound = actualRoundsCalculated;
					actualRoundsCalculated++;
					expectedRounds--;
				} 
				// Current average is my random sum, but there are more people trying to send it -> collusion
				// Either we do not send, or we take a look at the desiredPayloadlength() TODO
				else if ((a.getParticipantCount() > 1) &&  (myRandomNumber == a.getAverage())) 
				{
					if (rc == collisionsuspected + 1){
						if (collisionpayload.getParticipantCount() == a.getParticipantCount() 
								&& collisionpayload.getAverage() == a.getAverage()){ 
							collisiondetected = true;
							expectedRounds = expectedRounds - a.getParticipantCount();
						}
					}
					collisionsuspected = rc;
					collisionpayload = a;
				} 
				
				wait = 0;
				
			}
			else {
				if (waited == 1) wait = a.getParticipantCount();
				else if (waited == 0) wait   = 1;
				
				waited++;
				
				//Did somebody else find the slot?
				if(a.getParticipantCount() == 1){
					expectedRounds--;
					actualRoundsCalculated++;
					wait--;
				} else if (collisionpayload != null 
						&& collisionpayload.getAverage() == a.getAverage() 
						&& collisionpayload.getParticipantCount() == a.getParticipantCount()){
					expectedRounds = expectedRounds - a.getParticipantCount();
					wait = wait - a.getParticipantCount();
				}
				
				collisionpayload = a;
			}
			
			// inc Round count
			rc++;
			
			// Do we want to send in the next reservation round?
			finished = (expectedRounds == 0);
		}
		
		expectedRounds = actualRoundsCalculated;
		
		if (collisiondetected == true) relativeRound = -1;
		
		// Reservation has finished. Notify the observers!
		setChanged();
		notifyObservers(WorkCycle.WC_RESERVATION_FINISHED);
	}

	@Override
	public void run(){
		switch (method) {

		case WorkCycleManager.METHOD_DC:
			performDCReservationParticipantSide();
			break;
		case WorkCycleManager.METHOD_DCPLUS:
			Log.print(Log.LOG_ERROR,
					"Unsupported reservation method yet: DC PLUS", this);
			break;
		default:
			Log.print(Log.LOG_ERROR, "Unknown reservation method", this);
		}


	}
}
