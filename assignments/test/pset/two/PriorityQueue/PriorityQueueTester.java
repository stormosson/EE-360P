package pset.two.PriorityQueue;

/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  PriorityQueue
 */

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import org.junit.Test;

import pset.two.PriorityQueue.PriorityQueue;

public class PriorityQueueTester {

    @Test
    public void testGivenConcurrentPriorityQueue() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        PriorityQueue priorityQueue = new PriorityQueue(5);
        for (int i = 0; i < 10; ++i) {
            executorService.submit(new PriorityQueueGivenTesterWorker(priorityQueue));
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
    
    @Test
    public void testCreatedConcurrentPriorityQueue() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        PriorityQueue priorityQueue = new PriorityQueue(5);
        for (int i = 0; i < 10; ++i) {
            executorService.submit(new PriorityQueueCreatedTesterWorker(priorityQueue));
        }
        executorService.shutdown();
    }

}

class PriorityQueueGivenTesterWorker implements Runnable {
	static final int QUE_SIZE = 5;
    static final int THREAD_SIZE = 10;
    static final int THREAD_PAUSE_TIME = 50;
    static final int PRIORITY_RANGE = 10;
    private final PriorityQueue que;

    public PriorityQueueGivenTesterWorker(PriorityQueue priorityQueue) {
        this.que = priorityQueue;
    }

    @Override
    public void run() {
        Random random = new Random();
        String string = Integer.toString(random.nextInt(10));
        int n = random.nextInt(10);
        try {
            Thread.sleep(random.nextInt(50));
        }
        catch (InterruptedException var4_4) {
            var4_4.printStackTrace();
        }
        boolean bl = false;
        try {
            if (this.que.add(string, n) >= 0) {
                System.out.println("Insert " + string + " w/ priority " + n + " SUCEED!");
                bl = true;
            } else {
                System.out.println("Insert " + string + " w/ priority " + n + " FAILED!");
                bl = false;
            }
        }
        catch (Exception var5_6) {
            var5_6.printStackTrace();
        }
        try {
            Thread.sleep(random.nextInt(50));
        }
        catch (InterruptedException var5_7) {
            var5_7.printStackTrace();
        }
        if (bl) {
            System.out.println(string + " is located at " + this.que.search(string));
            try {
                Thread.sleep(random.nextInt(50));
            }
            catch (InterruptedException var5_9) {
                var5_9.printStackTrace();
            }
            try {
                System.out.println("Pop first: " + this.que.poll());
            }
            catch (Exception var5_10) {
                var5_10.printStackTrace();
            }
        }
    }
}

class PriorityQueueCreatedTesterWorker implements Runnable {

    private final PriorityQueue priorityQueue;

    public PriorityQueueCreatedTesterWorker(PriorityQueue queue) {
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
