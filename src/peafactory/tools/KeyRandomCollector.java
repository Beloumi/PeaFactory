package cologne.eck.peafactory.tools;

/*
 * Collects time values from key strokes
 * 
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
 * This class collects random values from key stroke events. 
 * 
 * 	Values are not the keys itself but the time between typing. 
 * 	For every key event a thread of EntropyPool is updated with this time value. 
 * 
 */

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;



public class KeyRandomCollector extends KeyAdapter{
	
	private transient long start;

	public KeyRandomCollector() {
		start = System.currentTimeMillis();
	}
	
	@Override
	public void keyPressed(KeyEvent kpe) {

		EntropyPool.getInstance().updateThread(System.currentTimeMillis() - start);
		
		// for tests only:
	/*	System.out.println("\n keyPressed: " 
				+ (System.currentTimeMillis() - start) 
				+ " event: " + eventCounter++); */
		start = System.currentTimeMillis(); 		
	}
}
