package pset.two.CyclicBarrier;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Eric Crosson {@literal <eric.s.crosson@utexas.edu>}
 * @author William "Stormy" Mauldin {@literal <stormymauldin@utexas.edu>}
 * @version 0.1
 * @since 2016-01-26
 */

public class CyclicBarrier {
	int parties;
	int numLeft;
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * Construct a CyclicBarrier object
	 * 
	 * @param parties the number of threads that have invoked await on this
	 * barrier
	 */
	public CyclicBarrier(int parties) {
		numLeft = this.parties = parties;
	}

	/**
	 * Waits until all parties have arrived at the barrier. If the current
	 * thread is not the last to arrive, then it is disabled for thread
	 * scheduling purposes and lies dormant until the last thread arrives.
	 * 
	 * @return int the arrival index of the current thread
	 */
	public synchronized int await() {

		lock.lock();
		int index = --numLeft;
		lock.unlock();
		try {
			if (index != 0) {
				wait();
			} else {
				numLeft = parties;
				notifyAll();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {}
		
		/*
		 * index == (parties - 1) indicates the first to arrive
		 * index == 0 indicates the last to arrive
		 */
		return index;
	}
}
