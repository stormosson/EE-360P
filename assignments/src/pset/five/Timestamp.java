package pset.five;

interface LamportTimestamp {

    int counter = 0;
    int increment();            /* increment counter */
    int get();                  /* get counter */
}

/**
 * Timestamp used in Lamport's Mutual Exclusion Algorithm.
 */
public class Timestamp implements LamportTimestamp, Comparable<Timestamp> {

    /** Timestamp counter. */
    private int counter = 0;

    public Timestamp() { 
        this(0); 
    }
    public Timestamp(int counter) {
        this.counter = counter; 
    }

    public int get() { 
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
