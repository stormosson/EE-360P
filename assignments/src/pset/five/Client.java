package pset.five;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/** Client class -- used to contact the Server with requests.
 */
public class Client {

    private static DataInputStream stdin;
    private static DataOutputStream stdout;
    private static final int TIMEOUT = 100;

    /** Start a client based on given command line arguments.
     */
    @SuppressWarnings("resource")
    public static void main (String[] args) {

        int numAddresses;
        ArrayList<String> addresses = new ArrayList<String>();

        if (!(args.length == 0 || args.length >= 3)) {
            String s = "";
            /* TODO: update to relevant help string */
            s += ("ERROR: Provide at least 3 arguments:\n");
            s += ("\t(1) <numServers>: the number of servers\n");
            s += ("\t(2) <address>:<portNum> the address of the first server and the port number\n");
            s += ("\t(3) <address>:<portNum> the address of the second server and the port number\n");
            s += ("...\n");
            s += ("\t(n) <address>:<portNum> the address of the nth server and the port number\n");
            System.out.println(s);
            System.exit(-1);
        }
        
        if(args.length != 0){
	        numAddresses = Integer.parseInt(args[0]);
	        for(int i = 1; i <= numAddresses; i++){
	            addresses.add(args[i]);
	        }
        } else {
        	Scanner setupScanner = new Scanner(System.in);
        	numAddresses = Integer.parseInt(setupScanner.nextLine());
        	for(int i = 0; i < numAddresses; i++){
        		addresses.add(setupScanner.nextLine());
        	}
        }

        Scanner sc = null;
        sc = new Scanner(System.in);
        while(sc.hasNextLine()) {

            String nextLine = sc.nextLine();
            String[] tokens = nextLine.split("\\s+");

            if (invalidCommand(tokens[0])) {
                System.out.format("No such command: %s", tokens[0]);
                continue;
            }

            String message = String.format("%s\n", nextLine);
            String response = "";
            response = sendTcp(message, addresses);
            System.out.format("%s", response);
        }
    }

    /* Return true if command is unrecgonized */
    private static boolean invalidCommand(String command) {
        command = command.toLowerCase();
        if (command.equals("purchase"))    { return false; }
        else if (command.equals("cancel")) { return false; }
        else if (command.equals("search")) { return false; }
        else if (command.equals("list"))   { return false; }
        return true;
    }

    /** Send a message to specified address and port via TCP.
     */
    public static String sendTcp(String message, ArrayList<String> addresses){
        int i = 0;
        Socket ssocket = new Socket();
        while(true){
            String strAddress = InetManipulator.getHostFromAddress(addresses.get(i));
            int port = InetManipulator.getPortFromAddress(addresses.get(i));
            try {
                ssocket.connect(new InetSocketAddress(strAddress,port));
                //turn off blocking I/O operations and set timeout
                ssocket.setSoTimeout(TIMEOUT);
                stdout = new DataOutputStream(ssocket.getOutputStream());
                stdin = new DataInputStream(ssocket.getInputStream());
                stdout.writeUTF(message);
                return stdin.readUTF();
            } catch (IOException e1) {
                i %= i + 1;
            }
        }
    }
}
