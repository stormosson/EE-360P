package pset.three;

import java.util.Arrays;
import java.util.Scanner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static int order_nonce = 1;
    private static int[] ports;
    private static Map<String, Integer> inventory = null;

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

    public static void purchase(String username, String product, 
                                String quantity, String tu) {

    }

    public static void cancel(String orderid, String tu) {
        
    }

    public static void search(String username, String tu) {
        
    }

    public static void list(String tu) {
        
    }

    private static void printMap(Map<String, Integer> map) {
        System.out.print("{");
        for(String item : map.keySet()) {
            System.out.print("<" + item + "," + map.get(item) + ">, ");
        }
        System.out.println("}");
    }
}
