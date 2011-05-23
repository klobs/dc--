/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

/**
 * In this class you'll find a lot of useful stuff, that Java does not offer by
 * itself. I.e. comfortably concatenate arrays, convert hex to string or vice
 * versa, etc.
 * 
 * @author klobs
 * 
 */
public class Util {
	
	/**
	 * Concatenate two byte arrays
	 * @param a
	 * @param b
	 * @return concatenation of a and b
	 */
	public static byte[] concatenate(byte[] a, byte[] b){
		// source http://forums.sun.com/thread.jspa?threadID=571260&messageID=2827711
		byte c[] = new byte[a.length + b.length];
		
		System.arraycopy(a, 0, c, 0, a.length);
	    System.arraycopy(b, 0, c, a.length, b.length);
	
		return c;
	}
	
	/**
	 * Convert a byte array to a hex string.
	 * 
	 * @param a
	 *            the byte array that you want to represent as hex string
	 * @return a hex string representation of the byte array a
	 */
	public static String convertToHex(byte [] a){
		String hexStr = "";
		for (int i = 0; i < a.length; i++) {
			hexStr +=  Integer.toString( ( a[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return hexStr;
	}

	/**
	 * Take a hex string, and convert it to the corresponding byte array. There
	 * is no checking whether the string, that you pass is actually a hex-value,
	 * so please don't mess with this method.
	 * 
	 * @param a the string that you want have to be converted.
	 * @return the corresponding byte [].
	 */
	public static byte[] convertFromHex(String a){
		int len = a.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(a.charAt(i), 16) << 4)
	                             + Character.digit(a.charAt(i+1), 16));
	    }
	    return data;
	}
	

	/**
	 * Merge two unequally sized byte arrays (by xoring each element). The
	 * shorter one will be padded up to the length of the longer one with 0s.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] fillAndMergeSending(byte[] a, byte[] b) {

		if (a == null | b == null)
			throw new IllegalArgumentException("The arrays a, b are null.");

		int i;
		byte[] shorterone = null;
		byte[] longerone = null;

		if (a.length <= b.length) {
			shorterone = a;
			longerone = b;
		} else {
			shorterone = b;
			longerone = a;
		}

		byte[] c = new byte[longerone.length];

		for (i = 0; i < shorterone.length; i++) {
			c[i] = (byte) (a[i] ^ b[i]);
		}

		while (i < longerone.length) {
			c[i] = longerone[i];
			i++;
		}

		return c;
	}
	
	/**
	 * Returns the first n bytes of array a
	 * @param a The array that contains the info
	 * @param n indicating how many bytes you want to have cut of the head.
	 * @return the first n bytes of a. If n > a.length or !(n>0) it returns a new byte[0] array.
	 */
	public static byte[] getFirstBytes(byte[] a, int n){
		if( n > a.length || !(n > 0)) return new byte[0];
		
		byte[] b = new byte[n];
		
		for( int i = 0; i < n; i++){
			b[i] = a[i];
		}
		
		return b;
	}

	/**
	 * Take byte[] a and return n bytes out of it, starting by offset o.
	 * @param a The byte[] with the content.
	 * @param o The offset.
	 * @param n The desired number of bytes.
	 * @return The information, or - if you provide bogus values for a,o, or n a new byte[0].
	 */
	public static byte[] getBytesByOffset(byte[] a, int o, int n){
		if ( a == null ) return new byte[0];
		if ( a.length - o < n && o >= 0 && n > 0 ) return new byte[0];
		
		byte[] b = new byte[n];
		
		for(int i = o; i < o + n; i++ ){
			b[i - o] = a[i];
		}
		
		return b;
	}
	
	public static byte[] getLastBytes(byte[] a, int n) {
		if (n == a.length)
			return a;
		if (n > a.length || !(n > 0))
			return new byte[0];

		byte[] b = new byte[n];

		for (int i = 1; i <= n; i++) {
			b[n - i] = a[a.length - i];
		}

		return b;
	}
	
	/**
	 * Merge two equally sized byte arrays (by xoring each element).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] mergeDCwise(byte[] a, byte[] b, final long modulus) {

		if (a == null | b == null | a.length != b.length)
			throw new IllegalArgumentException(
					"The arrays a, b are null or do not have the same length.");

		byte[] c = new byte[a.length];

		for (int i = 0; i< a.length; i = i+4){
			
			long ka = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(a,i, 4)) % modulus;
			long kb = Util.stuffBytesIntoLongUnsigned(Util.getBytesByOffset(b,i, 4)) % modulus;
			
			long d = (ka + kb) % modulus;
			
			c[i]   = (byte) (d >>> 24);
			c[i+1] = (byte) (d >>> 16);
			c[i+2] = (byte) (d >>> 8 );
			c[i+3] = (byte) d;
		}
		
		return c;

	}
	
	/**
	 * Put a byte array into an integer. Interpret the byte array as unsigned.
	 * Ugly work has to be done. 
	 * Length checking will be done.
	 * @throws IllegalArgumentException when b is too long
	 * @param b the byte array that you would like to have converted
	 * @return the corresponding int.
	 */
	public static int stuffBytesIntoUInt(byte[] b){
		int r = 0;
	
		if (!(b.length <= 4 && b.length > 0)) throw new IllegalArgumentException("The value you provided can not be stuffed into an int.");
		
		for (int i=0; i < b.length; i++){
			r = r | ((b[b.length - 1 - i] & 0x000000FF) << 8 *i);
		}
	
		return r;
	}

	/**
	 * Put a byte array into a long.
	 * Same ugly stuff as the int version.
	 * Length checking will be done.
	 * @throws IllegalArgumentException when b is too long
	 * @param b the byte array that you would like to have converted
	 * @return the corresponding long.
	 */
	public static long stuffBytesIntoLong(byte[] b){
		long r = 0;
	
		if (!(b.length <= 8 && b.length > 0)) throw new IllegalArgumentException("The value you provided can not be stuffed into an long.");
		
		for (int i=0; i < b.length; i++){
			r = r | ((b[b.length - 1 - i])<< 8 *i);
		}
		
		return r;
	}

	/**
	 * Put a byte array into a long.
	 * Same ugly stuff as the int version.
	 * Length checking will be done.
	 * <b>*WARNING*</b>Signs will be removed in each byte
	 * @throws IllegalArgumentException when b is too long
	 * @param b the byte array that you would like to have converted
	 * @return the corresponding long.
	 */
	public static long stuffBytesIntoLongUnsigned(byte[] b){
		long r = 0;
	
		if (!(b.length <= 8 && b.length > 0)) throw new IllegalArgumentException("The value you provided can not be stuffed into an long.");
		
		for (int i=0; i < b.length; i++){
			r = r | (((b[b.length - 1 - i]) & 0x00000000000000FFL)<< 8 *i);
		}
		
		return r;
	}

	/**
	 * Take an integer and return it as byte[1].
	 * 
	 * @param i
	 *            the integer that you want to convert.
	 * @return the byte[1] representation of i. If i is beyond max/min values
	 *         for a byte, an {@link IllegalArgumentException} is thrown.
	 */
	public static byte[] stuffIntIntoByte(int i){
		
		if(i > Byte.MAX_VALUE || i < Byte.MIN_VALUE) 
			throw new IllegalArgumentException("The value you provided can not be stuffed into a byte.");
		
		byte []b = new byte[1];
		
		b[0] = (byte) i;
		
		return b;
	}

	/**
	 * Take an integer and return it as byte[4].
	 * 
	 * @param i
	 *            the integer that you want to convert.
	 * @return the byte[4] representation of i. If i is beyond max/min values
	 *         for an integer, an {@link IllegalArgumentException} is thrown.
	 */
	public static byte[] stuffIntIntoInt(int i){
	
		byte []b = new byte[4];
		
		b[3] = (byte) i;
		b[2] = (byte) (i >>> 8);
		b[1] = (byte) (i >>> 16);
		b[0] = (byte) (i >>> 24);
		
		return b;
	}

	/**
	 * Take an integer and return it as byte[2].
	 * 
	 * @param i
	 *            the integer that you want to convert.
	 * @return the byte[2] representation of i. If i is beyond max/min values
	 *         for a short, an {@link IllegalArgumentException} is thrown.
	 */
	public static byte[] stuffIntIntoShort(int i){
	
		if(i > Short.MAX_VALUE || i < Short.MIN_VALUE) 
			throw new IllegalArgumentException("The value you provided can not be stuffed into a short.");
		
		byte []b = new byte[2];
		
		b[1] = (byte) i;
		b[0] = (byte) (i >>> 8);
		
		return b;
	}

	/**
	 * Take an integer and return it as byte[8].
	 * 
	 * @param l
	 *            the integer that you want to convert.
	 * @return the byte[8] representation of l. If l is beyond max/min values
	 *         for a long, an {@link IllegalArgumentException} is thrown.
	 */
	public static byte[] stuffLongIntoLong(long l){
		byte []b = new byte[8];
	
		b[7] = (byte) l;
		b[6] = (byte) (l >>> 8);
		b[5] = (byte) (l >>> 16);
		b[4] = (byte) (l >>> 24);
		b[3] = (byte) (l >>> 32);
		b[2] = (byte) (l >>> 40);
		b[1] = (byte) (l >>> 48);
		b[0] = (byte) (l >>> 56);
		
		return b;
	}

	/**
	 * Take a string and return it as byte[].
	 * 
	 * Actually only a wrapper for getBytes() of the {@link String} class.
	 * 
	 * @param s
	 *            the string that you want to convert.
	 * @return the byte[] representation of s. 
	 */
	public static byte[] stuffStringIntoCharArray(String s){
		return s.getBytes();
	}
}
