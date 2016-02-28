package pset.two.PriorityQueue;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author Eric Crosson {@literal <eric.s.crosson@utexas.edu>}
 * @author William "Stormy" Mauldin {@literal <stormymauldin@utexas.edu>}
 * @version 0.1
 * @since 2016-01-26
 */

public class PriorityQueue {

    private final int maxSize;
    private int currentSize = 0;
    private LinkedList<Node> linkedList = new LinkedList<Node>();
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    /**
     * Construct a PriorityQueue object
     *
     * @param maxSize the maximum allowed size of the priority queue
     */
    public PriorityQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * Construct a PriorityQueue object. Uses an arbitrary maxSize
     */
    public PriorityQueue() {
        maxSize = 10; // safety
    }

    /**
     * Adds a new Node to the queue.
     *
     * @param name the name of the Node
     * @param priority the priority of the Node
     * @return the index of placement
     * @return -1 if not placed
     */
    public int add(String name, int priority) {

        lock.lock();
        try {
            while (currentSize == maxSize) {
                try {
                    notFull.await();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /* keepLooking signifies we have not yet found the index to insert
             * the new node (i). When we do find it, we stop incrementing i but
             * continue iterating because we still need to ensure we are not
             * about to add a duplicate name. */
            boolean keepLooking = true;
            int i = 0;

            ListIterator<Node> iterator = linkedList.listIterator();
            while (iterator.hasNext()) {
                Node n = iterator.next();
                if (name.equals(n.name)) {
                    i = -1;
                    break;
                }
                if (n.priority < priority) {
                    keepLooking = false;
                }
                if (keepLooking) {
                    ++i;
                }
            }

            /* If we are a new name, add to the list and update currentSize and
             * other threads */
            if (i != -1) {
                Node curNode = new Node();
                curNode.priority = priority;
                curNode.name = name;

                linkedList.add(i, curNode);
                ++currentSize;
                notEmpty.signal();
            }

            return i;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Searches the queue for first instance of Node with specified name
     *
     * @param name the name of the Node
     * @return the position of the Node with specified name in the list
     * @return -1 if Node with specified name not found
     */
    public int search(String name) {

        lock.lock();
        System.out.println("Searching for " + name + " in ");
        try {
            int i = 0;
            boolean found = false;
            for (Node n : linkedList) {
                if (name.equals(n.name)) {
                    found = true;
                    break;
                }
                ++i;
            }

            if (!found) {
                i = -1;
            }

            return i;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the Node with the highest prioirty in the queue.
     * Blocks thread if queue is empty.
     *
     * @return name of the node with the highest priority
     */
    public String poll() {

        lock.lock();
        try {
            while (currentSize <= 0) {
                try {
                    notEmpty.await();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String name = linkedList.poll().name;
            --currentSize;
            notFull.signal();

            return name;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Data structure for placement in queue.
     * Contains name and priority.
     */
    class Node {
        int priority;
        String name;
    }

    /* Prints out names of all Nodes in the queue.
     * Used for debugging purposes.
     *
     * Warning: only use this method when you have the lock.
     */
    private void printList() {
        System.out.print("[ ");
        for (Node n : linkedList) {
            System.out.print(n.name + ", ");
        }
        System.out.print("]");
    }
}
