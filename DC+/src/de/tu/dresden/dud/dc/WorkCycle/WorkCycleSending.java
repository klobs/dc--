/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.ParticipantMgmntInfo;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdd;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;

/**
 * @author klobs
 * 
 */
public class WorkCycleSending extends WorkCycle implements Observer, Runnable {

	// Logging
	Logger log = Logger.getLogger(WorkCycleSending.class);
	
	private static final long MODULUS	= 0x100000000L;
	
	// internal variables
	protected boolean finished = false;
	protected byte[] payloadSend = null;
	protected WorkCycle assocWorkCycle = null;
	protected TreeSet<WorkCycleRound> rounds = new TreeSet<WorkCycleRound>(
			new WorkCycleRoundComparator());

	// sending
	protected int currentRound = 0;

	public WorkCycleSending(WorkCycle r) {
		assocWorkCycle = r;
		assocWorkCycleManag = r.getAssocWorkCycleManager();
		broadcastConnections = r.getBroadcastConnections();
		expectedConnections = r.getExpectedConnections();
		expectedRounds = r.getExpectedRounds() < 1 ? -1 : r
				.getExpectedRounds();
		method = r.getMethod();
		relativeRound = r.getRelativeRound();
		systemPayloadLength = r.getSystemPayloadLength();
		workcycleNumber = r.getWorkCycleNumber();
		timeout = r.getTimeout();
		if (relativeRound >= 0)
			payloadSend = r.consumePayload();
		else
			payloadSend = null;
		this.addObserver(this);
	}

	public synchronized void addMessageArrived(Connection c,
			ManagementMessageAdd m) {

		switch (method) {
		case WorkCycleManager.METHOD_DC:
			WorkCycleRound rn = getRoundByRoundNumber(m.getRoundNumber());
			rn.addMessageArrived(c, m);
			break;

		case WorkCycleManager.METHOD_DCPLUS:
			rn = getRoundByRoundNumber(m.getRoundNumber());
			rn.addMessageArrived(c, m);			
			break;
		
		default:
			break;
		}
	}

	public synchronized void addedMessageArrived(ManagementMessageAdded m) {
		if (relativeRound == m.getRoundNumber()
				&& !Arrays.equals(m.getPayload(), payloadSend)) {
			log.error("Expected and actual received payloads are not equal! Something is messing around with us!");
		}

		switch (method) {
		case WorkCycleManager.METHOD_DC:
			break;
		case WorkCycleManager.METHOD_DCPLUS:
			break;
		}
	}

	protected void addUp() {
		byte[] b = null;
		if (assocWorkCycle.getCurrentPhase() == WorkCycle.WC_RESERVATION) {
			b = new byte[WorkCycleReservationPayload.RESERVATION_PAYLOAD_SIZE];
		} else {
			b = new byte[systemPayloadLength];

			Iterator<byte[]> i = payloads.iterator();

			while (i.hasNext()) {
				byte[] c = i.next();
				b = mergeDCwise(b, c);
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

	/**
	 * This method generates "random" keys by using AES in ECB mode and using
	 * the exchanged DH-Key as IV.
	 * 
	 * Before adding, it checks whether there are enough different participant
	 * keys to be added.
	 * 
	 * @param length length in bytes. Must be multiple of 4 (bytes).
	 * @return
	 */
	public synchronized byte[] calcKeysMain(int length) {

		if ((length % 4) != 0) {
			log.error("Required key length is no multiple of 4");
		}

		byte[] b = new byte[length];
		Arrays.fill(b, (byte) 0);

		ParticipantMgmntInfo pmi = null;

		// apl = active participants list
		LinkedList<ParticipantMgmntInfo> apl = assocWorkCycleManag
				.getAssocParticipantManager().getActivePartExtKeysMgmtInfo();

		Iterator<ParticipantMgmntInfo> i = apl.iterator();

		ArrayList<byte[]> bl = new ArrayList<byte[]>(apl.size());

		if (apl.size() < WorkCycle.WC_MIN_ACTIVE_KEYS) {
			log.error("There are not enough active keys.");
			// TODO what is the resulting action we should perform?
		}

		while (i.hasNext()) {
			pmi = i.next();
			byte[] cr = null;

			cr = calcKeysAESPRNGCaller(workcycleNumber, currentRound, pmi,
					length);

			// if we use DC, we have the key here. If we use failstop,
			// the fun only begins here: cr now contains a_{ij}^t now let's
			// go for the Σ-part
			if (method == WorkCycleManager.METHOD_DCPLUS) {
				cr = mergeDCwise(
						cr,
						calcKeysSigmaCaller(workcycleNumber, currentRound, pmi,
								length));
			}

			// Finally calculate the inverse, when needed.
			if (pmi.getKey().getInverse()) {
				cr = inverseKey(cr);
			}
			bl.add(cr);
		}

		for (byte[] w : bl) {
			b = mergeDCwise(b, w);
		}

		return b;
	}

	private synchronized byte[] calcKeysAESPRNG(final long wcn, final int rn,
			final byte[] sharedSecret, final byte[] nonce, final int partn) {
		Cipher c = null;
		byte[] cr = new byte[0];
		SecretKeySpec s = null;

		try {

			byte[] trn = Util.concatenate(Util.stuffLongIntoLong(wcn),
					Util.stuffIntIntoShort(rn));

			s = new SecretKeySpec(sharedSecret, 0, 32, "AES");
			c = Cipher.getInstance("AES/ECB/NoPadding");
			c.init(Cipher.ENCRYPT_MODE, s);

			byte[] prern = nonce;

			prern = Util.concatenate(prern, trn);
			prern = Util.concatenate(prern, Util.stuffIntIntoShort(partn));

			cr = Util.concatenate(cr, c.doFinal(prern));
			
		} catch (NoSuchAlgorithmException e) {
			e.toString();
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.toString();
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.toString();
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.toString();
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.toString();
			e.printStackTrace();
		}

		return cr;
	}
	
	private synchronized byte[] calcKeysAESPRNGCaller(final long wcn, final int rn,
			ParticipantMgmntInfo pmi,final int length) {
		
		byte [] cr = new byte[0];
		
		// How much key material do we have to "produce" (One AES-Block is 16-Bytes)
		int prn = length / 16;
		int rrn = length % 16;

		if (rrn > 0)
			prn++;
		
		if (length%4 != 0){
			log.error("Desired key length is not dividable by four");
		}
		
		for (int i = 0; i<prn; i++) {
			cr = Util.concatenate(cr, calcKeysAESPRNG(wcn, rn, Util.getBytesByOffset(pmi.getKey()
							.getCalculatedSecret().toByteArray(), 0, 32), Util.getBytesByOffset(pmi.getKey()
							.getCalculatedSecret().toByteArray(), 32, 4), i));
		}
		
		cr = Util.getFirstBytes(cr, length);
		
		return cr;
	}

	private synchronized byte[] calcKeysSigmaCaller(final long wcn, final int rn,
			ParticipantMgmntInfo pmi,final int length){
		
		BigInteger 	b_ij 		= null;
		byte[] 		cr 			= new byte[0];
		BigInteger  mod			= BigInteger.valueOf(MODULUS);
		byte[] 		oldmesages 	= assocWorkCycle.getMessageBin();
		BigInteger	sigma		= null;
		BigInteger	vt			= null;
		int 		vtoffset 	= 0;
		final byte[]zero		= new byte[4];
				
		for (int j = 0; j < (length / 4); j++) {
			
			sigma		= BigInteger.ZERO;
			
			for (int i = 0; i < (oldmesages.length / 4); i++) {

				b_ij = new BigInteger(
						calcKeysAESPRNG(workcycleNumber, currentRound,
								Util.getBytesByOffset(pmi.getKey()
										.getCalculatedSecret().toByteArray(),
										0, 32), Util.getBytesByOffset(pmi
										.getKey().getCalculatedSecret()
										.toByteArray(), 36, 4), i));

				vtoffset = oldmesages.length - ((i * 4) + 4);

				vt = new BigInteger(Util.getBytesByOffset(oldmesages, vtoffset,
						4));

				b_ij = b_ij.multiply(vt);
				b_ij = b_ij.mod(mod);

				sigma.add(b_ij);
				sigma.mod(mod);
			}
			
			// Because we need longer keys than 4 byte, we assume
			// each previous 4 byte message as passed with content 0
			cr = Util.concatenate(cr, fillAndMergeSending(zero, sigma.toByteArray()));
			oldmesages = Util.concatenate(oldmesages, zero);
			
		}
		return cr;
	}
	
	/**
	 * Merge two unequally sized byte arrays (by xoring each element). The
	 * shorter one will be padded up to the length of the longer one with 0s.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] fillAndMergeSending(byte[] a, byte[] b) {

		if (a == null | b == null)
			throw new IllegalArgumentException("The arrays a, b are null.");

		int i;
		byte[] shorterone = null;
		byte[] longerone = null;

		if (a.length <= b.length) {
			shorterone = a;
			longerone = b;
		} else {
			shorterone = b;
			longerone = a;
		}

		byte[] c = new byte[longerone.length];

		for (i = 0; i < shorterone.length; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}

		while (i < longerone.length) {
			c[i] = longerone[i];
			i++;
		}

		return c;
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
			notifyObservers(WorkCycle.WC_SENDING_FINISHED);
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

	private byte[] inverseKey(byte[] keyToInverse){
		
		byte[] c = new byte[keyToInverse.length];

		for (int i = 0; i< keyToInverse.length; i = i+4){
			long d = (WorkCycleSending.MODULUS - Util
					.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(
							keyToInverse, i, 4)))
					% WorkCycleSending.MODULUS;
			if (d < 0) d = d + WorkCycleSending.MODULUS;
			c[i]   = (byte) (d >>> 24);
			c[i+1] = (byte) (d >>> 16);
			c[i+2] = (byte) (d >>> 8 );
			c[i+3] = (byte) d;
		}
		
		return c;
	}

	public boolean isServerMode() {
		return assocWorkCycle.getAssocWorkCycleManager().isServerMode();
	}

	/**
	 * Merge two equally sized byte arrays (by xoring each element).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] mergeDCwise(byte[] a, byte[] b) {

		if (a == null | b == null | a.length != b.length)
			throw new IllegalArgumentException(
					"The arrays a, b are null or do not have the same length.");

		byte[] c = new byte[a.length];

		for (int i = 0; i< a.length; i = i+4){
			
			long ka = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(a,i, 4)) % WorkCycleSending.MODULUS;
			long kb = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(b,i, 4)) % WorkCycleSending.MODULUS;
			
			long d = (ka + kb) % WorkCycleSending.MODULUS;
			
			c[i]   = (byte) (d >>> 24);
			c[i+1] = (byte) (d >>> 16);
			c[i+2] = (byte) (d >>> 8 );
			c[i+3] = (byte) d;
		}
		
		return c;

	}

	/**
	 * The rounds of a DC-Network are deterministic. What we can do is
	 * prepare all messages and then fire them as fast as possible to the
	 * server, hoping it can handle them.
	 */
	public void performDCRoundsParticipantSide() {
		ManagementMessageAdd m = null;
		while (!finished) {
			if (currentRound == relativeRound) {
				byte[] p = WorkCycleSending.mergeDCwise(payloadSend,
						calcKeysMain(systemPayloadLength));
				m = new ManagementMessageAdd(workcycleNumber, currentRound, p);
			} else {
				byte[] p = calcKeysMain(systemPayloadLength);
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
			if(method == WorkCycleManager.METHOD_DCPLUS && finished != true)
				assocWorkCycle.getSemaphore().acquire();
			}
			catch (InterruptedException e){
				log.error(e.toString());
			}
		}
		
		setChanged();
		notifyObservers(WorkCycle.WC_SENDING_FINISHED);		
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
