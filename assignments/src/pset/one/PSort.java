package pset.one;

import java.util.Arrays;

/**
 * @author      Eric Crosson {@literal <eric.s.crosson@utexas.edu>}
 * @author      William "Stormy" Mauldin {@literal <stormymauldin@utexas.edu>}
 * @version     0.1
 * @since       2016-01-26
 */

public class PSort implements Runnable {

    /** Array of ints to sort. */
    int[] A;
    /** Beginning index of range of A to sort. */
    int begin;
    /**End index of range of A to sort. */
    int end;

    /**
     * Construct a Parallel Sort object.
     *
     * @param A Array to sort
     * @param begin Starting index of range to sort
     * @param end End index of range to sort
     */
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
            Thread worker = new Thread(new PSort(A, begin, end-1));
            worker.start();
            worker.join();
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        }
    }

    /**
     * Partition A into two sections: the elements less than a chosen pivot and
     * the elements greater than said pivot.
     *
     * @return The index (after sorting to the correct position in this.A) of
     * the chosen pivot.
     */
    public int partition() {

        int temp;
        int pivot = this.begin;

        for (int i = this.begin; i < this.end; ++i) {
            if (A[i] < A[end]) {
                temp = A[i];
                A[i] = A[pivot];
                A[pivot] = temp;
                ++pivot;
            }
        }
        /* Place the pivot where it belongs */
        temp = A[pivot];
        A[pivot] = A[end];
        A[end] = temp;
        return pivot;
    }

    /**
     * Sort array A from begin to end.
     */
    @Override
    public void run() {
        try{
            if (this.begin >= this.end) {
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
