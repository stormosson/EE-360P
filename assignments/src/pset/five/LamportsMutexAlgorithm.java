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

public interface LamportsMutexAlgorithm {

    private Timestamp ts;
    private PriorityQueue<Message> msgq;

    /* Synchronization methods */
    int recordEvent();
    boolean isHead();         /* true if current object controls head of msgq */

    /* Critical section methods -- the point of synchronizing */
    void requestCS();
    void releaseCS();
    void CS();
}
