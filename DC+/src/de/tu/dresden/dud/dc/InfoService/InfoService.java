/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.InfoService;

import java.util.ArrayList;
import java.util.Iterator;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Participant;
import de.tu.dresden.dud.dc.Server;

/**
 * This class is responsible for providing all kinds of information about the server.
 * @author klobs
 */
public class InfoService {
	
	private Participant participant = null;
	private Server		server  	= null;
	private String 		version 	= new String("0.0.4");
	
	
	/**
	 * Default constructor
	 * @param s the server for which the information shall be provided.
	 */
	public InfoService(Server s){
		this.server = s;		
	}

	/**
	 * Default constructor
	 * @param p the participant for which the information shall be provided.
	 */
	public InfoService(Participant p){
		this.participant = p;
	}

	
	/**
	 * Do we accept new participants? 
	 * In the beginning - of course...
	 * TODO install an oracle here
	 * @return 1 for true and 0 for nope
	 */
	public int doAccept(){
		return 1;
	}
	
	/**
	 * Get the number of participants
	 * @return the number of participants
	 */
	public int getParticipantCount(){
		// the new connection has to be subtracted
		return server.getConnections().size();
	}
	
	public ArrayList<Participant> getActiveParticipants(){
		ArrayList<Participant> r = new ArrayList<Participant>(server.getActiveConnections().size());
		Connection c = null;
		
		for (Iterator<Connection> i = server.getActiveConnections().iterator(); i.hasNext();){
			c = i.next();
			r.add(c.getAssociatedParticipant());
		}
		
		return r;
	}
	
	/**
	 * Returns a list with all the participants that are currently connected to the server.
	 * @return
	 */
	public ArrayList<Participant> getTotalParticipants(){
		ArrayList<Participant> p = new ArrayList<Participant>();
		Iterator<Connection> i = server.getConnections().iterator();
	
		while(i.hasNext()){
			p.add(i.next().getAssociatedParticipant());
		}
		
		return p;
	}
	
	
	/**
	 * Return version supported by the server 
	 * @return
	 */
	public String getVersion(){
		return this.version;
	}

	
	/**
	 * Returns the directory URL for clients.
	 * not implemented yes
	 *  
	 * @return empty string ("")
	 */
	public String getDirURL(){
		return "";
	}
	
	
	/**
	 * When there is a getter, there has to be a setter, hasn't it?
	 * @param v
	 */
	public void setVersion(String v){
		this.version = v;
	}
}
