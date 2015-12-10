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
 * Write files. 
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

//import javax.swing.JOptionPane;



public final class WriteResources {
	

	
	public static void write(byte[] textBytes , String fileName, String dir) {
		
		//System.out.println("fileName: " + fileName + "   dir: " + dir);
		
		ByteBuffer bytebuff = ByteBuffer.allocate( textBytes.length );
		bytebuff.clear();
		bytebuff.put( textBytes );
		bytebuff.flip();
		
		FileOutputStream fos = null;
		FileChannel chan = null;

		File file = null;
		if ( dir == null) {
			file = new File(fileName);
		} else {
			fileName = dir + java.io.File.separator+ fileName;
			file = new File(fileName);
		}
		if (dir != null && ! new File(dir).exists()) {
			new File(dir).mkdirs();
		}
		if ( ! file.exists() ) {			
			//System.out.println("WriteResources: fileName: " + fileName + " file getName: " + file.getPath() );
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.err.println("WriteResources: can not create new file: " + file.getPath());
				e.printStackTrace();
			}
		}

		
		try {
			fos = new FileOutputStream(file);
			chan = fos.getChannel();
			while(bytebuff.hasRemaining()) {
			    chan.write(bytebuff);
			}		
		} catch (IOException ioe) {			
			System.err.println("WriteResources: " + ioe.toString() + "   " + fileName);
			ioe.printStackTrace();
		} 
		if ( chan != null){
			try {
				chan.close();
			} catch (IOException e) {
				System.err.println("WriteResources: " + e.toString() + " close()  " + fileName);
				e.printStackTrace();
			}
		}
		if ( fos != null) {
			try {
				fos.close();
			} catch (IOException e) {
				System.err.println("WriteResources: " + e);
				e.printStackTrace();
			}
		}
		// Message ausgeben, dass Datei geschrieben wurde
		//JOptionPane.showMessageDialog(null, "Die Datei " + fileName + " wurde geschrieben.");	
	}	
	
	
	public static void writeText(String text, String fileName) {
		//FileWriter fw = null;
		BufferedWriter buff = null;
		try {
			 // overwrites file if exists
			buff = new BufferedWriter(new FileWriter(fileName));
			buff.write(text);
			
			// Message ausgeben, dass Datei geschrieben wurde
//			JOptionPane.showMessageDialog(null, "New file: " + fileName);
		} catch (IOException ioe) {
			System.out.println("WriteResources: " + ioe.getMessage());
			ioe.printStackTrace();
		} finally {
			if (buff != null) {
				try {
					buff.close();
				} catch (IOException ioe) {
					System.out.println("WriteResources: " + ioe.getMessage());
				}
			}
		}
	}
}
