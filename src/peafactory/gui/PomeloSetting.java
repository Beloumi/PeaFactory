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
 * Settings for the password hashing scheme Pomelo. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.kdf.PomeloKDF;


@SuppressWarnings("serial")
public class PomeloSetting extends JDialog implements ActionListener {
	
	private static PomeloSetting pomeloSetting;

	private JTextField memoryField;
	private JTextField timeField;
	
	private JLabel errorLabel;
	
	PomeloSetting() {
		super(PeaFactory.getFrame() );
		pomeloSetting = this;
		//pomeloSetting.setUndecorated(true);
		pomeloSetting.setAlwaysOnTop(true);		
		
		this.setIconImage(MainView.getImage());

		JPanel pane = (JPanel) pomeloSetting.getContentPane();//new JPanel();
		pane.setBorder(new LineBorder(Color.GRAY,2));
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel("Settings for Pomelo:");
		label.setPreferredSize(new Dimension(280, 50));
		
		JLabel tipLabel1 = new JLabel("Settings for this session only");
		tipLabel1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		pane.add(label);
		pane.add(tipLabel1);

		pane.add(Box.createHorizontalStrut(20));

		JLabel recommendedLabel = new JLabel("Note: memory cost + time cost must be > 5 and < 25");// (r*2 MB) ");
		//recommendedLabel.setPreferredSize(new Dimension(260, 40));
		pane.add(recommendedLabel);

		pane.add(Box.createVerticalStrut(20));

		JLabel memoryLabel = new JLabel("Memory parameter: ");
		memoryLabel.setPreferredSize(new Dimension(220, 40));
		pane.add(memoryLabel);			
		memoryField = new JTextField() {
			private static final long serialVersionUID = 1L;
			public void processKeyEvent(KeyEvent ev) {
				    char c = ev.getKeyChar();
				    try {
				      // printable characters
				    	if (c > 31 && c < 65535 && c != 127) {
				        Integer.parseInt(c + "");// parse
				      }
				      super.processKeyEvent(ev);
				    }
				    catch (NumberFormatException nfe) {
				      // if not a number: ignore 
				    }
				  }
				};
		memoryField.setText("15");
		memoryField.setColumns(2);
		memoryField.setDragEnabled(true);

		JLabel memoryRecommendedLabel = new JLabel("recommended 15");
		memoryRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		JPanel parallelPanel = new JPanel();
		parallelPanel.add(memoryField);
		parallelPanel.add(memoryRecommendedLabel);
		pane.add(parallelPanel);		
		pane.add(Box.createVerticalStrut(20));
		
		JLabel timeLabel = new JLabel("Time parameter: ");
		timeLabel.setPreferredSize(new Dimension(220, 40));
		pane.add(timeLabel);			
		timeField = new JTextField() {
			private static final long serialVersionUID = 1L;
			public void processKeyEvent(KeyEvent ev) {
				    char c = ev.getKeyChar();
				    try {
				      // printable characters
				    	if (c > 31 && c < 65535 && c != 127) {
				        Integer.parseInt(c + "");// parse
				      }
				      super.processKeyEvent(ev);
				    }
				    catch (NumberFormatException nfe) {
				      // if not a number: ignore 
				    }
				  }
				};
		timeField.setText("0");
		timeField.setColumns(2);
		timeField.setDragEnabled(true);

		JLabel timeRecommendedLabel = new JLabel("recommended 0");
		timeRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		JPanel timePanel = new JPanel();
		timePanel.add(timeField);
		timePanel.add(timeRecommendedLabel);
		pane.add(timePanel);		
		pane.add(Box.createVerticalStrut(20));

		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);
		pane.add(errorLabel);
				
		JButton okButton = new JButton("ok");
		okButton.setActionCommand("newSetting");
		okButton.addActionListener(this);
		pane.add(okButton);
		pomeloSetting.pack();
		pomeloSetting.setLocation(100,100);
		pomeloSetting.setVisible(true);
	}
	


	@Override
	public void actionPerformed(ActionEvent ape) {

		String command = ape.getActionCommand();
		
		if (command.equals("newSetting")) {

			int m_cost = Integer.parseInt(memoryField.getText());
			int t_cost = Integer.parseInt(timeField.getText());
			
			if (t_cost < 0 || t_cost > 25) {
				errorLabel.setText("Invalid time cost parameter");
				return;
			}
			if (m_cost < 0 || m_cost > 25) {
				errorLabel.setText("Invalid memory cost parameter");
				return;
			}			
			if ( ((t_cost + m_cost) < 5 )|| (t_cost + m_cost > 25)){
				errorLabel.setText("Invalid parameters: memory cost + time cost must be > 5 and < 25");
				pack();
				return;
			}
			
			
			PomeloKDF.setMemoryCost( m_cost);
			PomeloKDF.setTimeCost( t_cost );

			KeyDerivation.setKdf(new PomeloKDF() );
			pomeloSetting.dispose();
		}
	}
/*	public static void main(String[] args){
		PomeloSetting p = new PomeloSetting();
		p.setVisible(true);
	} */
}
