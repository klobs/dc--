/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import java.util.ArrayList;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Util;

public class InfoServiceInfoRequestKeyExchange extends InfoServiceInfoRequest {

	private String from = null;
	private String to   = null;
	
	public InfoServiceInfoRequestKeyExchange(String from, String to) {
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		
		infoservicerequest = Util.stuffIntIntoShort(InfoServiceInfoRequest.IRQ_KEYEXCHANGE);
		
		b.add(Util.stuffIntIntoShort(from.length()));
		b.add(from.getBytes());
		b.add(Util.stuffIntIntoShort(to.length()));
		b.add(to.getBytes());
		
		for(int i = 0; i < b.size(); i++) {
			infoservicerequest = Util.concatenate(infoservicerequest, b.get(i));
		}
	}
	
	public InfoServiceInfoRequestKeyExchange(byte [] infopayload) {
		infoservicerequest = infopayload;
		
		int fl = 0;
		int tl = 0;
		int p  = 2;
		
		if (infopayload.length < 6) {
			Log.print(Log.LOG_WARN,
					"Payload does not correspond to the required min size",
					this);
		}
	
		fl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, p, 2));
		
		if (infopayload.length < p + fl){
			Log
			.print(
					Log.LOG_WARN,
					"Payload has strange differences between indicated and effective length: wrong information about id lengths",
					this);
		}
		
		p = p + 2;
		
		from = new String(Util.getBytesByOffset(infopayload, p, fl));
		
		p = p + fl;
		
		tl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, p, 2));

		if (infopayload.length < p + tl){
			Log
			.print(
					Log.LOG_WARN,
					"Payload has strange differences between indicated and effective length: wrong information about id lengths",
					this);
		}
		
		p = p + 2;
		
		to = new String(Util.getBytesByOffset(infopayload, p, fl));
		
	}

	
	public String getFrom(){
		return from;
	}
	
	public String getTo(){
		return to;
	}
	
	@Override
	public void handleRequest(Connection c) {
		
		// as a server, relay the message
		if (c.isServerMode()){
			c.getAssociatedServer().getKeyExchangeManager().handleExchange(this, c.getAssociatedParticipantManager(), c.getAssociatedWorkCycleManager());
		}
		// as a client, act correspondingly
		else {
			c.requestKeyExchange(getFrom());
		}
	}

}
