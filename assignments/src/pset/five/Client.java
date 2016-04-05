package pset.five;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/** Client class
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

        if (!(args.length >= 3 && args.length <= 4)) {
            String s = "";
            /* TODO: update to relevant help string */
            s += ("ERROR: Provide 3 arguments\n");
            s += ("\t(1) <hostAddress>: the address of the server\n");
            s += ("\t(2) <tcpPort>: the port number for TCP connection\n");
            s += ("\t(3) <udpPort>: the port number for UDP connection\n");
            s += ("\t<4> <input>: plaintext file containing input commands\n");
            System.out.println(s);
            System.exit(-1);
        }

        numAddresses = Integer.parseInt(args[0]);
        for(int i = 1; i <= numAddresses; i++){
            addresses.add(args[i]);
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

    private static int getPortFromAddress(String str) {
        return Integer.parseInt(str.split(":")[1]);
    }

    private static String getHostFromAddress(String str) {
        return str.split(":")[0];
    }

    /** Send a message to specified address and port via TCP.
     */
    public static String sendTcp(String message, ArrayList<String> addresses){
        int i = 0;
        Socket ssocket = new Socket();
        while(true){
            String strAddress = getHostFromAddress(addresses.get(i));
            int port = getPortFromAddress(addresses.get(i));
            try {
                //TODO: convert to nonblocking socket channel
                ssocket.connect(new InetSocketAddress(strAddress,port), TIMEOUT);
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
