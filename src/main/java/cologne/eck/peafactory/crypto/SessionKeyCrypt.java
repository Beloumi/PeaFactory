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
 * Handling of session keys to hold the key in RAM.
 * This is required to save new content and to change the password.
 */

/* 
 * Encrypt/decrypt the derived key with 5 session keys: 
 * keys are hopefully difficult to find in RAM, 
 * if an attacker has access it.
 * This is a similar construction like DES-X but only to
 * hamper exhaustive search for the keys in RAM. 
 */


import org.bouncycastle.crypto.BlockCipher;

import cologne.eck.peafactory.tools.Zeroizer;


public class SessionKeyCrypt {
	
	// keys to encrypt encryptedKey
	private byte[] sessionKey1;
	private byte[] sessionKey2;
	private byte[] sessionKey3;
	private byte[] sessionKey4;
	private byte[] sessionKey5;
	
	// key to encrypt plainText
	private byte[] encryptedKey; 

	
	private final static BlockCipher cipher = CipherStuff.getCipherAlgo();
	
	/**
	 * Decrypt the session key
	 * 
	 * @return	the key
	 */
	protected final byte[] getKey() {
		
		byte[] result; // return value
			
		result = new byte[encryptedKey.length];			

		System.arraycopy(encryptedKey, 0, result, 0, encryptedKey.length);		
		
		int ivLen = cipher.getBlockSize();
		int keyLen = CipherStuff.getKeySize();
		int size = keyLen + ivLen + ivLen;

		// 1. XOR with sessionKey5, skip IVs from step 2 and 4:
		for (int i = 0; i < size; i++) {
			result[i] ^= sessionKey5[i];
		}
		// 2. decrypt with sessionKey4: 
		result = CipherStuff.processCTR(false, result, sessionKey4, false);
		// 3. XOR with sessionKey3, skip IV from step 2:
		size -= ivLen;
		for (int i = 0; i < size; i++) {
			result[i] ^= sessionKey3[i];
		}
		// 4. encrypt with sessionKey2: 
		result = CipherStuff.processCTR(false, result, sessionKey2, false);
		// 5. XOR with sessionKey1
		size -= ivLen;
		for (int i = 0; i < keyLen; i++) {
			result[ i] ^= sessionKey1[i];
		}
		Zeroizer.zero(encryptedKey);
		// prevent encryption with zero-key: 
		encryptedKey = null;

		return result;
	}
	
	/**
	 * Encrypt the session key
	 * 
	 * @param key	the key to encrypt
	 */
	public final void storeKey(byte[] key) {	
		
		if (key.length % cipher.getBlockSize() != 0) {
			System.err.println("SessionKeyCrypt: Wrong key size");
			System.exit(1);
		}
		if (key.length < cipher.getBlockSize() ) {
			System.err.println("SessionKeyCrypt: key size to short");
			System.exit(1);			
		}
		checkSessionKeys();

		int ivLen = cipher.getBlockSize();
		int keyLen = CipherStuff.getKeySize();
		int size = keyLen;
		// 1. XOR with sessionKey1: 
		for (int i = 0; i < keyLen; i++) {
			key[i] ^= sessionKey1[i];
		}
		// 2. encrypt with sessionKey2: 
		key = CipherStuff.processCTR(true, key, sessionKey2, false);		
		// 3. XOR with sessionKey3, skip IV from step 2:
		size += ivLen;
		for (int i = 0; i < size; i++) {
			key[i] ^= sessionKey3[i];
		}
		// 4. encrypt with sessionKey4: 

		key = CipherStuff.processCTR(true, key, sessionKey4, false);
		// 5. XOR with sessionKey5, skip IVs from step 2 and 4: 
		size += ivLen;
		for (int i = 0; i < size; i++) {
			key[i] ^= sessionKey5[i];
		}
		
		encryptedKey = key;	
	}
	
	private final void checkSessionKeys() {

		
		int keySize = CipherStuff.getKeySize();
		int blockSize = cipher.getBlockSize();
		
		if (sessionKey1 == null) {
			sessionKey1 = generateSessionKey(keySize);
		}
		if (sessionKey2 == null) {
			sessionKey2 = generateSessionKey(keySize);
		}
		if (sessionKey3 == null) { // key + IV for CTR
			sessionKey3 = generateSessionKey(keySize + blockSize);
		}
		if (sessionKey4 == null) {
			sessionKey4 = generateSessionKey(keySize);
		}
		if (sessionKey5 == null) { // key + 2 * IV for CTR
			sessionKey5 = generateSessionKey(keySize + blockSize + blockSize);
		}		
	}
	/**
	 * Generate temporary key to encrypt the session key
	 * 
	 * @param size	the required size of the key
	 * @return		the generated key
	 */
	protected final byte[] generateSessionKey(int size) {
		return new RandomStuff().createRandomBytes(size );
	}
	/**
	 * Clear all used keys
	 */
	public final void clearKeys() {
		if ( sessionKey1 != null) {
			Zeroizer.zero(sessionKey1);
		}
		if ( sessionKey2 != null) {
			Zeroizer.zero(sessionKey2);
		}
		if ( sessionKey3 != null) {
			Zeroizer.zero(sessionKey3);
		}
		if ( sessionKey4 != null) {
			Zeroizer.zero(sessionKey4);
		}
		if ( sessionKey5 != null) {
			Zeroizer.zero(sessionKey5);
		}
		if ( encryptedKey != null) {
			Zeroizer.zero(encryptedKey);
		}
	}
}
