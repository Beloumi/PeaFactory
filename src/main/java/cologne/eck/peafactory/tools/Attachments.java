package cologne.eck.peafactory.tools;

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
 * Attachments for byte blocks and files. 
 */


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.RandomStuff;


public final class Attachments {
	
	// Random values:
	private static byte[] programRandomBytes  = null; // unique for every program/dialog
	private static byte[] nonce = null; // unique for every encrypted content 
	private static byte[] fileIdentifier = null; // unique for every program/dialog

	
	protected final static int PSW_IDENTIFIER_SIZE = 8; // byte
	protected final static int FILE_IDENTIFIER_SIZE = 8; // byte
	protected final static int PROGRAM_RANDOM_BYTES_SIZE = KeyDerivation.getSaltSize();// at most 129
	protected final static int NONCE_SIZE = 8; // byte

	
	//========== Helper Functions: ==============================================
	
	private final static void addBytes(RandomAccessFile f, byte[] supplementBytes) 
			throws IOException {
		f.seek( f.length() ); // set file pointer
		f.write(supplementBytes, 0, supplementBytes.length ); 
	}
	public final static byte[] getLastBytes(RandomAccessFile f, int resultSize, boolean truncate) 
			throws IOException {
		
		if (resultSize < 0 || f.length() < resultSize) {
			throw new IllegalArgumentException("invalid value to get last bytes");
		}
			
		byte[] result = new byte[resultSize];

		long fileSize = 0;

		fileSize = f.length();
		if (fileSize - resultSize < 0) {
			throw new IllegalArgumentException( "invalid file size, file:  " + f.toString() );
		}
		f.seek(fileSize - resultSize); // set file pointer
		f.read(result, 0, resultSize ); // read bytes
		if (truncate == true) {
			cutLastBytes(f,  resultSize); // truncate file
		}
		return result;
	}
	// more secure cut (overwrites truncated bytes)
	private final static void cutLastBytes(RandomAccessFile f, int truncateSize) 
			throws IOException {
		if (truncateSize < 0 || f.length() < truncateSize) {
			throw new IllegalArgumentException("invalid value to cut last bytes");
		}
		byte[] nullBytes = new byte[truncateSize];
		f.seek(f.length() - truncateSize); // set file pointer
		f.write(nullBytes, 0, nullBytes.length);
		f.setLength(f.length() - truncateSize);
	}

	
	//=== psw identifier ======================================================
	
	// add the inverted last bytes to the end to check later if password fails
	// (invert: if file contains all the same bytes, this method would fail)
	// short files: padding identifier with 0xFF (toInvertBytes with 0)
	// exits if failed
/*	public final static void addPswIdentifier(RandomAccessFile f) 
			throws IOException{ 
		
		byte[] pswIdentifier = new byte[PSW_IDENTIFIER_SIZE]; // return value		

		long fileSize = f.length();
		if (fileSize == 0) {
			throw new IllegalArgumentException("empty file");
		}			
		
		byte[] toInvertBytes = null;
		if (fileSize < PSW_IDENTIFIER_SIZE) {
			byte[] lastBytes = getLastBytes(f, (int)fileSize, false);
			toInvertBytes = new byte[PSW_IDENTIFIER_SIZE];
			System.arraycopy(lastBytes, 0, toInvertBytes, 0, lastBytes.length); // rest 0
		} else {
			toInvertBytes = getLastBytes(f, PSW_IDENTIFIER_SIZE, false); 
		}
		for (int i = 0; i < PSW_IDENTIFIER_SIZE; i++) {
			pswIdentifier[i] = (byte) ~( (int) toInvertBytes[i]);
		}	
		addBytes(f, pswIdentifier);
		Arrays.fill(toInvertBytes, (byte) 0);
		Arrays.fill(pswIdentifier, (byte) 0);
	}*/
	public final static byte[] addPswIdentifier(byte[] plainBytes) {
		
		byte[] pswIdentifier = new byte[PSW_IDENTIFIER_SIZE]; // return value		

		if (plainBytes.length == 0) { // if no text: set space as plainBytes
			plainBytes = " ".getBytes();
		}	
		int inputSize = plainBytes.length;
			
		byte[] toInvertBytes = new byte[PSW_IDENTIFIER_SIZE];
		if (inputSize < PSW_IDENTIFIER_SIZE) {
			System.arraycopy(plainBytes, 0, toInvertBytes, 0, inputSize); // rest 0
		} else {
			System.arraycopy(plainBytes, inputSize - PSW_IDENTIFIER_SIZE, toInvertBytes, 0, PSW_IDENTIFIER_SIZE);
		}
		for (int i = 0; i < PSW_IDENTIFIER_SIZE; i++) {
			pswIdentifier[i] = (byte) ~( (int) toInvertBytes[i]);
		}	
		byte[] tmp = new byte[inputSize + PSW_IDENTIFIER_SIZE];
		System.arraycopy(plainBytes, 0, tmp, 0, plainBytes.length);
		System.arraycopy(pswIdentifier, 0, tmp, plainBytes.length, PSW_IDENTIFIER_SIZE);
		Zeroizer.zero(plainBytes);
		plainBytes = tmp;
		Zeroizer.zero(toInvertBytes);
		Zeroizer.zero(pswIdentifier);
		
		return plainBytes;
	}
		
	// truncates only if true
	public final static byte[] checkAndCutPswIdentifier(byte[] block) { 
		
		if (block.length <= PSW_IDENTIFIER_SIZE) {
			System.err.println("block too short for pswIdentifier");
			return null;
		}
		byte[] result = new byte[block.length - PSW_IDENTIFIER_SIZE];

		byte[] bytesToCheck = new byte[PSW_IDENTIFIER_SIZE];
		byte[] pswIdentifier = new byte[PSW_IDENTIFIER_SIZE];
		System.arraycopy(block, block.length - PSW_IDENTIFIER_SIZE, pswIdentifier, 0, PSW_IDENTIFIER_SIZE);
		if (block.length > PSW_IDENTIFIER_SIZE + PSW_IDENTIFIER_SIZE) {
			System.arraycopy(block, block.length - PSW_IDENTIFIER_SIZE - PSW_IDENTIFIER_SIZE, bytesToCheck, 0, PSW_IDENTIFIER_SIZE);

		} else { // padded pswIdentifier
			System.arraycopy(block, 0, bytesToCheck, 0, block.length - PSW_IDENTIFIER_SIZE);		
		}
		
		for (int i = 0; i < PSW_IDENTIFIER_SIZE; i++) {
			if ( ~bytesToCheck[i] != pswIdentifier[i]) {
				System.err.println("pswIdentifier failed at position " + i);
				return null;
			}
		}
		Zeroizer.zero(bytesToCheck);

		System.arraycopy(block, 0, result, 0, result.length);
		Zeroizer.zero(block);
		return result;
	}


	
	//=== file identifier ======================================================
	
	public final static void addFileIdentifier( RandomAccessFile f) 
			throws IOException { 
		
		if( fileIdentifier == null ) {
			throw new IllegalArgumentException ("fileIdentifier null");
		}		
		addBytes(f, fileIdentifier);
	}

	public final static byte[] addFileIdentifier(byte[] cipherBytes) {
		
		if(cipherBytes == null || fileIdentifier == null) {
			System.err.println("Attachments: add fileIdentifier failed: "
					+ "cipherBytes or fileIdentifier null");
			System.exit(1);
		}
		
		byte[] result = new byte[cipherBytes.length + fileIdentifier.length];
		System.arraycopy(cipherBytes,  0,  result,  0,  cipherBytes.length);
		System.arraycopy(fileIdentifier,  0,  
				result, cipherBytes.length, fileIdentifier.length);
		
		Zeroizer.zero(cipherBytes);
		
		return result;		
	}

	public final static boolean checkFileIdentifier( RandomAccessFile f, boolean truncate) 
			throws IOException { 
		
		if(f.length() < FILE_IDENTIFIER_SIZE ) {
			System.err.println("file too short to contain fileIdentifier");
			return false;
		}	

		byte[] checkIdentifier = getLastBytes(f, FILE_IDENTIFIER_SIZE, false);
		
		if (Arrays.equals(checkIdentifier, fileIdentifier)) {
			if (truncate == true) {
				cutLastBytes(f, FILE_IDENTIFIER_SIZE);
			}
			return true;
		} else {
			return false;
		}
	}
	
	// return null if check failed
	public final static byte[] checkFileIdentifier( byte[] input, boolean truncate) {
		
		byte[] result = new byte[input.length - FILE_IDENTIFIER_SIZE];	
		//Help.printBytes("inpt", input);
		if(input.length < FILE_IDENTIFIER_SIZE ) {
			System.err.println("input too short to contain fileIdentifier");
			return null;
		}	
		byte[] identifierToCheck = new byte[FILE_IDENTIFIER_SIZE];
		System.arraycopy(input, input.length - FILE_IDENTIFIER_SIZE, identifierToCheck, 0, FILE_IDENTIFIER_SIZE);
		
		if (Arrays.equals(identifierToCheck, fileIdentifier)) {
			if (truncate == true) {
							
				System.arraycopy(input, 0, result, 0, result.length);
				Zeroizer.zero(input);
				//input = tmp;				
			}
			return result;
		} else {
			return null;
		}
	}

	public final static void generateAndSetFileIdentifier() {
		fileIdentifier = new RandomStuff().createRandomBytes( Attachments.getFileIdentifierSize() );		
	}
	public final static void setFileIdentifier(byte[] _fileIdentifier) {
		fileIdentifier = _fileIdentifier;
	}
	public final static byte[] getFileIdentifier() {
//		if (fileIdentifier == null) {
//			System.err.println("Attachments getFileIdentifier: fileIdentifier null");
//		}
		return fileIdentifier;
	}
	
	//=== NONCE ======================================================
	
	public final static void addNonce( RandomAccessFile f, byte[] nonce) 
			throws IOException { 
		
		if( nonce == null ) {
			throw new IllegalArgumentException ("Nonce null");
		}		
		addBytes(f, nonce);
	}
	public final static byte[] addNonce(byte[] cipherBytes, byte[] nonce) {
		
		if(cipherBytes == null || nonce == null) {
			System.err.println("Attachments: addNonce failed: "
					+ "cipherBytes or nonce null");
			System.exit(1);
		}
		
		byte[] result = new byte[cipherBytes.length + nonce.length];
		System.arraycopy(cipherBytes,  0,  result,  0,  cipherBytes.length);
		System.arraycopy(nonce,  0,  
				result, cipherBytes.length, nonce.length);
		
		Zeroizer.zero(cipherBytes);
		return result;		
	}
	
	public final static byte[] getAndCutNonce( RandomAccessFile f, boolean truncate) 
			throws IOException { 
		//int cryptIvLength = CipherStuff.getCipherAlgo().getBlockSize();
		if(f.length() < NONCE_SIZE ) {
			throw new IllegalArgumentException("file is too short: " + f.toString() );
		}
		return  getLastBytes(f, NONCE_SIZE, truncate);
	}
	
	
	public final static byte[] generateNonce() {
		return new RandomStuff().createRandomBytes( NONCE_SIZE);//CipherStuff.getCipherAlgo().getBlockSize() );

	}
	
	public final static void setNonce(byte[] _nonce
			) { // required in PswDialogBase
		if (_nonce == null) {
			throw new IllegalArgumentException("Nonce to set null");
		}
		nonce = _nonce;
	}	
	public final static byte[] getNonce() {
		if (nonce == null) {
			//System.err.println("Attachments getCryptIV: cryptIV null");
		}
		return nonce;
	}
	public final static byte[] calculateNonce( byte[] input ) {
		//int cryptIvLength = CipherStuff.getCipherAlgo().getBlockSize();
		
		byte[] result = new byte[NONCE_SIZE];
		
		if (input.length < NONCE_SIZE) {
			System.err.println("Attachments calculateNonce: input too short");
			return null;
		} else {
			System.arraycopy(input, input.length - NONCE_SIZE, result, 0, result.length);
			return result;
		}
	}
	
	public final static byte[] cutNonce(byte[] input) {	
				
		if(input.length < NONCE_SIZE ) {
			System.err.println("input is too short to contain Nonce. ");
			return null;
		} 
		byte[] result = new byte[input.length - NONCE_SIZE];
		System.arraycopy(input, 0, result, 0, result.length);
		Zeroizer.zero(input);
		
		return result;
	}
	
	//===================================================
	// Getter & Setter & Generators
	
	public final static void generateAndSetProgramRandomBytes() {
		programRandomBytes = new RandomStuff().createRandomBytes( Attachments.getProgramRandomBytesSize() );		
	}
	public final static void setProgramRandomBytes( byte[] _programRandomBytes) {
		programRandomBytes = _programRandomBytes;
	}
	public final static byte[] getProgramRandomBytes() {
		if (programRandomBytes == null) {
			System.err.println("Attachments getProgramRandomBytes: programRandomBytes null");
		}
		return programRandomBytes;
	}

	
	public final static int getProgramRandomBytesSize() {
		return PROGRAM_RANDOM_BYTES_SIZE;
	}
	public final static int getPswIdentifierSize() {
		return PSW_IDENTIFIER_SIZE;
	}
	public final static int getFileIdentifierSize() {
		return FILE_IDENTIFIER_SIZE;
	}
	public final static int getNonceSize() {
		return NONCE_SIZE;
	}
	public static byte[] attachBytes(byte[] sourceBytes, byte[] bytesToAttach) {
		
		if(sourceBytes == null || bytesToAttach == null) {
			System.err.println("Attachments: attachedBytes failed");
			System.exit(1);
		}
		
		byte[] result = new byte[sourceBytes.length + bytesToAttach.length];
		System.arraycopy(sourceBytes,  0,  result,  0,  sourceBytes.length);
		System.arraycopy(bytesToAttach,  0,  
				result, sourceBytes.length, bytesToAttach.length);
		
		Zeroizer.zero(sourceBytes);
		return result;	
	}	
	public final static void addSalt( RandomAccessFile f, byte[] attachedSalt) 
			throws IOException { 
		
		if( attachedSalt == null ) {
			throw new IllegalArgumentException ("Attached Salt null");
		}		
		addBytes(f, attachedSalt);
	}
	public final static void cutSalt( RandomAccessFile f) 
			throws IOException { 
		//int cryptIvLength = CipherStuff.getCipherAlgo().getBlockSize();
		
		if(f.length() < PROGRAM_RANDOM_BYTES_SIZE ) {
			throw new IllegalArgumentException("file is too short: " + f.toString() );
		}
		cutLastBytes(f, PROGRAM_RANDOM_BYTES_SIZE);
	}
	public final static byte[] cutSalt(byte[] input) {	
		
		if(input.length < PROGRAM_RANDOM_BYTES_SIZE ) {
			System.err.println("input is too short to contain the salt. ");
			return null;
		} 
		byte[] result = new byte[input.length - PROGRAM_RANDOM_BYTES_SIZE];
		System.arraycopy(input, 0, result, 0, result.length);
		Zeroizer.zero(input);
		
		return result;
	}
	public final static byte[] getSalt(byte[] cipherBytes) { 
		byte[] result = new byte[PROGRAM_RANDOM_BYTES_SIZE];
		System.arraycopy(cipherBytes,  0,  result,  0,  result.length);
		return result;
	}
	public final static byte[] getAndCutSalt( RandomAccessFile f, boolean truncate) 
			throws IOException { 
		if(f.length() < PROGRAM_RANDOM_BYTES_SIZE ) {
			throw new IllegalArgumentException("file is too short: " + f.toString() );
		}
		return  getLastBytes(f, PROGRAM_RANDOM_BYTES_SIZE, truncate);
	}
	public final static byte[] getEndBytesOfFile( String fileName, int resultLen) 
			throws IOException { 
		
		RandomAccessFile f = new RandomAccessFile( fileName, "r" );
		
		if(f.length() < resultLen) {
			f.close();
			throw new IllegalArgumentException("file is too short: " + f.toString() );
		}
		byte[] result = getLastBytes(f, resultLen, false);
		f.close();
		return result;
	}
}
