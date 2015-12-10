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
 * Dialog to get the password and start the file pea.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JCheckBox;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.RandomStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.NewPasswordDialog;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Zeroizer;

/*
 * 	main: 
 * 		- PswDialogBase.setFileType("file");	
 * 		- PswDialogBase.initializeVariables(); // KDFScheme, HashAlgo, CipherAlgo, triple, programRandomBytes, fileIdentifier, salt
 * 		- setSessionKeyAndIV ( new RandomStuff().createRandomBytes(
				CryptStuff.getCipherAlgo().getBlockSize() + CryptStuff.getCipherAlgo().getKeySize() ));
 * 		- pswDialog = PswDialogFile.getInstance();
 * 
 *  protected final static void clearSessionKeyAndIV()
 *  
 *  protected final static String[] getChosenFileNames()
 * 
 * 		lockFrame is initialized in startDecryption
 */



final class PswDialogFile extends PswDialogBase {

	private static LockFrameFile lockFrame;
	
	private static PswDialogFile pswDialog;
	private static PswDialogView dialogView;
	
	private static String[] chosenFileNames;
	
	
	private PswDialogFile() {
	
		pswDialog = this;
		dialogView = PswDialogView.getInstance();
	}
		

	
	private final static PswDialogFile getInstance() {
		if (pswDialog == null) {
			pswDialog = new PswDialogFile();
		} else {
			//return null
		}
		return pswDialog;
	}
	

	public static void main(String[] args) {
		
		
		if(args.length > 0) {
			PswDialogBase.setWorkingMode(args[0]);
		}
		
		PswDialogBase.setFileType("file");	
		
		Attachments.setFileIdentifier(PeaSettings.getFileIdentifier());
		if (PeaSettings.getBound() == true){
			CipherStuff.setBound(true);
			Attachments.setProgramRandomBytes(PeaSettings.getProgramRandomBytes());

		} else { // if (PeaSettings.getBound() == false) { 
			// check if pea was initialized, if not set value initializing in PswDialogView	
			PswDialogView.checkInitialization();
		}
		
		PswDialogBase.setTypePanel( new FileTypePanel(300, 200, false));
		
		PswDialogView.setUI();

		PswDialogBase.initializeVariables();

		pswDialog = PswDialogFile.getInstance();
		setDialog(pswDialog);
		dialogView.setVisible(true);
	}

	private boolean validFileFound(FileComposer fc){

		// check if at least one file was successfully decrypted:
		boolean success = false;
		for (int i = 0; i < fc.getAnnotatedFileNames().size(); i++) {
			if ( (fc.getAnnotatedFileNames().get(i).endsWith(FileTypePanel.getDirectoryMarker() ) ) ) {
				continue;
			}
			if (! (fc.getAnnotatedFileNames().get(i).contains(FileTypePanel.getInvalidMarker() )) ) {
				success = true;
				break;
			}
		}

		if (success == false) {
			PswDialogView.setMessage("None of these files was encrypted with this password...\n" 
					      + "Try another password or open other files.");

			return false;
		} else { // at least one valid file found
			return true;
		}
	}

	
	//-------------------HELPER-FUNCTIONS--------------			


	@Override
	public void preComputeInThread() {
		//getCipherBytes(encryptedFileName);		
	}

	@Override
	public void clearSecretValues() {
		if (CipherStuff.getInstance().getSessionKeyCrypt() != null) {
			CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();
		}
	}

	@Override
	public void startDecryption() {
		
		FileTypePanel encryptedFilePanel = (FileTypePanel) PswDialogView.getView().getFilePanel();
		FileTypePanel decryptedFilePanel = null;
		FileComposer fc = null;
		byte[] keyMaterial = null;
		
		try {

			decryptedFilePanel = new FileTypePanel(300,300, true);
			fc = decryptedFilePanel.getFileComposer();		
			
			
			if ( PswDialogView.isInitializing() == true ){
				System.out.println("Initialization");
				
				// reset salt if password failed before:
				KeyDerivation.setSalt(Attachments.getProgramRandomBytes());
				// set new salt to attach to file and xor with programRandomBytes
				KeyDerivation.setAttachedAndUpdateSalt(
						new RandomStuff().createRandomBytes(KeyDerivation.getSaltSize()));

				// add files to encrypt:
				decryptedFilePanel.addAction();		
				if (validFileFound(fc) == false){
					return;
				}
				// create the frame: 
				LockFrameFile.setFileDisplayPanel(decryptedFilePanel);
				lockFrame = LockFrameFile.getInstance();			

				decryptedFilePanel.displayNewNumberAndSize();

				lockFrame.setVisible(true);		

				decryptedFilePanel.revalidate();
				
				// add selected files to path file to display on next start:
				PswDialogBase.addFilesToPathFile(decryptedFilePanel.getSelectedFileNames());
				
				// set a password
				NewPasswordDialog newPswDialog = NewPasswordDialog.getInstance(PswDialogView.getView());
				char[] newPsw = newPswDialog.getDialogInput();

				// create new Nonce and set
				byte[] nonce = new RandomStuff().createRandomBytes(32);
				Attachments.setNonce(nonce);

				// derive the key from password and salt
				keyMaterial = PswDialogFile.deriveKeyFromPsw(newPsw);
				if (newPsw != null){
					Zeroizer.zero(newPsw);
				}
							
				// encrypt and set the key in RAM
				CipherStuff.getInstance().encryptKeyFromKeyMaterial(keyMaterial);

				// delete the file that contains the indicator string  to initialize:
				File initFile = new File(("resources" + File.separator + "text.lock"));
				try{
					System.gc();// bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154 
					initFile.delete();
				} catch (Exception e){ // if failed: delete the content:
					FileOutputStream to = null;
					try {
						to = new FileOutputStream(initFile);
				        to.write(" ".getBytes());
				        to.flush();
				        to.close();
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}	
				// close this view
				PswDialogView.getView().setVisible(false);	
				
				return;
				
			} 

				
			chosenFileNames = encryptedFilePanel.getSelectedFileNames();
			//		for(int i=0;i<chosenFileNames.length;i++) System.out.println("chosenFileName " + i + ": " + chosenFileNames[i]);

			decryptedFilePanel.showNewCheckBoxes( chosenFileNames );// doubles are not added and shown
			
			// all sub-directories and files of selected directories are included and selected
			// -> deselect previously unselected files and sub-directories: 
			JPanel lfCheckPanel = decryptedFilePanel.getCheckPanel();
			// deselect loop: deselect all
			for (int i = 0; i < lfCheckPanel.getComponentCount(); i++) {
				( (JCheckBox) lfCheckPanel.getComponent(i)).setSelected(false);
			}
			// select loop
			for (int i = 0; i < fc.getOriginalFileNames().size(); i++) {
				int checkBoxIndex = fc.getOriginalFileNames().indexOf(chosenFileNames[i]);
				( (JCheckBox) lfCheckPanel.getComponent(checkBoxIndex)).setSelected(true);
			}

			if (CipherStuff.isBound() == false){
				
				if (chosenFileNames[0] == null){
					PswDialogView.setMessage("No vald file was selected:\n");

				}

				// get and set the attached salt:
				//handleAttachedSalt(cipherBytes);
				int saltLen = KeyDerivation.getSaltSize();
				byte[] end = null;
				try {
					end = Attachments.getEndBytesOfFile( 
							chosenFileNames[0], 
							Attachments.getFileIdentifierSize() 
							+ saltLen);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					PswDialogView.setMessage("The file: \n" + chosenFileNames[0] + "\n"
								  + "was inappropriate and caused an error: \n"
							      + e.toString() + "\n"
							      + "(attached salt not found))\n"
							      + "Execution is aborted.");

					e.printStackTrace();
					return;
				}
				
				byte[] attachedSalt = new byte[saltLen];
				System.arraycopy(end,  0,  attachedSalt,  0,  saltLen);
				// reset salt if password failed before:
				KeyDerivation.setSalt(Attachments.getProgramRandomBytes());
				KeyDerivation.setAttachedAndUpdateSalt(attachedSalt);
			}
			
			// KDF:
			// derive the key from password and salt: 
			keyMaterial = getKeyMaterial();

			if (keyMaterial == null) {
					PswDialogView.setLabelText("Key derivation failed");
					PswDialogView.clearPassword();
					dialogView.pack();
					return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			PswDialogView.setMessage("An unexpected error has occurred:\n"
				      + ex.toString() + "\n"
				      + "Execution is aborted before decryption.");

			System.exit(2);
		}
//		long start = System.currentTimeMillis(); // Startpunkt
		
		encryptedFilePanel.startProgressTask();// starts if execution > 1 second

		String[] errorMessages = CipherStuff.getCipherMode().decryptFiles( chosenFileNames, keyMaterial, 
				true, encryptedFilePanel );

		encryptedFilePanel.closeProgressTask();// close automatically

		for (int i = 0; i < errorMessages.length; i++) {
			if (errorMessages[i] != null) {

				String newName = FileTypePanel.getInvalidMarker() 
						+  errorMessages[i] 
						+ fc.getOriginalFileNames().get(i);
				decryptedFilePanel.setCheckBoxInvalid(fc.getOriginalFileNames().get(i), newName);
			}
		}		
		
		if (validFileFound(fc) == false){
			return;

		} else { // at least one valid file found
			
			PswDialogView.clearPassword();			

			LockFrameFile.setFileDisplayPanel(decryptedFilePanel);
			lockFrame = LockFrameFile.getInstance();			

			decryptedFilePanel.displayNewNumberAndSize();

			lockFrame.setVisible(true);		

			decryptedFilePanel.revalidate();

			PswDialogView.getView().setVisible(false);	
		}
	}


	// used in PswDialogView to add selected file to path file:
	@Override
	public String[] getSelectedFileNames() {

		return ( (FileTypePanel)dialogView.getFilePanel()).getSelectedFileNames();

	}
}	
