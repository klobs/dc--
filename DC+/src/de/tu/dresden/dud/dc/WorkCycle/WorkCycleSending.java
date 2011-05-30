/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.KeyGenerators.KeyGenerator;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdd;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;

/**
 * @author klobs
 * 
 */
public class WorkCycleSending extends WorkCycle implements Observer, Runnable {

	// Logging
	private static Logger log = Logger.getLogger(WorkCycleSending.class);
	
	public static final long MODULUS	= 0x100000000L;
	
	// internal variables
	protected boolean finished = false;
	protected byte[] payloadSend = null;
	protected WorkCycle assocWorkCycle = null;
	protected TreeSet<WorkCycleRound> rounds = new TreeSet<WorkCycleRound>(
			new WorkCycleRoundComparator());
	private   boolean successful = false;
	
	// sending
	protected int currentRound = 0;

	public WorkCycleSending(WorkCycle r) {
		assocWorkCycle = r;
		assocWorkCycleManag = r.getAssocWorkCycleManager();
		assocKeyGenerator = assocWorkCycleManag.getKeyGenerator();
		broadcastConnections = r.getBroadcastConnections();
		expectedConnections = r.getExpectedConnections();
		expectedRounds = r.getExpectedRounds() < 1 ? -1 : r
				.getExpectedRounds();
		keyGenerationMethod = r.getKeyGenerationMethod();
		relativeRound = r.getRelativeRound();
		systemPayloadLength = r.getSystemPayloadLength();
		workcycleNumber = r.getWorkCycleNumber();
		timeout = r.getTimeout();
		
		if (relativeRound >= 0)
			payloadSend = r.consumePayload();
		else
			payloadSend = null;
		
		if (assocWorkCycleManag.getMessageLengthMode() == WorkCycleManager.MESSAGE_LENGTHS_VARIABLE){
			individualPayloadLengths = r.getIndividualMessageLenghts();
		}
		
		this.addObserver(this);
	}

	public void addMessageArrived(Connection c,
			ManagementMessageAdd m) {
		synchronized (assocWorkCycleManag) {
			WorkCycleRound rn = getRoundByRoundNumber(m.getRoundNumber());
			rn.addMessageArrived(c, m);
		}
	}

	public synchronized void addedMessageArrived(ManagementMessageAdded m) {
		if (relativeRound == m.getRoundNumber()
				&& !Arrays.equals(m.getPayload(), payloadSend)) {
			log.error("Expected and actual received payloads are not equal! Something is messing around with us!");
		}
		successful = true;
	}

	protected void addUp() {
		byte[] b = null;
		if (assocWorkCycle.getCurrentPhase() == WorkCycle.WC_RESERVATION) {
			b = new byte[WorkCycleReservationPayload.RESERVATION_PAYLOAD_SIZE];

		} else {
				
			b = new byte[systemPayloadLength];

			if (assocWorkCycleManag.getMessageLengthMode() == WorkCycleManager.MESSAGE_LENGTHS_VARIABLE){
				b = new byte[assocWorkCycle.getIndividualMessageLenghts().get(currentRound)];
			}
			
			Iterator<byte[]> i = payloads.iterator();

			while (i.hasNext()) {
				byte[] c = i.next();
				b = Util.mergeDCwise(b, c, WorkCycleSending.MODULUS);
			}

		} 
		
		payloadSend = b;
	}

	protected void broadcastSum() {
		ManagementMessageAdded m = new ManagementMessageAdded(workcycleNumber,
				currentRound, payloadSend);

		if (assocWorkCycle.getCurrentPhase() == WorkCycle.WC_RESERVATION)
			assocWorkCycle.checkWhetherReservationIsFinishedOnServerSide(m);

		try {
				Iterator<Connection> i = getBroadcastConnections().iterator();
				while (i.hasNext()) {
					i.next().sendMessage(m.getMessage());
				}
		} catch (IOException o) {
			log.error(o.toString());
		}
	}

	public void finalizeRoundServerSide(WorkCycleRound r) {
		// set right payloads
		payloads = r.getPayloads();
		// Add up
		addUp();
		log.debug("add up this " + Arrays.toString(payloadSend));
		// After adding up
		// send out the the added
		broadcastSum();

		// increase the round counter.
		currentRound++;

		// clean up the round
		rounds.remove(r);

		if (!(this instanceof WorkCycleReserving)
				&& (assocWorkCycle.getCurrentPhase() == WorkCycle.WC_SENDING)
				&& (currentRound == expectedRounds)) {
			finished = true;
			setChanged();
			notifyObservers(WorkCycle.WC_FINISHED);
		}
	}

	/**
	 * Returns a work cycle with a desired work cycle number. If the work cycle does not exsist
	 * yet in the database, it will be created.
	 * 
	 * After creation, the work cycle will be saved in the database, and be returned
	 * on the next request.
	 * 
	 * @param d
	 * @return The desired work cycle
	 */
	public synchronized WorkCycleRound getRoundByRoundNumber(int d) {
		Iterator<WorkCycleRound> i = rounds.iterator();

		WorkCycleRound r = null;
		int srn = 0; // Long.MIN_VALUE;

		while (i.hasNext()) {
			r = i.next();

			srn = r.getRoundNumber();

			if (srn < d) {
				if (!i.hasNext())
					r = null;
				continue;
			} else if (srn == d)
				break;
			else {
				r = null;
				break;
			}
		}

		if (r == null) {
			r = new WorkCycleRound(d, this);
			r.addObserver(this);
			rounds.add(r);
		}
		return r;
	}

	public boolean hasFinished() {
		return this.finished;
	}

	public boolean hasWorkCycleBeenSuccessful(){
		return successful;
	}
	
	public boolean isServerMode() {
		return assocWorkCycle.getAssocWorkCycleManager().isServerMode();
	}


	/**
	 * The rounds of a DC-Network are deterministic. What we can do is
	 * prepare all messages and then fire them as fast as possible to the
	 * server, hoping it can handle them.
	 */
	public void performDCRoundsParticipantSide() {
		ManagementMessageAdd m = null;
		int currentMessageLength = 0;
		
		if (relativeRound >= 0 && 
			assocWorkCycleManag.getMessageLengthMode() == WorkCycleManager.MESSAGE_LENGTHS_VARIABLE) {
			payloadSend = Util.fillAndMergeSending(payloadSend,
					new byte[individualPayloadLengths.get(relativeRound)
							.intValue()]);
		}
		
		while (!finished) {
			
			if (assocWorkCycleManag.getMessageLengthMode() == WorkCycleManager.MESSAGE_LENGTHS_VARIABLE) {
				currentMessageLength = individualPayloadLengths.get(
						currentRound).intValue();
			} else {
				currentMessageLength = systemPayloadLength;
			}
			
			if (currentRound == relativeRound) {
				byte[] p = Util.mergeDCwise(payloadSend,
						assocKeyGenerator.calcKeys(currentMessageLength, workcycleNumber, currentRound), MODULUS);
				m = new ManagementMessageAdd(workcycleNumber, currentRound, p);
			} else {
				byte[] p = assocKeyGenerator.calcKeys(currentMessageLength, workcycleNumber, currentRound);
				m = new ManagementMessageAdd(workcycleNumber, currentRound, p);
			}

			try {
				getAssocParticipantManager().getMyInfo().getAssocConnection()
						.sendMessage(m.getMessage());
			} catch (IOException o) {
				log.error(o.toString());
			}

			currentRound++;

			if (currentRound >= expectedRounds)
				finished = true;
			
			try{
			if(KeyGenerator.isSynchronous(keyGenerationMethod) && finished != true)
				assocWorkCycle.getSemaphore().acquire();
			}
			catch (InterruptedException e){
				log.error(e.toString());
			}
		}
		
		setChanged();
		notifyObservers(WorkCycle.WC_FINISHED);		
	}

	public void run() {
		performDCRoundsParticipantSide();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof WorkCycleRound
				&& ((Integer) arg).intValue() == WorkCycle.WC_ROUND_ADDUP) {
			finalizeRoundServerSide((WorkCycleRound) o);
		}
	}

}
