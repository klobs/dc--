/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.InfoService.InfoService;
import de.tu.dresden.dud.dc.KeyGenerators.KeyGenerator;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAccepted4Service;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageLeaveWorkCycle;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageRegisterAtService;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageWelcome2WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;


/**
 * @author klobs
 * 
 * Multi-threaded server. Ideas taken from
 * http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 * 
 * A multi-threaded server is chosen, instead of a thread-pooled, because normally we have
 * long duration connections. 
 * 
 */
public class Server implements Runnable {

	// Logging
	Logger log = Logger.getLogger(Server.class);

	private int				symbollength	= 12;
	private LinkedList<Connection> 	aspConns		= new LinkedList<Connection>();
	private InfoService		info			= null;
	private boolean			isStopped    	= false;
	private KeyExchangeManager keyExManager = new KeyExchangeManager(KeyExchangeManager.KEX_MANUAL);
	private ParticipantManager participantManager = new ParticipantManager();
	private int 			port 			= 6867;
	private WorkCycleManager	workCycleManager	= null;
	private ServerSocket	serverSocket	= null;
	private int				timeout			= 0;

	/**
	 * @param listenPort specifies the port on which the DC Service shall be provided.
	 */
	public Server(int listenPort) {
		// set port
		if(port > 0){ this.port = listenPort; }
		
		// create InfoService
		info = new InfoService(this);
		
		workCycleManager = new WorkCycleManager(KeyGenerator.KGMETHOD_DC_FAIL_STOP_WORK_CYCLE ,0 /*Long.MIN_VALUE*/, symbollength);
		workCycleManager.setServer(this);
		workCycleManager.setAssocParticipantManager(participantManager);
	}
	
	public void accept4Service(Connection c, ManagementMessageRegisterAtService m){
		if(! aspConns.contains(c)){
			// QUESTION do we need aspirants? Probably not.
			log.info(c.getClientSocket().toString() + "Wanted to register at the service, but is not in the list of aspirants");
			return;
		}
		
		// TODO install decision process (oracle) here, when new clients should be rejected.
		c.accept4Service(ManagementMessageAccepted4Service.ACCEPTED);
		aspConns.remove(c);
		
		// Associate the connection with a participant. 
		Participant p = new Participant(m.getParticipantID(), m.getUsername(), m.getSignature(), m.getDHPublicPart(), m.getDHPublicSig());
		c.setParticipant(p);
		
		c.setAssocParticipantManager(participantManager);
		c.setAssocWorkCycleManager(workCycleManager);
		
		participantManager.addParticipant(p);
		ParticipantMgmntInfo pmi = participantManager.getParticipantMgmntInfoFor(p);
		pmi.setAssocConnection(c);
		pmi.setPassive(true);
	}
	
	
	public void activateConnection(Connection c){
		participantManager.setParticipantActive(c.getAssociatedParticipant());
		this.workCycleManager.addExpectedConnection(c);
		c.setAssocWorkCycleManager(workCycleManager);
	}
	
	public void deactivateConnection(Connection c, long workcycle){
		participantManager.unsetParticipantActiveAfterWorkCycle(c.getAssociatedParticipant(), workcycle);
		c.setExpectedLeavingWorkCycle(workcycle);
		this.workCycleManager.addLeavingConnection(c);
	}
	
	public synchronized LinkedList<Connection> getActiveConnections(){
		LinkedList<Connection> c = new LinkedList<Connection>();
		LinkedList<ParticipantMgmntInfo> p = participantManager.getActivePartMgmtInfo();
		Iterator<ParticipantMgmntInfo> i = p.iterator();
		ParticipantMgmntInfo pmi = null;
		
		while(i.hasNext()){
			pmi = i.next();
			c.add(pmi.getAssocConnection());
		}
		return c;
	}
	
	/**
	 * Aspirants are participants, that can gather information, but do 
	 * not take part in work cycles, yet.
	 * @return a list of all waiting participants.
	 */
	public LinkedList<Connection> getAspirants(){
		return this.aspConns;
	}
	
	
	/**
	 * @return Returns the list with all connections of the running server
	 */
	public LinkedList<Connection> getConnections(){
		LinkedList<Connection> c = new LinkedList<Connection>();
		LinkedList<ParticipantMgmntInfo> p = participantManager.getPassivePartMgmtInfo();
		Iterator<ParticipantMgmntInfo> i = p.iterator();
		ParticipantMgmntInfo pmi = null;
		
		while(i.hasNext()){
			pmi = i.next();
			c.add(pmi.getAssocConnection());
		}
		return c;		
	}

	/**
	 * 
	 * @return the one and only info service
	 */
	public InfoService getInfoService(){
		return this.info;
	}
	
	
	public KeyExchangeManager getKeyExchangeManager(){
		return keyExManager;
	}
	
	public WorkCycleManager getWorkCycleManager(){
		return workCycleManager;
	}
	
	/**
	 * 
	 * @return Returns the status, whether server is running, or not.
	 */
	private synchronized boolean isStopped() {
        return this.isStopped;
    }

	/**
	 * This method decides whether a certain connection is allowed to participate in work cycles.
	 * @param c the {@link Connection} that belongs to the {@link Participant}, that wants to join the {@link WorkCycle}s.
	 */
	public void joinWorkCycleRequested(Connection c){
		if (!(getConnections().contains(c))) {
			log.info(c.getClientSocket().toString() + "Wanted to join the work cycles, but is not in the list of passive connections. We can not allow that.");
			return;
		}
		
		// install accept / reject oracle here
		c.welcome2WorkCycle(ManagementMessageWelcome2WorkCycle.ACCEPTED, workCycleManager.getEntryWorkCycleNumber(), timeout);
		this.activateConnection(c);
	}

	/**
	 * This method prepares everything on server side, to make a participant ready to leave a work cycle.
	 * 
	 * @param c 
	 */
	public void leaveWorkCycleRequested(Connection c, ManagementMessageLeaveWorkCycle m){
		if (!(getActiveConnections().contains(c))) {
			log.info(c.getClientSocket().toString() + "Wanted to leave the work cycle, but is not in the list of active connections. We can not allow that.");
			return;
		}
	
		if (workCycleManager.getCurrentWorkCycleNumber() >= m.geWorkCycleNumber()){
			log.info(
					c.getClientSocket().toString()
							+ "Wanted to leave the work cycle, but it is too soon. We can not allow that.");
			return;
		}
		
		this.deactivateConnection(c, m.geWorkCycleNumber());
	}
	
	private void openServerSocket(){
		try {
			log.info("Opening ServerSocket on port " + String.valueOf(port));

			this.serverSocket = new ServerSocket(this.port);
		
		} catch (IOException e) {
			log.error( "Could not bind to port " + String.valueOf(port));
			log.error(e.toString());
		}
	}

	/**
	 * Start the server
	 */
	@Override
	public void run(){
        synchronized(this){
            // this.runningThread = Thread.currentThread(); //can this be usefull?
        }
           
        openServerSocket();
        while(! isStopped()){
        	Socket clientSocket;
        	try {
            	clientSocket = null;
            	
            	clientSocket = this.serverSocket.accept();
            	
            	Connection c = new Connection(this, clientSocket, this);
            	
            	c.setAssocWorkCycleManager(workCycleManager);
            	
            	log.debug("New connection arrived from " + clientSocket.toString());
            	
                aspConns.add(c);

                new Thread(c, "connectionServerSide").start();
                
            } catch (IOException e) {
                
            	if(isStopped()) {
                    log.info("Server Stopped: " + e.toString());
                    for(Connection d : getConnections()){
                    	d.stop(true);
                    }
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            } finally {
            	
            }
        }
        log.info("Server Stopped.");
    }

	/**
	 * Sets the symbol length for the server
	 * @param symbolLength in bytes
	 */
	public void setSymbolLength(int symbolLength) {
		this.symbollength = symbolLength;
	}


	
	/**
	 * stop the server
	 */
    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
    
    public String toString(){
    	return "server";
    }
	
}
