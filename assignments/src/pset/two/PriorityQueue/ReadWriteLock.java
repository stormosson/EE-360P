package pset.two.PriorityQueue;

import java.util.concurrent.Semaphore;

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

    int numThreadsReading = 0;
    Semaphore isWriting = new Semaphore(1);

    public void beginRead() throws InterruptedException {
        isWriting.acquire();
        ++numThreadsReading;
        isWriting.release();
    }

    public void endRead() {
        --numThreadsReading;
    }

    public void beginWrite() throws InterruptedException {
        isWriting.acquire();
        while (numThreadsReading > 0);
    }

    public void endWrite() {
        isWriting.release();
    }
}
