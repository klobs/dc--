/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Server;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.KeyGenerators.KeyGenerator;

/**
 * @author klobs
 *
 */
public class ManagementMessageWelcome2Service extends ManagementMessage{

    // Logging
    private static Logger log = Logger.getLogger(ManagementMessageWelcome2Service.class);

    // Features
    public static final short FEATURE_KEY_GENERATION 			= 0;
    public static final short FEATURE_LOST_CONNECTION_HANDLING 	= 1;
    public static final short FEATURE_KEY_EXCHANGE				= 2;
    public static final short FEATURE_VARIABLE_PAYLOAD_LENGTH	= 3;
    
    
    private HashMap<Short, Short> featureMap = new HashMap<Short, Short>(10);
    
	private int version;
	private int charLength;
	private int participantsCount;
	private int accept;
	
	/**
	 * 	Generates the WELCOME2SERVICE management message. 
	 *  Introduced in protocol Version 0.0.7
	 *  
	 *  Fields are:
	 *      TYPE							2 Byte
	 *  	TOTAL LENGTH					2 Byte
	 *  	VERSION							2 Byte
	 *  	CHARACTER LENGTH	(in Bytes)	2 Byte
	 *  	PARTICIPANTS COUNT*				2 Byte
	 *  	ACCEPT / REJECT					2 Byte
	 *  
	 *   * registered for the service, but not necessarily  taking part in work cycles
	 *   
	 * @param s the server for which the WELCOME2SERVICE is to be generated
	 */
	public ManagementMessageWelcome2Service(Server s){
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		setupFeatureList(s);
		
		byte[] messagetype	= Util.stuffIntIntoShort(ManagementMessage.WELCOME2SERVICE);
		byte[] version 		= Util.stuffIntIntoShort(VERSION);
		byte[] charLength 	= Util.stuffIntIntoShort(s.getWorkCycleManager().getSymbolLength()); 
		byte[] partCount	= Util.stuffIntIntoShort(s.getInfoService().getParticipantCount());
		byte[] acRej		= Util.stuffIntIntoShort(s.getInfoService().doAccept());
		
		b.add(messagetype	);
		b.add(version		);
		b.add(charLength	);
		b.add(partCount		);
		b.add(acRej			);
		b.add(generateFeatureArray());
		
		if(log.isDebugEnabled()){
			log.debug("Encoding WELCOME2SERVICE MESSAGE");
			log.debug("	Server protocol version: " + String.valueOf(VERSION));
			log.trace(" / " + Arrays.toString(version));
			log.debug("	Server charlength: " + String.valueOf(s.getWorkCycleManager().getSymbolLength()));
			log.trace(" / " + Arrays.toString(charLength));
			log.debug("	Current users:" + String.valueOf(s.getInfoService().getParticipantCount()));
			log.trace(" / " + Arrays.toString(partCount));
			log.debug("	Server is accepting new participants: " + String.valueOf(s.getInfoService().doAccept()));
			log.trace(" / " + Arrays.toString(acRej));
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
	 *  	VERSION							2 Byte
	 *  	CHARACTER LENGTH	(in Bytes)	2 Byte
	 *  	PARTICIPANTS COUNT*				2 Byte
	 *  	ACCEPT / REJECT					2 Byte
	 *  	FEATURELIST						VARIABLE 
	 *  
	 *   * registered for the service, but not necessarily  taking part in work cycles
	 *   
	 * @param s the server for which the WELCOME2SERVICE is to be generated
	 */
	 public ManagementMessageWelcome2Service(byte[] payload){
		
		if(payload.length < 8){
				log.warn( "Payload length < minimal expected payload Length! Dropping packet!");
				errorProcessing = true;
		}
		
		version 			= Util.stuffBytesIntoUInt(Util.getFirstBytes(payload, 2));
		charLength 			= Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 2, 2));
		participantsCount	= Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 4, 2));
		accept				= Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 6, 2));

		setupFeatureList(Util.getBytesByOffset(payload, 8, payload.length - 8));

		if(log.isDebugEnabled()){
			log.debug("Decoding WELCOME2SERVICE MESSAGE");
			log.debug("	Server requires " + String.valueOf(version) + " as protocol");
			log.debug("	Server requires " + String.valueOf(charLength) + " as charlength");
			log.debug("	There are currently " + String.valueOf(participantsCount) + " participants (not necissarily taking part in work cycles)");
			log.debug("	Server is accepting new participants: " + String.valueOf(accept));
		}
	 }

	private byte[] generateFeatureArray(){
		byte[] fa = new byte[0];
		
		for(Short f: featureMap.keySet()){
			fa = Util.concatenate(fa, Util.stuffIntIntoShort(f.shortValue()));
			fa = Util.concatenate(fa, Util.stuffIntIntoShort(featureMap.get(f).shortValue()));
		}
		
		return fa;
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

	public short getMethod(){
		Short b = featureMap.get(Short.valueOf(FEATURE_KEY_GENERATION));
		if (b != null){
			return b.shortValue();
		}
		
		return KeyGenerator.KGMETHOD_DC;
	}
	
	private void setupFeatureList(byte[] fa){
		if (fa.length % 4 != 0){
			log.error("Feature array has wrong size");
			errorProcessing = true;
		}
		
		for (int i = 0; i< fa.length; i = i+4){
			featureMap.put(Short.valueOf((short) Util.stuffBytesIntoUInt(Util
					.getBytesByOffset(fa, i, 2))), Short.valueOf((short) Util
					.stuffBytesIntoUInt(Util.getBytesByOffset(fa, i + 2, 2))));
		}
	}
	
	private void setupFeatureList(Server s){
		featureMap.put(Short.valueOf(FEATURE_KEY_GENERATION), Short.valueOf(s.getWorkCycleManager().getKeyGenerationMethod()));

		featureMap.put(Short.valueOf(FEATURE_LOST_CONNECTION_HANDLING), Short
				.valueOf(Connection.HANDLING_EXPLODE));
		
		featureMap.put(Short.valueOf(FEATURE_KEY_EXCHANGE), Short.valueOf(s.getKeyExchangeManager().getKexHandling()));
	}
	
	
}
