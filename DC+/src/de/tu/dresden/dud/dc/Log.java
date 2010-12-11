/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.Arrays;
import java.util.Observable;

/**
 * @author klobs
 * 
 * This is the logging facility of the service. Each Server can has one.
 * (Singleton)
 * 
 * By now there are following log-verbosities
 * 
 * 0...1 = ERROR
 * 2...3 = WARN
 * 4...5 = INFO
 * 6...9 = DEBUG
 */
public class Log extends Observable{ // TODO -- Javax.log

	// Loglevel
	
	/**
	 * Log level. Take this log level for logging output that happens on actual errors.
	 */
	public static final int LOG_ERROR 	= 1;
	
	/**
	 * Log level. Take this log level for logging output that is a warning. (E.g. fixable errors)
	 */
	public static final int LOG_WARN  	= 3;
	
	/**
	 * Log level. Take this log level for logging output that is just for info
	 */
	public static final int LOG_INFO	= 5;
	
	/**
	 * Log level. Take this log level for all those useless developer infos...
	 */
	public static final int LOG_DEBUG	= 9;
	
	// internal variables
	private int 				currentLogLevel 	= 10;
	private static Log 			log 				= null;
	private boolean 			observable			= true;
		
	private Log(){}
	
	private String getCallerClassName(){
		Throwable 			t 					= new Throwable(); 
		StackTraceElement[] elements 			= t.getStackTrace(); 
		if (elements.length >= 3)
			return elements[2].getClassName();
		else 
			return "";
	}
	
	
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
	 * make some debug output
	 * @param logLevel which log level is this message (for different levels, see class description)
	 * @param l string that has to be logged.
	 * @param sender sender of the log message
	 */
	public static void print(int logLevel, String l, Object sender){
		if (Log.getInstance().getLogLevel() > 6)
			System.out.print(Log.getInstance().getCallerClassName() + " ");
		
		if (logLevel < Log.getInstance().getLogLevel())
			System.out.println(l);
		
		if (Log.getInstance().observable() == true) {
			Log.getInstance().setChanged();
			Log.getInstance().notifyObservers(sender.toString() + "|" + l);
		}
	}
	
	
	/**
	 * make some debug output.
	 * this is specially usefull for management messages (as they are prepared out of byte-arrays)
	 * Object s is the sender of the log message;
	 * @param logLevel
	 * @param l
	 * @param b
	 * @param sender sender of the log message
	 */
	public static void print(int logLevel, String l, byte[] b, Object sender){
		if (Log.getInstance().getLogLevel() > 6)
			System.out.print(Log.getInstance().getCallerClassName() + " ");	
		
		String m = l.concat(Arrays.toString(b));
		
		if (logLevel < Log.getInstance().getLogLevel()){
			System.out.println(m);
		}

		if (Log.getInstance().observable() == true) {
			Log.getInstance().setChanged();
			Log.getInstance().notifyObservers(sender.toString() + "|" + m);
		}
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
