package de.tu.dresden.dud.dc.WorkCycle;

import java.util.LinkedHashSet;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;

public class WorkCycleRoundTimeoutController implements Runnable {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycleManager.class);

	
	private long timeout = 0;
	private WorkCycleRound round = null;
	
	public WorkCycleRoundTimeoutController(long timeout, WorkCycleRound r){
		this.timeout = timeout;
		this.round = r;
	}
	
	@Override
	public void run() {
		if (timeout > 0) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				return;
			}

			synchronized (round.getExpectedConnections()) {

				if (round.getExpectedConnections().size() > 0) {
					log.info("There are "
							+ round.getExpectedConnections().size()
							+ " connections that have not yet sent their part.");

					 LinkedHashSet<Connection> ec =
					 round.getExpectedConnections();
					
					for (Connection c : ec) {
						c.handleEarlyQuitConnectionUnresponsive();
					}

				} else {
					log.info("Round finished within the boundaries of timeout. - Nothing to complain about.");
				}
			}
		}
	}
}
