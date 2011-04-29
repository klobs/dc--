/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;

/**
 * A ParticipantManager keeps track of different {@link Participant}s that are
 * known to the {@link Server} or the {@link Participant}s.
 * 
 * Each server has one associated participant manager. Each participant has one
 * associated participant manger per connection.
 * 
 * This class is an observable. It notifies the observers after changes happen
 * to the tracked participants.
 * 
 * @author klobs
 * 
 */
public class ParticipantManager extends Observable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2335675816206621702L;
	
	// this is for graphical output... :(
	/**
	 * Notification for observers. A new participant has been added for tracking.
	 */
	public  static final int 	PARTMNG_INTERVAL_ADDED_GENERAL 		= 0;

	/**
	 * Notification for observers. A new participant has been added and it
	 * directly became active.
	 */
	public  static final int 	PARTMNG_INTERVAL_ADDED_ACTIVE 		= 1;

	/**
	 * Notification for observers. A new participant has been added and it
	 * directly became passive.
	 */
	public  static final int 	PARTMNG_INTERVAL_ADDED_PASSIVE 		= 2;

	/**
	 * Notification for observers. A new participant has beend added and it
	 * directly became active.
	 */
	public  static final int 	PARTMNG_INTERVAL_CHANGED_ACTIVE 	= 3;

	/**
	 * Notification for observers. A new participant has beend added and it
	 * directly became passive.
	 */
	public  static final int 	PARTMNG_INTERVAL_CHANGED_PASSIVE 	= 4;
	
	private List<ParticipantMgmntInfo> participantDB 		= Collections.synchronizedList(new LinkedList<ParticipantMgmntInfo>());
	private Participant 					 me 			= null;
	
	/**
	 * Each {@link Participant} of the linked list will be added for tracking.
	 * 
	 * Actually only calls addParticipant() for each element of pl.
	 * 
	 * @param pl list of participants that shall be added for tracking.
	 */
	public void addParticipants(LinkedList<Participant> pl){
		for (Participant p : pl){
			addParticipant(p);
		}
	}
	
	/**
	 * Adds a participant for tracking.
	 * It also creates the corresponding {@link ParticipantMgmntInfo} for the new participant.
	 * @param p {@link Participant} that you want to add for tracking.
	 * @return {@link ParticipantMgmntInfo} that has been created for p.
	 */
	public ParticipantMgmntInfo addParticipant(Participant p){
		ParticipantMgmntInfo pmi = getParticipantMgmntInfoFor(p);
		if (pmi == null){
			pmi = new ParticipantMgmntInfo(p);
			// this is the model stuff.
			// As we do it for each participant individually, it can become quite slow, i guess
			// for a big number of participants.
			participantDB.add(pmi);
			setChanged();
			notifyObservers(PARTMNG_INTERVAL_ADDED_GENERAL);
		}	
		return pmi;
	}

	/**
	 * Returns a list with all known {@link Participant}'s
	 * {@link ParticipantMgmntInfo} that are active.
	 * 
	 * @return The list with active participants.
	 */
	public synchronized LinkedList<ParticipantMgmntInfo> getActivePartMgmtInfo(){
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		LinkedList<ParticipantMgmntInfo> l = new LinkedList<ParticipantMgmntInfo>();
		ParticipantMgmntInfo pmi = null;
		
		while(i.hasNext()){
			pmi = i.next();
			if (pmi.isActive()) l.add(pmi);
		}
		
		return l;
	}

	public synchronized void cleanAllButPassiveConnections(LinkedList<Participant> ppl){
		HashSet<ParticipantMgmntInfo> oldPpl = new HashSet<ParticipantMgmntInfo>();
		
		oldPpl.addAll(getPassivePartMgmtInfo());
		oldPpl.removeAll(ppl);
		
		participantDB.removeAll(oldPpl);
		
		setChanged();
		notifyObservers(PARTMNG_INTERVAL_CHANGED_PASSIVE);
	}
	
	public synchronized LinkedList<ParticipantMgmntInfo> getActivePartExtKeysMgmtInfo(){
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		LinkedList<ParticipantMgmntInfo> l = new LinkedList<ParticipantMgmntInfo>();
		ParticipantMgmntInfo pmi = null;
		
		while(i.hasNext()){
			pmi = i.next();
			if (pmi.isActive() && pmi.hasExchangedKey()) l.add(pmi);
		}
		
		return l;
	}
	
	/**
	 * Direct access to the database of tracked participants
	 * @param i which element of the database you want to return
	 * @return the corresponding {@link ParticipantMgmntInfo}
	 */
	public synchronized ParticipantMgmntInfo getElementAt(int i){
		return participantDB.get(i);
	}

	/**
	 * Standard getter.
	 * 
	 * @return The {@link Participant} which is the owner of the
	 *         {@link ParticipantManager}.
	 */
	public synchronized Participant getMe(){
		return me;
	}
	
	
	public synchronized ParticipantMgmntInfo getMyInfo(){
		return getParticipantMgmntInfoFor(me);
	}
	
	/**
	 * Standard getter.
	 * 
	 * @param p
	 *            an already tracked participant. Can be null, but then the
	 *            result will also be null.
	 * @return the ManagementInformation for the specific participant p. Returns
	 *         null, if p is null, or does not exist.
	 */
	public synchronized ParticipantMgmntInfo getParticipantMgmntInfoFor(Participant p){
		if (p == null) return null;
		
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		ParticipantMgmntInfo pm = null;
		
		while(i.hasNext()){
			pm = i.next();
			if (pm.getParticipant().equalsTo(p)) return pm;
		}
		
		return null;
	}

	/**
	 * Standard getter.
	 * 
	 * @param c
	 *            a connection that belongs to a participant. Can be null, but then the
	 *            result will also be null.
	 * @return the ManagementInformation for the specific participant p. Returns
	 *         null, if p is null, or does not exist.
	 */
	public synchronized ParticipantMgmntInfo getParticipantMgmntInfoFor(Connection c){
		if (c == null) return null;
		
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		ParticipantMgmntInfo pm = null;
		
		while(i.hasNext()){
			pm = i.next();
			if (pm.getAssocConnection().equals(c)) return pm;
		}
		
		return null;
	}
	
	
	/**
	 * Standard getter.
	 * 
	 * @param id
	 *            An id of a participant
	 * @return the ManagementInformation that belongs to the {@link Participant}
	 *         with the id.
	 */
	public synchronized ParticipantMgmntInfo getParticipantMgmntInfoByParticipantID(String id){
		if (id == null) return null;
		
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		ParticipantMgmntInfo pm = null;
		
		while(i.hasNext()){
			pm = i.next();
			if (pm.getParticipant().getId().compareTo(id) == 0) return pm;
		}
		
		return null;
	}

	/**
	 * Returns a list with all known {@link Participant}'s
	 * {@link ParticipantMgmntInfo} that are passive. These include all acvite
	 * participants, too.
	 * 
	 * @return The list with active participants.
	 */
	public synchronized LinkedList<ParticipantMgmntInfo> getPassivePartMgmtInfo(){
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		LinkedList<ParticipantMgmntInfo> l = new LinkedList<ParticipantMgmntInfo>();
		ParticipantMgmntInfo pmi = null;
		
		while(i.hasNext()){
			pmi = i.next();
			if (pmi.isPassive()) l.add(pmi);
		}
		
		return l;
	}
	
	/**
	 * Standard getter.
	 * @return The number of tracked participants.
	 */
	public synchronized int getSize() {
		return participantDB.size();
	}
	
	public synchronized void removeParticipant(ParticipantMgmntInfo pmi){
		if (participantDB.contains(pmi)){
			participantDB.remove(pmi);
		}
	}
	
	public synchronized void removeParticipant(Participant p){
		ParticipantMgmntInfo pmi = getParticipantMgmntInfoFor(p);
		
		if (pmi != null){
			participantDB.remove(pmi);
		}
	}
	
	/**
	 * Standard setter.
	 * 
	 * Associate a {@link Participant} with the {@link ParticipantManager}.
	 * @param p The participant that you want to associate to the participant manager.
	 */
	public synchronized void setMe(Participant p){
		me = p;
	}

	/**
	 * Find {@link Participant} p in the database, and set it to an active
	 * state. If p is not yet member of the database, create p.
	 * 
	 * This method notifies the observers, as described in the general
	 * description of this class.
	 * 
	 * @param p
	 */
	public synchronized void setParticipantActive(Participant p){
		if(p == null) return;
		ParticipantMgmntInfo t = getParticipantMgmntInfoFor(p);
		if(t == null) t = addParticipant(p);
		t.setActive();
		
		setChanged();
		notifyObservers(PARTMNG_INTERVAL_CHANGED_ACTIVE);
	}
	
	/**
	 * This method marks a participant to become active in {@link WorkCycle} with work cycle number r.
	 * @param p Participant that shall be modified.
	 * @param r work cycle number on which p becomes active.
	 */
	public synchronized void setParticipantActiveAfterWorkCycle(Participant p, long r){
		if(p == null) return;
		ParticipantMgmntInfo t = getParticipantMgmntInfoFor(p);
		if(t == null) t = addParticipant(p);
		t.setActiveInWorkCycle(r);
	}

	/**
	 * Take each {@link Participant} in p and call setParticipantActive().
	 * @param pl A list of participants that shall be set to be active.
	 */
	public synchronized void setParticipantsActive(LinkedList<Participant> pl){
		for(Participant p : pl)
			setParticipantActive(p);
	}

	
	/**
	 * Find {@link Participant} p in the database, and set it to an passive
	 * state. If p is not yet member of the database, create p.
	 * 
	 * This method notifies the observers, as described in the general
	 * description of this class.
	 * 
	 * @param p
	 */
	public synchronized void setParticipantPassive(Participant p){
		ParticipantMgmntInfo t = getParticipantMgmntInfoFor(p);
		if(p == null) return;
		if(t == null) t = addParticipant(p);
		t.setPassive(true);
		
		setChanged();
		notifyObservers(PARTMNG_INTERVAL_CHANGED_PASSIVE);
	}
	
	/**
	 * Take each {@link Participant} in p and call setParticipantPassive().
	 * @param pl A list of participants that shall be set to be passive.
	 */
	public synchronized void setParticipantsPassive(LinkedList<Participant> pl){
		for(Participant p : pl)
			setParticipantPassive(p);
	}
	

	/**
	 * Set the active record for participant p to false.
	 * 
	 * @param p Participant that you want to set inactive. Remark: p stays passive. 
	 */
	public synchronized void unsetParticipantActive(Participant p){
		if(p == null) return;
		ParticipantMgmntInfo t = getParticipantMgmntInfoFor(p);
		if(t == null) t = addParticipant(p);
		t.setInactive();
		
		setChanged();
		notifyObservers(PARTMNG_INTERVAL_CHANGED_ACTIVE);
	}
	
	
	/**
	 * Take each {@link Participant} in p and call unsetParticipantActive().
	 * @param pl A list of participants that shall be set to be inactive.
	 */
	public synchronized void unsetParticipantsActive(LinkedList<Participant> pl){
		for(Participant p : pl)
			unsetParticipantActive(p);
	}
	
	/**
	 * This method marks a participant to become inactive in {@link WorkCycle} with work cycle number r.
	 * @param p Participant that shall be modified.
	 * @param r work cycle number on which p becomes active.
	 */
	public synchronized void unsetParticipantActiveAfterWorkCycle(Participant p, long r){
		if(p == null) return;
		ParticipantMgmntInfo t = getParticipantMgmntInfoFor(p);
		if(t == null) t = addParticipant(p);
		t.setInactiveInWorkCycle(r);
	}

	
	/**
	 * Set the passive record for participant p to false.
	 * 
	 * @param p Participant that you want to set inpassive. 
	 */
	public synchronized void unsetParticipantPassive(Participant p){
		ParticipantMgmntInfo t = getParticipantMgmntInfoFor(p);
		if(p == null) return;
		if(t == null) t = addParticipant(p);
		t.setPassive(false);
		
		setChanged();
		notifyObservers(PARTMNG_INTERVAL_CHANGED_PASSIVE);
	}
	
	/**
	 * Take each {@link Participant} in p and call unsetParticipantPassive().
	 * @param pl A list of participants that shall be set to be inpassive.
	 */
	public synchronized void unsetParticipantsPassive(LinkedList<Participant> pl){
		for(Participant p : pl)
			unsetParticipantPassive(p);
	}

	/**
	 * After a new work cycle begins, this method shall be called with the new work cycle
	 * number. It then sets all participants to active whose 'become active
	 * in'-work cycle number is smaller or equal work cycle number.
	 * 
	 * Usually this method is called from {@link WorkCycle}, performWorkCycleOnServerSide().
	 * 
	 * @param workcyclenumber
	 */
	public synchronized void update(long workcyclenumber){
		Iterator<ParticipantMgmntInfo> i = participantDB.iterator();
		ParticipantMgmntInfo pmi = null;
		
		while(i.hasNext()){
			pmi = i.next();
			if(pmi.getActiveInWorkCycle() > -1 && pmi.getActiveInWorkCycle() <= workcyclenumber)
				pmi.setActive();
			if(pmi.getInactiveInWorkCycle() > -1 && pmi.getInactiveInWorkCycle() <= workcyclenumber)
				pmi.setInactive();
		}
	}
}
