package pset.two.PriorityQueue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;

public class PriorityQueue {

	private final int maxSize;
	private int currentSize = 0;
	private LinkedList<Node> linkedList = new LinkedList<Node>();
	private ReadWriteLock readWriteLock = new ReadWriteLock();

	public PriorityQueue(int maxSize) {
		// Creates a Priority queue with maximum allowed size as capacity
		this.maxSize = maxSize;
	}

	public PriorityQueue() {
		maxSize = 10; // safety
	}

	public int add(String name, int priority) {
		// Adds the name with its priority to this queue.
		// Returns the current position in the list where the name was inserted;
		// otherwise, returns -1 if the name is already present in the list.
		// This method blocks when the list is full.
		try {
			readWriteLock.beginWrite();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
		//printList();
		readWriteLock.endWrite();
		return i;
	}

	public int search(String name) {
		// Returns the position of the name in the list;
		// otherwise, returns -1 if the name is not found.
		try {
			readWriteLock.beginRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int i = 0;
		boolean found = false;
		for(Node n : linkedList)
		{
			if(n.name.equals(name)){
				found = true;
				break;
			}
			++i;
		}
		
		if (!found) {
			i = -1;
		}
		//printList();
		readWriteLock.endRead();
		return i;

	}

	public String poll() {
		// Retrieves and removes the name with the highest priority in the list,
		// or blocks the thread if the list is empty.
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
		//printList();
		readWriteLock.endWrite();
		return name;
	}

	class Node {
		int priority;
		String name;
	}

	private void printList() {
		ListIterator<Node> iterator = linkedList.listIterator();
		while (iterator.hasNext()) {
			Node n = iterator.next();
			System.out.print(n.name + ", ");
		}
	}
}
