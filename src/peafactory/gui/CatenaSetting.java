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
 * Settings for the password hashing scheme Catena. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.KeyDerivation;
import cologne.eck.peafactory.crypto.kdf.CatenaKDF;


@SuppressWarnings("serial")
public class CatenaSetting extends JDialog implements ActionListener {
	
	private static String instance = "Dragonfly-Full";
	
	private static CatenaSetting setting;

	private JTextField memoryField;
	private JTextField timeField;
	private JLabel memoryRecommendedLabel;
	private JLabel timeRecommendedLabel;
	
	private JLabel errorLabel;
	
	CatenaSetting() {
		super(PeaFactory.getFrame() );
		setting = this;
		//setting.setUndecorated(true);
		setting.setAlwaysOnTop(true);		
		
		this.setIconImage(MainView.getImage());

		JPanel scryptPane = (JPanel) setting.getContentPane();//new JPanel();
		scryptPane.setBorder(new LineBorder(Color.GRAY,2));
		scryptPane.setLayout(new BoxLayout(scryptPane, BoxLayout.Y_AXIS));
		
		JLabel scryptLabel = new JLabel("Settings for CATENA:");
		scryptLabel.setPreferredSize(new Dimension(250, 30));
		
		JLabel tipLabel1 = new JLabel("Settings for this session only");
		tipLabel1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		tipLabel1.setPreferredSize(new Dimension(250, 20));
		scryptPane.add(scryptLabel);
		scryptPane.add(tipLabel1);

		
		JLabel instanceLabel = new JLabel("Select an instance of Catena:");
		instanceLabel.setPreferredSize(new Dimension(250, 40));
		scryptPane.add(instanceLabel);		
		
		JLabel instanceRecommendedLabel = new JLabel("recommended: Dragonfly-Full");
		instanceRecommendedLabel.setPreferredSize(new Dimension(250, 20));
		instanceRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		scryptPane.add(instanceRecommendedLabel);

	    
		JRadioButton dragonflyFullButton = new JRadioButton("Dragonfly-Full");
	    dragonflyFullButton.setMnemonic(KeyEvent.VK_U);
	    dragonflyFullButton.addActionListener(this);
	    dragonflyFullButton.setActionCommand("Dragonfly-Full");
	    scryptPane.add(dragonflyFullButton);
	    
		JRadioButton butterflyFullButton = new JRadioButton("Butterfly-Full");
	    butterflyFullButton.setMnemonic(KeyEvent.VK_U);
	    butterflyFullButton.addActionListener(this);
	    butterflyFullButton.setActionCommand("Butterfly-Full");
	    scryptPane.add(butterflyFullButton);
	    
		JRadioButton dragonflyButton = new JRadioButton("Dragonfly");
	    dragonflyButton.setMnemonic(KeyEvent.VK_B);
	    dragonflyButton.addActionListener(this);
	    dragonflyButton.setActionCommand("Dragonfly");
	    scryptPane.add(dragonflyButton);
		
		JRadioButton butterflyButton = new JRadioButton("Butterfly");
	    butterflyButton.setMnemonic(KeyEvent.VK_B);
	    butterflyButton.addActionListener(this);
	    butterflyButton.setActionCommand("Butterfly");
	    scryptPane.add(butterflyButton);
	    
	    //Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(dragonflyButton);
	    group.add(dragonflyFullButton);
	    group.add(butterflyButton);
	    group.add(butterflyFullButton);
	    
	    group.setSelected(dragonflyFullButton.getModel(), true);
	    
		scryptPane.add(Box.createVerticalStrut(20));

		JLabel memoryLabel = new JLabel("Memory cost parameter GARLIC: ");
		memoryLabel.setPreferredSize(new Dimension(220, 40));
		scryptPane.add(memoryLabel);			
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
				memoryField.setText("18");
				memoryField.setColumns(2);
				memoryField.setDragEnabled(true);
				
				memoryField.getDocument().addDocumentListener(new DocumentListener() {
					  public void changedUpdate(DocumentEvent e) {
						    warn();
						  }
						  public void removeUpdate(DocumentEvent e) {
						    warn();
						  }
						  public void insertUpdate(DocumentEvent e) {
						    warn();
						  }
						  public void warn() {
							  int garlic = 0;
							  try{
								  garlic = Integer.parseInt(memoryField.getText());
							  } catch (Exception nfe) {
								  errorLabel.setText("Invalid input");
							      return;
							   }
							  if (garlic == 0){
								  errorLabel.setText("Invalid value");
							  } else if (garlic < 14) {
								  errorLabel.setText("Warning: Weak parameter");
							  } else if (garlic > 22 && garlic < 64){
								  errorLabel.setText("Warning: Long execution time");
							  } else if (garlic > 63){
								  errorLabel.setText("Invalid value: must be < 64");
							  } else {
								  errorLabel.setText("");
							  }
						  }
						});

		memoryRecommendedLabel = new JLabel("recommended >= 18");
		memoryRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		JPanel memoryPanel = new JPanel();
		memoryPanel.add(memoryField);
		memoryPanel.add(memoryRecommendedLabel);
		scryptPane.add(memoryPanel);
		
		scryptPane.add(Box.createVerticalStrut(10));

		JLabel timeLabel = new JLabel("Time cost parameter LAMBDA: ");
		timeLabel.setPreferredSize(new Dimension(220, 40));
		scryptPane.add(timeLabel);			
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
				timeField.setText("2");
				timeField.setColumns(3);
				timeField.setDragEnabled(true);

				timeField.getDocument().addDocumentListener(new DocumentListener() {
					  public void changedUpdate(DocumentEvent e) {
						    warn();
						  }
						  public void removeUpdate(DocumentEvent e) {
						    warn();
						  }
						  public void insertUpdate(DocumentEvent e) {
						    warn();
						  }
						  public void warn() {
							  int lambda = 0;
							  try{
								  lambda = Integer.parseInt(timeField.getText());
							  } catch (Exception nfe) {
								  errorLabel.setText("Invalid input");
							      return;
							   }
							  if (lambda == 0){
								  errorLabel.setText("Invalid value");
							  } else if (lambda == 1) {
								  errorLabel.setText("Warning: Weak parameter");
							  } else if (lambda > 4){
								  errorLabel.setText("Warning: Long execution time");
							  } else {
								  errorLabel.setText("");
							  }
						  }
						});
				
		timeRecommendedLabel = new JLabel("recommended 2");
		timeRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		JPanel timePanel = new JPanel();
		timePanel.add(timeField);
		timePanel.add(timeRecommendedLabel);
		scryptPane.add(timePanel);		
		
		scryptPane.add(Box.createVerticalStrut(10));
		
		errorLabel = new JLabel("");
		errorLabel.setForeground(Color.RED);
		scryptPane.add(errorLabel);
		
		JButton scryptOkButton = new JButton("ok");
		scryptOkButton.setActionCommand("newSetting");
		scryptOkButton.addActionListener(this);
		scryptPane.add(scryptOkButton);
		setting.pack();
		setting.setLocation(100,100);
		setting.setVisible(true);
	}



	@Override
	public void actionPerformed(ActionEvent ape) {

		String command = ape.getActionCommand();
		
		if (command.equals("newSetting")) {
			// check parameters
			int garlic = Integer.parseInt(memoryField.getText());
			int lambda = Integer.parseInt(timeField.getText());
			if (garlic > 63) {
				errorLabel.setText("invalid memory cost parameter: must be < 64");
				pack();
				return;
			}
			if (garlic == 0 || lambda == 0) {
				errorLabel.setText("invalid cost parameters");
				pack();
				return;
			}
			if (instance.equals("Dragonfly-Full") || instance.equals("Dragonfly")){
				CatenaKDF.setVersionID(instance);
				CatenaKDF.setGarlicDragonfly(garlic);
				CatenaKDF.setLambdaDragonfly(lambda);
			} else if (instance.equals("Butterfly-Full") || instance.equals("Butterfly")){
				CatenaKDF.setVersionID(instance);
				CatenaKDF.setGarlicButterfly(garlic);
				CatenaKDF.setLambdaButterfly(lambda);
			}
			KeyDerivation.setKdf( new CatenaKDF() );
			
			setting.dispose();
			
		} else { // RadioButton for instance
			instance = ape.getActionCommand();
			int garlic = Integer.parseInt(memoryField.getText());
			int lambda = Integer.parseInt(timeField.getText());
			if (garlic > 22 || lambda > 4){
				errorLabel.setText("Warning: Long execution time");
			} else {
				errorLabel.setText("");
			}
			
			if (instance.equals("Dragonfly")){
				timeRecommendedLabel.setText("recommended 2"); // lambda: 2
				memoryRecommendedLabel.setText("recommended >= 18"); // garlic			
				timeField.setText("2");
				memoryField.setText("18");
			} else if (instance.equals("Dragonfly-Full")){
				timeRecommendedLabel.setText("recommended 2"); // lambda: 2
				memoryRecommendedLabel.setText("recommended >= 18"); // garlic		
				timeField.setText("2");
				memoryField.setText("18");
			} else if (instance.equals("Butterfly")){
				timeRecommendedLabel.setText("recommended 4"); // lambda: 4
				memoryRecommendedLabel.setText("recommended >= 14"); // garlic		
				timeField.setText("4");
				memoryField.setText("14");
			} else if (instance.equals("Butterfly-Full")){
				timeRecommendedLabel.setText("recommended 4"); // lambda: 4
				memoryRecommendedLabel.setText("recommended >= 14"); // garlic	
				timeField.setText("4");
				memoryField.setText("14");
			}
		}
	}
/*	public static void main(String[] args){
		CatenaSetting c = new CatenaSetting();
		c.setVisible(true);
	} */
}
