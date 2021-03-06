/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 *
 */
public abstract class InfoServiceInfo {

	// here we have a list of available information requests.
	public static final int INFO_PASSIVEPARTICIPANTLIST 	= 0;
	public static final int INFO_ACTIVEPARTICIPANTLIST 		= 1;
	public static final int INFO_UPDATEACTIVEJOINING 		= 2;
	public static final int INFO_UPDATEACTIVELEAVING 		= 3;
	public static final int INFO_COMMITKEYEXCHANGE	 		= 4;
	public static final int INFO_EARLYQUITNOTIFICATION 		= 5;
	
	// Inforequests that are handles by the server need an associated server to answer.
	protected byte[] 		info			 		= new byte[0];
	protected Connection 	requestingconnection 	= null;
	
	InfoServiceInfo(){
			
	}
	
	public byte[] getInfo(){
		return info;
	}
	
	public Connection getRequestingConnection(){
		return requestingconnection;
	}
	
	public abstract void handleInfo(Connection c);
	
	public static InfoServiceInfo infoFactory(byte [] info){
		int infotype = Util.stuffBytesIntoUInt(Util.getFirstBytes(info, 2));
		
		switch(infotype){
		case INFO_PASSIVEPARTICIPANTLIST:
			return new InfoServiceInfoPassiveParticipantList(info);
		case INFO_ACTIVEPARTICIPANTLIST:
			return new InfoServiceInfoActiveParticipantList(info);
		case INFO_UPDATEACTIVEJOINING:
			return new InfoServiceUpdateActiveJoining(info);
		case INFO_UPDATEACTIVELEAVING:
			return new InfoServiceUpdateActiveLeaving(info);
		case INFO_COMMITKEYEXCHANGE:
			return new InfoServiceInfoKeyExchangeCommit(info);
		case INFO_EARLYQUITNOTIFICATION:
			return new InfoServiceInfoEarlyQuitServiceNotification(info);
		default:
			Logger.getLogger(InfoServiceInfo.class).warn("No such infotype defined: " + String.valueOf(infotype));
			return null;
		}
		
	}
	
	public void setRequestingConnection(Connection c){
		requestingconnection = c;
	}

	
	
}
