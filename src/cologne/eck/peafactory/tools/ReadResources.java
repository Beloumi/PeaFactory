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
 * Read files. 
 */


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;



public final class ReadResources {


    public static byte[] readResourceFile( String fileName ) {
    	
    	byte[] byteArray = null; // return value;    	
    	
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    	if (classLoader == null) {
    	    classLoader = Class.class.getClassLoader();
    	}
    	if (classLoader == null) System.err.println("ReadResources: Classloader is null");

    	InputStream is = classLoader.getResourceAsStream("resources/" + fileName);//"resources/fileName");
    	if (is == null) {
    		System.err.println("ReadResources:InputStream is is null");
    		return null;
    	}

    	// Stream to write in buffer 
    	//buffer of baos automatically grows as data is written to it
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	int bytesRead;
    	byte[] ioBuf = new byte[4096];
    	 try {
			while ((bytesRead = is.read(ioBuf)) != -1) baos.write(ioBuf, 0, bytesRead);			
		} catch (IOException e1) {
			System.err.println("ReadResources: " + e1);
			e1.printStackTrace();
		}
    	 
    	if (is != null)
			try {
				is.close();
			} catch (IOException e) {
				System.err.println("ReadResources: " + e);
				e.printStackTrace();
			}
  	
    	//System.out.println("baos vor fill: " + baos.toString() );
    	byteArray = baos.toByteArray();
    	
    	// Fill buffer of baos with Zeros
    	int bufferSize = baos.size();
    	baos.reset(); // count of internal buffer = 0
    	try {
			baos.write(new byte[bufferSize]); // fill with Null-Bytes
		} catch (IOException e) {
			System.err.println("ReadResources: " + e);
			e.printStackTrace();
		}
    	//System.out.println("baos nach fill: " + baos.toString() );
    	return byteArray;
    
    }
    
    public static final byte[] getResourceFromJAR( String fileNameInJar){

    	byte[] result = null;

       	URL url = ReadResources.class.getClassLoader().getResource(fileNameInJar);	
    	
    	ByteArrayOutputStream bais = new ByteArrayOutputStream();
    	InputStream is = null;
    	try {
    		is = url.openStream ();
    		byte[] buffer = new byte[4096]; 
    		int n;

    		while ( (n = is.read(buffer)) > 0 ) {
    			bais.write(buffer, 0, n);
    		}
    	} catch (IOException ioe) {
    		System.err.printf ("ReadResources: getResourceFromJar failes: " + url.toExternalForm());
    		ioe.printStackTrace ();
    	} finally {
    		if (is != null) { 
    			try {
    				is.close();
    			} catch (IOException e) {
    				System.err.println("ReadResources: " + e);
    				e.printStackTrace();
    			} 
    		}
    	}
    	result = bais.toByteArray();// no need to clear: resource is encrypted
    	return result;
    }
    

    
    public static byte[] readExternFile(String fileName) {
    	
    	byte[] byteArray = null;    	
       	
    	File file = new File(fileName);
    	if (checkFile(file) == false) {
    		return null;
    	}
       	
    	int sizeOfFile = (int) file.length();
    	
    	FileInputStream fis = null;
		try {
			fis = new FileInputStream( file );
		} catch (FileNotFoundException e1) {
			System.err.println("ReadResources: " + e1);
			e1.printStackTrace();
		}
    	FileChannel chan = fis.getChannel( );
    	ByteBuffer bytebuff = ByteBuffer.allocateDirect( (int)file.length() );
    	byteArray = new byte[sizeOfFile];
    	//long checkSum = 0L;
    	int nRead, nGet;
    	try {
			while ( (nRead=chan.read( bytebuff )) != -1 )
			{
			    if ( nRead == 0 )
			        continue;
			    bytebuff.position( 0 );
			    bytebuff.limit( nRead );
			    while( bytebuff.hasRemaining( ) )
			    {
			        nGet = Math.min( bytebuff.remaining( ), sizeOfFile );
			        bytebuff.get( byteArray, 0, nGet ); // fills byteArray with bytebuff
			    }
			    bytebuff.clear( );
			}
		} catch (IOException e1) {
			System.err.println("ReadResources: " + e1);
			e1.printStackTrace();
		}
    	
       	if (fis != null){
    			try {
    				fis.close();
    			} catch (IOException e) {
    				System.err.println("ReadResources: FileInputStream close: " + e.toString());
    				e.printStackTrace();
    			}
       	}
       	bytebuff.clear();
    	
    	return byteArray;
    }
    
    public final File[] list(File file) {
    	int number = countDirectoryContent(file);
    	File[] content = new File[number];
    	int i = 0;
        //System.out.println(file.getName());
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
            	content[i++] = child;
                list(child);
            }
        }
        return content;
    }

    
	private int fileNumber = 0;
    // number of containing files and sub-directories
	public final  int countDirectoryContent(File file) {

        File[] children = file.listFiles();
        
        if (children != null) {
        	fileNumber += children.length;
            for (File child : children) {
            	//System.out.println(file.getName());
            	//number++;
            	if (child.isDirectory() ) {
            		countDirectoryContent(child);
            	}
            }
        }
        return fileNumber;
    }
	
	public final static boolean checkFile(File file) {
		
       	if (file.length() > Integer.MAX_VALUE) { // > int
    		System.err.println("ReadResources: File is larger than size of int: " + file.getAbsolutePath() );
    		return false;//new byte[0];
    	}
       	if (! file.exists()) {
       		System.err.println("ReadResources: File does not exist: " + file.getAbsolutePath());
       		return false;
       	}
       	if (! file.canRead() ) {
       		System.err.println("ReadResources: Can not read file: " + file.getAbsolutePath());
       		return false;
       	}
       	if ( file.isDirectory() ) {
       		System.err.println("ReadResources: File is directory: " + file.getAbsolutePath());
       		return false;
       	}
       	if (file.length() == 0) {
       		System.err.println("ReadResources: File is empty: " + file.getAbsolutePath());
       		return false;
       	}
       	return true;		
	}
}
