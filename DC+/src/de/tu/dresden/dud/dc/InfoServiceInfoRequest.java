/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;


public abstract class InfoServiceInfoRequest {

	// here we have a list of available information requests.
	public static final int IRQ_PASSIVEPARTICIPANTLIST 	= 0;
	public static final int IRQ_ACTIVEPARTICIPANTLIST 	= 1;
	public static final int IRQ_KEYEXCHANGE				= 3;
	
	// Inforequests that are handles by the server need an associated server to answer.
	protected byte[] 		infoservicerequest 		= new byte[2];
	protected Connection 	requestingconnection 	= null;
	
	InfoServiceInfoRequest(){
			
	}
	
	public byte[] getInfoServicerequest(){
		return infoservicerequest;
	}
	
	public Connection getRequestingConnection(){
		return requestingconnection;
	}
	
	public abstract void handleRequest(Connection C);
	
	public static InfoServiceInfoRequest infoRequestFactory(byte [] i){
		int infotype = Util.stuffBytesIntoUInt(Util.getFirstBytes(i, 2));
		
		switch(infotype){
		case IRQ_PASSIVEPARTICIPANTLIST:
			return new InfoServiceInfoReqPassiveParticipantList();
		case IRQ_ACTIVEPARTICIPANTLIST:
			return new InfoServiceInfoReqActiveParticipantList();
		case IRQ_KEYEXCHANGE:
			return new InfoServiceInfoRequestKeyExchange(i);
		default:
			return null;
		}
	}
	
	public void setRequestingConnection(Connection c){
		requestingconnection = c;
	}
	
}
