/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import de.tu.dresden.dud.dc.Server;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;

/**
 * @author klobs_admin
 * 
 *         This class can be seen as {@link WorkCycleReserving} for the
 *         {@link Server} side. It helps the server to track in which phase the
 *         reservation is current. The server needs to track reservations for
 *         two reasons:
 * 
 *         1. To know when to switch from reservation message length to the
 *         normal message length (which might be dynamic in future).
 * 
 *         2. To know how many rounds are to expect. (And after they arrive,
 *         send a TICK).
 * 
 */
public class ServerReservationChecker {

	private boolean finished = false;
	
	private WorkCycleReservationPayload		 	a 	= null;
	
	// collision detection
	private WorkCycleReservationPayload collisionpayload 		= null;

	//	Rounds counter
	private int 	rc						= 0;
	
	// work cycle information
	private int 	expectedRounds			= -1;
	private int 	actualRoundsCalculated 	= 0;
	
	/**
	 * The content of this method is mostly a copy from 
	 * performDCReservationParticipantSide(), {@link WorkCycleReserving}
	 */
	public boolean hasReservationFinished(ManagementMessageAdded m) {
		
		a = new WorkCycleReservationPayload(m.getPayload());

		// get the expected count of steps for the reservation, if not done, yet
		// (so only the 1st time.
		if (expectedRounds < 0)
			expectedRounds = a.getParticipantCount();

		if (a.getParticipantCount() == 1) {
			expectedRounds--;
			actualRoundsCalculated++;
		}

		if (collisionpayload != null
				&& collisionpayload.getAverage() == a.getAverage()
				&& collisionpayload.getParticipantCount() == a
						.getParticipantCount()) {
			expectedRounds = expectedRounds - a.getParticipantCount();
		}
				
		collisionpayload = a;

		// inc round count
		rc++;

		// Do we want to send in the next reservation round?
		finished = (expectedRounds == 0);
		return finished;
	}

	/**
	 * Standard getter.
	 * 
	 * This method can be called on server side after the reservation took
	 * place.
	 * 
	 * @return How many rounds are to be expected in current work cycle after
	 *         reservation.
	 */
	public int getExpectedRounds(){
		return actualRoundsCalculated;
	}
}
