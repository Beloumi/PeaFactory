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
 * Button to prevent hardware key logging and to hamper software key logging
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.border.LineBorder;

//import cologne.eck.peafactory.tools.MouseRandomCollector;


@SuppressWarnings("serial")
class KeyButton extends JButton implements MouseListener {
	
/*	// this is for the strategy "hiding mouse cursor"
	private static final BufferedImage img = new BufferedImage(1, 1,
		        BufferedImage.TYPE_INT_ARGB);
	private static final Cursor zeroCursor = Toolkit.getDefaultToolkit().createCustomCursor(
			img , new Point(0,0), "zeroCursor"); 	 
*/	
	// the current default cursor 
	private static final Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    public KeyButton() { // avoid any displaying of events

    	super.setContentAreaFilled(false);      
    	
		this.setRolloverEnabled(false);
		this.setFocusPainted(false);
		this.setBorder(new LineBorder(Color.GRAY, 1));
		this.addMouseListener(this);
		
		//this.addMouseMotionListener(new MouseRandomCollector() );
    }

    public KeyButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
        
		this.setRolloverEnabled(false);
		this.setFocusPainted(false);
		this.setBorder(new LineBorder(Color.GRAY, 1));
		this.addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) { // use just a simple white background
        if (getModel().isPressed()) {
            g.setColor(Color.WHITE);
        } else if (getModel().isRollover()) {
            g.setColor(Color.WHITE);
        } else {
            g.setColor(Color.WHITE);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

	@Override
	public void mouseClicked(MouseEvent mc) {}
	@Override
	public void mouseEntered(MouseEvent e) {
		// Blinking with swingTimer?
	}
	@Override
	public void mouseExited(MouseEvent e) {
		this.setCursor(defaultCursor);
	}
	@Override
	public void mousePressed(MouseEvent e) {
/*		
		// Variant 1: hide cursor - against screenshots of whole screen
		this.setCursor(zeroCursor);				
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		this.setCursor(defaultCursor);
*/	
		
		// Variant 2: cover the area of the button - against 50*50 screenshots of buttons		
		int size = 64;		
		BufferedImage img = new BufferedImage(size, size,// win max: 32 * 32...
		        BufferedImage.TYPE_BYTE_INDEXED);
		Graphics2D graph = img.createGraphics();
		graph.setColor(Color.WHITE);//LIGHT_GRAY);
		graph.fill(new Rectangle(0, 0, size, size));
		graph.dispose();
	 
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Cursor c = null;
		try {
			c = toolkit.createCustomCursor(
	 			img , new Point(size / 2, size / 2), "img");   
		} catch (IndexOutOfBoundsException ioobe){ // Windows: max 32 * 32
			c = toolkit.createCustomCursor(
		 			img , new Point(size / 4, size / 4), "img");  			
		}
	 	this.setCursor((c));
	 	
		try { // cover only 0.1 second
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		this.setCursor(defaultCursor);
	 	
	}
	@Override
	public void mouseReleased(MouseEvent e) {}
}
