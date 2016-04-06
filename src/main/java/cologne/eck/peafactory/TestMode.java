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
 * Runs the PeaFactory in test mode: 
 * Store errors and exceptions in a log file 
 * along with information about the system, JVM... 
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import cologne.eck.peafactory.tools.ReadResources;

public class TestMode {

	public TestMode() {}
	
	protected final void runInTestMode(){
		
		String os = System.getProperty("os.name");
		
		File logFile = new File("log" + File.separator + "error_log.txt");
		
		try {
			if (! logFile.exists() ){
				String logDirString = logFile.getParent();
				File logDir = new File(logDirString);
				if ( ! logDir.exists()){
					logDir.mkdirs();
					logFile.createNewFile();
				} else {
					if ( ! logDir.isDirectory()){
						logDir.mkdirs();
						logFile.createNewFile();
					}
				}
			}
			if (! logFile.canWrite() ){
				System.err.println("Can't create log file: Write access failed");
				return;				
			}
		} catch (IOException e) {
			System.err.println("Can't create log file: " + e.toString());
			return;
		} catch (Exception e){
			System.err.println("Can't create log file: " + e.toString());
			return;
		}
		
		// log file for error:
		PrintStream ps = null;
		try {
			ps = new PrintStream(
			        		new FileOutputStream(
			        new File("log" + File.separator + "error_log.txt"), true));// append 
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}        

		System.setErr(ps);
		ps.println("\n_______________________");
		ps.println( PeaFactory.getVersion() );				
		ps.print( "OS: " + os );
		String distro = "";
		if (os.contains("inux")) {
			try {
			Process p = null;
			p = Runtime.getRuntime().exec("uname -r");
			BufferedReader in = new BufferedReader(  
                    new InputStreamReader(p.getInputStream()));
				distro = in.readLine();  
			} catch (Exception e) {
				distro = "Distribution not available";
			}  
			ps.print(", Distribution: " + distro );
			// Linux: try to find the distro name: 
			// 1. file /etc/issue:
			File issueFile = new File("/etc/issue");
			if (issueFile.exists() && issueFile.canRead() ) {
				String issue = new String (ReadResources.readExternFile("/etc/issue"), PeaFactory.getCharset());
				ps.print("\n " + issue);
			} else { // 2. any file in /etc/ which ends with release or version
				// there are probably some lines...
				 File dir = new File("/etc/");
				 if (dir.exists()) {
					 File[] list = dir.listFiles();
					 for (int i = 0; i < list.length; i++){
						 if (list[i].isDirectory() ) {
							 continue;
						 } else {
							 if (list[i].getName().endsWith("release") 
								 || list[i].getName().endsWith("version") ) {
								 String release =  new String (ReadResources.readExternFile(list[i].getAbsolutePath()), PeaFactory.getCharset());
								 ps.print("\n " + release);
								 break;
							 }
						 }						 
					 }					 
				 }
			}
		}
		try {
			ps.print("Enviroment: " + System.getenv("XDG_CURRENT_DESKTOP") );
		} catch (Exception e) {
			// do nothing
		}
		ps.print(	System.getProperty("os.arch")  + ",  "
			+	System.getProperty("os.version")  + "\n"
		+ "Java: " + System.getProperty("java.version") + ", Runtime: "
		+ System.getProperty("java.runtime.name") + ",  "
		+ System.getProperty("java.runtime.version") + "\n VM: "
		+ System.getProperty("java.vm.name") + "\n"
		);		
	}
}
