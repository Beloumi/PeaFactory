package cologne.eck.peafactory.gui;

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
 * Settings for Peas
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;



@SuppressWarnings("serial")
public class GeneralPeaSettings extends JDialog implements ActionListener {	
	
	private GeneralPeaSettings peaSettings;
		
	private JCheckBox boundCheck;
	
	private JCheckBox blankCheck;
	
	protected ResourceBundle languageBundle;
	
	public GeneralPeaSettings() {
		
		super(PeaFactory.getFrame() );
		
		peaSettings = this;
		
		peaSettings.setAlwaysOnTop(true);		

		// i18n
		languageBundle = PeaFactory.getLanguagesBundle();
		
		this.setIconImage(MainView.getImage());
		
		this.setTitle(languageBundle.getString("general_pea_settings"));

		JPanel contentPane = (JPanel) peaSettings.getContentPane();//new JPanel();
		contentPane.setBorder(new LineBorder(Color.GRAY,2));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		contentPane.add(Box.createVerticalStrut(10));
		
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
		JTextArea infoArea = new JTextArea(languageBundle.getString("general_pea_infos"));
		infoArea.setWrapStyleWord(true);
		infoArea.setLineWrap(true);
		infoArea.setEditable(false);
		infoPanel.add(Box.createHorizontalStrut(10));
		infoPanel.add(infoArea);
		infoPanel.add(Box.createHorizontalGlue());

		contentPane.add(infoPanel);
		
		contentPane.add(Box.createVerticalStrut(10));		
		
		JPanel tipPanel = new JPanel();
		tipPanel.setLayout(new BoxLayout(tipPanel, BoxLayout.X_AXIS));
		JLabel tipLabel1 = new JLabel(languageBundle.getString("this_session"));
		tipLabel1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		tipPanel.add(tipLabel1);
		tipPanel.add(Box.createHorizontalGlue());
		
		contentPane.add(tipPanel);		
		
		contentPane.add(Box.createVerticalStrut(20));
		
		JPanel boundPanel = new JPanel();
		
		boundPanel.setLayout(new BoxLayout(boundPanel, BoxLayout.X_AXIS));
		boundCheck = new JCheckBox(languageBundle.getString("bound_check"), false);
		boundCheck.setSelected(true);
		boundCheck.addActionListener(this);
		boundCheck.setActionCommand("boundCheck");

		boundPanel.add(boundCheck);
		boundPanel.add(Box.createHorizontalGlue());
		
		contentPane.add(boundPanel);		
		
		JPanel infoBoundPanel = new JPanel();
		infoBoundPanel.setLayout(new BoxLayout(infoBoundPanel, BoxLayout.X_AXIS));
		JTextArea infoBoundArea = new JTextArea(languageBundle.getString("bound_pea_infos"));
		infoBoundArea.setWrapStyleWord(true);
		infoBoundArea.setLineWrap(true);
		infoBoundArea.setEditable(false);
		
		infoBoundPanel.add(Box.createHorizontalStrut(20));
		infoBoundPanel.add(infoBoundArea);
		infoBoundPanel.add(Box.createHorizontalGlue());
		
		contentPane.add(infoBoundPanel);

		contentPane.add(Box.createVerticalStrut(10));
		
		JPanel blankPanel = new JPanel();
		blankPanel.setLayout(new BoxLayout(blankPanel, BoxLayout.X_AXIS));
		blankCheck = new JCheckBox(languageBundle.getString("blank_check"), false);
		blankCheck.setSelected(false);
		blankCheck.addActionListener(this);
		blankCheck.setActionCommand("blankCheck");
		
		blankPanel.add(blankCheck);
		blankPanel.add(Box.createHorizontalGlue());
		contentPane.add(blankPanel);
		
		JPanel infoBlankPanel = new JPanel();
		infoBlankPanel.setLayout(new BoxLayout(infoBlankPanel, BoxLayout.X_AXIS));
		JTextArea infoBlankArea = new JTextArea(languageBundle.getString("blank_pea_infos"));
		infoBlankArea.setWrapStyleWord(true);
		infoBlankArea.setLineWrap(true);
		infoBlankArea.setEditable(false);
		
		infoBlankPanel.add(Box.createHorizontalStrut(20));
		infoBlankPanel.add(infoBlankArea);
		infoBlankPanel.add(Box.createHorizontalGlue());

		contentPane.add(infoBlankPanel);
		
		contentPane.add(Box.createVerticalStrut(10));
				
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		JButton okButton = new JButton("ok");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okButton);
		
		contentPane.add(buttonPanel);
		
		peaSettings.setSize(new Dimension(550,350));
		peaSettings.setLocation(PeaFactory.getFrame().getLocation());
		
		//peaSettings.pack();
		peaSettings.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		String com = ape.getActionCommand();
		
		if (com.equals("ok")) {
			
			boolean oldBlankPea = MainView.isBlankPea();

			if (blankCheck.isSelected()) {
				
				MainView.setBlankPea(true);				
				
				if (oldBlankPea == false) {
					//update the frame: delete password fields and content fields
					MainView.updateFrame();
				}
			} else {
				
				MainView.setBlankPea(false);

				if (oldBlankPea == true) {
					//update the frame: delete password fields and content fields
					MainView.updateFrame();
				}
			}			

			if (boundCheck.isSelected() ) {
				
				CipherStuff.setBound(true);
				
			} else {
				
				CipherStuff.setBound(false);
				
			}

			peaSettings.dispose();
			 
		} else if ( com.startsWith("blankCheck")) {	
			
			// set boundCheck
			if (blankCheck.isSelected()) {
				boundCheck.setSelected(false);
			} 			
			
		} else if ( com.startsWith("boundCheck")) {	
			
			// set blankCheck
			if (boundCheck.isSelected()) {
				blankCheck.setSelected(false);
			} 
		}
	}
}
