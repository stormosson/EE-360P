package pset.one;

import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author Eric Crosson <eric.s.crosson@utexas.edu>
 * @author William "Stormy" Mauldin <stormymauldin@utexas.edu>
 * @version 0.1
 * @since 2016-01-26
 */

public class PSearch implements Callable<Integer> {
	
	final static int TARGET_DNE = -1;
	
	// TODO: document
	int[] A;
	int begin;
	int end;
	int target;
	
	public PSearch(int[] A, int begin, int end, int target) {
		this.A = A;
		this.begin = begin;
		this.end = end;
		this.target = target;
	}

	/**
	 * Search array A for int x with numThreads threads.
	 *
	 * @param x
	 *            The search element
	 * @param A
	 *            The search space
	 * @param numThreads
	 *            The number of searcher threads to spawn
	 * @return The index of x in array A
	 */
	public static int parallelSearch(int x, int[] A, int numThreads) {
		ArrayList<Integer> matches = new ArrayList<Integer>();
		try {
			ExecutorService es = Executors.newCachedThreadPool();

			for (int i = 0; i < numThreads; i++) {
				int size = A.length / numThreads;
				int begin = i * size;
				int end = (i + 1) * size - 1;
				if (i == numThreads - 1) {
					end = A.length - 1;
				}
				PSearch searcher = new PSearch(A, begin, end, x);
				Future<Integer> f = es.submit(searcher);
				int find = f.get();
				if (find != TARGET_DNE) {
					matches.add(f.get());
				}
			}
			es.shutdown();
		} catch (Exception e) {
			System.err.println(e);
		}

		if (matches.size() != 0) {
			return matches.get(0);
		} else {
			return TARGET_DNE;
		}
	}

	@Override
	public Integer call() throws Exception {
		for (int elt = this.begin; elt <= this.end; ++elt) {
			System.out.println("Searching element " + elt);
			if (this.A[elt] == this.target) {
				return elt;
			} else {
				System.out.println(this.target + " not found in " + A[elt]);
			}
		}
		/* Base case */
		return TARGET_DNE;
	}
}
