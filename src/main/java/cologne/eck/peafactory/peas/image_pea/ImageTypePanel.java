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
 * Panel to display the image of the image pea.
 */

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import cologne.eck.peafactory.gui.MainView;
import cologne.eck.peafactory.peagen.*;
import cologne.eck.peafactory.tools.ReadResources;



@SuppressWarnings("serial")
public class ImageTypePanel extends JPanel implements ActionListener {
	
	private ResourceBundle languageBundle;	
	private static boolean internal = true;
	
	private ImageTypePanel(ResourceBundle _languageBundle) {
		
		this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		languageBundle = _languageBundle;
		
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
		
		if (ImageType.getImageName() != null) {			

			if (validateImageFile(ImageType.getImageName()) == false){
				//	
			} else {

				Image scaledImage = new ImageType().getImageFromInternalImageName();
				JLabel picLabel = new JLabel(new ImageIcon(scaledImage));				 

				JScrollPane picScrollPane = new JScrollPane(picLabel);
				imagePanel.add(picScrollPane);
				picScrollPane.setPreferredSize(new Dimension(300, 200));

				JRadioButton externalButton = new JRadioButton();
				JRadioButton internalButton = new JRadioButton();

				internalButton.setText(languageBundle.getString("internal_image"));
				internalButton.setToolTipText(languageBundle.getString("tooltip_internal_image"));
			    internalButton.addActionListener(this);
			    internalButton.setActionCommand("internalImage");
			    internalButton.setSelected(true);
			    imagePanel.add(internalButton);
			    
				externalButton.setText(languageBundle.getString("external_image"));
				externalButton.setToolTipText(languageBundle.getString("tooltip_external_image"));
			    externalButton.addActionListener(this);
			    externalButton.setActionCommand("externalImage");
			    imagePanel.add(externalButton);
			    
			    ButtonGroup group = new ButtonGroup();
			    group.add(internalButton);
			    group.add(externalButton);
			    if (internal == true) {
			    	internalButton.setSelected(true);
			    } else {
			    	externalButton.setSelected(true);
			    }			    
			}
		} else {
			JLabel imageLabel = new JLabel(" ");
			imageLabel.setPreferredSize(new Dimension(300,100));
			imagePanel.add(imageLabel);
		}
		
		JButton openFileButton = new JButton(languageBundle.getString("open_image_button"));//"open image file");
		openFileButton.setToolTipText(languageBundle.getString("tooltip_open_image_button"));
		openFileButton.addActionListener(this);
		openFileButton.setActionCommand("openFile");
		imagePanel.add(openFileButton);
		
		this.add(Box.createVerticalStrut(20));
		this.add(imagePanel);						
	}
	
	public static ImageTypePanel getInstance(ResourceBundle languageBundle) {
		return new ImageTypePanel(languageBundle);
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		String com = ape.getActionCommand();
		
		if (com.equals("openFile")) { // text or image file
			DataType dataType = DataType.getCurrentType();
			JFileChooser chooser = new JFileChooser();

		    chooser.setAcceptAllFileFilterUsed(false);
		    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
		        "Images: jpg, gif, png", "jpg", "gif", "png", "jpeg", "JPG", "JPEG", "GIF", "PNG");
		    chooser.setFileFilter(imageFilter);
			
		    int returnVal = chooser.showOpenDialog(null);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {		

		    	File selectedFile = chooser.getSelectedFile();
		    	String selectedFileName = selectedFile.getAbsolutePath();		    	
		    	
		    	// check read access: 
		    	if ( validateImageFile(selectedFileName) == true ){
		    		
		    		Object[] possibilities = {
		    				languageBundle.getString("option_overwrite_file"),
		    				languageBundle.getString("option_copy_file"),
		    				languageBundle.getString("option_cancel")};

		    		String sel = (String)JOptionPane.showInputDialog(
		    		                    this,
		    		                    languageBundle.getString("deal_with_file")
		    		                    + "\n" + selectedFileName,
		    		                    "", // title
		    		                    JOptionPane.PLAIN_MESSAGE,
		    		                    null, // icon
		    		                    possibilities,
		    		                    languageBundle.getString("option_overwrite_file"));
		    		
		    		if (sel.equals(languageBundle.getString("option_overwrite_file"))) {
		    			
		    			if ( ! selectedFile.canWrite() ) {
				    		JOptionPane.showMessageDialog(this, 
				    				languageBundle.getString("no_write_access_to_file") + "\n"
				    					+ selectedFileName, 
				    				   "Error",                                 
				    				   JOptionPane.ERROR_MESSAGE);  
				    		return;
		    			}
		    			
		    			MainView.setOpenedFileName(selectedFileName);
		    			// used to display image
					    ImageType.setImageName (selectedFileName );					    
					    internal = false;

		    		} else if (sel.equals(languageBundle.getString("option_copy_file"))) {

		    			MainView.setOpenedFileName( null ); 
		    			dataType.setData(ReadResources.readExternFile(selectedFileName ));		    			
				    	// set data in DataType
					    ImageType.setImageName (selectedFileName );					    
					    internal = true;
				    	
		    		} else { // cancel
		    			return;
		    		}
		    	} else { // invalid file

	    			MainView.setOpenedFileName( null ); 
	    			dataType.setData(null);		    			
				    ImageType.setImageName (null);					    

		    	}
				MainView.updateFrame();		    	
		    }
			
		} else if (com.equals("internalImage")) { // imageName was set already in openFile
			
			// check if file is image (if previously encrypted image is used):
			if ( validateImageFile(ImageType.getImageName()) == false)  {

				internal = false;
				ImageType.setImageName(null);
				new ImageType().setData(null);
				MainView.setOpenedFileName(null);
				MainView.updateFrame();
				return;
		   }
		   if (MainView.getOpenedFileName() != null) {// frame as use before to encrypt image file
			   ImageType.setImageName (MainView.getOpenedFileName() );	
		   }
			MainView.setOpenedFileName( null );
			// set data in ImageType
			DataType.getCurrentType().setData(ReadResources.readExternFile(ImageType.getImageName() ));		    							    
		    internal = true;
			
		} else if (com.equals("externalImage")) {
			if ( ! new File(ImageType.getImageName()).canWrite() ) {
	    		JOptionPane.showMessageDialog(this, 
	    				languageBundle.getString("no_write_access_to_file") + "\n"
	    					+ ImageType.getImageName(), 
	    				   "Error",                                 
	    				   JOptionPane.ERROR_MESSAGE);  
	    		return;
			}
			// set openedFileName 
			MainView.setOpenedFileName( ImageType.getImageName() );							    
		    internal = false;
		}
	}
	private final boolean validateImageFile( String imageFileName) {

		
		File imageFile = new File(imageFileName);
		
		if ( ! imageFile.exists() || ! imageFile.isFile() ) { // this should never happen
			JOptionPane.showMessageDialog(this,  // frame
					languageBundle.getString("invalid_image_file")
					+ "\n*** Unexpected program error.***\n",
					"Unexpected Error",                     // title
					JOptionPane.ERROR_MESSAGE);  // type
			return false;
		}
		
    	// check read access: 
    	if ( ! imageFile.canRead() ){
    		System.err.println("image read failed");
    		JOptionPane.showMessageDialog(this, 
    				languageBundle.getString("no_read_access_to_file") + "\n"
    					+ imageFileName, 
    				   "Error",                                 
    				   JOptionPane.ERROR_MESSAGE);   
    		return false;
    	}
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			System.err.println(e.toString());
    		JOptionPane.showMessageDialog(this, 
    				languageBundle.getString("no_read_access_to_file") + "\n"
    					+ imageFileName, 
    				   "Error",                                 
    				   JOptionPane.ERROR_MESSAGE);   
    		return false;
		}	
		if (image == null) {
			JOptionPane.showMessageDialog(this,  // frame
					languageBundle.getString("invalid_image_file"),
					"Error",                     // title
					JOptionPane.ERROR_MESSAGE);  // type
			return false;
		}
		return true;
	}

}
