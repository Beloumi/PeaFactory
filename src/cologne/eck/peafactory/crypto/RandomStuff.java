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
 * Generation of random values, also random passwords. 
 */


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.EntropyPool;
import cologne.eck.peafactory.tools.Zeroizer;


public class RandomStuff {
	
	
	private final static int pswLength = 50;
	
	
	// 90 Printable 7-Bit ASCII (without "  \ 0)
	private final static char[] allChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789!#$%&()*+,-./:;<=>?@[]^_'{|}~".toCharArray();
	private static byte[] personalBytes = null;
	
	private static int entropyPoolCounter = 0;
	private static final int POOL_SIZE = EntropyPool.getPoolSize();
	
	private final static int PERSONAL_BYTES_SIZE = 16;
	private final static int RESEED_BYTES_SIZE = 12;
	
	/**
	 * Create random values
	 * 
	 * @param length	number of bytes (size of output)
	 * @return			the random values
	 */
	public final byte[] createRandomBytes(int length) {				
		
		byte[] result = new byte[length];// return value
		
		byte[] seed = new byte[52];
		int seedIndex = 0;
		
		// seed of SecureRandom alone might not be appropriate for keys > 20 byte because it uses state of SHA-1
		byte[] scSeed = new byte[20];
		SecureRandom sc = new SecureRandom(); // get system random bytes if available
		sc.nextBytes(scSeed);
		for (int i = 0; i < scSeed.length; i++) {
			seed[i] = scSeed[i];
			scSeed[i] = (byte) 0;
		}
		seedIndex += scSeed.length;
		//Help.printBytes("seed scSeed", seed);

		
		// get values from RandomCollector: 		
		byte[] tableSeed = getAndUpdateTableValue( 2 );
		System.arraycopy(tableSeed,  0,  seed,  seedIndex, tableSeed.length);
		Zeroizer.zero(tableSeed);
		seedIndex += tableSeed.length;


		// add personal bytes: 16 byte
		if (personalBytes == null) {
			personalBytes = generatePersonalBytes();// 16 byte
		}
		System.arraycopy(personalBytes,  0,  seed,  seedIndex, PERSONAL_BYTES_SIZE);
		//Help.printBytes("seed personalBytes", seed);


		// hash seed
		byte[] data = HashStuff.hashAndOverwrite(seed);
		Zeroizer.zero(seed);
		
		// truncate if output size of hash >= required length
		if (data.length >= length) {
			
			System.arraycopy(data,  0,  result,  0,  length);
	
		} else {
		
			// expand data to result.length:
			int dataIndex = 0; // index of hash value
			int resultIndex = 0; // index of result
			while (resultIndex < length) {
				
				while ( dataIndex < data.length && resultIndex < result.length){ 
					result[resultIndex++] = data[dataIndex++];
				}
				//Help.printBytes("result",  result);
				if (resultIndex < result.length){
					// create new data: 
					byte[] newSeed = new byte[data.length 
					                          + RESEED_BYTES_SIZE 
					                          + PERSONAL_BYTES_SIZE];// reseed with extraSeed + personalBytes
					int newSeedIndex = 0;
					
					System.arraycopy(data,  0,  newSeed,  0,  data.length);// 64 byte
					Zeroizer.zero(data);
					newSeedIndex += data.length;
					// reseed:
					byte[] newExtraSeed = generateExtraSeed(); // 12 byte
					System.arraycopy(newExtraSeed, 0, newSeed, newSeedIndex, RESEED_BYTES_SIZE);
					Zeroizer.zero(newExtraSeed);
					newSeedIndex += newExtraSeed.length;
					// add personal bytes:
					System.arraycopy(personalBytes,  0,  newSeed, newSeedIndex, PERSONAL_BYTES_SIZE);
					
					data = HashStuff.hashAndOverwrite(newSeed);
					dataIndex = 0;				
				}
			}
		}
		Zeroizer.zero(seed);
		Zeroizer.zero(data);
		return result;
	}
	
	//
	// two independent random functions: first mix allChars pseudo-randomly 
	// then choose chars pseudo-randomly
	//
	/**
	 * Create an array of characters randomly
	 * 
	 * @return	randomly chosen array of characters
	 */
	public final char[] createNewRandomChars() {
		
		char[] newRandomChars = new char[pswLength];// return value
		
		int[] mixPositions = getMixPositions (allChars.length / 2, 100);
		mixChars(mixPositions);		
		Zeroizer.zero(mixPositions);

		SecureRandom sRandom = new SecureRandom();

		for(int i = 0; i < pswLength; i++) {
			newRandomChars[i] = allChars[sRandom.nextInt(allChars.length)];
		}				
		return newRandomChars;
	}
	
	//
	// get array of values > 0 and < maxValue to mix allChars
	// mixNumber = times to swap values / 2
	//
	private final int[] getMixPositions (int maxValue, int mixNumber) {
		int[] result = new int[mixNumber];
		
		byte[] hash = createRandomBytes(mixNumber * 2);// this may not be enough		
		
		// get values from hash and fill result, compute new hashes if necessary
		int resultPosition = 0;
		int inputPosition = 0;
		while(resultPosition < mixNumber) {
			if ( (Math.abs(hash[inputPosition])) < maxValue) {
				result[resultPosition] = Math.abs(hash[inputPosition] );
				hash[inputPosition] = (byte) 0xFF;
				resultPosition++;
				inputPosition++;
			} else { 
				// throw away: 
				inputPosition++;
			}
			if (inputPosition >= hash.length) {
				// new hash
				hash = HashStuff.hashAndOverwrite(hash);
				inputPosition = 0;
			}
		}
		Zeroizer.zero(hash);
		return result;
	}
	
	//
	// mix allChars with positions > 0 and < allChars/2
	private final void mixChars(int[] mixPositions) {
		
		char swap;
		int half = allChars.length/2;
		for (int i = 0; i < mixPositions.length; i+=2) {
			swap = allChars[ mixPositions[i] ];
			allChars[ mixPositions[i] ] = allChars[ mixPositions[i+1] + half];
			allChars[ mixPositions[i+1] + half] = swap;
		}
		Zeroizer.zero(mixPositions);
	}
	
	
	private final byte[] generateExtraSeed() {
		byte[] seed = new byte[RESEED_BYTES_SIZE];
		int index = 0;
			
		// nanoTime: 8 bytes (four of them are mostly null) -> take 4 bytes
		long nanoSeed =  System.nanoTime();
		ByteBuffer nanoBuffer = ByteBuffer.allocate(8);
		byte[] nanoBytes = nanoBuffer.putLong(nanoSeed).array();
		System.arraycopy(nanoBytes,  2, seed ,index, 4);
		Zeroizer.zero(nanoBytes);
		index += 4; 
		//Help.printBytes("seed nano", seed);
		
		if (index >= RESEED_BYTES_SIZE){
			return seed;
		}
		    
		// currentTime: 4 bytes (two of them may be null, two are mostly the same) -> take 4 bytes
		long timeSeed =  System.currentTimeMillis(); 
		ByteBuffer buffer = ByteBuffer.allocate(8);
		byte[] timeBytes = buffer.putLong(timeSeed).array();
		System.arraycopy(timeBytes,  4, seed, index, 4);
		Zeroizer.zero(timeBytes);
		index += 4;
		//Main.printBytes("seed nano + time", seed);
		
		if (index >= RESEED_BYTES_SIZE){
			return seed;
		}
		    
		// free memory: 4 bytes (bad values)
		long freeMemory = Runtime.getRuntime().freeMemory();
		seed[index++] = (byte) freeMemory;
		seed[index++] = (byte) (freeMemory >>> 8);
		seed[index++] = (byte) (freeMemory >>> 16);
		seed[index++] = (byte) (freeMemory >>> 32);
		freeMemory = 0;
		//Main.printBytes("seedForStir time + pid + loc + mem", seed);	
		
		if (index >= RESEED_BYTES_SIZE){
			return seed;
		}
		
		// tmp files
/*		File f = new File(System.getProperties().getProperty("java.io.tmpdir"));
		File[] tempList =  f.listFiles();
		// number of temp files: 2 bytes
		seed[index++] = (byte) tempList.length;
		seed[index++] = (byte) (tempList.length >>> 8);
		// choose two files "light randomly" and get size: 4 bytes
		Random rand = new Random();
		int randomInt = rand.nextInt(tempList.length - 1);
		long fileSize = tempList[randomInt].length();
		seed[index++] = (byte) fileSize;
		seed[index++] = (byte) (fileSize >>> 8);
		randomInt = rand.nextInt(tempList.length - 1);
		fileSize = tempList[randomInt].length();
		seed[index++] = (byte) fileSize;
		seed[index++] = (byte) (fileSize >>> 8);
		randomInt = 0;*/

		 //Help.printBytes("seed", seed);		
		 return seed;
	}
	

	
	private final byte[] generatePersonalBytes() {
		
		byte[] personalBytes = new byte[PERSONAL_BYTES_SIZE];
		int index = 0;

		// ip address of local host: 4 byte
		byte[] ipBytes = new byte[4];
		try {
			InetAddress ia = InetAddress.getLocalHost();
			ipBytes = ia.getAddress();
			if (ipBytes != null) {
				System.arraycopy(ipBytes,  0,  personalBytes, index,  ipBytes.length);
				index += ipBytes.length;
			} else {
				System.err.println("RandomStuff: personal bytes for random seed: ip adress null");
			}
		} catch (UnknownHostException e) {
			System.err.println("RandomStuff: " + e.toString() + ": personal bytes for random seed: ip adress failed");
		} catch (NullPointerException e) {
			System.err.println("RandomStuff: " + e.toString() + ": personal bytes for random seed: ip adress failed");
		}
		
		// fill rest with system properties
		byte[] systemProps = ( System.getProperty("user.name") + System.getProperty("user.dir")
				+ System.getProperty("os.version")  ).getBytes();//+ System.getProperty("sun.boot.class.path")	
		for (int i = 0; i < systemProps.length && index < PERSONAL_BYTES_SIZE; i++) {
			personalBytes[index++] = systemProps[i];
		}			
		return personalBytes;
	}
	
	// get values of RandomCollectors table and overwrite these values
	// with new values from hash
	private final byte[] getAndUpdateTableValue(int number){
		EntropyPool rc = EntropyPool.getInstance();
		
		byte[] result = new byte[number * 8];
		byte[] tableSeed = null;
		for (int i = 0; i < number; i++) {

			// get 8 bytes from one value of the table
			tableSeed = Converter.long2bytesBE(rc.getValue(true));
			entropyPoolCounter++;
			//System.out.print(" " + entropyPoolCounter);
			if (entropyPoolCounter >= POOL_SIZE){
				reseedEntropyPool(rc);
			}
			// store in result:
			System.arraycopy(tableSeed,  0, result, i * 8, tableSeed.length);
		}
		return result;
	}
	// The pool is always reseeded if entropyPoolCounter >= POOL_SIZE
	// so every value is used at most once
	private final void reseedEntropyPool(EntropyPool ep){
		
		//System.out.println("\nreseed, counter: " + entropyPoolCounter);
		
		byte[] updateBytes = new byte[POOL_SIZE * 8];
		int hashLen = HashStuff.getHashAlgo().getDigestSize();
		// get old value from pool to hash
		byte[] reseedBytes = Converter.longs2bytesBE(ep.getValuesToReseed());// 32 bytes			
		// hash values
		byte[] hashedBytes = HashStuff.hash(reseedBytes);
		if (hashLen >= updateBytes.length) {
			System.arraycopy(hashedBytes,  0,  updateBytes,  0,  updateBytes.length);
		} else {
			
			int index = 0;
			while (index < updateBytes.length) {				
				hashedBytes = HashStuff.hash(hashedBytes);
				System.arraycopy(hashedBytes,  0,  updateBytes,  index,  
						(index + hashLen > updateBytes.length) ? (updateBytes.length - index) : hashedBytes.length);
				index += hashLen;
			}
		}

		// convert to long[]
		long[] reseedValues = Converter.bytes2longsLE(updateBytes);
		// reseed the pool
		ep.reseedPool(reseedValues);
		Zeroizer.zero(updateBytes);
		Zeroizer.zero(reseedBytes);
		Zeroizer.zero(hashedBytes);
		Zeroizer.zero(reseedValues);
		entropyPoolCounter = 0;
	}
}
