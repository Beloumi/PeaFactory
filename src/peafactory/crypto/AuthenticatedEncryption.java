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
 * Interface for mode of operation of authenticated encryption/decryption
 */
import cologne.eck.peafactory.peas.file_pea.FileTypePanel;

public interface AuthenticatedEncryption {
	
	public byte[] processBytes(	
			boolean forEncryption, 
			byte[] input, 
			byte[] key, 
			byte[] nonce);
	
	public String[] decryptFiles( String[] fileNames, byte[] keyMaterial, 
			boolean encryptBySessionKey, FileTypePanel filePanel);
	
	public String[] encryptFiles( String[] fileNames, byte[] keyMaterial, 
			boolean encryptBySessionKey, FileTypePanel filePanel);
}
