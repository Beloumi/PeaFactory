package cologne.eck.peafactory.peas;

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
 * This is the base class for all Password Encrypted Archives (peas).
 * This is the parent class of all PswDialog* classes. 
 */

import java.awt.HeadlessException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.HashStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.RandomStuff;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.WriteResources;
import cologne.eck.peafactory.tools.Zeroizer;


public abstract class PswDialogBase { 

	// special panel for PswDialogView, currently only used in fileType:
	private static JPanel typePanel;
	
	private static String encryptedFileName;	
	
	private static String fileType; // set in daughter class
	
	private final static String PATH_FILE_NAME = PeaSettings.getJarFileName() + ".path";
	
	private static PswDialogBase dialog;
	
	private char[] initializedPassword = null;
	
	private static String errorMessage = null;
	
	// i18n: 	
	private static String language; 
	private static Locale currentLocale;
	private static Locale defaultLocale = Locale.getDefault();
	private static ResourceBundle languagesBundle;
	
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	
	
	private static String workingMode = ""; // rescue mode or test mode
	
	//============================================================
	// --- abstract Methods:
	//

	// pre computation with windowOpening
	// this is nowhere used yet, but maybe used later 
	// for example ROM-hard KDF schemes  
	public abstract void preComputeInThread();
	//
	// before exit: clear secret values
	public abstract void clearSecretValues();
	//
	public abstract void startDecryption();

	
	// currently only used in fileType:
	public abstract String[] getSelectedFileNames();

	//============================================================
	
	public final static void setLanguagesBundle(String _language) throws MalformedURLException {
		
		
/*		if(languagesBundle == null){
			try {
				//System.out.println("language: " + Locale.getDefault().getLanguage());
				if (Locale.getDefault().getLanguage().contains("de")) {
					setLanguagesBundle(Locale.getDefault().getLanguage());
				} else {
					setLanguagesBundle(null);
				}
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}*/
		
		// i18n: 
		if (_language == null) {
			if (defaultLocale == null) { // openSuse
				defaultLocale = new Locale( System.getProperty("user.language") );
			}		
			language = defaultLocale.getLanguage(); // get default language
		} else {
			language = _language;
		}
	    currentLocale = new Locale(language); // set default language as language
	    
	    // First try to open Peafactory i18n, if PswDialogBase is not used inside of a pea 
	    // FilePanel uses this bundle
	    try {
	    	languagesBundle = ResourceBundle.getBundle("config.i18n.LanguagesBundle", currentLocale);
	    } catch (MissingResourceException mre) {// used inside of pea

	    	try {	    
	    	 /*   File file = new File("resources");  
	    	    URL[] urls = {file.toURI().toURL()};  
	    	    ClassLoader loader = new URLClassLoader(urls);  
	    	    languagesBundle = ResourceBundle.getBundle("LanguagesBundle", currentLocale, loader); */ 
	    		languagesBundle = ResourceBundle.getBundle("resources.PeaLanguagesBundle", currentLocale);
	    	} catch  (MissingResourceException mre1) {
	    		
	    		try{
	    			File file = new File("resources");  
		    	    URL[] urls = {file.toURI().toURL()};  
		    	    ClassLoader loader = new URLClassLoader(urls);  
		    	    languagesBundle = ResourceBundle.getBundle("LanguagesBundle", currentLocale, loader);  

	    		} catch (MissingResourceException mre2) {
	    		
		    		currentLocale = new Locale("en"); // set default language as language
		    		language = "en";

		    	    //File file = new File("src" + File.separator + "config" + File.separator + "i18n");
		    	    File file = new File("resources");  

		    	    URL[] urls = {file.toURI().toURL()};  
		    	    ClassLoader loader = new URLClassLoader(urls);  
		    	    languagesBundle = ResourceBundle.getBundle("LanguagesBundle", currentLocale, loader);
	    		}
	    	}
	    }
	}
	

	@SuppressWarnings("all")
	public final static void printInformations() {
		System.out.println("Cipher: " + PeaSettings.getCipherAlgo().getAlgorithmName() );
		System.out.println("Hash: " + PeaSettings.getHashAlgo().getAlgorithmName() );
	}
	
	public final static byte[] deriveKeyFromPsw(char[] pswInputChars) { 

		if (pswInputChars == null) {
			pswInputChars = "no password".toCharArray();
		} else if (pswInputChars.length == 0) {
			pswInputChars = "no password".toCharArray();
		}
		byte[] pswInputBytes = Converter.chars2bytes(pswInputChars); 
		if (pswInputBytes.length > 0)  {
			Zeroizer.zero(pswInputChars);
		}
		// prevent using a zero password:
		pswInputChars = null;

		//=============
		//  derive key: 
		//	
		// 1. initial hash: Bcrypt limited password length, password remains n RAM in Bcrypt and Scrypt
		byte[] pswHash = HashStuff.hashAndOverwrite(pswInputBytes);
		Zeroizer.zero(pswInputBytes);

		// 2. derive key from selected KDF: 
		byte[] keyMaterial = KeyDerivation.getKdf().deriveKey(pswHash);

		Zeroizer.zero(pswHash);		
	
		return keyMaterial;
	}


	protected final byte[] getKeyMaterial() {

		//-------------------------------------------------------------------------------
		// Check if t is alive, if so: wait...
		if (PswDialogView.getThread().isAlive()) { // this should never happen... 
			System.err.println("PswDialogBase: WhileTypingThread still alive!");
			try {
				PswDialogView.getThread().join();
			} catch (InterruptedException e) {
				System.err.println("PswDialogBase: Joining thread interrupted");
				e.printStackTrace();
			}
		}								
		
		//---------------------------------------------------------------------
		// get password 
		char[] pswInputChars = null;
		if (PswDialogView.isInitializing() == false){
			pswInputChars = PswDialogView.getPassword();	
		} else {
			pswInputChars = initializedPassword;
		}

		byte[] keyMaterial = deriveKeyFromPsw(pswInputChars);	
		
		Zeroizer.zero(initializedPassword);
		initializedPassword = null;

		printInformations();
	
		if (keyMaterial == null) {
			PswDialogView.getView().displayErrorMessages("Key derivation failed.\n"
					+ "Program bug.");
			PswDialogView.clearPassword();
			if (CipherStuff.isBound() == false) {
				// reset the salt:
				KeyDerivation.setSalt(Attachments.getProgramRandomBytes());
			}
		}		
		return keyMaterial;
	}

	public final static void initializeVariables() {	
		
		HashStuff.setHashAlgo(PeaSettings.getHashAlgo() );
		CipherStuff.setCipherAlgo(PeaSettings.getCipherAlgo() );
		CipherStuff.setBound(PeaSettings.getBound());
		KeyDerivation.setKdf( PeaSettings.getKdfScheme() );
		KeyDerivation.settCost(PeaSettings.getIterations() );
		//KDFScheme.setScryptCPUFactor(PeaSettings.getIterations() );
		KeyDerivation.setmCost(PeaSettings.getMemory() );
		KeyDerivation.setArg3(PeaSettings.getParallelization());
		KeyDerivation.setVersionString(PeaSettings.getVersionString() );
		Attachments.setProgramRandomBytes(PeaSettings.getProgramRandomBytes() );
		KeyDerivation.setSalt(Attachments.getProgramRandomBytes() );
		
		Attachments.setFileIdentifier(PeaSettings.getFileIdentifier() );
	}
	
	// If a blank PEA is started first: 
	public final byte[] initializeCiphertext(byte[] ciphertext){
		// salt must be set manually and will be attached to the file later
		// in encryption step
		KeyDerivation.setSalt(Attachments.getProgramRandomBytes());
		byte[] attachedSalt = new RandomStuff().createRandomBytes(KeyDerivation.getSaltSize());
		// this will set attachedSalt and update the salt:
		KeyDerivation.setAttachedAndUpdateSalt(attachedSalt);
		
		// attach salt:
		ciphertext = Attachments.attachBytes(ciphertext, attachedSalt);
		// attach fileIdentifier
		ciphertext = Attachments.attachBytes(ciphertext, Attachments.getFileIdentifier());
		
		return ciphertext;
	}
	
	// if the PAE is not bonded to a content: the salt is attached
	public final void handleAttachedSalt(byte[] ciphertext) {
		// reset salt if password failed before:
		KeyDerivation.setSalt(Attachments.getProgramRandomBytes());

		byte[] tmp = new byte[KeyDerivation.getSaltSize()];
		System.arraycopy(ciphertext, 
				ciphertext.length 
				- Attachments.getFileIdentifierSize()
				- KeyDerivation.getSaltSize(), 
				tmp, 0, tmp.length);
		KeyDerivation.setAttachedAndUpdateSalt(tmp);
	}

	//== file functions: ===============================================================	
	
	// checks access and file identifier
	// sets errorMessages
	public final static boolean checkFile(String fileName) {
		File file = new File( fileName );		
		

		if (file.exists() && file.isFile() && file.canRead() && file.canWrite() ) {
			//byte[] content = ReadResources.readExternFile(PswDialogBase.getExternalFilePath());
			RandomAccessFile f;
			try {
				f = new RandomAccessFile(file, "rwd");
		    	if ( Attachments.checkFileIdentifier(f, false) == true) {
			    	f.close();
			    	//============================
			    	// external file name success:
			    	//============================
			    	return true;
				} else {					
					/*if (fileType.equals("passwordSafe") && f.length() == 0) { // just not initialized
						System.out.println("Program not initialized.");
						f.close();
						return true;
					}*/
					f.close();
					System.out.println("PswDialogView: External file path - file identifier failed : " + fileName );
					PswDialogBase.setErrorMessage(
							PswDialogBase.getErrorMessage()
							+ fileName + languagesBundle.getString("file_identifier_failed") + "\n");//": \n file identifier failed.\n");
				}
			} catch (FileNotFoundException e) {
				PswDialogBase.setErrorMessage(
						PswDialogBase.getErrorMessage()
						+ fileName + ": " + e +".\n");
				//e.printStackTrace();
			} catch (HeadlessException e) {
				PswDialogBase.setErrorMessage(
						PswDialogBase.getErrorMessage()
						+ fileName + ": " + e +".\n");
				//e.printStackTrace();
			} catch (IOException e) {
				PswDialogBase.setErrorMessage(
						PswDialogBase.getErrorMessage()
						+ fileName + ": " + e +".\n");
				//e.printStackTrace();
			} catch (Exception e) {
				PswDialogBase.setErrorMessage(
						PswDialogBase.getErrorMessage()
						+ fileName + ": " + e +".\n");
				//e.printStackTrace();
			}
		}
		return false;
	}
	

	
	//
	// 1. checks external file name
	// 2. checks path file
	// success-> sets fileLabel, PswDialogBase.setEncryptedFileName(...)
	// no success -> PswDialogBase.setErrorMessage(...)
	public final static String searchSingleFile() {
		
		PswDialogBase.setErrorMessage("");

		// 1. externalFilePath:
		if ( PeaSettings.getExternalFilePath() != null) {
			
			String externalFileName = PeaSettings.getExternalFilePath();
		/*	if (PswDialogBase.getFileType().equals("passwordSafe")) { 
				externalFileName = externalFileName 
						+ File.separator 
						+ PswDialogBase.getJarFileName() + File.separator
						+ "resources" + File.separator + "password_safe_1.lock";	
			}*/
			if ( checkFile(externalFileName) == true ) { // access and fileIdentifier
				PswDialogView.setFileName( PeaSettings.getExternalFilePath() );
				PswDialogBase.setEncryptedFileName( PeaSettings.getExternalFilePath() ); 
		    	return externalFileName;
			} else {
				System.out.println("PswDialogView: External file path invalid : " + externalFileName );
				PswDialogBase.setErrorMessage(
						PswDialogBase.getErrorMessage()
						//+ "Default file: " + EXTERNAL_FILE_PATH + "--- No access.\n");
						+ languagesBundle.getString("default_file") + " "
						+ PeaSettings.getExternalFilePath()
						+ "--- " +  languagesBundle.getString("no_access") + "\n");
			}

		} else { //if (PswDialogBase.getEncryptedFileName() == null) {
			if (fileType.equals("image") ) {
				//if ( PeaSettings.getBound() == false) {
				//	System.out.println("Base");
				//} else {
					// image was stored as text.lock inside jar file in directory resource
				return "insideJar";
				//}
			}
		}
			
		// check path file
		String[] pathNames = accessPathFile();
		
		if (pathNames == null) {
			PswDialogBase.setErrorMessage(
					PswDialogBase.getErrorMessage()
					+ languagesBundle.getString("no_path_file_result") + "\n");
					//+ "No result from previously saved file names in path file.\n");
		} else {
			
			for (int i = 0; i < pathNames.length; i++) {
				
				if (checkFile(pathNames[i]) == true ) {
					PswDialogView.setFileName(pathNames[i]);
					PswDialogBase.setEncryptedFileName(pathNames[i]);
					return pathNames[i];
					
				} else {
					PswDialogBase.setErrorMessage(
							PswDialogBase.getErrorMessage() +
							//"File from path file: " + pathNames[i] + "--- No access.\n");
					 languagesBundle.getString("file_from_path_file") + " "
					+ pathNames[i] 
					+ "--- " +  languagesBundle.getString("no_access") + "\n");
				}
			}						
		}

		// no file found from external file name and path file:
		if (PswDialogView.getFileName() == null) {
			PswDialogView.setFileName(languagesBundle.getString("no_valid_file_found"));//"no valid file found");
		}				
		return null;
	}
	
	// detects file names from path file
	protected final static String[] accessPathFile() {
		File file = new File(PATH_FILE_NAME);
		if (! file.exists() ) {
			System.err.println("PswDialogBase: no path file specified");
			return null;
		}
		if (! file.canRead() ) {
			System.err.println("PswDialogBase: can not read path file " + file.getName() );
			PswDialogView.setMessage(languagesBundle.getString("path_file_access_error")
				+ "\n" +  PATH_FILE_NAME);
/*			JOptionPane.showInternalMessageDialog(PswDialogView.getView(),
					languagesBundle.getString("path_file_access_error")
				//"There is are file containing names of potentially encrypted files,\n" +
				//" but no access to it: \n"
				+ PATH_FILE_NAME, 
				"Info",//title
				JOptionPane.INFORMATION_MESSAGE);*/
			return null;
		}
		
		byte[] pathBytes = ReadResources.readExternFile( file.getName() );
		if (pathBytes == null) {
			System.err.println("PswDialogBase: path file does not contain any file name: " + file.getPath() );
			return null;
		}
		String pathString = new String(pathBytes, UTF_8);

		String[] pathNames = pathString.split("\n");
	
		return pathNames;			
	}
	
	// checks if file names already exist in path file
	// if not: adds them	
	public final static void addFilesToPathFile(String[] selectedFileNames) {

		byte[] newPathBytes = null;
		StringBuilder newPathNames = null;
				
		File file = new File(PATH_FILE_NAME);
		
		if (! file.exists() ) { // WriteResources creates new file
			
			newPathNames = new StringBuilder();
			
			for (int i = 0; i < selectedFileNames.length; i++) {
				newPathNames.append(selectedFileNames[i]);
				newPathNames.append("\n");
			}
			
		} else {	// path file exists
		
			if (! file.canRead() || ! file.canWrite() ) {
				System.err.println("PswDialogBase: can not read and write path file " + file.getName() );
				
				PswDialogView.setMessage(languagesBundle.getString("path_file_access_error")
						+ "\n" 
						+ PATH_FILE_NAME);
			/*	JOptionPane.showInternalMessageDialog(PswDialogView.getView(),
						languagesBundle.getString("path_file_access_error")
					//"There is are file containing names of potentially encrypted files,\n" +
					//" but no access to it: \n" 
					+ PATH_FILE_NAME, 
					"Info",//title
					JOptionPane.INFORMATION_MESSAGE); */
				return;
			}
			
			// get file names from path file:
			String[] oldPathNames = accessPathFile();
			
			newPathNames = new StringBuilder();
			
			// append old file names:
			for (int i = 0; i < oldPathNames.length; i++) {
				newPathNames.append(oldPathNames[i]);
				newPathNames.append("\n");
			}
			// check if file name already exists, if not append
			for (int i = 0; i < selectedFileNames.length; i++) {
				boolean append = true;
				for (int j = 0; j < oldPathNames.length; j++) {
					if (selectedFileNames[i].equals(oldPathNames[j] )){
						append = false;
					}
				}
				if (append == true) {
					newPathNames.append(selectedFileNames[i]);
					newPathNames.append("\n");
				}
			}
		}

		newPathBytes = new String(newPathNames).getBytes(UTF_8);
			
		WriteResources.write(newPathBytes, PATH_FILE_NAME, null);		
	}
	
	// checks if current encryptedFileName is in path file
	// asks to remember if not
	protected final static void pathFileCheck() {
		//String encryptedFileName = getEncryptedFileName();
		if (encryptedFileName != null) {
			if (encryptedFileName.equals(PeaSettings.getExternalFilePath() )) {
				return; // no need to remember
			}			
		}
		String pathFileName = PeaSettings.getJarFileName() + ".path";
		byte[] pathFileContent = ReadResources.readExternFile(pathFileName);
		if (pathFileContent == null) {
			int n = JOptionPane.showConfirmDialog(null,
					languagesBundle.getString("remember_files")
			//		"Should the name of the file be saved to remember for the next program start? \n" 
					+ languagesBundle.getString("path_file_storage_info")
				//+ "The name is then saved in the same folder as this program in:"
			+ pathFileName , 
				"?", 
				JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.PLAIN_MESSAGE);
				if (n == 2) { // cancel
					return;
				} else if (n == 0){
					byte[] encryptedFileNameBytes = encryptedFileName.getBytes(UTF_8);

					WriteResources.write(encryptedFileNameBytes, pathFileName, null);
				}
			return;
		} else {
			String pathNamesString = new String(pathFileContent, UTF_8);

			String[] pathNames = pathNamesString.split("\n");
			// check if encryptedFileName is already included
			for (int i = 0; i < pathNames.length; i++) {
				if (pathNames[i].equals(encryptedFileName)) {
					System.out.println("Already included: " + encryptedFileName);
					return;
				}
			}
						
			// is not included:
			int n = JOptionPane.showConfirmDialog(null,
			//	"Should the name of the file be saved to remember for the next program start? \n" 
			//+ "(The name is then saved in " + pathFileName + " in the same folder as this program).", 
			languagesBundle.getString("remember_files")
			+ languagesBundle.getString("path_file_storage_info")
			+ pathFileName , 
			"?", 
			JOptionPane.OK_CANCEL_OPTION, 
			JOptionPane.PLAIN_MESSAGE);
			if (n == 2) { // cancel
				return;
			} else if (n == 0){
				byte[] encryptedFileNameBytes =  ("\n" + encryptedFileName).getBytes(UTF_8);

				byte[] newContent = new byte[pathFileContent.length + encryptedFileNameBytes.length];
				System.arraycopy(pathFileContent, 0, newContent, 0, pathFileContent.length);
				System.arraycopy(encryptedFileNameBytes, 0, newContent, pathFileContent.length, encryptedFileNameBytes.length);
				
				if (newContent != null) {
					WriteResources.write(newContent, pathFileName, null);
				}
				
			}
		}		
	} 


	//==========================================
	// Getter & Setter

	public final static Charset getCharset(){
		return UTF_8;
	}
	
	protected final static void setFileType(String _fileType) {
		fileType = _fileType;
	}
	public final static String getFileType() {
		return fileType;
	}
	public final static String getEncryptedFileName() {
		return encryptedFileName;
	}
	public final static void setEncryptedFileName(String _encryptedFileName) {
		encryptedFileName = _encryptedFileName;
	}
	protected final static void setDialog(PswDialogBase _dialog) {
		dialog = _dialog;
	}
	public final static PswDialogBase getDialog() {
		return dialog;
	}
	public final static String getPathFileName() {
		return PATH_FILE_NAME;
	}
	public final static JPanel getTypePanel() {
		return typePanel;
	}
	public final static void setTypePanel( JPanel panel){
		typePanel = panel;
	}
	public final static String getErrorMessage() {
		return errorMessage;
	}
	public final static void setErrorMessage(String newErrorMessage) {
		errorMessage = newErrorMessage;
	}
	public final static ResourceBundle getBundle(){
		if (languagesBundle == null) { // set default language
			try {
				setLanguagesBundle(null);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return languagesBundle;
	}
	
	public char[] getInitializedPasword(){
		return initializedPassword;
	}
	public void setInitializedPassword(char[] psw){
		initializedPassword = psw;
	}
	/**
	 * @return the workingMode
	 */
	public static String getWorkingMode() {
		return workingMode;
	}
	/**
	 * @param workingMode the workingMode to set
	 */
	public static void setWorkingMode(String workingMode) {
		PswDialogBase.workingMode = workingMode;
	}
}
