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
 * Dialog to display content from files. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import cologne.eck.peafactory.PeaFactory;
import cologne.eck.peafactory.peas.gui.PswDialogView;
import cologne.eck.peafactory.tools.ReadResources;

@SuppressWarnings("serial")
public class InfoDialog extends JDialog implements ActionListener {
	
	public InfoDialog(String title, String content, String fileName) {
		
		super(PeaFactory.getFrame());
		
		if (title == null) {
		this.setTitle("Information");
		} else {
			this.setTitle(title);
		}
		this.setIconImage(MainView.getImage());

	    JPanel basePanel = new JPanel();//(JPanel) this.getContentPane();
	   
	    basePanel.setLayout(new BoxLayout(basePanel, BoxLayout.Y_AXIS));
	    basePanel.setBorder(new LineBorder(Color.BLACK));
	    basePanel.setBackground( PswDialogView.getPeaColor() );
	    
	
	    JTextPane textPane = new JTextPane();	    
	    textPane.setBackground( PswDialogView.getPeaColor() );
	    textPane.setBorder(new EmptyBorder(10,10,10,10));
	    if (content != null) {
	    	textPane.setContentType("text/plain");
	    } else {
	    	
	    	String language = PeaFactory.getLanguage();
	    	fileName = "config" + File.separator + "infos" + File.separator + fileName + "_" + language + ".html";
	    	if (! new File(fileName).exists() ) {
	    		System.err.println("Missing file " + fileName + " for language " + language);
	    		return;
	    	}
	    	content = new String( ReadResources.readExternFile(fileName), PeaFactory.getCharset());
	    	textPane.setContentType("text/html"); 
	    }
	    textPane.setText(content);
	    basePanel.add(textPane);
	    
	    JButton okButton = new JButton("ok");
	    okButton.addActionListener(this);

	    basePanel.add(okButton);

	    this.setLocation(100,100);

	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    if (basePanel.getPreferredSize().getWidth() > screenSize.getWidth()
	    		|| basePanel.getPreferredSize().getHeight() > screenSize.getHeight()){
		    JScrollPane scrollPane = new JScrollPane(basePanel);
		    this.add(scrollPane);

		    scrollPane.setPreferredSize(screenSize );
		    System.out.println(screenSize);
		    this.setSize(new Dimension((int)screenSize.getWidth()/10*6, (int)screenSize.getHeight()/10*9 -100));
	    	
	    } else {
	    	this.add(basePanel);
	    	this.pack();
	    }
	    this.setVisible(true);
	}

/*	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//InfoDialog info = new InfoDialog("Titel", "Text \n text text", null, new Point(200, 300));
		//InfoDialog info = new InfoDialog(null,  null, "config" + File.separator + "infos" + File.separator + "notes.html",null);
		
		//info.setVisible(true);
	}*/

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.dispose();		
	}	
}
