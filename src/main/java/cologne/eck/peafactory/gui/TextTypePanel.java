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
 * Panel to display and write text for text based peas. 
 * Displayed in MainView. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.peagen.*;
import cologne.eck.peafactory.peas.editor_pea.EditorType;
import cologne.eck.peafactory.peas.note_pea.NotesType;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.WriteResources;

@SuppressWarnings("serial")
public class TextTypePanel extends JPanel implements ActionListener {
	
	private static JTextArea area;
	private ResourceBundle languageBundle;
	
	private TextTypePanel(ResourceBundle _languageBundle) {
		
		this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS));
		languageBundle = _languageBundle;
		String openedFileName = MainView.getOpenedFileName();
		DataType dataType = DataType.getCurrentType();
		
		JPanel areaPanel = new JPanel();
		areaPanel.setLayout(new BoxLayout(areaPanel, BoxLayout.Y_AXIS));
		
		JPanel areaLabelPanel = new JPanel();
		areaLabelPanel.setLayout(new BoxLayout(areaLabelPanel, BoxLayout.X_AXIS));
		JLabel areaLabel = new JLabel(languageBundle.getString("area_label"));//("text to encrypt: ");
		areaLabel.setPreferredSize(new Dimension(300,30));
		
		areaLabelPanel.add(areaLabel);
		areaLabelPanel.add(Box.createHorizontalGlue()); 
		areaPanel.add(areaLabelPanel);
		
		area = new JTextArea(30, 20);
		area.setDragEnabled(true);
		area.setEditable(true);	
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setToolTipText(languageBundle.getString("tooltip_textarea"));

		if (openedFileName != null) {
			if (openedFileName.endsWith("txt")) {

				byte[] clearBytes = ReadResources.readExternFile(openedFileName);
				
				if (clearBytes == null){

					clearBytes = "empty file".getBytes(PeaFactory.getCharset());
				}
					
				displayText(new String( Converter.bytes2chars(clearBytes) ) );
			} else if (openedFileName.endsWith("rtf")) {
				Document doc = new DefaultStyledDocument();
			
				// get text and display in text area:
				RTFEditorKit kit = new RTFEditorKit();	
                FileInputStream in; 
                try {
                    in = new FileInputStream(openedFileName); 
                    kit.read(in, doc, 0); 
                    in.close();
                } catch (FileNotFoundException e) {
                	System.err.println("TextTypePanel: " + e);
                	JOptionPane.showMessageDialog(this,
                		    "File " + openedFileName + " not found...\n." + e,
                		    "Error",
                		    JOptionPane.ERROR_MESSAGE);
                	return;
                } catch (IOException e){
                	System.err.println("TextTypePanel: " + e);
                	JOptionPane.showMessageDialog(this,
                		    "reading the file " + openedFileName + " failed (IOException)\n," 
                	 + e,
                		    "Error",
                		    JOptionPane.ERROR_MESSAGE);
                	return;
                } catch (BadLocationException e){
                	System.err.println("TextTypePanel: " + e);
                	JOptionPane.showMessageDialog(this,
                		    "Bad Location of file " + openedFileName + ".\n." + e,
                		    "Error",
                		    JOptionPane.ERROR_MESSAGE);
                	return;
                }   
                try {
                	String text =  doc.getText(0, doc.getLength() );
                	//System.out.println("text: " + text);
					displayText(text);
				} catch (BadLocationException e1) {
					System.err.println("TextTypePanel: " + e1);
                	JOptionPane.showMessageDialog(this,
                		    "Bad Location of text in file " + openedFileName + ".\n." + e1,
                		    "Error",
                		    JOptionPane.ERROR_MESSAGE);
                	return;
				}	 
                // modifications would not be saved, because document was set to EditorType
                area.setEditable(false);
			} 
		} else {
			if (dataType.getData() != null) { // was set by open file
				if (dataType instanceof NotesType) {

					area.setText(new String(dataType.getData(), PeaFactory.getCharset()));

				} else if (dataType instanceof EditorType) {
					byte[] data = new EditorType().getData();
					DefaultStyledDocument doc = Converter.deserialize(data);
					if (doc == null) {
			    		JOptionPane.showMessageDialog(this, 
			    				languageBundle.getString("invalid_rtf_file") + "\n", 
			    				   "Error",                                 
			    				   JOptionPane.ERROR_MESSAGE);  
					} else {
						try {
							area.setText(doc.getText(0,  doc.getLength() ));
						} catch (BadLocationException e) {
							System.err.println("TextTypePanel: set text failed");
		                	JOptionPane.showMessageDialog(this,
		                		    "Bad Location in text of file " + openedFileName + ".\n." + e,
		                		    "Error",
		                		    JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
		                	return;
						}
					}
					area.setEditable(false);
				}
			}
		}
		
		JScrollPane areaScroll = new JScrollPane(area);	
		areaScroll.setPreferredSize(new Dimension(300,100));
		
		areaPanel.add(areaScroll);
		
		this.add(areaPanel);
		
		JPanel textFileTypeLabelPanel = new JPanel();
		textFileTypeLabelPanel.setLayout( new BoxLayout(textFileTypeLabelPanel, BoxLayout.X_AXIS));
		textFileTypeLabelPanel.add(Box.createHorizontalGlue());
		JLabel textFileTypeLabel = new JLabel();
		textFileTypeLabel.setBorder(new LineBorder(  Color.GRAY));
		String textFileType = null;

		if (dataType instanceof NotesType) {
				textFileType = languageBundle.getString("changeable_text_button");
		} else if (dataType instanceof EditorType){ // editor
				textFileType = languageBundle.getString("format_text_button");
		}
		if (openedFileName != null) {
			textFileTypeLabel.setText(textFileType + " - " + languageBundle.getString("external_file"));
		} else {
			textFileTypeLabel.setText(textFileType + " - " + languageBundle.getString("internal_file"));
		}
		textFileTypeLabelPanel.add(textFileTypeLabel);			
		this.add(textFileTypeLabelPanel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));			
		JButton openFileButton = new JButton(languageBundle.getString("open_file_button"));
		openFileButton.setToolTipText(languageBundle.getString("tooltip_open_file_button"));
		openFileButton.addActionListener(this);
		openFileButton.setActionCommand("openFile");
		buttonPanel.add(openFileButton);	
		
		JButton newFileButton = new JButton(languageBundle.getString("new_file_button"));
		newFileButton.setToolTipText(languageBundle.getString("tooltip_new_file_button"));
		newFileButton.addActionListener(this);
		newFileButton.setActionCommand("newTextFile");
		buttonPanel.add(newFileButton);	

		buttonPanel.add(Box.createHorizontalGlue()); 
		this.add(buttonPanel);		
	}
	
	public final static TextTypePanel getInstance(ResourceBundle languageBundle) {

		return new TextTypePanel(languageBundle);
	}


	@Override
	public void actionPerformed(ActionEvent ape) {
		
		String com = ape.getActionCommand();
		
		if (com.equals("openFile")) { // text or image file
			DataType dataType = DataType.getCurrentType();
			JFileChooser chooser = new JFileChooser();
			
			FileNameExtensionFilter txtfilter = null;
			if (dataType instanceof NotesType) {
				txtfilter = new FileNameExtensionFilter(
	                    "text files",  "txt");
			} else if (dataType instanceof EditorType) {
				txtfilter = new FileNameExtensionFilter(
	                    "rich text format", "rtf");	
			}

	        chooser.setFileFilter(txtfilter);
			
		    int returnVal = chooser.showOpenDialog(null);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) {		

		    	File selectedFile = chooser.getSelectedFile();

		    	String selectedFileName = selectedFile.getAbsolutePath();
		    	
		    	// check read access: 
		    	if ( selectedFile.canRead() ){
		    		
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
				    		JOptionPane.showMessageDialog(null, 
				    				languageBundle.getString("no_write_access_to_file") + "\n"
				    					+ selectedFileName, 
				    				   "Error",                                 
				    				   JOptionPane.ERROR_MESSAGE);  
				    		return;
		    			}
				    	// check rtf if valid:
		    			if (selectedFileName.endsWith("rtf")) {
							DefaultStyledDocument doc = Converter.deserialize(ReadResources.readExternFile(selectedFileName ));
							if (doc == null) {
					    		JOptionPane.showMessageDialog(this, 
					    				languageBundle.getString("invalid_rtf_file") + "\n", 
					    				   "Error",                                 
					    				   JOptionPane.ERROR_MESSAGE); 
					    		return;
							}
		    			}
		    			MainView.setOpenedFileName( selectedFileName );		    			

		    		} else if (sel.equals(languageBundle.getString("option_copy_file"))) {
		    			MainView.setOpenedFileName( null ); 
		    			dataType.setData(ReadResources.readExternFile(selectedFileName ));
		    			
				    	// set data in DataType
		    			if (dataType instanceof EditorType) {
				    		byte[] data = ReadResources.readExternFile(selectedFileName );
				    		new EditorType().setData(data);
				    	}				    	
		    		} else { // cancel
		    			return;
		    		}

		    	} else { // can not read file
		    		JOptionPane.showMessageDialog(this, 
		    				languageBundle.getString("no_read_access_to_file") + "\n"
		    					+ selectedFileName, 
		    				   "Error",                                 
		    				   JOptionPane.ERROR_MESSAGE);   
		    		return;
		    	}
		    	MainView.updateFrame();		    	
		    }

		} else if (com.equals("newTextFile")) { 

		    JFileChooser chooser = new JFileChooser();
		    
		    DataType dataType = DataType.getCurrentType();
		    
			FileNameExtensionFilter txtfilter = null;
			if (dataType instanceof NotesType) {
				txtfilter = new FileNameExtensionFilter(
	                    "text files",  "txt");
			} else if (dataType instanceof EditorType) {
				txtfilter = new FileNameExtensionFilter(
	                    "rich text format", "rtf");	
			}

            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(txtfilter);
		   // chooser.setCurrentDirectory(new File("/home/Documents"));
		    int retrival = chooser.showSaveDialog(null);
		    if (retrival == JFileChooser.APPROVE_OPTION) {		    	
		    	
		    	File selectedFile = chooser.getSelectedFile();
		    	
		    	// append file extension:
				if (dataType instanceof NotesType && ! (selectedFile.getPath().endsWith(".txt"))) {
					selectedFile = new File(selectedFile.getPath() + ".txt");
				} else if (dataType instanceof EditorType && ! (selectedFile.getPath().endsWith(".rtf"))) {
					selectedFile = new File(selectedFile.getPath() + ".rtf");
				}
		    	
		    	// Warning if file exists: 			    	
		    	if ( selectedFile.exists()){
		    		int n = JOptionPane.showConfirmDialog(this,
		    			languageBundle.getString("overwrite_existing_file"), 
		    			"Warning", 
		    			JOptionPane.OK_CANCEL_OPTION,
		    			JOptionPane.WARNING_MESSAGE);
		    		if (n != JFileChooser.APPROVE_OPTION) return; // cancel
		    	}		    	
		    	
		    	WriteResources.writeText("\n", selectedFile.getPath() );// prevent NullPointerEx.
		    	
		    	MainView.setOpenedFileName(  selectedFile.getPath() );
		    	
				MainView.updateFrame();
				
				area.setEditable(true);// was false for EditorType
		    }								
		}
	}
	//========================
	// Getter & Setter:
	public static char[] getText() {
		return area.getText().toCharArray();
	}
	public static void resetText() {
		area.setText("");
	}
	public static void displayText(String text) {
		// TODO java.lang.OutOfMemoryError: Java heap space
		// for large text files
			area.setText(text);
	}
}
