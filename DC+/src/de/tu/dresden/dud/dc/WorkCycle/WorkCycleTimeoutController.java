package de.tu.dresden.dud.dc.WorkCycle;

import org.apache.log4j.Logger;

public class WorkCycleTimeoutController implements Runnable {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycleManager.class);

	
	private int maxtimeout = 3000;
	private WorkCycleSending workCycle = null;
	
	public WorkCycleTimeoutController(WorkCycleSending wc, int timeout){
		this.maxtimeout = timeout;
		this.workCycle = wc;
	}
	
	@Override
	public void run() {
		if (maxtimeout > 0) {

			try {
				Thread.sleep(maxtimeout);
			} catch (InterruptedException e) {
				return;
			}

			synchronized (workCycle) {

				log
						.info("Those lazy participants have not sent one message, yet... :( Trying to sync");
				workCycle.getAssocWorkCycleManager().tickServerSide();
			}
		}
	}
}
