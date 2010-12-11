/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * @author klobs
 * 
 */
public class InfoServiceInfoActiveParticipantList extends InfoServiceInfo {

	private LinkedList<Participant> activeParticipant = new LinkedList<Participant>();
	
	
	public InfoServiceInfoActiveParticipantList(ArrayList<Participant> apl) {
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		Iterator<Participant> ip = apl.iterator();
		Participant p = null;

		byte[] ac = Util.stuffIntIntoShort(apl.size());

		b.add(Util
				.stuffIntIntoShort(InfoServiceInfo.INFO_ACTIVEPARTICIPANTLIST));
		b.add(ac);

		while (ip.hasNext()) {
			p = ip.next();
			b.add(Util.stuffIntIntoShort(p.getId().length()));
			b.add(p.getId().getBytes());
			b.add(Util.stuffIntIntoShort(p.getUsername().length()));
			b.add(p.getUsername().getBytes());
			b.add(Util.stuffIntIntoShort(p.getDSAPublicSignature().length));
			b.add(p.getDSAPublicSignature());
			b.add(Util.stuffIntIntoShort(p.getDHPublicPart().length));
			b.add(p.getDHPublicPart());
			b.add(Util.stuffIntIntoShort(p.getDHPublicPartSignature().length));
			b.add(p.getDHPublicPartSignature());
		}

		for(int i = 0; i < b.size(); i++) {
			info = Util.concatenate(info, b.get(i));
		}
	}
	
	public InfoServiceInfoActiveParticipantList(byte [] infopayload) {
		info = infopayload;
		
		int pc = 0;
		int ul = 4;

		ArrayList<byte[]> p = new ArrayList<byte[]>(5);
		
		if (infopayload.length < 4) {
			Log.print(Log.LOG_WARN,
					"Payload does not correspond to the required min size",
					this);
		}
		
		pc = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, 2, 2));

		for (int i = 0; i < pc; i++) {
			for (int j = 0; j < 5; j++) {
				if (infopayload.length >= ul + 2) {
					int pl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, ul, 2));
					ul = ul + 2;
					if (infopayload.length >= ul + pl) {
						p.add(j,Util.getBytesByOffset(infopayload, ul, pl));
						ul = ul + pl;
					} else {
						Log
								.print(
										Log.LOG_WARN,
										"Payload has strange differences between indicated and effective length: wrong information about id lengths",
										this);
					}
				} else {
					Log
							.print(
									Log.LOG_WARN,
									"Payload has strange differences between indicated and effective length: not enough data for the indicated number of users",
									this);
				}
			}
			activeParticipant.add(new Participant(new String(p.get(0)),
					new String(p.get(1)), p.get(2), p.get(3), p.get(3)));
			p.clear();
		}
	}
	
	public LinkedList<Participant> getActiveParticipantIDs(){
		return activeParticipant;
	}
	
	public void handleInfo(Connection c){
		requestingconnection = c;
		c.getAssociatedParticipantManager().setParticipantsActive(activeParticipant);
		c.finishUnfinishedKeyExchReqs();
	}
}
