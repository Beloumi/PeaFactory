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


public class Comparator {

	/**
	 * Compare two byte arrays time independent
	 * 
	 * @param one
	 * @param two
	 * @return		true, if arrays are equal, false if not
	 */
	public static final boolean compare(byte[] one, byte[] two) {
		boolean equal = true;

		if (one == null && two == null){
			System.out.println("compared arrays null");
			return true;
		}
		if (one == null && two != null) {
			System.out.println("first compared array null");
			return false;
		}
		if (one != null && two == null) {
			System.out.println("second compared array null");
			return false;
		}
		
		int len = one.length;
		if (two.length != len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (one[i] != two[i]) {
				equal = false;
			}
		}
		return equal;	
	}
	
	
	/**
	 * Compare two char arrays time independent
	 * 
	 * @param one
	 * @param two
	 * @return		true, if arrays are equal, false if not
	 */
	public static final boolean compare(char[] one, char[] two) {
		boolean equal = true;
		int len = one.length;
		if (two.length != len) {
			return false;
		}
		for (int i = 0; i < len; i++) {
			if (one[i] != two[i]) {
				equal = false;
			}
		}
		return equal;	
	}
}
