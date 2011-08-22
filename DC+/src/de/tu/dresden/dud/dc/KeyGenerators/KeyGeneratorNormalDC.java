package de.tu.dresden.dud.dc.KeyGenerators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.params.KeyParameter;

import de.tu.dresden.dud.dc.ParticipantMgmntInfo;
import de.tu.dresden.dud.dc.Util;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleSending;

/**
 * K_{ij}^t = a_{ij}^t
 * @author klobs
 *
 */
public class KeyGeneratorNormalDC extends KeyGenerator {


	// Logging
	private static Logger log = Logger.getLogger(KeyGeneratorNormalDC.class);
	
	protected long workcycleNumber;
	protected int  currentRound;
	
	public KeyGeneratorNormalDC(WorkCycleManager wcm) {
		super(wcm);

		actualKeyGeneratingMethod = KeyGenerator.KGMETHOD_DC;
	}

	@Override
	public byte[] calcKeys(final int length, final long wcn, final int rn) {
		workcycleNumber = wcn;
		currentRound = rn;
		
		return calcKeysMain(length);
	}

	protected synchronized byte[] calcKeysAESPRNG(final long wcn, final int rn,
			final byte[] sharedSecret, final byte[] nonce, final int partn) {
		BufferedBlockCipher cipher = null;
		BlockCipher engine = new AESEngine();
		byte[] cr = new byte[16];

		byte[] trn = Util.concatenate(Util.stuffLongIntoLong(wcn),
				Util.stuffIntIntoShort(rn));

		cipher = new BufferedBlockCipher(engine);
		cipher.init(true, new KeyParameter(sharedSecret));

		byte[] prern = nonce;

		prern = Util.concatenate(prern, trn);
		prern = Util.concatenate(prern, Util.stuffIntIntoShort(partn));

		try {
			cipher.doFinal(cr, cipher.processBytes(prern, 0, prern.length, cr, 0));
		} catch (DataLengthException e) {
			e.toString();
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.toString();
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			e.toString();
			e.printStackTrace();
		}

		return cr;
	}
	
	protected synchronized byte[] calcKeysAESPRNGCaller(final long wcn, final int rn,
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
	
	protected WorkCycle getCurrentWorkCycle(){
		return assocWorkCycleManag.getCurrentWorkCycle();
	}
	
	protected byte[] inverseKey(byte[] keyToInverse){
		
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

}
