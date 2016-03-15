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
 * Dialog to change the password of the pea. 
 */

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.tools.KeyRandomCollector;
import cologne.eck.peafactory.tools.MouseRandomCollector;
import cologne.eck.peafactory.tools.Zeroizer;


public class NewPasswordDialog extends JDialog implements ActionListener {
	

	private static final long serialVersionUID = 1L;

	
	private static NewPasswordDialog newPswDialog = null;
	private static JPasswordField newPasswordField;
	private static JPasswordField retypePasswordField;
	
	private static char[] returnPsw;
	
	private static boolean randomCollector = false;

	private NewPasswordDialog(Window owner) {

		super(owner);
				
		// show passwordField
		newPswDialog = this;
		newPswDialog.setAlwaysOnTop(true);
		newPswDialog.setModal(true);
		newPswDialog.setIconImage(PswDialogView.getImage() );
		
		newPswDialog.setLayout(new BoxLayout(newPswDialog.getContentPane(), BoxLayout.Y_AXIS));

		if(randomCollector == true){
			newPswDialog.addMouseMotionListener(new MouseRandomCollector() );
		}
		
		JPanel pswLabelPanel = new JPanel();
		pswLabelPanel.setLayout(new BoxLayout(pswLabelPanel, BoxLayout.X_AXIS));
		
		JLabel pswLabel = new JLabel("enter new password");
		pswLabel.setPreferredSize(new Dimension(300,30));
		pswLabelPanel.add(pswLabel);		
		pswLabelPanel.add(Box.createHorizontalGlue());
		
		JButton charTableButton = new JButton(PswDialogBase.getBundle().getString("char_table"));
		charTableButton.addActionListener(this);
		charTableButton.setActionCommand("charTable1");
		if(randomCollector == true){
			charTableButton.addMouseMotionListener(new MouseRandomCollector() );
		}
		pswLabelPanel.add(charTableButton);
		
		newPswDialog.add(pswLabelPanel);
			
		newPasswordField = new JPasswordField(50);
		newPasswordField.setActionCommand("newPsw");// Enter
		newPasswordField.addActionListener(this);
		if(randomCollector == true){
			newPasswordField.addKeyListener(new KeyRandomCollector() );
		}
		newPswDialog.add(newPasswordField);
		
		newPswDialog.add(Box.createVerticalStrut(10));
		
		JPanel pswLabelPanel2 = new JPanel();
		pswLabelPanel2.setLayout(new BoxLayout(pswLabelPanel2, BoxLayout.X_AXIS));
		
		JLabel pswLabel2 = new JLabel("retype password");
		pswLabel2.setPreferredSize(new Dimension(300,30));
		pswLabelPanel2.add(pswLabel2);		
		pswLabelPanel2.add(Box.createHorizontalGlue());
		
		JButton charTableButton2 = new JButton(PswDialogBase.getBundle().getString("char_table"));
		charTableButton2.addActionListener(this);
		charTableButton2.setActionCommand("charTable2");
		if(randomCollector == true){
			charTableButton2.addMouseMotionListener(new MouseRandomCollector() );
		}
		pswLabelPanel2.add(charTableButton2);
		
		newPswDialog.add(pswLabelPanel2);

		retypePasswordField = new JPasswordField(50);
		retypePasswordField.setActionCommand("retypePsw");// Enter
		retypePasswordField.addActionListener(this);
		if(randomCollector == true){
			retypePasswordField.addKeyListener(new KeyRandomCollector() );
		}
		newPswDialog.add(retypePasswordField);
		
		JPanel okPanel = new JPanel();
		if(randomCollector == true){
			okPanel.addMouseMotionListener(new MouseRandomCollector() );
		}
		okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.X_AXIS));
		JButton newPswButton = new JButton("ok");
		if(randomCollector == true){
			newPswButton.addMouseMotionListener(new MouseRandomCollector() );
		}
		newPswButton.addActionListener(this);
		newPswButton.setActionCommand("newPsw");// ok-Button
		okPanel.add(Box.createHorizontalGlue());
		okPanel.add(newPswButton);
		
		
		newPswDialog.add(okPanel);
		newPswDialog.setSize( 400, 170);
		newPswDialog.setLocation(100,100);
		newPswDialog.setVisible(true);
		
	}
	
	public final static NewPasswordDialog getInstance(Window owner) {
		if (newPswDialog == null) {
			newPswDialog =  new NewPasswordDialog(owner);
		} else {
			//
		}
		return newPswDialog;
	}

	
	public final char[] getDialogInput() {
		newPswDialog = null;
		return returnPsw;			
	}
	//
	// derive keys from password and set new keys in CryptStuff
	//
	@Override
	public void actionPerformed(ActionEvent ape) {
		
		String com = ape.getActionCommand();
		
		if ( com.startsWith("charTable")) {	
			if(com.equals("charTable1")){
				CharTable table = new CharTable(this, newPasswordField);
				table.setVisible(true);
			} else {
				CharTable table = new CharTable(this, retypePasswordField);
				table.setVisible(true);				
			}
			
		} else {//if (com.equals("newPsw")) {
			
			// get password 
			char[] newPsw = newPasswordField.getPassword();	
			char[] retypePsw = retypePasswordField.getPassword();
			
			if (retypePsw.length == 0 && newPsw.length != 0) {
				JOptionPane.showMessageDialog(newPswDialog,
						"You must retype the password.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (newPsw.length != retypePsw.length) {
				JOptionPane.showMessageDialog(newPswDialog,
						"Password are not equal. Type again",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				newPasswordField.setText("");
				retypePasswordField.setText("");
				return;				
			}
			boolean pswEqual = true;
			for (int i = 0; i < newPsw.length; i++) {
				if (newPsw[i] != retypePsw[i]) {
					pswEqual = false;
				}
			}
			if (pswEqual == false) {
				JOptionPane.showMessageDialog(newPswDialog,
						"Password are not equal. Type again",
						"Error",
						JOptionPane.ERROR_MESSAGE);
				newPasswordField.setText("");
				retypePasswordField.setText("");
				return;				
			}
			
			if (newPsw.length == 0){
				// allow null-passwords with warning
				JOptionPane.showMessageDialog(newPswDialog,
					"no passord - program continues with null password.",
					"Warning",
					JOptionPane.WARNING_MESSAGE);
				newPsw = "no password".toCharArray();
			}
			returnPsw = newPsw;
			if (retypePsw.length > 0){
				Zeroizer.zero(retypePsw);
			}
			newPasswordField.setText("");		

			this.dispose();
		} 
	}

	/**
	 * @return the randomCollector
	 */
	public static boolean isRandomCollector() {
		return randomCollector;
	}

	/**
	 * @param randomCollector the randomCollector to set
	 */
	public static void setRandomCollector(boolean _randomCollector) {
		NewPasswordDialog.randomCollector = _randomCollector;
	}
}
