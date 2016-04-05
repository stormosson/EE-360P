package pset.five;

import java.util.Scanner;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public interface LamportTimestamp {

    private int counter;
    int increment();            /* increment counter */
    int get();                  /* get counter */
}

/**
 * Timestamp used in Lamport's Mutual Exclusion Algorithm.
 */
class Timestamp implements LamportTimestamp, Comparable<Timestamp> {

    /** Timestamp counter. */
    private int counter = 0;

    public Timestamp() { 
        this(0); 
    }
    public Timestamp(int counter) {
        this.counter = counter; 
    }

    public get() { 
        return this.counter;
    }

    /** Record an event -- increment the counter before each event in a given
     * process. */
    public int increment() {
        return ++counter;
    }

    @Override
    public int compareTo(final Timestamp that) {
        return Integer.compare(this.counter, that.counter);
    }
}
