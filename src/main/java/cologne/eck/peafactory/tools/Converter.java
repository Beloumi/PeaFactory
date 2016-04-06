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
 * Convert data types. 
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

public final class Converter {
	
	public final static int[] chars2ints( char[] input) {
		if (input == null) {
			System.err.println("Converter: input null (char -> int)");
			return null;
		}
		if (input.length %2 != 0) {
			System.err.println("Converter (char -> int): wrong size, must be even. Is padded.");
			char[] tmp = new char[input.length + 1];
			System.arraycopy(input,  0 ,tmp, 0, input.length);
			tmp[tmp.length - 1] = '\0';
		}
		int[] result = new int[input.length / 2];
		
		for (int i=0; i<result.length; i++) {
			//System.out.println("  " + ( (int)input[i*2]) + "  " + (((int) (input[i*2+1]) << 16)));
			result[i] = ( (int)input[i*2]) | (((int) (input[i*2+1]) << 16));
		}
		return result;
	}		
	// Converts char[] to byte[]
	public final static byte[] chars2bytes(char[] charArray) {
		if (charArray == null) {
			System.err.println("Converter: input null (char -> byte)");
			return null;
		}
		byte[] result = Charset.forName("UTF-8").encode(CharBuffer.wrap(charArray)).array();
		return result;
	}
	// Converts byte[] to char[]
	public final static char[] bytes2chars(byte[] byteArray) {		
		//ByteBuffer bbuff = ByteBuffer.allocate(byteArray.length);
		if (byteArray == null) {
			System.err.println("Converter: input null (byte -> char)");
			return null;
		}		
		char[] result = Charset.forName("UTF-8").decode(ByteBuffer.wrap(byteArray)).array();
		
		// cut last null-bytes, appeared because of Charset/CharBuffer in bytes2chars for Linux
		int cutMarker;
		for (cutMarker = result.length - 1; cutMarker > 0; cutMarker--) {
			if (result[cutMarker] != 0) break;
		}
		char[] tmp = new char[cutMarker + 1];
		System.arraycopy(result, 0, tmp, 0, cutMarker + 1);
		Zeroizer.zero(result);
		result = tmp;			
		
		return result;
	}

	
	//===============================
	// BIG ENDIAN:
	
	public final static int[] bytes2intsBE( byte[] bytes) {
		if (bytes == null) {
			System.err.println("Converter: input null (byte -> int)");
			return null;
		}
		if (bytes.length % 4 != 0) {
			System.err.println("Converter bytes2int invalid length %4");
			System.exit(1);
		}
		int[] result = new int[bytes.length/4];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((bytes[i*4 ] & 0xFF) << 24) 
					| ((bytes[i*4+1] & 0xFF) << 16) 
					| ((bytes[i*4+2] & 0xFF) << 8) 
					| (bytes[i*4+3] & 0xFF); 
		}
		return result;
	}
	
	/**
	 * Convert an array of 8-bit signed values (Java: bytes) 
	 * into an array of 32-bit signed values (Java: int)
	 * 
	 * @param bytes		input: array of 8-bit signed values to convert
	 * @param inIndex	index at input to start
	 * @param inLen		number of bytes to convert
	 * @param outIndex	index at output, to store the converted values
	 * @return			output: array of 32-bit signed vales, 
	 * 					must be larger than outIndex + inLen/4
	 */
	public final static int[] bytes2intsBE( byte[] bytes, int inIndex, int inLen, int outIndex) {
		if (bytes == null) {
			System.err.println("Converter: input null (byte -> int)");
			return null;
		}
		if (bytes.length % 4 != 0) {
			System.err.println("Converter bytes2int invalid length %4");
			System.exit(1);
		}
		int[] result = new int[bytes.length/4];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((bytes[i*4 ] & 0xFF) << 24) 
					| ((bytes[i*4+1] & 0xFF) << 16) 
					| ((bytes[i*4+2] & 0xFF) << 8) 
					| (bytes[i*4+3] & 0xFF); 
		}
		return result;
	}
	
	public final static byte[] ints2bytesBE(int[] ints) {
		if (ints == null) {
			System.err.println("Converter: input null (int -> byte)");
			return null;
		}
		byte[] result = new byte[ints.length * 4];
		for (int i = 0; i < ints.length; i++ ) {
			result[i*4+3] = (byte) (ints[i]);
			result[i*4+2] = (byte)(ints[i] >>>  8);
			result[i*4+1] = (byte)(ints[i] >>>  16);
			result[i*4] = (byte)(ints[i] >>>  24);
		}		
		return result;
	}
	public final static byte[] int2bytesBE(int input) {

		byte[] result = new byte[4];

			result[3] = (byte) (input);
			result[2] = (byte)(input >>>  8);
			result[1] = (byte)(input >>>  16);
			result[0] = (byte)(input >>>  24);
		
		return result;
	}
	
	
	public final static byte[] long2bytesBE(long longValue) {
	    return new byte[] {
	        (byte) (longValue >> 56),
	        (byte) (longValue >> 48),
	        (byte) (longValue >> 40),
	        (byte) (longValue >> 32),
	        (byte) (longValue >> 24),
	        (byte) (longValue >> 16),
	        (byte) (longValue >> 8),
	        (byte) longValue
	    };
	}
	public final static byte[] longs2bytesBE(long[] longArray) {
		byte[] result = new byte[longArray.length * 8];
		for (int i = 0; i < longArray.length; i++) {
			result[i * 8 + 0] = (byte) (longArray[i] >>> 56);
			result[i * 8 + 1] = (byte) (longArray[i] >>> 48);
			result[i * 8 + 2] = (byte) (longArray[i] >>> 40);
			result[i * 8 + 3] = (byte) (longArray[i] >>> 32);
			result[i * 8 + 4] = (byte) (longArray[i] >>> 24);
			result[i * 8 + 5] = (byte) (longArray[i] >>> 16);
			result[i * 8 + 6] = (byte) (longArray[i] >>> 8);
			result[i * 8 + 7] = (byte) (longArray[i] >>> 0);
		}
	    return result;
	}
	public final static long bytes2longBE(byte[] byteArray) {
		if (byteArray.length != 8) {
			System.err.println("Converter bytes2long: invalid length");
			System.exit(1);
		}
	      return ((long)(byteArray[0]   & 0xff) << 56) |
	      ((long)(byteArray[1] & 0xff) << 48) |
	      ((long)(byteArray[2] & 0xff) << 40) |
	      ((long)(byteArray[3] & 0xff) << 32) |
	      ((long)(byteArray[4] & 0xff) << 24) |
	      ((long)(byteArray[5] & 0xff) << 16) |
	      ((long)(byteArray[6] & 0xff) << 8) |
	      ((long)(byteArray[7] & 0xff));
	}
	public final static long[] bytes2longsBE(byte[] byteArray) {
		if (byteArray.length % 8 != 0) {
			System.err.println("Converter bytes2long: invalid length: " + byteArray.length);
			System.exit(1);
		}
		long[] result = new long[byteArray.length / 8];
		for (int i = 0; i < result.length; i++) {
			result[i] = 
			  ((long)(byteArray[i * 8 + 0] & 0xff) << 56) |
		      ((long)(byteArray[i * 8 + 1] & 0xff) << 48) |
		      ((long)(byteArray[i * 8 + 2] & 0xff) << 40) |
		      ((long)(byteArray[i * 8 + 3] & 0xff) << 32) |
		      ((long)(byteArray[i * 8 + 4] & 0xff) << 24) |
		      ((long)(byteArray[i * 8 + 5] & 0xff) << 16) |
		      ((long)(byteArray[i * 8 + 6] & 0xff) << 8) |
		      ((long)(byteArray[i * 8 + 7] & 0xff));
		}
	    return result;
	}
	public final static long[] ints2longsBE(int[] intArray) {
		if (intArray.length % 2 != 0) {
			System.err.println("Converter ints2long: invalid length: " + intArray.length);
			System.exit(1);
		}
		long[] result = new long[intArray.length / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (long) (intArray[i * 2 + 1] & 0x00000000FFFFFFFFL) 
					| (long)intArray[i * 2 + 0] << 32;
		}
	    return result;
	}
	
	//===============================
	// LITTLE ENDIAN:
	public final static int[] bytes2intsLE( byte[] bytes) {
		if (bytes == null) {
			System.err.println("Converter: input null (byte -> int)");
			return null;
		}
		if (bytes.length % 4 != 0) {
			System.err.println("Converter bytes2int invalid length %4");
			System.exit(1);
		}
		int[] result = new int[bytes.length/4];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((bytes[i*4+3 ] & 0xFF) << 24) 
					| ((bytes[i*4+2] & 0xFF) << 16) 
					| ((bytes[i*4+1] & 0xFF) << 8) 
					| (bytes[i*4+0] & 0xFF); 
		}
		return result;
	}
	public final static byte[] ints2bytesLE(int[] ints) {
		if (ints == null) {
			System.err.println("Converter: input null (int -> byte)");
			return null;
		}
		byte[] result = new byte[ints.length * 4];
		for (int i = 0; i < ints.length; i++ ) {
			result[i*4+0] = (byte) (ints[i]);
			result[i*4+1] = (byte)(ints[i] >>>  8);
			result[i*4+2] = (byte)(ints[i] >>>  16);
			result[i*4+3] = (byte)(ints[i] >>>  24);
		}		
		return result;
	}
	public final static byte[] int2bytesLE(int input) {

		byte[] result = new byte[4];
		result[0] = (byte) (input);
		result[1] = (byte)(input >>>  8);
		result[2] = (byte)(input >>>  16);
		result[3] = (byte)(input >>>  24);	
		return result;
	}

	
	public final static byte[] long2bytesLE(long longValue) {
	    return new byte[] {
	        (byte) (longValue),
	        (byte) (longValue >> 8),
	        (byte) (longValue >> 16),
	        (byte) (longValue >> 24),
	        (byte) (longValue >> 32),
	        (byte) (longValue >> 40),
	        (byte) (longValue >> 48),
	        (byte) (longValue >> 56)
	    };
	}
	public final static byte[] longs2bytesLE(long[] longArray) {
		byte[] result = new byte[longArray.length * 8];
		for (int i = 0; i < longArray.length; i++) {
			result[i * 8 + 7] = (byte) (longArray[i] >>> 56);
			result[i * 8 + 6] = (byte) (longArray[i] >>> 48);
			result[i * 8 + 5] = (byte) (longArray[i] >>> 40);
			result[i * 8 + 4] = (byte) (longArray[i] >>> 32);
			result[i * 8 + 3] = (byte) (longArray[i] >>> 24);
			result[i * 8 + 2] = (byte) (longArray[i] >>> 16);
			result[i * 8 + 1] = (byte) (longArray[i] >>> 8);
			result[i * 8 + 0] = (byte) (longArray[i] >>> 0);
		}
	    return result;
	}
	public final static byte[] longs2intsLE(long[] longArray) {
		byte[] result = new byte[longArray.length * 2];
		for (int i = 0; i < longArray.length; i++) {
			result[i * 2 + 1] = (byte) (longArray[i] >>> 32);
			result[i * 2 + 0] = (byte) (longArray[i] >>> 0);
		}
	    return result;
	}
	public final static long bytes2longLE(byte[] byteArray) {
		if (byteArray.length % 8 != 0) {
			System.err.println("Converter bytes2long: invalid length: " + byteArray.length);
			System.exit(1);
		}
	      return 
	      ((long)(byteArray[7] & 0xff) << 56) |
	      ((long)(byteArray[6] & 0xff) << 48) |
	      ((long)(byteArray[5] & 0xff) << 40) |
	      ((long)(byteArray[4] & 0xff) << 32) |
	      ((long)(byteArray[3] & 0xff) << 24) |
	      ((long)(byteArray[2] & 0xff) << 16) |
	      ((long)(byteArray[1] & 0xff) << 8) |
	      ((long)(byteArray[0] & 0xff));
	}
	public final static long[] bytes2longsLE(byte[] byteArray) {
		if (byteArray.length % 8 != 0) {
			System.err.println("Converter bytes2long: invalid length");
			System.exit(1);
		}
		long[] result = new long[byteArray.length / 8];
		for (int i = 0; i < result.length; i++) {
			result[i] = 
			  ((long)(byteArray[i * 8 + 7]   & 0xff) << 56) |
		      ((long)(byteArray[i * 8 + 6] & 0xff) << 48) |
		      ((long)(byteArray[i * 8 + 5] & 0xff) << 40) |
		      ((long)(byteArray[i * 8 + 4] & 0xff) << 32) |
		      ((long)(byteArray[i * 8 + 3] & 0xff) << 24) |
		      ((long)(byteArray[i * 8 + 2] & 0xff) << 16) |
		      ((long)(byteArray[i * 8 + 1] & 0xff) << 8) |
		      ((long)(byteArray[i * 8 + 0] & 0xff));
		}
	    return result;
	}
	public final static long[] ints2longsLE(int[] intArray) {
		if (intArray.length % 2 != 0) {
			System.err.println("Converter ints2long: invalid length: " + intArray.length);
			System.exit(1);
		}
		long[] result = new long[intArray.length / 2];
		for (int i = 0; i < result.length; i++) {
			result[i] = (long)intArray[i * 2 + 1] << 32 |
					(long) (intArray[i * 2 + 0] & 0x00000000FFFFFFFFL);
		}
	    return result;
	}
	
/*	public final static byte[] swapBytes(byte[] byteArray) {
		byte[] result = new byte[byteArray.length];
		int index = result.length -1;
		for (int i = 0; i < byteArray.length; i++) {
			result[index--] = byteArray[i];
		}
		return result;
	}*/
	
	public final static long swapEndianOrder (long longValue) {
		return
			((((long)longValue) << 56) & 0xff00000000000000L) | 
			((((long)longValue) << 40) & 0x00ff000000000000L) | 
			((((long)longValue) << 24) & 0x0000ff0000000000L) | 
			((((long)longValue) <<  8) & 0x000000ff00000000L) | 
			((((long)longValue) >>  8) & 0x00000000ff000000L) | 
			((((long)longValue) >> 24) & 0x0000000000ff0000L) | 
			((((long)longValue) >> 40) & 0x000000000000ff00L) | 
			((((long)longValue) >> 56) & 0x00000000000000ffL);
	}
	public final static int swapEndianOrder (int intValue) {
		return
			((((int)intValue) <<  24) & 0xff000000) | 
			((((int)intValue) <<   8) & 0x00ff0000) | 
			((((int)intValue) >>>  8) & 0x0000ff00) | 
			((((int)intValue) >>> 24) & 0x000000ff);
	}
	
	//==============================================
	// HEX STRINGS AND BYTE ARRAYS:
	public final static byte[] hex2bytes(String hexString) {

		byte[] byteArray = new byte[hexString.length() / 2];// 2 Character = 1 Byte
			int len = hexString.length();
			if ( (len & 1) == 1){ // ungerade
				System.err.println("Illegal Argument (Function hexStringToBytes): HexString is not even");
				return byteArray; // return null-Array
			}
			final char [] hexCharArray = hexString.toCharArray ();// Umwandeln in char-Array
			for (int i = 0; i < hexString.length(); i+=2) {
				// 1. char in hex <<4, 2. char in hex
				byteArray[i / 2] = (byte) ((Character.digit (hexCharArray[i], 16) << 4) 
								+ Character.digit (hexCharArray[i + 1], 16));
			}		
			return byteArray;
	}
	
	public final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytes2hex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	private static final String HEXES = "0123456789ABCDEF";
	public static String getHex( byte [] raw ) {
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	        hex.append(HEXES.charAt((b & 0xF0) >> 4))
	            .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	    // oder: System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(bytes));
	}
	
	//===========================================
	// DOCUMENTS AND BYTE ARRAYS
	public final static byte[] serialize(DefaultStyledDocument dsd)  {

		RTFEditorKit kit =  new RTFEditorKit();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			kit.write( out,  dsd, 0, dsd.getLength() );
			out.close();
		} catch (BadLocationException e) {
			System.err.println("Converter: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Converter: " + e);
			e.printStackTrace();
		}  
	    return out.toByteArray();
	}
	public final static DefaultStyledDocument deserialize(byte[] data)  {
		RTFEditorKit kit =  new RTFEditorKit();
		DefaultStyledDocument dsd = new DefaultStyledDocument();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		try {
			kit.read(in, dsd, 0);
			in.close();
		} catch (BadLocationException e) {
			// 
			System.err.println("Converter deserialize:  "+ e.toString());
			return null;
			//e.printStackTrace();
			//System.err.println("BadLocationException");
		} catch (IOException e) {

			System.err.println("Converter deserialize:  "+ e.toString());
			return null;
			//e.printStackTrace();
		}		
	    return dsd;
	}
}
