/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.ParticipantManager;
import de.tu.dresden.dud.dc.ParticipantMgmntInfo;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfo;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoKeyExchangeCommit;
import de.tu.dresden.dud.dc.InfoService.InfoServiceUpdateActiveJoining;
import de.tu.dresden.dud.dc.InfoService.InfoServiceUpdateActiveLeaving;
import de.tu.dresden.dud.dc.KeyGenerators.KeyGenerator;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessage;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdd;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfo;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageTick;

/**
 * @author klobs
 * 
 * This class manages all phases of a work cycle:
 *  - Organizing participating clients
 *  - Reservation
 *  - Sending
 *  It checks at the WorkCycleManager, whether there is something to send.
 *  If so it tries to reserve a work cycle. If a work cycle could be reserved,
 *  it adds removes the payload from the WorkCycleManager and feeds the WorkCycleRound during the sending phase correspondingly.
 */
public class WorkCycle extends Observable implements Observer {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycle.class);

	// Events
	public final static int WC_ROUND_ADDUP              = 0;
	public static final int WC_COUNT_CHANGED 		= 1;
	public static final int WC_STARTED				= 2;
	public static final int WC_RESERVATION	  		= 2; // actually nothing else than started
	public static final int WC_RESERVATION_FINISHED  = 3;
	public static final int WC_SENDING			 	= 3; // actually nothing else than reservation finished.
	public static final int WC_SENDING_FINISHED 		= 4;
	public static final int WC_FINISHED 				= 4;
	
	public static final int WC_MIN_ACTIVE_KEYS		= 0; // TODO find a good minimum here
	
	protected LinkedList<ManagementMessageAdded> addedMessages = new LinkedList<ManagementMessageAdded>();
	private   byte[] addedMessagesBin = new byte[0];
	
	protected LinkedHashSet<Connection> 	broadcastConnections = new LinkedHashSet<Connection>();  // Those connections will receive messages as a broadcast:
	protected LinkedHashSet<Connection> 	confirmedConnections = new LinkedHashSet<Connection>();  // Connections  that  have  actually sent  packages  during  that  work cycle.
	protected LinkedHashSet<Connection> 	expectedConnections = new LinkedHashSet<Connection>();   //Connections that are expected to send packages during that work cycle.
	private   LinkedHashSet<Connection> 	notifyJoinConnections = new LinkedHashSet<Connection>(); // Connections  that  will  join in  a  few  work cycles
	private   LinkedHashSet<Connection> 	notifyLeaveConnections = new LinkedHashSet<Connection>();// Connections that will leave in a few work cycles
	private   LinkedHashSet<Connection> 	leavingConnections = new LinkedHashSet<Connection>(); 	 // Those participants will leave this work cycle
	
	private   LinkedList<InfoServiceInfoKeyExchangeCommit> keyExchangeCommitMessages = new LinkedList<InfoServiceInfoKeyExchangeCommit>();
	
	protected KeyGenerator			assocKeyGenerator	= null;
	protected WorkCycleManager		assocWorkCycleManag	= null;
	protected int					currentPhase		= 0;
	protected int 					expectedRounds 	= 0;
	protected int					keyGenerationMethod				= -1;
	protected LinkedList<byte[]> 	payloads 			= new LinkedList<byte[]>();
	protected LinkedList<Integer>	individualPayloadLengths	= new LinkedList<Integer>();
	protected int 					relativeRound 	= -1;
	private	  ServerReservationChecker reservationChecker = null;
	protected long 					workcycleNumber 		= 0; // Long.MIN_VALUE;
	protected WorkCycleReserving		workCycleReserving		= null;
	protected WorkCycleSending		workCycleSending 		= null;
	protected Thread				workCycleSendingThread	= null;
	protected boolean 				started 			= false;
	protected int 					systemPayloadLength = 0;
	protected int 					timeout 			= 0;
	private   boolean				trap_when_possible  = true;

	// Semaphore
	protected final Semaphore sem	= new Semaphore(0, true);
	
	/**
	 * do not use this constructor.
	 */
	public WorkCycle(){
	}

	public WorkCycle(long workcycleNumber, int timeout, int payloadLength, WorkCycleManager r) {
		this.workcycleNumber = workcycleNumber;
		this.timeout = timeout;
		this.systemPayloadLength = payloadLength;
		this.assocWorkCycleManag = r;
		this.keyGenerationMethod  = r.getKeyGenerationMethod();
		this.assocKeyGenerator = r.getKeyGenerator();
		this.addObserver(this);
	}
	
	public synchronized void addKeyExchangeCommitMessages(InfoServiceInfoKeyExchangeCommit c){
		keyExchangeCommitMessages.add(c);
	}
	
	public void addExpectedConnection(Connection c) {
		expectedConnections.add(c);
		setChanged();
		notifyObservers(new Integer(WC_COUNT_CHANGED));
	}

	public void addExpectedConnections(Collection<Connection> l) {
		expectedConnections.addAll(l);
	}

	public void addJoinerNotification(Connection c) {
		notifyJoinConnections.add(c);
	}

	public void addLeavingConnection(Connection c) {
		leavingConnections.add(c);
	}

	public void addLeavingConnections(Collection<Connection> c) {
		leavingConnections.addAll(c);
	}

	public void addLeaverNotification(Connection c) {
		notifyLeaveConnections.add(c);
	}
	
	public synchronized void addMessageArrived(Connection c, ManagementMessageAdd m){
		switch (currentPhase){
		
		case WC_RESERVATION:
			workCycleReserving.addMessageArrived(c, m);
			break;
		case WC_RESERVATION_FINISHED:
			workCycleSending.addMessageArrived(c, m);
			break;
		}
	}

	public synchronized void addedMessageArrived(ManagementMessageAdded m) {
		if (KeyGenerator.isAsynchronous(keyGenerationMethod)) {
			switch (currentPhase) {
			case WC_RESERVATION:
				m.setReservation(true);
				addedMessages.add(m);
				sem.release();
				break;
			case WC_SENDING:
				addedMessages.add(m); // redundancy rules
				workCycleSending.addedMessageArrived(m);
				break;
			}
		}
		else if (KeyGenerator.isSynchronous(keyGenerationMethod)){
			addedMessagesBin = Util.concatenate(addedMessagesBin, m.getPayload());
			switch (currentPhase) {
			case WC_RESERVATION:
				m.setReservation(true);
				addedMessages.add(m);
				sem.release();
				break;
			case WC_SENDING:
				addedMessages.add(m); // redundancy rulez
				workCycleSending.addedMessageArrived(m);
				sem.release();
				break;
			}
		}
	}
	
	/**
	 * Does not actually broadcast the key exchange commit message,
	 * but just sends it to involved participants.
	 */
	public void broadcastKeyCommits() {

		while (keyExchangeCommitMessages.size() > 0) {
			InfoServiceInfoKeyExchangeCommit kec = keyExchangeCommitMessages
					.removeFirst();
			ManagementMessageInfo m = new ManagementMessageInfo(kec);

			ParticipantMgmntInfo pmi1 = getAssocParticipantManager()
					.getParticipantMgmntInfoByParticipantID(kec.getP1());
			ParticipantMgmntInfo pmi2 = getAssocParticipantManager()
					.getParticipantMgmntInfoByParticipantID(kec.getP2());

			if (pmi1 == null || pmi2 == null) {
				log.warn("Did not find one of participants. Sorry. Can not acomplish Key Exchange Request");
				continue;
			}
			try {
				pmi1.getAssocConnection().sendMessage(m.getMessage());
				pmi2.getAssocConnection().sendMessage(m.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean checkWhetherReservationIsFinishedOnServerSide(ManagementMessageAdded m){
		if (reservationChecker.hasReservationFinished(m)){
			
			expectedRounds = reservationChecker.getExpectedRounds();
			
			if (assocWorkCycleManag.getMessageLengthMode() == WorkCycleManager.MESSAGE_LENGTHS_VARIABLE)
				individualPayloadLengths = reservationChecker.getIndividualPayloadLengths();
			
			currentPhase = WC_RESERVATION_FINISHED;
			
			if (reservationChecker.getExpectedRounds() > 0) {
				workCycleSending = new WorkCycleSending(this);
				workCycleSending.addObserver(this);
			
			} else {
				setChanged();
				notifyObservers(WC_FINISHED);
			}
			
			return true;
		}
		return false;
	}
	
	/**
	 * This method collects the payloads, which are incoming from the different
	 * connections
	 * 
	 * Should be called on <b>server side</b>.
	 * 
	 * @param c
	 * @param p
	 */
	public void collectPayload(Connection c, byte[] p) {
		if (expectedConnections.contains(c)) {
			payloads.add(p);
			expectedConnections.remove(c);
			confirmedConnections.add(c);
		}
	}

/*	public byte[] failStopKeyGeneration(ParticipantMgmntInfo pmi){
		
	}
	*/
	public byte[] consumePayload(){
		if (trap_when_possible && assocWorkCycleManag.getPayloadList().size() <= 0){
			return Util.fillAndMergeSending((new String("Trap").getBytes()), new byte [systemPayloadLength]);
		}
		else if (assocWorkCycleManag.getPayloadList().size() <= 0) return null;
		return assocWorkCycleManag.getPayloadList().removeFirst();
	}
	
	public LinkedHashSet<Connection> getBroadcastConnections(){
		return this.broadcastConnections;
	}
	
	/**
	 * Returns the LinkedList with all the added messages.
	 * @return
	 */
	public LinkedList<ManagementMessageAdded> getAddedMessages(){
		return this.addedMessages;
	}
	
	public ParticipantManager getAssocParticipantManager(){
		return getAssocWorkCycleManager().getAssocParticipantManager();
	}
	
	public WorkCycleManager getAssocWorkCycleManager(){
		return this.assocWorkCycleManag;
	}
	
	/**
	 * @return the all participants (expected and confirmed), thus not the
	 *         notify ones
	 */
	public LinkedHashSet<Connection> getConnections() {
		LinkedHashSet<Connection> l = expectedConnections;
		l.addAll(confirmedConnections);
		return l;
	}
	
	public int getCurrentPhase(){
		return this.currentPhase;
	}

	/**
	 * @return The participants that are expected to take part in this work cycle.
	 */
	public LinkedHashSet<Connection> getExpectedConnections() {
		return expectedConnections;
	}
	
	/**
	 * This value will be known only after reservation.
	 * 
	 * @return
	 */
	public int getExpectedRounds() {
		return this.expectedRounds;
	}

	public LinkedList<Integer> getIndividualMessageLenghts(){
		if(assocWorkCycleManag.getMessageLengthMode() != WorkCycleManager.MESSAGE_LENGTHS_VARIABLE){
			log.warn("Individual message lengths requested, but not in right mode for using them");
		}
		
		return individualPayloadLengths;
	}
	
	public int getKeyGenerationMethod(){
		return this.keyGenerationMethod;
	}

	public byte[] getMessageBin(){
		return addedMessagesBin;
	}
	
	public byte[] getNextPayload(){
		if (trap_when_possible && assocWorkCycleManag.getPayloadList().size() <= 0){
			return Util.fillAndMergeSending((new String("Trap").getBytes()), new byte [systemPayloadLength]);
		}
		else if (assocWorkCycleManag.getPayloadList().size() <= 0) return null;
		return assocWorkCycleManag.getPayloadList().getFirst();
	}
	
	public LinkedList<byte[]> getPayloads(){
		return this.payloads;
	}
	/**
	 * this information will be available after reservation;
	 * 
	 * @return
	 */
	public int getRelativeRound() {
		return relativeRound;
	}

	/**
	 * @return the work cycle number
	 */
	public long getWorkCycleNumber() {
		return workcycleNumber;
	}

	/**
	 * Semaphore is needed to solve the producer-consumer problem for 
	 * DC+ and Reservations
	 * @return the semaphare
	 */
	public Semaphore getSemaphore(){
		return sem;
	}
	
	public int getSystemPayloadLength() {
		return systemPayloadLength;
	}
	
	public int getTimeout() {
		return this.timeout;
	}
	
	public boolean hasPayload(){
		if (trap_when_possible) return true;
		else if (assocWorkCycleManag.getPayloadList().size() > 0) return true;
		else return false;
	}
	
	
	/**
	 * Actually does the work for a work cycle on the side of the participants
	 */
	public void performSendingOnParticipantSide() {
		started = true;

		currentPhase = WC_RESERVATION;
		workCycleReserving = new WorkCycleReserving(this);
		Thread t = new Thread(workCycleReserving, "WorkCycleReserving");

		workCycleReserving.addObserver(this);
		t.start();
	}

	/**
	 * Actually does the work for a work cycle on the side of the server
	 */
	public void performDCWorkCycleOnServerSide() {
		started = true;

		try {

			ManagementMessage m = null;
			InfoServiceInfo i = null;
			
			updateBroadcastAndExpectedConnections();
			getAssocParticipantManager().update(workcycleNumber);
			
			// Tell participants about new key commits
			broadcastKeyCommits();
			
			// Notify all active participants about upcoming new participants
			if (notifyJoinConnections.size() > 0){
				i = new InfoServiceUpdateActiveJoining(notifyJoinConnections);

				m = new ManagementMessageInfo(i);

				for (Connection c : getBroadcastConnections()) {
					c.sendMessage(m.getMessage());
				}
			}
			
		 	if(notifyLeaveConnections.size() > 0) {
				i = new InfoServiceUpdateActiveLeaving(notifyLeaveConnections);

				m = new ManagementMessageInfo(i);

				for (Connection c : getBroadcastConnections()) {
					c.sendMessage(m.getMessage());
				}
			}
		 	
			currentPhase = WC_STARTED;
			
			// Zeichen geben, dass naechste Runde an der Reihe ist,
			// aber vorher nochmal kurze Verschnaufpause.
			try {
				Thread.sleep(assocWorkCycleManag.getTickPause());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			m = new ManagementMessageTick(workcycleNumber);
			for (Connection c : getBroadcastConnections()) {
				c.sendMessage(m.getMessage());
			}
			
			// Reservierung Starten
			reservationChecker = new ServerReservationChecker();
			workCycleReserving = new WorkCycleReserving(this);
			workCycleReserving.addObserver(this);
			// rest gets done as soon as messages arrive...
			
		} catch (IOException e) {
			log.error(e.toString());
		}
	}

	public boolean workCycleHasStarted() {
		return started;
	}

	public void setBroadcastConnections(LinkedHashSet<Connection> l ){
		this.broadcastConnections = l;
	}
	
	/**
	 * @param participants
	 *            the participants to set
	 */
	public void setExpectedConnections(LinkedHashSet<Connection> participants) {
		this.expectedConnections = participants;
	}

	/**
	 * @param workCycleNumber
	 *            the workCycleNumber to set
	 */
	public void setWorkCycleNumber(int workCycleNumber) {
		this.workcycleNumber = workCycleNumber;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof WorkCycleReserving) {
			if (((Integer) arg).intValue() == WorkCycle.WC_RESERVATION_FINISHED) {

				// Suck out those information
				expectedRounds = ((WorkCycleReserving) o).getExpectedRounds();
				relativeRound = ((WorkCycleReserving) o).getRelativeRound();

				if(assocWorkCycleManag.getMessageLengthMode() == WorkCycleManager.MESSAGE_LENGTHS_VARIABLE){
					individualPayloadLengths = ((WorkCycleReserving) o).getIndividualMessageLengths();
				}
				
				currentPhase = WC_SENDING;

				workCycleSending = new WorkCycleSending(this);
				workCycleSending.addObserver(this);

				// Summierung Starten
				if (KeyGenerator.isAsynchronous(keyGenerationMethod)) {

					if (!assocWorkCycleManag.isServerMode())
						workCycleSending.performDCRoundsParticipantSide();

				} else if (KeyGenerator.isSynchronous(keyGenerationMethod)) {
					if (!assocWorkCycleManag.isServerMode()) {

						Thread t = new Thread(workCycleSending,
								"WorkCycleSending");

						t.start();
					}
				}
			}
		} else if (o instanceof WorkCycleSending
					&& ((Integer) arg).intValue() == WorkCycle.WC_SENDING_FINISHED) {

				// TODO
				// Statistiken: Durchschnittliche Rundendauer
				// Durchschnittliche Teilnehmerzahl

				// Runde zu ende
				setChanged();
				notifyObservers(WC_FINISHED);
			}
	}
	
	private void updateBroadcastAndExpectedConnections(){
		broadcastConnections.addAll(getConnections());
		broadcastConnections.addAll(notifyJoinConnections);
		broadcastConnections.removeAll(leavingConnections);
		
		expectedConnections.removeAll(leavingConnections);
	}
}
