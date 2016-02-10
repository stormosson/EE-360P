
/*
 * Decompiled with CFR 0_110.
 * 
 * Could not load the following classes:
 *  CyclicBarrier
 */
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CyclicBarrierTest {
	static final int SIZE = 10;
	static final int ROUND = 2;

	public static void main(String[] arrstring) {
		CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
		ExecutorService executorService = Executors.newCachedThreadPool();
		for (int i = 0; i < 10; ++i) {
			executorService.submit(new TestThread(cyclicBarrier));
		}
		executorService.shutdown();
	}

	public static class TestThread implements Runnable {
		final CyclicBarrier barrier_;

		public TestThread(CyclicBarrier cyclicBarrier) {
			this.barrier_ = cyclicBarrier;
		}

		@Override
		public void run() {
			int n = -1;
			for (int i = 0; i < 2; ++i) {
				System.out.println("Thread " + Thread.currentThread().getId() + " is WAITING round:" + i);
				try {
					n = this.barrier_.await();
				} catch (Exception var3_3) {
					var3_3.printStackTrace();
				}
				System.out.println("Thread " + Thread.currentThread().getId() + " got index:" + n);
			}
		}
	}
}
