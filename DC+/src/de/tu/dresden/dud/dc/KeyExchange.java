/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.util.HashSet;

public class KeyExchange {

	private HashSet<String> exchange = new HashSet<String>();
	
	public KeyExchange(String p1, String p2){
		exchange.add(p1);
		exchange.add(p2);
	}
	
	public boolean compareTo(InfoServiceInfoRequestKeyExchange i){
		return exchange.contains(i.getFrom()) && exchange.contains(i.getTo());
	}
	
	public HashSet<String> getExchangers(){
		return exchange;
	}
	
}
