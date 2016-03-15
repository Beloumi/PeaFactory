package cologne.eck.peafactory.crypto.kdf;

/**
 * This implementation refers to: 
 * Paper v3.2 and from reference implementation 2015-08-11
 */

/*
 * Password Hashing Scheme Catena: Instance Catena-Dragonfly (v3.2)
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


public class CatenaBRG extends Catena{
	
	private final static String VERSION_ID = "Dragonfly";
	private final static String VERSION_ID_FULL = "Dragonfly-Full";
	private final static int LAMBDA = 2;//  λ (depth of F)
	private final static int GARLIC = 21;// defines time and memory requirements
	private final static int MIN_GARLIC = 21;// minimum garlic	


	/**
	 * Default constructor. 
	 * Uses round-reduced hash function 
	 * and does not clear the password
	 */
	public CatenaBRG() {
		setVersionID(VERSION_ID);
	}
	
	/**
	 * Constructor for round-reduced hash function
	 * 
	 * @param fast	if true, use round-reduced 
	 * 				hash function for some computations
	 */
	public CatenaBRG(boolean fast) {

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
	public CatenaBRG(boolean fast, boolean overwrite) {

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
		BRG(r, garlic, lambda, h);
	}

	private void BRG(byte[] r, int garlic, int lambda, byte[] h) {
		
		int c = 1 << garlic;
		  
		for (int k = 0; k < lambda; k++) {

			digest.update(r, (c - 1) * H_LEN, H_LEN);
			digest.update(r, 0, H_LEN);
			digest.doFinal(r, 0);
			digest.reset();
			  
			if (reducedDigest != null){
				reducedDigest.reset();
			}
			//fastDigest.reset();

			byte[] previousR = new byte[H_LEN];
			System.arraycopy(r, 0, previousR, 0, H_LEN);
			    
			for (long i = 1; i < c; i++) {
		    	hashFast((int)i, previousR, 0, r, Math.abs((int) reverse(i, garlic) * H_LEN), r, (int)reverse(i, garlic) * H_LEN);
		    	System.arraycopy( r, (int)reverse(i, garlic) * H_LEN,  previousR,  0,  H_LEN);
			}
		    k++;
		    if (k >= lambda) {
		      break;
		    }
			digest.update(r, (c - 1) * H_LEN, H_LEN);
			digest.update(r, 0, H_LEN);
			digest.doFinal(r, 0);
			digest.reset();
		    
			if (reducedDigest != null){
				reducedDigest.reset();
			}

		    int pIndex = 0;
		    for (int i = 1; i < c; i++, pIndex += H_LEN) {
		    	hashFast( i, r, pIndex, r, pIndex + H_LEN, r, pIndex + H_LEN);
		    }
		}
		System.arraycopy(r, (c - 1) * H_LEN, h, 0, H_LEN);
		Arrays.fill(r,  (byte) 0);
	}
	
	
	private long byteSwap(long x) {
	    return  ((((x) & 0xff00000000000000L) >>> 56)				      
	    	      | (((x) & 0x00ff000000000000L) >>> 40)				      
	    	      | (((x) & 0x0000ff0000000000L) >>> 24)				      
	    	      | (((x) & 0x000000ff00000000L) >>> 8)				      
	    	      | (((x) & 0x00000000ff000000L) << 8)				      
	    	      | (((x) & 0x0000000000ff0000L) << 24)				      
	    	      | (((x) & 0x000000000000ff00L) << 40)				      
	    	      | (((x) & 0x00000000000000ffL) << 56));
	}
	
	/* Return the reverse bit order of x where x is interpreted as n-bit value */
	private long reverse(long x, int n) {

	  x = byteSwap(x);
	  x = ((x & (0x0f0f0f0f0f0f0f0fL)) <<  4) |
	      ((x & (0xf0f0f0f0f0f0f0f0L)) >>> 4);
	  x = ((x & (0x3333333333333333L)) <<  2) |
	      ((x & (0xccccccccccccccccL)) >>> 2);
	  x = ((x & (0x5555555555555555L)) <<  1) |
	      ((x & (0xaaaaaaaaaaaaaaaaL)) >>> 1);
	  return x >>> (64 - n);
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
