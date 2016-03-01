package pset.three;

import java.util.Scanner;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/** Server class. This server assumes all requests will be valid.
 */
public class Server {

    private static int order_nonce = 1;
    private static Map<String, Integer> inventory = null;
    private static Map<Integer, String> ledger = null;
    private static Map<String, ArrayList<String>> user_orders = null;

    /** Start a server based on given command line arguments. 
     */
    public static void main(String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 3) {
            String s = "";
            s += ("ERROR: Provide 3 arguments\n");
            s += ("\t(1) <tcpPort>: the port number for TCP connection\n");
            s += ("\t(2) <udpPort>: the port number for UDP connection\n");
            s += ("\t(3) <file>: the file of inventory\n");
            System.out.println(s);
            System.exit(-1);
        }

        tcpPort = Integer.parseInt(args[0]);
        udpPort = Integer.parseInt(args[1]);
        String filename = args[2];

        /* Initialize -- parse the inventory file */
        inventory = new ConcurrentHashMap<String, Integer>();
        ledger = new ConcurrentHashMap<Integer, String>();
        user_orders = new ConcurrentHashMap<String, ArrayList<String>>();

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
            /* Assume: nonmatching inventory lines refer to different items */
            String item = line[0];
            Integer quantity = Integer.valueOf(line[1]);
            inventory.put(item, quantity + (inventory.containsKey(item) ? 
                                            inventory.get(item) : 0));
        }

        /* Run -- accept incoming requests */
        //printMap(inventory);
        Thread u = new Thread(new UdpListener(udpPort));
        Thread t = new Thread(new TcpListener(tcpPort));
        u.start();
        t.start();
        try {
            u.join();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Handle a purchase event.
     */
    public synchronized static String purchase(String username,
                                               String productname,
                                               String quantity, String tu) {

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

    /** Handle an order cancellation.
     *
     * Assumes an order will not be canceled more than once.
     */
    public synchronized static String cancel(String orderid, String tu) {

        Integer ordernum = Integer.parseInt(orderid);
        if (!ledger.containsKey(ordernum)) {
            return String.format("%s not found, no such order", ordernum);
        }
        /* We recognize the order id, reverse contents of ledger */
        String[] order = ledger.get(ordernum).split("\\s+");
        String productname = order[0];
        Integer quantity = Integer.valueOf(order[1]);

        /* Remove the order from the ledger so we cannot 'spawn' infinite items
         * through false returns */
        ledger.remove(ordernum);

        inventory.put(productname, quantity + inventory.get(productname));
        return String.format("Order %s is canceled", ordernum);
    }

    /** Handle a search request.
     *
     * Assume canceled orders should still be listed.
     * Assume username exists in our database.
     */
    public synchronized static String search(String username, String tu) {

        if (!user_orders.containsKey(username)) {
            return String.format("No order found for %s", username);
        }

        String response = "";
        for (String order : user_orders.get(username)) {
            response += String.format("%s\n", order);
        }
        return response;
    }

    /** Handle an inventory list query.
     */
    public synchronized static String list(String tu) {

        String response = "";
        for (String item : inventory.keySet()) {
            response += String.format("%s %d\n", item, inventory.get(item));
        }
        return response;
    }

    /** Print a Map object to stdout.
     */
    @SuppressWarnings("unused")
	private static void printMap(Map<Integer, String> map) {
        System.out.print("{");
        for (Integer item : map.keySet())
            System.out.print(String.format("<%s,%s> ", item, map.get(item)));
        System.out.println("}");
    }
}

/** Handler class to dispatch received commands to the singleton Server.
 */
class Handler implements Runnable {

    String[] command;
    boolean udp;
    InetAddress address;
    Integer port;
    Socket tcpsocket;
    DatagramSocket udpsocket;

    /** Create a handler capable of responding over TCP/UDP.
     */
    public Handler(String command, boolean udp, Socket tcpsocket,
                   DatagramSocket udpsocket, InetAddress return_address,
                   Integer port) {

        this.command = command.trim().split("\\s+", 2);
        this.udp = udp;
        this.address = return_address;
        this.port = port;
        this.tcpsocket = tcpsocket;
        this.udpsocket = udpsocket;
    }

    /** Handler's run method, parses received command and relays information to
     * Server.
     */
    @Override
    public void run() {
        try {

            String response = "";
            String[] args = command[1].split("\\s+");
            
            if (this.command[0].equals("purchase")) {
                response = Server.purchase(args[0], args[1], args[2], args[3]);
            }
            else if (this.command[0].equals("cancel")) {
                response = Server.cancel(args[0], args[1]);
            }
            else if (this.command[0].equals("search")) {
                response = Server.search(args[0], args[1]);
            }
            else if (this.command[0].equals("list")) {
                response = Server.list(args[0]);
            }
            /* else: raise custom exception */
            respond(String.format("%s\n", response.trim()));
        } catch (IOException e) {
            System.err.println(String.format("Request aborted: %s", e));
        }
    }

    /** Respond via TCP or UDP to the Client that pinged the Server API.
     */
    private void respond(String message) throws IOException {

        if (udp) {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, 
                                                       this.address, this.port);
            this.udpsocket.send(packet);
        } else {
            DataOutputStream stdout =
                new DataOutputStream(tcpsocket.getOutputStream());
            stdout.writeUTF(message);
        }
    }
}

/** Listener class to accept requests over a TCP connection.
 */
class TcpListener implements Runnable {
    
    int port;
    ServerSocket ssocket;

    /** Spawn a TCP listener on specified port.
     */
    TcpListener(int port){
        this.port = port;
        try{
            ssocket = new ServerSocket(port);
        } catch(IOException e) {
            System.err.println(String.format("Server aborted: %s", e));
        }
    }

    /** Listener event loop -- dispatches messages to Handler.
     */
    @Override
    public void run() {
        try { 
            while (true) {
                Socket dsocket = ssocket.accept();
                DataInputStream stdin = 
                    new DataInputStream(dsocket.getInputStream());
               // DataOutputStream reader = 
                 //   new DataOutputStream(dsocket.getOutputStream());
                String cmd = stdin.readUTF();
                new Thread(new Handler(cmd, false, dsocket, null, 
                                       dsocket.getInetAddress(), 
                                       dsocket.getPort())).start();
            }
        } catch (IOException e) {
            System.err.println(String.format("Server aborted: %s", e));
        }
    }
}

/** Listener class to accept requests over a UDP connection.
 */
class UdpListener implements Runnable {

    int port;
    byte[] buffer = new byte[2048];

    /** Spawn a UDP listener on specified port.
     */
    UdpListener(int port) {
        this.port = port;
    }

    /** Listener event loop -- dispatches messages to Handler.
     */
    @Override
    public void run() {

        try {
            DatagramSocket dsocket = new DatagramSocket(port);
            DatagramPacket dpacket = new DatagramPacket(buffer, buffer.length);
            while (true) {
                dsocket.receive(dpacket);
                String command = new String(buffer, 0, dpacket.getLength());
                new Thread(new Handler(command, true, null, dsocket, 
                                       dpacket.getAddress(), 
                                       dpacket.getPort())).start();
                dpacket.setLength(buffer.length);
            }
        } catch (IOException e) {
            System.err.println(String.format("Server aborted: %s", e));
        }
    }
}
