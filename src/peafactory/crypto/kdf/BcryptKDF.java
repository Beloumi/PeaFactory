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
 * Class to call Bcrypt as key derivation function
 */

import org.bouncycastle.crypto.generators.BcryptCore;

import cologne.eck.peafactory.crypto.KeyDerivation;

public class BcryptKDF extends KeyDerivation {
	
	private static int rounds = 11;

	public BcryptKDF() {
		settCost(rounds);
		setmCost(0);
		setArg3(0);
		setArg4(0);
		setArg5(0);
		setArg6(0);
		setVersionString("");
		setKdf(this);
	}

	@Override
	public byte[] deriveKey(byte[] pswMaterial) {

		byte[] salt = getSalt();
		if (salt.length != 16) {
			if (salt.length > 16) {
				System.out.println("Warning: Bcrypt uses only 16 byte salt - salt is truncated");
				byte[] tmp = new byte[16];
				System.arraycopy(getSalt(), 0, tmp, 0, tmp.length);
				salt = tmp;
			}
			//throw new IllegalArgumentException("Bcrypt - invalid salt size");
		}

		byte[] keyMaterial = null;
		try {
		    keyMaterial = new BcryptCore().deriveRawKey(
		    		gettCost(), 
		    		salt, 
		    		pswMaterial);
		    } catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println("Bcrypt:  2 ^" + rounds + " iterations" );
		printInfos(true);

		keyMaterial = adjustKeyMaterial(keyMaterial);

		return keyMaterial;
	}

	@Override
	public String getName() {
		return "Bcrypt";
	}

	/**
	 * @return the rounds
	 */
	public static int getRounds() {
		return rounds;
	}

	/**
	 * @param rounds the rounds to set
	 */
	public static void setRounds(int _rounds) {
		rounds = _rounds;
	}
}
