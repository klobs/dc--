/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;


import java.util.Observable;

public class Log extends Observable{ // TODO -- Javax.log
	
	// internal variables
	private int 				currentLogLevel 	= 10;
	private static Log 			log 				= null;
	private boolean 			observable			= true;
		
	private Log(){}
	
	/**
	 * This is a singleton, so use this instead of the constructor!
	 * @return the log facility for a server.
	 */
	public static Log getInstance(){
		if (log == null)
			log = new Log();
		return log;
	}
	
	/**
	 * @return the current log level
	 */
	public int getLogLevel(){
		return this.currentLogLevel;
	}
	
	
	/**
	 * Tells you wheather the instance of this object is 
	 * in observable mode.
	 * @return
	 */
	public boolean observable(){
		return this.observable;
	}
	
	
	/**
	 * set the loglevel.
	 * @param newLogLevel for the levels see class description.
	 */
	public void setLogLevel(int newLogLevel){
		if ((newLogLevel >= 0) && (newLogLevel <= 10))
				this.currentLogLevel = newLogLevel;
	}
	
	/**
	 * Sets whether this instance of this object shall be observable!
	 * @param b
	 */
	public void setObservable(boolean b){
		observable = b;
	}
	
}
