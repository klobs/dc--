/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Participant;
import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 * 
 */
public class InfoServiceInfoEarlyQuitServiceNotification extends InfoServiceInfo {

	// Logging
	private static Logger log = Logger.getLogger(InfoServiceInfoEarlyQuitServiceNotification.class);

	private LinkedList<Participant> earlyLeavingParticipant = new LinkedList<Participant>();
	private long workCycle = Long.MIN_VALUE;
	private int roundNumber = Short.MIN_VALUE;
	
	public InfoServiceInfoEarlyQuitServiceNotification(LinkedList<Participant> apl, long workCycleNumber, int roundNumber) {
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		Iterator<Participant> ip = apl.iterator();
		Participant p = null;

		earlyLeavingParticipant = apl;
		
		this.workCycle   = workCycleNumber;
		this.roundNumber = roundNumber;
		
		byte[] wcn = Util.stuffLongIntoLong(workCycleNumber);
		byte[] rn  = Util.stuffIntIntoShort(roundNumber);
		byte[] ac  = Util.stuffIntIntoShort(apl.size());

		b.add(Util
				.stuffIntIntoShort(InfoServiceInfo.INFO_EARLYQUITNOTIFICATION));
		b.add(wcn);
		b.add(rn);
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
	
	public InfoServiceInfoEarlyQuitServiceNotification(byte [] infopayload) {
		info = infopayload;
		
		int pc = 0;
		int ul = 14;

		ArrayList<byte[]> p = new ArrayList<byte[]>(5);
		
		if (infopayload.length < 12) {
			log.warn("Payload does not correspond to the required min size");
		}
		
		workCycle 	= Util.stuffBytesIntoLong(Util.getBytesByOffset(infopayload, 2, 8));
		roundNumber = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, 10, 2));
		
		pc = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, 12, 2));

		for (int i = 0; i < pc; i++) {
			for (int j = 0; j < 5; j++) {
				if (infopayload.length >= ul + 2) {
					int pl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, ul, 2));
					ul = ul + 2;
					if (infopayload.length >= ul + pl) {
						p.add(j,Util.getBytesByOffset(infopayload, ul, pl));
						ul = ul + pl;
					} else {
						log.warn("Payload has strange differences between indicated and effective length: wrong information about id lengths");
					}
				} else {
					log.warn("Payload has strange differences between indicated and effective length: not enough data for the indicated number of users");
				}
			}
			earlyLeavingParticipant.add(new Participant(new String(p.get(0)),
					new String(p.get(1)), p.get(2), p.get(3), p.get(4)));
			p.clear();
		}
	}
	
	public static InfoServiceInfoEarlyQuitServiceNotification infoServiceInfoEarlyQuitServiceNotificationFor(Participant p, long workCycleNumber, int roundNumber){
		LinkedList<Participant> a = new LinkedList<Participant>();
		a.add(p);
		return new InfoServiceInfoEarlyQuitServiceNotification(a, workCycleNumber, roundNumber);
	}
	
	public LinkedList<Participant> getEarlyLeavingParticipantIDs(){
		return earlyLeavingParticipant;
	}
	
	public long getMissingInRoundNumber(){
		return roundNumber;
	}
	
	public long getMissingInWorkCycle(){
		return workCycle;
	}
	
	public void handleInfo(Connection c){
		requestingconnection = c;
		
		for(Participant p : earlyLeavingParticipant){
			c.getAssociatedParticipantManager().removeParticipant(p);
		}
	}
}
