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
 * Calls the Eclipse Jar Compiler to compile the modified files with
 * new settings and new random values. 
 */


/*
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
*/
import java.io.IOException;

import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import cologne.eck.peafactory.PeaFactory;


public class FileCompiler {	
	
    public static class MyDiagnosticListener implements DiagnosticListener<JavaFileObject>
    {


		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			 
            System.out.println("Line Number->" + diagnostic.getLineNumber());
            System.out.println("code->" + diagnostic.getCode());
            System.out.println("Message->"
                               + diagnostic.getMessage(Locale.ENGLISH));
            System.out.println("Source->" + diagnostic.getSource());
            System.out.println(" ");
			
		}
    }

	public void compile(String [] javaFileNames) {

		// get Compiler: package ecj-3.7.jar
		JavaCompiler compiler = new EclipseCompiler();// org.eclipse.jdt.internal.compiler.tool.EclipseCompiler
	
        // for compilation diagnostic message processing on compilation WARNING/ERROR
        MyDiagnosticListener c = new MyDiagnosticListener();
		
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(c, null, PeaFactory.getCharset());
        




        // java files -> file objects
        Iterable<? extends JavaFileObject> fileObjects =  fileManager.getJavaFileObjects(javaFileNames);

        try {
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, c, null, null, fileObjects);
            task.call();	
			fileManager.close();
		} catch (RuntimeException e) {
			System.err.println("FileCompiler: " + e.toString() );
			e.printStackTrace();		
		} catch (IOException e) {
			System.err.println("FileCompiler: Can not close StandardJavaFileManager " + e.toString() );
			e.printStackTrace();
		}
        
	    // JAR FILE SETTINGS.JAR:
		//
		// stream to write jar file:
		//
/*		JarOutputStream jos = null;
		try {
			// create jar file with manifest			
			jos = new JarOutputStream(new FileOutputStream("Setting.jar"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		// Add class files to jar file
		//
	
		File source = new File("settings" + File.separator + "PeaSettings.class");
			BufferedInputStream in = null;
			try  {
				// cut "bin/" and replace File.sep with "/"
				JarEntry entry = null;
				if(source.getPath().startsWith("settings")
						|| source.getPath().startsWith("start")){
					entry = new JarEntry( (source.getPath().replace(File.separator, "/") ));	
				} else {
					entry = new JarEntry( (source.getPath().substring(4, source.getPath().length())).replace(File.separator, "/") );	
				}
				entry.setTime(source.lastModified());
				jos.putNextEntry(entry);
				in = new BufferedInputStream(new FileInputStream(source));
			    byte[] buffer = new byte[1024];
			    while (true)  {
			    	int count = in.read(buffer);
			    	if (count == -1)  break;
			    	jos.write(buffer, 0, count);
			    }
			    jos.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		if (jos != null) {
			try {
				jos.flush();
				jos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} */
	}
}
