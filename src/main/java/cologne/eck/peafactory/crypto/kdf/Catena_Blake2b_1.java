package cologne.eck.peafactory.crypto.kdf;

/*
 * PeaFactory - Production of Password Encryption Archives
 * Copyright (C) 2015  Axel von dem Bruch
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * See:  http://www.gnu.org/licenses/gpl-2.0.html
 * You should have received a copy of the GNU General Public License 
 * along with this library.
 */

/*
 * Round-reduced Blake2b as H' for Password Hashing Scheme Catena 
 * Copyright (C) 2015  Axel von dem Bruch
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * See:  https://www.gnu.org/licenses/lgpl-2.1.html
 * You should have received a copy of the GNU General Public License 
 * along with this library.
 */

import java.util.Arrays;


public class Catena_Blake2b_1 implements ReducedDigest {
	
	private final static long blake2b_IV[] = 
			// Blake2b Initialization Vector: 
			// Produced from the square root of primes 2, 3, 5, 7, 11, 13, 17, 19.
			// The same as SHA-512 IV.
		{
		  0x6a09e667f3bcc908L, 0xbb67ae8584caa73bL, 
		  0x3c6ef372fe94f82bL, 0xa54ff53a5f1d36f1L,
		  0x510e527fade682d1L, 0x9b05688c2b3e6c1fL,
		  0x1f83d9abfb41bd6bL, 0x5be0cd19137e2179L 
		};
	private final static long blake2b_IV0 = 0x6a09e667f2bdc948L;
	
	private final static byte[][] blake2b_sigma = // Message word permutations
		{
		  {  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 } ,
		  { 14, 10,  4,  8,  9, 15, 13,  6,  1, 12,  0,  2, 11,  7,  5,  3 } ,
		  { 11,  8, 12,  0,  5,  2, 15, 13, 10, 14,  3,  6,  7,  1,  9,  4 } ,
		  {  7,  9,  3,  1, 13, 12, 11, 14,  2,  6,  5, 10,  4,  0, 15,  8 } ,
		  {  9,  0,  5,  7,  2,  4, 10, 15, 14,  1, 11, 12,  6,  8,  3, 13 } ,
		  {  2, 12,  6, 10,  0, 11,  8,  3,  4, 13,  7,  5, 15, 14,  1,  9 } ,
		  { 12,  5,  1, 15, 14, 13,  4, 10,  0,  7,  6,  3,  9,  2,  8, 11 } ,
		  { 13, 11,  7, 14, 12,  1,  3,  9,  5,  0, 15,  4,  8,  6,  2, 10 } ,
		  {  6, 15, 14,  9, 11,  3,  0,  8, 12,  2, 13,  7,  1,  4, 10,  5 } ,
		  { 10,  2,  8,  4,  7,  6,  1,  5, 15, 11,  9, 14,  3, 12, 13 , 0 } ,
		  {  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 } ,
		  { 14, 10,  4,  8,  9, 15, 13,  6,  1, 12,  0,  2, 11,  7,  5,  3 }
		};
	
	private final static int BLOCK_LENGTH_BYTES = 128;// bytes
	private final static int H_LEN = 64; 

	private int vIndex = 0; // used as vertex index for Catena

	private byte[] buffer = null;//new byte[BLOCK_LENGTH_BYTES];
	private boolean halfBuffer = false;

	private long[] internalState = new long[16]; // In the Blake2b paper it is called: v
	private long[] chainValue = null; // state vector, in the Blake2b paper it is called: h
	
	private long t0 = 0L; // holds last significant bits, counter (counts bytes)
	private long t1 = 0L; // counter: Length up to 2^128 are supported
	
	public Catena_Blake2b_1() {
		buffer = new byte[BLOCK_LENGTH_BYTES];
		init();
	}

	
	// initialize chainValue
	private void init() {
		
		if (chainValue == null){
			chainValue = new long[8];
			chainValue[0] = blake2b_IV0;
			chainValue[1] = blake2b_IV[1];
			chainValue[2] = blake2b_IV[2];		
			chainValue[3] = blake2b_IV[3];			
			chainValue[4] = blake2b_IV[4];
			chainValue[5] = blake2b_IV[5];			
			chainValue[6] = blake2b_IV[6];
			chainValue[7] = blake2b_IV[7];
		}
	}
	
	private void initializeInternalState(){
		
		// initialize v:
		System.arraycopy(chainValue, 0, internalState, 0, chainValue.length);
		System.arraycopy(blake2b_IV, 0, internalState, chainValue.length, 4);
		internalState[12] = t0 ^ blake2b_IV[4];
		internalState[13] = t1 ^ blake2b_IV[5];
		internalState[14] = 0xFFFFFFFFFFFFFFFFL ^ blake2b_IV[6];
		internalState[15] = blake2b_IV[7];
	}
	
	/**
	 * Processes the given message
	 * 
	 * @param message
	 *            byte array containing the message to be processed
	 */
	public void update(byte[] message) {

		update(message, 0, H_LEN);
	}
	
	/**
	 * Processes one single byte
	 * 
	 * @param b
	 *            single byte to be processed
	 */
	public void update(byte b) {}
	
	/**
	 * Processes a number of bytes of the given message 
	 * from a start position up to offset+len
	 * 
	 * @param message
	 *            byte array containing the message to be processed
	 * @param offset
	 *            position of message to start from
	 * @param len
	 *            number of bytes to be processed.
	 */
	public void update(byte[] message, int offset, int len) {

		if (halfBuffer == false){
			System.arraycopy(message,  offset, buffer, 0, H_LEN);
			halfBuffer = true;			
		} else {
			System.arraycopy(message,  offset, buffer, H_LEN, H_LEN);			
			halfBuffer = false;
		}
	}
	
	/**
	 * Calculates the final digest value 
	 * 
	 * @param out
	 * 			the calculated digest will be copied in this array
	 * @param outOffset
	 * 			start position of the array out, where the digest is copied
	 */	
	public int doFinal(byte[] out, int outOffset) {
		
		t0 += BLOCK_LENGTH_BYTES;
		// simplified because increment is always 128
		if ( (t0 == 0)){
			t1++;
		}
		compress(buffer, 0);
		halfBuffer = false;

		for (int i = 0; i < chainValue.length; i++) {
			System.arraycopy(long2bytes(chainValue[i]), 0, out, outOffset + i * 8, 8);
		}		
		return 64;
	}
	
	/**
	 * Reset the hash function to use again after doFinal().
	 * This will not work for keyed digests. 
	 */
	public void reset() {
		halfBuffer = false;
		t0 = 0L;
		t1 = 0L;
		Arrays.fill(buffer,  (byte) 0);
		Arrays.fill(chainValue, 0L);	
		Arrays.fill(internalState, 0L);
		chainValue = null;

		init();
	}
	
	private void compress(byte[] message, int messagePos) {

		initializeInternalState();
		
		long[] m = new long[16];
		for (int j = 0; j < 16; j++) {
			m[j] = bytes2long(message, messagePos + j*8);
		}
		// single round: Catenas H'
		// G apply to columns of internalState:m[blake2b_sigma[round][2 * blockPos]] /+1
	    G(m[blake2b_sigma[vIndex][0]], m[blake2b_sigma[vIndex][1]], 0,4,8,12); 
	    G(m[blake2b_sigma[vIndex][2]], m[blake2b_sigma[vIndex][3]], 1,5,9,13); 
	    G(m[blake2b_sigma[vIndex][4]], m[blake2b_sigma[vIndex][5]], 2,6,10,14); 
	    G(m[blake2b_sigma[vIndex][6]], m[blake2b_sigma[vIndex][7]], 3,7,11,15); 
	    // G apply to diagonals of internalState:
	    G(m[blake2b_sigma[vIndex][8]], m[blake2b_sigma[vIndex][9]], 0,5,10,15); 
	    G(m[blake2b_sigma[vIndex][10]], m[blake2b_sigma[vIndex][11]], 1,6,11,12); 
	    G(m[blake2b_sigma[vIndex][12]], m[blake2b_sigma[vIndex][13]], 2,7,8,13); 
	    G(m[blake2b_sigma[vIndex][14]], m[blake2b_sigma[vIndex][15]], 3,4,9,14); 
	    
		// update chain values: 
		for( int offset = 0; offset < 8; offset++ ) {
			chainValue[offset] = chainValue[offset] ^ internalState[offset] ^ internalState[offset + 8];	
		}
	}
	

	private void G(long m1, long m2, int posA, int posB, int posC, int posD) {

		internalState[posA] = internalState[posA] + internalState[posB] + m1; 
	    internalState[posD] = rotr64(internalState[posD] ^ internalState[posA], 32); 
	    internalState[posC] = internalState[posC] + internalState[posD]; 
	    internalState[posB] = rotr64(internalState[posB] ^ internalState[posC], 24); // replaces 25 of BLAKE
	    internalState[posA] = internalState[posA] + internalState[posB] + m2; 
	    internalState[posD] = rotr64(internalState[posD] ^ internalState[posA], 16); 
	    internalState[posC] = internalState[posC] + internalState[posD]; 
	    internalState[posB] = rotr64(internalState[posB] ^ internalState[posC], 63); // replaces 11 of BLAKE
	}
	
	private long rotr64(long x, int rot) {
		return x >>> rot | (x << (64 - rot));
	}
	

	public String getName(){
		return "Catena_Blake2b_1";
	}
	/**
	 * This function is used for password hashing scheme
	 * Catenas round-reduced version H'
	 */
	public void setVertexIndex(int _vIndex) {
		vIndex = _vIndex;
	}
	// convert one long value in byte array
	// little-endian byte order!
	public final static byte[] long2bytes(long longValue) {
	    return new byte[] {	        
		    (byte) longValue,
	        (byte) (longValue >> 8),
	        (byte) (longValue >> 16),
	        (byte) (longValue >> 24),
	        (byte) (longValue >> 32),
	        (byte) (longValue >> 40),
	        (byte) (longValue >> 48),
	        (byte) (longValue >> 56)};
	}
	// little-endian byte order!
	public final static long bytes2long(byte[] byteArray, int offset) {
	      
	      return (	    		  
				  ((long) byteArray[offset] & 0xFF ) |
				  (((long) byteArray[offset + 1] & 0xFF ) << 8) |
				  (((long) byteArray[offset + 2] & 0xFF ) << 16) |
				  (((long) byteArray[offset + 3] & 0xFF ) << 24) |
			      (((long) byteArray[offset + 4] & 0xFF ) << 32) |
			      (((long) byteArray[offset + 5] & 0xFF ) << 40) |
			      (((long) byteArray[offset + 6] & 0xFF ) << 48) |
			      (((long) byteArray[offset + 7] & 0xFF ) << 56) ) ;  	    			    		  
	}


	@Override
	public String getAlgorithmName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getDigestSize() {
		// TODO Auto-generated method stub
		return 0;
	}	
}
