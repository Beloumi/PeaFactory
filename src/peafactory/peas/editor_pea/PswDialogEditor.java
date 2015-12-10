package cologne.eck.peafactory.peas.editor_pea;

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
 * Dialog to get the password and start the editor pea.
 */


import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.Zeroizer;


public final class PswDialogEditor extends PswDialogBase {

	private static PswDialogView dialogView;
	private static PswDialogEditor pswDialog;
	private static LockFrameEditor lockFrame; 

	private PswDialogEditor() {
		
		pswDialog = this;		
		dialogView = PswDialogView.getInstance();
	}
	
	private static PswDialogEditor getInstance() {
		PswDialogEditor dialogInstance = null;
		if (pswDialog == null) {
		dialogInstance = new PswDialogEditor();
		} else {
			// return null
		}
		return dialogInstance;
	}
	
	public static void main(String[] args) {
		
		if(args.length > 0) {
			PswDialogBase.setWorkingMode(args[0]);
		}		
		initializeVariables();
		
		// settings:
		PswDialogBase.setFileType("text file");
		
		Attachments.setFileIdentifier(PeaSettings.getFileIdentifier());
		if (CipherStuff.isBound() == true){
			Attachments.setProgramRandomBytes(PeaSettings.getProgramRandomBytes());
		} else { // if (PeaSettings.getBound() == false) { // initialized?
			
			PswDialogView.checkInitialization();
		}
		PswDialogEditor pswDialog = PswDialogEditor.getInstance();		
		setDialog(pswDialog);		
		
		PswDialogEditor.dialogView.setVisible(true);
	}
	
	private final void displayText(byte[] plainBytes) {
		
		Object obj = Converter.deserialize( plainBytes);
		Zeroizer.zero(plainBytes);
		
		if (! (obj instanceof DefaultStyledDocument) ) { // wrong psw (malformed file) not all wrong files
			PswDialogView.setLabelText("unsuitable document");
			System.err.println("unsuitable document, not styled document");
			DefaultStyledDocument doc = (DefaultStyledDocument) LockFrameEditor.textPane.getStyledDocument();
			try {
				doc.remove(0, doc.getLength() );
			} catch (BadLocationException e) {
				System.err.println("EditorLockFrame.decryptAndOpenDocument, remove content of document " + e);
			}	
			lockFrame.setChars("unsuitable document, try another file...".toCharArray());
		} else {
			DefaultStyledDocument doc = (DefaultStyledDocument) obj;
			LockFrameEditor.textPane.setStyledDocument(doc);	
			
			// Listener must be added here
			LockFrameEditor.textPane.getDocument().addUndoableEditListener(LockFrameEditor.manager);
			LockFrameEditor.textPane.getDocument().addDocumentListener(lockFrame);
		}						
	}
	
	//====================================
	// Getter & Setter
	protected final static LockFrameEditor getLockFrame() {
		return lockFrame;
	}

	protected byte[] getCipherBytes(String fileName) {

		byte[] cipherBytes = ReadResources.readExternFile(fileName);// there is only one file name	
		return cipherBytes;
	}

	@Override
	public void clearSecretValues() {
		if (CipherStuff.getInstance().getSessionKeyCrypt() != null) {
			CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();
		}
	}

	@Override
	public void startDecryption() {		
		

		byte[] cipherBytes = getCipherBytes(PswDialogBase.getEncryptedFileName() );
		
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
		//byte[] plainBytes = CipherStuff.getInstance().decrypt(cipherBytes, keyMaterial, true );
		byte[] plainBytes = null;
		if (PswDialogView.isInitializing() == true){
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

		// display decrypted text: 
		if (lockFrame == null) {
			lockFrame = LockFrameEditor.getInstance(pswDialog);
		}
		displayText(plainBytes);
		Zeroizer.zero(plainBytes);

		lockFrame.setVisible(true);		

		if (PswDialogView.isInitializing() == true){
			// there is only one file: 
			String[] fileNames = { PswDialogBase.getEncryptedFileName()};
			// set new password:
			lockFrame.changePassword(plainBytes, fileNames);
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
