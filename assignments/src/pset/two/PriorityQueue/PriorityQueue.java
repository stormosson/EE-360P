package pset.two.PriorityQueue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;

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
        try {
            readWriteLock.beginWrite();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int i = 0;
        ListIterator<Node> iterator = linkedList.listIterator();
        while (iterator.hasNext()) {
            Node n = iterator.next();
            if (n.priority < priority) {
                break;
            }
            ++i;
        }

        Node curNode = new Node();
        curNode.priority = priority;
        curNode.name = name;
        linkedList.add(i, curNode);
        ++currentSize;
        readWriteLock.endWrite();
        return i;
    }

    /**
     * Searches the queue for first instance of Node with specified name
     *
     * @param name the name of the Node
     * @return the position of the Node with specified name in the list
     * @return -1 if Node with specified name not found
     */
    public int search(String name) {
        try {
            readWriteLock.beginRead();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 0;
        boolean found = false;
        for (Node n : linkedList) {
            if (n.name.equals(name)) {
                found = true;
                break;
            }
            ++i;
        }

        if (!found) {
            i = -1;
        }
        readWriteLock.endRead();
        return i;

    }

    /**
     * Retrieves and removes the Node with the highest prioirty in the queue.
     * Blocks thread if queue is empty.
     *
     * @return name of the node with the highest priority
     */
    public String poll() {
        while (true) {
            try {
                readWriteLock.beginWrite();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            if (currentSize > 0) {
                break;
            }
            readWriteLock.endWrite();
        }

        String name = linkedList.poll().name;
        currentSize--;
        // printList();
        readWriteLock.endWrite();
        return name;
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
     */
    private void printList() {
        ListIterator<Node> iterator = linkedList.listIterator();
        while (iterator.hasNext()) {
            Node n = iterator.next();
            System.out.print(n.name + ", ");
        }
    }
}
