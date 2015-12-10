package cologne.eck.peafactory.crypto;

/*
 * Peafactory - Production of Password Encryption Archives
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
 * Handles hash functions. 
 */
import org.bouncycastle.crypto.Digest;

import cologne.eck.peafactory.tools.Zeroizer;

public class HashStuff {
		
	private static Digest hashAlgo;
	
	/**
	 * Perform a hash function and clear the input immediately
	 * 
	 * @param input		the input to hash
	 * @return			the output of the hash function
	 */
	public static final byte[] hashAndOverwrite(byte[] input) {

        hashAlgo.update(input, 0, input.length);
        
        byte[] digest = new byte[hashAlgo.getDigestSize()];

        hashAlgo.doFinal(digest, 0);

        Zeroizer.zero(input);

        return digest;
	}

	/**
	 * Perform a hash function
	 * 
	 * @param input		the input to hash
	 * @return			the output of the hash function
	 */
	public static final byte[] hash(byte[] input) {
		
        hashAlgo.update(input, 0, input.length);

        byte[] digest = new byte[hashAlgo.getDigestSize()];

        hashAlgo.doFinal(digest, 0);

        return digest;
	}
	
	//=================
	// Getter & Setter:
	/**
	 * Get the used hash function
	 * 
	 * @return	the hash function
	 */
	public static final Digest getHashAlgo() {
		return hashAlgo;
	}
	/**
	 * Set the hash function
	 * 
	 * @param _hashAlgo	the hash function to be used
	 */
	public static final void setHashAlgo(Digest _hashAlgo) {
		hashAlgo = _hashAlgo;
	}	
}
