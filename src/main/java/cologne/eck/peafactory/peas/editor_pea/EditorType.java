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
 * Class to generate the editor pea, not used in the pea itself.
 */


import java.io.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.rtf.RTFEditorKit;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.gui.*;
import cologne.eck.peafactory.peagen.DataType;
import cologne.eck.peafactory.peagen.JarStuff;
import cologne.eck.peafactory.tools.ReadResources;
import cologne.eck.peafactory.tools.WriteResources;


public final class EditorType extends DataType {
	
	private static byte[] serializedDoc = null;
	
	// used in TextTypePanel to display text from selected file
	public final static String readStringFromRTFFile(File file) {
		
		String text = "";		
		Document doc = new DefaultStyledDocument();
		
		// get text and store in doc
		RTFEditorKit kit = new RTFEditorKit();	
        FileInputStream in; 
        try {
            in = new FileInputStream(file.getAbsolutePath() ); 
            kit.read(in, doc, 0); 
            in.close();
        } catch (FileNotFoundException e) {
        	System.err.println("EditorType: " + e);
        	return null;
        } catch (IOException e){
        	System.err.println("EditorType: " + e);
        	return null;
        } catch (BadLocationException e){
        	System.err.println("EditorType: " + e);
        	return null;
        }   
        try {
        	text =  doc.getText(0, doc.getLength() );

		} catch (BadLocationException e1) {
			System.err.println("EditorType: " + e1);
			return null;
		}	 
		
		return text;
	}
		


	@Override
	public void processData(byte[] keyMaterial) {
		
		//-----------------------------
		// get plainBytes: 
		// 1. TextTypePanel - open file: read plainBytes from MainView.getOpenedFileName()
		// 2. TextTypePanel - new file: read plainBytes from text area
		// 3. TextTypePanel - copy from opened file: read plainBytes from serializedDoc
		// 4. TextTypePanel - no file: read plainBytes from text area
		byte[] plainBytes = null;
		
		if (MainView.getOpenedFileName() != null) { // use external file
			if (MainView.getOpenedFileName().endsWith(".rtf") || MainView.getOpenedFileName().endsWith(".RTF")) {
				// do not get text from text area but from file:
				plainBytes = ReadResources.readExternFile(MainView.getOpenedFileName());
				
				if (new String(plainBytes, PeaFactory.getCharset()).equals("\n")) { // new selected file
					// get text from text area 
					String text = new String(TextTypePanel.getText());

					TextTypePanel.resetText();
					DefaultStyledDocument doc = string2document(text);
					plainBytes = document2bytes(doc);
				}
			} else { // text file, content displayed in MainView:
				char[] textChars = TextTypePanel.getText();
				TextTypePanel.resetText();
				DefaultStyledDocument doc = string2document(new String(textChars));
				plainBytes = document2bytes(doc);
			}
		} else { // internal file
			
			if (serializedDoc != null) { // copy of external file (was set in MainView)
				
				plainBytes = serializedDoc;
				
			} else {
				char[] textChars = TextTypePanel.getText();
				TextTypePanel.resetText();
				DefaultStyledDocument doc = string2document(new String(textChars));
				plainBytes = document2bytes(doc);
			}
		}
        
        //byte[] cipherBytes = CipherStuff.getInstance().encrypt(plainBytes, keyMaterial, false);
		byte[] cipherBytes = null;
		if (MainView.isBlankPea() == true){
			// String that indicates uninitialized file
			cipherBytes = "uninitializedFile".getBytes(PeaFactory.getCharset());
		} else {
			// encrypted content
			cipherBytes = CipherStuff.getInstance().encrypt(plainBytes, keyMaterial, false);
		}
        
		
		if (MainView.getOpenedFileName() == null) {
			// get name of directory containing jar archive and resources
			String dirName = JarStuff.getJarFileName().substring(0, (JarStuff.getJarFileName().length() - 4)); // extract ".jar"

			// store cipherBytes beside jar archive
			WriteResources.write(cipherBytes, "text.lock", "peas" + File.separator + dirName + File.separator + "resources");

		} else {
			// store cipherBytes in external file
			WriteResources.write(cipherBytes, MainView.getOpenedFileName(), null);	
		}
	}
	
	private final DefaultStyledDocument string2document(String text) {
		DefaultStyledDocument doc = new DefaultStyledDocument();			

		SimpleAttributeSet set = new SimpleAttributeSet();
		try {
			doc.insertString(0, text, set);
		} catch (BadLocationException e) {
			System.err.println("EditorType: " + e);
			e.printStackTrace();
		}
		return doc;
	}
	private final byte[] document2bytes(DefaultStyledDocument doc) {
		byte[] result = null;

		// StyledDoc to bytes:
		RTFEditorKit kit = new RTFEditorKit();		
		
        ByteArrayOutputStream out; 
        try {
            out = new ByteArrayOutputStream(); 
            kit.write(out, doc, doc.getStartPosition().getOffset(), doc.getLength()); 
            out.close();
            result = out.toByteArray();            
        } catch (IOException e){
        	System.err.println("EditorType: " + e);
        } catch (BadLocationException e){
        	System.err.println("EditorType: " + e);
        }
        return result;
	}

	@Override
	public String getDescription() {	
		return PeaFactory.getLanguagesBundle().getString("editor_description");
	}
	@Override
	public String getTypeName() {
		return "editor";
	}
	@Override
	public byte[] getData() {
		return serializedDoc;
	}
	@Override
	public void setData(byte[] data) {
		serializedDoc = data;		
	}
}
