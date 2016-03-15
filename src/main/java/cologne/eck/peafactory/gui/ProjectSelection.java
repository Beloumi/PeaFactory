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

/*
 * GUI for selection of project type, a type of a Password Encrypted Archive (pea)
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.peagen.*;
import cologne.eck.peafactory.peas.editor_pea.EditorType;
import cologne.eck.peafactory.peas.file_pea.FileType;
import cologne.eck.peafactory.peas.image_pea.ImageType;
import cologne.eck.peafactory.peas.note_pea.NotesType;
import cologne.eck.peafactory.tools.MouseRandomCollector;
//import cologne.eck.peafactory.tools.RandomCollector;



@SuppressWarnings("serial")
public class ProjectSelection extends JDialog 
		implements ActionListener, ItemListener {
	
	protected static ProjectSelection projectSelection;
	
	private JRadioButton notesButton;
	private JCheckBox simpleNotesCheck;


	protected ResourceBundle languageBundle;
	
	public ProjectSelection() {
		
		projectSelection = this;

		// i18n
		languageBundle = PeaFactory.getLanguagesBundle();

		this.setAlwaysOnTop(false);
		this.setTitle(languageBundle.getString("project_selection_title"));
		// to close program if this closed after start
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener( new WindowAdapter(){
			public void windowClosing(WindowEvent we) {
				// close program if ManView is not instantiated or not visible (program start)
				// else: close this dialog
				if (PeaFactory.isFrameInstantiated() == false  || MainView.isDisplayed() == false) {
					//System.out.println("Program terminates");
					System.exit(0);
				} else {
					projectSelection.dispose();
				}	
		}});
		
		// use frame to collect random values: 
		this.addMouseMotionListener(new MouseRandomCollector());

		this.setIconImage(MainView.getImage());
		
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setBorder(new EmptyBorder(10,10,10,10));
		
	    //--------------//
	    // COMPONENTS:  //
	    //--------------//		
		
		JLabel typeLabel = new JLabel();
		typeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		typeLabel.setText(languageBundle.getString("selection_type_label"));
		
		BufferedImage image = null;
		 try {
			//image = ImageIO.read(new File("src" + File.separator + "resources" + File.separator + "pea-factory.png"));
			image = ImageIO.read(new File("resources" + File.separator + "peafactory.png"));

		} catch (IOException e) {
			System.err.println("ProjectSelection: image read failed");
		}	 
		Image scaledImage = image.getScaledInstance(256, 128, Image.SCALE_FAST);//92, 92, Image.SCALE_FAST);
		JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
	    
		JRadioButton fileButton = new JRadioButton();
		fileButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		fileButton.setText(languageBundle.getString("selection_file_button"));
		fileButton.setToolTipText(languageBundle.getString("tooltip_selection_file_button"));
	    fileButton.setMnemonic(KeyEvent.VK_F);
	    fileButton.setMargin(new Insets(10,10,10,10));//oben, links, unten, rechts
	    fileButton.setActionCommand("file");
	    fileButton.addItemListener(this);

		notesButton = new JRadioButton();
		notesButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		notesButton.setText(languageBundle.getString("selection_notes_button"));
		notesButton.setToolTipText(languageBundle.getString("tooltip_selection_notes_button"));
	    notesButton.setMnemonic(KeyEvent.VK_N);
	    notesButton.setMargin(new Insets(10,10,0,10));//oben, links, unten, rechts
	    notesButton.setActionCommand("notes");
	    notesButton.addItemListener(this);
	    
	    simpleNotesCheck = new JCheckBox();	
	    simpleNotesCheck.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
	    simpleNotesCheck.setText(languageBundle.getString("selection_simple_notes"));
	    simpleNotesCheck.setToolTipText(languageBundle.getString("tooltip_selection_simple_check"));
	    simpleNotesCheck.setMnemonic(KeyEvent.VK_E);    
	    simpleNotesCheck.addItemListener(this);
	    simpleNotesCheck.setActionCommand("simple_notes");	    
	    
	    JRadioButton imageButton = new JRadioButton();
		imageButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		imageButton.setText(languageBundle.getString("selection_image_button"));
		imageButton.setToolTipText(languageBundle.getString("tooltip_selection_image_button"));
	    imageButton.setMnemonic(KeyEvent.VK_I);
	    imageButton.setMargin(new Insets(10,10,20,10));//oben, links, unten, rechts
	    imageButton.setActionCommand("image");
	    imageButton.addItemListener(this);	    	    
	    
	    ButtonGroup typeGroup = new ButtonGroup();
	    typeGroup.add(notesButton);
	    typeGroup.add(imageButton);
	    typeGroup.add(fileButton);	    
	    
	    fileButton.setSelected(true);//NullPointer if before pswChangeCheck is initialized
	    
	    JButton okButton = new JButton();
	    okButton.setText("ok");
	    okButton.addActionListener(this);
	    okButton.setActionCommand("ok");
	    okButton.setToolTipText(languageBundle.getString("tooltip_project_ok_button"));
	    	    
	    //-----------//
	    // LAYOUT:   //
	    //-----------//
	    
	    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
	    
	    JPanel typeLabelPanel = new JPanel();
	    typeLabelPanel.setLayout(new BoxLayout(typeLabelPanel, BoxLayout.X_AXIS));
	    typeLabelPanel.add(typeLabel);
	    typeLabelPanel.add(Box.createHorizontalGlue());
	    contentPane.add(typeLabelPanel);
	    contentPane.add(Box.createVerticalStrut(10));
	   
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.X_AXIS));
		imagePanel.add(Box.createHorizontalGlue());
		imagePanel.add(imageLabel);
		imagePanel.add(Box.createHorizontalStrut(10));
		contentPane.add(imagePanel);
//		contentPane.add(Box.createVerticalStrut(10));
	    
	    JPanel fileButtonPanel = new JPanel();
	    fileButtonPanel.setLayout(new BoxLayout(fileButtonPanel, BoxLayout.X_AXIS));
	    fileButtonPanel.add(fileButton);
	    fileButtonPanel.add(Box.createHorizontalGlue());
	    contentPane.add(fileButtonPanel);	    
	    contentPane.add(Box.createVerticalStrut(20));	    
	    
	    JPanel textButtonPanel = new JPanel();
	    textButtonPanel.setLayout(new BoxLayout(textButtonPanel, BoxLayout.X_AXIS));
	    textButtonPanel.add(notesButton);
	    textButtonPanel.add(Box.createHorizontalGlue());	    
	    contentPane.add(textButtonPanel);
	    
	    JPanel editorPanel = new JPanel();
	    editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.X_AXIS));
	    editorPanel.add(Box.createHorizontalStrut(50));
	    editorPanel.add(simpleNotesCheck);
	    editorPanel.add(Box.createHorizontalGlue());
	    contentPane.add(editorPanel);		    
	    contentPane.add(Box.createVerticalStrut(20));	    	    
	    
	    JPanel imageButtonPanel = new JPanel();
	    imageButtonPanel.setLayout(new BoxLayout(imageButtonPanel, BoxLayout.X_AXIS));
	    imageButtonPanel.add(imageButton);
	    imageButtonPanel.add(Box.createHorizontalGlue());
	    contentPane.add(imageButtonPanel);	    
	    
	    JPanel okButtonPanel = new JPanel();
	    okButtonPanel.setLayout(new BoxLayout(okButtonPanel, BoxLayout.X_AXIS));
	    okButtonPanel.add(Box.createHorizontalGlue());
	    okButtonPanel.add(okButton);
	    contentPane.add(okButtonPanel);
	    	    
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		
		String com = ape.getActionCommand();

		if (com.equals("ok")) {
			
			// reset openedFileName:
			MainView.setOpenedFileName(null);			
			// reset data in DataType from previous setting
			DataType.getCurrentType().setData(null);
			
			MainView.updateFrame();			
			this.dispose();
		} else {
			//
		}
	}

	//
	// logic of selection and deselection
	//
	@Override
	public void itemStateChanged(ItemEvent ie) {
		AbstractButton aButton = (AbstractButton)ie.getSource();
        int state = ie.getStateChange();
        String com = aButton.getActionCommand();
        
        if (com.equals("file")) {
           	if (state == ItemEvent.SELECTED) {				
				DataType.setCurrentType( new FileType() );
        		simpleNotesCheck.setSelected(false);
        	} else { // ItemEvent.DESELECTED
        		//
        	}
        } else if (com.equals("notes")) {
        	if (state == ItemEvent.SELECTED) {
				DataType.setCurrentType( new EditorType() );
        	} else { // ItemEvent.DESELECTED
        	}
        } else if (com.equals("simple_notes")) {
        	if (state == ItemEvent.SELECTED) {
        		notesButton.setSelected(true);
        		simpleNotesCheck.setSelected(true);
				DataType.setCurrentType( new NotesType() );
        	} else { // ItemEvent.DESELECTED
        	}
        } else if (com.equals("image")) {
        	if (state == ItemEvent.SELECTED) {
				DataType.setCurrentType( new ImageType() );
        		simpleNotesCheck.setSelected(false);
        		//DataType.getCurrentType().setData(null);
        		ImageType.setImageName(null);
        	} else { // ItemEvent.DESELECTED
        		//
        	}
        }
        DataType.getCurrentType().setData(null);
	}
}
