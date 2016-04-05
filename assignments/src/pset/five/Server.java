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


class Launcher {

    /**
     * Start a server based on given command line arguments.
     */
    public static void main(String[] args) {
        int serverID;
        int numServers;
        if (args.length != 5) {
            String s = "";
            //TODO: change to relevant help string
            s += ("ERROR: Provide 3 arguments\n");
            s += ("\t(1) <tcpPort>: the port number for TCP connection\n");
            s += ("\t(2) <udpPort>: the port number for UDP connection\n");
            s += ("\t(3) <file>: the file of inventory\n");
            System.out.println(s);
            System.exit(-1);
        }

        /* Sanitize command line arguments */
        serverID = Integer.parseInt(args[0]);
        numServers = Integer.parseInt(args[1]);
        String filename = args[2];
        ArrayList<String> addresses = new ArrayList<String>();
        for (int i = 3; i < args.length - 2; i++) {
            addresses.add(args[i]);
        }

        /* Stand up all the servers in individual threads, keeping a reference
         * to only the first server (guaranteed to exist). */
        Server initServer = null;
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
        Scanner scan = null;
        try {
            scan = new Scanner(new File(filename));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        while (scan.hasNextLine()) {
            String[] line = scan.nextLine().split("\\s+");
            if (line.length != 2)
                continue;
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

    /* Internal state variables */
    private int order_nonce = 1;
    private Map<String, Integer> inventory = null;
    private Map<Integer, String> ledger = null;
    private Map<String, ArrayList<String>> user_orders = null;

    private Integer port; //this is the port that the server "this" listens on
    private ArrayList<String> server_addresses = null;
    private Map<String, TcpListener> server_list = null;

    /* Variables used for Lamport's MuTex Algorithm */
    private Timestamp ts;
    PriorityQueue<Message> msgq = null;

    /* Temporary processing variables */
    private String[] command;
    private InetAddress address;
    private Socket tcpsocket;
    /*Used for futures*/
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public Server(int serverID, ArrayList<String> nodes){
        inventory = new ConcurrentHashMap<String, Integer>();
        ledger = new ConcurrentHashMap<Integer, String>();
        user_orders = new ConcurrentHashMap<String, ArrayList<String>>();
        server_list = new ConcurrentHashMap<String, TcpListener>();

        server_addresses = nodes;
        port = Integer.parseInt(server_addresses.get(serverID).split(":")[1]);

        /* Create a channel to every server, since we will have to synchronize
         * with them for Lamports Algorithm */
        server_list = new ArrayList<Server>();
        for (int i = 0; i < server_addresses.size(); ++i) {
            if (serverID == i) { continue; }
            server_list[server_addresses] = new 
                TcpListener(server_addresses.get(i));
        }

        ts = new Timestamp();
        msgq = new PriorityQueue<Message>();
    }

    @Override
    public void run() {
        /* Start listening for messages */
        Thread tcplistener = new Thread(new TcpListener(port, this));
        tcplistener.start();
        try {
            tcplistener.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void recordEvent() {
        ts.increment(); 
        /* room for expansion (saving global state) here */
    }

    /* Use to initialize a live node with initial inventory */
    public synchronized void add(String productname, String quantity){
        int intQuantity = Integer.parseInt(quantity);
        inventory.put(productname, intQuantity +
                      (inventory.containsKey(productname) ?
                       inventory.get(productname) : 0));
    }

    /* Notify ALL servers in server_addresses of message msg (requesting CS). */
    public void notifyServers(Message msg) {

        this.enqueue(msg);
        for (TcpListener channel : server_list.values()) {
            channel.enqueue(msg);
        }
        /* TODO: block until all acks are received */
    }

    /* Return true if 'this' is at the head of the priority queue msgq. */
    public boolean isHead() {
        Message head = msgq.peek();
        /* Peek might return null */
        if (head == null) { return false; }
        /* If head is current object, then yes 'this' is head */
        if (head.getSender() == this) { return true; }
        /* Otherwise, no we are not head */
        return false;
    }

    /* Single entry and exit point for associated TcpListener. */
    public Future<String> enqueue(Message msg) {
        switch(msg.type()) {
        case NONE:
            /* message came from Client */
            String cmd = msg.getServerCommand().getCommand();
            ArrayList<String> params = msg.getServerCommand().getParameters();
            /* Operate on the command in the Critical Section */
            CS(dispatch, parameters);
            break;

        case REQUEST:
            /* another Server requesting CS */
            break;

        case RELEASE:
            /* Server currently in CS releases mutex */
            /* Assert: only the head of the priority queue will send a release
             * message; */
            msgq.poll();        /* remove message holding CS */
            break;

        case ACK:
            /* Message is responding to a REQUEST message */
            break;
        }
        return null;
    }

    private void requestCS() {
        recordEvent();          /* new event -- this thread ready for CS */
        Message msg = new Message(this, this.ts, MessageType.REQUEST);
        this.enqueue(msg);
        /* This is a blocking method -- it will wait for all nodes to reply */
        notifyServers(msg);
    }

    private void releaseCS() {
        recordEvent();          /* new event -- CS has ended */
        Message msg = new Message(this, this.ts, MessageType.RELEASE);
        this.enqueue(msg);
        notifyServers(msg);
    }

    public String delta(String dispatch, ArrayList<String> parameters){

        recordEvent();          /* new event -- CS has begun */

        String response = "";
        ListIterator<String> it = parameters.listIterator();
        if("add".equals(dispatch)){
            response =add(it.next(), it.next());
        } else if("purchase".equals(dispatch)) {
            response =purchase(it.next(), it.next(), it.next());
        } else if("cancel".equals(dispatch)) {
            response =cancel(it.next());
        }
        return response;
    }

    public void CS(String dispatch, ArrayList<String> parameters) {
        requestCS();
        /* 1. all replies have been received */
        /* 2. when own request is at head of msgq, enter CS */
        /* TODO: ensure this server is at head of msgq */
        String response = delta(dispatch, parameters);
        /* TODO: respond to sender. when? inside or outside the CS? */
        releaseCS();
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

/** Handler class to dispatch received commands to the singleton Server.
 */
class Handler implements Runnable {

    String[] command;
    Server server;
    Socket tcpsocket;

    /** Create a handler capable of responding over TCP. */
    public Handler(Socket tcpsocket, Server server) {
        this.tcpsocket = tcpsocket;
        this.server = server;
        DataInputStream stdin;
        try {
            stdin = new DataInputStream(tcpsocket.getInputStream());
            String cmd = stdin.readUTF();
            this.command = cmd.trim().split("\\s+", 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Handler's run method, parses received command and relays information to
     * Server.
     */
    @Override
    public void run() {
        try {
            String response = "";
            ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(command[1].split("\\s+")));

            /* TODO: add last argument to enqueue, timestamp */
            Future<String> responseFuture = server.enqueue(command[0], parameters);
            /* blocking call: get */
            respond(String.format("%s\n", response.get().trim()));
        } catch (IOException e) {
            System.err.format("Request aborted: %s", e);
        }
    }

    /** Respond via TCP to the Client that pinged the Server API. */
    private void respond(String message) throws IOException {
        DataOutputStream stdout =
            new DataOutputStream(tcpsocket.getOutputStream());
        stdout.writeUTF(message);
    }
}
