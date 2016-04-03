package pset.five;

import java.util.Scanner;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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

		serverID = Integer.parseInt(args[0]);
		numServers = Integer.parseInt(args[1]);
		String filename = args[2];
		ArrayList<String> addresses = new ArrayList<String>();
		for (int i = 3; i < args.length - 2; i++) {
			addresses.add(args[i]);
		}
		
		Server initServer = null;
		ArrayList<Thread> servers = new ArrayList<Thread>();
		/* Initialize -- parse the inventory file */
		for(int i = 1; i <= numServers; i++){
			Server s = new Server(i-1, addresses);
			Thread t = new Thread(s);
			t.start();
			servers.add(t);
			if (initServer == null) {
				initServer = s;
			}
		}

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
			String quantity = line[1];
			ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(item, quantity));
			initServer.CS("add", parameters);
			initServer.list();
		}

		/* Run -- accept incoming requests */
		// printMap(inventory);
		
		
	}
}

/**
 * Server class. This server assumes all requests will be valid.
 */
public class Server implements Runnable{

	private int order_nonce = 1;
	private Map<String, Integer> inventory = null;
	private Map<Integer, String> ledger = null;
	private Map<String, ArrayList<String>> user_orders = null;
	private String[] command;
	private InetAddress address;
	private Integer port; //this is the port that the server "this" listens on
	private Socket tcpsocket;

	public Server(int serverID, ArrayList<String> nodes){
		inventory = new ConcurrentHashMap<String, Integer>();
		ledger = new ConcurrentHashMap<Integer, String>();
		user_orders = new ConcurrentHashMap<String, ArrayList<String>>();
		String strPort = nodes.get(serverID).split(":")[1];
		this.port = Integer.parseInt(strPort);
		new Thread(new TcpListener(port, this)).start();
	}
	
	public void requestCS() {

	}

	public void releaseCS() {
		
	}
	
	public void CS(String dispatch, ArrayList<String> parameters) {
		delta(dispatch, parameters);
		releaseCS();
	}
	
	public void delta(String dispatch, ArrayList<String> parameters){
		if("add".equals(dispatch)){
			String productname = parameters.get(0);
			String quantity = parameters.get(1);
			add(productname, quantity);
		} else if("purchase".equals(dispatch)) {
			String username = parameters.get(0);
			String productname = parameters.get(1);
			String quantity = parameters.get(2);
			purchase(username, productname, quantity);
		} else if("cancel".equals(dispatch)) {
			String orderid = parameters.get(0);
			cancel(orderid);
		}
	}
	
	public synchronized void add(String productname, String quantity){
		int intQuantity = Integer.parseInt(quantity);
		inventory.put(productname, intQuantity + (inventory.containsKey(productname) ? inventory.get(productname) : 0));
	}

	/**
	 * Handle a purchase event.
	 */
	public synchronized String purchase(String username, String productname, String quantity) {

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

		return String.format("You order has been placed, %s %s %s %s", orderid, username, productname, quantity);
	}

	/**
	 * Handle an order cancellation.
	 *
	 * Assumes an order will not be canceled more than once.
	 */
	public synchronized String cancel(String orderid) {

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
	public synchronized String search(String username) {

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

	@Override
	public void run() {
		try {

			String response = "";
			String[] args = command[1].split("\\s+");
			/*send lamport message with command and paramters*/
			
			/* else: raise custom exception */
			respond(String.format("%s\n", response.trim()));
		} catch (IOException e) {
			System.err.format("Request aborted: %s", e);
		}
		
	}
	
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

    /** Create a handler capable of responding over TCP/UDP.
     */
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
            String[] args = command[1].split("\\s+");
            
            if (this.command[0].equals("purchase")) {
                response = server.purchase(args[0], args[1], args[2]);
            }
            else if (this.command[0].equals("cancel")) {
                response = server.cancel(args[0]);
            }
            else if (this.command[0].equals("search")) {
                response = server.search(args[0]);
            }
            else if (this.command[0].equals("list")) {
                response = server.list();
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
            DataOutputStream stdout =
                new DataOutputStream(tcpsocket.getOutputStream());
            stdout.writeUTF(message);
    }
}

/**
 * Listener class to accept requests over a TCP connection.
 */
class TcpListener implements Runnable {

	int port;
	Server server;
	ServerSocket ssocket;

	/**
	 * Spawn a TCP listener on specified port.
	 */
	TcpListener(int port, Server server) {
		this.port = port;
		this.server = server;
		try {
			ssocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(String.format("Server aborted: %s", e));
		}
	}

	/**
	 * Listener event loop -- dispatches messages to Handler.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				new Thread(new Handler(ssocket.accept(), server)).start();
			}
		} catch (IOException e) {
			System.err.format("Server aborted: %s", e);
		}
	}
}
