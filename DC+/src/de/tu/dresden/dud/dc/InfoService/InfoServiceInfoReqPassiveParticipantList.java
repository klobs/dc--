/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfo;

/**
 * @author klobs
 *
 */
public class InfoServiceInfoReqPassiveParticipantList extends InfoServiceInfoRequest{
	
	// Logging
	Logger log = Logger.getLogger(InfoServiceInfoReqPassiveParticipantList.class);

	public InfoServiceInfoReqPassiveParticipantList(){
		infoservicerequest = Util.stuffIntIntoShort(InfoServiceInfoRequest.IRQ_PASSIVEPARTICIPANTLIST);
	}

	@Override
	public void handleRequest(Connection c) {
		if (!c.isServerMode()) {
			log.warn("Not in servermode - can not answer INFOREQUEST");
			return;
		}
		
		InfoServiceInfoPassiveParticipantList i = new InfoServiceInfoPassiveParticipantList(c.getAssociatedServer().getInfoService().getTotalParticipants());
		ManagementMessageInfo m = new ManagementMessageInfo(i);
		try {
			c.sendMessage(m.getMessage());
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
	
}
