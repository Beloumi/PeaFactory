package cologne.eck.peafactory.crypto.kdf;


/**
 * This implementation refers to: 
 * Paper v3.2 and from reference implementation 2015-08-11
 */


/*
 * Password Hashing Scheme Catena: Instance Catena-Butterfly (v3.2)
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


public class CatenaDBG extends Catena {

	private final static String VERSION_ID = "Butterfly";
	private final static String VERSION_ID_FULL = "Butterfly-Full";
	private final static int LAMBDA = 4; //  λ (depth of F)
	private final static int GARLIC = 16; // defines time and memory requirements
	private final static int MIN_GARLIC = 16; // minimum garlic

	
	/**
	 * Default constructor. 
	 * Uses round-reduced hash function 
	 * and does not clear the password
	 */
	public CatenaDBG() {
		setVersionID(VERSION_ID);		
		setFast(true);
	}
	
	/**
	 * Constructor for round-reduced hash function
	 * 
	 * @param fast	if true, use round-reduced 
	 * 				hash function for some computations
	 */
	public CatenaDBG(boolean fast) {

		setFast(fast);
		if (fast == false) {
			setVersionID(VERSION_ID_FULL);
		} else {
			setVersionID(VERSION_ID);
		}
	}
	
	/**
	 * Constructor for round-reduced hash function 
	 * and clearing the password
	 * 
	 * 
	 * @param fast			if true, use round-reduced 
	 * 						hash function for some computations
	 * @param overwrite		if true, clear password as soon 
	 * 						as possible
	 */
	public CatenaDBG(boolean fast, boolean overwrite) {

		setFast(fast);
		setOverwrite(overwrite);
		if (fast == false) {
			setVersionID(VERSION_ID_FULL);
		} else {
			setVersionID(VERSION_ID);
		}
	}
	
	@Override
	protected void gamma(int garlic, byte[] salt, byte[] r) {
		saltMix(garlic, salt, r);		
	}
	
	@Override
	protected void phi(byte[] r) {}
	
	@Override
	protected void F(byte[] r, int garlic, int lambda, byte[] h) {
		DBG(r, garlic, lambda, h);
	}
	
	
	long sigma(int g, int i, long j) {
	  if (i < g) {
	    return (j ^ (1L << (g-1-i))); //diagonal front
	  }
	  else {
	    return (j ^ (1L << (i-(g-1)))); //diagonal back
	  }
	}

	long idx(long i, long j, int co, long c, long m) {
		i += co;
		if (i % 3 == 0) {
			return j;
		} else if (i % 3 == 1) {
			if(j < m) { //still fits in the array
				return j + c;
			} else{ //start overwriting elements at the beginning
				return j - m;
			}
		} else { //i % 3 == 2
			return j + m;
		}
	}
	
	private void DBG(byte[] r, int garlic, int lambda, byte[] h) {
		
		byte[] tmp = new byte[H_LEN];
		long i,j;
		int k;
		int co = 0; //carry over from last iteration		  

		long c = 1L << garlic;
		long m = 1L << (garlic-1); //0.5 * 2^g
		int l = 2 * garlic;
		
		for (k = 0; k < lambda; k++) {

			byte[] tmp3 = new byte[H_LEN];				  	
					  
			for(i = 1; i < l; i++) {

				XOR(r, (int) idx(i-1,c-1,co,c,m) * H_LEN, 
						r, (int) idx(i-1,0,co,c,m) * H_LEN, 
						tmp);

				update(tmp);

				 System.arraycopy(r, (int) idx(i-1, sigma(garlic,(int) (i-1),0),co,c,m) * H_LEN, 
						tmp3, 0, 
						H_LEN);
				update(tmp3);
				digest.doFinal(r, (int) idx(i,0,co,c,m) * H_LEN);
				digest.reset();	    	
				  
				if (reducedDigest != null){
					reducedDigest.reset();
				}

			    for(j = 1; j < c; j++){

			    	XOR(r, (int) idx(i,j-1,co,c,m)*H_LEN, 
						r, (int) idx(i-1,j,co,c,m)*H_LEN,
						tmp);

			    	System.arraycopy(r, (int) idx(i-1,sigma(garlic,(int) (i-1),j),co,c,m)*H_LEN, 
						tmp3, 0, 
						H_LEN);
					hashFast((int)j, tmp, 0, tmp3, 0, r, ((int) idx(i,j,co,c,m) * H_LEN));
			    }
			}			    
			co = (int) ((co + (i-1)) % 3);
		}			  
		System.arraycopy(r, (int) idx(0,c-1,co,c,m) * H_LEN, h, 0, H_LEN);

		Arrays.fill(tmp,  (byte) 0);
		Arrays.fill(r,  (byte) 0);
	}

	/**
	 * XOR two vectors and store the result in a vector
	 * 
	 * @param input1	first input vector 
	 * @param index1	index of first vector to start
	 * @param input2	second input vector
	 * @param index2	index of second vector to start
	 * @param output	vector to store the result
	 */
	private void XOR(byte[] input1, int index1, byte[] input2, int index2, byte[] output) {
	  int i;
	  for(i = 0; i < H_LEN; i++){
	    output[i] = (byte) (input1[index1 + i] ^ input2[index2 + i]);
	  }
	}
	
	@Override
	public int getGarlic() {
		return GARLIC;
	}
	@Override
	public int getMinGarlic() {
		return MIN_GARLIC;
	}
	@Override
	public int getLambda() {
		return LAMBDA;
	}
	@Override
	public String getAlgorithmName() {
		return versionID;
	}

	@Override
	public byte[] hashPassword(int outlen, byte[] in, byte[] salt, int t_cost,
			int m_cost, Object... varArgs) throws Exception {
		byte[] hash = new byte[outlen];
		hashPassword(
				in, salt, null,  
			    t_cost, m_cost, m_cost,// lambda, minGarlic, garlic  
			    hash);
		return hash;
	}

	@Override
	public boolean isWipePassword() {
		return overwrite;
	}

	@Override
	public void setWipePassword(boolean _wipe) {
		overwrite = true;
	}
}
