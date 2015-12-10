package cologne.eck.peafactory.peas.file_pea;
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
 * Generation of the file pea, 
 * not used in the pea itself  
 */


import java.io.File;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.crypto.CipherStuff;
import cologne.eck.peafactory.gui.*;
import cologne.eck.peafactory.peagen.DataType;
import cologne.eck.peafactory.peagen.JarStuff;
import cologne.eck.peafactory.tools.WriteResources;




public class FileType extends DataType {
	
	private static byte[] fileNamesAsBytes = null;

	@Override
	public void processData( byte[] keyMaterial) {
		
		if (MainView.isBlankPea() == false) {
		
			String[] fileNames = MainView.getFilePanel().getSelectedFileNames();

			FileTypePanel filePanel = MainView.getFilePanel();
			
			filePanel.startProgressTask();// starts if execution > 1 second
			CipherStuff.getCipherMode().encryptFiles(fileNames, keyMaterial, false, filePanel);
			filePanel.closeProgressTask();// close automatically
			
		} else {
			
			byte[] cipherBytes = "uninitializedFile".getBytes(PeaFactory.getCharset());
			
			String dirName = JarStuff.getJarFileName().substring(0, JarStuff.getJarFileName().length() - 4); // extract ".jar"

			// store cipherBytes beside jar archive:
			WriteResources.write(cipherBytes, "text.lock", "peas" + File.separator + dirName + File.separator + "resources");
		}
	}

	@Override
	public String getDescription() {
		return PeaFactory.getLanguagesBundle().getString("file_description");
	}

	@Override
	public String getTypeName() {
		return "file";
	}

	@Override
	public byte[] getData() {
		return fileNamesAsBytes;
	}

	@Override
	public void setData(byte[] data) {
		fileNamesAsBytes = data;		
	}
}
