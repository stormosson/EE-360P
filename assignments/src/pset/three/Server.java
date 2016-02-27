package pset.three;

import java.util.Scanner;

import java.util.Arrays;
import java.util.ArrayList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static int order_nonce = 1;
    private static int[] ports;
    private static Map<String, Integer> inventory = null;
    private static Map<Integer, String> ledger = null;
    private static Map<String, ArrayList<String>> user_orders = null;

    public static void main (String[] args) {
        int tcpPort;
        int udpPort;
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(2) <udpPort>: the port number for UDP connection");
            System.out.println("\t(3) <file>: the file of inventory");
            System.exit(-1);
        }

        tcpPort = Integer.parseInt(args[0]);
        udpPort = Integer.parseInt(args[1]);
        ports = new int[]{tcpPort,udpPort};
        String filename = args[2];

        /* Initialize -- parse the inventory file */
        System.out.println(Arrays.toString(args));
        inventory = new ConcurrentHashMap<String, Integer>();
        ledger = new ConcurrentHashMap<Integer, String>();

        Scanner scan = new Scanner(filename);
        while(scan.hasNextLine()) {
            String[] line = scan.nextLine().split("\\s+");
            if (line.length != 2) {
                continue;
            }
            /* TODO: document assumption that we are adding discrete but
             * matching inventory items */
            String item = line[0];
            Integer quantity = Integer.valueOf(line[1]);
            if (inventory.containsKey(item)) {
                inventory.put(item, inventory.get(item) + quantity);
            } else {
                inventory.put(item, quantity);
            }
        }

        /* Run -- accept incoming requests */
        printMap(inventory);

    }

    public static void respond(String tu, String response) {


    }

    public synchronized static void purchase(String username, String productname,
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
        orders.add(String.format("%s, %s, %s", 
                                 orderid, productname, quantity));

        respond(tu, String.format("You order has been placed, %s %s %s %s",
                                  orderid, username, productname, quantity));
    }

    public synchronized static void cancel(String orderid, String tu) {

        if (!ledger.containsKey(orderid)) {
            respond(tu, String.format("%s not found, no such order", orderid));
            return;
        }
        /* We recognize the order id, reverse contents of ledger */
        String[] order = ledger.get(orderid).split("\\s+");
        String productname = order[0];
        Integer quantity = Integer.valueOf(order[1]);

        inventory.put(productname, quantity + inventory.get(productname));
        respond(tu, String.format("Order %s is canceled", orderid));
    }

    public synchronized static void search(String username, String tu) {
        
        if (!user_orders.containsKey(username)) {
            respond(tu, String.format("No order found for %s", username));
            return;
        }

        String response = "";
        for(String order : user_orders.get(username)) {
            response += String.format("%s\n");
        }
        respond(tu, response);
    }

    public synchronized static void list(String tu) {

        String response = "";
        for(String item : inventory.keySet()) {
            response += String.format("%s %d\n", item, inventory.get(item));
        }
        respond(tu, response);
    }

    private static void printMap(Map<String, Integer> map) {
        System.out.print("{");
        for(String item : map.keySet()) {
            System.out.print("<" + item + "," + map.get(item) + ">, ");
        }
        System.out.println("}");
    }
}
