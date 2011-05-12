package de.tu.dresden.dud.dc.WorkCycle;

import org.apache.log4j.Logger;

public class WorkCycleTimeoutController implements Runnable {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycleManager.class);

	private static final int WORKCYCLEMAXTIMEOUT = 3000;
	
	private WorkCycleSending workCycle = null;
	
	public WorkCycleTimeoutController(WorkCycleSending wc){
		this.workCycle = wc;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(WORKCYCLEMAXTIMEOUT);
		} catch (InterruptedException e) {
			return;
		}

		synchronized (workCycle) {

			if (!workCycle.hasAnyAddMessageArrivedAtServerside()) {
				log
						.info("Those lazy participants have not sent one message, yet... :( Trying to sync");
				workCycle.getAssocWorkCycleManager().tickServerSide();
			}
		}
	}
}
