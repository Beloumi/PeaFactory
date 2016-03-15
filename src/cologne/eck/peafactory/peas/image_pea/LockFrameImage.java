package cologne.eck.peafactory.peas.image_pea;

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
 * Main view of image pea. 
 */

/*
 * Frame to display images, decrypted in PswDialogImage
 * (this is not a daughter class of LockFrame!)
 * 
 */

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import settings.PeaSettings;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.LockFrame;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.WriteResources;
import cologne.eck.peafactory.tools.Zeroizer;

@SuppressWarnings("serial")
final class LockFrameImage extends LockFrame implements ActionListener {

	private static byte[] imageBytes;
	
	private static boolean isInstantiated = false;

	private LockFrameImage() {
		
	    this.setIconImage(PswDialogView.getImage());
		
		this.addWindowListener(this);// for windowClosing
		
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		this.setContentPane(contentPane);
		
		JScrollPane scrollPane = null;
		
		// get the image from file name:
		BufferedImage image = null;			
		try {
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
			//Arrays.fill(imageBytes, (byte) 0);
		} catch (IOException e) {
			System.err.println("LockFrameImage: " + e);
			e.printStackTrace();
		}
		
		if (image ==  null) { // invalid file
			JOptionPane.showMessageDialog(this, 
					   "Invalid or unsuitable image file: \n"                      
					      + PswDialogImage.getEncryptedFileName(), 
					   "Error",                                
					   JOptionPane.ERROR_MESSAGE);   
			System.exit(1);
		}
		// get settings:
		int picWidth = PswDialogImage.imageWidth;
		int picHeight = PswDialogImage.imageHeight;		

		int w = picWidth,h = picHeight;	
		JLabel picLabel = null;
		 
		// keep ratio of image width and height if resize == true: 
		if (PswDialogImage.resize == true) {	
			
			 double op = 0;		 
			 
			 if (image.getWidth() < picWidth) {
				 op = (double)picWidth / image.getWidth();
				 if (image.getHeight() * op > picHeight) op = (double)picHeight / image.getHeight();
				 w = (int) (image.getWidth() * op);
				 h = (int) (image.getHeight() * op);
			 } else {
				 op = image.getWidth() / (double)picWidth;
				 if ( (image.getHeight() / op) > picHeight) op = image.getHeight() / (double)picHeight;
				 w = (int) (image.getWidth() / op);
				 h = (int) (image.getHeight() / op);
			 }		 

			Image scaledImage = image.getScaledInstance(w, h, Image.SCALE_FAST);
			picLabel = new JLabel(new ImageIcon(scaledImage));
			scrollPane = new JScrollPane(picLabel);
		} else {
			picLabel = new JLabel(new ImageIcon(image));
			scrollPane = new JScrollPane(picLabel);
			scrollPane.setPreferredSize(new Dimension(PswDialogImage.imageWidth, PswDialogImage.imageHeight));
		}

		contentPane.add(scrollPane);
		
		if (PeaSettings.getExternFile() == true) {
		
			JPanel buttonPanel = new JPanel();
			JButton unencryptedButton = new JButton ("close unencrypted");
			unencryptedButton.setActionCommand("unencrypted");
			unencryptedButton.addActionListener(this);
			buttonPanel.add(unencryptedButton);
			
			JButton encryptedButton = new JButton ("close encrypted");
			encryptedButton.setActionCommand("encrypted");
			encryptedButton.addActionListener(this);
			buttonPanel.add(encryptedButton);
			
			JButton passwordButton = new JButton ("change password");
			passwordButton.setActionCommand("changePassword");
			passwordButton.addActionListener(this);
			buttonPanel.add(passwordButton);
			
			contentPane.add(buttonPanel);
		}

		this.setLocation(100, 100);

		pack();
	}
	
	protected final static LockFrameImage getInstance() {
		LockFrameImage frame = null;
		if (isInstantiated == false) {
			frame = new LockFrameImage();
		} else {
			//return null
		}		
		return frame;
	}


	protected final static void setImageBytes( byte[] input ) {
		//imageBytes = input;
		imageBytes = new byte[input.length];
		System.arraycopy(input,  0,  imageBytes,  0,  input.length);
	}

	@Override
	public void actionPerformed(ActionEvent ape) {

		String command = ape.getActionCommand();

		if (command.equals("unencrypted")) {
			WriteResources.write(imageBytes, PeaSettings.getExternalFilePath(), null );
			Zeroizer.zero(imageBytes);
			System.exit(0);
			
		} else if (command.equals("encrypted")){
			// file was not modified, just loaded unencrypted in RAM
			Zeroizer.zero(imageBytes);
			System.exit(0);
		} else if (command.equals("changePassword")){
			String[] fileNames = {PswDialogBase.getEncryptedFileName()};
			changePassword(imageBytes, fileNames);
		}
	}

	@Override
	protected void windowClosingCommands() {

		// append settings for this image and then encrypt and store	
		if (PswDialogView.isInitializing() == true) {
			
			// get setting values
			int resizeValue = 0;
			if (PswDialogImage.resize == true) resizeValue = 1;
			int[] settings = new int[3];
			settings[0] = PswDialogImage.imageWidth;
			settings[1] = PswDialogImage.imageHeight;
			settings[2] = resizeValue;
			byte[] settingBytes = Converter.ints2bytesBE(settings);;
			
			// append setting values to plaintext
			byte[] plainBytes = new byte[imageBytes.length + settingBytes.length];
			System.arraycopy(imageBytes, 0, plainBytes, 0, imageBytes.length);		
			System.arraycopy(settingBytes, 0, plainBytes, imageBytes.length, settingBytes.length);

			// encrypt
			byte[] cipherBytes = CipherStuff.getInstance().encrypt(plainBytes, null, true);
			
			// store in "resources/text.lock"
			WriteResources.write(cipherBytes, "resources/text.lock", null);

			Zeroizer.zero(plainBytes);
		} 

		Zeroizer.zero(imageBytes);
		System.exit(0);
	}

	@Override
	protected byte[] getPlainBytes() {
		return null;
	}
}
