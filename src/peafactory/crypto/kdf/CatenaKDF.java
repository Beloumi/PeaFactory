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
 * Password Hashing Scheme Catena (v3) version v3.2 as key derivation function
 */

import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;

public class CatenaKDF extends KeyDerivation {
	
	// Default setting:
	private static String versionID = "Dragonfly-Full";
	private static int lambdaDragonfly = 2;
	private static int garlicDragonfly = 18;
	
	private static int lambdaButterfly = 4;
	private static int garlicButterfly = 14;

	public CatenaKDF() {
		if ((versionID.equals("Dragonfly-Full")) || (versionID.equals("Dragonfly"))){
			settCost(lambdaDragonfly);
			setmCost(garlicDragonfly);	    	
			setArg3(0);
			setArg4(0);
			setArg5(0);
			setArg6(0);
			setVersionString(versionID);
			setKdf(this);
		} else if ((versionID.equals("Butterfly-Full")) || (versionID.equals("Butterfly"))){
			settCost(lambdaButterfly);
			setmCost(garlicButterfly);	    	
			setArg3(0);
			setArg4(0);
			setArg5(0);
			setArg6(0);
			setVersionString(versionID);
			setKdf(this);
		} else {
			System.err.println("Invalid Catena version ID");
		}
		setKdf(this);
	}

	@Override
	public byte[] deriveKey(byte[] pswMaterial) {
		
		Catena cat = null;
		if (getVersionString().equals("Dragonfly-Full")){
			cat = new CatenaBRG(false, true); // not fast, wipe password
			cat.setFast(false);
		} else if (getVersionString().equals("Butterfly-Full")){
			cat = new CatenaDBG(false, true);
			cat.setFast(false);
		} else if (getVersionString().equals("Butterfly")){
			cat = new CatenaDBG(true, true);
			cat.setFast(true);
			cat.setReducedDigest(new Catena_Blake2b_1());
		} else if (getVersionString().equals("Dragonfly")){
			cat = new CatenaBRG(true, true);
			cat.setFast(true);
			cat.setReducedDigest(new Catena_Blake2b_1());
		} else {
			throw new IllegalArgumentException("invalid version ID");
		}
		byte[] key = new byte[CipherStuff.getKeySize()];
		//System.out.println("butterfly " + lambdaButterfly + "  " + gettCost());
		cat.deriveKey(
				pswMaterial, getSalt(), getExtraValues(),  
		       gettCost(), getmCost(), getmCost(), 
		       0, key); // key id = 0
		printInfos(true);
		return key;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Catena";
	}

	/**
	 * @return the versionID
	 */
	public static String getVersionID() {
		return versionID;
	}

	/**
	 * @param versionID the versionID to set
	 */
	public static void setVersionID(String versionID) {
		CatenaKDF.versionID = versionID;
	}

	/**
	 * @return the lambdaDragonfly
	 */
	public static int getDragonflyLambda() {
		return lambdaDragonfly;
	}

	/**
	 * @param lambdaDragonfly the lambdaDragonfly to set
	 */
	public static void setLambdaDragonfly(int lambda) {
		CatenaKDF.lambdaDragonfly = lambda;
	}

	/**
	 * @return the garlicDragonfly
	 */
	public static int getGarlicDragonfly() {
		return garlicDragonfly;
	}

	/**
	 * @param garlicDragonfly the garlicDragonfly to set
	 */
	public static void setGarlicDragonfly(int garlic) {
		CatenaKDF.garlicDragonfly = garlic;
	}

	/**
	 * @return the lambdaButterfly
	 */
	public static int getLambdaButterfly() {
		return lambdaButterfly;
	}

	/**
	 * @param lambdaButterfly the lambdaButterfly to set
	 */
	public static void setLambdaButterfly(int lambdaButterfly) {
		CatenaKDF.lambdaButterfly = lambdaButterfly;
	}

	/**
	 * @return the garlicButterfly
	 */
	public static int getGarlicButterfly() {
		return garlicButterfly;
	}

	/**
	 * @param garlicButterfly the garlicButterfly to set
	 */
	public static void setGarlicButterfly(int garlicButterfly) {
		CatenaKDF.garlicButterfly = garlicButterfly;
	}
}
