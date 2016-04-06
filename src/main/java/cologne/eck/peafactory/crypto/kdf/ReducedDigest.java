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
 * Reduced digest for Catena
 */

import org.bouncycastle.crypto.Digest;

public interface ReducedDigest extends Digest {
	
	public void update(byte[] message);
	
	public void update(byte b);
	
	public void update(byte[] message, int offset, int len);
	
	public void reset();
	
	public String getName();

	public void setVertexIndex(int vIndex);
}
