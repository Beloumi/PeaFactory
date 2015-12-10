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


public class Help {

	

	public final static void printBytes (String comment, byte[] input) {
		System.out.println("\n" + comment + ": ");
		for (int i = 0; i<input.length; i++) {
			if (i > 0 &&i % 32 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	public final static void printBytes (String comment, byte[] input, int begin, int end) {
		System.out.println("\n" + comment + ": "+begin+"-"+end);
		for (int i = begin; i<end; i++) {
			if (i > 0 &&i % 32 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	public final static void printlastBytes (String comment, byte[] input) {
		System.out.println("\n" + comment);
		for (int i = input.length - 32; i<input.length; i++) {
			if (i > 0 &&i % 32 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	public final static void printfirstBytes (String comment, byte[] input) {
		System.out.println("\n" + comment);
		for (int i = 0; i<32; i++) {
			if (i > 0 &&i % 32 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	
	public final static void printInts (String comment, int[] input) {
		System.out.println("\n" + comment + ": ");
		for (int i = 0; i<input.length; i++) {
			if (i > 0 && i % 16 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	
	public final static void printLongs (String comment, long[] input) {
		System.out.println("\n" + comment + ": ");
		for (int i = 0; i<input.length; i++) {
			if (i > 0 && i % 16 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	public final static void printLongs (String comment, long[] input, int begin, int end) {
		System.out.println("\n" + comment + ": ");
		for (int i = begin; i<end; i++) {
			if (i > 0 && i % 16 == 0) System.out.println("");
			System.out.print(" " + input[i]);			
		}
		System.out.println("");
	}
	
	public final static void printStrings(String comment, String[] input) {
		System.out.println("\n" + comment + ": ");
		if (input == null) return;
		for (int i = 0; i<input.length; i++) {
			System.out.println(input[i]);			
		}
		System.out.println("");
	}
}
