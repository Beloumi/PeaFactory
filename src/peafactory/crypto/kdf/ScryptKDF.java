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

import org.bouncycastle.crypto.generators.SCrypt;

import cologne.eck.peafactory.crypto.KeyDerivation;
//import cologne.eck.peafactory.tools.Help;

public class ScryptKDF extends KeyDerivation {
	
	private static int memoryFactor = 32; // r Memory cost parameter
	private static int cPUFactor = 16384;//16384; // N CPU cost parameter
	private static int parallelFactor = 1;// 1; // p Parallelization parameter

	public ScryptKDF() {
		settCost(cPUFactor);
		setmCost(memoryFactor);
		setArg3(parallelFactor);
		setArg4(0);
		setArg5(0);
		setArg6(0);
		setVersionString("");
		setKdf(this);
	}

	@Override
	public byte[] deriveKey(byte[] pswMaterial) {
/*
System.out.println("=== ScryptKDF deriveKey:");
System.out.println("memoryFactor: " + memoryFactor 
		+ ", cpuFactor: " + cPUFactor
		+ ", parallelFactor: " + parallelFactor);
Help.printBytes("Salt", KeyDerivation.getSalt());
Help.printBytes("pswMaterial", pswMaterial);
*/		

		if (KeyDerivation.getSalt().length < 16) {
			System.err.println("Warning: Scrypt: short salt.");
		}
		if(KeyDerivation.getSalt().length < 8) {
			System.err.println("Srypt: salt too short");
			throw new IllegalArgumentException("Scrypt - invalid salt size");
		}

		//long start =  System.currentTimeMillis(); // Startpunkt	
		byte[] keyMaterial = null;
		try {
		    keyMaterial = SCrypt.generate(pswMaterial, KeyDerivation.getSalt(), 
		    		cPUFactor, memoryFactor, parallelFactor, 64);			
		} catch (Exception e) {
			System.err.println("ScryptKDF Exception.");
			e.printStackTrace();
		}
		//System.out.println("Scrypt: " + cPUFactor + " iterations, " + memoryFactor + " memory factor" );
		printInfos(true);
		
		keyMaterial = adjustKeyMaterial(keyMaterial);
//Help.printBytes("keyMaterial", keyMaterial);
		return keyMaterial;
	}

	@Override
	public String getName() {
		return "Scrypt";
	}

	/**
	 * @return the memoryFactor
	 */
	public static int getMemoryFactor() {
		return memoryFactor;
	}

	/**
	 * @param memoryFactor the memoryFactor to set
	 */
	public static void setMemoryFactor(int _memoryFactor) {
		memoryFactor = _memoryFactor;
	}

	/**
	 * @return the cPUFactor
	 */
	public static int getcPUFactor() {
		return cPUFactor;
	}

	/**
	 * @param cPUFactor the cPUFactor to set
	 */
	public static void setcPUFactor(int _cPUFactor) {
		cPUFactor = _cPUFactor;
	}

	/**
	 * @return the parallelFactor
	 */
	public static int getParallelFactor() {
		return parallelFactor;
	}

	/**
	 * @param parallelFactor the parallelFactor to set
	 */
	public static void setParallelFactor(int _parallelFactor) {
		parallelFactor = _parallelFactor;
	}
}
