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
 * Settings for the password hashing scheme Scrypt. 
 * Changing the parameters should result in displaying the required
 * memory. 
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
//import cologne.eck.peafactory.crypto.KDFScheme;
import cologne.eck.peafactory.crypto.kdf.ScryptKDF;


@SuppressWarnings("serial")
public class ScryptSetting extends JDialog implements ActionListener {
	
	private static ScryptSetting scryptSetting;
	@SuppressWarnings("rawtypes")
	private JComboBox scryptMemoryList;
	private JLabel memoryResultLabel;
	private JLabel scryptIterationLabel;
	private JTextField parallelizationField;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	ScryptSetting() {
		super(PeaFactory.getFrame() );
		scryptSetting = this;
		//scryptSetting.setUndecorated(true);
		scryptSetting.setAlwaysOnTop(true);		
		
		this.setIconImage(MainView.getImage());

		JPanel scryptPane = (JPanel) scryptSetting.getContentPane();//new JPanel();
		scryptPane.setBorder(new LineBorder(Color.GRAY,2));
		scryptPane.setLayout(new BoxLayout(scryptPane, BoxLayout.Y_AXIS));
		
		JLabel scryptLabel = new JLabel("Settings for SCRYPT:");
		scryptLabel.setPreferredSize(new Dimension(280, 50));
		
		JLabel tipLabel1 = new JLabel("Settings for this session only");
		tipLabel1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		scryptPane.add(scryptLabel);
		scryptPane.add(tipLabel1);

		scryptPane.add(Box.createHorizontalStrut(20));

		JLabel memoryLabel = new JLabel("Memory cost parameter r");// (r*2 MB) ");
		memoryLabel.setPreferredSize(new Dimension(260, 40));
		scryptPane.add(memoryLabel);
		
		JPanel memoryPanel = new JPanel();

		String[] memoryValues = { "2", "4", "8", "16", "32", "64" , "128", "256", "512", "1024" , "2048"};
		scryptMemoryList = new JComboBox(memoryValues);
		scryptMemoryList.setSelectedIndex(2);
		scryptMemoryList.addActionListener(this);
		scryptMemoryList.setActionCommand("scryptMemory");
		scryptMemoryList.setSize(120, 30);
		memoryPanel.add(scryptMemoryList);
		
		memoryResultLabel = new JLabel();
		memoryResultLabel.setText(" = " + 16 + "MiB");
		memoryPanel.add(memoryResultLabel);

		scryptPane.add(memoryPanel);
		
		JLabel memoryRecommendedLabel = new JLabel("(recommended at least 8) ");
		memoryRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		
		JLabel memoryTipLabel = new JLabel(" MiB > 64 may not be executable on all plattforms");
		memoryTipLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		scryptPane.add(memoryRecommendedLabel);
		scryptPane.add(memoryTipLabel);
		scryptPane.add(Box.createVerticalStrut(20));


		JLabel iterationLabel = new JLabel("CPU cost parameter N (iterations 2^14/2^20)");
		iterationLabel.setPreferredSize(new Dimension(380, 40));
		scryptPane.add(iterationLabel);		
		
		JPanel iterationPanel = new JPanel();

		scryptIterationLabel = new JLabel();
		scryptIterationLabel.setBorder(new LineBorder(Color.BLACK));
		scryptIterationLabel.setPreferredSize(new Dimension(100, 30));
		scryptIterationLabel.setText("16384"); // default value
		
		JButton increaseButton = new JButton ("increase");
		increaseButton.setPreferredSize(new Dimension(100, 20));
		increaseButton.addActionListener(this);
		increaseButton.setActionCommand("increaseIterations");
		
		JButton reduceButton = new JButton ("reduce");
		reduceButton.setPreferredSize(new Dimension(100, 20));
		reduceButton.addActionListener(this);
		reduceButton.setActionCommand("reduceIterations");
		
		iterationPanel.add(scryptIterationLabel);
		iterationPanel.add(increaseButton);
		iterationPanel.add(reduceButton);

		scryptPane.add(iterationPanel);		
		
		JLabel iterationRecommendedLabel = new JLabel("recommended 16384 (2^14 - normal) or 1048576 (2^20 - high)");
		iterationRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));

		scryptPane.add(iterationRecommendedLabel);
		scryptPane.add(Box.createVerticalStrut(20));

		JLabel parallelizationLabel = new JLabel("Parallelization parameter p: ");
		parallelizationLabel.setPreferredSize(new Dimension(220, 40));
		scryptPane.add(parallelizationLabel);			
		parallelizationField = new JTextField() {
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
		parallelizationField.setText("1");
		parallelizationField.setColumns(3);
		parallelizationField.setDragEnabled(true);

		JLabel parallelizationRecommendedLabel = new JLabel("recommended 1");
		parallelizationRecommendedLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		JPanel parallelPanel = new JPanel();
		parallelPanel.add(parallelizationField);
		parallelPanel.add(parallelizationRecommendedLabel);
		scryptPane.add(parallelPanel);		
		scryptPane.add(Box.createVerticalStrut(20));
				
		JButton scryptOkButton = new JButton("ok");
		scryptOkButton.setActionCommand("newScryptSetting");
		scryptOkButton.addActionListener(this);
		scryptPane.add(scryptOkButton);
		scryptSetting.pack();
		scryptSetting.setLocation(100,100);
		scryptSetting.setVisible(true);
	}
	
	private void setScryptMemory() {
		
		long memoryParameter = Long.parseLong((String) scryptMemoryList.getSelectedItem() );

		long iterations;

		try{
			iterations = Long.parseLong(scryptIterationLabel.getText());
			if (iterations < 1) {
				memoryResultLabel.setText("ERROR");
				return;	
			}
			if (iterations > (Long.MAX_VALUE / 128 / memoryParameter)) {
				memoryResultLabel.setText("ERROR");
				return;
			}
			if (iterations > Integer.MAX_VALUE) { // scrypt accepts only Integer
				memoryResultLabel.setText("ERROR");
				return;
			}
		} catch (NumberFormatException nfe) {
				memoryResultLabel.setText("ERROR");
				return;
		}


		long memoryResult = memoryParameter * 128 * iterations / 1024 / 1024;
		memoryResultLabel.setText(" = " + memoryResult + "MiB");
	}

	@Override
	public void actionPerformed(ActionEvent ape) {

		String command = ape.getActionCommand();
		
		if (command.equals("scryptMemory")) {
			setScryptMemory();
			
		} else if (command.equals("increaseIterations")){
			long iterations = 0;
			try{
				iterations = Long.parseLong(scryptIterationLabel.getText());
				if (iterations < 0) {
					memoryResultLabel.setText("ERROR");
					return;	
				}
				if (iterations > Integer.MAX_VALUE) { // scrypt accepts only Integer
					memoryResultLabel.setText("ERROR");
					return;
				}
			} catch (NumberFormatException nfe) {
					memoryResultLabel.setText("ERROR");
					return;
			}

			long newIterations = iterations * 2;
			scryptIterationLabel.setText(newIterations + "");
			setScryptMemory();
			
		} else if (command.equals("reduceIterations")){
			long iterations = 0;
			try{
				iterations = Long.parseLong(scryptIterationLabel.getText());
				if (iterations < 1) {
					memoryResultLabel.setText("ERROR");
					return;	
				}
			} catch (NumberFormatException nfe) {
					memoryResultLabel.setText("ERROR");
					return;
			}

			long newIterations = iterations / 2;
			scryptIterationLabel.setText(newIterations + "");
			setScryptMemory();
			
		} else if (command.equals("newScryptSetting")) {
			//System.out.println("ok");
			int memoryFactor = Integer.parseInt((String)scryptMemoryList.getSelectedItem());
			//KDFScheme.setScryptMemoryFactor(memoryFactor );
			//KDFScheme.setScryptCPUFactor( Integer.parseInt(scryptIterationLabel.getText()) );
			
			ScryptKDF.setMemoryFactor(memoryFactor );

			ScryptKDF.setcPUFactor( Integer.parseInt(scryptIterationLabel.getText()) );


			int parallelFactor = 0;
			try {
				parallelFactor = Integer.parseInt(parallelizationField.getText() );		

			} catch (NumberFormatException nfe) {
				return;
			}
			if (Integer.parseInt(parallelizationField.getText()) <= 0) {
				return;
			}
			if (parallelFactor > 4) {
	    		int n = JOptionPane.showConfirmDialog(scryptSetting,
		    			"Parallelfactor > 4 will cause very long execution times", 
		    			"Warning", 
		    			JOptionPane.OK_CANCEL_OPTION,
		    			JOptionPane.WARNING_MESSAGE);
		    		if (n != 0) return; // cancel, close
			}
			//KDFScheme.setScryptParallelFactor( parallelFactor );
			ScryptKDF.setParallelFactor( parallelFactor );
			scryptSetting.dispose();
		}
	}
}
