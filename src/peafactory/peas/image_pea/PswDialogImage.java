package cologne.eck.peafactory.peas.image_pea;

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
 * Dialog to get the password and start the image pea. 
 */

/*
 * Password dialog to decrypt image. 
 * Image is displayed in LockFrameImage. 
 * 
 * Password can not be changed and content (image) can not be modified. 
 * 
 * cipher text is stored in external file or inside jar
 * 
 */

import java.io.File;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.NewPasswordDialog;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;


final class PswDialogImage extends PswDialogBase {

	// Setting (previously appended to image file)
	protected static int imageWidth = 800;
	protected static int imageHeight = 600;
	protected static boolean resize = false;

	private static PswDialogView dialogView;

	private static PswDialogImage pswDialog;	
	private static LockFrameImage lockFrame = null;
	
	private PswDialogImage() {		
		
		pswDialog = this;		
		dialogView = PswDialogView.getInstance();
	}
	
	private final static PswDialogImage getInstance() {

		PswDialogImage dialog = null;
		if (pswDialog == null) {

			dialog = new PswDialogImage();
			
		} else {
			// return null
		}
		return dialog;
	}
	
	public static void main(String[] args) {		
		
		if(args.length > 0) {
			PswDialogBase.setWorkingMode(args[0]);
		}		
		initializeVariables();
		
		// settings:
		PswDialogBase.setFileType("image");
		
		Attachments.setFileIdentifier(PeaSettings.getFileIdentifier());
		if (CipherStuff.isBound() == true){
			Attachments.setProgramRandomBytes(PeaSettings.getProgramRandomBytes());
		} else { // if (PeaSettings.getBound() == false) { 
			// check if pea was initialized, if not set value initializing in PswDialogView		
			PswDialogView.checkInitialization();
		}

		PswDialogImage pswDialog = PswDialogImage.getInstance();		
		setDialog(pswDialog);		
		
		PswDialogImage.dialogView.setVisible(true);
		
		if (PswDialogView.isInitializing() == true) {
			
			NewPasswordDialog.setRandomCollector(true);
			NewPasswordDialog newPswDialog = NewPasswordDialog.getInstance(PswDialogImage.dialogView);
			pswDialog.setInitializedPassword( newPswDialog.getDialogInput() );
			NewPasswordDialog.setRandomCollector(false);

			if (newPswDialog.getDialogInput() == null
					|| Arrays.equals(newPswDialog.getDialogInput(), "no password".toCharArray() )) {
				PswDialogView.setMessage("Program continues with no password.\n "
						+ "You can set the password later.\n\n "
						+ "Select the image you want to encrypt...");
			} else {
				PswDialogView.setMessage("Select the image you want to encrypt...");

				PswDialogImage.dialogView.clickOkButton();
			}
		}
	}

	
	//-------------------HELPER-FUNCTIONS--------------
	
	// settings for displaying the image
	// setting informations are added to the end of the plain text
	private final byte[] setAndCutSettingBytes(byte[] plainBytes) {
		
		if (plainBytes == null) {
			JOptionPane.showMessageDialog(PswDialogView.getView(),
				    "Missing encrypted image.\n"
					+ "Program bug.",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			PswDialogView.clearPassword();
			return null;
		}		
		if (plainBytes.length < 12) {
			JOptionPane.showMessageDialog(PswDialogView.getView(),
				    "Invalid encrypted image file.\n",
				    "Error",
				    JOptionPane.ERROR_MESSAGE);
			PswDialogView.clearPassword();
			System.err.println("cipher length can not contain image settings");
			return null;
		}
		
		byte[] settingBytes = new byte[12];
		byte[] newCipherBtes = new byte[plainBytes.length - 12];
		
		System.arraycopy(plainBytes, 0, newCipherBtes, 0, newCipherBtes.length);
		System.arraycopy(plainBytes,  plainBytes.length - 12,  settingBytes,  0,  settingBytes.length);
		
		int[] imageSetting = Converter.bytes2intsBE(settingBytes);
		imageWidth = imageSetting[0];
		imageHeight = imageSetting[1];
		if (imageSetting[2] == 1) {
			resize = true; 
		} else if (imageSetting[2] == 0) {
			resize = false;
		} else {
			System.err.println("cipher does not contain correct image settings");
			
			dialogView.displayErrorMessages("Image file does not contain image settings.\n"
					+ "Program bug.");
			PswDialogView.clearPassword();
			return null;
		}
		return newCipherBtes;		
	}


	@Override
	public void clearSecretValues() {
		// is done in LockFrameImage to handle initialization
		//LockFrameImage.clearImageBytes();		
	}

	@Override
	public void startDecryption() {
		
		byte[] cipherBytes = null;
		if (PswDialogBase.searchSingleFile().equals("insideJar")){

			String insideJarName = "resources/text.lock";		
			
			if(PeaSettings.getBound() == true){
				// text.lock in folder resources beside jar file
				cipherBytes = ReadResources.getResourceFromJAR(insideJarName);
			} else {
				// text.lock inside of jar file
				cipherBytes = ReadResources.readExternFile(insideJarName);
			}
			
		} else {
			// file anywhere
			cipherBytes = ReadResources.readExternFile(PswDialogBase.getEncryptedFileName());// there is only one file name
		}
		
		if ( PswDialogView.isInitializing() == true ){
			System.out.println("Initialization");
			
			// setting byte are attached on text.lock in plaintext
			// cut settings and set image width and height:
			cipherBytes = setAndCutSettingBytes( cipherBytes);
			
			// handle the salt
			cipherBytes = initializeCiphertext(cipherBytes);
		}


		if (CipherStuff.isBound() == false){
			// get and set the attached salt:
			handleAttachedSalt(cipherBytes);
		}
			
		byte[] keyMaterial = getKeyMaterial();
		
		if (keyMaterial == null) { // bug
			return;
		}

		
		//
		// decrypt:
		//	
		byte[] plainBytes = null;
		
		if (PswDialogView.isInitializing() == true) {
			// select a file to encrypt, read file and set image for LockFrameImage
			// a copy of this file + settings will be store beside the jar file
			// in "resources/text.lock"
			
			// set session key:
			CipherStuff.getInstance().getSessionKeyCrypt().storeKey(keyMaterial);
			
			// open FileChooser for Images			
			JFileChooser chooser = new JFileChooser();

		    chooser.setAcceptAllFileFilterUsed(false);
		    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
		        "Images: jpg, gif, png", "jpg", "gif", "png", "jpeg", "JPG", "JPEG", "GIF", "PNG");
		    chooser.setFileFilter(imageFilter);
			
		    int returnVal = chooser.showOpenDialog(null);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {	// ok

		    	File selectedFile = chooser.getSelectedFile();
		    	String selectedFileName = selectedFile.getAbsolutePath();	
		    	
		    	// set file name in PswDialogBase
		    	PswDialogBase.setEncryptedFileName(selectedFileName);
		    	
		    	// read file (it is plaintext)
		    	plainBytes = ReadResources.readExternFile(selectedFileName);

		    } else { // no file chosen
		    	System.out.println("No file selected");
		    	return;
		    }	
			// set LockFrame:
			LockFrameImage.setImageBytes(plainBytes);
			// display decrypted image: 
			if (lockFrame == null) {
				lockFrame = LockFrameImage.getInstance();
			}

			lockFrame.setVisible(true);	
			
		} else { // no initialization

			if (CipherStuff.isBound() == false){
				// encrypt key by sessionKey
				plainBytes = CipherStuff.getInstance().decrypt(cipherBytes, keyMaterial, true );
			} else {
				// do not use sessionKey
				plainBytes = CipherStuff.getInstance().decrypt(cipherBytes, keyMaterial, false );
			}
	
			if (plainBytes == null) {
				dialogView.displayErrorMessages("Decryption failed");
				PswDialogView.clearPassword();
				return;
			}		
			
			// cut settings and set image width and height
			plainBytes = setAndCutSettingBytes( plainBytes);

			LockFrameImage.setImageBytes(plainBytes);

			// display decrypted image: 
			if (lockFrame == null) {
				lockFrame = LockFrameImage.getInstance();
			}

			lockFrame.setVisible(true);		
		}

		PswDialogView.getView().setVisible(false);		
		
		if (PeaSettings.getExternFile() == true) {
			pathFileCheck();
		}				
	}
	
	// not used here:
	@Override
	public void preComputeInThread() {}
	@Override
	public String[] getSelectedFileNames() {return null;}
}	
