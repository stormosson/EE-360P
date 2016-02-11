package pset.two.PriorityQueue;

/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  PriorityQueue
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import org.junit.Test;

import pset.two.PriorityQueue.PriorityQueue;

public class PriorityQueueTester {

    @Test
    public void testConcurrentPriorityQueue() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        PriorityQueue priorityQueue = new PriorityQueue(5);
        for (int i = 0; i < 10; ++i) {
            executorService.submit(new PriorityQueueTesterWorker(priorityQueue));
        }
        executorService.shutdown();
    }

    @Test
    public void testAddDuplicateReturnsNegativeOne() {
        PriorityQueue priorityQueue = new PriorityQueue(5);
        assertEquals(0, priorityQueue.add("Horses", 0));
        assertEquals(-1, priorityQueue.add("Horses", 2));
    }
    
    @Test
    public void testSearchNotFoundReturnsNegativeOne(){
    	PriorityQueue priorityQueue = new PriorityQueue(5);
    	assertEquals(0, priorityQueue.add("Horses", 0));
    	assertEquals(-1, priorityQueue.search("Horspes"));
    }
    
    @Test
    public void testAddAndPollRegular1(){
    	PriorityQueue priorityQueue = new PriorityQueue(5);
    	assertEquals(0, priorityQueue.add("Horses", 0));
    	assertEquals(0, priorityQueue.add("Horspes", 2));
    	assertEquals("Horspes", priorityQueue.poll());
    }
    
    @Test
    public void testAddAndPollRegular2(){
    	PriorityQueue priorityQueue = new PriorityQueue(5);
    	assertEquals(0, priorityQueue.add("Horspes", 2));
    	assertEquals(1, priorityQueue.add("Horses", 1));
    	assertEquals("Horspes", priorityQueue.poll());
    }

}

class PriorityQueueTesterWorker implements Runnable {

    private final PriorityQueue priorityQueue;

    public PriorityQueueTesterWorker(PriorityQueue queue) {
        this.priorityQueue = queue;
    }

    @Override
    public void run() {
    	assertEquals(0, priorityQueue.add("Horspes", 2));
    	assertEquals(1, priorityQueue.add("Horsqes", 1));
    	assertEquals(2, priorityQueue.add("Horsres", 0));
    	assertEquals(0, priorityQueue.add("Horsses", 5));
    	assertEquals(0, priorityQueue.add("Horstes", 8));
    	assertEquals(0, priorityQueue.add("Horsues", 9));	//will have to wait until poll is called
    	assertEquals("Horsues", priorityQueue.poll());
    	assertEquals(-1, priorityQueue.search("Horsxes"));
    	assertEquals(0, priorityQueue.search("Horspes"));
    }
}
