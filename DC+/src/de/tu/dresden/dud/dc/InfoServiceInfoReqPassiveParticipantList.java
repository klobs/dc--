/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.IOException;

/**
 * @author klobs
 *
 */
public class InfoServiceInfoReqPassiveParticipantList extends InfoServiceInfoRequest{
	
	public InfoServiceInfoReqPassiveParticipantList(){
		infoservicerequest = Util.stuffIntIntoShort(InfoServiceInfoRequest.IRQ_PASSIVEPARTICIPANTLIST);
	}

	@Override
	public void handleRequest(Connection c) {
		if (!c.isServerMode()) {
			Log.print(Log.LOG_WARN, "Not in servermode - can not answer INFOREQUEST", this);
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
