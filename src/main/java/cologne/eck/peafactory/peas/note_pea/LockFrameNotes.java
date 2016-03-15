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
 * Main view of note pea. 
 */


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.LockFrame;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.Zeroizer;


@SuppressWarnings("serial")
public class LockFrameNotes extends LockFrame implements ActionListener {

	private static LockFrameNotes lockFrame;	
	private JTextArea textArea;
	

	private LockFrameNotes() {
		lockFrame = this;
		
		setTitle(PswDialogBase.getEncryptedFileName());
		
	    this.setIconImage( PswDialogView.getImage() );
		
		this.addWindowListener(this);// for windowClosing
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		this.setContentPane(contentPane);		

		textArea = new JTextArea();
		textArea.setBackground(new Color(231, 231, 231) );
		textArea.setDragEnabled(true);
		
		JScrollPane scrollPane = new JScrollPane(textArea);
		//scrollPane.setPreferredSize(new Dimension(450, 500));

		contentPane.add(scrollPane);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		contentPane.add(buttonPanel);
		
		JButton saveButton = new JButton("save");
		saveButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		buttonPanel.add(saveButton);		

		JButton changePswButton = new JButton("change password");
		changePswButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		changePswButton.setActionCommand("changePsw");
		changePswButton.addActionListener(this);
		buttonPanel.add(changePswButton);
		
		if (PeaSettings.getPswGenerator() != null) {
			JButton pswGeneratorButton = new JButton("generate random password");
			pswGeneratorButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			pswGeneratorButton.setActionCommand("psw_generator");
			pswGeneratorButton.addActionListener(this);
			buttonPanel.add(pswGeneratorButton);
		}
		
		buttonPanel.add(Box.createHorizontalGlue());
		
		this.setLocation(100, 100);
		this.setMinimumSize(new Dimension(500, 500));
		pack();		
	}
	
	protected final static LockFrameNotes getInstance() {
		if (lockFrame == null) {
			lockFrame = new LockFrameNotes();
		} else {
			// return null
		}
		return lockFrame;
	}


	@Override
	public void actionPerformed(ActionEvent ape) {
		String com = ape.getActionCommand();
	
		if ( com.equals("save") ) {			
			
			byte[] plainBytes = getPlainBytes();
			saveContent(null, plainBytes, PswDialogBase.getEncryptedFileName() );

		} else if ( com.equals("changePsw") ) {	

			String[] fileName = { PswDialogBase.getEncryptedFileName() };
			 changePassword(getPlainBytes(), fileName );
		} else if(com.equals("psw_generator")){

			PeaSettings.getPswGenerator().setVisible(true);

		} else {
			System.err.println("ChangableLockFrame: invalid action: " + ape.getActionCommand());
		}
	}

	
	//-----------------------------------------------------------
	// Getter & Setter
	//
	protected void setChars( char[] input ) {
		textArea.setText( new String (input) );
	}

	@Override
	protected void windowClosingCommands() {

		textArea.setText(" ");				
		lockFrame.dispose();
		CipherStuff.getInstance().getSessionKeyCrypt().clearKeys();		
		System.exit(0);		
	}

	@Override
	protected byte[] getPlainBytes() {
		char[] plainChars = textArea.getText().toCharArray();
		byte[] plainBytes = Converter.chars2bytes(plainChars);
		Zeroizer.zero(plainChars);

		return plainBytes;
	}
}
