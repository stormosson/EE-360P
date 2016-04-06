package pset.five;

import java.util.ArrayList;
import java.util.PriorityQueue;

/** Defines the interface adhered to by a process participating in Lamport's
 * Mutual Exclusion Algorithm. */
public interface LamportsMutexAlgorithm {

    /** Timestamp used as Lamport's Clock */
    Timestamp ts = null;
    /** Priority queue used to sort incoming messages by Timestamp */
    PriorityQueue<Message> msgq = null;

    /* Synchronization methods */
    /** Used to differentiate states, increments internal clock by one. */
    int recordEvent();
    /** True if the given object controlls the head of the message queue
     * msgq. (Nonblocking) */
    boolean isHead();         /* true if current object controls head of msgq */

    /* Critical section methods -- the point of synchronizing */
    /** How a given thread requests the critical section. */
    void requestCS();
    /** How a given thread releases the critical section. */
    void releaseCS();
    /** A given thread's critical section. */
    String CS(String dispatch, ArrayList<String> parameters);
}
