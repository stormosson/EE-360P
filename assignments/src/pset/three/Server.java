package pset.three;

import java.util.Scanner;

import java.util.Arrays;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static int order_nonce = 1;
    private static int[] ports;
    private static Map<String, Integer> inventory = null;
    private static Map<Integer, String> ledger = null;
    private static Map<String, ArrayList<String>> user_orders = null;

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
        ports = new int[] { tcpPort, udpPort };
        String filename = args[2];

        /* Initialize -- parse the inventory file */
        System.out.println(Arrays.toString(args));
        inventory = new ConcurrentHashMap<String, Integer>();
        ledger = new ConcurrentHashMap<Integer, String>();

        Scanner scan = new Scanner(filename);
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
        Thread u = new Thread(new UdpListener(udpPort));
        Thread t = new Thread(new TcpListener(tcpPort));
        u.start();
        t.start();
        u.join();
        t.join();
    }

    public static void respond(String tu, String response) {
        String protocol = tu.toLowerCase();
        if (protocol.startsWith("u")) {
            udpRespond(response);
        } else if (protocol.startsWith("t")) {
            tcpRespond(response);
        }
    }

    public static void udpRespond(String response) {

    }

    public static void tcpRespond(String response) {

    }

    public synchronized static void purchase(String username,
                                             String productname,
                                             String quantity, String tu) {

        if (!inventory.containsKey(productname)) {
            respond(tu, "Not Available - We do not sell this product");
            return;
        }
        /* We do have productname in our database */
        int stock = inventory.get(productname);
        if (stock - Integer.parseInt(quantity) < 0) {
            respond(tu, "Not Available - Not enough items");
            return;
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

        respond(tu, String.format("You order has been placed, %s %s %s %s",
                                  orderid, username, productname, quantity));
    }

    /* Assume: an order will not be canceled more than once */
    public synchronized static void cancel(String orderid, String tu) {

        if (!ledger.containsKey(orderid)) {
            respond(tu, String.format("%s not found, no such order", orderid));
            return;
        }
        /* We recognize the order id, reverse contents of ledger */
        String[] order = ledger.get(orderid).split("\\s+");
        String productname = order[0];
        Integer quantity = Integer.valueOf(order[1]);

        /* remove the order from the ledger so we cannot 'spawn' infinite items
         * through false returns */
        ledger.remove(orderid);

        inventory.put(productname, quantity + inventory.get(productname));
        respond(tu, String.format("Order %s is canceled", orderid));
    }

    /* Assume: canceled orders should still be listed */
    /* Assume: username exists */
    public synchronized static void search(String username, String tu) {

        if (!user_orders.containsKey(username)) {
            respond(tu, String.format("No order found for %s", username));
            return;
        }

        String response = "";
        for (String order : user_orders.get(username)) {
            response += String.format("%s\n", order);
        }
        respond(tu, response);
    }

    public synchronized static void list(String tu) {

        String response = "";
        for (String item : inventory.keySet()) {
            response += String.format("%s %d\n", item, inventory.get(item));
        }
        respond(tu, response);
    }

    private static void printMap(Map<String, Integer> map) {
        System.out.print("{");
        for (String item : map.keySet())
            System.out.print(String.format("<%s,%s> ", item, map.get(item)));
        System.out.println("}");
    }
}

class Handler implements Runnable {

    String[] command;

    UdpHandler(String command) {
        this.command = command.split("\\s+", 2);
    }

    @Override
    public void run() {
        try {
            if (this.command.equals("purchase")) {
                Server.purchase(command[1].split())
            }
            else if (this.command.equals("cancel")) {
                Server.cancel(command[1].split())
            }
            else if (this.command.equals("search")) {
                Server.search(command[1].split())
            }
            else if (this.command.equals("list")) {
                Server.list(command[1].split())
            }
            /* else: raise custom exception */
        } catch (IOException e) {
            System.err.println(String.format("Request aborted: %s", e));
        }
    }
}

class TcpListener implements Runnable {
    
    int port;

    TcpListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try {
            ServerSocket ssocket = new ServerSocket(port);
            while (true) {
                Socket dsocket = ssocket.accept();
                InputStreamReader stdin = 
                    new InputStreamReader(dsocket.getInputStream());
                BufferedReader reader = new BufferedReader(stdin);
                new Thread(new Handler(reader.readLine())).start();
            }
        } catch (IOException e) {
            System.err.println("Server aborted: " + e);
        }
    }
}

class UdpListener implements Runnable {

    int port;
    byte[] buffer = new byte[2048];

    UdpListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try {
            DatagramSocket dsocket = new DatagramSocket(port);
            DatagramPacket dpacket = new DatagramPacket(buffer, buffer.length);
            while (true) {
                dsocket.receive(packet);
                String command = new String(buffer, 0, packet.getLength());
                new Thread(new Handler(command)).start();
                packet.setLength(buffer.length);
            }
        } catch (IOException e) {
            System.err.println(String.format("Request aborted: %s", e));
        }
    }
}
