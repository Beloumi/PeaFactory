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


import java.awt.EventQueue;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.JOptionPane;

import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.HashStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.RandomStuff;
import cologne.eck.peafactory.crypto.kdf.CatenaKDF;
import cologne.eck.peafactory.gui.MainView;
import cologne.eck.peafactory.gui.ProjectSelection;
import cologne.eck.peafactory.peagen.DataType;
import cologne.eck.peafactory.peagen.JarStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.file_pea.FileType;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SkeinDigest;
import org.bouncycastle.crypto.engines.TwofishEngine;

public final class PeaFactory {

	// i18n: 	
	private static String language; 
	private static Locale currentLocale;
	private static Locale defaultLocale = Locale.getDefault();
	private static ResourceBundle languageBundle;
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	
	private static String os = null;

	private static MainView frame;
	
	private static String version = "Peafactory version 0.0";
	
	private final static String packagePathName = "cologne" + File.separator + "eck" + File.separator + "peafactory";
	
	// default algos:
	private static final KeyDerivation DEFAULT_KDF = new CatenaKDF();
	private static final Digest DEFAULT_HASH = new SkeinDigest(512, 512);
	private static final BlockCipher DEFAULT_CIPHER = new TwofishEngine();
	
	public static void main(String[] args) throws MalformedURLException {		
		
		os = System.getProperty("os.name");
		
		boolean testMode = false;
		if (args.length > 0) {
			if (args[0] != null) {
				if(args[0].equals("-r")){
					System.out.println("Rescue mode only works for PEAs, not for PeaFactory...");
				} else if(args[0].equals("-t")){
					System.out.println("#####---TEST MODE---#####");
					testMode = true;
				} else {
					System.out.println("Unknown argument: " + args[0]);
				}
			}
		}
		if (testMode == true) {
			
			new TestMode().runInTestMode();
		}
		
		// i18n: 
	    language = defaultLocale.getLanguage(); // get default language
	    currentLocale = new Locale(language); // set default language as language

	    //File file = new File("src" + File.separator + "config" + File.separator + "i18n");
	    File file = new File("config" + File.separator + "i18n"); 
	    URL[] urls = {file.toURI().toURL()};  
	    ClassLoader loader = new URLClassLoader(urls);  
	    languageBundle = ResourceBundle.getBundle("LanguagesBundle", currentLocale, loader);  
    
	    // DEFAULT HASH FUNCTION: 
	    if (HashStuff.getHashAlgo() == null) { // needed for RandomPasswordDialog
	    	HashStuff.setHashAlgo( DEFAULT_HASH );
	    }
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {		
					
					PswDialogView.setUI();
					
					// select the kind of pea
					ProjectSelection proj = new ProjectSelection();
					proj.setLocation( 100, 100 );
					proj.setVisible(true);

					frame = MainView.getInstance();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public final static void createFile() {

		//=====================
		// initialize (order!)
		//
	    if (HashStuff.getHashAlgo() == null) {
	    	HashStuff.setHashAlgo( DEFAULT_HASH );// default
	    }
	    if (CipherStuff.isBound() == true){
	    	Attachments.generateAndSetProgramRandomBytes();
	    	Attachments.generateAndSetFileIdentifier();	
			KeyDerivation.setSalt(Attachments.getProgramRandomBytes() );
	    } else {
	    	// use default values for fileIdentifier and programRandomBytes
	    	// calculate default salt and update via setAttachedSalt: 
	    	// xor the salt with attachedSalt (will be attached to the content)
	    	Attachments.setFileIdentifier("PFACTORY".getBytes(UTF_8)); // 8 byte
	    	// 129 byte string to initialize the salt: 
	    	String fixedSaltString = "PEAFACTORY - PRODUCTION OF PASSWORD ENCRYPTED ARCHIVES - FIXED STRING USED TO INITIALIZE THE SALT FOR THE KEY DERIVATION FUNCTION";
	    	String saltString = fixedSaltString.substring(0, KeyDerivation.getSaltSize() );
	    	Attachments.setProgramRandomBytes(saltString.getBytes(UTF_8));
			KeyDerivation.setSalt(saltString.getBytes(UTF_8));

		    byte[] attachedSalt = new RandomStuff().createRandomBytes(Attachments.getProgramRandomBytesSize());
		    KeyDerivation.setAttachedAndUpdateSalt(attachedSalt);
	    }
	    
	    // DEFAULT CIPHER ALGO: 
	    if (CipherStuff.getCipherAlgo() == null) {
	    	CipherStuff.setCipherAlgo( DEFAULT_CIPHER );
	    }
	    // DEFAULT KEY DERIVATION FUNCTION
	    if (KeyDerivation.getKdf() == null) {
	    	KeyDerivation.setKdf( DEFAULT_KDF );
	    }

		//======================================
		// get password from pswField and check:
		//
		char[] pswInputChars = MainView.getPsw();	
		MainView.resetPsw();
		char[] pswInputChars2 = MainView.getPsw2();
		MainView.resetPsw2();
		
		if ( ( pswInputChars2.length == 0)  && ( pswInputChars.length != 0)) {
			JOptionPane.showMessageDialog(null, languageBundle.getString("password2_is_null"), null, JOptionPane.PLAIN_MESSAGE);
			return;
		} else if (pswInputChars.length != pswInputChars2.length){
			JOptionPane.showMessageDialog(null, languageBundle.getString("different_password"), null, JOptionPane.PLAIN_MESSAGE);
			return;
		}
		boolean pswEqual = true;
		// prevent timing attacks: 
		for (int i = 0; i < pswInputChars.length; i++) {
			if (pswInputChars[i] != pswInputChars2[i]) {
				pswEqual = false;
			}
		}		
		if (pswEqual == false) {
			JOptionPane.showMessageDialog(null, languageBundle.getString("different_password"), null, JOptionPane.PLAIN_MESSAGE);
			return;
		}

		
		int inputLength = pswInputChars.length;

		Arrays.fill(pswInputChars2, '\0');		
		// Warning for insecure password
		if ((inputLength == 0) && (MainView.isBlankPea() == false)) {
			// allow null-passwords with warning
			JOptionPane.showMessageDialog(frame,
					languageBundle.getString("no_password_warning"),
				"Warning",
				JOptionPane.WARNING_MESSAGE);
			pswInputChars = "no password".toCharArray();

		} else if (inputLength < 12 && MainView.isBlankPea() == false) {
			JOptionPane.showMessageDialog(null, languageBundle.getString("short_password_warning"), null, JOptionPane.PLAIN_MESSAGE);
		}

		//=============
		//  derive key: 
		//	
		byte[] keyMaterial = null;
		
		if (MainView.isBlankPea() == false) {// if true: no need to derive key
			keyMaterial = PswDialogBase.deriveKeyFromPsw(pswInputChars);
			
			if (keyMaterial == null) {
				JOptionPane.showMessageDialog(frame,
						languageBundle.getString("program_bug")
						+ ": \n  (keyMaterial is null)",
					"Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}		
		}
		MainView.progressBar.setValue(35);
		MainView.progressBar.paint(MainView.progressBar.getGraphics());
			
		//=================
		// generate jar file:
		//
		//long start = System.currentTimeMillis(); // Startpunkt

		new JarStuff().generateJarFile(keyMaterial);
		
		//System.out.println("generateJarFile - Zeit in ms: \n" + (System.currentTimeMillis() - start));//Messpunkt
		
		String fileOrDirectory = (MainView.getOpenedFileName() == null) ? languageBundle.getString("directory") : languageBundle.getString("file");
		if (MainView.isBlankPea() == true) {
			fileOrDirectory = languageBundle.getString("directory"); // for all peas
		}
		if (DataType.getCurrentType() instanceof FileType) {
			fileOrDirectory = languageBundle.getString("file");
		}
		String peaFileName = JarStuff.getJarFileName().substring(0, (JarStuff.getJarFileName().length() - 4)); // extract ".jar"
		String peaPath = System.getProperty("user.dir") + File.separator + "peas";
		
		// show informations about created pea:
		int input = JOptionPane.showConfirmDialog(frame,
				languageBundle.getString("new_pea_info") + "\n"
				+ DataType.getCurrentType().getDescription() + "\n\n"
				+ languageBundle.getString("new_pea_type_info") + " "
				+ fileOrDirectory + " \n "
				+ "    - " + peaFileName + " -\n"
				+ languageBundle.getString("new_pea_location_info") + "\n "
				+ "   " + peaPath + "\n\n"
				+ languageBundle.getString("open_directory") + "\n ",
			"Pea info",
				    JOptionPane.YES_NO_OPTION);
		
		if (input == JOptionPane.YES_OPTION) { // 0 
			
			String error = OperatingSystemStuff.openFileManager(peaPath);
			
			if (error != null) {
				JOptionPane.showMessageDialog(frame,
						languageBundle.getString("desktop_error")
						+ "\n\n  " + error,
						languageBundle.getString("error"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else { // do not open folder peas:
			//
		}
	}
	
	//=========================
	// Helper functions:
	
	// set new language, locale, ResourceBundle
	public final static void setI18n(String newLanguage){
        
        // get ResourceBundle
	    File file = new File("config" + File.separator + "i18n");  
	    URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			System.err.println("PeaFactory: Missing Language files.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    URL[] urls = {url};
	    ClassLoader loader = new URLClassLoader(urls);  
	    ResourceBundle newLanguageBundle = ResourceBundle.getBundle("LanguagesBundle", new Locale(newLanguage), loader);  

		// set variables:
	    language = newLanguage;
	    currentLocale = new Locale(newLanguage);
	    languageBundle = newLanguageBundle;

		MainView.updateFrame();
	}

	// ==============================================================================================
	// Getter & Setter
	//
	public final static String getLanguage() {
		return language;
	}
	public final static ResourceBundle getLanguagesBundle(){
		return languageBundle;
	}
	public final static String getPackagePath() {
		return packagePathName;
	}
	public final static boolean isFrameInstantiated() {
		if (frame == null) {
			return false;
		} else {
			return true;
		}
	}
	public final static Charset getCharset() {
		return UTF_8;
	}
	public final static String getOS() {
		return os;
	}
	public final static MainView getFrame() {
		return frame;
	}
	public final static KeyDerivation getDefaultKDF() {
		return DEFAULT_KDF;
	}
	public static BlockCipher getDefaultCipher() {
		return DEFAULT_CIPHER;
	}
	public static Digest getDefaultHash() {
		return DEFAULT_HASH;
	}

	/**
	 * @return the version
	 */
	public static String getVersion() {
		return version;
	}
}
