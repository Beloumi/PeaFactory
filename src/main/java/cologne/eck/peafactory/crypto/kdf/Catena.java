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

/**
 * Password Hashing Scheme Catena (v3) version v3.2
 */


import java.util.Arrays;

import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.bouncycastle.crypto.Digest;


public abstract class Catena implements PasswordHashingScheme {

	
	protected Digest digest;// = new Blake2b();
	protected ReducedDigest reducedDigest;// = new Catena_Blake2b_1();
	
	private boolean fast = true; // use round-reduced versions or not	

	// Values independent on instance:
	protected final static int H_LEN = 64;
	private final static int KEY_LEN = 16;

	protected String versionID; // V / version identifier
	// possible values: "Dragonfly", Dragonfly-Full", Butterfly", Butterfly-Full"
	
	// mode (d / domain) values: 
	private static final int PASSWORD_HASHING_MODE = 0;// "PASSWORD_SCRAMBLER"
	private static final int KEY_DERIVATION_MODE = 1;
//	private static final int PROOF_OF_WORK_MODE = 2;

	// Server Relief values: 
	public final static int REGULAR = 0;
	private final static int CLIENT = 1;
	
	// true = clear the password as soon as possible
	protected boolean overwrite = true;
	
	/**
	 * Default constructor. 
	 * Uses round-reduced hash function 
	 * and does not clear the password
	 */
	public Catena() {
	}
	
	/**
	 * Constructor for round-reduced hash function
	 * 
	 * @param fast	if true, use round-reduced 
	 * 				hash function for some computations
	 */
	public Catena(boolean fast) {
		setFast(fast);
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
	public Catena(boolean fast, boolean overwrite) {
		setFast(fast);
		setOverwrite(overwrite);
	}
	
	
	/**
	 * Use Catena with default cost parameters
	 * 
	 * @param pwd	the password
	 * @param salt	the salt
	 * @param data	the associated data or null
	 * @param hash	holds the resulting hash value
	 * 	 			the length of this vector indicates 
	 * 				the length of the resulting hash value
	 */
	public void hashPassword(
			byte[] pwd, byte[] salt, byte[] data, 
			byte[] hash) {
		
		catena(pwd, salt, data, 
		getLambda(), getMinGarlic(), getGarlic(), hash.length,
		REGULAR, PASSWORD_HASHING_MODE, hash);
	}
	
	/**
	 * Catena password hashing with cost parameters
	 * 
	 * @param pwd			the password
	 * @param salt			the salt
	 * @param data			the associated data or null
	 * @param lambda		the depth of the graph
	 * @param min_garlic	the min. Garlic
	 * @param garlic		the cost parameter
	 * @param hash			holds the resulting hash value, 
	 * 						the length of this vector indicates 
	 * 						the length of the resulting hash value
	 */
	public void hashPassword(
			byte[] pwd, byte[] salt, byte[] data,  
		     int lambda, int min_garlic, int garlic, 	    
		     byte[] hash) {
		
		catena(pwd, salt, data, 
				lambda, garlic, garlic, hash.length,
				REGULAR, PASSWORD_HASHING_MODE, hash);
	}
	
	/**
	 * Catena with arguments from reference implementation 
	 * 
	 * @param pwd			the password
	 * @param salt			the salt
	 * @param data			the associated data or null
	 * @param lambda		the depth of the graph
	 * @param min_garlic	the min. Garlic
	 * @param garlic		the cost parameter
	 * @param hashlen		the length of the resulting hash value
	 * @param _client		indicates if Catena uses server relief or not
	 * @param tweak_id		the mode to run: PASSWORD_HASHING_MODE 
	 * 						or KEY_DERIVATION_MODE 
	 * @param hash			holds the resulting hash value, 
	 * 						the length of this vector indicates 
	 * 						the length of the resulting hash value
	 */
	public void catena(
			 byte[] pwd, byte[] salt, byte[] data,  
		     int lambda, int  min_garlic, int garlic, int  hashlen,
		     int _client, int  tweak_id, 
		     byte[] hash) {
		_catena(
				 pwd, salt, salt, data,  
			     lambda, min_garlic, garlic, 
			     _client, tweak_id, 
			     hash);
	}
	
	/**
	 * Catena with arguments from reference implementation and a public input 
	 * 
	 * @param pwd			the password
	 * @param salt			the salt
	 * @param publicInput	the public input for the randomization layer 
	 * 						if the salt is kept secret
	 * @param data			the associated data or null
	 * @param lambda		the depth of the graph
	 * @param min_garlic	the min. Garlic
	 * @param garlic		the cost parameter
	 * @param hashlen		the length of the resulting hash value
	 * @param _client		indicates if Catena uses server relief or not
	 * @param tweak_id		the mode to run: PASSWORD_HASHING_MODE 
	 * 						or KEY_DERIVATION_MODE 
	 * @param hash			holds the resulting hash value, 
	 * 						the length of this vector indicates 
	 * 						the length of the resulting hash value
	 */
	public void _catena(
			 byte[] pwd, byte[] salt, byte[] publicInput, byte[] data,  
		     int lambda, int  min_garlic, int garlic,
		     int _client, int  tweak_id, 
		     byte[] hash) {
		
		int hashlen = hash.length;

		if((hashlen > H_LEN) || (garlic > 63) || (min_garlic > garlic) || 
				(lambda == 0)){
			throw new IllegalArgumentException("illegal argument for __Catena");
		}
		
		if (publicInput == null && salt != null) {
			publicInput = salt;
		}

		initDigests();

		byte[] x = new byte[H_LEN];//hashlen];
		byte[] hv = new byte[H_LEN];//[hashlen];
		byte[] t = new byte[4];
		int c;


		// Compute H(V)
		update(versionID.getBytes() );// Encoding?
		digest.doFinal(hv, 0);
		digest.reset();

		// Compute Tweak 
		t[0] = (byte)tweak_id;
		t[1] = (byte)lambda;
		t[2] = (byte)hashlen;
		int saltlen = 0;
		if (salt != null){
			saltlen = salt.length;
		}
		t[3] = (byte)saltlen;

		// Compute H(AD) 
		update(data);
		digest.doFinal(x, 0);
		digest.reset();

		// Compute the initial value to hash  
		update(hv);
		update(t);
		update(x);
		update(pwd);
		update(salt);
		digest.doFinal(x, 0);
		digest.reset();


		// Overwrite Password if enabled
		if (overwrite == true) {
			erasePwd(pwd);
		}

		// provide resistance against weak garbage collector attacks:
		flap(x, lambda, (min_garlic+1)/2, publicInput, x);

		for(c=min_garlic; c <= garlic; c++) {
			flap(x, lambda, c, publicInput,  x);
		  	
		    if( (c==garlic) && (CLIENT == _client)) {
		    	System.arraycopy(x, 0, hash, 0, hashlen);
		    	return;
		    }
		    digest.update( (byte) c);
		    update(x);
		    digest.doFinal(x, 0);	  
		    digest.reset();
		}
		System.arraycopy(x, 0, hash, 0, hashlen);
	}

	// Server Relief
	/**
	 * Call this function on client side to use the server relief. 
	 * 
	 * @param pwd			password
	 * @param salt			salt 
	 * @param data			associated data
	 * @param lambda		depth of graph
	 * @param min_garlic	min. Garlic
	 * @param garlic		cost parameter
	 * @param hashlen		length of hash value
	 * @param x				value to store the result
	 */
	public void catenaClient(
			byte[] pwd, byte[] salt, byte[] data, 
			int lambda, int min_garlic, int garlic, 
			int hashlen, byte[] x) {
		catena(pwd, salt, data, 
				lambda, min_garlic, garlic, hashlen,
				CLIENT, PASSWORD_HASHING_MODE, x);
	}
	
	// increase garlic but not minGarlic!!
	/**
	 * Call this function on server side 
	 * for the result value of the function 
	 * catenaClient to use the server relief
	 * 
	 * @param garlic	the cost parameter
	 * @param x			the resulting hash value 
	 * 					of the function catenaClient
	 * @param hashlen	the length of the hash value
	 * @param hash		the resulting hash valueresulting 
	 */
	public void catenaServer(
			int garlic, byte[] x,
			int hashlen, byte[] hash) {
		byte[] z = new byte[H_LEN];

		initDigests();
  
		if (hashlen > H_LEN){
			throw new IllegalArgumentException("illegal length of output");
		}
		digest.update((byte) garlic);
		digest.update(x, 0, H_LEN);
		digest.doFinal(z, 0);
		digest.reset();
		System.arraycopy(z,  0,  hash,  0,  hashlen);
		Arrays.fill(z,  (byte) 0);
	}

	/**
	 * Client-independent update: Increase the cost
	 * parameter without knowledge of the password 
	 * 
	 * @param old_hash		the previous hash value
	 * @param lambda		the depth of the graph
	 * @param salt			the salt value
	 * @param old_garlic	the previous used cost parameter
	 * @param new_garlic	the new cost parameter
	 * @param hashlen		the length of the hash value
	 * @param new_hash		the resulting hash value
	 */
	public void ciUpdate(
			byte[] old_hash,  int lambda,
			byte[] salt,  
			int old_garlic, int new_garlic,
			int hashlen, byte[] new_hash) {
		int c;
		byte[] x = new byte[H_LEN];
 
		initDigests();

		System.arraycopy(old_hash, 0, x, 0, hashlen);

		for(c=old_garlic+1; c <= new_garlic; c++) {
		     flap(x, lambda, c, salt, x);
		     digest.update((byte) c);
		     update(x);
		     digest.doFinal(x,  0);
		     digest.reset();

		     for(int i= hashlen; i < H_LEN; i++) {
		    	 x[i] = (byte) 0;
		     }
		}
		System.arraycopy(x, 0, new_hash, 0, hashlen); 
	}

	/**
	 * Use Catena as key derivation function to
	 * derive a cryptographic key with arbitrary length. 
	 * 
	 * @param pwd			the password
	 * @param salt			the salt
	 * @param data			the associated data 
	 * @param lambda		the depth of the graph
	 * @param min_garlic	min. cost parameter
	 * @param garlic		the cost parameter
	 * @param key_id		the key identifier
	 * @param key			the result key, the length 
	 * 						of the vector indicates the 
	 * 						length of the result key
	 */
	public void deriveKey(
			byte[] pwd, byte[] salt, byte[] data,  
	       int lambda, int  min_garlic, int garlic, 
	       int key_id, byte[] key) {
		
		byte[] hash = new byte[H_LEN];
		int keylen = key.length;
		int len = keylen / H_LEN;
		int rest = keylen % H_LEN;
		long i;
		if(digest == null) {
			digest = new Blake2bDigest();
		}
		// default is FULL
		if(reducedDigest == null) {
			fast = false;
			//reducedDigest = digest;
		}  else {
			fast = true;
		}
		catena(pwd, salt, data, 
				lambda, min_garlic, garlic, 
				H_LEN, REGULAR, KEY_DERIVATION_MODE,
				hash);

		for(i=0; i < len; i++) {

			long tmp = i;
		    digest.update( (byte) 0);
		    update(long2bytesLE(tmp));
		    digest.update( (byte) key_id);
		    update(int2bytesLE(keylen));
		    update(hash);
		    digest.doFinal(key, (int) i * H_LEN);
		    digest.reset();
		}

		if(rest > 0) {
			
			long tmp = i;// TO_LITTLE_ENDIAN_64(i);
			digest.update( (byte) 0);
			update(long2bytesLE(tmp));
			digest.update( (byte) key_id);
			update(int2bytesLE(keylen));
			update(hash);
			digest.doFinal(hash, 0);
			digest.reset();
			System.arraycopy(hash,  0,  key,  len * H_LEN,  rest);
		}
	}

	public void catenaKeyedHashing(
			byte[] pwd, byte[] salt, byte[] data,
			int lambda, int  min_garlic, int garlic, 
			int  hashlen, byte[] key, 
			long uuid, byte[] chash) {
		
		byte[] keystream = new byte[H_LEN];
		long tmp = uuid;
		
		initDigests();
		
		catena(pwd, salt, data, 
				lambda, min_garlic, garlic, hashlen,
				REGULAR, PASSWORD_HASHING_MODE, chash);
 
		digest.update(key, 0, KEY_LEN);
		update(long2bytesLE(tmp));
		digest.update((byte) garlic);
		digest.update(key, 0, KEY_LEN);
		digest.doFinal(keystream,  0);

		for(int i=0; i<hashlen; i++){
			chash[i] ^= keystream[i];
		}
	}

	//====== ABSTRACT METHODS ======	
	
	/**
	 * Memory-hard function. 
	 * @param r			the memory consuming state vector
	 * @param garlic	cost parameter
	 * @param lambda	depth of graph
	 * @param h			value, holds the result
	 */
	protected abstract void F(byte[] r, int garlic, int lambda, byte[] h);
	
	/**
	 * an optional randomization layer Γ,
	 * to harden the memory initialization;
	 * updates the state array, 
	 * depending on the public input (salt)
	 * 
	 * @param garlic	cost parameter
	 * @param salt		salt
	 * @param r			memory consuming state vector
	 */
	protected abstract void gamma(int garlic, byte[] salt, byte[] r);
	
	/**
	 * an optional password-dependent randomization layer Φ 
	 * to provide sequential memory-hardness. 
	 * Updates the state array. 
	 * Note: this function is not resistant against cache-timing attacks. 
	 * 
	 * @param r			memory consuming state vector
	 */
	protected abstract void phi(byte[] r);
	
	/**
	 * returns the default cost parameter
	 * of the child class 
	 */
	public abstract int getGarlic();
	
	/**
	 * returns the default minGarlic parameter
	 * of the child class 
	 */
	public abstract int getMinGarlic();
	
	/**
	 * returns the default parameter for the graphs depth
	 * of the child class 
	 */
	public abstract int getLambda();
	
	
	//====== Implemented Methods ======
	
	/** The function f lap consists of three phases: 
	 * (1) an initialization phase, where the memory of size 2g · n bits 
	 * is written in a sequential order, 
	 * (2) the function Γ depending on the public input γ, and 
	 * (3) a call to a memory-hard function F 
	 *
	 * 
	 * @param x			64 byte vector
	 * @param lambda	depth of graph
	 * @param garlic	cost parameter
	 * @param salt		salt parameter, recommended at least 16 bytes
	 * @param h			value, holds the result
	 */
	protected void flap(byte[] x, int lambda, int garlic, byte[] salt, byte[] h) {

		byte[]  r   = new byte[ (int) (( (1 << garlic) + (1 << (garlic-1)) ) * H_LEN)];

		initmem(x, (1 << garlic), r);

		gamma(garlic, salt, r);

		F(r, garlic, lambda, h);
	}
	
	/**
	 * Initializes the state vector
	 * @param x		64 byte vector
	 * @param c		int value
	 * @param r		memory consuming state vector
	 */
	protected void initmem(byte[] x, long c, byte[] r) {
		
		  byte[] tmp = new byte[H_LEN];
		  
		  System.arraycopy(x,  0,  tmp,  0, H_LEN);
		  tmp[H_LEN-1] ^= 1;
	
		  update(x);
		  update(tmp);
		  digest.doFinal(r,  0);
		  digest.reset();
		  
		  if(reducedDigest != null)
		  reducedDigest.reset();
		  //fastDigest.reset();
	
		  hashFast(1, r, 0, x, 0, r, H_LEN);
		    
		  for(int i = 2; i < c; i++){
	
			  hashFast(i, r, (i-1) * H_LEN, r, (i-2) * H_LEN, r, i * H_LEN);
		  }
	}	

	// === RNG xorshift1024star ===
	private long[] s ;// state of the Xorshift RNG
	private int p; // position in state vector s
	
	/**
	 * Used for Xorshift generator
	 */
	void initXSState(byte[] a, byte[] b){ // seed the state with two hash values
		s = new long[16];

		p = 0;
		int sIndex = 0;
		for ( int i = 0; i < a.length; i+=8) {
			s[sIndex++] = bytes2long(a, i);
		}
		for ( int i = 0; i < b.length; i+=8) {
			s[sIndex++] = bytes2long(b, i);
		}		
	}

	/**
	 * Xorshift generator with 1024 bits of state
	 * 
	 * @return
	 */
	private long xorshift1024star(){	
		// computes random g-bit value j1 / j2
		// in each iteration of the for-loop of saltMix 
		long s0 = s[p];
		p = (p+1) & 15;
		long s1 = s[ p];
		s1 ^= s1 << 31; // a
		s1 ^= s1 >>> 11; // b
		s0 ^= s0 >>> 30; // c
		s[p] = s0 ^ s1;
		return s[p] * 1181783497276652981L;
	}
	

	/**
	 * The gamma function for Butterfly and Dragonfly, 
	 * updates the state array in salt-dependent manner 
	 * 
	 * @param garlic
	 * @param publicInput
	 * @param r
	 */
	protected void saltMix(int garlic, byte[] publicInput, byte[] r) {

		long q = 1 << ((3*garlic+3)/4);
		int vertexIndex; 
		long j; // index of updated word and index for first input
		long j2; // index of second input
		byte[] tmp = new byte[H_LEN];
		byte[] tmp2 = new byte[H_LEN];

		// generate the seed		
		update(publicInput);
		digest.doFinal(tmp, 0);
		digest.reset();
		//blake2b = new Blake2b();
		update(tmp);
		digest.doFinal(tmp2, 0);
		digest.reset();

		initXSState(tmp, tmp2);

		if(reducedDigest != null)
		reducedDigest.reset();
		//fastDigest.reset();
		for(vertexIndex = 0; vertexIndex < q; vertexIndex++){ 
			j = xorshift1024star() >>> (64 - garlic); 
			j2 = xorshift1024star() >>> (64 - garlic);

	  		hashFast(vertexIndex, 
				r, (int) j * H_LEN, 
				r, (int) j2 * H_LEN, 
				r, (int) j * H_LEN);
		}	
	}

	//====== Helper Functions ======	

	/**
	 * If fast = true, this function uses a round-reduced 
	 * version of the digest. 
	 * 
	 * @param vIndex	vertex index, indicates the round 
	 * 					of the hash function to be used
	 * @param input1	first input vector for hash function
	 * @param inIndex1	index of first input vector, the 
	 * 					length is always H_LEN (64 byte)
	 * @param input2	second input vector for hash function
	 * @param inIndex2	index of second input vector, the 
	 * 					length is always H_LEN (64 byte)
	 * @param hash		vector to store the resulting hash value
	 * @param outIndex	index, where hash value is stored
	 */
	protected void hashFast(int vIndex, 
			byte[] input1, int inIndex1, 
			byte[] input2, int inIndex2, 
			byte[] hash, int outIndex) {
		
		if (fast == true) {
			//ReducedDigest reducedDigest = (ReducedDigest) fastDigest;
			reducedHash(vIndex, input1, inIndex1, input2, inIndex2, hash, outIndex);
		} else { // FULL
			fullHash(
					input1, inIndex1, 
					input2, inIndex2, 
					hash, outIndex);
		}
	}
	
	/**
	 * This function uses a round-reduced 
	 * version of a digest. 
	 * 
	 * @param vIndex	vertex index, indicates the round 
	 * 					of the hash function to be used
	 * @param input1	first input vector for hash function
	 * @param inIndex1	index of first input vector, the 
	 * 					length is always H_LEN (64 byte)
	 * @param input2	second input vector for hash function
	 * @param inIndex2	index of second input vector, the 
	 * 					length is always H_LEN (64 byte)
	 * @param hash		vector to store the resulting hash value
	 * @param outIndex	index, where hash value is stored
	 */
	protected void reducedHash(
			int vIndex, 
			byte[] input1, int inIndex1, 
			byte[] input2, int inIndex2, 
			byte[] hash, int outIndex) {

		reducedDigest.setVertexIndex(vIndex % 12);
		
		reducedDigest.update(input1, inIndex1, H_LEN);
		reducedDigest.update(input2, inIndex2, H_LEN);
		reducedDigest.doFinal(hash, outIndex);
	}
	
	/**
	 * This function uses a digest with all rounds
	 * 
	 * @param vIndex	vertex index, indicates the round 
	 * 					of the hash function to be used
	 * @param input1	first input vector for hash function
	 * @param inIndex1	index of first input vector, the 
	 * 					length is always H_LEN (64 byte)
	 * @param input2	second input vector for hash function
	 * @param inIndex2	index of second input vector, the 
	 * 					length is always H_LEN (64 byte)
	 * @param hash		vector to store the resulting hash value
	 * @param outIndex	index, where hash value is stored
	 */
	protected void fullHash(
			byte[] input1, int inIndex1, 
			byte[] input2, int inIndex2, 
			byte[] hash, int outIndex) {
		
		digest.update(input1, inIndex1, H_LEN);
		digest.update(input2, inIndex2, H_LEN);
		digest.doFinal(hash, outIndex);
		digest.reset();
	}

	
	
	
	/**
	 * Initializes the digests. 
	 */
	private void initDigests() {
		if(digest == null) {
			digest = new Blake2bDigest();
		}
		if(reducedDigest == null) {
			if (fast == true) {
				reducedDigest = new Catena_Blake2b_1();
			} else {
				//
			}
		} 
	}	
		
	/**
	 * Clear the password 
	 * 
	 * @param pwd	the password to be cleared
	 */
	private final void erasePwd(byte[] pwd) {
		Arrays.fill(pwd, (byte) 0);
	}


	/**
	 * Convert a 64 bit input value in an array of 8 bit values
	 * in little endian order
	 * 
	 * @param 	longValue	the value to be converted
	 * 
	 * @return	the resulting byte array
	 */
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
	/**
	 * Convert an array of 8 bytes in a 64 bit long value
	 * in little endian order
	 * 
	 * @param byteArray		byte array
	 * @param offset		start index in byte array 
	 * 
	 * @return				the resulting 64 bit long value
	 */
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

	public final static byte[] int2bytesLE(int val) {
		byte[] result = new byte[4];
		result[0] = (byte) val;
		result[1] = (byte)(val >>>  8);
		result[2] = (byte)(val >>>  16);
		result[3] = (byte)(val >>>  24);
		return result;		
	}

	public final static byte[] long2bytesLE(long longValue) {
	    return new byte[] {
	        (byte) (longValue),
	        (byte) (longValue >> 8),
	        (byte) (longValue >> 16),
	        (byte) (longValue >> 24),
	        (byte) (longValue >> 32),
	        (byte) (longValue >> 40),
	        (byte) (longValue >> 48),
	        (byte) (longValue >> 56)
	    };
	}
	
	protected final void update(byte[] input) {
		digest.update(input, 0, input.length);
	}
	
	//====== GETTER & SETTER ======
	
	/**
	 * @return the versionID
	 */
	public String getVersionID() {
		return versionID;
	}
	/**
	 * @param versionID the versionID to set
	 */
	public void setVersionID(String versionID) {
		this.versionID = versionID;
	}
	/**
	 * @param _digest	the hash function for Catena
	 */
	public void setDigest(Digest _digest) {
		digest = _digest;
	}
	/**
	 * @param _fastDigest	 the possibly round-reduced hash function
	 */
	public void setReducedDigest(ReducedDigest _fastDigest) {
		reducedDigest = _fastDigest;
	}
	/**
	 * @return	the hash function for Catena
	 */
	public Digest getDigest() {
		return digest;
	}
	/**
	 * @return	 the possibly round-reduced hash function
	 */
	public Digest getFastDigest() {
		return reducedDigest;
	}
	/**
	 * @return 	value that indicates if the 
	 * 			password is cleared as soon as possible
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * @param 	if true, the password 
	 * 			is cleared as soon as possible
	 */
	public void setOverwrite(boolean _overwrite) {
		this.overwrite = _overwrite;
	}
	/**
	 * @return 	value indicates if round-reduced 
	 * 			hash function is used or not 
	 */
	public boolean isFast() {
		return fast;
	}

	/**
	 * @param 	if true: Catena uses a 
	 * 			round-reduced hash function
	 * 			for some computations
	 */
	public void setFast(boolean fast) {
		this.fast = fast;
	}
}
