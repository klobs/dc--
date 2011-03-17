/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;


import de.tu.dresden.dud.dc.Gui.Gui;

	
/**
 * SuperDuperTrooper-Class. Everything gets initialized here
 * @author klobs
 */
public class DCService {

	/**
	 * @param args No args are expected. No args are processed.
	 */
	public static void main(String[] args) {

	    BasicConfigurator.configure();
		
	    Logger.getRootLogger().setLevel(Level.DEBUG);
		
		Gui mainWindow = new Gui();
		mainWindow.setVisible(true);
	}

	public DCService(){}
	

}
