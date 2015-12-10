package cologne.eck.peafactory.crypto.kdf;

/**
 * @author Axel von dem Bruch
 */

/*
 * Key Derivation Mode of Password Hashing Scheme Pomelo
 * related to the Paper v3
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



/**
 * Pomelo was designed by Hongjun Wu.
 * It is one of the nine finalists of the Password Hashing Competition. 
 * Submission paper: "POMELO: A Password Hashing Algorithm (Version 2)".
 *  
 * Paper and C reference implementation can be found at:
 * https://password-hashing.net/submissions/POMELO-v2.tar.gz
 * and also:
 * http://www3.ntu.edu.sg/home/wuhj/research/pomelo/
 * 
 * Version Pomelo v3 does not change the code of Pomelo except the value 
 * of S8[389] (here: S[48]) is set to 1 for the newly introduced
 * key derivation mode. 
 */

/*
	Recommended parameters:  recommend 5 ≤ m cost + t cost ≤ 25
	
	66 additional input bytes are reserved for the extension of the algorithm
 	(such as the inclusion of secret key):  
 	64 bytes can be loaded in the state between salt and in.length in step 2, 
 	2 bytes between outlen and the fibonacci sequence. 
 */

import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;

import java.util.Arrays;

public class PomeloKDF extends KeyDerivation {

	    private int i0, i1,i2,i3,i4;
	    private long [] S; // 2 ^ (13 + m_cost) (byte)
	    private int mask, mask1;
	    private int state_size;
	    
	    private long random_number; 
	    private int index_global, index_local;

	    private long temp;    
	    
		// if set true: password is filled immediately: 
	    private boolean wipePassword = false;
	    
	    private static int memoryCost = 15;
	    private static int timeCost = 0;
	    
	    public PomeloKDF() {
	    	
	    	//System.out.println("t: " + timeCost + "  m: " + memoryCost);
	    	// DEFAULT WAS CHANGED!
	    	this.wipePassword = true;
	    	setmCost(memoryCost);
	    	settCost(timeCost);
	    	
			setArg3(0);
			setArg4(0);
			setArg5(0);
			setArg6(0);
			setVersionString("");
			setKdf(this);
	    }
	    public PomeloKDF (boolean _wipe) {

	    	this.wipePassword = _wipe;
	    	setmCost(memoryCost);
	    	settCost(timeCost);	    	
	    	
			setArg3(0);
			setArg4(0);
			setArg5(0);
			setArg6(0);
			setVersionString("");
			setKdf(this);
	    }
	    
		byte[] output = new byte[256];
	    
	    public byte[] deriveKey(int keylen, byte[] pwd, byte[] salt, int t_cost, int m_cost) {
	    	 
			// length > 2^64 will result anyway in an exception, so no check

	    	byte[] key = new byte[keylen];// return value
	    	
	    	// first run Pomelo with pwd in KDF mode (bit set in S[48]):
	    	try {
				output = hashPassword(256, pwd, salt, t_cost, m_cost);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	// copy second half of output into the key
	    	System.arraycopy(output, 128, key, 0, (keylen > 128) ? 128 : keylen);
	    	
	    	// Perform iterations if necessary:
	    	if (keylen > 128) {
	    		boolean originalWipe = wipePassword;
	    		wipePassword = true;// this will overwrite the temporary output
	    		
	        	int keylen128 = keylen - keylen%128;
	            for (int i = 128; i < keylen128; i += 128) {
	            	try {
	    				output = hashPassword(256, output, salt, t_cost, m_cost);
	    			} catch (Exception e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            	// copy second half of output into the key
	            	System.arraycopy(output, 128, key, i, 128);
	            }
	            // perform the rest:
	            if (keylen % 128 != 0) {
	            	int remainder = keylen % 128;
	            	try {
	    				output = hashPassword(256, output, salt, t_cost, m_cost);
	    			} catch (Exception e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            	// copy the rest into the key
	            	System.arraycopy(output, 128, key, keylen - remainder, remainder);
	            }
	            wipePassword = originalWipe;
	    	}
	    	Arrays.fill(output, (byte) 0);
	    	if (output[output.length - 1] != 0) {
	    		System.err.println("Zerization failed");
	    	}
	    	return key;
	    }

		public byte[] hashPassword(int outlen, byte[] pwd, byte[] salt, int t_cost,
				int m_cost, Object... varArgs) throws Exception {

		    // check the size of password, salt and output: 
			// Note: the salt.length from paper and reference implementation differs from the comment in the
			// reference implementation: "the salt is at most 32 bytes". This seems to be mistake in the 
			// comment, because in the code and the paper salt.length is at most 64 and the length of the
			// additional bytes (64 + 3) let also assume a salt.length of 64
		    if (pwd.length > 256 
		    		|| salt.length > 64 
		    		|| pwd.length < 0 || salt.length < 0 || outlen < 0) {
				throw new IllegalArgumentException("illegal length parameters");	    	
		    }
		    //  0 ≤ m cost ≤ 25
		    if (m_cost < 0 || m_cost > 25) {
				throw new IllegalArgumentException("illegal memory cost parameter, requird: 0 - 25");	    	
		    }
		    //  0 ≤ t cost ≤ 25
		    if (t_cost < 0 || t_cost > 25) {
				throw new IllegalArgumentException("illegal time cost cost parameter, requird: 0 - 25");	    	
		    }
		    
		    //  recommend 5 ≤ m cost + t cost ≤ 25
		    if (t_cost + m_cost < 5 || t_cost + m_cost > 25 ) {
		    	System.err.println("Warning: Not recommended parameters, recommended: 5 ≤ m cost + t cost ≤ 25");
		    }


		    //Step 1: Initialize the state S
		    state_size = 1 << (13 + m_cost );    // state size is 2**(13+m_cost) bytes
		    S = new long[ state_size / 8 ];
		    mask  = (1 << (8+m_cost))  - 1;   // mask is used for modulation: modulo size_size/32;
		    mask1 = (1 << (10+m_cost)) - 1;   // mask is used for modulation: modulo size_size/8;


		    //Step 2:  Load the password, salt, input/output/salt sizes into the state S	 
		    // password:
		    int sIndex = pwd.length / 8 ;
			for (int i = 0; i < sIndex; i++) {
				S[i] = 
				  ((long)(pwd[i * 8 + 7] & 0xff) << 56) |
			      ((long)(pwd[i * 8 + 6] & 0xff) << 48) |
			      ((long)(pwd[i * 8 + 5] & 0xff) << 40) |
			      ((long)(pwd[i * 8 + 4] & 0xff) << 32) |
			      ((long)(pwd[i * 8 + 3] & 0xff) << 24) |
			      ((long)(pwd[i * 8 + 2] & 0xff) << 16) |
			      ((long)(pwd[i * 8 + 1] & 0xff) << 8) |
			      ((long)(pwd[i * 8 + 0] & 0xff));			
			}
			// incomplete long value if in.length % 8 != 0
			int shift = 0;
			for (int i = sIndex * 8; i < pwd.length; i++) {
				S[sIndex] |= ((long) (pwd[i] & 0xFF) << shift);
				shift += 8;
			}				
			if(wipePassword == true) {
				Arrays.fill(pwd,  (byte) 0);
			}
			// salt: 
			if (shift == 64) {
				shift = 0;
			}
			// complete long value if in.length % 8 != 0
			// with first values of salt:
			for (int saltIndex = 0; saltIndex < salt.length; saltIndex++) {
				S[sIndex] |= ((long) (salt[saltIndex] & 0xFF) << shift);
				shift += 8;
				if (shift == 64) { // long value completed
					sIndex++;
					shift = 0;
				}
			}	

			//============= 64 bytes are reserved here for extensions: ==========
		    /* int addLen = addBytesLen / 8;
			for (int i = 40; i < addLen; i++) {
				S[i] = 
				  ((long)(addBytes[i * 8 + 7] & 0xff) << 56) |
			      ((long)(addBytes[i * 8 + 6] & 0xff) << 48) |
			      ((long)(addBytes[i * 8 + 5] & 0xff) << 40) |
			      ((long)(addBytes[i * 8 + 4] & 0xff) << 32) |
			      ((long)(addBytes[i * 8 + 3] & 0xff) << 24) |
			      ((long)(addBytes[i * 8 + 2] & 0xff) << 16) |
			      ((long)(addBytes[i * 8 + 1] & 0xff) << 8) |
			      ((long)(addBytes[i * 8 + 0] & 0xff));			
			}
			// incomplete long value if addByteslen % 8 != 0
			shift = 0;
			for (int i = sIndex * 8; i < inlen; i++) {
				S[sIndex] |= ((long) (addBytes[i] & 0xFF) << shift);
				shift += 8;
			}*/
			//===============================================================
			
			// sizes
			S[48] =   ((long)(pwd.length & 0xff) << 0) |
				      ((long)((pwd.length >>> 8) & 0xff) << 8) |
				      ((long)(salt.length & 0xff) << 16) |
				      ((long)(outlen & 0xff) << 24) |
				      ((long)((outlen >>> 8) & 0xff) << 32);	
			// for key derivation mode: 
			S[48] |= 0x0000010000000000L;

			
			//=========== two more bytes for extensions: ==================
			/*  S[48] |=   
			  		  (
				      ((long)(val2 & 0xff) << 48) |
				      ((long)(val3 & 0xff) << 56); */
			//================================================================
			
			//introducing random constants to the state using Fibonacci sequence:
			// The Fibonacci sequence is sufficient for preventing the symmetry structure of
			// the hashing algorithm being exploited in an attack.
			sIndex = 49;
			S[sIndex] = ((long)(1 & 0xff) << 8) |
				      ((long)(1 & 0xff) << 0);
			shift = 16;
		    int fib1 = 1;
		    int fib2 = 1;
		    int sum = 0;
			for (int fibonacci = 0; fibonacci < 22; fibonacci++) {
				sum = fib1 + fib2;
				S[sIndex] |= ((long) ( (fib1 + fib2 ) & 0xFF) << shift);
				fib2 = fib1;
				fib1 = sum;
				shift += 8;
				if (shift == 64) { // long value completed
					sIndex++;
					shift = 0;
				}
			}	
			
		    //Step 3: Expand the data into the whole state.
			for (int i = 13*4; i < (1 << (10+m_cost)); i=i+4) {
				F0(i);
			}
			
		    //Step 4: Update the state using function G
		    // password-independent random memory access
			// not affected by the cache-timing side-channel attack
		    random_number = 123456789L;
		    for (int i = 0; i < (1 << (9 + m_cost + t_cost)); i = i + 128) {
			    	G(i);
		    }	

		    //Step 5: Update the state using function H
		    // password-dependent random memory access
			// affected by the cache-timing side-channel attack
		    for (int i = 1 << (9+m_cost+t_cost);  i < (1L << (10+m_cost+t_cost)); i=i+128){
		    	H(i);
		    }

		    //Step 6: Update the state using function F
		    for (int i = 0; i < (1L << (10+m_cost)); i=i+4){
		    	F(i);
		    }	    

		    //Step 7: generate the output
		    // The hash output is given as the last t bytes of the state S (t <= 256)
			byte[] out = new byte[outlen];
			int outlenLong = outlen / 8;
			int mod = outlen % 8;
			
			for ( int i = S.length - outlenLong, j = 0; i < S.length; i++, j++) {
				out[mod + j * 8 + 7] = (byte) (S[i] >>> 56);
				out[mod + j * 8 + 6] = (byte) (S[i] >>> 48);
				out[mod + j * 8 + 5] = (byte) (S[i] >>> 40);
				out[mod + j * 8 + 4] = (byte) (S[i] >>> 32);
				out[mod + j * 8 + 3] = (byte) (S[i] >>> 24);
				out[mod + j * 8 + 2] = (byte) (S[i] >>> 16);
				out[mod + j * 8 + 1] = (byte) (S[i] >>> 8);
				out[mod + j * 8 + 0] = (byte) (S[i] >>> 0);
			}
			shift = 64 - mod * 8;
			sIndex = S.length - (outlenLong + 1);
			for (int i = 0; i < mod; i++) {
				out[i] = (byte) (S[sIndex] >>> shift);
				shift += 8;
			}

		    Arrays.fill(S, 0L);	        	
			// prevent dead code eliminations (compiler optimizations):
			if (S[ state_size / 8 -1] != 0) {
				System.err.print("zeroization failed!");
			}
		    return out;
		}
		
		// state update function F: 
		private void F(int i)  {                
		    i0 = ((i) - 0*4)  & mask1; 
		    i1 = ((i) - 2*4)  & mask1; 
		    i2 = ((i) - 3*4)  & mask1; 
		    i3 = ((i) - 7*4)  & mask1; 
		    i4 = ((i) - 13*4) & mask1; 
		    S[(i0+0)] += ((S[(i1+0)] ^ S[ (i2+0)]) + S[(i3+0)]) ^ S[(i4+0)];         
		    S[(i0+1)] += ((S[(i1+1)] ^ S[(i2+1)]) + S[(i3+1)]) ^ S[(i4+1)];         
		    S[(i0+2)] += ((S[(i1+2)] ^ S[(i2+2)]) + S[(i3+2)]) ^ S[(i4+2)];         
		    S[(i0+3)] += ((S[(i1+3)] ^ S[(i2+3)]) + S[(i3+3)]) ^ S[(i4+3)];         
		    temp = S[(i0+3)];         
		    S[(i0+3)] = S[(i0+2)];      
		    S[(i0+2)] = S[(i0+1)];      
		    S[(i0+1)] = S[(i0+0)];      
		    S[(i0+0)] = temp;         
		    S[(i0+0)] = (S[(i0+0)] << 17) | (S[(i0+0)] >>> 47);  
		    S[(i0+1)] = (S[(i0+1)] << 17) | (S[(i0+1)] >>> 47);  
		    S[(i0+2)] = (S[(i0+2)] << 17) | (S[(i0+2)] >>> 47);  
		    S[(i0+3)] = (S[(i0+3)] << 17) | (S[(i0+3)] >>> 47);  
		}
		// update function to fill S
		private void F0(int i)  {               
		    i0 = ((i) - 0*4)  & mask1; 
		    i1 = ((i) - 2*4)  & mask1; 
		    i2 = ((i) - 3*4)  & mask1; 
		    i3 = ((i) - 7*4)  & mask1; 
		    i4 = ((i) - 13*4) & mask1; 
		    S[i0+1] = ((S[i1+0] ^ S[i2+0]) + S[i3+0]) ^ S[i4+0];         
		    S[i0+2] = ((S[i1+1] ^ S[i2+1]) + S[i3+1]) ^ S[i4+1];         
		    S[i0+3] = ((S[i1+2] ^ S[i2+2]) + S[i3+2]) ^ S[i4+2];         
		    S[i0+0] = ((S[i1+3] ^ S[i2+3]) + S[i3+3]) ^ S[i4+3];         
		    S[i0+0] = (S[i0+0] << 17) | (S[i0+0] >>> 47);  
		    S[i0+1] = (S[i0+1] << 17) | (S[i0+1] >>> 47);  
		    S[i0+2] = (S[i0+2] << 17) | (S[i0+2] >>> 47);  
		    S[i0+3] = (S[i0+3] << 17) | (S[i0+3] >>> 47); 
		}	
		// cache-timing attack resistant update of the state S:
		private void G(int i){                                                      
		    index_global = (int) (((random_number >>> 16) & mask) << 2);                             
		    for (int j = 0; j < 128; j = j+4)                                                   
		    {                                                                               
		        F(i+j);                                                                     
		        index_global   = (index_global + 4) & mask1;                                      
		        index_local    = (((i + j) >>> 2) - 0x1000 + ( (int) random_number & 0x1fff)) & mask;     
		        index_local    = index_local << 2;                                                
		        S[(i0+0)]       += (S[index_local+0] << 1);                                   
		        S[(i0+1)]       += (S[index_local+1] << 1);                                   
		        S[(i0+2)]       += (S[index_local+2] << 1);                                   
		        S[(i0+3)]       += (S[index_local+3] << 1);                                   
		        S[index_local+0] += (S[(i0+0)] << 2); 
		        S[index_local+1] += (S[(i0+1)] << 2); 
		        S[index_local+2] += (S[(i0+2)] << 2); 
		        S[index_local+3] += (S[(i0+3)] << 2); 
		        S[(i0+0)]       += (S[index_global+0] << 1);                                   
		        S[(i0+1)]       += (S[index_global+1] << 1);                                   
		        S[(i0+2)]       += (S[index_global+2] << 1);                                   
		        S[(i0+3)]       += (S[index_global+3] << 1);                                   
		        S[index_global+0] += (S[(i0+0)] << 3); 
		        S[index_global+1] += (S[(i0+1)] << 3); 
		        S[index_global+2] += (S[(i0+2)] << 3); 
		        S[index_global+3] += (S[(i0+3)] << 3); 
		        // update of random_number independent of password, 
		        // so this function is not affected by cache-timing attacks
		        random_number += (random_number << 2);                                      
		        random_number  = (random_number << 19) ^ (random_number >>> 45)  ^ 3141592653589793238L;   
		    }                                                                               
		}
		// cache-timing attack vulnerable update of the state S:
		private void H(int i){                                                      
		    index_global = (int) (((random_number >>> 16) & mask) << 2);                             
		    for (int j = 0; j < 128; j = j+4)                                                   
		    {                                                                               
		        F(i+j);                                                                     
		        index_global   = (index_global + 4) & mask1;                                      
		        index_local    = (int) ((((i + j) >>> 2) - 0x1000 + (random_number & 0x1fff)) & mask);     
		        index_local    = index_local << 2;                                                
		        S[(i0+0)]       += (S[index_local+0] << 1);                                   
		        S[(i0+1)]       += (S[index_local+1] << 1);                                   
		        S[(i0+2)]       += (S[index_local+2] << 1);                                   
		        S[(i0+3)]       += (S[index_local+3] << 1);                                   
		        S[index_local+0] += (S[(i0+0)] << 2); 
		        S[index_local+1] += (S[(i0+1)] << 2); 
		        S[index_local+2] += (S[(i0+2)] << 2); 
		        S[index_local+3] += (S[(i0+3)] << 2); 
		        S[(i0+0)]       += (S[index_global+0] << 1);                                   
		        S[(i0+1)]       += (S[index_global+1] << 1);                                   
		        S[(i0+2)]       += (S[index_global+2] << 1);                                   
		        S[(i0+3)]       += (S[index_global+3] << 1);                                   
		        S[index_global+0] += (S[(i0+0)] << 3); 
		        S[index_global+1] += (S[(i0+1)] << 3); 
		        S[index_global+2] += (S[(i0+2)] << 3); 
		        S[index_global+3] += (S[(i0+3)] << 3); 
		        // update of random_number with value of S which depends on the password, 
		        // so this function is affected by cache-timing attacks
		        random_number  = S[i3];              
		    }                                        
		}


		
		/**
		 * indicates if zeroization of password is performed or not
		 * 
		 * @return the wipePassword value
		 */
		public boolean isWipePassword() {
			return wipePassword;
		}

		/**
		 * zeroize the password or keep it
		 * 
		 * @param _wipe 
		 * 					true: wipe the password as soon as 
		 * 					possible 
		 * 					false: keep it for later use
		 */
		public void setWipePassword(boolean _wipe) {
			this.wipePassword = _wipe;
		}


	

	@Override
	public byte[] deriveKey(byte[] pswMaterial) {
		
		int outlen = CipherStuff.getKeySize();
		wipePassword = true;
		byte[] result = null;

		try {
			result = hashPassword(outlen, pswMaterial, getSalt(), gettCost(),
					getmCost() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	printInfos(true);
		return result;
	}

	@Override
	public String getName() {
		return "PomeloKDF";
	}
	/**
	 * @return the memoryCost
	 */
	public static int getMemoryCost() {
		return memoryCost;
	}
	/**
	 * @param memoryCost the memoryCost to set
	 */
	public static void setMemoryCost(int memoryCost) {
		PomeloKDF.memoryCost = memoryCost;
	}
	/**
	 * @return the timeCost
	 */
	public static int getTimeCost() {
		return timeCost;
	}
	/**
	 * @param timeCost the timeCost to set
	 */
	public static void setTimeCost(int timeCost) {
		PomeloKDF.timeCost = timeCost;
	}
}
