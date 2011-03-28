/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Participant;
import de.tu.dresden.dud.dc.ParticipantManager;
import de.tu.dresden.dud.dc.Server;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoKeyExchangeCommit;
import de.tu.dresden.dud.dc.KeyGenerators.KeyGenerator;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdd;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;
import de.tu.dresden.dud.dc.Util;

/**
 * @author klobs
 *
 */
public class WorkCycleManager implements Observer{

	// Logging
	Logger log = Logger.getLogger(WorkCycleManager.class);

	private KeyGenerator		assocKeyGenerator	= null;
	private ParticipantManager	assocParticipantManager = null;
	private long				currentWorkCycle	= -1;
	private boolean				fixedMessagemode=true;
	private int 				infoOffset		= 0;
	private int					joinOffset		= 0;
	private int 				leaveOffset		= 5;
	private int					packettimeout	= 0;
	private Participant			participant		= null;
	private boolean				participantmode = false;
	private int 				payloadlengths	= 0;
	private LinkedList<byte[]>	payloads		= new LinkedList<byte[]>();
	private TreeSet<WorkCycle> 		workcycless 			= new TreeSet<WorkCycle>(new WorkCycleComparator());
	private TreeSet<WorkCycle>		oldworkcycles		= new TreeSet<WorkCycle>(new WorkCycleComparator());
	private Server				server			= null;
	private boolean 			servermode		= false;
	
	
	
	public WorkCycleManager(short keyGenerationMethod, long workCycleNumber, int payloadLengths){
		payloadlengths = payloadLengths;
		
		assocKeyGenerator = KeyGenerator.keyGeneratorFactory(keyGenerationMethod, this);
		
		currentWorkCycle = workCycleNumber;
		
		workcycless.add(getWCByWCNumber(workCycleNumber));
		getCurrentWorkCycle().addObserver(this);
	}
	
	public synchronized void addKeyExchangeCommitMessages(InfoServiceInfoKeyExchangeCommit c){
		
	}
	
	public synchronized void addExpectedConnection(Connection c){
		long rn = c.getExpectedEntryWorkCycle();
		if (servermode)
			getWCByWCNumber(rn - infoOffset).addJoinerNotification(c);
		getWCByWCNumber(rn).addExpectedConnection(c);
	}

	public synchronized void addLeavingConnection(Connection c){
		long rn = c.getExpectedLeavingWorkCycle();
		if (servermode)
			getWCByWCNumber(rn - infoOffset).addLeaverNotification(c);
		getWCByWCNumber(rn).addLeavingConnection(c);
	}

	
	/**
	 * Tell the {@link WorkCycleManager} to schedule a message for delivery in future work cycles.
	 * Messages get delivered as soon as it is possible.
	 * It checks, whether the service uses the fixed size message protocol and then 
	 * paddes the messages with 0s, or rejects messages, if they are too long.
	 * @param p
	 */
	public synchronized int addMessage(byte[] p){
		if (p.length <= payloadlengths){
			if(fixedMessagemode){
				p = Util.fillAndMergeSending(p, new byte[payloadlengths]);
			}
			payloads.add(p);
			return 0;
		}
		else {
			log.error("Message too long to enter the send queue. Rejected");
			return 1;
		}
	}

	public synchronized void addMessageArrived(Connection c, ManagementMessageAdd m){
		getWCByWCNumber(m.getWorkCycleNumber()).addMessageArrived(c,m);
	}
	
	public synchronized void addedMessageArrived(ManagementMessageAdded m){
		// if (becomeActiveAfter <= m.getWCNumber())							// do we actually need this check? I dont think so.
			getWCByWCNumber(m.getWorkCycleNumber()).addedMessageArrived(m);
	}
	
	public ParticipantManager getAssocParticipantManager(){
		return assocParticipantManager;
	}
	
	public synchronized WorkCycle getCurrentWorkCycle(){
		return getWCByWCNumber(currentWorkCycle);
	}
	
	public synchronized long getCurrentWorkCycleNumber(){
		return currentWorkCycle;
	}
	
	/**
	 * This returns the work cycle number when a new participant 
	 * expected to start.
	 * @return
	 */
	public synchronized long getEntryWorkCycleNumber(){
		return getCurrentWorkCycle().getWorkCycleNumber() + joinOffset;
	}
	
	public long getInfoOffset(){
		return infoOffset;
	}
	
	public long getLeaveOffset(){
		return leaveOffset;
	}
	
	public synchronized LinkedList<byte []> getPayloadList(){
		return payloads;
	}
	
	public KeyGenerator getKeyGenerator(){
		return assocKeyGenerator;
	}
	
	public short getKeyGenerationMethod(){
		if (assocKeyGenerator != null)
			return assocKeyGenerator.getKeyGenerationMethod();
		return -1;
	}
	
	/**
	 * This method returns the next work cycle.
	 * But there is still work to be done, which means it is not ready to use, yet.
	 * 
	 * E.g. the expected participants have to be set up, etc.
	 * this will be done by the next() method.
	 * 
	 * @return A skeleton for the next work cycle.
	 */
	public synchronized  WorkCycle getNextWorkCycle(){
		return getWCByWCNumber(getCurrentWorkCycle().getWorkCycleNumber()+1);
	}
	
	public synchronized int getPacketTimeout(){
		return this.packettimeout;
	}
	
	public synchronized Participant getParticipant(){
		return this.participant;
	}
	
	public synchronized boolean getParticipantMode(){
		return participantmode;
	}
	/**
	 * Returns a work cycle with a desired work cycle number.
	 * If the work cycle does not exsist yet in the database,
	 * it will be created.
	 * 
	 * After creation, the work cycle will be saved in the database,
	 * and be returned on the next request.
	 *  
	 * @param d
	 * @return The desired work cycle
	 */
	public synchronized WorkCycle getWCByWCNumber(long d){
		Iterator<WorkCycle> i = workcycless.iterator();
		
		WorkCycle 	r  = null;
		long 	rn = 0; // Long.MIN_VALUE;
		
		while(i.hasNext()){
			r = i.next();
			
			rn = r.getWorkCycleNumber();
			
			if (rn < d){ 
				if (! i.hasNext()) r = null;
				continue;
			}
			else if (rn == d)
				break;
			else {
				r = null;
				break;
			}
		}
		
		if(r == null){
			r = new WorkCycle(d, packettimeout, payloadlengths, this);
			workcycless.add(r);
		}
		return r;
	}
	
	public synchronized Server getServer(){
		return server;
	}
	
	/**
	 * How long is one symbol payload? 
	 * @return character length in bytes
	 */
	public int getSymbolLength() {
		return payloadlengths;
	}
	
	
	public boolean isRunning(){
		return getCurrentWorkCycle().workCycleHasStarted();
	}
	
	public boolean isServerMode(){
		return servermode;
	}
		
	public void setAssocParticipantManager(ParticipantManager p){
		assocParticipantManager = p;
	}
	
	/**
	 * Advance to the next work cycle.
	 * 
	 * Migrate all important data from the current to the next work cycle.
	 * Delete the old work cycle and make the next work cycle the new current one
	 */
	private synchronized void setupNextWorkCycle(){
		WorkCycle c = getCurrentWorkCycle();
		WorkCycle n = getNextWorkCycle();
		
		c.deleteObserver(this);
		n.addObserver(this);
		
		// the next work cycle should contain all the connection that 
		// were also in the current work cycle.
		// those connections that were marked for leaving will be 
		// subtracted internally
		n.addExpectedConnections(c.getConnections());
		n.setBroadcastConnections(c.getBroadcastConnections());
		oldworkcycles.add(c); // save the old work cycle
		workcycless.remove(c);
		currentWorkCycle = n.getWorkCycleNumber();
	}
	
	public synchronized void setFixedMessageMode(boolean m){
		fixedMessagemode = m;
	}
	
	public synchronized void setInfoOffset(int i){
		infoOffset = i;
	}

	public synchronized void setJoinOffset(int j){
		joinOffset = j;
	}
	
	public synchronized void setPacketTimeout(int t){
		packettimeout = t;
	}
	
	public synchronized void setParticipant(Participant p){
		participantmode = true;
		participant = p;
	}
	
	public synchronized void setServer(Server s){
		servermode = true;
		server = s;
	}
	
	/**
	 * A this message is called when a tick arrived at the participant side.
	 * @param t
	 */
	public synchronized void tickArrived(long t){
		if (getCurrentWorkCycle().workCycleHasStarted()){
			// the current work cycle has already started, so it seems that we are
			// in a hot system.
			setupNextWorkCycle();
		} 
		
		currentWorkCycle = t;
		
		if (assocParticipantManager.getMyInfo().isActive()) {
			getWCByWCNumber(t).performSendingOnParticipantSide();
		}
	}
	
	/**
	 * Advance to next work cycle.
	 * This method detects whether the work cycles are in timeout / notimeout mode
	 * and advances correspondingly.
	 * 
	 * In notimeout mode, the WorkCycleManager waits until all expected participants sent their part.
	 * 
	 * This method also sends notification about the new work cycle start to all active participants.
	 */
	public synchronized void tickServerSide(){
		if (getCurrentWorkCycle().workCycleHasStarted()){
			// the current work cycle has already started, so it seems that we are
			// in a hot system.
			setupNextWorkCycle();
			setJoinOffset(3);
			setInfoOffset(1);
		} 
		
		// it seems to be the first work cycle, or we had 
		// interruptions in between
			getCurrentWorkCycle().performDCWorkCycleOnServerSide();
	}
	
	@Override
	public synchronized void update(Observable o, Object arg){
		if( o instanceof WorkCycle ){
	
			switch (((Integer) arg).intValue()) {
			
			case WorkCycle.WC_COUNT_CHANGED:
				if (servermode) {
					int p = ((WorkCycle) o).getConnections().size();

					if ((p > 1) && !((WorkCycle) o).workCycleHasStarted()) { // This 1 must become
																		// a 2 in future
						tickServerSide(); // Start the work cycle;
					}
				}
				break;
			
			case WorkCycle.WC_FINISHED:
				if (servermode){
				tickServerSide();
				}
				
				if(! servermode){
					
					if (assocParticipantManager.getParticipantMgmntInfoFor(participant).getInactiveInWorkCycle() == currentWorkCycle + 1){
						assocParticipantManager.update(currentWorkCycle + 1);
					}
				}
				break;
			}
		}
	}
	
}
