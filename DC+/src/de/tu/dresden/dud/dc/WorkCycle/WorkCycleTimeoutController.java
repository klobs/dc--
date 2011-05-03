package de.tu.dresden.dud.dc.WorkCycle;

import org.apache.log4j.Logger;

public class WorkCycleTimeoutController implements Runnable {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycleManager.class);

	
	private long timeout = 0;
	private WorkCycle workCycle = null;
	
	public WorkCycleTimeoutController(long timeout, WorkCycle wc){
		this.timeout = timeout;
		this.workCycle = wc;
	}
	
	@Override
	public void run() {
		if (timeout > 0) {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				log.warn(e.toString());
			}

			if (workCycle.getCurrentPhase() != WorkCycle.WC_FINISHED) {
				log.info("There are "
						+ workCycle.getRemainingConnections().size()
						+ " connections that have not yet sent their part.");
			} else {
				log.info("WorkCycle finished within the boundaries of timeout. - Nothing to complain about.");
			}
		}
	}
}
