/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoKeyExchangeCommit;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoRequestKeyExchange;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfo;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfoRequest;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

/**
 * @author klobs
 * This class will be used for key exchange, later
 */
public class KeyExchangeManager {

	// Logging
	private static Logger log = Logger.getLogger(KeyExchangeManager.class);

	// Key exchange methods
	
	/**
	 * Exchange Keys all manual
	 */
	public static final short KEX_MANUAL = 0;
	
	/**
	 * All all active participants will receive exchange commit notifications 
	 */
	public static final short KEX_SEMI_AUTOMATIC = 1;
	
	/**
	 * All active participants are expected to generate keys with for all other 
	 * active participants, even without receiving exchange commit notifications.
	 */
	public static final short KEX_FULLY_AUTOMATIC = 2;
	
	private HashSet<String> exchanges = new HashSet<String>();
	private short handling = -1;
	
	public KeyExchangeManager(short kex_handling){
		handling = kex_handling;
	}
	
	public short getKexHandling(){
		return handling;
	}
	
	public void handleExchange(InfoServiceInfoRequestKeyExchange info, ParticipantManager pm, WorkCycleManager rm){
		String fromTo = new String(info.getFrom() + info.getTo());
		String toFrom = new String(info.getTo() + info.getFrom());
		
		if (exchanges.contains(fromTo)) {

			// ok to commit
			// Prepare commit message and let the WorkCycleManager / Work Cycle
			// broadcast it.
			ParticipantMgmntInfo pmi1 = pm
					.getParticipantMgmntInfoByParticipantID(info.getFrom());
			ParticipantMgmntInfo pmi2 = pm
					.getParticipantMgmntInfoByParticipantID(info.getTo());

			if (pmi1 == null || pmi2 == null) {
				log.warn("Did not find one of participants. Sorry. Can not acomplish Key Exchange Request");
				return;
			}

			WorkCycle nextWorkCycle = rm.getNextWorkCycle();

			InfoServiceInfoKeyExchangeCommit kec = new InfoServiceInfoKeyExchangeCommit(
					nextWorkCycle.getWorkCycleNumber(), info.getFrom(), info.getTo());

			// If work cycles are already running, let the work cycle manager do the work,
			// if not, send the broadcast here...
			if (rm.isRunning()) {
				nextWorkCycle.addKeyExchangeCommitMessages(kec);
				// Forget about the commit, as the work cycle will broadcast it...
				exchanges.remove(fromTo);
			} else {
				ManagementMessageInfo m = new ManagementMessageInfo(kec);

				try {
					pmi1.getAssocConnection().sendMessage(m.getMessage());
					pmi2.getAssocConnection().sendMessage(m.getMessage());
					exchanges.remove(fromTo);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		} else {
			ParticipantMgmntInfo pmi = pm.getParticipantMgmntInfoByParticipantID(info.getTo());
			
			if(pmi == null) return;
			
			ManagementMessageInfoRequest m = new ManagementMessageInfoRequest(info);

			try {
				pmi.getAssocConnection().sendMessage(m.getMessage());
				exchanges.add(toFrom);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
