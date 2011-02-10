/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.math.BigInteger;

/**
 * This class will be used for keys later
 * @author klobs
 *
 */
public class DCKey {

	public static final int KEY_UNEXCHANGED  = 0;
	public static final int KEY_REQUESTED 	 = 1;
	public static final int KEY_EXCHANGED	 = 2;
	public static final int KEY_VOID		 = 3;
	
	private	boolean	inverseKey	= false;
	
	private 	int	state	= KEY_UNEXCHANGED;
	
	private BigInteger calculatedSecret = null;
		
	public BigInteger getCalculatedSecret(){
		return calculatedSecret;
	}
	
	public int getState(){
		return state;
	}
	
	public boolean getInverse(){
		return inverseKey;
	}
	
	public boolean isKeyExchanged(){
		if (state == KEY_EXCHANGED) return true;
		return false;
	}
	
	public void setCalculatedSecret(BigInteger i, boolean useInverse){
		calculatedSecret = i;
		state = KEY_EXCHANGED;
		inverseKey = useInverse;
	}
	
	public void setSate(int i){
		state = i;
	}
	
}
