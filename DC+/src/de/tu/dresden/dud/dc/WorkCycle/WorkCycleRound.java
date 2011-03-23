/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.util.LinkedHashSet;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdd;

/**
 * A round represents the smallest step in a DC / DC+ work cycle.
 * 
 * Each round belongs to a work cycle and has a unique round number. It
 * logically consists of each participant generating it's local sum, sending it
 * to the server and receiving the global sum.
 * 
 * @author klobs
 * 
 */
public class WorkCycleRound extends WorkCycle {

	// Logging
	Logger log = Logger.getLogger(WorkCycleRound.class);
	
	private int 			roundNumber = 0;

	/**
	 * Each round belongs to a work cycle and has a unique round number.
 	 * 
	 * @param roundNumber unique round number to identify the round
	 * @param r the {@link WorkCycleSending} which the round belongs to. 
	 */
	WorkCycleRound(int roundNumber, WorkCycleSending r){
		this.expectedConnections = (LinkedHashSet<Connection>) r.getConnections().clone(); // TODO can we find a less expansive possibility here?
		this.workCycleSending = r;
		this.roundNumber = roundNumber;		
	}

	/**
	 * This method is called on server side after an
	 * {@link ManagementMessageAdd} arrived over the {@link Connection}.
	 * 
	 * This method is responsible for collecting all expected add messages, and
	 * as soon as all expected add messages arrived, notify the observers (which
	 * is usually only the "parent" {@link WorkCycleSending}) that the global sum
	 * can be calculated.
	 * 
	 * @param c
	 *            the connection over which the ADD message arrived
	 * @param m
	 *            the management message that arrived.
	 */
	public synchronized void addMessageArrived(Connection c, ManagementMessageAdd m){
		payloads.add(m.getPayload());
		
		expectedConnections.remove(c);
		confirmedConnections.add(c);

		log.debug(
				"New ADD message for work cycle "+ m.getWorkCycleNumber() +" and round "+ m.getRoundNumber() +" is being processed. There are now "
						+ confirmedConnections.size()
						+ " confirmed messages and "
						+ expectedConnections.size()
						+ " more expected  messages");

		checkWhetherToAddUp();
	}
	
	public void checkWhetherToAddUp(){
		if (expectedConnections.size() == 0) {
			log.debug("	there are no more new messages expected. ADDing notification");
			setChanged();
			notifyObservers(WorkCycle.WC_ROUND_ADDUP);
		}	
	}
	
	/**
	 * Standard getter
	 * @return The unique round number.
	 */
	public int getRoundNumber(){
		return this.roundNumber;
	}
}
