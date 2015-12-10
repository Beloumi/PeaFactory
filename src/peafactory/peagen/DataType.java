package cologne.eck.peafactory.peagen;

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
 * Abstract class, parent class of type classes of peas. 
 */

import java.io.File;

import cologne.eck.peafactory.peas.file_pea.FileType;

/* Every child of DataType holds a static variable with its specific data type. 
 * 
 * Every child contains type specific functions. 
 */

public abstract class DataType {
	
	private static DataType dataType = new FileType();
		
	
	public abstract String getDescription();
	public abstract String getTypeName();
	
	public abstract void processData(byte[] keyMaterial);
	public abstract byte[] getData();
	public abstract void setData(byte[] data);
	

	
	// delete all files and directories in this directory
	protected static final void clearDirectory(File dir) {
	    for (File file: dir.listFiles()) {
	        if (file.isDirectory()) clearDirectory(file);
	        file.delete();
	    }
	}
	
	//====================================
	// Getter & Setter
	public final static DataType getCurrentType() { // used in Main
		return dataType;
	}
	public final static void setCurrentType(DataType _dataType) { // used in ProjectSelection
		dataType = _dataType;
	}
}
