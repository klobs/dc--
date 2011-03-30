package de.tu.dresden.dud.dc.KeyGenerators;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.ParticipantMgmntInfo;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleSending;

/**
 *  K_{ij} = a_{ij}^t + b_{ij}^t * K_{ij}^{t-1} + e * I_{i}^{t-1}
 * @author klobs
 *
 */
public class KeyGeneratorProbabFailStop extends KeyGeneratorNormalDC {

	// Logging
	private static Logger log = Logger.getLogger(KeyGeneratorFailStopWorkCycle.class);
	
	private WorkCycle assocWorkCycle = null;
	private HashMap<ParticipantMgmntInfo, byte[]> lastKeys = new HashMap<ParticipantMgmntInfo, byte[]>();
	
	public KeyGeneratorProbabFailStop(WorkCycleManager wcm) {
		super(wcm);
		actualKeyGeneratingMethod = KeyGenerator.KGMETHOD_PROBAB_FAIL_STOP;
		
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
	@Override
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

			if(lastKeys.get(pmi) != null){
				cr = Util.mergeDCwise(
						cr,
						calcKeysBPartCaller(workcycleNumber, currentRound, pmi,
								length), WorkCycleSending.MODULUS);
				
				cr = Util.mergeDCwise(cr, calcKeysEPart(pmi, length),
						WorkCycleSending.MODULUS);
			}
			
			// Finally calculate the inverse, when needed.
			if (pmi.getKey().getInverse()) {
				cr = inverseKey(cr);
			}
		
			lastKeys.put(pmi, cr);
			
			bl.add(cr);
		}

		for (byte[] w : bl) {
			b = Util.mergeDCwise(b, w, WorkCycleSending.MODULUS);
		}

		return b;
	}

	private byte[] calcKeysBPartCaller(long workcycleNumber, int currentRound,
			ParticipantMgmntInfo pmi, int length) {
		
		BigInteger 	b_ij 		= null;
		byte[] 		cr 			= new byte[0];
				
		int prn = length / 16;
		int rrn = length % 16;

		if (rrn > 0)
			prn++;
		
		for (int i = 0; i<prn; i++) {
			cr = Util.concatenate(cr, calcKeysAESPRNG(workcycleNumber, currentRound, Util.getBytesByOffset(pmi.getKey()
							.getCalculatedSecret().toByteArray(), 0, 32), Util.getBytesByOffset(pmi.getKey()
							.getCalculatedSecret().toByteArray(), 36, 4), i));
		}
		
		b_ij = new BigInteger(cr);

		b_ij = b_ij.multiply(new BigInteger(lastKeys.get(pmi)));
		
		cr = b_ij.toByteArray();
	
		if (cr.length > length)
			cr = Util.getLastBytes(cr, length);
		
		if (cr.length < length)
			cr = Util.fillAndMergeSending(cr, new byte[length]);
		
		return cr;	
	}
	
	private byte[] calcKeysEPart(ParticipantMgmntInfo pmi, int length){
		byte[] cr 			= null;
		byte[] oldMessages 	= assocWorkCycle.getMessageBin();
		
		BigInteger e = new BigInteger(Util.getBytesByOffset(pmi.getKey()
				.getCalculatedSecret().toByteArray(), 40, 40));
		
		if(oldMessages.length != lastKeys.get(pmi).length)
			log.warn("Wrong old message length!");
		
		BigInteger i_tminus1 = new BigInteger(Util.getLastBytes(oldMessages,
				lastKeys.get(pmi).length));
		
		e.multiply(i_tminus1);

		cr = e.toByteArray();
		
		if (cr.length > length)
			cr = Util.getLastBytes(cr, length);
		
		if (cr.length < length)
			cr = Util.fillAndMergeSending(cr, new byte[length]);
		
		return cr;
	}
	
}
