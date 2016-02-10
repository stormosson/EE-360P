package pset.two.ReadWriteLock;

/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  ReadWriteLock
 */

import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pset.two.ReadWriteLock.Writer;
import pset.two.ReadWriteLock.Reader;
import pset.two.ReadWriteLock.ReadWriteLock;

import static org.junit.Assert.*;
import org.junit.Test;

public class ReadWriteLockTest {
    private static final int THREAD_SIZE = 16;

    @Test
    public void testReadWriteLock() {
        int n;
        StringBuffer stringBuffer = new StringBuffer();
        ReadWriteLock readWriteLock = new ReadWriteLock();
        Random random = new Random();
        Future[] arrfuture = new Future[16];
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (n = 0; n < 16; ++n) {
            int n2 = random.nextInt(100);
            arrfuture[n] = n2 < 30 ? executorService.submit(new Writer(readWriteLock, stringBuffer, n)) : executorService.submit(new Reader(readWriteLock, stringBuffer, n));
        }
        executorService.shutdown();
        for (n = 0; n < 16; ++n) {
            try {
                arrfuture[n].get();
                continue;
            }
            catch (InterruptedException var7_8) {
                var7_8.printStackTrace();
                continue;
            }
            catch (ExecutionException var7_9) {
                var7_9.printStackTrace();
            }
        }
    }
}
