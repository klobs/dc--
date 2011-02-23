/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.IOException;
import java.security.SecureRandom;
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
			b = mergeDCwise(b, c);
		}

		payloadSend = b;
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
				byte[] p = mergeDCwise(rp.getPayload(), calcKeysMain(WorkCycleReservationPayload.RESERVATION_PAYLOAD_SIZE));
				m = new ManagementMessageAdd(workcycleNumber, rc, p);
				waited = 0;
			} else {
				// no, so only an empty message with the keys has to be sent.
				byte[] p = calcKeysMain(WorkCycleReservationPayload.RESERVATION_PAYLOAD_SIZE);
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
			performDCReservationParticipantSide();
			break;
		default:
			Log.print(Log.LOG_ERROR, "Unknown reservation method", this);
		}


	}
}
