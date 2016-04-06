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
 * Generates the image pea. 
 */

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.gui.*;
import cologne.eck.peafactory.peagen.DataType;
import cologne.eck.peafactory.peagen.JarStuff;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.WriteResources;


public final class ImageType extends DataType {
	
	// default values:
	private static byte[] imageBytes = null;
	
	private static String imageName = null;
	private static int imageWidth = 800;
	private static int imageHeight = 600;
	private static boolean resize = true;


	// returns null if not a valid image
	public final Image getImageFromInternalImageName	() {
		
		if (MainView.isBlankPea() == true) {
			return null;
		}
		
		// Maximal width and height to display in MainView:
		int picWidth = 500;
		int picHeight = 500;				
		
		// get image
		 BufferedImage image = null;
		 try {
			//image = ImageIO.read(new File("resources" + File.separator + "antalya.jpg"));
			image = ImageIO.read(new File(imageName));
		} catch (IOException e) {
			System.err.println("image read failed");
			return null;
		}	 
		 
		 if (image == null) {
			 System.err.println("ImageType: image null, imageName: " + imageName + ". Invalid image? ");
			 //System.exit(3);
			 return null;
		 }

		 int w = picWidth,h = picHeight;	

		 // resize image to size from menu (or default)
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
//		 JPanel imagePanel = new JPanel();
		 
		 return scaledImage;
	}
	
	//===============================================
	// Getter & Setter
	public static final int getImageWidth() {
		return imageWidth;
	}
	public static final void setImageWidth(int _imageWidth) {
		imageWidth = _imageWidth;
	}
	public static final int getImageHeight() {
		return imageHeight;
	}
	public static final void setImageHeight(int _imageHeight) {
		imageHeight = _imageHeight;
	}
	public static final void setResize(boolean _resize) {
		resize = _resize;
	}
	public static final boolean getResize() {
		return resize;
	}
	
	public static final String getImageName() {
		return imageName;
	}
	public static final void setImageName(String _imageName) {
		imageName = _imageName;
	}

	@Override
	public void processData(byte[] keyMaterial) {
		
		// get plainBytes: 
		// if (MainView.getOpenedFileName() == null) from imageBytes (set before ImageTypePanel)
		//     (the fileName to store was set as imageName not as MainView.getOpenedFileName() )
		// else from file MainView.getOpenedFileName() (and store cipherBytes there later)
		
		byte[] plainBytes = null;
		if (MainView.getOpenedFileName() == null) {

			plainBytes = imageBytes;

		} else {
			plainBytes = ReadResources.readExternFile(MainView.getOpenedFileName());
		}
		
		if (plainBytes == null) { // read-write was tested in ImageTypePanel, so this might not happen
			if (MainView.isBlankPea() == true) {
				//
			} else {
				System.err.println("ImageType: getting plainBytes failed");
				return;
			}
		}
		
		// append settings for this image: 		
		int resizeValue = 0;
		if (resize == true) resizeValue = 1;
		int[] settings = new int[3];
		settings[0] = imageWidth;
		settings[1] = imageHeight;
		settings[2] = resizeValue;
		byte[] settingBytes = Converter.ints2bytesBE(settings);
//Help.printBytes("ImagTyp setting", settingBytes);


		// encrypt: append padding, pswIdentifier, cryptIV, fileIdentifier
		// do not encrypt by session key: zero key
		byte[] cipherBytes = null;
		if (MainView.isBlankPea() == true) {
			
			cipherBytes = "uninitializedFile".getBytes(PeaFactory.getCharset());
		
			
			// attach setting in plaintext:
			byte[] tmp = new byte[cipherBytes.length + settingBytes.length];
			System.arraycopy(cipherBytes, 0, tmp, 0, cipherBytes.length);		
			System.arraycopy(settingBytes, 0, tmp, cipherBytes.length, settingBytes.length);
			cipherBytes = tmp;
			
			String dirName = JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() - 4); // extract ".jar"

			// store cipherBytes beside jar archive:
			WriteResources.write(cipherBytes, "text.lock", "peas" + File.separator + dirName + File.separator + "resources");
			//setData(null);
			return;
			
		} else {
			
			byte[] tmp = new byte[plainBytes.length + settingBytes.length];
			System.arraycopy(plainBytes, 0, tmp, 0, plainBytes.length);		
			System.arraycopy(settingBytes, 0, tmp, plainBytes.length, settingBytes.length);
			plainBytes = tmp;	
			
			cipherBytes = CipherStuff.getInstance().encrypt(plainBytes, keyMaterial, false);
		}
		
		
		// store cipherBytes + settings:
		if (MainView.getOpenedFileName() == null) {
			
			// write text.lock file in directory resources of program (not pea!)
			// and add it later in CreateJarFile to the jar
			WriteResources.write(cipherBytes, "resources/text.lock", null);
			
		} else { // write cipherBytes in external file and file path in text.lock			
			WriteResources.write(cipherBytes, MainView.getOpenedFileName(), null);
		}		
	}
	

	@Override
	public String getDescription() {
		return PeaFactory.getLanguagesBundle().getString("image_description");
	}

	@Override
	public String getTypeName() {
		return "image";
	}

	@Override
	public byte[] getData() {
		return imageBytes;
	}

	@Override
	public void setData(byte[] data) {
		imageBytes = data;		
	}
}
