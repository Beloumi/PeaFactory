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
 * Interface for Password Hashing Schemes. 
 * This is based on the standard function of the Password Hashing Competition,
 * but does not completely corresponds to the (C-style) prototype.
 */


public interface PasswordHashingScheme {
	
	  static final int PASSWORD_HASHING = 0;// in Catena called: Password Scrambler
	  	// most used for Authentication
	  static final int KEY_DERIVATION = 1;
	  static final int PROOF_OF_WORK = 2; // Catena 

    /**
     * Return the name of the password hashing scheme
     *
     * @return 
	 * 			the name of the password hashing scheme
     */
    public String getAlgorithmName();

    /**
     * Calculate the password hash. This can be used for key derivation,
     * authentication or any other purpose. If these modes differ, this 
     * must be determined by the varArgs parameter. 
     * 
     * @param 	outlen
     * 					required length in byte for the password hash
     * @param 	in 
	 *					the password to be hashed
     * @param 	salt
     * 					a salt value
     * @param 	t_cost 
	 *					the time cost parameter
     * @param 	m_cost
     * 					the memory cost parameter
     * @param 	varArgs
     * 					additional parameters, not used by all schemes
     * 
     * @return the password hash
     * 
     * @throws Exception 
     * 					
     */
    public byte[] hashPassword(int outlen, byte[] in, byte[] salt, int t_cost, int m_cost, Object ... varArgs) throws Exception; 


	/**
	 * indicates if zeroization of password is performed or not
	 * 
	 * @return the wipePassword value
	 */
	public boolean isWipePassword();

	/**
	 * zeroize the password or keep it
	 * 
	 * @param _wipe 
	 * 					true: wipe the password as soon as 
	 * 					possible 
	 * 					false: keep it for later use
	 */
	public void setWipePassword(boolean _wipe);
}
