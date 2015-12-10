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
 * Main frame to display the selected files for file pea. 
 */


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.crypto.RandomStuff;
import cologne.eck.peafactory.peas.gui.LockFrame;
import cologne.eck.peafactory.peas.gui.NewPasswordDialog;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Attachments;
import cologne.eck.peafactory.tools.Zeroizer;





@SuppressWarnings("serial")
public class LockFrameFile extends LockFrame implements ActionListener {


	private static LockFrameFile frame;
	private static FileTypePanel filePanel;

	
	private LockFrameFile( ){//String[] selectedFileNames) {//char[] textChars) {
		frame = this;
		
		//for(int i=0;i<selectedFileNames.length;i++) System.out.println("selected: " + selectedFileNames[i]);

		
	    this.setIconImage( PswDialogView.getImage() );
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);// for windowClosing
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		this.setContentPane(contentPane);		
		
		
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
		
		JMenuBar menuBar = new JMenuBar();
		//menuBar.setPreferredSize(new Dimension(300, 30));
		menuBar.setBorder(new EtchedBorder() );
		
		JMenu menu = new JMenu();
		menu.setText("Menu");
		menuBar.add(menu);
		
		JMenuItem encryptItem = new JMenuItem();
		encryptItem.setText("close and encrypt selected files");
		encryptItem.setActionCommand("encryptWithClosing");
		encryptItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		encryptItem.setMnemonic(KeyEvent.VK_E);
		encryptItem.addActionListener(this);
		menu.add(encryptItem);
		
		JMenuItem unencryptItem = new JMenuItem();
		unencryptItem.setText("close and leave all opened file unencrypted");
		unencryptItem.setActionCommand("leaveUnencrypted");
		unencryptItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		unencryptItem.setMnemonic(KeyEvent.VK_U);
		unencryptItem.addActionListener(this);
		menu.add(unencryptItem);
		
		JMenuItem addItem = new JMenuItem();
		addItem.setText("add new files to encrypt");
		addItem.setActionCommand("addNewFiles");
		addItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		addItem.setMnemonic(KeyEvent.VK_E);
		addItem.addActionListener(this);
		menu.add(addItem);

//		if (PswDialogBase.getPswChangeable() == true) {
			JMenuItem pswChangeItem = new JMenuItem();
			pswChangeItem.setText("change password");
			pswChangeItem.setActionCommand("changePsw");
			pswChangeItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			pswChangeItem.setMnemonic(KeyEvent.VK_P);
			pswChangeItem.addActionListener(this);
			menu.add(pswChangeItem);
//		}
		menuPanel.add(menuBar);
		menuPanel.add(Box.createHorizontalGlue());
		contentPane.add(menuPanel);
		
		contentPane.add(Box.createVerticalStrut(20) );
		
		//
		// displayed Files:
		//
		filePanel.removeAddButton();
		contentPane.add(filePanel);
		

		contentPane.add(Box.createVerticalStrut(10) );
		
		
		JPanel noteLabelPanel = new JPanel();
		noteLabelPanel.setLayout(new BoxLayout(noteLabelPanel, BoxLayout.X_AXIS));
		JLabel noteLabel = new JLabel();
		noteLabel.setText(" - Selected files are automatically encrypted by closing this window - ");
		noteLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
		noteLabelPanel.add(noteLabel);
		noteLabelPanel.add(Box.createHorizontalGlue());
		contentPane.add(noteLabelPanel);
		
		this.setLocation(100, 100);
		//this.setMinimumSize(new Dimension(400, 500));
		pack();		
	}

	protected final static LockFrameFile getInstance(){//String[] selectedFileNames) {
		//FileLockFrame frame = null;
		if (frame == null) {
			frame = new LockFrameFile();// selectedFileNames);
		} else {
			System.out.println("there is already an instance of FileLockFrame");
			return null;
		}
		return frame;
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		if ( ape.getActionCommand().equals("encryptWithClosing") ) {	


			// if unselected files remain: warning
			if (warningForRemainingFiles() == false) {
				return;
			}
			encryptCurrentFiles(null, true);
			CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();		
			System.exit(0);
			
		} else if ( ape.getActionCommand().equals("leaveUnencrypted") ) {		
			int result = JOptionPane.showConfirmDialog(this,
					   "Do you really want to leave all the files unencrypted?",
					   "Warning",
					   JOptionPane.WARNING_MESSAGE,
					    JOptionPane.YES_NO_OPTION);
			// yes = 0, no = 1, cancel = 2
			if (result == 0) {		
				CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();		
				System.exit(0);
			} else {
				return;
			}
			
		} else if ( ape.getActionCommand().equals("addNewFiles") ) {		
			filePanel.addAction();
			

		} else if ( ape.getActionCommand().equals("changePsw") ) {		

			// if unselected files remain: warning
			if (warningForRemainingFiles() == false) {
				return;
			}
			
			NewPasswordDialog newPswDialog = NewPasswordDialog.getInstance(this);
			char[] newPsw = newPswDialog.getDialogInput();

			//==============================================
			// new Nonce for every new password
			byte[] nonce = new RandomStuff().createRandomBytes(32);//, PswDialog.hashAlgo);
			Attachments.setNonce(nonce);
			//==============================================

			byte[] keyMaterial = PswDialogFile.deriveKeyFromPsw(newPsw);
			if (newPsw != null){
				Zeroizer.zero(newPsw);
			}
						
			CipherStuff.getInstance().encryptKeyFromKeyMaterial(keyMaterial);//, PswDialogFile.getSessionKeyAndIV() );
		}
	}
	
	private final static void encryptCurrentFiles(byte[] keyMaterial, boolean write) {
		
		String[] fileNames = filePanel.getSelectedFileNames();

		filePanel.startProgressTask();// starts if execution > 1 second
		String[] errorMessages = CipherStuff.getCipherMode().encryptFiles(fileNames, keyMaterial, true, filePanel);
		filePanel.closeProgressTask();// close automatically
		
		// give informations if there has been errors:
		String errors = null;
		for (int i = 0; i < errorMessages.length; i++) {
			if (errorMessages[i] != null) {
				errors +=  errorMessages[i] + " - " + fileNames[i] + "\n";
			}
		}	
		if (errors != null) {
			JOptionPane.showMessageDialog(filePanel, 
					   "Error occured in decryption process:\n"    
					      + errors, 
					   "Decryption error",                                
					   JOptionPane.ERROR_MESSAGE);    
		}
	}
	
	private final boolean warningForRemainingFiles(){
		// if unselected files remain: warning
		String[] unselectedFiles = filePanel.getUnselectedValidFileNames();
		if (unselectedFiles != null) {
			StringBuilder unselectedFilesBuilder = new StringBuilder();
			for (int i = 0; i < unselectedFiles.length; i++) {
				unselectedFilesBuilder.append(unselectedFiles[i]);
				unselectedFilesBuilder.append("\n");
			}
			String unselectedFilesString = new String(unselectedFilesBuilder);
			
			JPanel optionPanel = new JPanel();
			optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
			JLabel optionLabel = new JLabel();
			optionLabel.setText("There are unselected files left." +
				    "These files are left unencrypted:");
			JTextArea optionArea = new JTextArea();
			optionArea.setText( unselectedFilesString);
			JScrollPane optionScroll = new JScrollPane(optionArea);  
			optionArea.setLineWrap(true);  
			optionArea.setWrapStyleWord(true); 
			optionScroll.setPreferredSize( new Dimension( 300, 300 ) );
			optionPanel.add(optionLabel);
			optionPanel.add(optionScroll);
			int result = JOptionPane.showConfirmDialog(null, optionPanel, "Warning",  
					JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			
			if (result == JOptionPane.OK_OPTION) {
				return true;
			} else { // cancel
				return false;
			}
		}
		return true; // close
	}

	@Override
	public void windowClosing(WindowEvent arg0) { // by X-Button
		
		// if unselected files remain: warning
		if (warningForRemainingFiles() == false) {
			// nix
		} else {
			encryptCurrentFiles(null, true);
			CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();		
			System.exit(0);
		}		
	}

	@Override
	protected void windowClosingCommands() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected byte[] getPlainBytes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//==================================
	// Getter & Setter
	protected final static FileTypePanel getFileDisplayPanel() {
		return filePanel;
	}
	protected final static void setFileDisplayPanel(FileTypePanel _fdp) {
		filePanel = _fdp;
	}
}
