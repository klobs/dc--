/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.LinkedList;


/**
 * The Key Manager handles almost all the crypto stuff.
 * 
 * It generates the Diffie-Hellman key parts, and verifies DSA signatures.
 * 
 * 
 * Diffie-Hellman needs a generator g and a prime number p. Both are considered as public,
 * and fixed for this DC protocol. They are taken out of RFC 5114.
 * 
 * Furthermore each exchanging party needs a secret (a for participant p1 and b for participant p2).
 * 
 * p1 calculates A = g^a mod p; and sends A,g,p to p2.
 * p2 calculates B = g^b mod p; and sends B to p2.
 * 
 * The key K = B^a mod p = A^b mod p;
 * 
 * In this implementation "a" is called "dhPrivatePart"
 * 
 * @author klobs
 *
 */
public class KeyManager {

	// p and g taken from http://www.rfc-archive.org/getrfc.php?rfc=5114
	public static final String P = new String
					("87A8E61DB4B6663CFFBBD19C651959998CEEF608660DD0F2"
					+ "5D2CEED4435E3B00E00DF8F1D61957D4FAF7DF4561B2AA30"
					+ "16C3D91134096FAA3BF4296D830E9A7C209E0C6497517ABD"
					+ "5A8A9D306BCF67ED91F9E6725B4758C022E0B1EF4275BF7B"
					+ "6C5BFC11D45F9088B941F54EB1E59BB8BC39A0BF12307F5C"
					+ "4FDB70C581B23F76B63ACAE1CAA6B7902D52526735488A0E"
					+ "F13C6D9A51BFA4AB3AD8347796524D8EF6A167B5A41825D9"
					+ "67E144E5140564251CCACB83E6B486F6B3CA3F7971506026"
					+ "C0B857F689962856DED4010ABD0BE621C3A3960A54E710C3"
					+ "75F26375D7014103A4B54330C198AF126116D2276E11715F"
					+ "693877FAD7EF09CADB094AE91E1A1597");
	
	public static final String G = new String
					("3FB32C9B73134D0B2E77506660EDBD484CA7B18F21EF2054"
					+ "07F4793A1A0BA12510DBC15077BE463FFF4FED4AAC0BB555"
					+ "BE3A6C1B0C6B47B1BC3773BF7E8C6F62901228F8C28CBB18"
					+ "A55AE31341000A650196F931C77A57F2DDF463E5E9EC144B"
					+ "777DE62AAAB8A8628AC376D282D6ED3864E67982428EBC83"
					+ "1D14348F6F2F9193B5045AF2767164E1DFC967C1FB3F2E55"
					+ "A4BD1BFFE83B9C80D052B985D182EA0ADB2A3B7313D3FE14"
					+ "C8484B1E052588B9B7D2BBD2DF016199ECD06E1557CD0915"
					+ "B3353BBB64E0EC377FD028370DF92B52C7891428CDC67EB6"
					+ "184B523D1DB246C32F63078490F00EF8D647D148D4795451"
					+ "5E2327CFEF98C582664B4C0F6CC41659");
	
	
	private BigInteger	dhPublicPart = null;
	private BigInteger  dhPrivatePart = null;
	private Signature 	dsa   	= null;
	private KeyPair 	keyPair = null;
	private LinkedList<String> unfinishedKeyExReqs = new LinkedList<String>();
	
	
	public KeyManager(){
		try {
			
			// Generate DSA public/private key-pair
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
			SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG", "SUN");
		
			keyGen.initialize(1024, r1);
		
			dsa = Signature.getInstance("SHA1withDSA", "SUN"); 
			keyPair = keyGen.generateKeyPair();
			
			dsa.initSign(keyPair.getPrivate());
			
			// Call DH-Private part generation:
			generateDHPrivatePart();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}	
	}
	
	public KeyManager(byte[] dsaPublicKey, byte[] dsaPrivateKey){
        try {
        	// setup the DSA Part
        	// look at http://download.oracle.com/javase/tutorial/security/apisign/vstep2.html for an explanation
        	X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(dsaPublicKey);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");

	        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
			

        	X509EncodedKeySpec privKeySpec = new X509EncodedKeySpec(dsaPublicKey);
	
	        PrivateKey privKey = keyFactory.generatePrivate(privKeySpec);

	        keyPair = new KeyPair(pubKey, privKey);
	        
        } catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		// generate DH-Public part
		generateDHPrivatePart();
	}

	/**
	 * This method keeps helping track of unfinished key exchanges. It adds a
	 * participant's id to a list in case this participant is unknown during the
	 * key exchange request.
	 * 
	 * As soon as a new {@link InfoServiceInfoActiveParticipantList} or
	 * {@link InfoServiceInfoPassiveParticipantList} arrives,
	 * finishKeyExchangeRequests should be called.
	 * 
	 * @param id
	 *            The id of the unknown participant.
	 */
	public synchronized void addUnfinishedKeyExchangeRequest(String id){
		if (! unfinishedKeyExReqs.contains(id)) unfinishedKeyExReqs.add(id);
	}
	
	/**
	 * This function does the DH-Key / El-Gamal Key-Calculation.
	 * 
	 * @param The other participants to exchange keys with.
	 * @param pm The participant manager that contains all the Participants
	 */
	public void activateKeyExchangeBetween(String p1, ParticipantManager pm){
		BigInteger 			 calculatedSecret 	= null;
		ParticipantMgmntInfo pmi 				= null;
		BigInteger 			 remoteDHPublicPart = null;
		
		if (p1 == null || pm == null){
			Log.print(Log.LOG_WARN, "Key commit arguments can not be null. Aborting Key Commitment.", this);
			return;
		}
		
		pmi = pm.getParticipantMgmntInfoByParticipantID(p1);
		
		if (p1.equals(pm.getMe().getId())) {
			Log.print(Log.LOG_WARN, "No need to exchange keys with myself. Aborting Key Commitment.", this);
			return;
		}
		
		if(pmi == null) {
			Log.print(Log.LOG_WARN, "Unable to find ParticipantManagementInfo for " + p1 + ". Aborting Key Commitment.", this);
			return;
		}
		
		if (pmi.getKey().getState() != DCKey.KEY_REQUESTED) {
			Log.print(Log.LOG_WARN, "Key of " + p1 + " is not in exchanged state. Aborting Key Commitment.", this);
			return;
		}
		
		remoteDHPublicPart = new BigInteger(pmi.getParticipant().getDHPublicPart());
		
		calculatedSecret = remoteDHPublicPart.modPow(dhPrivatePart, new BigInteger(P, 16));
		
		pmi.getKey().setCalculatedSecret(calculatedSecret);
	}

	/**
	 * If the participant <i>A</i> doesn't know the key exchange requesting
	 * other participant <i>B</i>, <i>A</i> stores <i>B</i>'s id in a list
	 * (managed by the KeyManager). In case <i>A</i> receives a new
	 * {@link InfoServiceInfoPassiveParticipantList} or an
	 * {@link InfoServiceInfoActiveParticipantList}, it shall call this method
	 * to try finishing the unfinished key requests.
	 * 
	 * TODO: The current procedure hopes that the requested <i>id</i>s in the
	 * list will show up after a while. If they do not, there will be an
	 * infinite loop, requesting for the participant id.
	 * 
	 * @param c
	 *            the connection on which the other participant is expected.
	 */
	public synchronized void finishUnfinishedKeyExchReqs(Connection c){
	
		while(unfinishedKeyExReqs.size() > 0){
			String id = unfinishedKeyExReqs.removeFirst();
			unfinishedKeyExReqs.remove(id);
			c.requestKeyExchange(id);
		}
	}
	
	private void generateDHPrivatePart(){
		// Generate random for session keys:
		SecureRandom 	r 	= new SecureRandom();
		byte[] 			a 	= new byte[4];
		
		r.nextBytes(a);
		
		BigInteger generator = new BigInteger(G, 16);
		
		dhPrivatePart = new BigInteger(a);
		
		dhPublicPart = generator.modPow(dhPrivatePart, new BigInteger(P, 16));
	}
	
	public byte[] getDHPublicPart(){
		if (dhPublicPart == null) return null;
		return dhPublicPart.toByteArray();
	}
    
	public byte[] getDHPublicPartSignature(){
		if (dhPublicPart == null) return null;

		return sign(getDHPublicPart());
	}
	
    public PublicKey getDSAPublicKey(){
		return keyPair.getPublic();
	}

	public KeyPair getDSAKeypair(){
		return keyPair;
	}
	
	public String getDSAPublicKeyID(){
		if (keyPair == null) return null;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance( "SHA1" );
			md.update(keyPair.getPublic().getEncoded());

			byte[] digest = md.digest();
			
			return Util.convertToHex(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public byte[] sign(byte[] m){
		if(dsa == null) return null;
		try {
			dsa.update(m);
			return dsa.sign();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean verifyKey(Participant p){
		try {
		
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(
					p.getDSAPublicSignature());
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");

	        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

	        Signature sig = Signature.getInstance("SHA1withDSA", "SUN");

	        sig.initVerify(pubKey);

	        sig.update(p.getDHPublicPart());
	        
	        return sig.verify(p.getDHPublicPartSignature());
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
