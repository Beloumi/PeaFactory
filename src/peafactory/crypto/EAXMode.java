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
 * The EAX mode of operation: Encryption with authentication. 
 * Contains function for byte blocks and files. 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.JOptionPane;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import settings.PeaSettings;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.file_pea.FileTypePanel;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Comparator;
import cologne.eck.peafactory.tools.Zeroizer;




public final class EAXMode implements AuthenticatedEncryption {
	
	// block of file to encrypt/decrypt: 
	private static final int FILE_BLOCK_SIZE = 8192 * 64 * 16; 
	
	
	/**
	 * Encrypt/decrypt an array of bytes
	 * 
	 * @param forEncryption - true for encryption, false for decryption
	 * @param input			- plain text or cipher text
	 * @param key			- the cryptographic key for the cipher
	 * @param nonce			- unique Nonce of 8 byte
	 * @return				- plain text or cipher text
	 */
	public final byte[] processBytes(	
										boolean forEncryption, 
										byte[] input, 
										byte[] key, 
										byte[] nonce) {

		int resultLen = 0;// proceeded bytes

		KeyParameter aeKey = new KeyParameter(key);
		
		int macLength = CipherStuff.getCipherAlgo().getBlockSize();
		if (macLength < 16) {
			 System.out.println("Warning: short mac size: " + macLength);
		}

		EAXBlockCipher eaxCipher = new EAXBlockCipher(CipherStuff.getCipherAlgo());

		 AEADParameters params = new AEADParameters(aeKey, macLength * 8, nonce);
		 
		 eaxCipher.init(forEncryption, params);
		 
		 byte[] result = new byte[eaxCipher.getOutputSize(input.length)];
		 resultLen = eaxCipher.processBytes(input, 0, input.length, result, 0);
		 try {
			 resultLen += eaxCipher.doFinal(result, resultLen);
			 // KeyParameter uses a copy of the key: 
			 Zeroizer.zero(aeKey.getKey());
		} catch (IllegalStateException e) {
			 CipherStuff.setErrorMessage("Internal application error");
			 System.err.println("EAXMode - processBytes: " + e.toString());
			 return null;	
		} catch (InvalidCipherTextException e) {

			 System.err.println("Authentication failed. ");			
			 if (PswDialogBase.getWorkingMode().equals("-r")) { // rescue mode
			 
				 Object[] options = {"Continue decryption despite error",
	                    "Do not decrypt"};
				 int n = JOptionPane.showOptionDialog(null,
						 "Authentication failed:  \n"
								 + "The content is not the previousely encrypted content. \n"
								 + "Normally that means, that the password is not correct, \n"
								 + "but there is the possibility that the file is damaged. \n"
								 + "In this case, some parts of the file might be restored \n"
								 + "by the decryption. \n"
								 + "If you are sure, the password is correct, continue.\n"
								 + "Warning: For incorrect password files may be irretrievably lost. ",
						"Authentication Error",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE,
						null,
						options,
						options[1]);
			 
				 if (n == JOptionPane.YES_OPTION) {
					return result;
				 } else {
					return null;
				 }		 
			 } else {// not rescue mode
				 CipherStuff.setErrorMessage("Authentication failed");
				 return null;
			 }			
		} catch (Exception e) {
			 CipherStuff.setErrorMessage("Unexpected error");
			 return null;			
		}
		return result;
	}
	
	/**
	 * Encrypt an array of files
	 * 
	 * @param fileNames 	array of file names
	 * @param keyMaterial	derived material from KDF, contains the key
	 * @param encryptBySessionKey	whether encrypt and store the derived key
	 * 								or zeroize it
	 * @param filePanel		JPanel to display the progress of encryption
	 * @return				array of error messages, with the same indexing as fileNames
	 */
	public final String[] encryptFiles( String[] fileNames, byte[] keyMaterial, 
			boolean encryptBySessionKey, FileTypePanel filePanel){
		
		// Attachments: 
		// 1. Padding to plaintext
		// 2. pswIdentifier to ciphertext
		// 3. encryptedPswIdentifier to ciphertext
		// 4. nonce to ciphertext
		// (5. salt to ciphertext if bound == false)
		// 6. fileIdentifier to ciphertext
				
		
		//return value: 
		String[] errorMessages = new String[fileNames.length];

		//------------------
		// get the key: 
		//------------------
		byte[] keyBytes = CipherStuff.getInstance().detectKey(keyMaterial);
		KeyParameter key = new KeyParameter(keyBytes);
		
		int blockSize = CipherStuff.getCipherAlgo().getBlockSize();	
		int macSize = blockSize;
		int nonceSize = Attachments.getNonceSize();
		
		 EAXBlockCipher eaxCipher = new EAXBlockCipher(CipherStuff.getCipherAlgo());
		 
		 byte[] attachedSalt = null;
		 int saltLen = 0;

		 if (CipherStuff.isBound() == false) {
			 
			 if (KeyDerivation.getAttachedSalt() == null) {

				KeyDerivation.setSalt(Attachments.getProgramRandomBytes());
				attachedSalt = new RandomStuff().createRandomBytes(KeyDerivation.getSaltSize());
				// this will set attachedSalt and update the salt:
				KeyDerivation.setAttachedAndUpdateSalt(attachedSalt);
			 }
			 saltLen = KeyDerivation.getSaltSize();
		 }
		 
		
		//----------------------------------
		// encrypt the files:  
		// attach pswIdenitfier, do padding, 
		// encrypt block by block, 
		// attach IV, attach salt (if bound == false),
		 // attach fileIdentifier
		//----------------------------------
		int progress = 0;

		for (int i = 0; i < fileNames.length; i++) {
			
			RandomAccessFile f = null;

			try {	
				// update progress for each file (don't care about file length)
				filePanel.setProgressValue(progress);
				progress += 1000 / fileNames.length; 
				

				if (new File( fileNames[i]).isDirectory() ) {
					continue;
				}
				f = new RandomAccessFile( fileNames[i], "rwd" );

				long fileSizeLong = f.length();							
				if (fileSizeLong > (Integer.MAX_VALUE 
						- (nonceSize * 2) // for pswIdentifier
						- blockSize // mac 
						- saltLen // o if bounded
						- Attachments.getNonceSize() ) ) {// Nonce
					errorMessages[i] = "file too large";
					continue;
				}
				int fileSize = (int) fileSizeLong;

				// random Nonce for each file
				byte[] nonce = Attachments.generateNonce( );				

				byte[] block = new byte[FILE_BLOCK_SIZE];
				
				// generate random pswIdentifier of 8 bytes:
				byte[] pswIdentifier = Attachments.generateNonce(); // 8 byte				

				AEADParameters idParams = new AEADParameters(key, 0, nonce, null);// no mac				 
				eaxCipher.init(true, idParams);
				byte[] encryptedPswIdentifier = new byte[eaxCipher.getOutputSize(nonceSize)];

				// encrypt pswIdentifier without mac and store in 
				int processedIDBytes = eaxCipher.processBytes(pswIdentifier, 0, nonceSize, encryptedPswIdentifier, nonceSize);
				eaxCipher.doFinal(encryptedPswIdentifier, processedIDBytes);
				eaxCipher.reset();

				fileSize = (int) f.length();
				// round down: get block number except last block
				int blockNumber = (fileSize / FILE_BLOCK_SIZE);
				if (fileSize % FILE_BLOCK_SIZE == 0) {
					blockNumber--;
				}

				 if (macSize < 16) {
					 System.out.println("Warning: short mac size: " + macSize);
				 }
				 AEADParameters params = new AEADParameters(key, macSize * 8, nonce, null);//associatedText);
				 
				 eaxCipher.init(true, params);
				 
				 int processedBytes = 0;

				// process the blocks:
				for (int j = 0; j < blockNumber; j++) { // only full blocks

					f.seek(j * FILE_BLOCK_SIZE);
					f.read(block, 0, FILE_BLOCK_SIZE);
					
					byte[] out = new byte[FILE_BLOCK_SIZE];

					processedBytes += eaxCipher.processBytes(block, 0, FILE_BLOCK_SIZE, out, 0);
					
					f.seek(j * FILE_BLOCK_SIZE);
					f.write(out, 0, FILE_BLOCK_SIZE );
				}

				
				// process the last (maybe only) block:
				f.seek(FILE_BLOCK_SIZE * blockNumber);
				int lastSize = fileSize - (FILE_BLOCK_SIZE * blockNumber);

				byte[] lastBlock = new byte[lastSize ];

				f.read(lastBlock, 0, lastBlock.length );

				byte[] lastOut = new byte[eaxCipher.getOutputSize( lastSize)];
				
				processedBytes = 0;
				
				processedBytes += eaxCipher.processBytes(lastBlock, 0, lastSize, lastOut, 0);
				Zeroizer.zero(lastBlock);
				processedBytes += eaxCipher.doFinal(lastOut, processedBytes); // + extra + macSize
			
				f.seek(FILE_BLOCK_SIZE * blockNumber);
				f.write(lastOut, 0, lastOut.length );
				
				// add pswIdentifier to ciphertext file: 
				f.seek( f.length() ); // set file pointer to end
				f.write(pswIdentifier, 0, pswIdentifier.length);
				f.seek(f.length() );
				// add encryptedPswIdentifier to ciphertext file
				f.write(encryptedPswIdentifier, 0, encryptedPswIdentifier.length);

				// add nonce to ciphertext file
				Attachments.addNonce(f, nonce);

				if (CipherStuff.isBound() == false) {
					
					// if not bound: add salt to ciphertext file
					Attachments.addSalt(f, KeyDerivation.getAttachedSalt());
				}
				
				// add fileIdetifier to check later if this file was encrypted with this archive
				Attachments.addFileIdentifier(f);				

				f.close();
	
			} catch (FileNotFoundException e) {
				errorMessages[i] = "file not found";
				System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
				try {
					f.close();

				} catch (IOException e1) {
					e1.printStackTrace();
					errorMessages[i] += " - " + e1.toString();
					System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
					try{
						eaxCipher.reset();
					} catch (Exception e2) {
						System.err.println("EAXMode: Cipher reset failed");
					}
					continue;
				} catch (NullPointerException npe) {
					errorMessages[i] += " - file is probably used by other program";
					System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
					try{
						eaxCipher.reset();
					} catch (Exception e1) {
						System.err.println("EAXMode: Cipher reset failed");
					}
					continue;
				}
				try{
					eaxCipher.reset();
				} catch (Exception e1) {
					System.err.println("EAXMode: Cipher reset failed");
				}
				continue;
				//e.printStackTrace();
			} catch (IOException e) {
				errorMessages[i] = "read/write failed";
				System.err.println("CryptStuff " + e.toString() + ", file: " + fileNames[i]);
				try {
					f.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errorMessages[i] += " - " + e1.toString();
					System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
					eaxCipher.reset();
					continue;
				}
				try{
					eaxCipher.reset();
				} catch (Exception e1) {
					System.err.println("EAXMode: Cipher reset failed");
				}
				continue;
				//e.printStackTrace();
			} catch (Exception e) {
				errorMessages[i] = "unexpected error: " + e.getMessage();
				System.err.println("CryptStuff " + e.toString() + ", file: " + fileNames[i]);
				e.printStackTrace();
				try {
					f.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errorMessages[i] += " - " + e1.toString();
					System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
					eaxCipher.reset();
					continue;
				}
				try{
					eaxCipher.reset();
				} catch (Exception e1) {
					System.err.println("EAXMode: Cipher reset failed");
				}
				continue;
				//e.printStackTrace();			
			}
			eaxCipher.reset();
		} // end for		
		
		//-----------------
		// Handle the keys:
		// encrypt or fill
		//-----------------
		CipherStuff.getInstance().handleKey(key.getKey(), encryptBySessionKey);

		return errorMessages;
	}
	
	/**
	 * Decrypt an array of files
	 * 
	 * @param fileNames 	array of file names
	 * @param keyMaterial	derived material from KDF, contains the key
	 * @param encryptBySessionKey	whether encrypt and store the derived key
	 * 								or zeroize it
	 * @param filePanel		JPanel to display the progress of encryption
	 * @return				array of error messages, with the same indexing as fileNames
	 */
	public final String[] decryptFiles( String[] fileNames, byte[] keyMaterial, 
			boolean encryptBySessionKey, FileTypePanel filePanel){		
		
		int fileNamesLen = fileNames.length;
		
		//return value: 
		String[] errorMessages = new String[fileNamesLen];
		
		// Attachments: 
		// 1. cut fileIdentifier from ciphertext
		// (2. cut salt from Ciphertext if bound == false)
		// 3. cut nonce from ciphertext
		// 4. cut encryptedPswIdentifier from ciphertext
		// 5. cut pswIdentifier from ciphertext
		// 6. undo padding

		//------------------
		// get the key: 
		//------------------
		byte[] keyBytes = CipherStuff.getInstance().detectKey(keyMaterial);
		KeyParameter key = new KeyParameter(keyBytes);


		int blockSize = CipherStuff.getCipherAlgo().getBlockSize();	
		int macSize = blockSize;
		int nonceSize = Attachments.getNonceSize();
		
		int blockNumber = 0;

		EAXBlockCipher eaxCipher = new EAXBlockCipher(CipherStuff.getCipherAlgo());

		//----------------------------------
		// decrypt the files: 
		// decrypt block by block, 
		// add short plain text and cipher text to check password
		// attach Nonce, attach fileIdentifier
		//----------------------------------
		int progress = 0;
		
		int saltLen = 0;
		if (CipherStuff.isBound() == false) {
			saltLen = KeyDerivation.getSaltSize();
		}

		for (int i = 0; i < fileNamesLen; i++) {

			RandomAccessFile f = null;
			byte[] lastNonce = null;// to add if error occurs
			int blockCounter = 0;// to get block where authentication failed

			try {	
				
				// update progress for each file (don't care about file length)
				filePanel.setProgressValue(progress);
				progress += 1000 / fileNamesLen; 		

				if (new File( fileNames[i]).isDirectory() ) {
					continue;
				}
				f = new RandomAccessFile( fileNames[i], "rwd" );

				int fileSize = (int) f.length();	
				if (fileSize < (macSize 
						+ (nonceSize * 3) // Nonce + pswId + encPswId  
						+ saltLen // 0 if bounded
						+ Attachments.getFileIdentifierSize()) ) {
					errorMessages[i] = "inappropriate file (length)";
					System.err.println("file size < minimum: " + fileNames[i]);
					f.close();
					continue;
				}
				// cut fileIdentifier
				if (Attachments.checkFileIdentifier(f, true) == false){ // truncates if true
					errorMessages[i] = "inappropriate file (identifier)";
					System.err.println("file identifier failed: " + fileNames[i]);
					f.close();
					continue;
				}			
				// cut the salt
				if (CipherStuff.isBound() == false) {
					
					byte[] attachedSalt = Attachments.getAndCutSalt(f, true);
					
					if (Comparator.compare(attachedSalt, KeyDerivation.getAttachedSalt()) == false){

						errorMessages[i] = "salt differs from first selected file";
						System.err.println("different salt: " + fileNames[i]);
						f.close();
						if (i == fileNamesLen -1){
							return errorMessages;
						} else {						
							continue;
						}
					}
				}
				// get and cut random Nonce for each file
				byte[] nonce = Attachments.getAndCutNonce( f, true);	
				lastNonce = nonce;
		
				// Check and cut pswIdentifier:
				f.seek(f.length() - (nonceSize * 2));// 
				byte[] bytesToCheckPswIdentifier = new byte[ nonceSize * 2];
				f.read(bytesToCheckPswIdentifier);
				byte[] pswIdentifier = new byte[nonceSize];
				byte[] encryptedPswIdentifier = new byte[nonceSize];
				System.arraycopy(bytesToCheckPswIdentifier, 0, pswIdentifier, 0, nonceSize);
				System.arraycopy(bytesToCheckPswIdentifier, nonceSize, encryptedPswIdentifier, 0, nonceSize);
					 
				AEADParameters idParams = new AEADParameters(key, 0, nonce, null);// no mac				 
				eaxCipher.init(false, idParams);
				byte[] decryptedPswIdentifier = new byte[eaxCipher.getOutputSize(nonceSize)];

				int procesedIDBytes = eaxCipher.processBytes(encryptedPswIdentifier, 0, nonceSize, decryptedPswIdentifier, nonceSize);
				eaxCipher.doFinal(decryptedPswIdentifier, procesedIDBytes);

				// compare:
				boolean check = Comparator.compare(pswIdentifier, decryptedPswIdentifier);

				if (check == false) {
					errorMessages[i] = "password failed";
					System.out.println("password failed: identifier not equal");
					try {
						Attachments.addNonce(f,  nonce);
						if (CipherStuff.isBound() == false){
							Attachments.addSalt(f, KeyDerivation.getAttachedSalt());
						}
						Attachments.addFileIdentifier(f);
						f.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					eaxCipher.reset();
					continue;
				} else {
					f.setLength( f.length() - (nonceSize * 2) );
				}
				eaxCipher.reset();
				
				byte[] block = new byte[FILE_BLOCK_SIZE];
				fileSize = (int) f.length();
				// round down: get block number except last block
				blockNumber = ( (fileSize - macSize) / FILE_BLOCK_SIZE);
				if ( (fileSize - macSize) % FILE_BLOCK_SIZE == 0) {
					blockNumber--;
				}

				//	byte[] associatedText = Attachments.getFileIdentifier();// 8 byte, not really necessary
				AEADParameters params = new AEADParameters(key, macSize * 8, nonce, null);//associatedText);
		 
				eaxCipher.init(false, params);
		 
				int processedBytes = 0;

				// process the blocks:
				for (blockCounter = 0; blockCounter < blockNumber; blockCounter++) { // only full blocks

					f.seek(blockCounter * FILE_BLOCK_SIZE);
					f.read(block, 0, FILE_BLOCK_SIZE);
			
					byte[] out = new byte[FILE_BLOCK_SIZE];
					processedBytes += eaxCipher.processBytes(block, 0, FILE_BLOCK_SIZE, out, 0);
			
					f.seek(blockCounter * FILE_BLOCK_SIZE);
					f.write(out, 0, FILE_BLOCK_SIZE );			
				}
		
				// process the last (maybe only) block:
				f.seek(FILE_BLOCK_SIZE * blockNumber);

				byte[] lastBlock = new byte[fileSize - (FILE_BLOCK_SIZE * blockNumber) ];				
				f.read(lastBlock, 0, lastBlock.length );
				
				byte[] lastOut = new byte[eaxCipher.getOutputSize(lastBlock.length)];
				processedBytes = 0; // reset				
				processedBytes += eaxCipher.processBytes(lastBlock, 0, lastBlock.length, lastOut, 0);
				processedBytes += eaxCipher.doFinal(lastOut, processedBytes); // + macSize

				f.seek(FILE_BLOCK_SIZE * blockNumber);
				f.write(lastOut, 0, lastOut.length );
				
				// truncate file (mac and extra bytes):
				f.setLength(FILE_BLOCK_SIZE * blockNumber + lastOut.length);

				f.close();

			} catch (FileNotFoundException e) {
				errorMessages[i] = "file not found";
				System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
				try {
					Attachments.addNonce(f,  lastNonce);
					Attachments.addFileIdentifier(f);
					f.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					errorMessages[i] += " - " + e1.toString();
					System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
					try{
						eaxCipher.reset();
					} catch (Exception e2) {
						System.err.println("EAXMode: Cipher reset failed");
					}
					continue;

				}
				try{
					eaxCipher.reset();
				} catch (Exception e1) {
					System.err.println("EAXMode: Cipher reset failed");
				}
				continue;
				//e.printStackTrace();
			} catch (IOException e) {
				errorMessages[i] = "read/write failed";
				System.err.println("CryptStuff " + e.toString() + ": "  + e.getMessage() + ", file: " + fileNames[i]);
				try {
					Attachments.addNonce(f, lastNonce);
					Attachments.addFileIdentifier(f);
					f.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					errorMessages[i] += " - " + e1.toString();
					System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
					try{
						eaxCipher.reset();
					} catch (Exception e2) {
						System.err.println("EAXMode: Cipher reset failed");
					}
					continue;
				}
				eaxCipher.reset();
				continue;
				//e.printStackTrace();
			} catch (InvalidCipherTextException icte) {// Authentication failed
				
				if(PswDialogBase.getWorkingMode().equals("-r")){ // rescue mode: 
					System.out.println("===============   Corrupted file   =====================");					
					System.out.println("Authentication failed, block: " + blockCounter + "  file: " + fileNames[i]);
					System.out.println("========================================================\n");					
				} else {// try to undo decryption:
					JOptionPane.showMessageDialog(PswDialogView.getView(),
						    "The file \n" + fileNames[i] + "\n has been corrupted at block " + blockCounter + ".\n\n"
						    + "The reason may be a simple error of the medium or a targeted manipulation.\n"
						    + "The Decryption of the file is cancelled. \n"
						    + "The program will now try to restore the original state.\n"
						    + "You can run this pea in rescue mode to decrypt this file anyway:\n "
						    + "java -jar " + PeaSettings.getJarFileName() + ".jar -r",
						    "Authentication failed",
						    JOptionPane.ERROR_MESSAGE);
					errorMessages[i] = "authentication failed - encrypted file was corrupted";
					System.err.println("CryptStuff " + icte.toString() + ", file: " + fileNames[i]);
					System.err.println("You can try to run in rescue modus (Parameter -r)\n java -jar THIS_PEA.jar -r");
					System.out.println("===============   Corrupted file   =====================");
					System.out.println("You can try to run in rescue modus (Parameter -r)\n java -jar THIS_PEA.jar -r");
					System.out.println("========================================================");
					//icte.printStackTrace();
					try {
						if (blockNumber == 0) { // only last block, nothing was written
							Attachments.addNonce(f, lastNonce);
							if (CipherStuff.isBound() == false){
								Attachments.addSalt(f, KeyDerivation.getAttachedSalt());
							}
							Attachments.addFileIdentifier(f);					
							f.close();
						} else { 
							System.out.println("Try to undo decryption...");
							// encrypt blocks
							 AEADParameters params = new AEADParameters(key, macSize * 8, lastNonce, null);//associatedText);					 
							 eaxCipher.init(true, params);							 
							// process the blocks:
							 byte[] repairBlock = new byte[FILE_BLOCK_SIZE];
							for (int j = 0; j < blockCounter; j++) { // only full blocks
								f.seek(j * FILE_BLOCK_SIZE);
								f.read(repairBlock, 0, FILE_BLOCK_SIZE);								
								byte[] out = new byte[FILE_BLOCK_SIZE];
								eaxCipher.processBytes(repairBlock, 0, FILE_BLOCK_SIZE, out, 0);								
								f.seek(j * FILE_BLOCK_SIZE);
								f.write(out, 0, FILE_BLOCK_SIZE );
							}
							// generate pswIdentifier:
							byte[] pswId = Attachments.generateNonce(); // 8 byte				 
							AEADParameters idParams = new AEADParameters(key, 0, lastNonce, null);// no mac				 
							eaxCipher.init(true, idParams);
							byte[] idOut = new byte[eaxCipher.getOutputSize(pswId.length)];
							int procesedIDBytes = eaxCipher.processBytes(pswId, 0, pswId.length, idOut, pswId.length);
							eaxCipher.doFinal(idOut, procesedIDBytes);
							eaxCipher.reset();
							// ad pswIdentifier: 
							f.seek( f.length() ); // set file pointer to end
							f.write(pswId, 0, pswId.length);
							f.seek(f.length() );
							f.write(idOut, 0, idOut.length);							
							
							Attachments.addNonce(f, lastNonce);
							if (CipherStuff.isBound() == false){
								Attachments.addSalt(f, KeyDerivation.getAttachedSalt());
							}
							Attachments.addFileIdentifier(f);	
							
							f.close();							
							eaxCipher.reset();
							continue;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						errorMessages[i] += " - " + e.toString();
						System.err.println("EAXMode " + e.toString() + ", file: " + fileNames[i]);
						try{
							eaxCipher.reset();
						} catch (Exception e1) {
							System.err.println("EAXMode: Cipher reset failed");
						}
						continue;
					}
					try{
						eaxCipher.reset();
					} catch (Exception e1) {
						System.err.println("EAXMode: Cipher reset failed");
					}
					continue;
					//e.printStackTrace();
				}
			} catch (Exception e) {
				errorMessages[i] = "unexpected error: " + e.getMessage();
				System.err.println("CryptStuff " + e.toString() + ": "  + e.getMessage() + ", file: " + fileNames[i]);
				try {
					Attachments.addNonce(f, lastNonce);
					if (CipherStuff.isBound() == false){
						Attachments.addSalt(f, KeyDerivation.getAttachedSalt());
					}
					Attachments.addFileIdentifier(f);
					f.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try{
					eaxCipher.reset();
				} catch (Exception e1) {
					System.err.println("EAXMode: Cipher reset failed");
				}
				continue;
				//e.printStackTrace();			
			}
			eaxCipher.reset();
		} // end for		

		//---------------
		// Handle keys:
		// encrypt or fill
		//----------------
		CipherStuff.getInstance().handleKey(key.getKey(), encryptBySessionKey);

		return errorMessages;
	}
}
