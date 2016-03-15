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
 * Dialog to create random passwords.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.crypto.RandomStuff;



@SuppressWarnings("serial")
public final class PasswordGeneratorDialog extends JDialog implements ActionListener {

	
//	protected static PasswordGeneratorDialog randomDialog;
	private JTextField randomField;
	
	public PasswordGeneratorDialog (Window owner) {		
		
		super(owner);

//		this.setDefaultCloseOperation(hideOnClose());
		
//		randomDialog = this;

		this.setTitle("create new random password");

		JPanel randomPane = (JPanel) this.getContentPane();
		randomPane.setBorder(new LineBorder(Color.GRAY,2));
		randomPane.setLayout(new BoxLayout(randomPane, BoxLayout.Y_AXIS));
	
		JPanel displayPanel = new JPanel();
		displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));
	
		randomField = new JTextField();
		randomField.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
		randomField.setPreferredSize(new Dimension(400, 30));
		displayPanel.add(randomField);
	
		JLabel randomLabel = new JLabel("select password, copy with CTRL + C and paste with CTRL + V");//Main.getI18n().getString("random_label"));
		randomLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		displayPanel.add(randomLabel);
		JButton randomButton = new JButton("new password");
		randomButton.setActionCommand("createRandomPassword");
		randomButton.addActionListener(this);
		displayPanel.add(randomButton);
	
		randomPane.add(displayPanel);
		if (owner != null) {
			this.setLocation(owner.getX() + 150, owner.getY() + 50);
		} else {
			this.setLocation(100,100);
		}
		this.pack();
		//randomDialog.setVisible(true);
	}
	
/*	protected static final PasswordGeneratorDialog getInstance(Window owner) {
		randomDialog = new PasswordGeneratorDialog(owner);
		return randomDialog;
	}*/
	
	@Override
	public void actionPerformed(ActionEvent ape) {
		if (ape.getActionCommand().equals("createRandomPassword")) {
			// creates a random password with security properties of at least a 256 bit key: 50 characters
			char[] rand = new RandomStuff().createNewRandomChars();
			randomField.setText(new String(rand));
		}
	}
}
