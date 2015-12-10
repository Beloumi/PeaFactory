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
 * A simple keyboard (US layout) to hamper key logging. 
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.Random;



import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import cologne.eck.peafactory.tools.Zeroizer;




@SuppressWarnings("serial")
public class Keyboard extends JDialog implements ActionListener {
	
	
	private char[][] enTable = {
		{ '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+' },// backspace <-
		{'\'', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '=' },
		
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '\u007B' , '\u007D' , '|'},
		{'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '[', ']', '\\'},
		
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ':'},
		{'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';'},//enter
		
		{' ', ' ', ' ', ' ', ' ', ' ', ' ', '<', '>', '?'},
		{'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/'},//shift left, '<', shift right
	};
	
	private boolean shiftEnabled = false;
	
	private static char[] password = new char[256];
	int index = 0;
	
	private static JPanel topPanel;
	private static Color bgColor = new Color(234, 234, 234);
	private static Color shiftColor = new Color(200,200,200);
	
	public Keyboard(JDialog owner) {
		
		this.setTitle( "Keyboard" );
		
		this.setIconImage(PswDialogView.getImage() );
		this.setAlwaysOnTop(true);

		topPanel = (JPanel) this.getContentPane();
		topPanel.setBackground(bgColor);
		
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		
		JPanel firstPanel = new JPanel();
		firstPanel.setBackground(bgColor);
		firstPanel.setLayout(new BoxLayout(firstPanel, BoxLayout.X_AXIS));
		topPanel.add(firstPanel);
		for (int i = 0; i < enTable[0].length; i++) {
			KeyButton firstLineButton = new KeyButton();			
			firstLineButton.setText("<html>" + enTable[0][i] + "<br/>" + enTable[1][i] + "<html>");
			firstLineButton.addActionListener(this);
			firstLineButton.setActionCommand("" + enTable[1][i] + i + 1);
			firstLineButton.setBackground(Color.WHITE);
			firstLineButton.setPreferredSize(new Dimension(50, 50));
			//firstLineButton.setMaximumSize(new Dimension(40, 40));
			firstPanel.add(firstLineButton);
		}
		firstPanel.add(Box.createHorizontalStrut(5));

		JButton backspace = new KeyButton("<--");
		backspace.addActionListener(this);
		backspace.setActionCommand("backspace");
		backspace.setMaximumSize(new Dimension(60, 30));
		backspace.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		firstPanel.add(backspace);
		firstPanel.add(Box.createHorizontalGlue());
		
		JPanel secondPanel = new JPanel();
		secondPanel.setBackground(bgColor);
		secondPanel.setLayout(new BoxLayout(secondPanel, BoxLayout.X_AXIS));
		topPanel.add(secondPanel);
		secondPanel.add(Box.createHorizontalStrut(15));
		for (int i = 0; i < enTable[2].length; i++) {
			JButton secondLineButton = new KeyButton();
			secondLineButton.setText("<html>" + enTable[2][i] + "<br/>" + enTable[3][i] + "<html>");
			secondLineButton.addActionListener(this);
			secondLineButton.setActionCommand("" + enTable[3][i] + i + 3);
			secondLineButton.setMargin(new Insets(5,5,5,5));
			secondPanel.add(secondLineButton);
		}
		secondPanel.add(Box.createHorizontalGlue() );
		
		JPanel thirdPanel = new JPanel();
		thirdPanel.setBackground(bgColor);
		thirdPanel.setLayout(new BoxLayout(thirdPanel, BoxLayout.X_AXIS));
		topPanel.add(thirdPanel);
		thirdPanel.add(Box.createHorizontalStrut(30));
		for (int i = 0; i < enTable[4].length; i++) {
			JButton thirdLineButton = new KeyButton();
			thirdLineButton.setText("<html>" + enTable[4][i] + "<br/>" + enTable[5][i] + "<html>");
			thirdLineButton.addActionListener(this);
			thirdLineButton.setActionCommand("" + enTable[5][i] + i + 5);
			thirdPanel.add(thirdLineButton);
		}
		thirdPanel.add(Box.createHorizontalStrut(5));
		JButton enter = new KeyButton("enter");
		enter.addActionListener(this);
		enter.setActionCommand("enter");
		enter.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		enter.setMargin(new Insets( 5, 10, 5, 40));
		enter.setMaximumSize(new Dimension(60, 30));
		thirdPanel.add(enter);
		thirdPanel.add(Box.createHorizontalGlue() );
		
		JPanel fourthPanel = new JPanel();
		fourthPanel.setBackground(bgColor);
		fourthPanel.setLayout(new BoxLayout(fourthPanel, BoxLayout.X_AXIS));
		topPanel.add(fourthPanel);
		fourthPanel.add(Box.createHorizontalStrut(45));
		JButton shift = new KeyButton("shift");
		shift.addActionListener(this);
		shift.setActionCommand("shift");
		shift.setMargin(new Insets( 10, 1, 5, 1));
		shift.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		shift.setMaximumSize(new Dimension(60, 30));
		fourthPanel.add(shift);
		fourthPanel.add(Box.createHorizontalStrut(5));
		for (int i = 0; i < enTable[6].length; i++) {
			JButton fourthLineButton = new KeyButton();
			fourthLineButton.setText("<html>" + enTable[6][i] + "<br/>" + enTable[7][i] + "<html>");
			fourthLineButton.addActionListener(this);
			fourthLineButton.setActionCommand("" + enTable[7][i] + i + 7);
			fourthPanel.add(fourthLineButton);
		}
		// extra button: can not be used in html
		JButton fourthLineButton = new KeyButton();
		fourthLineButton.setText("  <  ");
		fourthLineButton.addActionListener(this);
		fourthLineButton.setActionCommand("<");
		fourthLineButton.setMargin(new Insets(10,5,10,5));
		fourthLineButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		fourthLineButton.setMaximumSize(new Dimension(40,32));
			//	fourthPanel.getComponent(2).getSize().getHeight()));
		fourthPanel.add(fourthLineButton);
		
		fourthPanel.add(Box.createHorizontalStrut(5));
		JButton shift2 = new KeyButton("shift");
		shift2.addActionListener(this);
		shift2.setActionCommand("shift");
		shift2.setMargin(new Insets( 10, 1, 5, 1));
		shift2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		shift2.setMaximumSize(new Dimension(60, 30));
		fourthPanel.add(shift2);
		fourthPanel.add(Box.createHorizontalGlue() );

		
		JPanel fifthPanel = new JPanel();
		fifthPanel.setBackground(bgColor);
		fifthPanel.setLayout(new BoxLayout(fifthPanel, BoxLayout.X_AXIS));
		topPanel.add(fifthPanel);
		
		JButton space = new KeyButton("space");
		space.addActionListener(this);
		space.setActionCommand("space");
		space.setMaximumSize(new Dimension (400, 40));
		space.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		fifthPanel.add(space);

		
/*		// this makes it a bit harder to recover the password from mouse position
		Random rand = new Random();
		int push1 = rand.nextInt(90);
		int push2 = rand.nextInt(90);
		int stretch1 = rand.nextInt(200);
		stretch1 -= 100; 
		int stretch2 = rand.nextInt(80);
		stretch2 -= 20;
		this.setLocation(100 - push1,100 - push2);
		this.pack();// needed to get preferredSize
		this.setSize((int)(this.getPreferredSize().getWidth() + stretch1), (int)(this.getPreferredSize().getHeight() + stretch2));
*/
		this.setSize((int)(this.getPreferredSize().getWidth() -100), (int)(this.getPreferredSize().getHeight() + 20));
	}
	
	private void setChar(char c) {
		if (c == '\n'){
			char[] psw = new char[index];
			System.arraycopy(password, 0, psw, 0, index);
			Zeroizer.zero(password);
			PswDialogView.setPassword(psw);
			index = 0;
			this.setVisible(false);
			this.dispose();
		}
		password[index] = c;
		index++;		
	}
	private void deleteLastChar() {
		if (index > 0) {
			password[index - 1] = 0xFF;
			index--;
		}
	}

	@Override
	public void actionPerformed(ActionEvent ape) {
		
		String command = ape.getActionCommand();
		
		if (command.equals("shift")){
			if (shiftEnabled == false) {
				shiftEnabled = true;
				topPanel.setBackground(shiftColor);
			} else {
				shiftEnabled = false;
				topPanel.setBackground(bgColor);
			}
		} else if (command.equals("space")){
			setChar(' ');
		} else if (command.equals("enter")){
			setChar('\n');
		} else if (command.equals("backspace")) {
			deleteLastChar();
		} else if (command.equals("<")) {
			setChar('<');
		} else { // single char
			//=========== TEST ==================
			//System.out.println( command.charAt(0));
			if (shiftEnabled == false) {
				setChar(command.charAt(0));
			} else { // command = char + index + array
				int arrayIndex = Integer.parseInt("" + command.charAt(command.length() - 1));
				int charIndex = Integer.parseInt(command.substring(1, command.length() - 1));
				if (enTable[arrayIndex - 1][charIndex] == ' ') {
					setChar( Character.toUpperCase(command.charAt(0)) );
				} else {
					setChar(enTable[arrayIndex - 1][charIndex]);
				}
			}
		}
	}

	//=========== TEST ==================
	public static void main(String[] args) {
		Keyboard kb = new Keyboard(null);
		kb.setVisible(true);
	} 
}
