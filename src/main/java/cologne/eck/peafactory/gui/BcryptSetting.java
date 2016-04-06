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
 * Dialog to change parameters of the Bcrypt password hashing scheme.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
//import cologne.eck.peafactory.crypto.KDFScheme;
import cologne.eck.peafactory.crypto.kdf.BcryptKDF;


@SuppressWarnings("serial")
public class BcryptSetting extends JDialog implements ActionListener {
	
	private static BcryptSetting bcryptSetting;

	@SuppressWarnings("rawtypes")
	private JComboBox bcryptList;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	BcryptSetting() {
		
		super(PeaFactory.getFrame() );
		
		bcryptSetting = this;
		bcryptSetting.setAlwaysOnTop(true);		

		this.setIconImage(MainView.getImage());

		JPanel bcryptPane = (JPanel) bcryptSetting.getContentPane();
		bcryptPane.setBorder(new LineBorder(Color.GRAY,2));
		bcryptPane.setLayout(new BoxLayout(bcryptPane, BoxLayout.Y_AXIS));
		
		JPanel labelPanel = new JPanel();
		JLabel teapotLabel = new JLabel("Settings for BCRYPT:");
		teapotLabel.setPreferredSize(new Dimension(280, 50));
		JLabel tipLabel1 = new JLabel("Settings for this session only");
		tipLabel1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		labelPanel.add(teapotLabel);
		labelPanel.add(tipLabel1);
		bcryptPane.add(labelPanel);
		
		JPanel iterationPanel = new JPanel();
		JLabel iterationLabel = new JLabel("iterations (as a power of two)");
		iterationLabel.setPreferredSize(new Dimension(230, 40));
		iterationPanel.add(iterationLabel);
		
		String[] bcryptValues = { "4", "5", "6", "7", "8", "9", "10" , "11", "12", "13", "14" , "15", "16", "17", "18", "19", "20", "21", "22" , "23", "24", "25", "26" , "27", "28", "29", "30", "31"};
		bcryptList = new JComboBox(bcryptValues);
		bcryptList.setSelectedIndex(8);
		iterationPanel.add(bcryptList);
		JLabel bcryptRecommendedLabel = new JLabel("(recommended at least 10)");
		bcryptRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		JLabel bcryptTipLabel = new JLabel("Values > 16 may cause execution times more than 10s");
		bcryptTipLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		iterationPanel.add(bcryptRecommendedLabel);
		iterationPanel.add(bcryptTipLabel);
		bcryptPane.add(iterationPanel);
				
		JButton bcryptOkButton = new JButton("ok");
		bcryptOkButton.setActionCommand("newBcryptSetting");
		bcryptOkButton.addActionListener(this);
		bcryptPane.add(bcryptOkButton);
		bcryptSetting.setSize(new Dimension(350,220));
		bcryptSetting.setLocation(100,100);
		bcryptSetting.setVisible(true);	}
	


	@Override
	public void actionPerformed(ActionEvent ape) {

		//KDFScheme.setBcryptRounds( Integer.parseInt((String)bcryptList.getSelectedItem()) );
		BcryptKDF.setRounds( Integer.parseInt((String)bcryptList.getSelectedItem()) );
		bcryptSetting.dispose();	}
}
