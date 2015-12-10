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
 * Modifies the java files with new settings and random values. 
 * These files will be compiled later by FileCompiler. 
 * Creates two files:
 *
 * - start/Start.java: to start the program with enough memory and in specified mode,
 * 		to check the required java version and to check if an instance of this
 *      program is already running (OS dependent). 
 *      Redirects out and err. 
 *      
 * - settings/PeaSettings.java: sets cryptographic primitives, type of pea, parameters
 *      of the key derivation function, random values, optional features like virtual 
 *      keyboard, password generator
 */

import java.io.*;

import javax.swing.JOptionPane;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.Blake2bDigest;
//import org.bouncycastle.crypto.digests.RIPEMD256Digest;
import org.bouncycastle.crypto.digests.RIPEMD320Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.SkeinDigest;
import org.bouncycastle.crypto.digests.WhirlpoolDigest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.engines.Shacal2Engine;
import org.bouncycastle.crypto.engines.ThreefishEngine;
import org.bouncycastle.crypto.engines.TwofishEngine;

import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.HashStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.kdf.*;
import cologne.eck.peafactory.gui.*;
import cologne.eck.peafactory.peas.editor_pea.EditorType;
import cologne.eck.peafactory.peas.file_pea.FileType;
import cologne.eck.peafactory.peas.image_pea.ImageType;
import cologne.eck.peafactory.peas.note_pea.NotesType;
import cologne.eck.peafactory.tools.Attachments;

public class FileModifier {
	
	private static boolean setKeyboard = false;
	
	private static boolean pswGenerator = false;
	

	/**
	 * Generate the file "start/Start.java". 
	 * This file will be compiled and stored in the new PEA
	 */
	public final static void generateStart(){
		
		StringBuilder builder = new StringBuilder();
		
		// Fixed content: 
		builder.append(
				"package start;\n"
				+"\n/* Automatically produced file */\n"
				+ "/* This class is newly created for each PEA */\n\n"
				+"import java.io.*;\n"
				+"import javax.swing.JOptionPane;\n\n"
				+"public class Start {\n\n"
				);

		
		// variable content: 
		if (KeyDerivation.getKdf() instanceof ScryptKDF) {
		// memory factor * 2 is required memory for JVM
			builder.append("private static final int MEMORY = " +  KeyDerivation.getmCost() * 2 + ";\n" );
		} else {
			builder.append("private static final int MEMORY = 16;\n" );
		}
		String base = "private static final String CLASS_NAME = ";
		if (DataType.getCurrentType() instanceof FileType ) {
			builder.append(base + "\"file_pea/PswDialogFile\";\n");	
		} else if (DataType.getCurrentType() instanceof ImageType ) {
			builder.append(base + "\"image_pea/PswDialogImage\";\n");				
		} else if (DataType.getCurrentType() instanceof NotesType ) {
			builder.append(base + "\"note_pea/PswDialogNotes\";\n");				
		} else if (DataType.getCurrentType() instanceof EditorType ) {
			builder.append(base + "\"editor_pea/PswDialogEditor\";\n");								
		} 
		if (JarStuff.getJarFileName() != null) {
		builder.append("private static final String JAR_FILE_NAME = "
				+ "\"" + JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() -4) + "\";\n");
		} else {
			builder.append("private static final String JAR_FILE_NAME = \"default\";\n");			
		}
		
		// Fixed content:
		builder.append(
				"private final static int FIXED_MEMORY = 160;\n" 
			+	"private final static int NEEDED_MEMORY = MEMORY + FIXED_MEMORY;\n"
			+	"private static double requiredJavaVersion = 1.6;\n"
			+	"private static Runtime run = Runtime.getRuntime();\n"
			+	"private static String mode = \"\";\n\n"

+				"public static void main(String[] args) {\n\n"
+				"// check mode:\n"
+				"if (args.length > 0) {\n"
+				"  if (args[0] != null) {\n"
+				"    if(args[0].equals(\"-r\")){// -r is rescue mode\n"
+				"      mode = args[0];// pass parameter to command later\n"
+				"      System.out.println(\"#####---RESCUE MODE---#####\");}}}\n\n"

+				"String command = \"java \";// create the program call \n"
+				"// check memory:\n"
+				"if ( ( NEEDED_MEMORY * 1024 *1024) > run.maxMemory() ) {\n"
+				"  command = command +  \"-Xmx\" + NEEDED_MEMORY + \"m \";}\n"
+				"else { command = command + \"-Xms\" + NEEDED_MEMORY + \"m \"; }\n"

+				"// check java version:\n"
+				"try {\n"
+				"  String javaVersionString = System.getProperty(\"java.version\");\n"
+				"  double javaVersion = Double.parseDouble( javaVersionString.substring(0,3) );\n"
+				"  if (javaVersion < requiredJavaVersion) {\n"
+				"    JOptionPane.showMessageDialog(null,\n"
+				"    \"Wrong java version. Required java version: \" + requiredJavaVersion + \", available: \"+ System.getProperty(\"java.version\"),\n"
+				"    null, JOptionPane.ERROR_MESSAGE);\n"
+				"    System.exit(1); }\n"
+				"} catch (Exception e) {\n"
+				"  System.err.println(\"Check java version failed: \" + System.getProperty(\"java.version\"));\n"
+				"  System.exit(1);}\n\n"

+				"// check if there is an instance of this program already running:\n"
+				"boolean warn = false;\n"

+				"// check for windows and unix:\n"
+				"String OS = System.getProperty(\"os.name\").toLowerCase();\n\n"

+				"if (OS.contains(\"windows\")){\n"
+				"  String line;\n"
+				"  boolean firstOccurence = false;\n"
+				"  try {\n"
+			    "    Process procCheckWin = Runtime.getRuntime().exec(\"wmic PROCESS where \\\"name like \'%java%\'\\\" get Processid,Caption,Commandline\");\n"
+			    "    BufferedReader inputCheckWin = new BufferedReader(new InputStreamReader(procCheckWin.getInputStream()));\n"
+			    "    while ((line = inputCheckWin.readLine()) != null) {\n"
+		        "      if(line.contains(JAR_FILE_NAME + \".jar\")){\n"
+				"        if(firstOccurence == true){// process is running already\n"
+        		"          warn = true;\n"
+				"          System.out.println(\"instance already running\");\n"
+	        	"          break;\n"
+				"        } else {\n"
+				"          firstOccurence = true;// this is only the instance of this start program\n"
+		        "        }\n"
+ 				"      }\n"
+			    "    }\n"
+			    "    inputCheckWin.close();\n"
+				"  } catch (IOException ioe) {\n"
+			    "    ioe.printStackTrace();\n"
+				"}\n\n"

+				"} else { // should work for most Unix: Linux, BSD, Solaris, Mac OS except OS X \n"

+				"  try {// Redirecting out and err\n"
+				"    Process checkPro = run.exec(\"pgrep -f \"+ JAR_FILE_NAME + \".jar\");\n"
// does not work... +				"Process checkPro = run.exec(\"ps -axo pid,command,args | grep \" + JAR_FILE_NAME + \".jar | awk \'{ print $1 }\'\");\n"
+				"    checkPro.waitFor();\n"
+				"    BufferedReader buffCheck = new BufferedReader(new InputStreamReader(checkPro.getInputStream()));\n"
+				"    buffCheck.readLine(); // this is always existent: the ID of this process\n"
+				"    int checkProID = Integer.parseInt(buffCheck.readLine()); // next line: there should be no ID\n"
+				"    //System.out.println(checkProID);\n"
+ 				"    if( checkProID > 100){\n"
+				"      warn = true;\n"
+				"      System.out.println(\"instance already running\");\n"
+				"    }\n"
+				"  } catch (NumberFormatException e) { \n"
+				"    //System.out.println(\"no instance running\");\n"
+				"  } catch (Exception e) { \n"
+				"    //... this will be thrown for rarely used systems with no pgrep\n"
+				"}}\n\n"

+				"if(warn == true){ \n"
+				"  Object[] options = {\"start nevertheless\", \"cancel\"};\n"
+				"  int n = JOptionPane.showOptionDialog(null,\n"
+			   	"  \"It seems that there is already a running instance of this program.\" +\n"
+				"  \"\\nYou should close those instance before you start a new one.\",\n"
+				"  \"Warning\", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, \n"
+			    "  options, options[1]);\n"		
+				"  if(n != 0) { System.exit(0);}}\n\n"

+				"// create the command:\n"
+				"command =  command + \"-cp \" + JAR_FILE_NAME \n"
+				"+ \".jar cologne.eck.peafactory.peas.\" + CLASS_NAME + \" \" + mode;\n\n"

+				"try {\n"
+				"// run the program\n"
+				"  Process p = run.exec(command);\n"

+				"// Redirect out and err:\n"
+				"  String line;\n"
+				"  BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));\n"
+				"  BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));\n"
+				"  while ((line = bri.readLine()) != null) { System.out.println(line); }\n"
+				"  bri.close();\n"
+				"  while ((line = bre.readLine()) != null) { System.out.println(line); }\n"
+				"  bre.close();\n"
+				"  p.waitFor();\n"
+				"} catch (IOException ie) {\n"
+				"  JOptionPane.showMessageDialog(null,\n"
+				"  \"Execution failed.\"\n"
+				"  + ie.toString(),\n"
+				"  null,JOptionPane.ERROR_MESSAGE);\n"
+				"  ie.printStackTrace();\n"
+				"} catch (InterruptedException ire) { // for terminal only\n"
+				"  ire.printStackTrace();}\n"
+				"  System.exit(0);}}\n"
				);
		
		// Write file: 
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter( new FileWriter("start" + File.separator + "Start.java"));
			writer.write( new String(builder) );
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate the file "settings/PeaSettings.java". 
	 * This file will be compiled and stored in the new PEA
	 */
	
	public final static void generatePeaSettings() {
		
		StringBuilder builder = new StringBuilder();
		
		// Fixed content:
		builder.append("package settings;\n"
+"\n/* Automatically produced file */\n"
+ "/* This class is newly created for each PEA */\n\n"
+ "import javax.swing.JDialog;\n"
+ "import org.bouncycastle.crypto.*;\n"
//+ "import org.bouncycastle.crypto.Digest;\n"
+ "import org.bouncycastle.crypto.digests.*;\n"
+ "import org.bouncycastle.crypto.engines.*;\n"
+ "import cologne.eck.peafactory.crypto.KeyDerivation;\n"
+ "import cologne.eck.peafactory.crypto.kdf.*;\n");
//+ "import cologne.eck.peafactory.peas.gui.*;\n");
		

		
		// Variable content:
		String base = "";
		if(pswGenerator == true || ((pswGenerator == true) 
				&& ( (DataType.getCurrentType() instanceof NotesType) 
						|| (DataType.getCurrentType() instanceof EditorType)))) {
			builder.append("import cologne.eck.peafactory.peas.gui.*;\n");
		}

		builder.append("\npublic class PeaSettings {\n\n"); 
		
		if(setKeyboard == true) {
			builder.append("private static JDialog keyboard = new Keyboard(PswDialogView.getView() );\n" );			
		} else {
			builder.append("private static JDialog keyboard = null;\n" );			
		}

		base = "private static final JDialog pswGenerator = ";
		if( (pswGenerator == true) 
				&& ( (DataType.getCurrentType() instanceof NotesType) 
						|| (DataType.getCurrentType() instanceof EditorType))) {
			builder.append("private static final JDialog pswGenerator = new PasswordGeneratorDialog(null);\n" );	
		} else {
			builder.append("private static final JDialog pswGenerator = null;\n" );	
		}
		builder.append("private static final boolean BOUND = " +  CipherStuff.isBound() + ";\n" );
		
		boolean extern = false;
		base = "private static final String EXTERNAL_FILE_PATH = ";
		if (MainView.getOpenedFileName() != null) {
			extern = true;			
			// Windows: handle backslash \
			String fileName = MainView.getOpenedFileName().replace("\\", "\\\\");
			builder.append(base + "\"" +  fileName + "\";\n" );
		} else {
			if (DataType.getCurrentType() instanceof FileType ) {
				extern = true;
			} else {
				extern = false;
			}
			builder.append(base + "null;\n" );
		/*	if (DataType.getCurrentType() instanceof ImageType 
					&& MainView.isBlankPea() == true) {
				extern = true;
			}*/
		}
		builder.append("private static final boolean EXTERN_FILE = " +  extern + ";\n" );
		if (JarStuff.getJarFileName() == null) {
			builder.append("private static final String JAR_FILE_NAME = \"default.jar\";\n");
			//JarStuff.setJarFileName("default.jar");
		} else {
		builder.append("private static final String JAR_FILE_NAME = "
				+ "\"" + JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() -4) + "\";\n");
		}
		if ( (JarStuff.getLabelText() != null) 
				&& (JarStuff.getLabelText().length() > 0) ) { // if set: 
			if (JarStuff.getLabelText().length() > 40) {					
				JOptionPane.showMessageDialog(null,"Warning:\n" 					
						+ " Text above password-field longer than 40 characters will not be displayed. \n"
						+ " Encryption continues ..." );
			}
			builder.append("private static final String LABEL_TEXT = " + "\"" +  JarStuff.getLabelText() + "\";\n");

		} else {
			builder.append("private static final String LABEL_TEXT = null;\n" );
		}

		String programRandomString = "{";
		for (int i=0;i< Attachments.getProgramRandomBytesSize();i++) {
			programRandomString += Attachments.getProgramRandomBytes()[i] + ", ";
		}
		programRandomString = programRandomString.substring(0, programRandomString.length() - 2); // delete last ,
		builder.append("private static final byte[] PROGRAM_RANDOM_BYTES = " +  programRandomString + "};\n");

		String fileIdentifierString = "{";
		for (int i=0;i<Attachments.getFileIdentifier().length;i++) {
			fileIdentifierString += Attachments.getFileIdentifier()[i] + ", ";
		}
		fileIdentifierString = fileIdentifierString.substring(0, fileIdentifierString.length() - 2); // delete last ,
		builder.append("private static final byte[] FILE_IDENTIFIER = " +  fileIdentifierString + "};\n");

		base = "private static final BlockCipher CIPHER_ALGO = ";
		BlockCipher cipherAlgo = CipherStuff.getCipherAlgo();
		if (cipherAlgo instanceof Shacal2Engine) {
			builder.append(base + "new Shacal2Engine();\n");
		} else if (cipherAlgo instanceof TwofishEngine) {
			builder.append(base + "new TwofishEngine();\n");
		} else if (cipherAlgo instanceof AESEngine) {
			builder.append(base + "new AESEngine();\n");
		} else if (cipherAlgo instanceof AESFastEngine) {
			builder.append(base + "new AESFastEngine();\n");
		} else if (cipherAlgo instanceof SerpentEngine) {
			builder.append(base + "new SerpentEngine();\n");
		} else if (cipherAlgo instanceof ThreefishEngine) {
			int blockSize = cipherAlgo.getBlockSize();
			builder.append(base + "new ThreefishEngine(" + blockSize * 8 + ");\n");
		} else {
			System.err.println("FileModifier: CipherAlgorithm invalid");
		}		
		// Hash algorithm
		base = "private static final Digest HASH_ALGO = ";
		Digest hashAlgo = HashStuff.getHashAlgo();
		if (hashAlgo instanceof WhirlpoolDigest) {
			builder.append(base + "new WhirlpoolDigest();\n");			
		} else if (hashAlgo instanceof SHA3Digest) {
			builder.append(base + "new SHA3Digest();\n");
		} else if (hashAlgo instanceof SkeinDigest) {
			builder.append(base + "new SkeinDigest(512, 512);\n");	
		} else if (hashAlgo instanceof SHA512Digest) {
			builder.append(base + "new SHA512Digest();\n");	
		} else if (hashAlgo instanceof SHA384Digest) {
			builder.append(base + "new SHA384Digest();\n");
		} else if (hashAlgo instanceof Blake2bDigest) {
			builder.append(base + "new Blake2bDigest();\n");
//		} else if (hashAlgo instanceof RIPEMD256Digest) {
//			builder.append(base + "new RIPEMD256Digest();\n");	
		} else if (hashAlgo instanceof RIPEMD320Digest) {
			builder.append(base + "new RIPEMD320Digest();\n");
		} else {
			System.err.println("FileModifier: HashAlgorithm failed");
		}		
		// KDF scheme
		KeyDerivation kdfScheme = KeyDerivation.getKdf();
		base = "private static final KeyDerivation KDF_SCHEME = ";
		if (kdfScheme instanceof BcryptKDF) {
			builder.append(base + "new BcryptKDF();\n");
		} else if (kdfScheme instanceof ScryptKDF) {
			builder.append(base + "new ScryptKDF();\n");
		} else if (kdfScheme instanceof PomeloKDF) {
			builder.append(base + "new PomeloKDF();\n");
		} else if (kdfScheme instanceof CatenaKDF) {
			builder.append(base + "new CatenaKDF();\n");
		} else {
			System.err.println("FileModifier: Key derivation scheme invalid");
		}	
//		if (kdfScheme instanceof BcryptKDF) {
			builder.append("private static final int ITERATIONS = " +  KeyDerivation.gettCost() + ";\n" );
			builder.append("private static final int MEMORY = " +  KeyDerivation.getmCost() + ";\n" );
			builder.append("private static final int PARALLELIZATION = " +  KeyDerivation.getArg3() + ";\n" );
			if (KeyDerivation.getVersionString().equals("")){
				builder.append("private static final String VERSION_STRING = \"\";\n" );
			} else {
				builder.append("private static final String VERSION_STRING = " + "\"" + KeyDerivation.getVersionString() + "\";\n" );
			}
//		} else if (kdfScheme instanceof ScryptKDF) {
//			builder.append("private static final int ITERATIONS = " +  KeyDerivation.gettCost() + ";\n" );
//			builder.append("private static final int MEMORY = " +  KeyDerivation.getmCost() + ";\n" );
//			builder.append("private static final int PARALLELIZATION = " +  KeyDerivation.getArg3() + ";\n" );
//		} else {
//			System.err.println("FileModifier: KDFScheme invalid");
//		}	
		
		// Fixed content:
		builder.append(
				"public final static JDialog getKeyboard() { return keyboard; }\n"
				+ "public final static JDialog getPswGenerator() { return pswGenerator; }\n"
				+ "public final static Digest getHashAlgo() { return HASH_ALGO; }\n"
				+ "public final static int getIterations() { return ITERATIONS; }\n"
				+ "public final static int getMemory() { return MEMORY; }\n"
				+ "public final static int getParallelization() { return PARALLELIZATION; }\n"
				+ "public final static String getVersionString() { return VERSION_STRING; }\n"
				+ "public final static byte[] getProgramRandomBytes() { return PROGRAM_RANDOM_BYTES; }\n"
				+ "public final static byte[] getFileIdentifier() { return FILE_IDENTIFIER; }\n"	
				+ "public final static String getJarFileName() { return JAR_FILE_NAME; }\n"
				+ "public final static String getLabelText() { return LABEL_TEXT; }\n"
				+ "public final static boolean getExternFile() { return EXTERN_FILE; }\n"
				+ "public final static boolean getBound() { return BOUND; }\n"
				+ "public final static String getExternalFilePath() { return EXTERNAL_FILE_PATH; }\n"
				+ "public final static BlockCipher getCipherAlgo() { return CIPHER_ALGO; }\n"
				+ "public final static KeyDerivation getKdfScheme() { return KDF_SCHEME; }\n"
				+ "}\n"
				);
	    //System.out.println(new String(builder));
		BufferedWriter writer = null;

	    try {
			writer = new BufferedWriter( new FileWriter("settings" + File.separator + "PeaSettings.java"));
		    writer.write( new String(builder) );
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    


	}


/*// FOR TESTING===========================
	public static void main(String[] args) {		
		//=====================
		// initialize (order!)
		//
	    if (HashStuff.getHashAlgo() == null) {
	    	HashStuff.setHashAlgo( new SkeinDigest(512, 512) );// default
	    }
		Attachments.generateAndSetProgramRandomBytes();
		Attachments.generateAndSetFileIdentifier();	
		KDFScheme.calculateSalt(Attachments.getProgramRandomBytes() );	    

	    if (CipherStuff.getCipherAlgo() == null) {
	    	CipherStuff.setCipherAlgo( new TwofishEngine() );
	    }
	    if (KDFScheme.getKDFScheme() == null) {
	    	KDFScheme.setKDFScheme( new SCryptConnector() );
	    }		
		FileModifier.generatePeaSettings();
		FileModifier.generateStart();		
		//new FileCompiler().compile(null);
	}*/

	/**
	 * @return the setKeyboard
	 */
	public static boolean isSetKeyboard() {
		return setKeyboard;
	}

	/**
	 * @param setKeyboard the setKeyboard to set
	 */
	public static void setSetKeyboard(boolean setKeyboard) {
		FileModifier.setKeyboard = setKeyboard;
	}

	/**
	 * @return the pswGenerator
	 */
	public static boolean isPswGenerator() {
		return pswGenerator;
	}

	/**
	 * @param pswGenerator the pswGenerator to set
	 */
	public static void setPswGenerator(boolean pswGenerator) {
		FileModifier.pswGenerator = pswGenerator;
	}
}
