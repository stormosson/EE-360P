package pset.two.ReadWriteLock;

import java.util.concurrent.Semaphore;

/**
 * @author Eric Crosson {@literal <eric.s.crosson@utexas.edu>}
 * @author William "Stormy" Mauldin {@literal <stormymauldin@utexas.edu>}
 * @version 0.1
 * @since 2016-02-10
 */

public class ReadWriteLock {
	// This class has to provide the following properties:
	// a. There is no read-write or write-write conflict.
	// b. A writer thread that invokes beginWrite() will be block only when
	// there is a thread holding the lock.
	// c. A reader thread that invokes beginRead() will be block if either
	// the lock is held by a writer or there is a waiting writer thread.
	// d. A reader thread cannot be blocked if all preceding writer threads
	// have acquired and released the lock or no preceding writer thread
	// exists.

	/** Counter for number of threads currently reading **/
	int numThreadsReading = 0;

	/** Binary semaphore used to track and control write access **/
	Semaphore isWriting = new Semaphore(1);

	/**
	 * Signify that a reader thread wants to read. Begin reading once writing
	 * permit is available
	 */
	public void beginRead() throws InterruptedException {
		isWriting.acquire();
		++numThreadsReading;
		isWriting.release();
	}

	/**
	 * Signal that a reader thread has completed reading
	 */
	public void endRead() {
		--numThreadsReading;
	}

	/**
	 * Signal that a writer thread wants to write. Begin writing once writing
	 * permit is available and no threads are currently reading
	 */
	public void beginWrite() throws InterruptedException {
		isWriting.acquire();
		while (numThreadsReading > 0)
			;
	}

	/**
	 * Signal that a writer thread has completed writing
	 */
	public void endWrite() {
		isWriting.release();
	}
}
