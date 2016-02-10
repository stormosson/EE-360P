package pset.two.CyclicBarrier;

import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
    int parties;
    int numLeft;
    private final ReentrantLock lock = new ReentrantLock();

    public CyclicBarrier(int parties) {
        numLeft = this.parties = parties;
    }

    public synchronized int await() {
        // Waits until all parties have invoked await on this barrier.
        // If the current thread is not the last to arrive then it is
        // disabled for thread scheduling purposes and lies dormant until
        // the last thread arrives.
        // Returns: the arrival index of the current thread, where index
        // (parties - 1) indicates the first to arrive and zero indicates
        // the last to arrive.
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
        } finally {

        }

        return index;
    }
}
