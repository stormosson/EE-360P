package pset.one;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

		/* TODO: document. no more threads than elements */
		if (numThreads > A.length) {
			numThreads = A.length;
		}

		List<Future<Integer>> results = null;
		ArrayList<PSearch> callables = new ArrayList<PSearch>();

		for (int i = 0; i < numThreads; ++i) {
			int size = A.length / numThreads;
			int begin = i * size;
			int end = (i + 1) * size - 1;
			// some splits will have a remainder
			if (i == numThreads - 1) {
				end = A.length - 1;
			}
			PSearch searcher = new PSearch(A, begin, end, x);
			callables.add(searcher);
		}
		try {
			ExecutorService es = Executors.newCachedThreadPool();
			results = es.invokeAll(callables);
			es.shutdown();
		} catch (Exception e) {
			System.err.println(e);
		}

		int ret = TARGET_DNE;
		for (int i = 0; i < results.size(); ++i) {
			try {
				if (results.get(i).get() != -1) {
					ret = results.get(i).get();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	@Override
	public Integer call() throws Exception {
		for (int elt = this.begin; elt <= this.end; ++elt) {
			if (this.A[elt] == this.target) {
				return elt;
			}
		}
		/* Base case */
		return TARGET_DNE;
	}
}
