/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  PriorityQueue
 */
import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PriorityQueueTester
implements Runnable {
    static final int QUE_SIZE = 5;
    static final int THREAD_SIZE = 10;
    static final int THREAD_PAUSE_TIME = 50;
    static final int PRIORITY_RANGE = 10;
    private final PriorityQueue que;

    public PriorityQueueTester(PriorityQueue priorityQueue) {
        this.que = priorityQueue;
    }

    public static void main(String[] arrstring) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        PriorityQueue priorityQueue = new PriorityQueue(5);
        for (int i = 0; i < 10; ++i) {
            executorService.submit(new PriorityQueueTester(priorityQueue));
        }
        executorService.shutdown();
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
