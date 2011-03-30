/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Participant;
import de.tu.dresden.dud.dc.ParticipantManager;
import de.tu.dresden.dud.dc.Util;

public class InfoServiceUpdateActiveLeaving extends InfoServiceInfo {

	// Logging
	private static Logger log = Logger.getLogger(InfoServiceUpdateActiveLeaving.class);

private LinkedList<Participant> activeLeavingParticipants = new LinkedList<Participant>();
	private LinkedList<Long> workCycleNumbers = new LinkedList<Long>();	
	
	public InfoServiceUpdateActiveLeaving(
			LinkedHashSet<Connection> leavingConnections) {
		ArrayList<byte[]> b = new ArrayList<byte[]>();
		Iterator<Connection> ip = leavingConnections.iterator();
		Connection c = null;
		Participant p = null;

		byte[] ac = Util.stuffIntIntoShort(leavingConnections.size());
		b.add(Util.stuffIntIntoShort(InfoServiceInfo.INFO_UPDATEACTIVELEAVING));
		b.add(ac);

		while (ip.hasNext()) {
			c = ip.next();
			p = c.assocParticipant;
			b.add(Util.stuffLongIntoLong(c.getExpectedLeavingWorkCycle()));
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

		for (int i = 0; i < b.size(); i++) {
			info = Util.concatenate(info, b.get(i));
		}
	}

	public InfoServiceUpdateActiveLeaving(byte[] infopayload) {
		info = infopayload;

		int pc = 0;
		int ul = 4;
		long rn = 0;

		ArrayList<byte[]> p = new ArrayList<byte[]>(5);

		if (infopayload.length < 10) {
			log.warn("Payload does not correspond to the required min size");
		}

		info = infopayload;

		pc = Util.stuffBytesIntoUInt(Util.getBytesByOffset(infopayload, 2, 2));

		for (int i = 0; i < pc; i++) {
			rn = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(
					infopayload, ul, 8));
			workCycleNumbers.add(new Long(rn));
			ul = ul + 8;
			for (int j = 0; j < 5; j++) {
				if (infopayload.length >= ul + 2) {
					int pl = Util.stuffBytesIntoUInt(Util.getBytesByOffset(
							infopayload, ul, 2));
					ul = ul + 2;
					if (infopayload.length >= ul + pl) {
						p.add(j, Util.getBytesByOffset(infopayload, ul, pl));
						ul = ul + pl;
					} else {
						log.warn("Payload has strange differences between indicated and effective length: wrong information about id lengths");
					}
				} else {
					log.warn("Payload has strange differences between indicated and effective length: not enough data for the indicated number of users");
				}
			}
			activeLeavingParticipants.add(new Participant(new String(p.get(0)),
					new String(p.get(1)), p.get(2), p.get(3), p.get(4)));
			p.clear();
		}
	}

	public LinkedList<Participant> getJoiningActiveParticipantIDs() {
		return activeLeavingParticipants;
	}

	public long getWorkCycleNumberForParticipant(Participant p) {
		int i = activeLeavingParticipants.indexOf(p);

		return workCycleNumbers.get(i).longValue();
	}

	@Override
	public void handleInfo(Connection c) {
		ParticipantManager pm = c.getAssociatedParticipantManager();
		for (Participant p : activeLeavingParticipants) {
			pm.unsetParticipantActiveAfterWorkCycle(p,
					getWorkCycleNumberForParticipant(p));
			
			if(c.equals(pm.getParticipantMgmntInfoFor(pm.getMe()).getAssocConnection())){
				c.setMode(Connection.MODE_PASSIVE);
			}
		}
	}

}
