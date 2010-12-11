/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.ArrayList;

public class ManagementMessageRegisterAtService extends ManagementMessage {

	private byte[] dhPublicPart;
	private byte[] dhPublicSig;
	private String participantID;
	private String username;
	private byte[] signature;

	/**
	 * 	Handles the REGISTER AT SERVICE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.10
	 * 
	 *  Fields are:
	 *  	PARTICIPANT ID LENGTH			1 Byte
	 *  	PARTICIPANT ID					variable (up to 255 bytes)
	 *  	USERNAME LENGTH					1 Byte
	 *  	USERNAME						variable (up to 255 bytes)
	 *  	SIGNATURE LENGTH				2 Byte
	 *  	SIGNATURE						variable (up to 65536 bytes)
	 *  	DH LENGTH						2 Byte
	 *  	DH								variable (up to 65536 bytes)
	 *  	DH_SIGNATURE LENGTH				2 Byte
	 *  	DH_SIGNATURE					variable (up to 65536 bytes)
	 *   
	 * @param id user id
	 * @param u  user name
	 * @param s  signature
	 * @param dh diffie hellman public part
	 * @param dhs signature for diffie hellman public part.
	 */	
	public ManagementMessageRegisterAtService(String id, String u, byte[] s, byte[] dh, byte[] dhs){

		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		byte[] messagetype		= Util.stuffIntIntoShort(ManagementMessage.REGISTERATSERVICE);
		byte[] partIDLength 	= Util.stuffIntIntoByte(id.length());
		byte[] participantID	= Util.stuffStringIntoCharArray(id);
		byte[] usernameLength 	= Util.stuffIntIntoByte(u.length());
		byte[] username			= Util.stuffStringIntoCharArray(u);
		byte[] signatureLength	= Util.stuffIntIntoShort(s.length);
		byte[] signature		= s;
		byte[] dhLength			= Util.stuffIntIntoShort(dh.length);
		byte[] diffieHellman	= dh;
		byte[] dhsLength		= Util.stuffIntIntoShort(dhs.length);
		byte[] diffieHellmanSig	= dhs;
		
		
		b.add(messagetype 	);
		b.add(partIDLength	);
		b.add(participantID	);
		b.add(usernameLength);
		b.add(username		);
		b.add(signatureLength);
		b.add(signature		);
		b.add(dhLength      );
		b.add(diffieHellman );
		b.add(dhsLength     );
		b.add(diffieHellmanSig);
	
		this.message = craftMessage(b);
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Encoding REGISTER AT SERVICE MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Participant id " + id  + " / " , participantID, this);
			Log.print(Log.LOG_DEBUG, "	Username: " + u + " / ", username, this);
			Log.print(Log.LOG_DEBUG, "	Signature: " + new String(s) + " / ", signature, this);
			Log.print(Log.LOG_DEBUG, "	DH: ", dh, this);
			Log.print(Log.LOG_DEBUG, "	DH Sig: ", dhs, this);
		}
	
	}
	
	
	/**
	 *  Handles the REGISTER AT SERVICE management message.
	 *  Handling means interpreting the payload and setting corresponding 
	 *  internal flags. 
	 *  Introduced in protocol Version 0.0.10
	 *  
	 *  Fields are:
	 *		PARTICIPANT ID LENGTH			1 Byte
	 *  	PARTICIPANT ID					variable (up to 255 bytes)
	 *  	USERNAME LENGTH					1 Byte
	 *  	USERNAME						variable (up to 255 bytes)
	 *  	SIGNATURE LENGTH				2 Byte
	 *  	SIGNATURE						variable (up to 65536 bytes)
	 *  	DH LENGTH						2 Byte
	 *  	DH								variable (up to 65536 bytes)
	 *  	DH_SIGNATURE LENGTH				2 Byte
	 *  	DH_SIGNATURE					variable (up to 65536 bytes)
	 *   
	 * @param payload the payload
	 */	
	public ManagementMessageRegisterAtService(byte[] payload){
		
		int participantIDLength = 0;
		int usernameLength 		= 0;
		int signatureLength		= 0;
		int dhLength			= 0;
		int dhSigLength			= 0;
		
		message = payload;
		
		if(payload.length < 8){
				Log.print(Log.LOG_WARN, "Payload length < minimal expected payload Length! Dropping packet!", this);
				errorProcessing = true;
		}
		
		participantIDLength = payload[0];
		
		if(! (payload.length > 1 + participantIDLength)){
			errorProcessing = true;
			throw new IllegalArgumentException("Indicated length fields do not match effective lengths");
		}
		participantID = new String(payload, 1, participantIDLength);
		
		usernameLength = payload[1 + participantIDLength];

		if(! (payload.length > 2 + participantIDLength + usernameLength)){
			errorProcessing = true;
			throw new IllegalArgumentException("Indicated length fields do not match effective lengths");
		}
		username = new String(payload, 2 + participantIDLength , usernameLength);
		
		signatureLength = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 2 + participantIDLength + usernameLength, 2));
		
		if(! (payload.length >= 4 + participantIDLength + usernameLength + signatureLength)){
			errorProcessing = true;
			throw new IllegalArgumentException("Indicated length fields do not match effective lengths");
		}
		signature = Util.getBytesByOffset(payload, 4 + participantIDLength + usernameLength, signatureLength);
		
		dhLength = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 4 + participantIDLength + usernameLength + signatureLength, 2));
		
		if(! (payload.length >= 6 + participantIDLength + usernameLength + signatureLength + dhLength)){
			errorProcessing = true;
			throw new IllegalArgumentException("Indicated length fields do not match effective lengths");
		}
		dhPublicPart = Util.getBytesByOffset(payload, 6 + participantIDLength + usernameLength + signatureLength, dhLength);
		
		dhSigLength = Util.stuffBytesIntoUInt(Util.getBytesByOffset(payload, 6 + participantIDLength + usernameLength + signatureLength + dhLength, 2));
		
		if(! (payload.length == 8 + participantIDLength + usernameLength + signatureLength + dhLength + dhSigLength)){
			errorProcessing = true;
			throw new IllegalArgumentException("Indicated length fields do not match effective lengths");
		}
		dhPublicSig = Util.getBytesByOffset(payload, 8 + participantIDLength + usernameLength + signatureLength + dhLength, dhSigLength);
		
		if(Log.getInstance().getLogLevel() >= Log.LOG_DEBUG){
			Log.print(Log.LOG_DEBUG, "Decoding REGISTER AT SERVICE MESSAGE", this);
			Log.print(Log.LOG_DEBUG, "	Participant id: " + participantID, this);
			Log.print(Log.LOG_DEBUG, "	Username: " + username, this);
			Log.print(Log.LOG_DEBUG, "	Signature: ", signature, this);
			Log.print(Log.LOG_DEBUG, "	DH Public part: ", dhPublicPart, this);
			Log.print(Log.LOG_DEBUG, "	DH Signature: ", dhPublicSig, this);
		}
		
	}
	
	public byte[] getDHPublicPart(){
		return dhPublicPart;
	}
	
	public byte[] getDHPublicSig(){
		return dhPublicSig;
	}
	
	public String getParticipantID() {
		return participantID;
	}


	public String getUsername() {
		return username;
	}


	public byte[] getSignature() {
		return signature;
	}
	
}
