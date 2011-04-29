package de.tu.dresden.dud.dc.WorkCycle;

import org.apache.log4j.Logger;

public class WorkCycleTimeoutController implements Runnable {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycleManager.class);

	
	private long timeout = 0;
	
	public WorkCycleTimeoutController(long timeout, WorkCycle wc){
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			log.warn(e.toString());
		}
	}

}
