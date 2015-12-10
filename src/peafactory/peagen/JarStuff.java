package cologne.eck.peafactory.peagen;

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
 * Program flow of generation the jar file (Password Encrypted Archive). 
 */

import java.io.File;


import cologne.eck.peafactory.gui.*;
import cologne.eck.peafactory.peas.file_pea.FileType;
import cologne.eck.peafactory.peas.image_pea.ImageType;
import cologne.eck.peafactory.tools.Zeroizer;

public class JarStuff {	

	private static String jarFileName;// set in MainView, get in FileModifier
	private static String labelText;// set in MainView, get in FileModifier

	private static final String fileToCompile1 = "settings" + File.separator + "PeaSettings.java";// file to be modified: 
	private static final String fileToCompile2 = "start" + File.separator + "Start.java";// file to be modified: 


	public final void generateJarFile (byte[] keyMaterial) {
		
		DataType dataType = DataType.getCurrentType();		
		
		//=======================
		// generate java files:
		FileModifier.generateStart();
		FileModifier.generatePeaSettings();

		MainView.progressBar.setValue(45);
		MainView.progressBar.paint(MainView.progressBar.getGraphics());
		
		//=====================================
		// compile with new variables
		//			
		// java files to compile
		String javaFileNames[] = {fileToCompile1, fileToCompile2};	
		FileCompiler compiler = new FileCompiler();
		compiler.compile(javaFileNames);
		MainView.progressBar.setValue(60);
		MainView.progressBar.paint(MainView.progressBar.getGraphics());		
		//
		// clear secret values in java file:
		//
		//FileModifier.generatePeaSettings();
		
		//=======================================
        // Create jar archive with compiled files
        //
		// put in directory peas if openedFileName == null
		if ( MainView.isBlankPea() == true ||
				(MainView.getOpenedFileName() == null 
				&& !(dataType instanceof FileType ) ) ) {
			if (dataType instanceof ImageType && dataType.getData() != null){
					//&& MainView.isBlankPea() == false){ // internal image
				//
				System.out.println("internal image");
				dataType.processData(keyMaterial);
				dataType.setData( "     ".getBytes() );// overwrite later
			} else {
				String dirName = JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() - 4); // extract ".jar"
				dirName = "peas" + File.separator + dirName;
				File dir = new File(dirName);// directory containing jar archive and resources
				if (dir.exists()){
					System.out.println("Warning: directory exists");
					// delete all files:
					DataType.clearDirectory(dir);
				} else {
					if ( !dir.mkdir() ) { // try to create
						System.err.println("JarStuff: Can not create directory: " + dirName);
						return;
					}
				}
			}
		}		
		CreateJarFile.createJarFile();// includes text.lock in jar if changeable false
		
		dataType.processData(keyMaterial);
		Zeroizer.zero(keyMaterial);		
		
		// overwrite random values:
		FileModifier.generatePeaSettings();
		compiler.compile(javaFileNames);

		MainView.progressBar.setValue(100);
		MainView.progressBar.paint(MainView.progressBar.getGraphics());
	}
	
	
	public final static String getJarFileName() {
		return jarFileName;
	}
	public final static void setJarFileName(String _jarFileName) {
		jarFileName = _jarFileName;
	}
	public final static String getLabelText() {
		return labelText;
	}
	public final static void setLabelText(String _labelText) {
		labelText = _labelText;
	}
	public final static String getFileToCompile1(){
		return fileToCompile1;
	}
	public final static String getFileToCompile2(){
		return fileToCompile2;
	}
}
