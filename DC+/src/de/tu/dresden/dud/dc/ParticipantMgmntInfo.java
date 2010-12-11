/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

public class ParticipantMgmntInfo {
	private boolean 	active			= false;
	private long		activeInWorkCycle		= -1;
	private Connection	assocConnection	= null;
	private long		inactiveInWorkCycle		= -1;
	private DCKey		key				= new DCKey();
	private Participant participant 	= null;
	private boolean		passive			= false;
	
	public ParticipantMgmntInfo(Participant p) {
		participant = p;
		key 		= new DCKey();
	}

	public long getActiveInWorkCycle(){
		return activeInWorkCycle;
	}
	
	public Connection getAssocConnection(){
		return assocConnection;
	}

	public long getInactiveInWorkCycle(){
		return inactiveInWorkCycle;
	}
	
	public DCKey getKey(){
		return key;
	}
	
	/**
	 * @return the participant
	 */
	public Participant getParticipant() {
		return participant;
	}

	public boolean hasExchangedKey(){
		if (key == null) return false;
		else if (key.getState() != DCKey.KEY_EXCHANGED) return false;
		return true;
	}
	
	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * @return the passive
	 */
	public boolean isPassive() {
		return passive;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive() {
		this.active = true;
		activeInWorkCycle = -1;
		if (active) setPassive(active);
	}

	public void setActiveInWorkCycle(long wcn){
		activeInWorkCycle = wcn;
	}
	
	public void setAssocConnection(Connection c){
		assocConnection = c;
	}
	
	public void setInactive(){
		this.active = false;
		inactiveInWorkCycle = -1;	
	}
	
	public void setInactiveInWorkCycle(long wcn){
		inactiveInWorkCycle = wcn;
	}
	
	public void setKey(DCKey k){
		key = k;
	}
		
	/**
	 * @param participant the participant to set
	 */
	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	/**
	 * @param passive the passive to set
	 */
	public void setPassive(boolean passive) {
		this.passive = passive;
	}
	
	
}
