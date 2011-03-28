package de.tu.dresden.dud.dc.KeyGenerators;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import de.tu.dresden.dud.dc.ParticipantMgmntInfo;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleSending;

public class KeyGeneratorFailStopWorkCycle extends KeyGeneratorNormalDC {

	WorkCycle assocWorkCycle;
	
	public KeyGeneratorFailStopWorkCycle(WorkCycleManager wcm) {
		super(wcm);
		actualKeyGeneratingMethod = KeyGenerator.METHOD_DC_FAIL_STOP_WORK_CYCLE;
		
		assocWorkCycle = wcm.getCurrentWorkCycle();
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
	protected synchronized byte[] calcKeysMain(int length) {

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
				cr = Util.mergeDCwise(
						cr,
						calcKeysSigmaCaller(workcycleNumber, currentRound, pmi,
								length), WorkCycleSending.MODULUS);

			// Finally calculate the inverse, when needed.
			if (pmi.getKey().getInverse()) {
				cr = inverseKey(cr);
			}
			bl.add(cr);
		}

		for (byte[] w : bl) {
			b = Util.mergeDCwise(b, w, WorkCycleSending.MODULUS);
		}

		return b;
	}
	
	private synchronized byte[] calcKeysSigmaCaller(final long wcn, final int rn,
			ParticipantMgmntInfo pmi,final int length){
		
		BigInteger 	b_ij 		= null;
		byte[] 		cr 			= new byte[0];
		BigInteger  mod			= BigInteger.valueOf(WorkCycleSending.MODULUS);
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

				sigma = sigma.add(b_ij);
				sigma = sigma.mod(mod);
			}
			
			// Because we need longer keys than 4 byte, we assume
			// each previous 4 byte message as passed with content 0
			cr = Util.concatenate(cr, Util.fillAndMergeSending(zero, Util.getLastBytes(sigma.toByteArray(), 4)));
			oldmesages = Util.concatenate(oldmesages, zero);
			
		}
		return cr;
	}
	

}
