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
        

        if (args.length != 3 && args.length != 4) {
            String s = "";
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

            String cmd = sc.nextLine();
            String[] tokens = cmd.split("\\s+");

            if (tokens[0].equals("purchase")) {
            } else if (tokens[0].equals("cancel")) {
            } else if (tokens[0].equals("search")) {
            } else if (tokens[0].equals("list")) {
            } else {
                System.out.println("ERROR: No such command");
            }

         
            String message = String.format("%s\n", cmd);
            String response = "";
            response = sendTcp(message, addresses);
            System.out.print(String.format("%s", response));
        }
    }

    /** Send a message to specified address and port via TCP.
     */
    public static String sendTcp(String message, ArrayList<String> addresses){
    	int i = 0;
    	Socket ssocket = new Socket();
    	while(true){
    		String strAddress = addresses.get(i).split(":")[0];
    		int intPort = Integer.parseInt(addresses.get(i).split(":")[1]);
            try {
            	//TODO: convert to nonblocking socket channel
				ssocket.connect(new InetSocketAddress(strAddress,intPort), TIMEOUT);
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
