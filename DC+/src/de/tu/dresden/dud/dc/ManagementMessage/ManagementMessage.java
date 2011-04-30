/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.ManagementMessage;

import java.util.ArrayList;

import de.tu.dresden.dud.dc.Util;

/**
 * This class is mother of all ManagementMessages.
 * Assembley, handling, definition. 
 * @author klobs
 *
 */
public class ManagementMessage{ 
	
	// TODO Manches auslagern in Utility
								 
	// How went the processing of a message?
	protected boolean errorProcessing = false;
	
	/**
	 *  All available management messages are listed here
	 *  
	 *  The type indicates the length (I know this is bad, 
	 *  but as far as I know Java does not allow nice handling
	 *  of bit-fields)
	 */
	
	public static final int WELCOME2SERVICE   	= 0; 
	public static final int REGISTERATSERVICE		  	= 1; 
	public static final int ACCEPTED4SERVICE    = 2; 
	public static final int INFOREQ   			= 3; 
	public static final int INFO   				= 4;
	public static final int JOINWORKCYCLE			= 5;
	public static final int WELCOME2WORKCYCLE   	= 6; 
	public static final int ADD   				= 7; 
	public static final int ADDED   			= 8; 
	public static final int ADDRESERVATION		= 9;
	public static final int ADDEDRESERVATION	= 10;
	public static final int TICK   				= 11; 
	public static final int LEAVEWORKCYCLE   		= 12; 
	public static final int QUITSERVICE			= 13;
	public static final int KTHXBYE				= 14;

	// The spoken / implemented version
	public static final char  VERSION			= 1;
	
	// variable for the assembled payload
	protected byte[] message = null;
	
	protected long arrivalTime = 0;
	
	/**
	 * This intern method prepares a bytefield which can be send over the wire.
	 * Necessary length is calculated here.
	 * 
	 * ! <b>WARGING</b> !
	 * This method uses a dirty hack to insert the total length at the right position. You should know when you use this method.
	 * 
	 * @param b ArrayList of all the bytefields that shall be crafted to a message
	 * @return 
	 */
	protected static byte[] craftMessage(ArrayList<byte[]> b ){
		
		int  totallength = -2;			// Don't forget the 2 bytes for message 
										// which does not count to the payload 
		byte r[] 		 = new byte[0];

		
		for( byte[] d : b){
			totallength = totallength + d.length;	
		}
		
		
		for (int i = 0; i < b.size(); i++){
			r = Util.concatenate(r,b.get(i));
			
			// OK, lets do a dirty hack here!!!
			if(i == 0){ 
				// The message field is already in r. 
				// now let's add the length
				r = Util.concatenate(r, Util.stuffIntIntoShort(totallength));
			}
		}
		
		return r;
	}
	
	/**
	 * Provides verbose information, whether there were problems while the processing of the 
	 * message.
	 * @return true, when an error occurred
	 */
	public boolean errorProcessing(){
		return this.errorProcessing;
	}
	
	public long getArrivalTime(){
		return arrivalTime;
	}
	
	/**
	 * Getter for the message as byte array.
	 * This has to be set in the sub-classes.
	 * @return
	 */
	public byte[] getMessage() {
		return message;
	}

	
	/**
	 * Transforms an payload to a ManagementMessage object, or its subclasses.
	 * (-> will be looking at the message type and then passing the message to the right payload)
	 * @param messageType
	 * @param payload 
	 * @return The corresponding subclassed object for the payload.
	 */
	public static ManagementMessage parseMessage(int messageType, byte[] payload) throws IllegalArgumentException{
		
		ManagementMessage m;
		
		if (messageType == ManagementMessage.WELCOME2SERVICE){
		
			m = new  ManagementMessageWelcome2Service(payload);
			
			if ( m.errorProcessing() ) throw new IllegalArgumentException("The Payload you provided could not be evaluated as WELCOME2SERVICE Message");
			return m;
			
		} else if (messageType == ManagementMessage.REGISTERATSERVICE){
			
			m =  new ManagementMessageRegisterAtService(payload);
			
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as REGISTER AT SERVICE Message");
			return m;
			
		} else if (messageType == ManagementMessage.ACCEPTED4SERVICE){
			m = new ManagementMessageAccepted4Service(payload);
		
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as ACCEPTED4SERVICE Message");
			return m;
			
		} else if (messageType == ManagementMessage.JOINWORKCYCLE){
			m = new ManagementMessageJoinWorkCycle(payload);
			
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as JOINWORKCYCLE Message");
			return m;
		} else if (messageType == ManagementMessage.TICK){
			m = new ManagementMessageTick(payload);
		
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as TICK Message");
			return m;
		} else if (messageType == ManagementMessage.WELCOME2WORKCYCLE){
			m = new ManagementMessageWelcome2WorkCycle(payload);
			
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as WELCOME2WORKCYCLE Message");
			return m;
		} else if (messageType == ManagementMessage.ADD){
			m = new ManagementMessageAdd(payload);

			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as ADD Message");
			return m;
		} else if (messageType == ManagementMessage.ADDED){
			m = new ManagementMessageAdded(payload);
			
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as ADDED Message");
			return m;
		} else if (messageType == ManagementMessage.INFOREQ){
			m = new ManagementMessageInfoRequest(payload);
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as INFOREQ Message");
			return m;
		} else if (messageType == ManagementMessage.INFO){
			m = new ManagementMessageInfo(payload);
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as INFO Message");
			return m;
		} else if (messageType == ManagementMessage.LEAVEWORKCYCLE){
			m = new ManagementMessageLeaveWorkCycle(payload);
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as LEAVEWORKCYCLE Message");
			return m;
		} else if (messageType == ManagementMessage.QUITSERVICE){
			m = new ManagementMessageQuitService(payload);
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as QUITSERVICE Message");
			return m;
		} else if (messageType == ManagementMessage.KTHXBYE){
			m = new ManagementMessageKThxBye(payload);
			if( m.errorProcessing()) throw new IllegalArgumentException("The Payload you provided could not be evaluated as QUIT SERVICE CONFIRMATION Message");
			return m;
		} else {
			throw new IllegalArgumentException("The Payload you provided could not be evaluated as any Message (perhaps messagetype " + String.valueOf(messageType) + " is not implemented, or invalid)");
		}
	}

	public void setArrivalTime(long t){
		arrivalTime = t;
	}
	
}
