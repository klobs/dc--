/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.InfoService.InfoService;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessage;


/**
 * This class is the Participant with all the needed functionalities.
 * @author klobs
 */
public class Participant extends Observable implements Observer {

	// Logging
	private static Logger log = Logger.getLogger(Participant.class);

	private ArrayList<Connection> 	connections = new ArrayList<Connection>();
	private InfoService				infoService	= null;
	private boolean					isStopped	= false;
	private KeyManager				keyManager	= null;
	private boolean					manualSetup = false;
	
	private byte[]					dh			= null;
	private byte[]					dhs			= null;
	private String					id			= new String();
	private byte[]					signature	= null;
	private String					username	= new String();
	
	/**
	 * Constructor for a new participant.
	 * Use this constructor, if id, username and signature are already known.
	 * 
	 * @param id participant's ID
	 * @param u  username
	 * @param s  signature
	 * @param dh Diffie Hellman public part
	 * @param dhs Diffie Hellman signed
	 */
	public Participant(String id, String u, byte[] s, byte[] dh, byte[] dhs)
	{
		this.dh			= dh;
		this.dhs		= dhs;
		this.id 		= id;
		this.username 	= u;
		this.signature 	= s;	
	}
	
	/**
	 * Constructor for a new participant.
	 * Use this constructor, if a new DSA Keypair have to be generated.
	 * 
	 * @param u
	 */
	public Participant(String u){
		this.username 	= u;
		
		keyManager = new KeyManager();
		this.signature =  keyManager.getDSAPublicKey().getEncoded();
		this.id		   =  keyManager.getDSAPublicKeyID();
		this.dh		   =  keyManager.getDHPublicPart();
		this.dhs 	   =  keyManager.getDHPublicPartSignature();
	}
	
	public Connection doAllTheThingsToBecomeActive(String hostname, int port){
		Connection c = establishNewConnection(hostname, port);
		registerAtService(c);
		
		return c;
	}
	
	/**
	 * compares 2 participants, by comparing id, uname and sig.
	 * @param p
	 * @return whether compared parameters are equal
	 */
	public boolean equalsTo(Participant p){
		if (this.id.equals(p.getId()) && this.username.equals(p.getUsername())
				&& Arrays.equals(this.signature,p.getDSAPublicSignature()))
			return true;
		return false;
	}
	
	/**
	 * Establish a new Connection to the given host and specified port.
	 * @param host
	 * @param port
	 * @return
	 */
	public Connection establishNewConnection(String host, int port){
		try{
			Socket e = new Socket(host, port);
		
			Connection c = new Connection(null, e, this);
			c.setParticipant(this);
			c.setAssocKeyManager(keyManager);
			c.addObserver(this);
			connections.add(c);
			
			new Thread(c, "connectionParticipant_"+ id).start();
			
			infoService = new InfoService();

			return c;
			
		} catch( IOException e){
			log.error("Problems when establishing connection");
			return new Connection(null,null, this);
		}
	}
	
	public void exchangeKeyWith(String id, Connection c){
		c.requestKeyExchange(id);
	}
	
	/**
	 * @return ArrayList with all Connections owned by the participant.
	 */
	public ArrayList<Connection> getConnections(){
		return connections;
	}
	
	public byte[] getDHPublicPart(){
		return dh;
	}
	
	public byte[] getDHPublicPartSignature(){
		return dhs;
	}
	
	public byte[] getDSAPublicSignature() {
		return signature;
	}
	
	public String getId() {
		return id;
	}
	
	public InfoService getInfoService(){
		return infoService;
	}
	
	public KeyPair getKeyPair(){
		if (keyManager == null) return null;
		return keyManager.getDSAKeypair();
	}

	public boolean getManualSetup(){
		return manualSetup;
	}
	
	public String getUsername() {
		return username;
	}

	public boolean isStopped() {
		return isStopped;
	}
	
	public void joinWorkCycle(Connection c){
		if(connections.contains(c))
			c.joinWorkCycle(this);
	}
		
	/**
	 * Tells the participant to join the service with a specific connection.
	 * Only works for own connections. (i.e. owned by the participant instance)
	 * @param c
	 */
	public void registerAtService(Connection c){
		if(connections.contains(c))
			c.registerAtService(this);
	}
			
	public void leaveWorkCycle(Connection c){
		if(connections.contains(c))
			c.leaveWorkCycle(this);
	}
	
	public void quitService(Connection c){
		if(connections.contains(c))
			c.quitService(this);
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String toString(){
		return "part:id=" + id;
	}
	
    public void update(Observable o, Object a){
    	
    	// hand on all management messages to our observers
    	// this is needed for the gui
    	if (o instanceof Connection && a instanceof ManagementMessage){
    		setChanged();
    		notifyObservers(a);
    	}
    }
}
