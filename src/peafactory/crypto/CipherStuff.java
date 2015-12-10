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
 * Performs the encryption/decryption of byte blocks
 * ECB mode for session key and key handling.
 * File encryption is done in the mode class.
 */
import javax.swing.JOptionPane;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;







//import cologne.eck.dr.op.peafactory.tools.Help;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Zeroizer;


public class CipherStuff {
	
	private static CipherStuff cipherStuff = new CipherStuff();
	
	private static BlockCipher cipherAlgo;
	
	private static AuthenticatedEncryption authenticatedEncryption = new EAXMode();
	
	private static String errorMessage = null;
	
	private static SessionKeyCrypt skc = null;
	
	private static boolean bound = true;
	
	// returns required length of key in bytes
	public final static int getKeySize() {
		
		int keySize = 0;
		
		if (cipherAlgo == null) {
			System.err.println("CipherStuff getKeySize: cipherAlgo null");
			System.exit(1);
		}
		
		if (cipherAlgo.getAlgorithmName().startsWith("Threefish") ) {
			// key size = block size for Threefish 256/512/1024
			keySize = cipherAlgo.getBlockSize();
		} else if ( cipherAlgo.getAlgorithmName().equals("Twofish" )) {
			keySize = 32; // Bytes
		} else if (cipherAlgo.getAlgorithmName().equals("Serpent") ) {
			//System.out.println("SerpentEngine");
			keySize = 32; // Bytes			
		} else if (cipherAlgo.getAlgorithmName().equals("AES") ) {
			keySize = 32; // Bytes		
		} else if (cipherAlgo.getAlgorithmName().equals("AESFast") ) {
			keySize = 32; // Bytes	
		} else if (cipherAlgo.getAlgorithmName().equals("Shacal2") ) {
			keySize = 64; // Bytes
		} else {
			System.err.println("CipherStuff getKeySize: invalid Algorithm");
			System.exit(1);
		}
		return keySize;
	}
	
	/**
	 * Encrypt/decrypt an array of bytes. This function is only used to 
	 * encrypt the session key
	 * 
	 * @param forEncryption - true for encryption, false for decryption
	 * @param input			- plain text or cipher text
	 * @param key			- the cryptographic key for the cipher
	 * @param zeroize		- fills the key with 0 for true (when the key is no longer used)
	 * @return				- plain text or cipher text
	 */
	public final static byte[] processCTR(boolean forEncryption, byte[] input, byte[] key,
			boolean zeroize) {
		//Help.printBytes("key",  input);
		KeyParameter kp = new KeyParameter(key);
		if (zeroize == true) {
			Zeroizer.zero(key);
		}
		
		byte[] iv = null;
		
		if (forEncryption == false) {
			// input is ciphertext, IV is stored on top of ciphertext
			// get IV:
			iv = new byte[CipherStuff.getCipherAlgo().getBlockSize()];
			System.arraycopy(input,  0,  iv,  0,  iv.length);
			// truncate ciphertext:
			byte[] tmp = new byte[input.length - iv.length];
			System.arraycopy(input,  iv.length, tmp,  0,  tmp.length);
			input = tmp;
		} else {
			// input is plaintext
			iv = new RandomStuff().createRandomBytes(CipherStuff.getCipherAlgo().getBlockSize());
		}
		ParametersWithIV ivp = new ParametersWithIV(kp, iv);

		BufferedBlockCipher b = new BufferedBlockCipher(new SICBlockCipher(cipherAlgo));

        b.init(true, ivp);
        byte[] out = new byte[input.length];

        int len = b.processBytes(input, 0, input.length, out, 0);

        try {
			len += b.doFinal(out, len);
		} catch (DataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Zeroizer.zero(kp.getKey());

		if (forEncryption == false) {// decryption: output plaintext
			return out;
		} else { // encryption: output (IV || ciphertext)
			Zeroizer.zero(input);
			// store IV on top of ciphertext
			byte[] tmp = new byte[out.length + iv.length];
			System.arraycopy(iv,  0,  tmp,  0,  iv.length);
			System.arraycopy(out,  0,  tmp,  iv.length,  out.length);
			return tmp;
		}		
	}

	
	/*
	 * Encryption if plainBytes can be loaded in memory in one block
	 */
	/**
	 * Encrypt an array of bytes. 
	 * 
	 * @param plainBytes			the plain text to encrypt
	 * @param keyMaterial			the derived key or null (there
	 * 								is a session key available)
	 * @param encryptBySessionKey	if true, the derived key is encrypted, 
	 * 								otherwise the key is cleared
	 * @return						the cipher text
	 */
	public final byte[] encrypt(byte[] plainBytes, byte[] keyMaterial, 
			boolean encryptBySessionKey){
		
		checkSessionKeyCrypt();
		
		// Check if plainBytes is too long to be loaded in memory:
		if (plainBytes.length > 8192 * 64 * 16) { // fileBlockSize = 8192 * 64 * 16
			
			// check free memory to avoid swapping:
			System.gc(); // this might not work...
			long freeMemory = Runtime.getRuntime().maxMemory() 
					- (Runtime.getRuntime().totalMemory()
							- Runtime.getRuntime().freeMemory());
			//System.out.println("Free memory: " + freeMemory);
			if (plainBytes.length <= freeMemory) {			
				System.out.println("Warning: long plain text");
			} else {
				JOptionPane.showMessageDialog(null,
					    "The content you want to encrypt is too large \n"
					    + "to encrypt in one block on your system: \n"
					    + plainBytes.length + "\n\n"
					    + "Use the file encrytion pea instead. \n"
					    + "(If you blew up an existing text based pea, \n"
					    + "you have to save the content by copy-and-paste).",
					    "Memory error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}			
		}
		
		// unique for each encryption
		byte[] iv = Attachments.getNonce();
		if (iv == null) {
			// for every file: new unique and random IV
			iv = Attachments.generateNonce( );
			Attachments.setNonce(iv);
		}
		byte[] key = detectKey(keyMaterial);
		//
		//------ENCRYPTION: 
		//				
		// add pswIdentifier to check password in decryption after check of Mac
		plainBytes =  Attachments.addPswIdentifier(plainBytes);

		byte[] cipherBytes = authenticatedEncryption.processBytes(true, plainBytes, key, iv);	
		Zeroizer.zero(plainBytes);				
		// prepare to save:
		cipherBytes = Attachments.addNonce(cipherBytes, iv);	
		if (CipherStuff.isBound() == false) {
			cipherBytes = Attachments.attachBytes(cipherBytes, KeyDerivation.getAttachedSalt());
		}
		cipherBytes = Attachments.addFileIdentifier(cipherBytes);	
		//
		// handle the keys
		//
		handleKey(key, encryptBySessionKey);

//	Help.printBytes("cipherBytes", cipherBytes);
		return cipherBytes;
	}
	

	
	// changes the encryptedKey for new keyMaterial
	// used by changing password
	/**
	 * Encrypt the derived key and use as session key
	 * 
	 * @param keyMaterial	the derived key
	 */
	public final void encryptKeyFromKeyMaterial(byte[] keyMaterial) {
		
		checkSessionKeyCrypt();
		
		if (keyMaterial == null) {
			System.err.println("Error: new keyMaterial null (CryptStuff.encryptKeyFromKeyMaterial");
			System.exit(1);
		}		
		byte[] key = detectKey(keyMaterial);
		handleKey(key, true);
	}

	
	
	//
	// returns null for incorrect password or input
	//
	/**
	 * Decrypt an array of bytes. 
	 * 
	 * @param cipherText			the cipher text to decrypt
	 * @param keyMaterial			the derived key or null if 
	 * 								there is a session key
	 * @param encryptBySessionKey	encrypt the key or clear it
	 * @return						the plain text or null if the
	 * 								decryption failed
	 */
	public final byte[] decrypt(byte[] cipherText, byte[] keyMaterial, 		
			boolean encryptBySessionKey) {
		
		checkSessionKeyCrypt();
		//Help.printBytes("ciphertext + Nonce + fileId",  cipherText);
		// check
		if (cipherText == null) {
			errorMessage = "no cipher text";
			return null;
		}
		cipherText = Attachments.checkFileIdentifier(cipherText, true);
		if (cipherText == null) { // check returns null if failed
			errorMessage = "unsuitable cipher text (identifier)";
			return null;
		}
		if (CipherStuff.isBound() == false){ // cut salt
			cipherText = Attachments.cutSalt(cipherText);
		}

		byte[] iv = Attachments.calculateNonce(cipherText);
		if (iv == null) {
			errorMessage = "unsuitable cipher text (length)";
			return null;
		} else {
			cipherText = Attachments.cutNonce(cipherText);
		}		

		// Check if plainBytes is too long to be loaded in memory:
		if (cipherText.length > 8192 * 64 * 16) { // fileBlockSize = 8192 * 64 * 16
			
			// get free memory to avoid swapping:
			System.gc(); // this might not work...
			long freeMemory = Runtime.getRuntime().maxMemory() 
					- (Runtime.getRuntime().totalMemory()
							- Runtime.getRuntime().freeMemory());
			//System.out.println("Free memory: " + freeMemory);
			if (cipherText.length <= freeMemory) {			
				System.out.println("Warning: long plain text");
				
				if (cipherText.length > (freeMemory - 8192 * 64)){
					JOptionPane.showMessageDialog(null,
						    "The content you want to decrypt is already large: \n"
						    + cipherText.length + "\n\n"
						    + "Adding more content may cause a memory error. \n",
						    "Memory warning",
						    JOptionPane.WARNING_MESSAGE);					
				}
			} else {
				JOptionPane.showMessageDialog(null,
					    "The content you want to decrypt is too large \n"
					    + "to decrypt in one block on your system: \n"
					    + cipherText.length + "\n\n"
					    + "It was probably encrypted on a system with more memory. \n"
					    + "You have to decrypt it on another system... \n",
					    "Memory error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}			
		}
		
		byte[] key = detectKey(keyMaterial);

		int macLength = CipherStuff.getCipherAlgo().getBlockSize();		
		// check mac length:
		if (cipherText.length < macLength) {
			errorMessage = "unsuitable cipher text (Mac length)";
			return null;			
		}

		byte[] plainText = authenticatedEncryption.processBytes(false, cipherText, key, iv);
		if (plainText == null) { // wrong password
			errorMessage = "authentication failed for password";
			return null;
		}
		byte[] rescueText = null;
		if (PswDialogBase.getWorkingMode().equals("-r")) { // rescue mode: make copy of plainText
			rescueText = new byte[plainText.length];
			System.arraycopy(plainText, 0, rescueText, 0, plainText.length);
		}
		plainText = Attachments.checkAndCutPswIdentifier(plainText); // truncates pswIdentifier

		if (plainText == null) {
			
			System.out.println("Password identifier failed.");
			
			if (PswDialogBase.getWorkingMode().equals("-r")) { // rescue mode
				 
				 Object[] options = {"Continue",
	                    "Break"};
				 int n = JOptionPane.showOptionDialog(null,
						 "Password identifier failed:  \n"
								 + "The password identifier checks if the beginning and the end \n"
								 + "of the content.\n "
								 + "An error means that wether the beginning or the end is not correct.\n"
								 + "Normally that means, that the password is wrong, \n"
								 + "but there is the possibility that a file is damaged. \n"
								 + "In this case, some parts of the file might be restored \n"
								 + "by the decryption. \n"
								 + "If you are sure, the password is correct, continue.\n"
								 + "Warning: For incorrect password files may be irretrievably lost. ",
						"Password Identification Error",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						options,
						options[1]);
			 
				 if (n == JOptionPane.YES_OPTION) {
					// do nothing
					 System.out.println("continue");
					 plainText = rescueText;
				 } else {
					 System.out.println("break");
					return null;
				 }		 
			 } else {// not recue mode
					errorMessage = "password failed (identifier)";
					return null;
			 }				
		} 

		//
		// handle the keys
		//		
		handleKey(key, encryptBySessionKey);
		return plainText;
	}

	/**
	 * Get the key from keyMaterial or the session key
	 * 
	 * @param keyMaterial	the derived key or null
	 * @return				the key
	 */
	protected final byte[] detectKey(byte[] keyMaterial) {
		
		byte[] key = null;
		if (keyMaterial != null) { // use as key 
			
			// check size:
			int keySize = CipherStuff.getKeySize();
			if (keyMaterial.length != keySize) {
				System.err.println("CryptStuff detectKey: invalid size of keyMaterial");
				System.exit(1);
			} else {
				key = keyMaterial;
			}
			
		} else { // keyMaterial == null: decrypt encryptedKey to get key
			if (skc == null) {
				skc = new SessionKeyCrypt();
			}
			key = skc.getKey();
		}
		return key;
	}
	/**
	 * Encrypt or clear the key
	 * 
	 * @param key				the key
	 * @param encryptSessionKey	if true, encrypt the key, if
	 * 							false clear it
	 */
	protected final void handleKey(byte[] key, boolean encryptSessionKey) {		

		if (encryptSessionKey == false) {
			Zeroizer.zero(key);
			
		} else {		
			if (skc == null) {
				skc = new SessionKeyCrypt();
			}
			skc.storeKey(key);
		}
	}
	private final void checkSessionKeyCrypt(){
		if (skc == null) {
			skc = new SessionKeyCrypt();
		}
	}

	
	//=========================
	// Getter & Setter:
	public final static void setCipherAlgo (BlockCipher _cipherAlgo) {
		cipherAlgo = _cipherAlgo;
	}
	public final static BlockCipher getCipherAlgo () {
		return cipherAlgo;
	}	
	
	public final static void setCipherMode (AuthenticatedEncryption _cipherMode) {
		authenticatedEncryption = _cipherMode;
	}
	public final static AuthenticatedEncryption getCipherMode(){
		return authenticatedEncryption;
	}
	
	public final SessionKeyCrypt getSessionKeyCrypt(){
		return skc;
	}
	// used in PswDialogFile for initialization
	public final static void setSessionKeyCrypt(SessionKeyCrypt _skc){ 
		skc = _skc;
	}
	public final static String getErrorMessage() {
		return errorMessage;
	}
	public final static void setErrorMessage(String _errorMessage) {
		errorMessage = _errorMessage;
	}

	/**
	 * @return the cipherStuff
	 */
	public final static CipherStuff getInstance() {
		if(cipherStuff == null){
			cipherStuff = new CipherStuff();
		}
		return cipherStuff;
	}

	/**
	 * @return the bound
	 */
	public static boolean isBound() {
		return bound;
	}

	/**
	 * @param bound the bound to set
	 */
	public static void setBound(boolean bound) {
		CipherStuff.bound = bound;
	}
}
