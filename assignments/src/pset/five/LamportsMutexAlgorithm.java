package pset.five;

import java.util.ArrayList;
import java.util.PriorityQueue;

public interface LamportsMutexAlgorithm {

    Timestamp ts = null;
    PriorityQueue<Message> msgq = null;

    /* Synchronization methods */
    int recordEvent();
    boolean isHead();         /* true if current object controls head of msgq */

    /* Critical section methods -- the point of synchronizing */
    void requestCS();
    void releaseCS();
    String CS(String dispatch, ArrayList<String> parameters);
}
