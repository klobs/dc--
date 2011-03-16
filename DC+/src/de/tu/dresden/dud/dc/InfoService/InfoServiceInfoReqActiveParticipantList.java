/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import java.io.IOException;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Log;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfo;

/**
 * @author klobs
 *
 */
public class InfoServiceInfoReqActiveParticipantList extends InfoServiceInfoRequest{
	
	public InfoServiceInfoReqActiveParticipantList(){
		infoservicerequest = Util.stuffIntIntoShort(InfoServiceInfoRequest.IRQ_ACTIVEPARTICIPANTLIST);
	}

	@Override
	public void handleRequest(Connection c) {
	
		if (!c.isServerMode()) {
			Log.print(Log.LOG_WARN, "Not in servermode - can not answer INFOREQUEST", this);
			return;
		}
		
		InfoServiceInfoActiveParticipantList i = new InfoServiceInfoActiveParticipantList(c.getAssociatedServer().getInfoService().getActiveParticipants());
		ManagementMessageInfo m = new ManagementMessageInfo(i);
		try {
			c.sendMessage(m.getMessage());
		} catch (IOException e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	
	}
	
}
