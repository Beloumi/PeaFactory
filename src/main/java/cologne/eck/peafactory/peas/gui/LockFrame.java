package cologne.eck.peafactory.peas.gui;

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
 * Parent class of all pea frames except image pea. 
 */

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.tools.WriteResources;
import cologne.eck.peafactory.tools.Zeroizer;

@SuppressWarnings("serial")
public abstract class LockFrame extends JFrame implements WindowListener {
	

	protected abstract void windowClosingCommands();
	
	protected abstract byte[] getPlainBytes();
	
	
	
	public final static void saveContent(byte[] keyMaterial, byte[] plainBytes, String fileName) {
		
		if (new File(fileName).isFile() == false) {
			System.err.println("LockFrame wrong file");
			return;
		}

		//Help.printBytes("LockFrame: plainBytes", plainBytes);
		byte[] cipherBytes = CipherStuff.getInstance().encrypt(plainBytes, keyMaterial, 
				true );

		if(PeaSettings.getExternFile() == true) {
			WriteResources.write(cipherBytes,fileName, null);
		} else {
			WriteResources.write(cipherBytes, "text.lock", "resources");// PswDialog2.jarFileName + File.separator +
		}
	}
	
	public final void changePassword(byte[] plainBytes, String[] fileNames){
		
		NewPasswordDialog newPswDialog = NewPasswordDialog.getInstance(this);
		char[] newPsw = newPswDialog.getDialogInput();		
		
		byte[] keyMaterial = PswDialogBase.deriveKeyFromPsw(newPsw);
		if (newPsw != null){
			Zeroizer.zero(newPsw);
		}
		byte[] unfilledKeyMaterial = new byte[keyMaterial.length];
		for (int i = 0; i < fileNames.length; i++) {			
			if (keyMaterial != null) {
				// keyMaterial is filled/overwritten in encryption
				System.arraycopy(keyMaterial,  0,  unfilledKeyMaterial,  0,  keyMaterial.length);	
			}
			saveContent(unfilledKeyMaterial, plainBytes, fileNames[i]);
		}
		Zeroizer.zero(keyMaterial);
	}
	


	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}

	@Override
	public void windowClosing(WindowEvent arg0) {
		windowClosingCommands();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
}
