package cologne.eck.peafactory.peas.note_pea;

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
 * Dialog to get the password and start the note pea. 
 */


import java.io.File;
import java.util.Arrays;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.NewPasswordDialog;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.Zeroizer;


class PswDialogNotes extends PswDialogBase {

	private static LockFrameNotes lockFrame;// = new ChangeableLockFrame();
	
	private static PswDialogView dialogView;
	private static PswDialogNotes dialog;
	
	private PswDialogNotes() {
		dialog = this;
		dialogView = PswDialogView.getInstance();
	}
	
	protected final static PswDialogNotes getInstance() {
		if (dialog == null) {
			dialog = new PswDialogNotes();			
		} else {
			// return existing dialog
		}
		return dialog;
	}
	

	public static void main(String[] args) {						
		
		if(args.length > 0) {
			PswDialogBase.setWorkingMode(args[0]);
		}
		
		// if the previously internal file was moved to another place: 
		if(!PeaSettings.getExternFile() &&
				!new File("resources" + File.separator + "text.lock").exists()) {
//FIXME			PeaSettings.setExternFile(true);
			PswDialogBase.setEncryptedFileName("");
		}
		
		initializeVariables();		

		// settings:
		PswDialogBase.setFileType("text");
		
		Attachments.setFileIdentifier(PeaSettings.getFileIdentifier());
		if (CipherStuff.isBound() == true){
			Attachments.setProgramRandomBytes(PeaSettings.getProgramRandomBytes());
		} else { // if (PeaSettings.getBound() == false) { // initialized?
		
			PswDialogView.checkInitialization();
		}
		
		PswDialogNotes pswDialog = PswDialogNotes.getInstance();		
		setDialog(pswDialog);
			
		PswDialogNotes.dialogView.setVisible(true);
		
		if (PswDialogView.isInitializing() == true) {
			
			NewPasswordDialog.setRandomCollector(true);
			NewPasswordDialog newPswDialog = NewPasswordDialog.getInstance(PswDialogNotes.dialogView);
			pswDialog.setInitializedPassword( newPswDialog.getDialogInput() );
			NewPasswordDialog.setRandomCollector(false);

			if (newPswDialog.getDialogInput() == null
					|| Arrays.equals(newPswDialog.getDialogInput(), "no password".toCharArray() )) {
				PswDialogView.setMessage("Program continues with no password.\n "
						+ "You can set the password later.");
			} else {
				PswDialogNotes.dialogView.clickOkButton();
			}
		}
	}

	
	@Override
	public
	final void startDecryption() {	
		
		//byte[] cipherBytes = getCipherBytes(PswDialogBase.getEncryptedFileName() );
		byte[] cipherBytes = ReadResources.readExternFile(PswDialogBase.getEncryptedFileName());// there is only one file name	
		
		if (PswDialogView.isInitializing() == true) {
			
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
		if (PswDialogView.isInitializing() == true){

			CipherStuff.getInstance().getSessionKeyCrypt().storeKey(keyMaterial);
			// one space as content:
			plainBytes = " ".getBytes(getCharset());
		} else {
			// decrypt:
			plainBytes = CipherStuff.getInstance().decrypt(cipherBytes, keyMaterial, true );
		}

		if (plainBytes == null) {
			
			dialogView.displayErrorMessages("Decryption failed");

			PswDialogView.clearPassword();
			return;
		}

		char[] textChars = Converter.bytes2chars(plainBytes);
		Zeroizer.zero(plainBytes);


		// display decrypted text: 
		if (lockFrame == null) {
			lockFrame = LockFrameNotes.getInstance();
		}
		lockFrame.setChars( textChars );
		lockFrame.setVisible(true);		


		PswDialogView.getView().setVisible(false);		
		
		if (PeaSettings.getExternFile() == true) {
			pathFileCheck();
		}		
	}

	@Override
	public void clearSecretValues() {
		if (CipherStuff.getInstance().getSessionKeyCrypt() != null) {
			CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();
		}
	}

	// not used here:
	@Override
	public void preComputeInThread() {}
	@Override
	public String[] getSelectedFileNames() {return null;}	
}	
