package pset.three;

import java.util.Scanner;

import java.util.Arrays;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* TODO: javadoc */

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
        try {
            u.join();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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

    /* Assume: an order will not be canceled more than once */
    public synchronized static String cancel(String orderid, String tu) {

        if (!ledger.containsKey(orderid)) {
            return String.format("%s not found, no such order", orderid);
        }
        /* We recognize the order id, reverse contents of ledger */
        String[] order = ledger.get(orderid).split("\\s+");
        String productname = order[0];
        Integer quantity = Integer.valueOf(order[1]);

        /* remove the order from the ledger so we cannot 'spawn' infinite items
         * through false returns */
        ledger.remove(orderid);

        inventory.put(productname, quantity + inventory.get(productname));
        return String.format("Order %s is canceled", orderid);
    }

    /* Assume: canceled orders should still be listed */
    /* Assume: username exists */
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

    public synchronized static String list(String tu) {

        String response = "";
        for (String item : inventory.keySet()) {
            response += String.format("%s %d\n", item, inventory.get(item));
        }
        return response;
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
    boolean udp;
    InetAddress address;
    Integer port;
    Socket tcpsocket;
    DatagramSocket udpsocket;

    public Handler(String command, Socket tcpsocket, DatagramSocket udpsocket, 
               InetAddress return_address, Integer port) {
        this.command = command.split("\\s+", 2);
        this.udp = udpsocket == null;
        this.address = return_address;
        this.port = port;
        this.tcpsocket = tcpsocket;
        this.udpsocket = udpsocket;
    }

    @Override
    public void run() {
        try {
            String response = "";
            String[] args = command[1].split("\\s+");
            /* fuck you java this is so stupid. give me a splat operator! */
            if (this.command.equals("purchase")) {
                response = Server.purchase(args[0], args[1], args[2], args[3]);
            }
            else if (this.command.equals("cancel")) {
                response = Server.cancel(args[0], args[1]);
            }
            else if (this.command.equals("search")) {
                response = Server.search(args[0], args[1]);
            }
            else if (this.command.equals("list")) {
                response = Server.list(args[0]);
            }
            /* else: raise custom exception */
            respond(response);
        } catch (IOException e) {
            System.err.println(String.format("Request aborted: %s", e));
        }
    }

    private void respond(String message) throws IOException {
        if (udp) {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, 
                                                       this.address, port);
            this.udpsocket.send(packet);
        } else {
            DataOutputStream stdout = 
                new DataOutputStream(tcpsocket.getOutputStream());
            stdout.writeBytes(message);
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
                new Thread(new Handler(reader.readLine(), dsocket, null, 
                                       dsocket.getInetAddress(), 
                                       dsocket.getPort())).start();
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
                dsocket.receive(dpacket);
                String command = new String(buffer, 0, dpacket.getLength());
                new Thread(new Handler(command, null, dsocket, 
                                       dpacket.getAddress(), 
                                       dpacket.getPort())).start();
                dpacket.setLength(buffer.length);
            }
        } catch (IOException e) {
            System.err.println(String.format("Request aborted: %s", e));
        }
    }
}
