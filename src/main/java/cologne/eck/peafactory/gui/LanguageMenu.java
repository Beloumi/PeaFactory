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
 * Language support for PeaFactory, not for peas.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JSeparator;

import cologne.eck.peafactory.PeaFactory;


@SuppressWarnings({ "rawtypes", "serial" })
public class LanguageMenu extends JComboBox implements ActionListener {
	
	@SuppressWarnings("unchecked")
	public LanguageMenu() {
	
		final String[] languageStrings = { "de", "en" };
		
		//set default language first
		this.insertItemAt(PeaFactory.getLanguage(), 0);
		int index = 1;
		for (int i = 0; i < languageStrings.length; i++) {
			if (! PeaFactory.getLanguage().equals(languageStrings[i]) ) {
				this.insertItemAt(languageStrings[i], index++);
				this.add(new JSeparator());
			}
		}
		this.addActionListener(this);
		this.setActionCommand("language");
		
		this.setPreferredSize(new Dimension(20, 30));
		this.setMaximumSize(new Dimension(20, 30));
		this.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		this.setBackground(Color.WHITE);
	}
	

	@Override
	public void actionPerformed(ActionEvent ape) {
		String com = ape.getActionCommand();
		if ( com.equals("language")) {

			JComboBox cb = (JComboBox)ape.getSource();
	        String selectedLanguage = (String)cb.getSelectedItem();
	        
	        PeaFactory.setI18n(selectedLanguage);
		}
	}
}
