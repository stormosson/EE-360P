package pset.five;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class Launcher {
    /**
     * Start a server based on given command line arguments.
     */
    public static void main(String[] args) {
      
        /** Server's unique ID number */
        int serverID;

        /** Total number of servers */
        int numServers;
        
        /** List of addresses to be considered*/
        ArrayList<String> addresses = new ArrayList<String>();
        
        /** Inventory file name*/
        String filename = "";

        if (args.length != 0 && args.length < 4) {
            String s = "";
            s += ("ERROR: Provide at least 3 arguments\n");
            s += ("\t(1) <serverID> <numServers> <inventoryFileName>: ");
            s += ("server's unique identification number, number of servers, and the name of the inventory file\n");
            s += ("\t(2) <totalServers>: the total number of servers\n");
            s += ("\t(3) <address>:<portNum> the address of the first server and the port number\n");
            s += ("\t(4) <address>:<portNum> the address of the second server and the port number\n");
            s += ("\t...\n");
            s += ("\t(n) <address>:<portNum> the address of the nth server and the port number\n");
            System.out.println(s);
            System.exit(-1);
        }
        
        /* Sanitize command line arguments */
        Scanner scan = new Scanner(System.in);
        if(args.length != 0){
            scan = new Scanner(String.join(" ", args));
        }
        serverID = scan.nextInt();
        numServers = scan.nextInt();
        filename = scan.nextLine();
        for(int i = 0; i < numServers; i++){
            addresses.add(scan.next());
        }

        /* Stand up all the servers in individual threads, keeping a reference
         * to only the first server (guaranteed to exist). */
        /** Server initialized with inventoryfile, he synchronizes the other n-1
         * servers. */
        Server initServer = null;
        /** List of threads controlling n servers. */
        ArrayList<Thread> server_threads = new ArrayList<Thread>();
        for(int i = 1; i <= numServers; i++){
            Server s = new Server(i-1, addresses);
            Thread t = new Thread(s);
            t.start();
            server_threads.add(t);
            initServer = initServer == null ? s : initServer;
        }

        /* Scan the inventory and add items to our single server reference. He
         * will synchronize the other servers to the same state. Our server is a
         * boy his name is Asimov. */
        try {
            scan = new Scanner(new File(filename));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        while (scan.hasNextLine()) {
            String[] line = scan.nextLine().split("\\s+");
            if (line.length != 2) {
                continue;
            }
            /* Assume: nonmatching 'item' field in inventory lines refer to
             * different items */
            String item = line[0];
            String quantity = line[1];
            ArrayList<String> parameters = new
                ArrayList<String>(Arrays.asList(item, quantity));
            initServer.CS("add", parameters);
        }

        initServer.list();

        /* Run -- accept incoming requests */
        for (Thread t : server_threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * Server class. This server assumes all requests will be valid.
 */
public class Server implements Runnable, LamportsMutexAlgorithm {

    public final boolean debug = true;

    /** Lock used to implement signals and awaiting. */
    private final Lock lock = new ReentrantLock();
    /** Condition triggered when a new head appears at the tip of the msgq PriorityQueue. */
    private final Condition newHead = lock.newCondition();
    /** Condition triggered when an ACK msg is received from another server. */
    private final Condition responseReceived = lock.newCondition();

    /* Internal state variables */
    /** Nonce used to keep track of order id's. */
    private int order_nonce = 1;
    /** Keeps track of inventory items. */
    private Map<String, Integer> inventory = null;
    /** Keeps track of past sales. */
    private Map<Integer, String> ledger = null;
    /** Keeps track of past orders for each user. */
    private Map<String, ArrayList<String>> user_orders = null;

    /** The port used for this server's tcp communication. */
    private Integer port; //this is the port that the server "this" listens on
    /** The address used by this server. */
    public String server_address;
    /** The addresses used by the other servers mirroring this one. */
    private ArrayList<String> server_addresses = null;
    /** The communication channels to each server in server_addresses. */
    private Map<String, TcpListener> server_list = null;

    /* Variables used for Lamport's MuTex Algorithm */
    /** Timestamp used to keep track of Lamports Clock (used for Lamports Mutual
     * Exclusion Algorithm). */
    private Timestamp ts;
    /** Message queue. Used to implement Lamports Mutual Exclusion Algorithm. */
    PriorityQueue<Message> msgq = null;

    /* Temporary processing variables */
    private String[] command;
    private InetAddress address;
    /** TcpSocket used for tcp communication by this server. */
    private Socket tcpsocket;

    public Server(int serverID, ArrayList<String> nodes){
        inventory = new ConcurrentHashMap<String, Integer>();
        ledger = new ConcurrentHashMap<Integer, String>();
        user_orders = new ConcurrentHashMap<String, ArrayList<String>>();
        server_list = new ConcurrentHashMap<String, TcpListener>();

        server_addresses = nodes;
        server_address = nodes.get(serverID);
        port = Integer.parseInt(server_addresses.get(serverID).split(":")[1]);

        /* Create a channel to every server, since we will have to synchronize
         * with them for Lamports Algorithm */
        for (int i = 0; i < server_addresses.size(); ++i) {
            if (serverID == i) { continue; }
            server_list.put(server_addresses.get(i), new 
                            TcpListener(server_addresses.get(i)));
        }

        ts = new Timestamp();
        msgq = new PriorityQueue<Message>();
    }

    @Override
    public void run() {
        /* Start listening for messages */
        Thread tcplistener = new Thread(new TcpListener(server_address, port, this));
        tcplistener.start();
        try {
            tcplistener.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Update the local clock by incrementing it once. */
    public int recordEvent() {
        return recordEvent(new Timestamp());
    }

    /** Update the local clock by taking the maximum of the local clock and the
     * clock used by the server that sent this server a message. */
    private int recordEvent(Timestamp t) {
        ts.increment(); 
        ts = new Timestamp(Math.max(ts.get(), t.get()));
        return ts.get();
        /* room for expansion (saving global state) here */
    }

    /** Use to initialize a live node with initial inventory */
    public synchronized String add(String productname, String quantity){
        int intQuantity = Integer.parseInt(quantity);
        inventory.put(productname, intQuantity +
                      (inventory.containsKey(productname) ?
                       inventory.get(productname) : 0));
        return String.format("Added %s of %s", quantity, productname);
    }

    /** Notify all servers in server_list of msg.
     *
     * \warning This is a blocking method, returning only when all threads have
     * acknowledged this request. */
    public void notifyServers(Message msg) {
        lock.lock();
        try{
            this.enqueue(msg);

            for (TcpListener channel : server_list.values()) {
                recordEvent();
                channel.sendMessage(msg);
            }
            int waitingOn = server_list.size()+1;
            while (waitingOn > 0 ) {
                responseReceived.await();
                --waitingOn;
            }
            if (debug) { System.out.format("== BARRIER REACHED =="); }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /** Return true if 'this' server is at the head of the priority queue
     * msgq. */
    public boolean isHead() {
        Message head = msgq.peek();
        /* Peek might return null */
        if (head == null) { return false; }
        /* If head is current object, then yes 'this' is head */
        if (head.getSender() == this) { return true; }
        /* Otherwise, no we are not head */
        return false;
    }

    /** Enqueue a message on the msgq. This method is the single entry and exit
     * point for associated TcpListener. */
    public String enqueue(Message msg) {
        recordEvent(msg.getTimestamp()); /* new event -- message channel is alive */

        String responseToClient = "";
        Message reply;
        TcpListener channel;
        String requesting_server;

        switch(msg.type()) {
        case NONE:
            /* message came from Client */
            String cmd = msg.getServerCommand().getCommand();
            ArrayList<String> params = msg.getServerCommand().getParameters();
            /* Operate on the command in the Critical Section */
            responseToClient = CS(cmd, params);
            break;

        case REQUEST:
            /* another Server requesting CS */
            msgq.add(msg);
            reply = new Message(this, msg.getServerCommand(), ts, MessageType.ACK);
            requesting_server = msg.getSender().server_address;
            channel = server_list.get(requesting_server);
            channel.sendMessage(reply);
            break;

        case RELEASE:
            /* Server currently in CS releases mutex */
            /* Assert: only the head of the priority queue will send a release
             * message; */
            msgq.poll();        /* remove message holding CS */
            newHead.signal();
            break;

        case ACK:
            /* Message is responding to 'this' object's REQUEST message */
            responseReceived.signal();
            break;

        case SYNC:
            /* Message received during another server's CS -- treat this message
             * specially. Update your database while he is still in the CS (and
             * everybody else is frozen to local-state-changes only) and
             * respond. He'll wait for all responses before releasing the CS. */
            delta(msg.getServerCommand().getCommand(), 
                  msg.getServerCommand().getParameters());
            /* TODO: question: do we need to differentiate the ACK and the synchronization ACK? */
            reply = new Message(this, msg.getServerCommand(), ts, MessageType.ACK);
            requesting_server = msg.getSender().server_address;
            channel = server_list.get(requesting_server);
            channel.sendMessage(reply);
            break;
        }
        return responseToClient;
    }

    /** Request the critical section and alert all other servers. */
    public void requestCS() {
        recordEvent();          /* new event -- this thread ready for CS */
        Message msg = new Message(this, this.ts, MessageType.REQUEST);
        this.enqueue(msg);
        /* notifyServers is a blocking method -- it will wait for all nodes to reply */
        notifyServers(msg);
    }

    /** Release the critical section and alert all other servers. */
    public void releaseCS() {
        recordEvent();          /* new event -- CS has ended */
        Message msg = new Message(this, this.ts, MessageType.RELEASE);
        this.enqueue(msg);
        notifyServers(msg);
    }

    /** Apply a transactional change to our inventory. */
    public String delta(String dispatch, ArrayList<String> parameters){
        recordEvent();          /* new event -- CS has begun */

        String response = "";

        /* Update current state */
        ListIterator<String> it = parameters.listIterator();
        if("add".equals(dispatch)){
            response = add(it.next(), it.next());
        } else if("purchase".equals(dispatch)) {
            response = purchase(it.next(), it.next(), it.next());
        } else if("cancel".equals(dispatch)) {
            response = cancel(it.next());
        }

        return response;
    }

    /* TODO: verify this method, drunk coding */
    /** Send a signal to the n-1 other servers after each local transaction,
     * keeping the entire fleet up to date and immortal. */
    public void synchronizeServers(ServerCommand srv_cmd) {
        lock.lock();
        Message sync = new Message(this, srv_cmd, ts, MessageType.SYNC);
        try {
            for (TcpListener channel : server_list.values()) {
                recordEvent();
                channel.sendMessage(sync);
            }
            int waitingOn = server_list.size()+1;
            while (waitingOn > 0 ) {
                responseReceived.await();
                --waitingOn;
            }
            if (debug) { System.out.format("== BARRIER REACHED =="); }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /** The Critical Section. This method acquires, executes and releases a
     * critical section, invoking the delta function and modifying the store
     * inventory. */
    public String CS(String dispatch, ArrayList<String> parameters) {
        requestCS();
        lock.lock();
        try{
            /* 1. all replies have been received if requestCS has returned */
            /* 2. when own request is at head of msgq, enter CS */
            while(!isHead()) {
                try {
                    newHead.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String response = delta(dispatch, parameters);
            synchronizeServers(new ServerCommand(dispatch, parameters));
            releaseCS();
            return response;
        } finally {
            lock.unlock();
        }
    }
    /* -- End Lamports Interface -- */

    /* -- Begin Server interface -- acting as a server acts -- */
    /**
     * Handle a purchase event.
     */
    private synchronized String purchase(String username, String productname,
                                         String quantity) {

        if (!inventory.containsKey(productname)) {
            return "Not Available - We do not sell this product";
        }
        /* We do have productname in our database */
        int stock = inventory.get(productname);
        if (stock - Integer.parseInt(quantity) < 0) {
            return "Not Available - Not enough items";
        }
        /* We do have enough items in stock to complete sale */
        inventory.put(productname, stock - Integer.parseInt(quantity));
        Integer orderid = order_nonce++;

        /* Add user order to ledger */
        ledger.put(orderid, String.format("%s %s", productname, quantity));

        /* Relational databases would be great here -- record user orders */
        ArrayList<String> orders = null;
        if (!user_orders.containsKey(username)) {
            orders = new ArrayList<String>();
            user_orders.put(username, orders);
        } else {
            orders = user_orders.get(username);
        }
        orders.add(String.format("%s, %s, %s", orderid, productname, quantity));

        return String.format("You order has been placed, %s %s %s %s",
                             orderid, username, productname, quantity);
    }

    /**
     * Handle an order cancellation.
     *
     * Assumes an order will not be canceled more than once.
     */
    private synchronized String cancel(String orderid) {

        Integer ordernum = Integer.parseInt(orderid);
        if (!ledger.containsKey(ordernum)) {
            return String.format("%s not found, no such order", ordernum);
        }
        /* We recognize the order id, reverse contents of ledger */
        String[] order = ledger.get(ordernum).split("\\s+");
        String productname = order[0];
        Integer quantity = Integer.valueOf(order[1]);

        /*
         * Remove the order from the ledger so we cannot 'spawn' infinite items
         * through false returns
         */
        ledger.remove(ordernum);

        inventory.put(productname, quantity + inventory.get(productname));
        return String.format("Order %s is canceled", ordernum);
    }

    /**
     * Handle a search request.
     *
     * Assume canceled orders should still be listed. Assume username exists in
     * our database.
     */
    private synchronized String search(String username) {

        if (!user_orders.containsKey(username)) {
            return String.format("No order found for %s", username);
        }

        String response = "";
        for (String order : user_orders.get(username)) {
            response += String.format("%s\n", order);
        }
        return response;
    }

    /**
     * Handle an inventory list query.
     */
    public synchronized String list() {

        String response = "";
        for (String item : inventory.keySet()) {
            response += String.format("%s %d\n", item, inventory.get(item));
        }
        return response;
    }

    /**
     * Print a Map object to stdout.
     */
    @SuppressWarnings("unused")
    private void printMap(Map<Integer, String> map) {
        System.out.print("{");
        for (Integer item : map.keySet())
            System.out.print(String.format("<%s,%s> ", item, map.get(item)));
        System.out.println("}");
    }
    /* -- End Server interface -- */

    private void respond(String message) throws IOException {
        DataOutputStream stdout = new DataOutputStream(tcpsocket.getOutputStream());
        stdout.writeUTF(message);
    }
}
