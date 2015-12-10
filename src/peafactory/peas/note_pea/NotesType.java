package cologne.eck.peafactory.peas.note_pea;

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
 * Generation of the note pea. 
 */

import java.io.File;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.gui.*;
import cologne.eck.peafactory.peagen.DataType;
import cologne.eck.peafactory.peagen.JarStuff;
import cologne.eck.peafactory.tools.Converter;
import cologne.eck.peafactory.tools.WriteResources;
import cologne.eck.peafactory.tools.Zeroizer;

public final class NotesType extends DataType {

	private static byte[] textBytes = null;

	@Override
	public void processData(byte[] keyMaterial) {
		byte[] plainBytes = null;
		

		char[] text = TextTypePanel.getText();
		TextTypePanel.resetText();
		plainBytes = Converter.chars2bytes(text);
		if (plainBytes.length > 0){
			
			Zeroizer.zero(text);
		}
		
		byte[] cipherBytes = null;
		if (MainView.isBlankPea() == true){
			// String that indicates uninitialized file
			cipherBytes = "uninitializedFile".getBytes(PeaFactory.getCharset());
		} else {
			// encrypted content
			cipherBytes = CipherStuff.getInstance().encrypt(plainBytes, keyMaterial, false);
		}
				
		
		if (MainView.getOpenedFileName()  == null) {
			// get name of directory containing jar archive and resources
			String dirName = JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() - 4); // extract ".jar"

			// store cipherBytes beside jar archive:
			WriteResources.write(cipherBytes, "text.lock", "peas" + File.separator + dirName + File.separator + "resources");
			Zeroizer.zero(cipherBytes);
		} else {
			// overwrite external file with cipherBytes:
			WriteResources.write(cipherBytes,  MainView.getOpenedFileName() , null);		
		}		
	}

	@Override
	public String getDescription() {
		return PeaFactory.getLanguagesBundle().getString("notes_description");
	}

	@Override
	public String getTypeName() {
		return "notes";
	}

	@Override
	public byte[] getData() {
		return textBytes;
	}

	@Override
	public void setData(byte[] data) {
		textBytes = data;		
	}
}
