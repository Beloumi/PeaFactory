package cologne.eck.peafactory.peas.gui;

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
 * CharTable provides UTF-8 characters for JPasswordFields. 
 * The intention behind to increase the entropy of passwords and to hinder key loggers. 
 */

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import cologne.eck.peafactory.tools.Zeroizer;



@SuppressWarnings("serial")
public class CharTable extends JDialog implements ActionListener {
	
	// avoiding not correctly displayed characters makes the current 
	// lastUtf a bit unpredictable for the previous button... 
	private ArrayList<Integer> startPoints;// = new ArrayList<Integer>(8);
		
	// current UTF character value
	private int lastUtf = 32;
	// current index of table
	private int tableNumber = 1;
	// the tables' contentPane
	private JPanel contentPane = null;
	// the password field, this class is used for
	private JPasswordField pswField = null;	

	
	public CharTable(Window owner, JPasswordField _pswField) {
		
		super(owner);
		
		pswField = _pswField;		
		
		this.setTitle("Table " + tableNumber);
		this.setAlwaysOnTop(true);
		this.setModal(false);
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		
		startPoints = new ArrayList<Integer>(8);
		startPoints.add(new Integer(lastUtf));
		
		JPanel indexPanel = new JPanel();
		indexPanel.setLayout(new BoxLayout(indexPanel, BoxLayout.X_AXIS));
		JLabel zeroLabel = new JLabel("  ");
		zeroLabel.setMaximumSize(new Dimension(35, 25));
		indexPanel.add(zeroLabel);
		
		// first line
		for (int i = 1; i <= 12; i++) {
			JLabel label = new JLabel("" + i);
			label.setMaximumSize(new Dimension(25, 25));
			indexPanel.add(label);
		}
		contentPane.add(indexPanel);
		
		// second line
		JPanel secondPanel = new JPanel();
		secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.X_AXIS));
		JLabel indexLabel = new JLabel("" + 1);
		indexLabel.setMaximumSize(new Dimension(25, 25));
		secondPanel.add(indexLabel);
		
		JButton previousButton = new JButton("previous");
		previousButton.setMaximumSize(new Dimension(75,25));
		previousButton.setMargin(new Insets(1,1,1,1));
		previousButton.addActionListener(this);
		previousButton.setActionCommand( "previous");
		secondPanel.add(previousButton);
		
		for (int i = 0; i < 9; i++) {
			KeyButton b = new KeyButton();
			b.setMaximumSize(new Dimension(25,25));
			b.addActionListener(this);
			b.setText( Character.toString( (char) (lastUtf + i) ) ); 
			b.setActionCommand( Character.toString( (char) (lastUtf + i) ));
			secondPanel.add(b);
		}
		lastUtf += 9;
		contentPane.add(secondPanel);
		
		// 10 middle lines
		for (int i = 0; i < 11; i++) {
			JPanel panel = new JPanel();
			
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			JLabel label = new JLabel("" + (i + 2));
			label.setMaximumSize(new Dimension(25, 25));
			panel.add(label);
			for(int j = 0; j < 12; j++) {
				KeyButton b = new KeyButton();
				b.setMaximumSize(new Dimension(25,25));
				b.addActionListener(this);
				if(i == 7 &&  j == 2) {
					lastUtf += 34;//avoid control and no break space
				}
				b.setText( Character.toString( (char) (lastUtf + j) ) ); 
				b.setActionCommand( Character.toString( (char) (lastUtf + j) ));
				panel.add(b);
			}
			lastUtf += 12;
			contentPane.add(panel);
		}
		
		// last line
		JPanel lastPanel = new JPanel();
		lastPanel.setLayout(new BoxLayout(lastPanel, BoxLayout.X_AXIS));
		JLabel lastIndexLabel = new JLabel("" + 12);
		lastIndexLabel.setMaximumSize(new Dimension(25, 25));
		lastPanel.add(lastIndexLabel);
		
		for (int i = 0; i < 9; i++) {
			KeyButton b = new KeyButton();
			b.setMaximumSize(new Dimension(25,25));
			b.addActionListener(this);
			b.setText( Character.toString( (char) (lastUtf + i) ) ); 
			b.setActionCommand( Character.toString( (char) (lastUtf + i) ));
			lastPanel.add(b);
		}
		lastUtf += 9;
		
		startPoints.add(new Integer(lastUtf));
		
		JButton nextButton = new JButton("next");
		nextButton.setMaximumSize(new Dimension(75,25));
		nextButton.setMargin(new Insets(1,1,1,1));
		nextButton.addActionListener(this);
		nextButton.setActionCommand( "next");
		lastPanel.add(nextButton);
		
		contentPane.add(lastPanel);

		//this.setSize(new Dimension(335, 375));//optimal openSuse LXDE
		this.setSize(new Dimension(360, 390));
		if (pswField != null) {
			this.setLocation((int) pswField.getLocation().getX() + 100, 
					(int) pswField.getLocation().getY() + 100);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		String com = ape.getActionCommand();
		//=========== TEST ============= works only on Java 1.7
//		System.out.println(com + "   " + (Character.getName(com.charAt(0))));

		
		if (com.equals("next")){

			this.setTitle("Table " + ++tableNumber);

			// first line
			JPanel p1 = (JPanel) contentPane.getComponent(1);
			for (int i = 0; i < 9; i++) {
				//if ( (Character.getName(lastUtf + i) == null)) {// works only on Java 1.7
				if ( (checkChar(lastUtf + i) == false)) {
					lastUtf++;
					i--;
					continue;
				}	
				KeyButton b = (KeyButton) p1.getComponent(i+2);
				b.setText( Character.toString( (char) (lastUtf + i) ) ); 
				b.setActionCommand( Character.toString( (char) (lastUtf + i) ));
			}
			lastUtf += 9;
			
			// 10 middle lines
			for (int i = 0; i < 11; i++) {
				JPanel p = (JPanel) contentPane.getComponent(i+2);				
				for (int j = 0; j <= 11; j++) {
					if ( (checkChar(lastUtf + j) == false)) {
						lastUtf++;
						j--;
						continue;
					}	
					KeyButton b = (KeyButton) p.getComponent(j+1);
					b.setText( Character.toString( (char) (lastUtf + j) ) ); 
					b.setActionCommand( Character.toString( (char) (lastUtf + j) ));
				}		
				lastUtf += 12;
			}
			
			// last line
			JPanel p12 = (JPanel) contentPane.getComponent(13);
			for (int i = 0; i <= 8; i++) {
				if ( (checkChar(lastUtf + i) == false)) {
					lastUtf++;
					i--;
					continue;
				}	
				KeyButton b = (KeyButton) p12.getComponent(i+1);
				b.setText( Character.toString( (char) (lastUtf + i) ) ); 
				b.setActionCommand( Character.toString( (char) (lastUtf + i) ));
			}
			lastUtf += 9;
			startPoints.add(new Integer(lastUtf));

		} else if (com.equals("previous")){
			if (tableNumber == 1) {
				// do nothing
			}
			else if(tableNumber == 2){ // -> new instance of CharTable:
				
				Point loc = this.getLocation();
				Window win = this.getOwner();
				this.dispose();
				CharTable ct = new CharTable(win, pswField);
				ct.setLocation(loc);
				ct.setVisible(true);
				
			} else {
			
				this.setTitle("Table " + --tableNumber);

				lastUtf = startPoints.get(startPoints.size() - 3);
				startPoints.remove(startPoints.size() - 1);
				startPoints.trimToSize();
				// first line
				JPanel p1 = (JPanel) contentPane.getComponent(1);
				for (int i = 0; i < 9; i++) {
					if ( (checkChar(lastUtf + i) == false)) {
						lastUtf++;
						i--;
						continue;
					}	
					KeyButton b = (KeyButton) p1.getComponent(i+2);
					b.setText( Character.toString( (char) (lastUtf + i) ) ); 
					b.setActionCommand( Character.toString( (char) (lastUtf + i) ));
				}
				lastUtf += 9;
				
				// 10 middle lines
				for (int i = 0; i < 11; i++) {
					JPanel p = (JPanel) contentPane.getComponent(i+2);				
					for (int j = 0; j <= 11; j++) {
						if ( (checkChar(lastUtf + j) == false)) {
							lastUtf++;
							j--;
							continue;
						}	
						KeyButton b = (KeyButton) p.getComponent(j+1);
						b.setText( Character.toString( (char) (lastUtf + j) ) ); 
						b.setActionCommand( Character.toString( (char) (lastUtf + j) ));
					}		
					lastUtf += 12;
				}
				
				// last line
				JPanel p12 = (JPanel) contentPane.getComponent(13);
				for (int i = 0; i <= 8; i++) {
					if ( (checkChar(lastUtf + i) == false)) {
						lastUtf++;
						i--;
						continue;
					}	
					KeyButton b = (KeyButton) p12.getComponent(i+1);
					b.setText( Character.toString( (char) (lastUtf + i) ) ); 
					b.setActionCommand( Character.toString( (char) (lastUtf + i) ));
				}
				lastUtf += 9;
			}

		} else { // one of the character buttons: append to chars in password field
			
			if (pswField != null) {
				char[] oldPsw = pswField.getPassword();
				char[] tmp = new char[oldPsw.length + 1];
				System.arraycopy(oldPsw,  0,  tmp,  0,  oldPsw.length);
				Zeroizer.zero(oldPsw);
				tmp[tmp.length-1] = com.charAt(0);
				
				pswField.setText(new String(tmp));
				Zeroizer.zero(tmp);
			} else {
	//			System.out.println("Type: " + Character.getType(com.charAt(0)));
				System.err.println("Password field for table was closed.");
			}
		}
	}
	private boolean checkChar(int utf) {
		
		int type = Character.getType(utf);
	//	System.out.println(type);
		
		if ( // avoid characters which are not displayed correctly
				type == 1 || type == 2 
	//			|| type == 24 //OTHER_PUNCTUATION
				|| type == 25 // MATH_SYMBOL  
	//			|| type == 26 // CURRENCY_SYMBOL
				|| type == 28 // OTHER_SYMBOL
				
				) {
			//System.out.println(Character.getType(utf));
	    	return true;

	    } else {
//			(! (Character.isValidCodePoint(utf)) )
/*	
			|| Character.getType(utf) == 0 // UNASSIGNED + MIN_VALUE + MIN_CODE_POINT
			|| Character.getType(utf) == 27 // MODIFIER_SYMBOL
			|| Character.getType(utf) == 6 // NON_SPACING_MARK + DIRECTIONALITY_ARABIC_NUMBER
			|| Character.getType(utf) == 5  // OTHER_LETTER + DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR
			|| Character.getType(utf) == 15 // CONTROL + DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE + 
			|| Character.getType(utf) == 16  // SIZE + FORMAT + DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING + 	
*/

	    	return false;
	    }	
	}


	//=========== TEST =============
/*	public static void main(String[] args) {
		CharTable ct = new CharTable(null,null);
		ct.setVisible(true);
	} */
}
