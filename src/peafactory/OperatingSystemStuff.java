package cologne.eck.peafactory;

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
 * Operating system dependent stuff
 */

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;



public class OperatingSystemStuff {

	public OperatingSystemStuff() {	}
	
	/**
	 * Open a file manager and display the given file
	 * 
	 * @param fileName	the name of the file to be displayed
	 * 
	 * @return			an error message why the function fails
	 */
	public static String openFileManager(String fileName){
		
		String errorMessage = null;// error message
		
		File file = new File(fileName);
		
		if ( ! file.exists() ){
			return "Specified file doesn't exist: " + fileName;
		}
		
		String os = System.getProperty("os.name");
		
		try {				
			if (os.contains("Windows")){
				if (Desktop.isDesktopSupported() == true 
						&& GraphicsEnvironment.isHeadless() == false){					
					// check if open action is supported:
					if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
						Desktop.getDesktop().open(file);
					}
				}					
			// check several file manager for Linux and BSD
			} else if (os.contains("Linux") || os.contains("BSD")) {
				try {
					Runtime.getRuntime().exec("nautilus " + fileName);// gnome						
				} catch (Exception e) {
					try {
						Runtime.getRuntime().exec("dolphin " + fileName);// kde						
					} catch (Exception e1) {
						try {
							Runtime.getRuntime().exec("pcmanfm " + fileName);// lxde					
						} catch (Exception e2) {
							try {
								Runtime.getRuntime().exec("thunar " + fileName);	//xfce							
							}  catch (Exception e3) {
								try {
									Runtime.getRuntime().exec("nemo " + fileName);//Mint
								}  catch (Exception e4) {
									try {
										Runtime.getRuntime().exec("konqueror " + fileName);
									}  catch (Exception e5) {
										try {
											Runtime.getRuntime().exec("rox " + fileName);//puppy linux
										}  catch (Exception e6) {
											try {
												Runtime.getRuntime().exec("xfe " + fileName);
											}  catch (Exception e7) {
												try {
													Runtime.getRuntime().exec("gentoo " + fileName);
												} catch (Exception e8) {
													try {
														Runtime.getRuntime().exec("caja " + fileName);//Mate
													}  catch (Exception e9) {
														Runtime.getRuntime().exec("xdg-open " + fileName);
													}
												}
											}
										}											
									}
								}									
							}
						}
					}
				}	
			} else if (os.contains("Mac")) {
				Runtime.getRuntime().exec("open " + fileName);
			} else {
				if (Desktop.isDesktopSupported() == true 
						&& GraphicsEnvironment.isHeadless() == false){
					
					// check if open action is supported:
					if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
						// try and hope the best...
						Desktop.getDesktop().open(file);
					}
				}
			}
		} catch (IllegalArgumentException iae) {
			errorMessage = "Specified file doesn't exist: " + fileName;

		} catch (UnsupportedOperationException uoe) {
			errorMessage = "Open peas dirctory failed. \n" +
					"Current platform does not support the Desktop.Action.OPEN action";
		} catch (Exception e) {
			errorMessage = "Unexpected error while opening the file " + fileName;
		} 		
		return errorMessage;
	}
}
