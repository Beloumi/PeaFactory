package cologne.eck.peafactory.peas.file_pea;

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
 * Composing the selected files for file pea, 
 * used in PswDialogFile, LockFrameFile and MainView  
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import cologne.eck.peafactory.tools.Attachments;

public class FileComposer {
	
	private FileTypePanel fdp;
	
	// initial capacity of ten: 
	private ArrayList <String> annotatedFileNames = new ArrayList <String>(); // if file invalid: (PREFIX_TAB +) INVALID_MARKER + comment + fileName
	private ArrayList <String> originalFileNames = new ArrayList <String>(); // fileNames without Tabs, Marker and comments

	private static final String INVALID_MARKER = FileTypePanel.getInvalidMarker();//"/***/ "; // all files of annotatedFileNames starting with INVALID_MARKER will not be encrypted
	private static final String DIRECTORY_MARKER = FileTypePanel.getDirectoryMarker();//"###";
	
	private RandomAccessFile raf;	// getFileSize
	private boolean breakCheck = false; // for checkNumberAndSize: break recursive function
	private String prefixTab = "   ";// to display file hierarchy

	
	// checks if file is already displayed 
	protected final boolean checkDoubles(String fileName) {
		if (originalFileNames.contains(fileName) ) {
			//System.out.println("already exists: " + fileName);
			return true;
		}
		return false;
	}
	
    // result[0]: number of containing files and sub-directories
	// result[1]: size of all included files in byte
	// function breaks if one of the limits is reached
	// -> Warning message
	protected  final long[] checkNumberAndSize(String directoryName) {
		
		File directory = new File(directoryName);
		
		long[] result = new long[2];

		if (breakCheck == true) {
			return result;
		}

        File[] children = directory.listFiles();
        
        if (children != null) {
        	
        	result[0] += children.length;
        	if (result[0] + fdp.getFileNumber() > fdp.getFileNumberLimit()) {
        		breakCheck = true;
        		return result;
        	}

            for (int i = 0; i < children.length; i++) {

            	if (children[i].isDirectory() ) {
            		long[] dirResult = checkNumberAndSize(children[i].getAbsolutePath() );
            		result[0] += dirResult[0];
            		result[1] += dirResult[1];
                	if (result[0] + fdp.getFileNumber() > fdp.getFileNumberLimit()) {
                		breakCheck = true;
                		return result;
                	}
                	if (result[1] + fdp.getAllSize() > fdp.getSizeLimit()) {
                		breakCheck = true;
                		return result;
                	}
            	} else if (children[i].isFile() ) {
            		result[1] += getFileSize(children[i].getAbsolutePath());

            		if (result[1] + fdp.getAllSize() > fdp.getSizeLimit()) {
            			breakCheck = true;
            			return result;
            		}
            	}
            }
        }       
        return result;
    }	
	
	// checks access and sets annotatedFileNames
	private final void checkAccess(File file, int index) {
		
		//File file = new File(fileName);

  		// check if exists:
		if ( ! file.exists() ) {	
			annotatedFileNames.set(index, INVALID_MARKER + " - can not find: " + originalFileNames.get(index) );
			return;
		}	
		// check access:
		if ( ! file.canRead() ) {
			annotatedFileNames.set(index, INVALID_MARKER + " - can not read: " + originalFileNames.get(index) );
			return;
		}
		if ( ! file.canWrite() ) {
			annotatedFileNames.set(index, INVALID_MARKER + " - can not write: " + originalFileNames.get(index) );
			return;
		}				
		if ( file.length() == 0 && ! file.isDirectory() ) {
			annotatedFileNames.set(index, INVALID_MARKER + " - empty file: " + originalFileNames.get(index) );
			return;
		}			
//System.out.println("memory: " + Runtime.getRuntime().freeMemory()); // ca. 35MiB
	/*	if (file.length() > (Runtime.getRuntime().freeMemory() / 2) ){
			annotatedFileNames.set(index, INVALID_MARKER + " - file is too large: " + originalFileNames.get(index) );
			return;
		}*/


		annotatedFileNames.set(index, originalFileNames.get(index) );
	}
	
	// checks fileIdentifier and sets annotatedFileNames
	private final void checkFileIdentifier(String fileName, int index) {
		//byte[] fileContent = ReadResources.readExternFile(fileName);
		RandomAccessFile f;
		try {
			f = new RandomAccessFile(fileName, "r" );
			if(Attachments.checkFileIdentifier(f, false) == true) {
				annotatedFileNames.set(index, originalFileNames.get(index) );
			} else {
				annotatedFileNames.set(index, INVALID_MARKER + " - not encrypted with this archive: " + originalFileNames.get(index) );
			}			
			f.close();
		} catch (FileNotFoundException e) {
			System.err.println("FileComposer: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("FileComposer: " + e);
			e.printStackTrace();
		}
	

	}
	
	protected final boolean checkIncludedChildren(int dirIndex) {
		String dirName = originalFileNames.get(dirIndex);
		//System.out.println("dir: "+ dirName);
   		int nextIndex = dirIndex + 1;
   		// check if at least one child file is valid 
   		while (nextIndex < originalFileNames.size() 
				&& originalFileNames.get(nextIndex).startsWith(dirName)) { // child files
			if (annotatedFileNames.get(nextIndex).contains(INVALID_MARKER) 
					| annotatedFileNames.get(nextIndex).endsWith(DIRECTORY_MARKER) ) {
				//System.out.println(" false: " + annotatedFileNames.get(nextIndex));
			} else {
				//System.out.println(" true: " + annotatedFileNames.get(nextIndex));
				return true;
			}
			nextIndex++;
		}
		return false;
	}
	
	//------------------------------------------------------------------------------------------

    // result[0]: number of containing files and sub-directories
	// result[1]: size of all included files in byte
	protected  final long[] getNumberAndSize(String directoryName) {
		
		File directory = new File(directoryName);
		
		long[] result = new long[2];

        File[] children = directory.listFiles();        
        if (children != null) {        	
        	result[0] += children.length;
        	//System.out.println(result[0]);
            for (int i = 0; i < children.length; i++) {
            	if (children[i].isDirectory() ) {
            		long[] dirResult = getNumberAndSize(children[i].getAbsolutePath());
            		result[0] += dirResult[0];
            		//System.out.println(result[0]);
            		result[1] += dirResult[1];
            	} else if (children[i].isFile() ) {
            		try {            			
						raf = new RandomAccessFile(children[i], "r");	            		
	            		result[1] += raf.length();
	            		raf.close(); 
					} catch (FileNotFoundException e) {
						System.err.println("FileComposer: " + e);
						e.printStackTrace();
					} catch (IOException e) {
						System.err.println("FileComposer: " + e);
						e.printStackTrace();
					}
            	}
            }
        }       
        return result;
    }
	
	// return size of file in bytes
	protected final long getFileSize(String fileName) {
		File file = new File(fileName);
		if (file.isDirectory() ) {
			return 0;
		}
		long result = 0;
   		try {            			
				raf = new RandomAccessFile(file, "r");	            		
        		result = raf.length();
        		raf.close(); 
			} catch (FileNotFoundException e) { 
				// this will be thrown if file is not file and not directory
				System.out.println(e.getMessage() );
			} catch (IOException e) {
				System.err.println("FileComposer: " + e);
				e.printStackTrace();
			}
   		return result;		
	}
	

    // lists all included files and directories
	// check all files
    // file hierarchy is shown by prefix
    protected final void listFilesHierarchic(String directoryName) {         
        
        File directory = new File(directoryName);
        File[] fileList = directory.listFiles();
        	
        if (fileList != null) {

            for (int i = 0; i < fileList.length; i++) {
            	String fileName = fileList[i].getAbsolutePath();    	
            	
	    		if (checkDoubles(fileName) == true)  {
	    			continue;
	    		}
            	
            	checkAndAddFile(fileName);// modifies annotatedFileNames
            	
            	int index = originalFileNames.indexOf(fileName);
            	
                if (fileList[i].isFile()) {
                	annotatedFileNames.set(index, prefixTab + annotatedFileNames.get(index));

                } else if (fileList[i].isDirectory()) {                   	
                	annotatedFileNames.set(index, prefixTab + annotatedFileNames.get(index));
                	prefixTab = prefixTab + "   ";
                	// expand prefix:
             	   	listFilesHierarchic(fileList[i].getAbsolutePath() );
             	   	// restore prefixTab:
             	   	prefixTab = prefixTab.substring(0, prefixTab.length() - 3);
                } else { // neither file nor directory
                	annotatedFileNames.set(index, prefixTab + annotatedFileNames.get(index));
                }
            }
        }
    }
    protected final void checkAndAddFile(String fileName) {
    	/*if (checkDoubles(fileName) == true ){
    		return;
    	}*/
    	originalFileNames.add(fileName);
    	annotatedFileNames.add(fileName);// might be modified later in checkAccess and checkFileIdentifier
    	
    	// get index in origiginalFileNames, annotatedFileNames, checkPanel:
    	int index = originalFileNames.indexOf(fileName);
    	
    	File file = new File(fileName);
    	checkAccess(file, index);    	

    	// check fileIdentifier, set DIRECTORY_MARKER
    	if (file.isFile() ) {
    		
    		if( fdp.getPlainModus() == false && ! annotatedFileNames.get(index).contains(INVALID_MARKER) ){    	
    			checkFileIdentifier(fileName, index);
    		}
    		
    	} else if (file.isDirectory()) {                
    		
        	annotatedFileNames.set(index, annotatedFileNames.get(index) + DIRECTORY_MARKER );
        	
    	} else { //if ( ! file.isDirectory() && ! file.isFile()) {
    		
			annotatedFileNames.set(index, INVALID_MARKER + " - unknown file type: " + originalFileNames.get(index) );
			
		}   	
    }
    //=======================================
    // Getter & Setter
    protected final void setFileDisplayPanel(FileTypePanel _fdp) {
    	fdp = _fdp;
    }
    protected final ArrayList<String> getAnnotatedFileNames() {
    	return annotatedFileNames;
    }
    protected final void setAnnotatedFileName(String newName, int index) {
    	annotatedFileNames.set(index, newName);
    }
    protected final void addAnnotatedFileName(String newName) {
    	annotatedFileNames.add(newName);
    }
    protected final ArrayList<String> getOriginalFileNames() {
    	return originalFileNames;
    }
    protected final void setOriginalFileName(String newName, int index) {
    	originalFileNames.set(index, newName);
    }
    protected final void setOriginalFileNames(String[] newNames) {
    	originalFileNames = new ArrayList<String>();
    	//annotatedFileNames = new ArrayList<String>();
    	if (newNames != null){
    	for (int i = 0; i < newNames.length; i++) {
    		originalFileNames.add(newNames[i]);
    		//annotatedFileNames.add(newNames[i]);
    	}
    	}
    }
    protected final void setAnnotatedFileNames(String[] newNames) {
    	annotatedFileNames = new ArrayList<String>();
    	//annotatedFileNames = new ArrayList<String>();
    	if (newNames != null) {
    	for (int i = 0; i < newNames.length; i++) {
    		annotatedFileNames.add(newNames[i]);
    		//annotatedFileNames.add(newNames[i]);
    	}
    	}
    }

    protected final void addOriginalFileName(String newName) {
    	originalFileNames.add(newName);
    }
    protected final int getListIndex(String originalFileName) {
    	return originalFileNames.indexOf(originalFileName);
    }
    public final String getInvalidMarker() {
    	return INVALID_MARKER;
    }
    public final String getDirectoryMarker() {
    	return DIRECTORY_MARKER;
    }
}
