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
 * Settings for ImageType: 
 *    - WIDTH and HEIGHT of displayed image in LockFrameImage
 *    - RESIZE (keep ratio of width and height) or not. 
 *    
 * Values are set in peagen.ImageType, 
 *   appended as bytes[] to the plain image, 
 *   read as settings in PswDialogImage
 *   and determine the image displayed in peas.LockFrameImage
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.peas.image_pea.ImageType;


@SuppressWarnings("serial")
public class ImageSetting extends JDialog implements ActionListener {	
	
	private ImageSetting imageSetting;
	private JTextField imageWidthField;
	private JTextField imageHeightField;
	private JCheckBox resizeCheck;
	
	public ImageSetting() {
		super(PeaFactory.getFrame() );
		imageSetting = this;
		//imageSetting.setUndecorated(true);
		imageSetting.setAlwaysOnTop(true);
		
		this.setIconImage(MainView.getImage());

		JPanel imagePanel = (JPanel) imageSetting.getContentPane();//new JPanel();
		imagePanel.setBorder(new LineBorder(Color.GRAY,2));
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
		
		JPanel labelPanel = new JPanel();
		JLabel imageLabel = new JLabel("Settings for images:");
		imageLabel.setPreferredSize(new Dimension(200, 50));
		JLabel tipLabel1 = new JLabel("Settings for this session only");
		tipLabel1.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		labelPanel.add(imageLabel);
		labelPanel.add(tipLabel1);
		imagePanel.add(labelPanel);
		
		JPanel screenPanel = new JPanel();
		JLabel screenLabel = new JLabel("values of this screen: width: " + Toolkit.getDefaultToolkit().getScreenSize().getWidth() + "  height: " + Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		screenLabel.setPreferredSize(new Dimension(400, 40));
		screenPanel.add(screenLabel);
		imagePanel.add(screenPanel);
					
		JPanel widthPanel = new JPanel();
		JLabel widthLabel = new JLabel("width of image: ");
		widthPanel.add(widthLabel);
		imageWidthField = new JTextField() {
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
		imageWidthField.setText("800");
		imageWidthField.setColumns(5);
		imageWidthField.setDragEnabled(true);
		widthPanel.add(imageWidthField);

		imagePanel.add(widthPanel);
		
		JPanel heightPanel = new JPanel();
		JLabel heightLabel = new JLabel("height of image: ");
		heightPanel.add(heightLabel);
		imageHeightField = new JTextField() {
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
		imageHeightField.setText("600");
		imageHeightField.setColumns(5);
		imageHeightField.setDragEnabled(true);
		heightPanel.add(imageHeightField);

		imagePanel.add(heightPanel);
		
		JPanel checkPanel = new JPanel();
		resizeCheck = new JCheckBox("resize image (keep the ratio of width and height)",true);
		checkPanel.add(resizeCheck);
		imagePanel.add(checkPanel);
				
		JButton imageOkButton = new JButton("ok");
		imageOkButton.setActionCommand("newImageSetting");
		imageOkButton.addActionListener(this);
		imagePanel.add(imageOkButton);
		imageSetting.setSize(new Dimension(500,300));
		imageSetting.setLocation(100, 100);
		imageSetting.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		if (ape.getActionCommand().equals("newImageSetting")) {
			ImageType.setImageWidth( Integer.parseInt(imageWidthField.getText()) );
			ImageType.setImageHeight( Integer.parseInt(imageHeightField.getText()) );
			 if (resizeCheck.isSelected() == true) {
				 ImageType.setResize(true);
			 } else {
				 ImageType.setResize(false);
			 }
			 imageSetting.dispose();
		}
	}
}
