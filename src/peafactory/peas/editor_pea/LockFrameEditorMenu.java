package cologne.eck.peafactory.peas.editor_pea;

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
 * Menu of main frame of editor pea.  
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;

import settings.PeaSettings;
import cologne.eck.peafactory.peas.PswDialogBase;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.WriteResources;
import cologne.eck.peafactory.tools.Zeroizer;



@SuppressWarnings("serial")
final class LockFrameEditorMenu extends JMenuBar implements ActionListener {

	public LockFrameEditorMenu() {
		setBorder(new EtchedBorder() );
		//setPreferredSize(new Dimension(300, 30));
		setMinimumSize(new Dimension(100, 50));
		setMaximumSize(new Dimension(200, 50));
		

		JMenu fileMenu = new JMenu("File");//\u00FC");
		fileMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		this.add(fileMenu);
								
		JMenuItem saveItem = new JMenuItem("save (encrypted)");
		saveItem.setActionCommand("save");
		saveItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.addActionListener(this);
		fileMenu.add(saveItem);
		
		JMenuItem saveAsItem = new JMenuItem("Save as...");
		saveAsItem.setActionCommand("saveAs");
		saveAsItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		saveAsItem.setMnemonic(KeyEvent.VK_A);
		saveAsItem.addActionListener(this);
		fileMenu.add(saveAsItem);
		
		if (PeaSettings.getExternFile() == true) {
			JMenuItem openItem = new JMenuItem("open file (txt, rtf or lock file)");
			openItem.setActionCommand("open");
			openItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			openItem.setMnemonic(KeyEvent.VK_O);
			openItem.addActionListener(this);
			fileMenu.add(openItem);
		}

		if (PeaSettings.getPswGenerator() != null) {
			JMenuItem randomPswItem = new JMenuItem("Generate random password");
			randomPswItem.setActionCommand("randomPsw");
			randomPswItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
			randomPswItem.setMnemonic(KeyEvent.VK_R);
			randomPswItem.addActionListener(this);
			fileMenu.add(randomPswItem);		
		}

		JMenuItem pswItem = new JMenuItem("Change password");
		pswItem.setActionCommand("psw");
		pswItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		pswItem.setMnemonic(KeyEvent.VK_P);
		pswItem.addActionListener(this);
		fileMenu.add(pswItem);			
		

		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.setActionCommand("quit");
		quitItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		quitItem.setMnemonic(KeyEvent.VK_Q);
		quitItem.addActionListener(this);
		fileMenu.add(quitItem);
		
		
		JMenu editMenu = new JMenu("Edit");
		editMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		editMenu.setMnemonic(KeyEvent.VK_E);
		this.add(editMenu);

		JMenuItem undoItem = new JMenuItem("Undo");
		undoItem.setActionCommand("undo");
		undoItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		undoItem.setMnemonic(KeyEvent.VK_U);
		undoItem.addActionListener(this);
		editMenu.add(undoItem);

		JMenuItem redoItem = new JMenuItem("Redo");
		redoItem.setActionCommand("redo");
		redoItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		redoItem.setMnemonic(KeyEvent.VK_R);
		redoItem.addActionListener(this);
		editMenu.add(redoItem);
		
		JMenuItem cutItem = new JMenuItem("Cut");
		cutItem.setActionCommand("cut");
		cutItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		cutItem.setMnemonic(KeyEvent.VK_C);
		cutItem.addActionListener(this);
		editMenu.add(cutItem);

		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.setActionCommand("copy");
		copyItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		copyItem.setMnemonic(KeyEvent.VK_O);
		copyItem.addActionListener(this);
		editMenu.add(copyItem);

		JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.setActionCommand("paste");
		pasteItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		pasteItem.setMnemonic(KeyEvent.VK_P);
		pasteItem.addActionListener(this);
		editMenu.add(pasteItem);

		JMenuItem selectAllItem = new JMenuItem("Select All");
		selectAllItem.setActionCommand("selectAll");
		selectAllItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		selectAllItem.setMnemonic(KeyEvent.VK_S);
		selectAllItem.addActionListener(this);
		editMenu.add(selectAllItem);
		
		
		JMenu formatMenu = new JMenu("Format");
		formatMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		formatMenu.setMnemonic(KeyEvent.VK_F);
		this.add(formatMenu);

		JMenu fontMenu = new JMenu("Font");
	//	fontMenu.getPopupMenu().setLayout(new GridLayout(25,5));
		fontMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		fontMenu.setMnemonic(KeyEvent.VK_F);
		formatMenu.add(fontMenu);
		
	    //GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //String fontNames[] = ge.getAvailableFontFamilyNames();
		String fontNames[] = {"SERIF", "SANS_SERIF", "MONOSPACED"};
        for (String font : fontNames) {
        	fontMenu.add(new StyledEditorKit.FontFamilyAction(font,font));
        }

		JMenu fontStyleMenu = new JMenu("Font style");
		fontStyleMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		fontStyleMenu.setMnemonic(KeyEvent.VK_S);
		formatMenu.add(fontStyleMenu);
		
        Action action =  new StyledEditorKit.BoldAction();
        action.putValue(Action.NAME, "Bold");
        fontStyleMenu.add(action);
 
        action = new StyledEditorKit.ItalicAction();
        action.putValue(Action.NAME, "Italic");
        fontStyleMenu.add(action);
 
        action = new StyledEditorKit.UnderlineAction();
        action.putValue(Action.NAME, "Underline");
        fontStyleMenu.add(action);
		
		JMenu fontSizeMenu = new JMenu("Font size");
		fontSizeMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		fontSizeMenu.setMnemonic(KeyEvent.VK_Z);
		formatMenu.add(fontSizeMenu);
		
		for (int i = 6; i < 25; i++) {				
			fontSizeMenu.add(new StyledEditorKit.FontSizeAction("" + i, i));
		}
		
		JMenu colorMenu = new JMenu("Font color");
		colorMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		colorMenu.setMnemonic(KeyEvent.VK_C);
		formatMenu.add(colorMenu);
		
		String[] colors = {"BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", 
				"LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW"};
		for (int i = 0; i < 12; i++) {
			Color color;
			try {
			    java.lang.reflect.Field field = Color.class.getField(colors[i]);
			    color = (Color)field.get(null);
			} catch (Exception e) {
			    color = null; // Not defined
			}				
			colorMenu.add(new StyledEditorKit.ForegroundAction(colors[i], color));
		}
		

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		helpMenu.setMnemonic(KeyEvent.VK_H);
		this.add(helpMenu);
		
		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.setActionCommand("about");
		aboutItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		aboutItem.setMnemonic(KeyEvent.VK_A);
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		JMenuItem homepageItem = new JMenuItem("Homepage");
		homepageItem.setActionCommand("home");
		homepageItem.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		homepageItem.setMnemonic(KeyEvent.VK_H);
		homepageItem.addActionListener(this);
		helpMenu.add(homepageItem);
	}
	

	
	@Override
	public void actionPerformed(ActionEvent ape) {
		String command = ape.getActionCommand();
		
		if (command.equals("save")) { // saves current content in current file
			
			byte[] plainBytes = PswDialogEditor.getLockFrame().getPlainBytes();
			LockFrameEditor.saveContent(null, plainBytes, PswDialogBase.getEncryptedFileName());
			
			LockFrameEditor.setDocChangeUnsaved(false);
			
		} else if (command.equals("saveAs")) {
			
			Object[] possibleOptions = { // "save as encrypted lock file", 
					"A. save as simple plain text file (unencrypted)", 
					"B. save as plain text in rich text format (unencrypted)"};
			Object selectedValue = JOptionPane.showInputDialog(this,
					"Choose how you want to save the file: \n","Choose...",
			        JOptionPane.PLAIN_MESSAGE, null,  possibleOptions, possibleOptions[0]);
			FileFilter filter = null;
			String fileName = null;
			String filePath = null;
			//String fileExtension = null;
			if (selectedValue == null) { // cancel		
				return;
//			} else if ( ((String)selectedValue).endsWith("lock file")) {
//				filter = new LockFilter();
    	        //fileExtension = ".lock";
			} else if ( ((String)selectedValue).startsWith("A")) {				
				filter = new TxtFilter();
    	        //fileExtension = ".txt";
			} else if ( ((String)selectedValue).startsWith("B")) {
				filter = new RtfFilter();
    	       //fileExtension = ".rtf";
			} 
			final JFileChooser fc = new JFileChooser();     
	        fc.setFileFilter( filter );
			int returnVal = fc.showSaveDialog(null);
		    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
		    	fileName = fc.getSelectedFile().getName();
		    	filePath = fc.getSelectedFile().getPath();
		    } else { // cancel option
		    	return;
		    }		

			// WriteRessources...	
		    if (filter instanceof TxtFilter) {		
			    String text = null;
				try {
					text = LockFrameEditor.textPane.getDocument().getText(0, LockFrameEditor.textPane.getDocument().getLength());
				} catch (BadLocationException e) {
					System.err.println("LockFrameEditor: " + e);
					e.printStackTrace();
				}
		    	byte[] textBytes = text.getBytes( PswDialogBase.getCharset() );//.Converter.chars2bytes(text.toCharArray());
		    	if (! fileName.endsWith(".txt")) filePath = filePath + ".txt";
			    WriteResources.write(textBytes, filePath, null);
			    
		    } else if (filter instanceof RtfFilter) {	

		    	if (! filePath.endsWith(".rtf")) filePath = filePath + ".rtf";
				StyledDocument doc = LockFrameEditor.textPane.getStyledDocument();
				RTFEditorKit kit = new RTFEditorKit();					 
                BufferedOutputStream out; 
                try {
                    out = new BufferedOutputStream(new FileOutputStream(filePath )); 
                    kit.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength()); 
                    out.close();
                } catch (FileNotFoundException e) {
                	System.err.println("EditorLockFrame Menu actionPerformed: " + e);
                } catch (IOException e){
                	System.err.println("EditorLockFrame Menu actionPerformed: " + e);
                } catch (BadLocationException e){
                	System.err.println("EditorLockFrame Menu actionPerformed: " + e);
                }	     
		    }  
/*		    if (filter instanceof LockFilter) {	 
		    	
		    	// store old encryptedFileName
		    	String oldFileName = PswDialogBase.getEncryptedFileName();
		    	if (! filePath.endsWith(".lock")) filePath = filePath + ".lock";
		    	String newFileName = filePath;
		    	// set new encryptedFileName:
		    	PswDialogBase.setEncryptedFileName( newFileName );
				byte[] plainBytes = PswDialogEditor.getLockFrame().getPlainBytes();
				LockFrameEditor.saveContent(null, plainBytes, PswDialogBase.getEncryptedFileName());
				// reset encryptedFileName:
                PswDialogBase.setEncryptedFileName ( oldFileName);
		    } */
		    
		} else if (command.equals("open")) { // current content get lost if not saved
			
			int n = JOptionPane.showConfirmDialog(
				    this,
				    "Your current content will get lost \n"
				    + "if you load the content of a new file\n\n"
				    + "(you can copy & paste the current content)",
				    "Warning",
				    JOptionPane.WARNING_MESSAGE,
				    JOptionPane.OK_CANCEL_OPTION);
			if (n == JOptionPane.CLOSED_OPTION || n == JOptionPane.CANCEL_OPTION) {
				return;
			} else if (n == JOptionPane.OK_OPTION) {
			
				Object[] possibleOptions = { //"open encrypted lock file", 
						"open simple clear text file"
						, "open clear text file in rich text format"};
				Object selectedValue = JOptionPane.showInputDialog(this,
						"Open what kind of file? \n",null,
				        JOptionPane.PLAIN_MESSAGE, null,  possibleOptions, possibleOptions[0]);
				FileFilter filter = null;
				String fileName = null;
				if (selectedValue == null) { // cancel		
					return;
				//}
				//else if ( ((String)selectedValue).endsWith("lock file")) {
				//	filter = new LockFilter();
				} else if ( ((String)selectedValue).endsWith("text file")) {
					filter = new TxtFilter();
				} else if ( ((String)selectedValue).endsWith("text format")) {
					filter = new RtfFilter();
				} 		
				final JFileChooser fc = new JFileChooser();       
		        fc.setFileFilter( filter );
				int returnVal = fc.showOpenDialog(null);
			    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
			    	fileName = fc.getSelectedFile().getPath();
			    } else { // cancel option
			    	return;
			    }		

			    if (fileName.endsWith(".txt")) {
			    	byte[] textBytes = ReadResources.readExternFile(fileName);
			    	char[] textChars = Converter.bytes2chars(textBytes);

					LockFrameEditor.textPane.setText(new String(textChars));
			    	Zeroizer.zero(textChars);
			    } else  { // rtf
			    	//System.out.println("fileName: " + fileName );
					RTFEditorKit kit = new RTFEditorKit();	
					LockFrameEditor.textPane.setEditorKit(kit);
	                FileInputStream in; 
	                try {
	                    in = new FileInputStream(fileName ); 
	                    kit.read(in, LockFrameEditor.textPane.getDocument(), 0); 
	                    in.close();
	                } catch (FileNotFoundException e) {
	                	System.err.println("EditorLockFrame Menu actionPerformed: " + e);
	                } catch (IOException e){
	                	System.err.println("EditorLockFrame Menu actionPerformed: " + e);
	                } catch (BadLocationException e){
	                	System.err.println("EditorLockFrame Menu actionPerformed: " + e);
	                }   
			    } 	 
			}
		} else if (command.equals("randomPsw")) {
			PeaSettings.getPswGenerator().setVisible(true);

		} else if (command.equals("psw")) {
			String[] singleFileName = { PswDialogBase.getEncryptedFileName() };
			PswDialogEditor.getLockFrame().changePassword(PswDialogEditor.getLockFrame().getPlainBytes(), singleFileName);

		} else if (command.equals("quit")) {
			
			if (LockFrameEditor.isDocChangeUnsaved() == true) {

				int n = JOptionPane.showConfirmDialog(
					    this,
					    "Save changes?",
					    "Unsaved Changes",
					    JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					byte[] plainBytes = PswDialogEditor.getLockFrame().getPlainBytes();
					LockFrameEditor.saveContent(null, plainBytes, PswDialogBase.getEncryptedFileName());
				}
			}

			StyledDocument doc = LockFrameEditor.textPane.getStyledDocument();
			try {
				doc.remove(0, doc.getLength() );
			} catch (BadLocationException e) {
				System.err.println("LockFrameEditor: " + e);
				e.printStackTrace();
			}			
			System.exit(0);	
		} else if (command.equals("undo")) {
			if (LockFrameEditor.manager.canUndo()){
				LockFrameEditor.manager.undo();
			}
		} else if (command.equals("redo")) {
			if (LockFrameEditor.manager.canRedo()){
				LockFrameEditor.manager.redo();
			}
		} else if (command.equals("cut")) {
			LockFrameEditor.textPane.cut();
		} else if (command.equals("copy")) {
			LockFrameEditor.textPane.copy();
		} else if (command.equals("paste")) {
			LockFrameEditor.textPane.paste();
		} else if (command.equals("selectAll")) {
			LockFrameEditor.textPane.selectAll();
		} else if (command.equals("about")) {
			JOptionPane.showMessageDialog(
					PswDialogView.getView(),
					"Peafactory\n test version 0.0\neditor pea", 
					null, JOptionPane.PLAIN_MESSAGE);
		} else if (command.equals("home")) {
			JOptionPane.showMessageDialog(
					PswDialogView.getView(),
					"Home: \n eck.cologne/peafactory.", 
					null, JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	//--------------------------------
	// inner classes
	//
	public class TxtFilter extends FileFilter  {
        public boolean accept( File f ) {
             return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
        }	    
        public String getDescription() {
            return "text" + " (*.txt)";
        }	    		
    };
	public class RtfFilter extends FileFilter  {
        public boolean accept( File f ) {
             return f.isDirectory() || f.getName().toLowerCase().endsWith(".rtf");
        }	    
        public String getDescription() {
            return "rich text format" + " (*.rtf)";
        }	    		
    };
/*	public class LockFilter extends FileFilter  {
        public boolean accept( File f ) {
             return f.isDirectory() || f.getName().toLowerCase().endsWith(".lock");
        }	    
        public String getDescription() {
            return "encrypted text" + " (*.lock)";
        }	    		
    }; */
}
