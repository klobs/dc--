/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.ArrayList;

/**
 * The {@link Server} shall send an {@link InfoServiceInfoKeyExchangeCommit} to two
 * {@link Participant}s, that undertook all necessary steps to exchange keys. The order
 * of the participants in this message is unimportant.
 * 
 * @author klobs
 * 
 */
public class InfoServiceInfoKeyExchangeCommit extends InfoServiceInfo {

	public String p1 = null;
	public String p2 = null;
	public long   workcycle = -1;
	
	/**
	 * Use this constructor, if you received the
	 * {@link InfoServiceInfoKeyExchangeCommit} over the wire.
	 * 
	 * @param infopayload
	 *            the payload that you received and of which the
	 *            InfoServiceInfoKeyExchangeCommit is created
	 */
	public InfoServiceInfoKeyExchangeCommit(byte[] infopayload) {
	
		int ul = 10;
		int pl = 0; 
		
		workcycle = Util.stuffBytesIntoLong(Util.getBytesByOffset(infopayload, 2, 8));
		
		pl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, ul, 2));
		ul = ul + 2;
		if (! (infopayload.length >= ul + pl + 2)){
			Log.print(Log.LOG_ERROR, "Wrong false length for info packet", this);
			return;
		}
		
		p1 = new String(Util.getBytesByOffset(infopayload, ul, pl));
		ul = ul + pl;
		
		pl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, ul, 2));
		ul = ul + 2;
		if (! (infopayload.length >= ul + pl)){
			Log.print(Log.LOG_ERROR, "Wrong false length for info packet", this);
			return;
		}
		
		p2 = new String(Util.getBytesByOffset(infopayload, ul, pl));
		
	}
	
	
	/**
	 * Use this constructor if you want to send an {@link InfoServiceInfoKeyExchangeCommit} over the wire.
	 * @param workcycle indicates the {@link WorkCycle}number when the {@link Participant}s shall start using the exchanged keys.
	 * @param p1 one participant of the key exchange
	 * @param p2 the other participant of the key exchange.
	 */
	public InfoServiceInfoKeyExchangeCommit(long workcycle, String p1, String p2){
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		b.add(Util.stuffIntIntoShort(INFO_COMMITKEYEXCHANGE));
		b.add(Util.stuffLongIntoLong(workcycle));
		b.add(Util.stuffIntIntoShort(p1.length()));
		b.add(p1.getBytes());
		b.add(Util.stuffIntIntoShort(p2.length()));
		b.add(p2.getBytes());
		
		this.p1 = p1;
		this.p2 = p2;
		
		for(int i = 0; i < b.size(); i++) {
			info = Util.concatenate(info, b.get(i));
		}
	}
	
	/**
	 * Standard getter
	 * @return returns the first participant of a key exchange.
	 */
	public String getP1(){
		return p1;
	}
	
	/**
	 * Standard getter
	 * @return returns the second participant of a key exchange.
	 */
	public String getP2(){
		return p2;
	}
	
	public long getWorkCycle(){
		return workcycle;
	}
	
	@Override
	public void handleInfo(Connection c) {
		c.commitKeyExchange(this);
	}

}
