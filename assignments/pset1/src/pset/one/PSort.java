package pset.one;

/**
 * @author      Eric Crosson <eric.s.crosson@utexas.edu>
 * @author      William "Stormy" Mauldin <stormymauldin@utexas.edu>
 * @version     0.1
 * @since       2016-01-26
 */

import java.util.Arrays;

public class PSort implements Runnable {

    int[] A;
    int begin;
    int end;

    public PSort(int[] A, int begin, int end) {
        this.A = A;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Sort array A for int x with numThreads threads.
     *
     * @param  A The array to sort, from index begin to index end.
     * @param  begin The index to not sort before
     * @param  end The index to not sort after
     */
    public static void parallelSort(int[] A, int begin, int end) {
        try {
            Thread worker = new Thread(new PSort(A, begin, end));
            worker.start();
            worker.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
    }

    /* TODO: document */
    public int partition() {

        int pivot = this.begin;

        for (int i = this.begin; i < this.end; ++i) {
            if (A[i] < A[end]) {
                ++pivot;
            }
        }
        /* Place the pivot where it belongs */
        int temp = A[pivot];
        A[pivot] = A[end];
        A[end] = temp;
        return pivot;
    }

    /* TODO: document */
    @Override
    public void run() {
        try {
            System.out.println("Sorting A over " + begin + " to " + end);
            if (this.begin < this.end) {
                return;
            }
            int pivot = this.partition();
            Thread thread_small = new Thread(new PSort(A, begin, pivot - 1));
            Thread thread_large = new Thread(new PSort(A, pivot + 1, end));
            thread_small.start();
            thread_large.start();
            thread_small.join();
            thread_large.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
    }
}
