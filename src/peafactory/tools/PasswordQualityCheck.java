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
 * This class checks the quality of a password. 
 * This is only a simple check without a dictionary test. 
 * 
 */

/* Used in:
 * 			 cologne.eck.peafactory.gui.MainView.
 * 
 *  Note: This is just a simple test. It should 
 *  give only a vague clue. 
 */

public class PasswordQualityCheck {

	/**
	 * Check the quality of a password
	 * 
	 * @param password	the password as an array of chars
	 * 
	 * @return			the quality, a numeric value that
	 * 					indicates the strength of the password, 
	 * 					higher values indicate stronger passwords
	 */
	public static int checkQuality (char[] password) {
		
		int len = password.length;
		if (len == 0) {
			return 0;
		}
		
		// check worst password:
		if (Comparator.compare(password, "password".toCharArray()) == true) {
			return 0;
		}		
		
		Double bonusPoints = 0.0;
		
		int digit = 0;
		int upperCase = 0;
		int lowerCase = 0;
		int specialChar = 0;
		int tableChar1 = 0;
		int tableChar2 = 0;
		int tableChar3 = 0;
		int tableCharX = 0;
	
		char c = '\0';
		
		// get some statistics about the password:
		// How many digits, upper case...
		for (int i = 0; i < password.length; i++) {

			c = password[i];

			if (c > 47 && c < 58){ // 0-9
				digit++;
			} else if (c > 64 && c < 91) {// A-Z
				upperCase++;
			} else if (c > 96 && c < 123) {// a - z
				lowerCase++;
			} else if (c < 128) { // ~[{(^|@?... special character
				specialChar++;
			} else if (c > 128 && c < 50072 ){ // c >= 128: character from table
				tableChar1++;
			} else if (c >= 50072 && c < 50606) { // table 2
				tableChar2++;
			} else if (c >= 50606 && c < 51341) { // table 3
				tableChar3++;
			} else { // table > 3
				tableCharX++;
			}
		}
		c = '\0';
		
		//-------------------------------
		// Bonus Points and Malus Points:
		//
		
		// Password length:
		bonusPoints = bonusPoints + len;
		
		//System.out.println("1: " + bonusPoints);
			
		// check if only one sort: only digit or only...
		if (digit == len || lowerCase == len || upperCase == len) {
			bonusPoints /= 1.25;
		} else {		
			// check variety:
			if (digit > 0){
				bonusPoints++;
			}
			if (upperCase > 0) {
				bonusPoints++;
			}
			if (lowerCase > 0) {
				bonusPoints++;
			}
			if (specialChar > 0) {
				bonusPoints += specialChar;
			}
		}
		
		// check characters from tables: 
		if (tableChar1 > 0) {
			bonusPoints += tableChar1 + 2;
		}
		if (tableChar2 > 0) {
			bonusPoints += tableChar2 + 5;
		}
		if (tableChar3 > 0) {
			bonusPoints += tableChar3 + 8;
		}
		if (tableCharX > 0) {
			bonusPoints += tableCharX + 10;
		}
		
		//System.out.println("2: " + bonusPoints);
		
		// check succession: for example abcde or 1234
		// and equality: for example AAAAAAAAA
		// Decrement 1/2 for each
		int previous = 0;
		for (int i = 0; i < password.length; i++) {
			c = password[i];
			if (c == previous || c == previous+1) {
				bonusPoints = bonusPoints - 0.3;
			}
			previous = c;				
		}
		c = '\0';
		
		
		//System.out.println("3: " + bonusPoints);
		
		if (bonusPoints < 1) {
			bonusPoints = 1.0;
		}
		return bonusPoints.intValue();
	}
	
/*	// for testing only:
	public static void main(String[] args) {
		String s = "123456789yjrnc~&";
		int x = checkQuality(s.toCharArray() );
		System.out.println("Quality: " + x);
	}*/
}
