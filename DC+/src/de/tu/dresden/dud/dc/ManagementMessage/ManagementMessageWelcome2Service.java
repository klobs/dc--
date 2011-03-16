/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Server;
import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 *
 */
public class ManagementMessageWelcome2Service extends ManagementMessage{
	
	private int version;
	private int charLength;
	private int participantsCount;
	private int accept;
	private int method;
	private int urlLength;
	private String url;
	
	/**
	 * 	Generates the WELCOME2SERVICE management message. 
	 *  Introduced in protocol Version 0.0.7
	 *  
	 *  Fields are:
	 *      TYPE							2 Byte
	 *  	TOTAL LENGTH					2 Byte
	 *  	VERSION							1 Byte
	 *  	CHARACTER LENGTH	(in Bytes)	2 Byte
	 *  	PARTICIPANTS COUNT*				2 Byte
	 *  	ACCEPT / REJECT					1 Byte
	 *		METHOD							1 Byte 
	 *  	URL LENGTH						2 Byte
	 *  	URL								variable (not yet used)
	 *  
	 *   * registered for the service, but not necessarily  taking part in work cycles
	 *   
	 * @param s the server for which the WELCOME2SERVICE is to be generated
	 */
	public ManagementMessageWelcome2Service(Server s, int method){
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype	= Util.stuffIntIntoShort(ManagementMessage.WELCOME2SERVICE);
		byte[] version 		= Util.stuffIntIntoByte(VERSION);
		byte[] charLength 	= Util.stuffIntIntoShort(s.getSymbolLength()); 
		byte[] partCount	= Util.stuffIntIntoShort(s.getInfoService().getParticipantCount());
		byte[] acRej		= Util.stuffIntIntoByte(s.getInfoService().doAccept());
		byte[] meth			= Util.stuffIntIntoByte(method);
		byte[] urlLength	= Util.stuffIntIntoShort(s.getInfoService().getDirURL().length());
		byte[] url			= Util.stuffStringIntoCharArray(s.getInfoService().getDirURL());
		
		b.add(messagetype	);
		b.add(version		);
		b.add(charLength	);
		b.add(partCount		);
		b.add(acRej			);
		b.add(meth			);
		b.add(urlLength		);
		b.add(url			);
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Encoding WELCOME2SERVICE MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Server protocol version: " + String.valueOf(VERSION) + " / " , version, this);
			Log.print(Log.LOG_DEBUG, "	Server charlength: " + String.valueOf(s.getSymbolLength()) +  " / ", charLength, this);
			Log.print(Log.LOG_DEBUG, "	Current users:" + String.valueOf(s.getInfoService().getParticipantCount()) + " / ", partCount, this);
			Log.print(Log.LOG_DEBUG, "	Server is accepting new participants: " + String.valueOf(s.getInfoService().doAccept()) + " / ", acRej, this);
			Log.print(Log.LOG_DEBUG, "	Service is using DC = 0, DC+ =1: " + String.valueOf(method), this);
			Log.print(Log.LOG_DEBUG, "	Server's direcotory is located at (not yet implemented, though): " + s.getInfoService().getDirURL() + " / ", url, this);
		}

		
		message = craftMessage(b);
	}

	
	/**
	 * 	Generates the WELCOME2SERVICE management message. 
	 *  Introduced in protocol Version 0.0.7
	 *  
	 *  Fields are:
	 *      TYPE							2 Byte
	 *  	TOTAL LENGTH					2 Byte
	 *  	VERSION							1 Byte
	 *  	CHARACTER LENGTH	(in Bytes)	2 Byte
	 *  	PARTICIPANTS COUNT*				2 Byte
	 *  	ACCEPT / REJECT					1 Byte
	 *		METHOD							1 Byte 
	 *  	URL LENGTH						2 Byte
	 *  	URL								variable (not yet used)
	 *  
	 *   * registered for the service, but not necessarily  taking part in work cycles
	 *   
	 * @param s the server for which the WELCOME2SERVICE is to be generated
	 */
	 public ManagementMessageWelcome2Service(byte[] payload){
		
		if(payload.length < 9){
				Log.print(Log.LOG_WARN, "Payload length < minimal expected payload Length! Dropping packet!", this);
				errorProcessing = true;
		}
		
		version 			= payload[0];
		charLength 			= Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 1, 2));
		participantsCount	= Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 3, 2));
		accept				= payload[5];
		method				= payload[6];
		urlLength			= Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 7, 2));

		if(! (payload.length - 9 == urlLength))
			errorProcessing = true;
		url					= new String(payload, 9, urlLength);

		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Decoding WELCOME2SERVICE MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Server requires " + String.valueOf(version) + " as protocol", this);
			Log.print(Log.LOG_DEBUG, "	Server requires " + String.valueOf(charLength) + " as charlength", this);
			Log.print(Log.LOG_DEBUG, "	There are currently " + String.valueOf(participantsCount) + " participants (not necissarily taking part in work cycles)", this);
			Log.print(Log.LOG_DEBUG, "	Server is accepting new participants: " + String.valueOf(accept), this);
			Log.print(Log.LOG_DEBUG, "	Server's direcotory is located at: " + url + " (not yet implemented, though)", this);
		}
		 
	 }

	
	public int getVersion() {
		return version;
	}


	public int getCharLength() {
		return charLength;
	}


	public int getParticipantsCount() {
		return participantsCount;
	}


	public int getAccept() {
		return accept;
	}

	public int getMethod(){
		return method;
	}

	public int getUrlLength() {
		return urlLength;
	}


	public String getUrl() {
		return url;
	}
}
