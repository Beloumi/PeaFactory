package cologne.eck.peafactory.tools;


/*
 * Collects values from mouse events
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
 * This class collects random values from mouse motion events. 
 * 
 * 	Values are generated from 
 * 		- the time between events 
 * 		- XOR the addition of X position and Y position
 *  Note: Because the time values itself do no provide good random values, the position is added
 *  
 * 	For every mouse motion event a thread of EntropyPool is updated with this value. 
 */



import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;



public class MouseRandomCollector extends MouseMotionAdapter{
	
	private transient long start; // time start point
	private transient int sum = 0; // sum of very short times
	

	public MouseRandomCollector() {
		start = System.currentTimeMillis(); 
	}
	
	@Override
	public void mouseMoved(MouseEvent mme) {
		sum += System.currentTimeMillis() - start;
		if ( sum > 16) { 	// do not start threads for very short events
							// because these are often equal
		

			EntropyPool.getInstance().updateThread(
					(System.currentTimeMillis() - start) ^ (mme.getX() + mme.getY()) );
			// for tests only:
			/*			System.out.println("\n mouseMoved: " 
					+ ((System.currentTimeMillis() - start) ^ (mme.getX() + mme.getY()))
					+ "  event: " + eventCounter++);*/
			sum = 0;
			start = System.currentTimeMillis();

		} else {
			start = System.currentTimeMillis();
		}
	}


	@Override
	public void mouseDragged(MouseEvent mde) {
		sum += System.currentTimeMillis() - start;
		if ( sum > 16) { 	// do not start threads for very short events
							// because these are often equal		

			EntropyPool.getInstance().updateThread(
					(System.currentTimeMillis() - start) ^ (mde.getX() + mde.getY()) );
			// for tests only:
			/*			System.out.println("\n mouseMoved: " 
					+ ((System.currentTimeMillis() - start) ^ (mde.getX() + mde.getY()))
					+ "  event: " + eventCounter++);*/
			sum = 0;
			start = System.currentTimeMillis();

		} else {
			start = System.currentTimeMillis();
		}
	}
}
