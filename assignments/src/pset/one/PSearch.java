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

    /** The value to return when target does not exist in array A. */
    final static int TARGET_DNE = -1;

    /** Array of ints to search. */
    int[] A;
    /** Beginning index of range of A to search. */
    int begin;
    /** End index of range of A to search. */
    int end;
    /** Target value to search array A for. */
    int target;

    /**
     * Construct a Parallel Search object.
     *
     * @param A Array to search through
     * @param begin Starting index of range to search
     * @param end End index of range to search
     * @param target The target value to search for
     */
    public PSearch(int[] A, int begin, int end, int target) {
        this.A = A;
        this.begin = begin;
        this.end = end;
        this.target = target;
    }

    /**
     * Search array A for int x with numThreads threads.
     *
     * @param x The search element
     * @param A The search space
     * @param numThreads The maximum number of searcher threads to spawn
     * @return The index of x in array A
     */
    public static int parallelSearch(int x, int[] A, int numThreads) {

        /* In the event that more threads are permitted to exist than elements
         * to search, limit the number of spawned threads to the number of
         * elements in A. */
        if (numThreads > A.length) {
            numThreads = A.length;
        }

        List<Future<Integer>> results = null;
        ArrayList<PSearch> callables = new ArrayList<PSearch>();

        for (int i = 0; i < numThreads; ++i) {
            int size = A.length / numThreads;
            int begin = i * size;
            int end = (i + 1) * size - 1;
            /* Assign the workload remainder to the last thread */
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
            e.printStackTrace();
        }

        int ret = TARGET_DNE;
        for (int i = 0; i < results.size(); ++i) {
            try {
                int get = results.get(i).get();
                if (get != -1) {
                    ret = get;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Search the assigned range of A for target.
     */
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
