/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Observable;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoActiveParticipantList;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoKeyExchangeCommit;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoReqActiveParticipantList;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoReqPassiveParticipantList;
import de.tu.dresden.dud.dc.InfoService.InfoServiceInfoRequestKeyExchange;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessage;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAccepted4Service;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdd;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfo;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfoRequest;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageJoinWorkCycle;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageLeaveWorkCycle;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageRegisterAtService;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageTick;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageWelcome2Service;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageWelcome2WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

/**
 * One of the most important classes in this implementation. Most of the actions
 * that belong to the actual communication are being performed here. I.e.
 * message forming and handling, etc.
 * 
 * Each DC/DC+ communication relays at least on one connection.
 * 
 * On the server side a connection for each new participant is established.
 * 
 * For the time being, on the participant side there is only one connection
 * possible at a time.
 * 
 * 
 * @author klobs
 */
public class Connection extends Observable implements Runnable {

	// Logging
	Logger log = Logger.getLogger(Connection.class);

	/**
	 *  Default port
	 */
	public static final int DEFAULTPORT = 6867;

	// available modes for the connection.
	
	/**
	 * Status indicator for connections. This connection is broken and can no
	 * longer be used.
	 */
	public static final int MODE_BROKEN = -1000;

	/**
	 * This connection is welcome to the service. It may now register at the service.
	 */
	public static final int MODE_WELCOME2SERVICE = 1;

	/**
	 * Status indicator for connections.
	 */
	public static final int MODE_REGISTERATSERVICE = 2;

	/**
	 * Status indicator for connections. This connection has fully registered at the
	 * service. It may use info services and also request to join work cycles, to
	 * actively take part in them.
	 */
	public static final int MODE_PASSIVE = 3;

	/**
	 * Status indicator for connections. This connection actively takes part in
	 * work cycles.
	 */
	public static final int MODE_ACTIVE = 5;
	
	// Sockets and their endpoints
	protected Socket 		clientSocket 	= null;
	protected Server 		server 			= null;
	protected ServerSocket 	serverSocket 	= null;
	protected boolean 		stopped 		= true;

	// other internal variables
	protected KeyManager	assocKeyManager = null;
	public	  Participant 	assocParticipant;
	protected WorkCycleManager 	assocWorkCycleManager;
	protected ParticipantManager assocParticipantManager;
	protected int 			errorCount 			= 0;
	protected long 			exptectedEntryWC = -1; // TODO: Will have to fix the singed /
													 // unsigned problem with java.
													 // Long.MIN_VALUE;
	protected long 			expectedLeavingWC = -1;
	private   LinkedList<String> sendMessageBuffer = new LinkedList<String>(); // we use this if no work cycle manager is active yet.
	protected boolean 		servermode; 		// we should know whether this connection is
												// created on serverside, or not

	// timestamps for statistics
	protected long 			firstError 		= 0; // If we have a certain mount of
												 // errors, let's shut down the
												 // connection.

	// Channels
	protected DataInputStream 	input 	= null;
	protected DataOutputStream 	output 	= null;

	// Current mode
	protected int 				currentMode = 0;

	// Message store
	protected ManagementMessageAccepted4Service acceptedForService;
	protected ManagementMessageRegisterAtService 		regsiterAtService;
	protected ManagementMessageJoinWorkCycle 	joinWorkCycle;
	protected ManagementMessageLeaveWorkCycle	leaveWorkCycle;
	protected ManagementMessageAdd				lastAdd;
	protected ManagementMessageAdded			lastAdded;
	protected ManagementMessage 				lastMessage;
	protected ManagementMessageTick 			lastTick;
	protected ManagementMessageWelcome2Service 	welcome2Service;
	protected ManagementMessageWelcome2WorkCycle 	welcome2WorkCycle;

	// identifier (will be set up in the toSting method)
	protected String ident = null;

	/**
	 * Constructor
	 * 
	 * @param server
	 *            The server that the connection belongs to. This can be null
	 *            (in case a participant creates a new connection)
	 * @param clientSocket
	 *            The established socket, with the client waiting on the other
	 *            site.
	 * @param creator
	 *            the object which is creating the connection (needed for
	 *            logging)
	 */
	public Connection(Server server, Socket clientSocket, Object creator) {
		this.server = server;
		this.clientSocket = clientSocket;

		if (server == null)
			servermode = false;
		else
			servermode = true;

		ident = creator.toString();

		if (clientSocket != null)
			stopped = false;

		currentMode = MODE_BROKEN;
	}

	/**
	 * This method will be called on server side (normally by a server object).
	 * It checks the current status of the connection (to verify whether the state transition is allowed),
	 * crafts a ManagementMessage and transfers it to the participant.
	 * 
	 * This method does not check the value of acceptReject.
	 * @param acceptReject Integer, that indicates whether the participant is accepted, or not. No semantic checking is done here.
	 */
	public void accept4Service(int acceptReject) {
		if (!(currentMode == MODE_WELCOME2SERVICE)) {
			log.warn("Bad transition. Not in WELCOME2SERVICE status, to send ACCEPTED4SERVICE message");
			return;
		}

		try {
			ManagementMessageAccepted4Service m = new ManagementMessageAccepted4Service(
					acceptReject);
			this.sendMessage(m.getMessage());
			currentMode = MODE_PASSIVE;

		} catch (IOException e) {
			log.error("Problems occured with the ACCEPTED4SERVICE message: " + e.toString());
		}
	}
	
	
	/**
	 * Analyses an {@link InfoServiceInfoKeyExchangeCommit} and activates the
	 * keys between this participant and the other one mentioned in i.
	 * Activation is being done in the {@link KeyManager}.
	 * 
	 * @param i
	 *            the {@link InfoServiceInfoKeyExchangeCommit} which is received
	 *            and which contains the two {@link Participant}s of a key
	 *            exchange.
	 */
	public void commitKeyExchange(InfoServiceInfoKeyExchangeCommit i){
		if (i.getP1().equals(assocParticipant.getId()))
			assocKeyManager.activateKeyExchangeBetween(i.getP2(), true,
					assocParticipantManager);
		if (i.getP2().equals(assocParticipant.getId()))
			assocKeyManager.activateKeyExchangeBetween(i.getP1(), false,
					assocParticipantManager);
	}

	/**
	 * Each {@link Participant} can decide at any time to send DC/DC+ messages
	 * over a communication, whether the {@link WorkCycle}s are already running, or
	 * not.
	 * 
	 * This method takes those messages and if the work cycles are already running,
	 * it passes them directly to the {@link WorkCycleManager}, or - if not - it
	 * buffers them until the {@link WorkCycleManager} starts up and fetches them.
	 * 
	 * @param s the plain text message that you want to transmit over the connection.
	 */
	public void feedWorkCycleManager(String s) {
		if(assocWorkCycleManager != null)
			assocWorkCycleManager.addMessage(Util
					.stuffStringIntoCharArray(s));
		else sendMessageBuffer.add(s);
	}

	/**
	 * An {@link InfoServiceInfoActiveParticipantList} should call this method
	 * on arrival, to remember this connection to finish unfinished key exchange
	 * requests.
	 * 
	 * This method invokes the {@link KeyManager}'s
	 * finishUnfinishedKeyExchReqs().
	 */
	public synchronized void finishUnfinishedKeyExchReqs(){
		assocKeyManager.finishUnfinishedKeyExchReqs(this);
	}
	
	/**
	 * Standard getter.
	 * 
	 * Each Connection must have an associated {@link Participant}. This
	 * participant is the participant waiting on the one side of the connection.
	 * 
	 * The associated participant is being set in the corresponding setter
	 * method.
	 * 
	 * @return The associated participant
	 */
	public Participant getAssociatedParticipant() {
		return this.assocParticipant;
	}
	
	/**
	 * Standard getter.
	 * @return The associated {@link ParticipantManager}.
	 */
	public ParticipantManager getAssociatedParticipantManager(){
		if (assocParticipantManager == null){
			assocParticipantManager = new ParticipantManager();
			assocParticipantManager.addParticipant(assocParticipant);
			assocParticipantManager.setMe(assocParticipant);
			assocParticipantManager
					.getParticipantMgmntInfoFor(assocParticipant)
					.setAssocConnection(this);
		}
		return this.assocParticipantManager;
	}

	/**
	 * Standard getter.
	 * @return The associated {@link WorkCycleManager}.
	 */
	public WorkCycleManager getAssociatedWorkCycleManager(){
		return this.assocWorkCycleManager;
	}
	
	/**
	 * Standard getter.
	 * @return the associated Server 
	 */
	public Server getAssociatedServer(){
		return this.server;
	}
	
	/**
	 * Getter for the client socket of the connection.
	 */
	public Socket getClientSocket() {
		return clientSocket;
	}
	
	
	/**
	 * @return when a connection is expected to take part in a work cycle.
	 */
	public long getExpectedEntryWorkCycle() {
		return exptectedEntryWC;
	}
	
	public long getExpectedLeavingWorkCycle(){
		return expectedLeavingWC;
	}
	
	/**
	 * Return the current mode of the connection.
	 * 
	 * @return the current mode / status of the connection.
	 */
	public int getMode() {
		return this.currentMode;
	}

	/**
	 * Getter for the server socket of the connection (, if has any. Only server
	 * side connections have server sockets)
	 * 
	 * @return the serverSocket.
	 */
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	/**
	 * A connection can be in server mode, or not. A connection being in server
	 * mode, has an associated {@link Server}. Normally only connections on the
	 * server side are in server mode.
	 * 
	 * A connection can automatically determine, whether it is in server mode,
	 * or not.
	 * 
	 * @return whether the connection is in server mode, or not.
	 */
	public boolean isServerMode(){
		return servermode;
	}

	/**
	 * Each {@link Connection} runs as a thread. The run() method runs in a
	 * loop, to wait for arriving data. With each beginning of the loop, run()
	 * checks, whether the connection shall run another time or not.
	 * 
	 * Therefore it calls this method.
	 * 
	 * @return true, or false - it is a boolean ;)
	 */
	public boolean isStopped() {
		return this.stopped;
	}

	
	/**
	 * A {@link Participant} calls this method on a passive connection, to join
	 * a work cycle. The method checks the state of the connection and decides
	 * whether the desired transition is allowed. It then crafts the
	 * corresponding {@link ManagementMessage} ({@link ManagementMessageJoinWorkCycle}) and sends it to the
	 * {@link Server}.
	 * 
	 * The {@link Server} then replies with corresponding
	 * {@link ManagementMessage}.
	 * 
	 * @param p
	 *            The participant that requests to join a work cycle. (Usually it is
	 *            the same as the assocParticipant of the connection/ or this).
	 */
	public void joinWorkCycle(Participant p) {
		if (!(assocParticipantManager.getParticipantMgmntInfoFor(p).isPassive() 
				&& !assocParticipantManager.getParticipantMgmntInfoFor(p).isActive())) {
			log.warn("Bad transition. Not in PASSIVE status, to send JOINWORKCYLE message");
			return;
		}

		try {
			ManagementMessageJoinWorkCycle m = new ManagementMessageJoinWorkCycle();
			this.sendMessage(m.getMessage());
		} catch (IOException e) {
			log.error("Problems occured within the joinWorkCycle method: " + e.toString());
		}
	}

	/**
	 * A client can call this method to send a
	 * {@link ManagementMessageRegisterAtService} {@link ManagementMessage} to the
	 * server. The method checks the state of the connection and decides whether
	 * the desired transition is allowed. It then crafts the corresponding
	 * {@link ManagementMessage} ({@link ManagementMessageRegisterAtService}) and sends
	 * it to the {@link Server}.
	 * 
	 * @param p
	 */
	public void registerAtService(Participant p) {
		if (!(currentMode == MODE_REGISTERATSERVICE)) {
			log.warn("Bad transition. Not in broken status, to send REGISTER AT SERVICE message");
			return;
		}

		try {
			ManagementMessageRegisterAtService m = new ManagementMessageRegisterAtService(p
					.getId(), p.getUsername(), p.getDSAPublicSignature(), p
					.getDHPublicPart(), p.getDHPublicPartSignature());
			this.sendMessage(m.getMessage());
		} catch (IOException e) {
			log.error("Problems occured while being in the registerAtService method: " + e.toString());
		}
	}

	/**
	 * A {@link Participant} calls this method on an active connection, to leave
	 * a work cycle. The method checks the state of the connection and decides
	 * whether the desired transition is allowed. It then crafts the
	 * corresponding {@link ManagementMessage} ({@link ManagementMessageLeaveWorkCycle}) and sends it to the
	 * {@link Server}.
	 * 
	 * The {@link Server} then replies with corresponding
	 * {@link ManagementMessage}.
	 * 
	 * @param p
	 *            The participant that requests to leave a work cycle. (Usually it is
	 *            the same as the assocParticipant of the connection/ or this).
	 */
	public void leaveWorkCycle(Participant p) {
		if (! assocParticipantManager.getParticipantMgmntInfoFor(p).isActive()) {
			log.warn("Bad transition. Not in ACTIVE status, to send LEAVEWORKCYLE message");
			return;
		}

		try {
			ManagementMessageLeaveWorkCycle m = new ManagementMessageLeaveWorkCycle(
					assocWorkCycleManager.getCurrentWorkCycleNumber() + assocWorkCycleManager.getLeaveOffset());
			this.sendMessage(m.getMessage());
		} catch (IOException e) {
			log.error("Problems occured within the leaveWorkCycle method: " + e.toString());
		}

	}
	
	/**
	 * A {@link Participant} can call this method to request a list of all
	 * active participants from the {@link Server}.
	 */
	public void requestActiveConnections(){
		ManagementMessage m = new ManagementMessageInfoRequest(new InfoServiceInfoReqActiveParticipantList());
		try {
			sendMessage(m.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@link Participant}s can call this method to request a key exchange with
	 * another participant. This participant is then indicated by the ID.
	 * 
	 * The method checks whether the ID is known / valid. If not (yet), it tries
	 * to update the participant list by requesting it once more from the
	 * server. It then performs a Diffie-Hellmann Key-Exchange with singed
	 * public values. See {@link KeyExchange} for more information about the
	 * exchange.
	 * 
	 * @param id
	 *            the ID (SHA1 fingerprint of the public part of the signature)
	 *            of the participant that you want to exchange keys with.
	 */
	public void requestKeyExchange(String id){
		if (id == null || assocKeyManager == null) return;
		
		ParticipantMgmntInfo pmi = assocParticipantManager.getParticipantMgmntInfoByParticipantID(id);
		
		if (pmi == null) {
			requestPassiveConnections();
			
			log.warn("Sorry - remote participant is not known.\n\tNew ParticipantList requested.\n\t"
									+ id
									+ " marked as unfinished key exchange request.\n\tKey exchange request aborted.");
			
			assocKeyManager.addUnfinishedKeyExchangeRequest(id);
			
			return;
		}
		
		if(assocParticipant.getId().compareTo(pmi.getParticipant().getId()) == 0) {
			log.debug("No need to exchange keys with yourself. Key exchange request aborted.");
			return;
		}
		
		DCKey dck = pmi.getKey();
		
		if(dck.getState() != DCKey.KEY_UNEXCHANGED) return;
		
		if(! assocKeyManager.verifyKey(pmi.getParticipant())){
			log.warn("Signature did not work with provided key. Key exchange request aborted.");
			return;
		}
		
		InfoServiceInfoRequestKeyExchange ke = new 	InfoServiceInfoRequestKeyExchange(assocParticipant.getId(),pmi.getParticipant().getId());
		ManagementMessageInfoRequest mi = new ManagementMessageInfoRequest(ke);
		
		try {
			sendMessage(mi.getMessage());
			dck.setSate(DCKey.KEY_REQUESTED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * A {@link Participant} can call this method to request a list of all
	 * passive participants from the {@link Server}.
	 */
	public void requestPassiveConnections(){
		ManagementMessage m = new ManagementMessageInfoRequest(new InfoServiceInfoReqPassiveParticipantList());
		try {
			sendMessage(m.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * As every connection is a thread, this method implements the main task for
	 * these threads. First of it sends out a
	 * {@link ManagementMessageWelcome2Service} management message (by calling
	 * welcome2Service()), and sets the associated participant manager.
	 * 
	 * It then enteres a loop which is performed until isStopped() returns a
	 * true, or some other unfixable erros are detected. In this loop it waits
	 * for arriving {@link ManagementMessage}s and tries to handle them, by
	 * calling the setSatus() method.
	 * 
	 * If a connection is going down, it tries to do some cleanup-work.
	 * 
	 */
	public void run() {
		try {

			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());

			byte[] messagetypeb = new byte[2];
			byte[] lengthb = new byte[2];

			if (servermode)
				this.welcome2Service(server);
			else
				assocParticipantManager = getAssociatedParticipantManager();

			// TODO Maybe timeout for a socket, on unresponsiveness of a
			// client!?
			while (!isStopped()) {
				try {
					input.readFully(messagetypeb, 0, 2); // Read the first 2
															// bytes
					// (management message type)

					input.readFully(lengthb, 0, 2); // next two bytes are
													// message length;
					// payload length)

					int messagetype = Util
							.stuffBytesIntoUInt(messagetypeb);
					int length = Util.stuffBytesIntoUInt(lengthb);
					byte[] messageb = new byte[length]; // We can gain
														// performance
					// if we can initialize
					// messageb before entering
					// the while loop
					// already with 2^16. message passing has to be adjusted
					// TODO

					input.readFully(messageb, 0, length);

					log.debug("received message from" + clientSocket.toString());
					log.trace(Arrays.toString(messageb));

					try {

						ManagementMessage m = ManagementMessage.parseMessage(
								messagetype, messageb);
						if (m != null)
							setStatus(m);

					} catch (IllegalArgumentException e) {
						log.warn(e.toString());
						log.warn("Could not handle message correctly: "
										+ String.valueOf(messagetype) + ","
										+ String.valueOf(length));
						log.trace(Arrays.toString(messageb));
						log.warn(e.toString());
					}
				} catch (NullPointerException e) {
					log.error("Experiencing problems with the connection: ");
					log.error(e.toString());
				} catch (IndexOutOfBoundsException e) {
					log.error(e.toString());
				} catch (IOException e) {
					log.error(e.toString());
					log.info("Remote part " + this.toString() + " seems to be disappeared");
					log.debug("Better error handling should be installed.");
					break;
				}
			}
		} catch (IOException e) {
			log.error("IOExcheption caught");
			e.printStackTrace();
		} finally {
			if (clientSocket != null)
				try {
					clientSocket.close();
				} catch (IOException e) {
				}

			if (server != null) {
				server.getAspirants().remove(clientSocket);
				server.getConnections().remove(clientSocket);
			}
		}

	}

	/**
	 * Sends byte array r to the other end of the connection
	 * 
	 * @param r
	 * @throws IOException
	 */
	public synchronized void sendMessage(byte[] r) throws IOException {
		if (output == null)
			throw new IOException("No output stream ready to send response");

		log.debug("Sending message: "  + Arrays.toString(r));

		try {
			output.write(r, 0, r.length);
			// output.flush();
		} catch (IOException e) {
			log.error("Output error: " + e.toString());
			log.error("Closing Connection");
			stopped = true;
		}
	}

	/**
	 * Standard setter.
	 * 
	 * Associates a {@link KeyManager} to this {@link Connection}.
	 * @param k the KeyManager that you want to associate.
	 */
	public void setAssocKeyManager(KeyManager k){
		assocKeyManager = k;
	}

	/**
	 * Standard setter.
	 * 
	 * Associates a {@link ParticipantManager} to this connection.
	 * 
	 * @param p
	 *            the ParticipantManager that you want to associate.
	 */
	public void setAssocParticipantManager(ParticipantManager p){
		assocParticipantManager = p;
	}
	
	/**
	 * Standard setter.
	 * 
	 * Associates a {@link WorkCycleManager} to this connection.
	 * @param r the {@link WorkCycleManager}, that you want to associate.
	 */
	public void setAssocWorkCycleManager(WorkCycleManager r){
		assocWorkCycleManager = r;
	}

	/**
	 * Standard setter.
	 * 
	 * Can be used to set the expected entry work cycle number which is usually
	 * indicated by the server with the {@link ManagementMessageWelcome2WorkCycle}
	 * management message.
	 * 
	 * @param r
	 *            work cycle number after which the participant is expected to
	 *            actively take part in work cycles.
	 */
	public void setExpectedEntryWorkCycle(long r) {
		exptectedEntryWC = r;
	}

	public void setExpectedLeavingWorkCycle(long r){
		expectedLeavingWC = r;
	}
	
	/**
	 * Each connection should have an associated participant
	 * 
	 * @param p the participant that shall be associated to the connection.
	 */
	public void setParticipant(Participant p) {
		this.assocParticipant = p;
	}
	
	public void setMode(int m){
		currentMode = m;
	}

	/**
	 * This method is one of the most important methods in the connection class.
	 * After new {@link ManagementMessage}s arrive, they are crafted and then
	 * passed here for correct evaluation.
	 * 
	 * This method realizes the transitions of the connection, as well, as it
	 * calls the right actions that follow on these messages (i.e. it invokes
	 * the right handling methods).
	 * 
	 * @param m The ManagementMessage that influences the connection.
	 */
	public void setStatus(ManagementMessage m){

		this.lastMessage = m;
	    	
	    	// before actually make the mode transition, notify the others. 
	    	// this should be the other way work cycle, but then an early return would not be possible
	    	
	    	setChanged();
	    	notifyObservers(m);

	    	// Fist check only messages, that can go from Participants -> Server
	    	if (servermode){  

		    	//REGISTERATSERVICE
		    	if ((m instanceof ManagementMessageRegisterAtService) && (currentMode == MODE_BROKEN)){
		    		this.regsiterAtService = (ManagementMessageRegisterAtService) m;
		    		this.currentMode = MODE_WELCOME2SERVICE;
		    		// On the server side ignite the acception process.
		    		server.accept4Service(this, (ManagementMessageRegisterAtService) m);
		    		return;
		    	}
		    	
		    	//JOINWORKCYCLE
		    	else if ((m instanceof ManagementMessageJoinWorkCycle) 
		    			&& assocParticipantManager.getParticipantMgmntInfoFor(this).isPassive()
		    			&& !assocParticipantManager.getParticipantMgmntInfoFor(this).isActive()
		    			){
		    		this.joinWorkCycle   = (ManagementMessageJoinWorkCycle) m;
		    		this.server.joinWorkCycleRequested(this);
		    		this.currentMode = MODE_ACTIVE;
		    		return;
		    	}
		    	
		    	//ADD
		    	else if((m instanceof ManagementMessageAdd) && (currentMode == MODE_ACTIVE)){
		    		this.lastAdd = (ManagementMessageAdd) m;
		    		log.debug("ADD message arrived");
		    		assocWorkCycleManager.addMessageArrived(this, lastAdd);
		    		return;
		    	}
		    	//LEAVEWORKCYCLE
		    	else if((m instanceof ManagementMessageLeaveWorkCycle) && (currentMode == MODE_ACTIVE)){
		    		this.leaveWorkCycle = (ManagementMessageLeaveWorkCycle) m;
		    		log.debug("LEAVEWORKCYCLE message arrived. Good bye and Thanks for participation.");
		    		server.leaveWorkCycleRequested(this, leaveWorkCycle);
		    		return;
		    	}
		    	//LEAVESERVICE
	    	} 
	    	// only messages that can go from Server -> Participant
	    	else if ( !servermode ) {

		    	//WELCOME2SERVICE
		    	if ( m instanceof ManagementMessageWelcome2Service ){
		    		this.welcome2Service = (ManagementMessageWelcome2Service) m;
		    		this.currentMode = MODE_REGISTERATSERVICE;
		    		return;
		    	}
		    	
		    	//ACCEPTED4SERVICE
		    	else if ((m instanceof ManagementMessageAccepted4Service) && (currentMode == MODE_REGISTERATSERVICE)){
		    		this.acceptedForService = (ManagementMessageAccepted4Service) m;
		    		this.currentMode = MODE_PASSIVE;
		    		getAssociatedParticipantManager().setParticipantPassive(assocParticipant);
		    		return;
		    	}

		    	//WELCOME2WORKCYCLE
		    	else if ((m instanceof ManagementMessageWelcome2WorkCycle)
					&& (currentMode == MODE_PASSIVE)) {
		    		this.welcome2WorkCycle = (ManagementMessageWelcome2WorkCycle) m;
		    		
		    		if (welcome2WorkCycle.isAccepted()) {
		    			
		    			this.currentMode = MODE_ACTIVE;
		    			this.setExpectedEntryWorkCycle(welcome2WorkCycle.getWorkCycle());
		    			
					if (welcome2WorkCycle.isAccepted())
						assocParticipantManager.setParticipantActiveAfterWorkCycle(
								assocParticipant, welcome2WorkCycle.getWorkCycle());
		    			assocParticipantManager.setParticipantsActive(welcome2WorkCycle
							.getActiveParticipantIDs());
		    		}
				return;
			} 	
		    	
			// TICK
			else if (m instanceof ManagementMessageTick) {
				lastTick = (ManagementMessageTick) m;
				assocParticipantManager.update(lastTick.getWorkCycleNumber());

				// Only let active connections proceed.
				if (assocParticipantManager.getParticipantMgmntInfoFor(
						assocParticipant).isActive()) {
					
					if (assocWorkCycleManager != null) {
						assocWorkCycleManager
								.tickArrived(lastTick.getWorkCycleNumber());
					} else {
						assocWorkCycleManager = new WorkCycleManager(welcome2Service
								.getMethod(), lastTick.getWorkCycleNumber(),
								welcome2Service.getCharLength());
						assocWorkCycleManager.setParticipant(assocParticipant);
						assocWorkCycleManager
								.setAssocParticipantManager(assocParticipantManager);
						
						// feed with the messages that we already have
						while (sendMessageBuffer.size() > 0) {
							feedWorkCycleManager(sendMessageBuffer.removeFirst());
						}

						assocWorkCycleManager.addExpectedConnection(this);
						assocWorkCycleManager
								.tickArrived(lastTick.getWorkCycleNumber());
					}

				}
				return;
			}
		    	
		    	// ADDED
		    	else if ((m instanceof ManagementMessageAdded)){
		    		lastAdded = (ManagementMessageAdded) m;
		    		
		    		if (assocWorkCycleManager == null){
		    			log.error("ADDED message arrived before the first tick. Skipping message.");
		    			return;
		    		}
		    		
		    		assocWorkCycleManager.addedMessageArrived(lastAdded);
		    		return;
		    	}
	    	}
	    	// only messages that could go to both directions could go here.
	    	// or messages that were send in a bad state.
	    	
	    	if (m instanceof ManagementMessageInfoRequest){
	    		ManagementMessageInfoRequest w = (ManagementMessageInfoRequest) m;
	    		w.getInfoServiceRequest().handleRequest(this);
	    		return;
	    	} else if (m instanceof ManagementMessageInfo){
	    		ManagementMessageInfo w = (ManagementMessageInfo) m;
	    		w.getInfo().handleInfo(this);
	    		return;
	    	}
	    
	    	
	    	// error handling - if a certain amount of errors occur in a period of time,
	    	// we stop the connection
	    	log.info("Could not react correctly on message: " + m.toString());
	    	
	    	long n = System.currentTimeMillis();
	    	
	    	if(n - firstError < 1000){
	    		errorCount++;
	    		if(errorCount > 2) {
	    			stopped = true;
	    			log.error("There were too many connection erros in the last few moments. Stopping connection");
	    		}
	    	} else{
	    		firstError = n;
	    		errorCount = 0;
	    	}
	    	
	    	return;
	    }

	/**
	 * Set whether a connection shall be stopped.
	 * 
	 * @param s
	 *            : stop it, or not
	 */
	public void stop(boolean s) {
		this.stopped = s;
	}

	/**
	 * This method has to be called on {@link Participant} side after a
	 * {@link ManagementMessageTick} arrived.
	 * 
	 * @param workCycleNumber
	 *            the work cycle number that was transferred with the TICK management
	 *            message.
	 */
	public void tickArrived(long workCycleNumber){
		assocWorkCycleManager.tickArrived(workCycleNumber);
	}
	
	
	/**
	 * This was overwritten to enhance the debug / logging output.
	 * It future it would be nice to use log4java and throw this crutch away.
	 */
	public String toString() {
		return ident + "/con:" + String.valueOf(clientSocket.getPort()) + ":"
				+ String.valueOf(clientSocket.getLocalPort());
	}

	/**
	 * generate a WELCOME2WORKCYLE message and send it.
	 * 
	 * @param a
	 *            accept/reject
	 * @param r
	 *            work cycle number
	 * @param t
	 *            timeout (0 == no timeout)
	 */
	public void welcome2WorkCycle(int a, long r, int t) {
		if (! assocParticipantManager.getParticipantMgmntInfoFor(this).isPassive()) {
			log.warn("Bad transition. Not in PASSIVE status, to send WELCOME2WORKCYCLE message");
			return;
		}

		exptectedEntryWC = r;

		ManagementMessageWelcome2WorkCycle m = new ManagementMessageWelcome2WorkCycle(a, r, t, server.getInfoService().getActiveParticipants());
		try {
			this.sendMessage(m.getMessage());
		} catch (IOException e) {
			log.error("Input error: " + e.toString());
		}

	}

	/**
	 * generate a WELCOME2SERVICE message and send it.
	 * 
	 * @param s
	 *            Server for which the connection has to be prepared
	 */
	public void welcome2Service(Server s) {

		if (!(currentMode == MODE_BROKEN)) {
			log.warn("Bad transition. Not in broken status, to send WELCOME2SERVICE message");
			return;
		}

		ManagementMessageWelcome2Service m = new ManagementMessageWelcome2Service(s, assocWorkCycleManager.getMethod());
		try {

			log.debug("Sending WELCOME2SERVER: " );
			log.trace(Arrays.toString(m.getMessage()));
			this.sendMessage(m.getMessage());
		} catch (IOException e) {
			log.error("Problemas occured with the WELCOME2SERVER message: " + e.toString());
		}
	}
}
