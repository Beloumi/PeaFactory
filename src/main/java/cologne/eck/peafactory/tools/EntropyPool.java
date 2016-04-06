package cologne.eck.peafactory.tools;

import java.util.Arrays;


/*
 * Collection of entropy from key strokes, 
 * mouse events and the java thread schedules. 
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
 * Collects a pool of entropy values to use as 
 * additional seeds for random number generators. 
 * 
 * This class holds a pool of 512 bytes, which are modified
 * several times by 64 simultaneously running threads 
 * computing various XorShift generators.
 * 
 * The randomness is based on the thread schedule (priority of
 * all threads is equal) - it should be unpredictable which
 * thread modifies which value of the pool -, and the seed
 * values (time between MouseMotionEvents (and positions) 
 * and KeyStrokeEvents). 
 */


public final class EntropyPool {
	
	
	/**
	 * Default value for quality. The EntropyPool must be stopped manually
	 * by a call to the function stopCollection(). 
	 */
	private EntropyPool(){
		pause = 10; // threads sleep 10 milliseconds after access
		maxAccess = Integer.MAX_VALUE;
		maxUpdates = Integer.MAX_VALUE;
	}
	
	/**
	 * Constructor with quality-performance-setting and time limitation
	 * 
	 * @param quality			Higher quality means more accesses to the pool per second, 
	 * 							and therefore more unpredictability of the access order, 
	 * 							but also means more CPU cost. Recommended values are
	 * 							from 1 to 25, default value is 10. 
	 * @param maximalAccesses	Limit of accesses to the pool. The collections stops 
	 * 							after maximalAccesses to the pool. Recommended values
	 * 							are from 8192 to 262144. 
	 * @param maximalUpdates	Limit of updates from other classes like MouseRandomCollector
	 * 							and KeyRandomCollector. The collection stops after
	 * 							maximalUpdates to the pool. Recommended values are
	 * 							from 256 to 1024. 
	 */
	private EntropyPool(int quality, int maximalAccesses, int maximalUpdates){
		pause = quality;
		maxAccess = maximalAccesses;
		maxUpdates = maximalUpdates;
	}	
	
	
	// Allow only one instance: 
	public static EntropyPool ePool = null;
	
	public static EntropyPool getInstance() {
		
		if (ePool == null) {
			ePool = new EntropyPool();
		}
		return ePool;
	}
	public static EntropyPool getInstance(
			int _quality, 
			int _maximalAccesses, 
			int _maximalUpdates) {
		
		if (ePool == null) {
			ePool = new EntropyPool(_quality, _maximalAccesses, _maximalUpdates);
		}
		return ePool;
	}
	

	// the seed pool:
	private transient long[] pool = new long[64];
	private static final int POOL_SIZE = 64;
	private static final int POOL_MASK = POOL_SIZE - 1;
	
	// Array of threads, that update the pool
	private static EntropyThread[] threads = null;
	private static final int THREAD_NUMBER = 64;
	private static final int THREAD_MASK = THREAD_NUMBER -1;	
	
	// shift values for the Xorshift generator
	private final static int[][] triples = { // 16 xorshift-triples 
		// from: Sebastiano Vigna: An experimental exploration of Marsaglia’s 
		// xorshift generators, scrambled. S. 13
		{11, 5, 45}, 	{17, 23, 52}, 	{12, 25, 27}, 	{17, 23, 29}, 
		{14, 23, 33}, 	{17, 47, 29}, 	{16, 25, 43}, 	{23, 9, 57}, 
		{11, 5, 32}, 	{8, 31, 17}, 	{3, 21, 31}, 	{17, 45, 22}, 
		{8, 37, 21}, 	{13, 47, 23}, 	{13, 35, 30}, 	{9, 37, 31} };
	private final static int TRIPLE_NUMBER = 16;
	private final static int TRIPLE_MASK = TRIPLE_NUMBER - 1;	
	
	private boolean isRunning = false;// the collection is running or not
	private boolean wasStarted = false; // the instance of the collection was started or not

	// index of the next pool value to update:  
	// incremented for every access to the pool
	private int tableCounter = 0;
	
	// counts the number of getValue() calls to
	// avoid using values twice without reseeding
	// the pool
	private int getValueCounter = 0;
	
	// Limit the running time be the number of the accesses
	// to the pool and the number of updates with values from
	// KeyRandomCollector and MouseRandomCollector: 
	
	// maximal accesses to the pool: 
	// collector stops when accessCounter > maxAccess
	private int maxAccess = 262144;//65536;//32768;//16384;
	private int accessCounter = 0;
	
	// maximal updates from KeyRandomCollector and MouseRandomCollector
	// collector stops when updateCounter > maxUpdates
	private int maxUpdates = 1024;
	private int updateCounter = 0;
		
	// helper AND mask for pool index (never > 63)
	//private static final int mask63 = 0x0000003F;
	
	// counter is incremented for every new update
	// the thread is chosen by this value % 64
	private static int threadCounter = 0; 
	
	// threads pauses: 
	// shorter pauses are more expensive, 
	// longer pauses give worse quality
	private static int pause = 10;
	
	// the priority of the threads
	private static final int priority = 2;


	/**
	 * Update a running thread with a random value. This value
	 * might be e.g. the time between key strokes or mouse events. 
	 * If the threads are not already running, this method will start them. 
	 * 
	 * @param val	the value to update the thread
	 */
	public final void updateThread(long val) {
		
		if (updateCounter > maxUpdates || accessCounter > maxAccess){
			return;
		}

		if (isRunning == false) {
			if (threads == null) {
				startAllThreads();
			}
			return;
		}		
		EntropyThread st = threads[threadCounter & THREAD_MASK];// % (THREAD_NUMBER - 1)];

		st.update(val);
		
		threadCounter++;		
		updateCounter++;		
	}
	
	/**
	 * Stop the collection. 
	 */
	public final void stopCollection(){
		
		for (int i = 0; i < THREAD_NUMBER; i++) {
			EntropyThread st = null;
			// If there is no password and the ok button is clicked directly
			// without any mouse action, the threads might be not started yet...
			try {
				st = threads[i];
			} catch (NullPointerException npe){
				System.out.println("Thread " + i + " for EntropyPool wasn't started");
				continue;
			}
			if (st != null) {
				st.terminate();
				try {
					st.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				st = null;
			}
		}
		tableCounter = 0;
		ePool.isRunning = false;
	}

	/**
	 * Get the next value of the seed pool. If the collection is not stopped, 
	 * the index is determined by the updates. 
	 * The index does not necessarily starts 0 and if the collection 
	 * is not stopped, the next index is not necessarily the previous index + 1. 
	 * If the pool was finished and the next value would be used twice
	 * without reseeding the pool, an Exception is thrown
	 * 
	 * @param stopCollection	if true, the collection stops.
	 * 
	 * @return					one 64-bit value of the pool. If the 
	 * 							collection is still running, the index
	 * 							is unpredictable.
	 */
	public final long getValue(boolean stopCollection) {		
				
		// check if the collection was started: 
		if (wasStarted == false) {
			throw new IllegalStateException("The entropy pool wasn't started yet!");
		}
		// check if the next value was already used:
		if (getValueCounter >= POOL_SIZE) {
			throw new IllegalStateException("The entroy pool must be reseeded!");
		}
		
		// return next value, if > 63 start at 0, then increment counter:
		int resultIndex = tableCounter++ & POOL_MASK;// % (POOL_SIZE - 1);
		long result = ePool.pool[resultIndex];	
		
		if (stopCollection == true){
			if (ePool.isRunning == true){
				stopCollection();
				ePool.isRunning = false;
			}
			getValueCounter++;
		} 

		return result;
	}	
	
	/**
	 * Get one value of the seed pool. The collection must be stopped before. 
	 * If the next value would be used twice without reseeding the pool, 
	 * an Exception is thrown.
	 * 
	 * @param index				the index of the pool
	 * 
	 * @return					one 64-bit value of the pool. 
	 */
	public final long getValue(int index) {		
		
		// check if the collection was started: 
		if (wasStarted == false) {
			throw new IllegalStateException("The EntropyPool wasn't started yet!");
		}
		if (ePool.isRunning == true){
			throw new IllegalStateException("The EntropyPool must be stopped before.");
		}
		// check if the next value was already used:
		if (getValueCounter >= POOL_SIZE) {
			throw new IllegalStateException("The entroy pool must be reseeded!");
		}
		
		// return next value, if > 63 start at 0, then increment counter:
		long result = ePool.pool[index];	
		 
		tableCounter = (index + 1) & POOL_MASK;//% (POOL_SIZE - 1);
		
		getValueCounter++;
		
		return result;
	}	
	
	/**
	 * Get values from pool to reseed it. This allows using values twice.
	 * 
	 * @return	a long array with 4 element (32 byte)
	 */
	public final long[] getValuesToReseed(){
		long[] result = new long[4];
		System.arraycopy(pool, 0, result, 0, 4);
		return result;
	}
	
	/**
	 * Reseed the pool. Avoids using a value of the pool twice. 
	 * To be cryptographically secure, the argument to update 
	 * should be performed e.g. by a cryptographically
	 * secure hash algorithm. 
	 * 
	 * @param reseedValues		array of at least 64 values
	 * 							to reseed the values of the pool
	 */
	public final void reseedPool(long[] reseedValues) {
		
		if (isRunning == true) {
			throw new IllegalStateException("Collection must be stopped");
		}
		if (wasStarted == false) {
			throw new IllegalStateException("The EntropyPool wasn't started yet!");
		}
		if (reseedValues.length < pool.length) {
			throw new IllegalArgumentException("reseed array must contain at least "+ POOL_SIZE + " values");
		}
		// update every value of the pool
		for ( int i = 0; i < POOL_SIZE; i++){
			// perform every triple:
			for (int j = 0; j < TRIPLE_NUMBER; j++) {
				ePool.pool[i] ^= xorShift(
						ePool.pool[i], 
						triples[j][0], triples[j][1], triples[j][2]); 
			}
			// xor with reseed value:
			ePool.pool[i] ^= reseedValues[i];
		}
		// reset the counter: you can get POOL_SIZE values now without reseeding
		getValueCounter = 0;
	}
	
	/**
	 * Wipe the pool to avoid memory attacks like cold boot attacks
	 */
	public final void clearPool(){
		Arrays.fill(pool,  0l);
		wasStarted = false;
	}
	
	/**
	 * Get the pool size (number of 64-bit values)
	 * 
	 * @return	the pool size
	 */
	public final static int getPoolSize(){
		return POOL_SIZE;
	}
	
	/**
	 * For security reasons cloning is not supported. 
	 */
	@Override
	public final Object clone() throws java.lang.CloneNotSupportedException {
		   throw new java.lang.CloneNotSupportedException();
	}
	
	//======= private methods =============================================================
	
	// modified xor shift generator (nanoTime added)
	private final static long xorShift(long x, int shift1, int shift2, int shift3) {
		
		  x ^= x << shift1;
		  x ^= x >>> shift2;
		  x ^= x << shift3;
		  
		  // Add nanoTime. 
		  x += System.nanoTime();
		  
		  return x;		
	}

	private final void startAllThreads() {
		
		int previousPriority = Thread.currentThread().getPriority();
		Thread.currentThread().setPriority(9);
		
		threads = new EntropyThread[64];
		
		for (int i = 0; i < POOL_SIZE; i++) {

			threads[i] = new EntropyThread(i);		

			threads[i].setPriority(priority);
			
			threads[i].start();	
		}

		Thread.currentThread().setPriority(previousPriority);
		isRunning = true;
		wasStarted = true;
	}


	//======= inner class =============================================================
	
	private class EntropyThread extends Thread {
		
		int index; // index in threads[]
		
		public EntropyThread(int _index){
			index = _index;
		}
		
		boolean running = true;
		
		long x0 = System.nanoTime() + index;
		
		int triplesIndex;
		
		public void run() {
			
			// for first call, take triple one by one
			triplesIndex = index % TRIPLE_MASK;//(TRIPLE_NUMBER - 1);		
			
			while (running == true) {	
				// === for testing the order of threads ================
				//System.out.print(" " + index);
				//System.out.println("tableCounter: " + tableCounter);
				// =====================================================
				
				if (accessCounter > maxAccess || updateCounter > maxUpdates) {
					stopCollection();
					return;
				}
				
				x0 += index;
				x0 += tableCounter;
				
				x0 = xorShift(x0, triples[triplesIndex][0], triples[triplesIndex][1], triples[triplesIndex][2]);
				pool[tableCounter++ & POOL_MASK] ^= x0;
				
				try {
					sleep(pause);
				} catch (InterruptedException e) {
					//System.err.println("Thread interrupted");
				} 
				x0 = xorShift(x0, triples[triplesIndex][0], triples[triplesIndex][1], triples[triplesIndex][2]);
				pool[tableCounter++ & POOL_MASK] ^= x0;	
				accessCounter++;
			}			      
		}
		// get new value from KeyRandomCollector or MouseRandomCollector
		public void update(long value) {
			// update x0: 
			x0 ^= value;
			// select new triple: 			
			triplesIndex = (int) (x0 & TRIPLE_MASK);
		}
	    public void terminate() {
	        running = false;
	    }
	}
//}
	
/*	//====== FOR TESTING FUNCTIONALITY ==================
	public static void main(String[] args){		

		EntropyPool entropy = EntropyPool.getInstance();
		long x = 55;
		int c = 0;
		while(c < 100){
			try {
				Thread.sleep(5);
				entropy.updateThread(x++);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			c++;
		}
		entropy.stopCollection();
		
		long y = entropy.getValue(0);
		System.out.println("getTableValue: " + y);

		long[] xy = new long[64];
		Arrays.fill(xy, 1);
		entropy.reseedPool(xy);
		System.out.println("After updateTable(): " + entropy.pool[0]);

		System.exit(0);
	}
	//=======================================
	*/
}

