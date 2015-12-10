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
 * Interface for all used key derivation functions 
 */

public abstract class KeyDerivation {
	
	private static KeyDerivation kdf = null;
	
	private static int tCost = 0;
	private static int mCost = 0;
	private static int arg3 = 0;
	private static int arg4 = 0;
	private static int arg5 = 0;
	private static int arg6 = 0;
	
	private final static int SALT_SIZE = 32;// at most 129 byte for fixed initialized salt
	private static byte[] salt;
	private static byte[] attachedSalt;
	

	private static byte[] extraValues = "peafactory_extra".getBytes(); // 16 byte
	
	private static String versionString = "";
	
	/**
	 * Derive the key. All parameters are set in child classes
	 * 
	 * @param pswMaterial	material build from the password
	 * 
	 * @return				the derived key
	 */
	public abstract byte[] deriveKey( byte[] pswMaterial);
	
	/**
	 * Get the name of the key derivation scheme
	 * 
	 * @return the name of the key derivation scheme
	 */
	public abstract String getName();

	/**
	 * Get a key of required length (expand or extract)
	 * 
	 * @param keyMaterial	the material from key derivation
	 * @return				the key
	 */
	public final static byte[] adjustKeyMaterial(byte[] keyMaterial){

		if (keyMaterial == null) {
			System.err.println("KDFScheme: keyMaterial null");
			System.exit(2);
		}	
		int requiredSize = CipherStuff.getKeySize();

		byte[] result = null;
	
		if (keyMaterial.length >= requiredSize) {
			result = new byte[requiredSize];
			// there is enough entropy in keyMaterial: just cut
			System.arraycopy(keyMaterial, 0, result, 0, result.length);
		} else {
			// stretch keyMaterial: HMAC-based Expand Key Derivation Function
			result = expandKey(keyMaterial, requiredSize); 
		}
		return result;
	}
	
	private final static byte[] expandKey( byte[] keyMaterial, int requiredSize) {
		byte[] result = new byte[requiredSize];
		System.arraycopy(keyMaterial, 0, result, 0, keyMaterial.length);
		int resultPosition = keyMaterial.length;
		short counter = 0;
		byte[] tmp = new byte[HashStuff.getHashAlgo().getDigestSize() + salt.length + 2];
		
		while (resultPosition + 1 < requiredSize) {
			
			// prepare hash input:
			System.arraycopy(keyMaterial, 0, tmp, 0, 
					(keyMaterial.length < tmp.length)? keyMaterial.length : tmp.length);
			System.arraycopy(salt, 0, tmp, tmp.length - (salt.length + 2), salt.length);
			tmp[tmp.length - 2] = (byte) (counter >> 8);
			tmp[tmp.length -1] = (byte) counter;
			
			keyMaterial = HashStuff.hashAndOverwrite(tmp);
			
			for (int i = 0; i < keyMaterial.length && (i + resultPosition) < result.length; i++) {
				result[resultPosition + i] = keyMaterial[i];
			}		

			resultPosition += keyMaterial.length;
			counter++;
		}
		//Help.printBytes("expandedKey in KDFScheme", result);
		return result;	
	}
	/**
	 * Print informations about the used key derivation function
	 * 
	 * @param print 	if true, informations are printed
	 */
	public final static void printInfos(boolean print) {
		if (print == true) {
			System.out.print("Key derivation: " +  kdf.getName() );
			
			if (! versionString.equals("")){
				System.out.print(", " + versionString);
			}
			System.out.print(", time parameter: " + tCost 
					+ ", memory parameter: " + mCost
					);
			if (arg3 != 0) {
				System.out.print(", parameter 3: " + arg3);
			}
			if (arg4 != 0) {
				System.out.print(", parameter 4: " + arg4);
			}
			if (arg5 != 0) {
				System.out.print(", parameter 5: " + arg5);
			}
			if (arg6 != 0) {
				System.out.print(", parameter 6: " + arg6);
			}
			System.out.println("");
		}		
	}
	/**
	 * @param attachedSalt the attachedSalt to set
	 */
	public static void setAttachedAndUpdateSalt(byte[] _attachedSalt) {
		
		if (_attachedSalt.length != SALT_SIZE){
			System.err.println("KeyDerivation: Wrong size of attached salt" );
			System.exit(1);
		}
		KeyDerivation.attachedSalt = _attachedSalt;
		// xor default programRandomBytes with attached salt
		for (int i = 0; i < SALT_SIZE; i++) {
			salt[i] ^= attachedSalt[i];
		}
	}

	//=============================================
	// Getter & Setter  
	/**
	 * @return the tCost
	 */
	public static int gettCost() {
		return tCost;
	}

	/**
	 * @param tCost the tCost to set
	 */
	public static void settCost(int _tCost) {
		KeyDerivation.tCost = _tCost;
	}

	/**
	 * @return the mCost
	 */
	public static int getmCost() {
		return mCost;
	}

	/**
	 * @param mCost the mCost to set
	 */
	public static void setmCost(int _mCost) {
		KeyDerivation.mCost = _mCost;
	}

	/**
	 * @return the arg3
	 */
	public static int getArg3() {
		return arg3;
	}

	/**
	 * @param arg3 the arg3 to set
	 */
	public static void setArg3(int _arg3) {
		KeyDerivation.arg3 = _arg3;
	}

	/**
	 * @return the arg4
	 */
	public static int getArg4() {
		return arg4;
	}

	/**
	 * @param arg4 the arg4 to set
	 */
	public static void setArg4(int _arg4) {
		KeyDerivation.arg4 = _arg4;
	}

	/**
	 * @return the arg5
	 */
	public static int getArg5() {
		return arg5;
	}

	/**
	 * @param arg5 the arg5 to set
	 */
	public static void setArg5(int _arg5) {
		KeyDerivation.arg5 = _arg5;
	}

	/**
	 * @return the arg6
	 */
	public static int getArg6() {
		return arg6;
	}

	/**
	 * @param arg6 the arg6 to set
	 */
	public static void setArg6(int _arg6) {
		KeyDerivation.arg6 = _arg6;
	}

	/**
	 * @return the salt
	 */
	public static byte[] getSalt() {
		return salt;
	}

	/**
	 * @param salt the salt to set
	 */
	public static void setSalt(byte[] _salt) {
		salt = new byte[SALT_SIZE];
		System.arraycopy(_salt,  0,  KeyDerivation.salt,  0,  SALT_SIZE);
	}

	/**
	 * @return the extraValues
	 */
	public static byte[] getExtraValues() {
		return extraValues;
	}

	/**
	 * @param extraValues the extraValues to set
	 */
	public static void setExtraValues(byte[] _extraValues) {
		KeyDerivation.extraValues = _extraValues;
	}

	/**
	 * @return the kdf
	 */
	public static KeyDerivation getKdf() {
		return kdf;
	}

	/**
	 * @param kdf the kdf to set
	 */
	public static void setKdf(KeyDerivation kdf) {
		KeyDerivation.kdf = kdf;
	}

	/**
	 * @return the versionString
	 */
	public static String getVersionString() {
		return versionString;
	}

	/**
	 * @param versionString the versionString to set
	 */
	public static void setVersionString(String versionString) {
		KeyDerivation.versionString = versionString;
	}
	
	public static int getSaltSize(){
		return SALT_SIZE;
	}

	/**
	 * @return the attachedSalt
	 */
	public static byte[] getAttachedSalt() {
		return attachedSalt;
	}
}
