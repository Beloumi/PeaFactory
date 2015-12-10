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
 * Zeroization of sensitive data. 
 */


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/*
 * Class to prevent compilers' redundant code elimination. 
 * 
 */

public class Zeroizer {
	
	private static byte zero8bit = 0;
	private static char zeroChar = '\0';
	private static int zero32bit = 0;
	private static long zero64bit = 0L;
	
	public final static void zero(byte[] input) {		
		if (input != null && input.length > 0) {
			Arrays.fill(input,  (byte) 0);		
			/*for (int i = 0; i < input.length; i++) {
				zero8bit |= input[i];
			} */
			zero8bit |= input[input.length - 1];
		}
	}
	public final static void zero(int[] input) {	
		if (input != null && input.length > 0) {
			Arrays.fill(input, 0);		
			/*for (int i = 0; i < input.length; i++) {
				zero32bit |= input[i];
			}*/
			zero32bit |= input[input.length - 1];
		}
	}	
	public final static void zero(long[] input) {		
		if (input != null && input.length > 0) {
			Arrays.fill(input, 0);		
			/*for (int i = 0; i < input.length; i++) {
				zero64bit |= input[i];
			}*/
			zero64bit |= input[input.length - 1];
		}
	}		
	public final static void zero(char[] input) {			
		/*for (int i = 0; i < input.length; i++) {
			zeroChar |= input[i];
		}*/
		if (input != null && input.length > 0) {
			Arrays.fill(input,  '\0');	
			zeroChar |= input[input.length - 1];
		}
	}
	
	public final static void zero(byte zero) {
		zero = (byte) 0;
		zero8bit |= zero;
	}
	public final static void zero(int zero) {
		zero = 0;
		zero32bit |= zero;
	}
	public final static void zero(long zero) {
		zero = (byte) 0;
		zero64bit |= zero;
	}
	public final static void zero(char zero) {
		zero = '\0';
		zeroChar |= zero;
	}
	public final static void zeroFile(String fileName)  {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(fileName, "rw");
			byte[] zeroBytes = new byte[(int)raf.length()];
			raf.seek(0);
			raf.write(zeroBytes);
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * To check the execution, this function should be 
	 * called as last step before the application exits
	 */
	public final static void getZeroizationResult() {
		
		if (zeroChar != '\0') {
			System.err.println("Zeroization failed - char: " + zeroChar); 
		}

		if ( (zero8bit != 0) || (zero32bit != 0) || (zero64bit != 0)) {
			System.err.println("Zeroization failed - \nbyte: " + zero8bit 
					+ "\nint: " + zero32bit
					+ "\nlong: " + zero64bit);
		} else {
			System.out.println("Zeroization: success");
		}
	}
	
	// Test:
/*	public static void main(String[] args) {
		
		char[] testChars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		byte[] testBytes = new byte[128];
		Arrays.fill(testBytes,  (byte) 5);
		int[] testInts = new int[55];
		Arrays.fill(testInts,  55);
		long[] testLongs = new long[33];
		Arrays.fill(testLongs,  33L);
		byte b = (byte) 0xFF;
		int i = 0xFFFFFFFF;
		long l = 0xFFFFFFFFFFFFFFFFL;
		char c = 'X';
		
		zero(testChars);
		zero(testBytes);
		zero(testInts);
		zero(testLongs);
		
		zero(b);
		zero(i);
		zero(l);
		zero(c);
		
		getZeroizationResult();
	} */
}
